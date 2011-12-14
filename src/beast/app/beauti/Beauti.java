package beast.app.beauti;


import beast.app.beastapp.BeastVersion;
import beast.app.beauti.BeautiDoc.ActionOnExit;
import beast.app.draw.*;
import beast.app.util.Utils;
import beast.util.AddOnManager;

import jam.framework.Application;
import jam.framework.DocumentFrame;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import com.apple.eawt.ApplicationEvent;

import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.ClipboardOwner;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;


public class Beauti extends JTabbedPane {
	private static final long serialVersionUID = 1L;
	
//    ExtensionFileFilter ef0 = new ExtensionFileFilter(".nex", "Nexus files");
//    ExtensionFileFilter ef1 = new ExtensionFileFilter(".xml", "BEAST files");
	
    /**
     * current directory for opening files *
     */
    public static String g_sDir = System.getProperty("user.dir");
    /**
     * File extension for Beast specifications
     */
    static public final String FILE_EXT = ".xml";

	/** document in document-view pattern. BTW this class is the view */
    BeautiDoc doc;
    JFrame frame;
    
    /** currently selected tab **/
    BeautiPanel currentTab;
    
	boolean [] bPaneIsVisible;
	BeautiPanel [] panels;

	/** menu for switching templates **/
    JMenu templateMenu;
	/** menu for making showing/hiding tabs **/
    JMenu viewMenu;

    
    /** flag indicating beauti is in the process of being set up and panels should not sync with current model **/
    boolean isInitialising = true;
	
	public Beauti(BeautiDoc doc) {
		bPaneIsVisible = new boolean[ BeautiConfig.g_panels.size()];
		Arrays.fill(bPaneIsVisible, true);
		//m_panels = new BeautiPanel[NR_OF_PANELS];
		this.doc = doc;
		this.doc.setBeauti(this);
	}
	
	void setTitle() {
		frame.setTitle("Beauti 2: " + this.doc.sTemplateName + " " + doc.sFileName);
	}
	
	void toggleVisible(int nPanelNr) {
		if (bPaneIsVisible[nPanelNr]) {
			bPaneIsVisible[nPanelNr] = false;
			int nTabNr = tabNrForPanel(nPanelNr);
			removeTabAt(nTabNr);
		} else {
			bPaneIsVisible[nPanelNr] = true;
			int nTabNr = tabNrForPanel(nPanelNr);
				BeautiPanelConfig panel = BeautiConfig.g_panels.get(nPanelNr);
				insertTab(BeautiConfig.getButtonLabel(this, panel.sNameInput.get()), null, panels[nPanelNr], panel.sTipTextInput.get(), nTabNr);
//			}
			setSelectedIndex(nTabNr);
		}
	}
	
	int tabNrForPanel(int nPanelNr) {
		int k = 0;
		for (int i = 0; i < nPanelNr; i++) {
			if (bPaneIsVisible[i]) {
				k++;
			}
		}
		return k;
	}

	
    Action a_new = new ActionNew();
    Action a_load = new ActionLoad();
    Action a_template = new ActionTemplate();
    Action a_addOn = new ActionAddOn();
    Action a_import = new ActionImport();
    Action a_save = new ActionSave();
    Action a_saveas = new ActionSaveAs();
    Action a_quit = new ActionQuit();
    Action a_viewall = new ActionViewAllPanels();
    
    Action a_help = new ActionHelp();
    Action a_citation = new ActionCitation();
    Action a_about = new ActionAbout();
    Action a_viewModel = new ActionViewModel();

    class ActionSave extends MyAction {
        private static final long serialVersionUID = 1;

        public ActionSave() {
            super("Save", "Save Model", "save", "ctrl S");
            setEnabled(false);
        } // c'tor

        public ActionSave(String sName, String sToolTipText, String sIcon,
                          String sAcceleratorKey) {
            super(sName, sToolTipText, sIcon, sAcceleratorKey);
        } // c'tor

        public void actionPerformed(ActionEvent ae) {
            if (!doc.sFileName.equals("")) {
                if (!doc.validateModel()) {
                    return;
                }
                saveFile(doc.sFileName);
//                m_doc.isSaved();
            } else {
                if (saveAs()) {
//                    m_doc.isSaved();
                }
            }
        } // actionPerformed

        
        
    } // class ActionSave
    class ActionSaveAs extends ActionSave {
        /**
         * for serialisation
         */
        private static final long serialVersionUID = -20389110859354L;

