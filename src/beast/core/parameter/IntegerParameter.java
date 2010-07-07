package beast.core.parameter;

import java.io.PrintStream;

import beast.core.Description;
import beast.core.Input;
import beast.core.State;
import beast.core.StateNode;


/**
 * @author Alexei Drummond
 */

@Description("An integer-valued parameter represents a value in the state space that can be changed " +
        "by operators.")
public class IntegerParameter extends Parameter<java.lang.Integer> {
    public Input<Integer> m_pValues = new Input<Integer>("value", "start value for this parameter");
    public Input<Integer> lowerValueInput = new Input<Integer>("lower", "lower value for this parameter");
    public Input<Integer> upperValueInput = new Input<Integer>("upper", "upper value for this parameter");


    public Integer getValue() {
        return values[0];
    }

    @Override
    public void initAndValidate(State state) throws Exception {
        m_fUpper = lowerValueInput.get();
        m_fLower = upperValueInput.get();
        values = new Integer[m_nDimension.get()];
        for (int i = 0; i < values.length; i++) {
            values[i] = m_pValues.get();
        }
    }

    /**
     * deep copy *
     */
    public Parameter<?> copy() {
        Parameter<Integer> copy = new IntegerParameter();
        copy.setID(getID());
        copy.values = new Integer[values.length];
        System.arraycopy(values, 0, copy.values, 0, values.length);
        copy.m_fLower = m_fLower;
        copy.m_fUpper = m_fUpper;
        return copy;
    }

    @Override
    public void log(int nSample, State state, PrintStream out) {
      IntegerParameter var = (IntegerParameter) state.getStateNode(m_sID);
      int nValues = var.getDimension();
      for (int iValue = 0; iValue < nValues; iValue++) {
          out.print(var.getValue(iValue) + "\t");
      }
    }
}
