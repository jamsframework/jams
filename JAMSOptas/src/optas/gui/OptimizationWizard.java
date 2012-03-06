/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package optas.gui;

import java.awt.Dimension;
import java.io.File;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import org.w3c.dom.Document;
import jams.JAMS;
import jams.JAMSProperties;
import jams.gui.ScrollableMessageDialog;
import jams.gui.tools.GUIHelper;
import jams.tools.XMLTools;
import java.awt.BorderLayout;
import java.awt.Component;
import optas.metamodel.ModelAnalyzer;
import optas.metamodel.ModelModifier.WizardException;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.ExceptionListener;
import java.beans.XMLDecoder;
import java.beans.XMLEncoder;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.SortedSet;
import java.util.TreeSet;
import javax.swing.JButton;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JTabbedPane;
import javax.swing.JToolBar;
import optas.metamodel.AttributeWrapper;
import optas.metamodel.ModelModifier;
import optas.metamodel.ModificationExecutor;
import optas.metamodel.Optimization;
import optas.metamodel.OptimizationDescriptionDocument;
import optas.metamodel.Tools;

/**
 *
 * @author chris
 */
public class OptimizationWizard extends JPanel {

    JTabbedPane mainPane = new JTabbedPane();
    ArrayList<WizardOptimizerPanel> optimizerWizard = new ArrayList<WizardOptimizerPanel>();
    File modelFile, propertyFile;
    Document doc;
    JAMSProperties properties;
    ModelAnalyzer analyzer;
    OptimizationDescriptionDocument scheme;
    Document modifiedModel = null;
    String schemaName = null;
    JFrame owner;

    public OptimizationWizard(Document modelFile, JAMSProperties propertyFile, OptimizationDescriptionDocument scheme, JFrame owner) {
        doc = Tools.preProcessDocument(modelFile);
        this.properties = propertyFile;
        this.scheme = scheme;
        this.owner = owner;
        schemaName = "generic_" + System.currentTimeMillis() + ".odd";
        init();
    }

    public OptimizationWizard(File modelFile, File propertyFile, OptimizationDescriptionDocument scheme, JFrame owner) {
        this.modelFile = modelFile;
        this.propertyFile = propertyFile;
        this.scheme = scheme;
        this.owner = owner;
        schemaName = "generic_" + System.currentTimeMillis() + ".odd";
        init();
    }

    public void setWorkspace(String ws) {
        this.scheme.setWorkspace(ws.replace("\\", "/"));
    }

    public void setRemoveRedundantComponents(boolean b) {
        scheme.setRemoveRedundantComponents(b);
    }

    public boolean isRemoveRedundantComponents() {
        return scheme.isRemoveRedundantComponents();
    }

    public void setRemoveGUIComponents(boolean b) {
        scheme.setRemoveGUIComponents(b);
    }

    public boolean isRemoveGUIComponents() {
        return scheme.isRemoveGUIComponents();
    }

    public Document getNewModel() {
        return modifiedModel;
    }

    private void setData() {
        if (analyzer == null) {
            return;
        }

        for (WizardOptimizerPanel optimizerWizardPanel : optimizerWizard) {
            optimizerWizardPanel.setParameterList(analyzer.getParameters());
            optimizerWizardPanel.setObjectiveList(analyzer.getObjectives());
        }

    }

    private JPanel emptyGUI() {
        JPanel panel = new JPanel();
        JLabel label = new JLabel("Please load a model and a property file!");
        panel.add(label);

        return panel;
    }