        public ActionSaveAs() {
            super("Save As", "Save Model As", "saveas", "");
            setEnabled(false);
        } // c'tor

        public void actionPerformed(ActionEvent ae) {
            saveAs();
        } // actionPerformed
    } // class ActionSaveAs    

    boolean saveAs() {
        if (!doc.validateModel()) {
            return false;
        }
        File file = beast.app.util.Utils.getSaveFile("Save Model As", new File(doc.sFileName), null, (String[]) null);
        if (file != null) {
        	if (file.exists()) {
        		if (JOptionPane.showConfirmDialog(null, 
        				"File " + file.getName() + " already exists. Do you want to overwrite?", "Overwrite file?", 
        				JOptionPane.YES_NO_CANCEL_OPTION) != JOptionPane.YES_OPTION) {
        			return false;
        		}
        	}
            // System.out.println("Saving to file \""+
            // f.getAbsoluteFile().toString()+"\"");
        	doc.sFileName = file.getAbsolutePath();//fc.getSelectedFile().toString();
            if (doc.sFileName.lastIndexOf('/') > 0) {
                g_sDir = doc.sFileName.substring(0, doc.sFileName.lastIndexOf('/'));
            }
            if (!doc.sFileName.endsWith(FILE_EXT))
            	doc.sFileName = doc.sFileName.concat(FILE_EXT);
            saveFile(doc.sFileName);
            setTitle();
            return true;
        }
        return false;
    } // saveAs    

    protected void saveFile(String sFileName) {
        try {
        	if (currentTab != null) {
        		currentTab.config.sync(currentTab.iPartition);
        	} else {
        		panels[0].config.sync(0);
        	}
            doc.save(sFileName);
        } catch (Exception e) {
            e.printStackTrace();
        }
    } // saveFile

    class ActionNew extends MyAction {
        private static final long serialVersionUID = 1;

        public ActionNew() {
            super("New", "Start new analysis", "new", "ctrl N");
        } // c'tor
        
        public void actionPerformed(ActionEvent ae) {
        	doc.newAnalysis();
			a_save.setEnabled(false);
			a_saveas.setEnabled(false);
        }
    }
    
    class ActionLoad extends MyAction {
        private static final long serialVersionUID = 1;

        public ActionLoad() {
            super("Load", "Load Beast File", "open", "ctrl O");
        } // c'tor

        public ActionLoad(String sName, String sToolTipText, String sIcon,
                          String sAcceleratorKey) {
            super(sName, sToolTipText, sIcon, sAcceleratorKey);
        } // c'tor

        public void actionPerformed(ActionEvent ae) {
        	File file = beast.app.util.Utils.getLoadFile("Load Beast XML File", new File(g_sDir), "Beast XML files", "xml");
//    		JFileChooser fileChooser = new JFileChooser(g_sDir);
//    		fileChooser.addChoosableFileFilter(ef1);
//    		fileChooser.setDialogTitle("Load Beast XML File");
//    		if (fileChooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
//        		sFileName = fileChooser.getSelectedFile().toString();
        	if (file != null) {
        		doc.sFileName = file.getAbsolutePath();
                if (doc.sFileName.lastIndexOf('/') > 0) {
                    g_sDir = doc.sFileName.substring(0, doc.sFileName.lastIndexOf('/'));
                }
    			try {
    				doc.loadXML(doc.sFileName);
    				a_save.setEnabled(true);
    				a_saveas.setEnabled(true);
    				setTitle();
    			} catch (Exception e) {
    				e.printStackTrace();
					JOptionPane.showMessageDialog(null, "Something went wrong loading the file: " + e.getMessage());
				}
            }
        } // actionPerformed
    }

    class ActionTemplate extends MyAction {
        private static final long serialVersionUID = 1;

        public ActionTemplate() {
            super("Other Template", "Load Beast Analysis Template From File", "template", "");
        } // c'tor

