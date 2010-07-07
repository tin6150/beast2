/*
* File Logger.java
*
* Copyright (C) 2010 Remco Bouckaert remco@cs.auckland.ac.nz
*
* This file is part of BEAST2.
* See the NOTICE file distributed with this work for additional
* information regarding copyright ownership and licensing.
*
* BEAST is free software; you can redistribute it and/or modify
* it under the terms of the GNU Lesser General Public License as
* published by the Free Software Foundation; either version 2
* of the License, or (at your option) any later version.
*
*  BEAST is distributed in the hope that it will be useful,
*  but WITHOUT ANY WARRANTY; without even the implied warranty of
*  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
*  GNU Lesser General Public License for more details.
*
* You should have received a copy of the GNU Lesser General Public
* License along with BEAST; if not, write to the
* Free Software Foundation, Inc., 51 Franklin St, Fifth Floor,
* Boston, MA  02110-1301  USA
*/
package beast.core;


import beast.evolution.tree.Tree;
import beast.util.Randomizer;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;

@Description("Logs results of calculation processes.")
public class Logger extends Plugin implements Loggable {
    final static int COMPOUND_LOGGER = 0, 
//	    PARAMATER_LOGGER = 1, 
	    TREE_LOGGER = 2//, 
//	    LIKELIHOOD_LOGGER = 3, 
//	    OTHER_LOGGER = 4
	    ;

    public Input<List<Plugin>> m_pLoggers = new Input<List<Plugin>>("log", "element in a log", new ArrayList<Plugin>());
//    public Input<RealParameter> m_pParameter = new Input<RealParameter>("parameter", "parameter for logging");
//    public Input<Tree> m_pTree = new Input<Tree>("tree", "beast.tree for logging");
//    public Input<Distribution> m_pLikelihood = new Input<Distribution>("probabilityDistribution", "a (log-transformed) probability (e.g. log-transformed likelihood or prior probability) for logging");
    public Input<Integer> m_pEvery = new Input<Integer>("logEvery", "number of the samples logged", new Integer(1));
    public Input<String> m_pFileName = new Input<String>("fileName", "name of the file, or stdout if left blank");

    /* list of loggers, if any */
    List<Plugin> m_loggers;

    int m_mode = COMPOUND_LOGGER;
    /* integer for parameter number, or beast.tree number*/
    int m_nVarId = -1;
    int m_nEvery = 1;
    /* stream to log to */
    PrintStream m_out;

    long m_nStartLogTime;

    @Override
    public void initAndValidate(State state) throws Exception {
        m_loggers = m_pLoggers.get();
        
        // verify everything is loggable
        for (Plugin plugin: m_loggers) {
        	if (!(plugin instanceof Loggable)) {
        		throw new Exception("Object " + plugin.getClass().getName() + " " + plugin.getID() +" is not loggable");
        	}
        }
        
//        int nInputs = 0;
//        if (m_pLoggers.get().size() > 0) {
            m_mode = COMPOUND_LOGGER;
            if (m_pEvery.get() != null) {
                m_nEvery = m_pEvery.get();
            }
//            nInputs++;
//        }
//        if (m_pParameter.get() != null) {
//            m_nVarId = m_pParameter.get().getIndex(state);
//            m_mode = PARAMATER_LOGGER;
//            //m_pEvery.setValue(null);
//            nInputs++;
//        }
            if (m_loggers.size()==1 && m_loggers.get(0) instanceof Tree) {
                m_mode = TREE_LOGGER;
            }
//        if (m_pTree.get() != null) {
//            m_nVarId = state.getStateNodeIndex(m_pTree.get().getID());
//            m_mode = TREE_LOGGER;
//            m_nEvery = m_pEvery.get();
//            //m_pEvery.setValue(null);
//            nInputs++;
//        }
//        if (m_pLikelihood.get() != null) {
//            m_mode = LIKELIHOOD_LOGGER;
//            //m_pEvery.setValue(null);
//            nInputs++;
//        }
        if (m_loggers.size() == 0) {
            throw new Exception("Logger with nothing to log specified");
        }
        m_nStartLogTime = System.currentTimeMillis();
    }