    private void init() {
        if (this.modelFile != null && this.propertyFile != null) {
            try {
                analyzer = new ModelAnalyzer(propertyFile, modelFile);
                this.doc = Tools.preProcessDocument(analyzer.getModelDoc());
                this.properties = analyzer.getProperties();
                this.schemaName = modelFile.getName().replaceAll("\\..*", "") + "_" + System.currentTimeMillis() + ".odd";
            } catch (WizardException e) {
                System.out.println(e.toString());
            }
        } else if (this.doc != null && this.properties != null) {
            analyzer = new ModelAnalyzer(properties, doc);
            setData();

        } else {
            removeAll();
            add(emptyGUI());
        }

        mainPane.removeAll();
        this.optimizerWizard.clear();

        if (this.scheme == null) {
            scheme = new OptimizationDescriptionDocument();
            Optimization o = new Optimization();
            scheme.addOptimization(o);
            WizardOptimizerPanel optimizerWizardPanel = new WizardOptimizerPanel(this.owner, o);
            optimizerWizard.add(optimizerWizardPanel);
            mainPane.addTab(optimizerWizardPanel.getDescription().getName(), optimizerWizardPanel);
            setData();
        } else if (analyzer != null) {
            SortedSet<AttributeWrapper> objList = this.analyzer.getObjectives();
            SortedSet<AttributeWrapper> parameterList = new TreeSet<AttributeWrapper>();
            parameterList.addAll(this.analyzer.getParameters());

            scheme.repair(this.doc, parameterList, objList);

            for (Optimization o : scheme.getOptimization()) {
                WizardOptimizerPanel optimizerWizardPanel = new WizardOptimizerPanel(this.owner, o);
                optimizerWizard.add(optimizerWizardPanel);
                mainPane.addTab(o.getName(), optimizerWizardPanel);
            }
        }
        removeAll();
        setData();
        if (owner != null) {
            mainPane.setPreferredSize(new Dimension(this.owner.getSize()));
            setPreferredSize(new Dimension(this.owner.getSize()));
        }
        add(mainPane);
    }

    public static JFrame createDialog(JFrame parent, Document modelFile, JAMSProperties propertyFile, String workspace) {
        JFrame dialog = new JFrame(JAMS.i18n("Optimization_Wizard"));
        OptimizationWizard wizard = new OptimizationWizard(modelFile, propertyFile, null, parent);
        //dialog
        dialog.getContentPane().add(wizard);
        /*wizard.loadPropertiesFile(propertyFile);
        wizard.loadModel(modelFile);*/

        /*wizard.setModel(new File("C:/Arbeit/modeldata/JAMS-Gehlberg/j2k_gehlberg.jam"),
        new File("C:/Arbeit/JAMS/standard.jap"));*/
        //wizard.setModel(modelFile, propertyFile);
        wizard.setWorkspace(workspace);
        dialog.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        dialog.setSize(750, 800);
        //dialog.setMaximumSize(new Dimension(750,700));
        dialog.setPreferredSize(new Dimension(750, 800));
        dialog.setResizable(false);
        dialog.setAlwaysOnTop(false);
        dialog.pack();
        //dialog.setVisible(true);
        return dialog;
    }
//use createFrame instead
    /*public static JFrame createDialog(JFrame parent, File modelFile, File propertyFile) {
    JFrame dialog = new JFrame(JAMS.i18n("Optimization_Wizard"));
    OptimizationWizard wizard = new OptimizationWizard(modelFile, propertyFile, null, dialog);
    dialog.getContentPane().add(wizard);
    wizard.setModel(new File("C:/Arbeit/modeldata/JAMS-Gehlberg/j2k_gehlberg.jam"),
    new File("C:/Arbeit/JAMS/standard.jap"));
    //wizard.setModel(modelFile, propertyFile);
    dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
    dialog.setSize(750, 800);
    //dialog.setMaximumSize(new Dimension(750,700));
    //dialog.setPreferredSize(new Dimension(750,700));
    dialog.setResizable(false);
    dialog.pack();
    dialog.getContentPane().add(new JButton("Ok"){

    @Override
    public void addActionListener(ActionListener l) {
    super.addActionListener(new );
    }

    });
    return dialog;
    }*/

    private void showOutputAttributeConfigurator() {
        WizardOutputPanel wop = new WizardOutputPanel(this.analyzer.getAttributes(), this.scheme.getOutput());
        wop.showDialog(owner);
    }
    boolean schemeLoadingSuccessful = true;