        public void actionPerformed(ActionEvent ae) {
        	File file = beast.app.util.Utils.getLoadFile("Load Template XML File");
//    		JFileChooser fileChooser = new JFileChooser(System.getProperty("user.dir")+"/templates");
//    		fileChooser.addChoosableFileFilter(ef1);
//    		fileChooser.setDialogTitle("Load Template XML File");
//    		if (fileChooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
//    			String sFileName = fileChooser.getSelectedFile().toString();
        	if (file != null) {
        		String sFileName = file.getAbsolutePath();
    			try {
    				doc.loadNewTemplate(sFileName);
    			} catch (Exception e) {
    				e.printStackTrace();
					JOptionPane.showMessageDialog(null, "Something went wrong loading the template: " + e.getMessage());
				}
            }
        } // actionPerformed
    } // ActionTemplate

    
    class ActionAddOn extends MyAction {
        private static final long serialVersionUID = 1;

        public ActionAddOn() {
            super("Manage Add-ons", "Manage Add-ons", "addon", "");
        } // c'tor

        public void actionPerformed(ActionEvent ae) {
        	JAddOnDialog dlg = new JAddOnDialog(frame);
        	dlg.setVisible(true);
        	// refresh template menu item
        	templateMenu.removeAll();
    		List<AbstractAction> templateActions = getTemplateActions();
    		for (AbstractAction a: templateActions) {
    			templateMenu.add(a);
    		}
    		templateMenu.addSeparator();
    		templateMenu.add(a_template);

        } // actionPerformed
    }

    class ActionImport extends MyAction {
        private static final long serialVersionUID = 1;

        public ActionImport() {
            super("Import Alignment", "Import Alignment File", "import", "ctrl I");
        } // c'tor

        public ActionImport(String sName, String sToolTipText, String sIcon,
                          String sAcceleratorKey) {
            super(sName, sToolTipText, sIcon, sAcceleratorKey);
        } // c'tor

        public void actionPerformed(ActionEvent ae) {
        	
//    		JFileChooser fileChooser = new JFileChooser(g_sDir);
//    		fileChooser.addChoosableFileFilter(ef1);
//    		fileChooser.addChoosableFileFilter(ef0);
//    		fileChooser.setMultiSelectionEnabled(true);
//    		fileChooser.setDialogTitle("Import alignment File");
//
//    		if (fileChooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
			try {
    			File[] files = Utils.getLoadFiles("Import alignment File", new File(g_sDir), "alignment files", "nex","nexus","xml");
    			if (files == null) {
    				return;
    			}
    			for (int i = 0; i < files.length; i++) {
    				String sFileName = files[i].getAbsolutePath();
    				if (sFileName.lastIndexOf('/') > 0) {
    					Beauti.g_sDir = sFileName.substring(0, sFileName.lastIndexOf('/'));
    				}
    				if (sFileName.toLowerCase().endsWith(".nex") || sFileName.toLowerCase().endsWith(".nxs")) {
            			doc.importNexus(sFileName);
    				}
    				if (sFileName.toLowerCase().endsWith(".xml")) {
            			doc.importXMLAlignment(sFileName);
    				}
    			}
				a_save.setEnabled(true);
				a_saveas.setEnabled(true);
			} catch (Exception e) {
				e.printStackTrace();
				JOptionPane.showMessageDialog(null, "Something went wrong importing the alignment: " + e.getMessage());
			}
//            }
        } // actionPerformed
    }
    
    class ActionQuit extends ActionSave {
        /**
         * for serialisation
         */
        private static final long serialVersionUID = -2038911085935515L;

        public ActionQuit() {
            super("Exit", "Exit Program", "exit", "alt F4");
        } // c'tor

        public void actionPerformed(ActionEvent ae) {
//            if (!m_doc.m_bIsSaved) {
        	if (!quit()) {
        		return;
        	}
            System.exit(0);
        }
    } // class ActionQuit

    boolean quit() {
    	if (doc.validateModel()) {
            int result = JOptionPane.showConfirmDialog(null,
                    "Do you want to save the Beast specification?",
                    "Save before closing?",
                    JOptionPane.YES_NO_CANCEL_OPTION);
            System.err.println("result=" + result);
            if (result == JOptionPane.CANCEL_OPTION) {
                return false;
            }
            if (result == JOptionPane.YES_OPTION) {
                if (!saveAs()) {
                    return false;
                }
            }
        }
    	return true;
    }
    