    public void init(State state) throws Exception {
        String sFileName = m_pFileName.get();
        if (sFileName == null || sFileName.length() == 0) {
            m_out = System.out;
        } else {
            if (sFileName.contains("$(seed)")) {
                sFileName = sFileName.replace("$(seed)", Randomizer.getSeed() + "");
            }
            m_out = new PrintStream(sFileName);
        }
        if (m_mode == COMPOUND_LOGGER) {
            m_out.print("Sample\t");
        }
        for (int i = 0; i < m_loggers.size(); i++) {
             System.out.println("logger " + i);
             ((Loggable)m_loggers.get(i)).init(state, m_out);
        }
        m_out.println();
    } // init

    public void log(int nSample, State state) {
        if ((nSample < 0) || (nSample % m_nEvery > 0)) {
            return;
        }
        if (m_mode == COMPOUND_LOGGER) {
            m_out.print(nSample + "\t");
        }
        for (int i = 0; i < m_loggers.size(); i++) {
        	((Loggable)m_loggers.get(i)).log(nSample, state, m_out);
        }
        if (m_out == System.out) {
            long nLogTime = System.currentTimeMillis();
            int nSecondsPerMSamples = (int) ((nLogTime - m_nStartLogTime) * 1000.0 / (nSample + 1.0));
            String sTimePerMSamples =
                    (nSecondsPerMSamples > 3600 ? nSecondsPerMSamples / 3600 + "h" : "") +
                            (nSecondsPerMSamples > 60 ? (nSecondsPerMSamples % 3600) / 60 + "m" : "") +
                            (nSecondsPerMSamples % 60 + "s");
            m_out.print(sTimePerMSamples + "/Msamples");
        }
        m_out.println();
    } // log

    public void close() {
        for (int i = 0; i < m_loggers.size(); i++) {
        	((Loggable)m_loggers.get(i)).close(m_out);
        }
        // close all file, except stdout
        if (m_pFileName.get() != null && m_pFileName.get() != "") {
            m_out.close();
        }
    } // close



//    void init(State state, PrintStream out) throws Exception {
//        switch (m_mode) {
//            case PARAMATER_LOGGER:
//                int nID = m_nVarId;
//                Parameter<?> var = (Parameter<?>) state.getStateNode(nID);
//                int nValues = var.getDimension();
//                if (nValues == 1) {
//                    out.print(m_pParameter.get().getID() + "\t");
//                } else {
//                    for (int iValue = 0; iValue < nValues; iValue++) {
//                        out.print(m_pParameter.get().getID() + iValue + "\t");
//                    }
//                }
//                break;
//            case TREE_LOGGER:
//                out.println("#NEXUS\n");
//                out.println("Begin trees");
//                Node node = m_pTree.get().getRoot();
//                out.println("\tTranslate");
//                printTranslate(node, out, m_pTree.get().getNodeCount() / 2);
//                out.println(";");
//                break;
//            case LIKELIHOOD_LOGGER:
//                out.print(m_pLikelihood.get().getID() + "\t");
//                break;
//            case OTHER_LOGGER:
//                m_loggers.get(0).init(state, out);
//                break;
//        }
//    } // init


//    void log(int nSample, State state, PrintStream out) {
//        switch (m_mode) {
//            case PARAMATER_LOGGER:
//                int nID = m_nVarId;
//                Parameter<?> var = (Parameter<?>) state.getStateNode(nID);
//                int nValues = var.getDimension();
//                for (int iValue = 0; iValue < nValues; iValue++) {
//                    out.print(var.getValue(iValue) + "\t");
//                }
//                break;
//            case TREE_LOGGER:
//                int iTree = m_nVarId;
//                out.print("beast.tree STATE_" + nSample + " = ");
//                out.print(((Tree) state.getStateNode(iTree)).getRoot().toString());
//                out.println(";");
//                break;
//            case LIKELIHOOD_LOGGER:
//                out.print(m_pLikelihood.get().getCurrentLogP() + "\t");
//                ;
//                break;
//            case OTHER_LOGGER:
//                m_loggers.get(0).log(nSample, state, out);
//                break;
//        }
//    } // log
//
//    void close(PrintStream out) {
//        switch (m_mode) {
//            case PARAMATER_LOGGER:
//                break;
//            case TREE_LOGGER:
//                out.println("End;");
//                break;
//            case LIKELIHOOD_LOGGER:
//                break;
//            case OTHER_LOGGER:
//                break;
//        }
//    } // close

    /** Dummy Loggable interface implementation **/
    @Override public void close(PrintStream out) {}
	@Override public void init(State state, PrintStream out) throws Exception {}
	@Override public void log(int nSample, State state, PrintStream out) {}
}