    public void loadScheme(File xmlFile) {
        schemeLoadingSuccessful = true;
        try {
            XMLDecoder encoder = new XMLDecoder(
                    new BufferedInputStream(
                    new FileInputStream(xmlFile)));
            encoder.setExceptionListener(new ExceptionListener() {

                public void exceptionThrown(Exception e) {
                    schemeLoadingSuccessful = false;
                }
            });
            scheme = (OptimizationDescriptionDocument) encoder.readObject();
            encoder.close();
        } catch (IOException ioe) {
            JOptionPane.showMessageDialog(mainPane, "Loading of Optimization Description Document failed." + ioe.toString());
            return;
        }
        if (!schemeLoadingSuccessful) {
            JOptionPane.showMessageDialog(mainPane, "Loading of Optimization Description Document failed."
                    + "You used probably a wrong version\n"
                    + "Current Version:" + OptimizationDescriptionDocument.VERSION + "\n"
                    + "Document Version:" + scheme.getVersion());
            scheme = null;
            return;
        }
        init();
    }

    public void exportScheme(File xmlFile) {
        //this.syncScheme();
        try {
            XMLEncoder encoder = new XMLEncoder(
                    new BufferedOutputStream(
                    new FileOutputStream(xmlFile)));

            encoder.writeObject(scheme);
            encoder.close();
        } catch (IOException ioe) {
            System.out.println("Could not save optimization scheme XML:" + ioe.toString());
            return;
        }
    }

    public void loadPropertiesFile(JAMSProperties propertyFile) {
        this.properties = propertyFile;
        this.propertyFile = null;
        init();
    }

    public void loadPropertiesFile(File properties) {
        this.propertyFile = properties;
        this.properties = null;
        init();
    }

    public void loadModel(File model) {
        this.modelFile = model;
        this.doc = null;
        init();
    }

    public void clear() {
        this.analyzer = null;
        this.doc = null;
        this.mainPane.removeAll();
        this.modelFile = null;
        this.optimizerWizard.clear();
        this.properties = null;
        this.propertyFile = null;
        this.scheme = null;
        this.init();
    }

    //menu actions
    public void openModel() {
        JFileChooser openModelChooser = jams.gui.tools.GUIHelper.getJFileChooser(jams.JAMSFileFilter.getModelFilter());
        int result = openModelChooser.showOpenDialog((Component) OptimizationWizard.this.owner);
        if (result == JFileChooser.APPROVE_OPTION) {
            OptimizationWizard.this.loadModel(openModelChooser.getSelectedFile());
        }
    }

    public void openPropertyFile() {
        JFileChooser openPropertyChooser = jams.gui.tools.GUIHelper.getJFileChooser(jams.JAMSFileFilter.getPropertyFilter());
        int result = openPropertyChooser.showOpenDialog(OptimizationWizard.this.owner);
        if (result == JFileChooser.APPROVE_OPTION) {
            OptimizationWizard.this.loadPropertiesFile(openPropertyChooser.getSelectedFile());
        }
    }

    public void openOptimizationScheme() {
        JFileChooser openSchemeChooser = jams.gui.tools.GUIHelper.getJFileChooser(jams.JAMSFileFilter.getOddFilter());
        int result = openSchemeChooser.showOpenDialog(OptimizationWizard.this.owner);
        if (result == JFileChooser.APPROVE_OPTION) {
            OptimizationWizard.this.loadScheme(openSchemeChooser.getSelectedFile());
        }
    }

    public void launchLocally() {
        if (this.modifiedModel == null) {
            JOptionPane.showMessageDialog(mainPane, "Please create first a modified model!");
            return;
        }
        String fileName = this.getWorkspace() + "/launch" + System.currentTimeMillis() + ".jam";
        exportModifiedModel(new File(fileName));

        //jamsui.launcher.JAMSui.main(new String[]{"-m",fileName,"-c",this.propertyFile.getAbsolutePath(),""});
    }

    public String getWorkspace() {
        return this.scheme.getWorkspace();
    }

    public void addSubOptimization() {
        WizardOptimizerPanel optimizerWizardPanel = new WizardOptimizerPanel(this.owner);
        optimizerWizard.add(optimizerWizardPanel);
        setData();

        mainPane.addTab(JAMS.i18n("Optimizer_Configuration"), optimizerWizardPanel);
    }

    public void exportToXml() {
        JFileChooser openSchemeChooser = jams.gui.tools.GUIHelper.getJFileChooser(jams.JAMSFileFilter.getOddFilter());
        int result = openSchemeChooser.showSaveDialog(OptimizationWizard.this.owner);
        if (result == JFileChooser.APPROVE_OPTION) {
            OptimizationWizard.this.exportScheme(openSchemeChooser.getSelectedFile());
        }
    }