    ViewPanelCheckBoxMenuItem [] m_viewPanelCheckBoxMenuItems;
    
    class ViewPanelCheckBoxMenuItem extends JCheckBoxMenuItem{
		private static final long serialVersionUID = 1L;
		int m_iPanel;
		
    	ViewPanelCheckBoxMenuItem(int iPanel) {
    		super("Show " +	BeautiConfig.g_panels.get(iPanel).sNameInput.get() + " panel", 
    				BeautiConfig.g_panels.get(iPanel).bIsVisibleInput.get());
    		m_iPanel = iPanel;
    		if (m_viewPanelCheckBoxMenuItems == null) {
    			m_viewPanelCheckBoxMenuItems = new ViewPanelCheckBoxMenuItem[ BeautiConfig.g_panels.size()];
    		}
    		m_viewPanelCheckBoxMenuItems[iPanel] = this;
    	} // c'tor
    	
    	void doAction() {
    		toggleVisible(m_iPanel);
    	}
    };

    /** makes all panels visible **/
    class ActionViewAllPanels extends MyAction {
        private static final long serialVersionUID = -1;

        public ActionViewAllPanels() {
            super("View all", "View all panels", "viewall", "");
        } // c'tor

        public void actionPerformed(ActionEvent ae) {
        	for(int nPanelNr = 0; nPanelNr < bPaneIsVisible.length; nPanelNr++) {
        		if (!bPaneIsVisible[nPanelNr]) {
        			toggleVisible(nPanelNr);
        			m_viewPanelCheckBoxMenuItems[nPanelNr].setState(true);
        		}
        	}
        } // actionPerformed
    } // class ActionViewAllPanels

    class ActionAbout extends MyAction {
        private static final long serialVersionUID = -1;

        public ActionAbout() {
            super("About", "Help about", "about", "");
        } // c'tor

        public void actionPerformed(ActionEvent ae) {
        	BeastVersion version = new BeastVersion();
            JOptionPane.showMessageDialog(null, version.getCredits(), "About Beauti 2", JOptionPane.PLAIN_MESSAGE, BeautiPanel.getIcon(0, null));
        }
    } // class ActionAbout

    class ActionHelp extends MyAction {
        private static final long serialVersionUID = -1;

        public ActionHelp() {
            super("Help", "Help on current panel", "help", "");
        } // c'tor

        public void actionPerformed(ActionEvent ae) {
            setCursor(new Cursor(Cursor.WAIT_CURSOR));
            HelpBrowser b = new HelpBrowser(currentTab.config.getType());
            b.setSize(800, 800);
            b.setVisible(true);
            b.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
            setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
        }
    } // class ActionHelp

    class ActionCitation extends MyAction implements ClipboardOwner {
        private static final long serialVersionUID = -1;

        public ActionCitation() {
            super("Citation", "Show appropriate citations and copy to clipboard", "citation", "");
        } // c'tor

        public void actionPerformed(ActionEvent ae) {
        	String sCitations = doc.mcmc.get().getCitations();
        	try {
	            StringSelection stringSelection = new StringSelection( sCitations );
	            Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
	            clipboard.setContents(stringSelection, this);
        	} catch (Exception e) {
        		e.printStackTrace();
			}
            JOptionPane.showMessageDialog(null, sCitations + "\nCitations copied to clipboard", "Citation(s) applicable to this model:", JOptionPane.INFORMATION_MESSAGE);
        	
        } // getCitations

		@Override
		public void lostOwnership(Clipboard clipboard, Transferable contents) {
			// do nothing
		}
     } // class ActionAbout

    class ActionViewModel extends MyAction {
        private static final long serialVersionUID = -1;

        public ActionViewModel() {
            super("View model", "View model graph", "model", "");
        } // c'tor