    public boolean modifyModel() {
        try {
            modifiedModel = null;
            ModelModifier modifyModel = new ModelModifier(this.properties, this.doc, System.out);
            modifyModel.setSchemaName(schemaName);
            if (scheme.getWorkspace().isEmpty() || scheme.getWorkspace().equals(" ")) {
                scheme.setWorkspace(analyzer.getRuntime().getModel().getWorkspacePath()); //there should be a check if ws is correct/feasible
            }
            modifyModel.setOptimizationDescriptionDocument(this.scheme);

            ModificationExecutor executor = modifyModel.modifyModel();
            if (executor == null) {
                JOptionPane.showMessageDialog(mainPane, "failed to generate optimization model");
                return false;
            }
            String log = executor.getLog();
            String question = "Would you like to continue? Following modification will be made ..";

            if (ScrollableMessageDialog.showConfirmDialog(this.owner, "Continue?", question, log) == JOptionPane.YES_OPTION) {
                modifiedModel = executor.execute();
                return true;
            }
        } catch (ModelModifier.WizardException we) {
            JOptionPane.showMessageDialog(mainPane, "An error occured during model modification!\n" + we.toString());
            return false;
        }
        return false;
    }

    private void exportModifiedModel(File path) {
        try {
            XMLTools.writeXmlFile(modifiedModel, path);
            String fileName = path.getParent() + "/" + this.schemaName;
            OptimizationWizard.this.exportScheme(new File(fileName));
        } catch (IOException ioe) {
            JOptionPane.showMessageDialog(mainPane, "An error occured during saving the new model\n" + ioe.toString());
        }
    }

    public void exportModifiedModel() {
        if (modifiedModel == null) {
            JOptionPane.showMessageDialog(mainPane, "Please create an optimization model first!");
            return;
        }
        JFileChooser saveXMLChooser = jams.gui.tools.GUIHelper.getJFileChooser(jams.JAMSFileFilter.getXMLFilter());
        saveXMLChooser.setCurrentDirectory(this.analyzer.getRuntime().getModel().getWorkspaceDirectory());

        int result = saveXMLChooser.showSaveDialog(OptimizationWizard.this.owner);
        if (result == JFileChooser.APPROVE_OPTION) {
            exportModifiedModel(saveXMLChooser.getSelectedFile());
        }

    }

    public class OptimizationWizardFrame extends JFrame {

        OptimizationWizard wizard;

        public OptimizationWizardFrame(OptimizationWizard wizard1) {
            super(JAMS.i18n("Optimization_Wizard"));
            this.wizard = wizard1;
            setSize(750, 800);

            getContentPane().add(wizard);

            JToolBar toolbar = new JToolBar("main toolbar");
            JButton modify = new JButton("Create Optimization Model");
            modify.addActionListener(new ActionListener() {

                public void actionPerformed(ActionEvent e) {
                    if (wizard.modifyModel());
                        finish();
                }
            });

            toolbar.add(modify);
            toolbar.setBorderPainted(true);

            JButton launchLocally = new JButton(("Finish & Return to JUICE"));
            launchLocally.addActionListener(new ActionListener() {

                public void actionPerformed(ActionEvent e) {
                    if (wizard.modifyModel()){
                        finish();
                        OptimizationWizardFrame.this.setVisible(false);
                    }
                }
            });
            toolbar.add(modify);
            toolbar.add(launchLocally);
            toolbar.setFloatable(false);

            getContentPane().setLayout(new BorderLayout());
            getContentPane().add(toolbar, BorderLayout.NORTH);

            //toolBar.add(modify);

            JMenuItem newMenu = new JMenuItem("New");
            JMenuItem openModelMenu = new JMenuItem("Open model");
            JMenuItem openPropertyFileMenu = new JMenuItem("Open property file");
            JMenuItem openOptimizationScheme = new JMenuItem("Open scheme");

            newMenu.addActionListener(new ActionListener() {

                public void actionPerformed(ActionEvent e) {
                    wizard.clear();
                }
            });
            openPropertyFileMenu.addActionListener(new ActionListener() {

                public void actionPerformed(ActionEvent e) {
                    wizard.openPropertyFile();
                }
            });
            openModelMenu.addActionListener(new ActionListener() {

                public void actionPerformed(ActionEvent e) {
                    wizard.openModel();
                }
            });

            openOptimizationScheme.addActionListener(new ActionListener() {

                public void actionPerformed(ActionEvent e) {
                    wizard.openOptimizationScheme();
                }
            });

            JMenuItem subOptimization = new JMenuItem("Add sub optimization");
            subOptimization.addActionListener(new ActionListener() {

                public void actionPerformed(ActionEvent e) {
                    wizard.addSubOptimization();
                }
            });


            JMenuItem exitMenu = new JMenuItem("Exit");
            exitMenu.addActionListener(new ActionListener() {

                public void actionPerformed(ActionEvent e) {
                    System.exit(0);
                }
            });

            JMenuBar mainMenu = new JMenuBar();

            JMenu openMenu = new JMenu("Open");
            openMenu.add(openModelMenu);
            openMenu.add(openPropertyFileMenu);
            openMenu.add(openOptimizationScheme);

            JMenu optionsMenu = new JMenu("Options");

            JCheckBoxMenuItem adjustModellingTimeIntervalMenu = new JCheckBoxMenuItem("Adjust Model Time Interval");
            adjustModellingTimeIntervalMenu.setState(wizard.scheme.isAdjustModellTimeInterval());
            adjustModellingTimeIntervalMenu.addActionListener(new ActionListener() {

                public void actionPerformed(ActionEvent e) {
                    boolean isOn = ((JCheckBoxMenuItem) e.getSource()).getState();
                    wizard.scheme.setAdjustModellTimeInterval(isOn);
                }
            });

            JCheckBoxMenuItem removeRedundandComponentsMenu = new JCheckBoxMenuItem("Remove redundant components");
            removeRedundandComponentsMenu.setState(wizard.isRemoveRedundantComponents());
            removeRedundandComponentsMenu.addActionListener(new ActionListener() {

                public void actionPerformed(ActionEvent e) {
                    boolean isOn = ((JCheckBoxMenuItem) e.getSource()).getState();
                    wizard.scheme.setRemoveRedundantComponents(isOn);
                }
            });

            JCheckBoxMenuItem removeGUIComponentsMenu = new JCheckBoxMenuItem("Remove graphical components");
            removeGUIComponentsMenu.setState(wizard.isRemoveGUIComponents());
            removeGUIComponentsMenu.addActionListener(new ActionListener() {

                public void actionPerformed(ActionEvent e) {
                    boolean isOn = ((JCheckBoxMenuItem) e.getSource()).getState();
                    wizard.scheme.setRemoveGUIComponents(isOn);
                }
            });

            JMenuItem changeWorkspace = new JMenuItem("Change Workspace");
            changeWorkspace.addActionListener(new ActionListener() {

                public void actionPerformed(ActionEvent e) {
                    JFileChooser chooser = GUIHelper.getJFileChooser();
                    chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
                    if (chooser.showOpenDialog(OptimizationWizardFrame.this) == JFileChooser.APPROVE_OPTION) {
                        //do workspace check ..
                        wizard.scheme.setWorkspace(chooser.getSelectedFile().getAbsolutePath());
                    }
                }
            });

            JMenuItem setOutputMenu = new JMenuItem("Set Output Attributes");
            setOutputMenu.addActionListener(new ActionListener() {

                public void actionPerformed(ActionEvent e) {
                    wizard.showOutputAttributeConfigurator();
                }
            });

            optionsMenu.add(setOutputMenu);

            optionsMenu.add(adjustModellingTimeIntervalMenu);
            optionsMenu.add(removeRedundandComponentsMenu);
            optionsMenu.add(removeGUIComponentsMenu);
            optionsMenu.add(changeWorkspace);


            JMenu exportMenu = new JMenu("Export");

            JMenuItem exportSchemeMenu = new JMenuItem("Scheme (odd file)");
            exportSchemeMenu.addActionListener(new ActionListener() {

                public void actionPerformed(ActionEvent e) {
                    wizard.exportToXml();
                }
            });

            JMenuItem saveModifiedModelMenu = new JMenuItem("Calibration Model (xml file)");
            saveModifiedModelMenu.addActionListener(new ActionListener() {

                public void actionPerformed(ActionEvent e) {
                    wizard.exportModifiedModel();
                }
            });

            exportMenu.add(exportSchemeMenu);
            exportMenu.add(saveModifiedModelMenu);

            JMenu fileMenu = new JMenu("File");
            fileMenu.add(newMenu);
            fileMenu.add(openMenu);
            fileMenu.add(exportMenu);

            fileMenu.add(exitMenu);

            JMenu editMenu = new JMenu("Edit");
            editMenu.add(subOptimization);

            mainMenu.add(fileMenu);
            mainMenu.add(editMenu);
            mainMenu.add(optionsMenu);

            setJMenuBar(mainMenu);
            getContentPane().add(wizard, BorderLayout.CENTER);
            /*wizard.setModel(new File("C:/Arbeit/modeldata/JAMS-Gehlberg/j2k_gehlberg.jam"),
            new File("C:/Arbeit/JAMS/standard.jap"));*/
            setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
            setMinimumSize(new Dimension(750, 700));
            setResizable(false);
            pack();
        }