        public void actionPerformed(ActionEvent ae) {
            JFrame frame = new JFrame("Model Builder");
            ModelBuilder modelBuilder = new ModelBuilder();
            modelBuilder.init();
            frame.add(modelBuilder, BorderLayout.CENTER);
            frame.add(modelBuilder.m_jTbTools2, BorderLayout.NORTH);
            modelBuilder.setEditable(false);
            modelBuilder.m_doc.init(doc.mcmc.get());
            modelBuilder.setDrawingFlag();
            frame.setSize(600, 800);
            frame.setVisible(true);
        }
    } // class ActionViewModel
    
    
    void refreshPanel() {
		try {
			BeautiPanel panel = (BeautiPanel) getSelectedComponent();
			if (panel != null) {
				this.doc.determinePartitions();
				panel.updateList();
				panel.refreshPanel();
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
    }
    
    public JMenuBar makeMenuBar() {
        JMenuBar menuBar = new JMenuBar();
        JMenu fileMenu = new JMenu("File");
        fileMenu.setMnemonic('F');
        menuBar.add(fileMenu);
        fileMenu.add(a_new);
        fileMenu.add(a_load);
        fileMenu.add(a_import);
        fileMenu.addSeparator();
		templateMenu = new JMenu("Template");
		fileMenu.add(templateMenu);
		List<AbstractAction> templateActions = getTemplateActions();
		for (AbstractAction a: templateActions) {
			templateMenu.add(a);
		}
		templateMenu.addSeparator();
		templateMenu.add(a_template);
		fileMenu.add(a_addOn);
        fileMenu.addSeparator();
        fileMenu.add(a_save);
        fileMenu.add(a_saveas);
        if (!Utils.isMac()) {
            fileMenu.addSeparator();
        	fileMenu.add(a_quit);
        }
        
        JMenu modeMenu = new JMenu("Mode");
        menuBar.add(modeMenu);
        modeMenu.setMnemonic('M');
                
		final JCheckBoxMenuItem autoSetClockRate = new JCheckBoxMenuItem("Automatic set clock rate", this.doc.bAutoSetClockRate);
		autoSetClockRate.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ae) {
				doc.bAutoSetClockRate = autoSetClockRate.getState();
				refreshPanel();
			}
		});
		modeMenu.add(autoSetClockRate);