        HashSet<ActionListener> listeners = new HashSet<ActionListener>();
        public void addActionListener(ActionListener listener){
            listeners.add(listener);
        }
        public void removeActionListener(ActionListener listener){
            listeners.remove(listener);
        }
        public OptimizationWizard getWizard(){
            return OptimizationWizard.this;
        }
        private void finish(){
            //export odd file
            for (ActionListener l : listeners){
                l.actionPerformed(new ActionEvent(OptimizationWizard.this, 0, "modified model exported"));
                String fileName = OptimizationWizard.this.getWorkspace() + "/launch" + System.currentTimeMillis() + ".jam";
                exportModifiedModel(new File(fileName));
            }
        }
    }



    private OptimizationWizardFrame createFrame() {
        OptimizationWizardFrame wizardFrame = new OptimizationWizardFrame(this);

        return wizardFrame;
    }

    public static OptimizationWizardFrame createFrame(File modelFile, File propertyFile, OptimizationDescriptionDocument scheme) {

        final OptimizationWizard wizard = new OptimizationWizard(modelFile, propertyFile, scheme, null);

        return wizard.createFrame();
    }

    public static OptimizationWizardFrame createFrame(Document modelFile, JAMSProperties propertyFile, OptimizationDescriptionDocument scheme) {

        final OptimizationWizard wizard = new OptimizationWizard(modelFile, propertyFile, scheme, null);

        return wizard.createFrame();
    }

    /*public static void main(String arg[]) {
    JDialog dialog = new JDialog();
    OptimizationWizard wizard = new OptimizationWizard();
    dialog.add(wizard.getPanel());
    wizard.setModel(new File("C:/Arbeit/modeldata/JAMS-Gehlberg/j2k_gehlberg.jam"),
    new File("C:/Arbeit/JAMS/standard.jap"));
    dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
    dialog.setSize(750,700);
    dialog.setResizable(false);
    dialog.setVisible(true);
    }*/
    public static void main(String arg[]) {
        if (arg.length < 1) {
            //System.err.println("error: no arguments");
            createFrame(new File("C:/Arbeit/modeldata/JAMS-Gehlberg/j2k_gehlberg.jam"), new File("C:/Arbeit/JAMS/standard.jap"), null).setVisible(true);
            return;
        }

        if (arg[0].compareTo("analyze") == 0) {
            if (arg.length < 3) {
                System.err.println("error: not enough arguments");
                return;
            }
            File modelFile = new File(arg[1]);
            File propertyFile = new File(arg[2]);
            try {
                ModelAnalyzer.modelAnalyzer(propertyFile, modelFile);
            } catch (WizardException e) {
                System.out.println(e.toString());
            }
        } else if (arg[0].compareTo("modify") == 0) {
            if (arg.length < 5) {
                System.err.println("error: not enough arguments");
                return;
            }
            File modelFile = new File(arg[1]);
            File propertyFile = new File(arg[2]);
            File optimizationSchemeFile = new File(arg[3]);
            File workspace = new File(arg[4]);
            System.setProperty("user.dir", workspace.getAbsolutePath());
            System.getProperty("user.dir");
            try {
                ModelModifier.modelModifier(propertyFile, modelFile, optimizationSchemeFile, workspace);
            } catch (ModelModifier.WizardException e) {
                System.err.println(e.toString());
            }
        } else {
            System.err.println("unknown command: " + arg[0]);
        }
        return;
    }
}
/*
 *
public HashMap<String, String> createOptimizationIni() {
HashMap<String, String> iniMap = new HashMap<String, String>();
//erzeuge property file
Object[] objects = this.parameterList.getSelectedValuesList();
String parameterIDString = "";
String objectiveIDString = "";
String lowerBoundString = "";
String upperBoundString = "";
String startValueString = "";
String objectiveMode = "";
boolean startValueValid = true;
iniMap.put("n", Integer.toString(objects.length));

for (Object obj : objects) {
Parameter p = (Parameter) obj;
parameterIDString += p.extendedtoString2() + ";";
lowerBoundString += p.lowerBound + ";";
upperBoundString += p.upperBound + ";";
if (p.startValueValid) {
startValueString += p.startValue + ";";
} else {
startValueValid = false;
}
}

objects = this.objectiveList.getSelectedValuesList();
for (Object obj : objects) {
String objective = (String) obj;
objectiveIDString += objective + ";";
Integer modeIndex = (Integer) this.objectiveConfigurationPanel.getClientProperty(objective);
Integer mode = new Integer(0);
switch (modeIndex.intValue()) {
case 0:
mode = new Integer(1);
break;
case 1:
mode = new Integer(2);
break;
case 2:
mode = new Integer(4);
break;
case 3:
mode = new Integer(3);
break;
default:
mode = new Integer(1);
break;
}
objectiveMode += mode.toString() + ";";
}

OptimizerDescription desc = (OptimizerDescription) optimizerConfigurationPanel.getClientProperty("optimizer");

iniMap.put("jobMode", "optimizationRun");
iniMap.put("efficiency_modes", objectiveMode);
iniMap.put("efficiencies", objectiveIDString);
iniMap.put("method", Integer.toString(desc.getId()));
iniMap.put("parameters", parameterIDString);
iniMap.put("lowerbounds", lowerBoundString);
iniMap.put("upperbounds", upperBoundString);
if (startValueValid) {
iniMap.put("startvalues", startValueString);
}

for (OptimizerParameter param : desc.propertyMap) {
if (param instanceof NumericOptimizerParameter) {
iniMap.put(param.name, Double.toString(((NumericOptimizerParameter) param).value));
}
if (param instanceof StringOptimizerParameter) {
iniMap.put(param.name, ((StringOptimizerParameter) param).value);
}
if (param instanceof BooleanOptimizerParameter) {
if (((BooleanOptimizerParameter) param).value) {
iniMap.put(param.name, "1");
} else {
iniMap.put(param.name, "0");
}
}
}
if (workspace != null) {
iniMap.put("workspace", workspace);
}
return iniMap;
}
 */
/*
public ActionListener exportModelXML() {
return new ActionListener() {
public void actionPerformed(ActionEvent e) {
HashMap<String, String> ini = createOptimizationIni();
String strIni = "";
Iterator<Entry<String, String>> iter = ini.entrySet().iterator();
if (OptimizationWizard.this.parameterWizard.parameterList.getSelectedIndices().length < 1) {
JOptionPane.showMessageDialog(OptimizationWizard.this.mainPanel, JAMS.i18n("Error_You_should_select_at_least_one_parameter_for_optimization"));
return;
}
if (OptimizationWizard.this.objectiveList.getSelectedIndices().length < 1) {
JOptionPane.showMessageDialog(OptimizationWizard.this.mainPanel, JAMS.i18n("Error_You_should_select_at_least_one_objective_for_optimization"));
return;
}
while (iter.hasNext()) {
Entry<String, String> entry = iter.next();
strIni += entry.getKey() + "=" + entry.getValue() + "\n";
}

try {
newModel = ModelModifier.modelModifier(propertyFile, doc, strIni);
} catch (WizardException we) {
JOptionPane.showMessageDialog(OptimizationWizard.this.mainPanel, we.toString());
return;
}
}
};
}*/