		final JCheckBoxMenuItem muteSound = new JCheckBoxMenuItem("Mute sound", false);
		muteSound.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ae) {
				BeautiPanel.soundIsPlaying = muteSound.getState();
				refreshPanel();
			}
		});
		modeMenu.add(muteSound);
        
        
        viewMenu = new JMenu("View");
        menuBar.add(viewMenu);
        viewMenu.setMnemonic('V');
        setUpViewMenu();
        

        JMenu helpMenu = new JMenu("Help");
        menuBar.add(helpMenu);
        helpMenu.setMnemonic('H');
        helpMenu.add(a_help);
        helpMenu.add(a_citation);
        helpMenu.add(a_viewModel);
        if (!Utils.isMac()) {
        	helpMenu.add(a_about);
        }
    	
    	setMenuVisibiliy("", menuBar);
    	
        return menuBar;
    } // makeMenuBar
	
    
    void setUpViewMenu() {
    	m_viewPanelCheckBoxMenuItems = null;
    	viewMenu.removeAll();
		for (int iPanel = 0; iPanel < BeautiConfig.g_panels.size(); iPanel++) {
        	final ViewPanelCheckBoxMenuItem viewPanelAction = new ViewPanelCheckBoxMenuItem( iPanel);
        	viewPanelAction.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent ae) {
                	viewPanelAction.doAction();
                }
            });
        	viewMenu.add(viewPanelAction);
		}
        viewMenu.addSeparator();
    	viewMenu.add(a_viewall);
	}


	class TemplateAction extends AbstractAction {
		private static final long serialVersionUID = 1L;
		
		String m_sFileName;
		
    	public TemplateAction(File file) {
    		super("xx");
    		m_sFileName = file.getAbsolutePath();
    		String sName = m_sFileName.substring(m_sFileName.lastIndexOf("/") + 1, m_sFileName.length() - 4);
    		putValue(Action.NAME, sName);
    	}

		@Override
		public void actionPerformed(ActionEvent e) {
			try {
				if (JOptionPane.showConfirmDialog(frame, "Changing templates means the information input so far will be lost. " +
						"Are you sure you want to change templates?", "Are you sure?", JOptionPane.YES_NO_CANCEL_OPTION) == 
							JOptionPane.YES_OPTION) {
    				doc.loadNewTemplate(m_sFileName);
				}
			} catch (Exception ex) {
				ex.printStackTrace();
				JOptionPane.showMessageDialog(null, "Something went wrong loading the template: " + ex.getMessage());
			}
		}
    }
    
    private List<AbstractAction> getTemplateActions() {
    	List<AbstractAction> actions = new ArrayList<AbstractAction>();
    	List<String> sBeastDirectories = AddOnManager.getBeastDirectories();
    	for (String sDir : sBeastDirectories) {
    		File dir = new File(sDir + "/templates");
    		getTemplateActionForDir(dir, actions);
    	}
    	return actions;
	}

	private void getTemplateActionForDir(File dir, List<AbstractAction> actions) {
		if (dir.exists() && dir.isDirectory()) {
			File[] files = dir.listFiles();
			if (files != null) {
				for (File template : files) {
					if (template.getName().toLowerCase().endsWith(".xml")) {
						try {
						String sXML2 = BeautiDoc.load(template.getAbsolutePath());
						if (sXML2.contains("<mergepoint ")) {
							actions.add(new TemplateAction(template));
						}
						} catch (Exception e) {
							System.err.println(e.getMessage());
						}
					}
				}
			}
		}
	}

	void setMenuVisibiliy(String sParentName, Component c) {
    	String sName = "";
    	if (c instanceof JMenu) {
    		sName = ((JMenu)c).getText();
    	} else if (c instanceof JMenuItem) {
    		sName = ((JMenuItem)c).getText();
    	}
   		if (sName.length() > 0 && BeautiConfig.menuIsInvisible(sParentName+sName)) {
   			c.setVisible(false);
   		}
       	if (c instanceof JMenu) {
       		for (Component x: ((JMenu)c).getMenuComponents()){
        		setMenuVisibiliy(sParentName + sName + (sName.length() > 0 ? ".":""),  x);
       		}
       	} else if (c instanceof Container) {
        	for (int i =0 ; i < ((Container)c).getComponentCount(); i++) {
        		setMenuVisibiliy(sParentName,  ((Container)c).getComponent(i));
        	}
       	}
   	}

    
    // hide panels as indicated in the hidepanels attribute in the XML template,
    // or use default tabs to hide otherwise.
	void hidePanels() {
//		for (int iPanel = 0; iPanel < BeautiConfig.g_panels.size(); iPanel++) {
//			BeautiPanelConfig panelConfig = BeautiConfig.g_panels.get(iPanel);
//			if (!panelConfig.m_bIsVisibleInput.get()) {
//				toggleVisible(iPanel);
//			}
//		}
	} // hidePanels
	
    void setUpPanels() throws Exception {
    	isInitialising = true;
    	// remove any existing tabs
    	if (getTabCount() > 0) {
	    	while (getTabCount() > 0) {
	    		removeTabAt(0);
	    	}
			bPaneIsVisible = new boolean[ BeautiConfig.g_panels.size()];
			Arrays.fill(bPaneIsVisible, true);
    	}
		for (int iPanel = 0; iPanel < BeautiConfig.g_panels.size(); iPanel++) {
			BeautiPanelConfig panelConfig = BeautiConfig.g_panels.get(iPanel);
			bPaneIsVisible[iPanel] = panelConfig.bIsVisibleInput.get();
		}    		
    	// add panels according to BeautiConfig 
		panels = new BeautiPanel[ BeautiConfig.g_panels.size()];
		for (int iPanel = 0; iPanel < BeautiConfig.g_panels.size(); iPanel++) {
			BeautiPanelConfig panelConfig = BeautiConfig.g_panels.get(iPanel);
			panels[ iPanel] = new BeautiPanel( iPanel, this.doc, panelConfig);
			addTab(BeautiConfig.getButtonLabel(this, panelConfig.getName()), null, panels[ iPanel], panelConfig.getTipText());
		}
		
		for (int iPanel = BeautiConfig.g_panels.size() - 1; iPanel >= 0; iPanel--) {
			if (!bPaneIsVisible[iPanel]) {
				removeTabAt(iPanel);
			}
		}
    	isInitialising = false;
    }

	public static void main(String[] args) {
		try {
			AddOnManager.loadExternalJars();
			Utils.loadUIManager();
			PluginPanel.init();
			
	        BeautiDoc doc = new BeautiDoc();
	        if (doc.parseArgs(args) == ActionOnExit.WRITE_XML) {
               	return;
            }

            boolean lafLoaded = false;
	        if (Utils.isMac()) {
                System.setProperty("apple.awt.graphics.UseQuartz", "true");
                System.setProperty("apple.awt.antialiasing","true");
                System.setProperty("apple.awt.rendering","VALUE_RENDER_QUALITY");

                System.setProperty("apple.laf.useScreenMenuBar","true");
                System.setProperty("com.apple.mrj.application.apple.menu.about.name", "BEAUti 2");
                System.setProperty("apple.awt.draggableWindowBackground","true");
                System.setProperty("apple.awt.showGrowBox","true");

                try {

                    try {
                        // We need to do this using dynamic class loading to avoid other platforms
                        // having to link to this class. If the Quaqua library is not on the classpath
                        // it simply won't be used.
                        Class<?> qm = Class.forName("ch.randelshofer.quaqua.QuaquaManager");
                        Method method = qm.getMethod("setExcludedUIs", Set.class);

                        Set<String> excludes = new HashSet<String>();
                        excludes.add("Button");
                        excludes.add("ToolBar");
                        method.invoke(null, excludes);

                    }
                    catch (Throwable e) {
                    }

                    //set the Quaqua Look and Feel in the UIManager
                    UIManager.setLookAndFeel(
                            "ch.randelshofer.quaqua.QuaquaLookAndFeel"
                    );
                    lafLoaded = true;

                } catch (Exception e) {

                }

                UIManager.put("SystemFont", new Font("Lucida Grande", Font.PLAIN, 13));
                UIManager.put("SmallSystemFont", new Font("Lucida Grande", Font.PLAIN, 11));
                
                
                
            }


            if (!lafLoaded) {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            }
	        
	        final Beauti beauti = new Beauti(doc);

	     if (Utils.isMac()) {
	    	// set up application about-menu for Mac
	            new com.apple.eawt.Application() {
	                {
	                    addApplicationListener(new AboutBoxHandler());
	                }

	                class AboutBoxHandler extends com.apple.eawt.ApplicationAdapter {
	                    public void handleAbout(com.apple.eawt.ApplicationEvent event) {
	                    	beauti.a_about.actionPerformed(null);
	                    	event.setHandled(true);
	                    }
	                
                    @Override
                    public void handleQuit(ApplicationEvent event) {
                    	beauti.a_quit.actionPerformed(null);
                    	event.setHandled(true);
                    }
	                }
	            };
		} 
	        beauti.setUpPanels();
			
			beauti.currentTab = beauti.panels[0];
			beauti.hidePanels();

			beauti.addChangeListener(new ChangeListener() {
				@Override
				public void stateChanged(ChangeEvent e) {
					if (beauti.currentTab == null) {
						beauti.currentTab = beauti.panels[0];
					}
					if (beauti.currentTab != null) {
						if (!beauti.isInitialising) {
							beauti.currentTab.config.sync(beauti.currentTab.iPartition);
						}
						BeautiPanel panel = (BeautiPanel) beauti.getSelectedComponent();
						beauti.currentTab = panel;
						beauti.refreshPanel();
					}
				}
			});
			
			
			
			beauti.setVisible(true);
			beauti.refreshPanel();
			JFrame frame = new JFrame("BEAUti 2: " + doc.sTemplateName + " " + doc.sFileName);
			beauti.frame = frame;
			frame.setIconImage(BeautiPanel.getIcon(0, null).getImage());

			JMenuBar menuBar = beauti.makeMenuBar(); 
			frame.setJMenuBar(menuBar);
			
			if (doc.sFileName != null || doc.alignments.size()> 0) {
				beauti.a_save.setEnabled(true);
				beauti.a_saveas.setEnabled(true);
			}
			
			frame.add(beauti);
	        frame.setSize(1024, 768);
	        frame.setVisible(true);

	        // check file needs to be save on closing main frame
	        frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
			frame.addWindowListener(new WindowAdapter() {
			    public void windowClosing(WindowEvent e) {
		        	if (!beauti.quit()) {
			    		return;
			    	}
		        	System.exit(0);
			    }
			});
		} catch (Exception e) {
			e.printStackTrace();
		}
    } // main

} // class Beauti




