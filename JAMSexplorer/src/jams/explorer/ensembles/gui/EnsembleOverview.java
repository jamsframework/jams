/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jams.explorer.ensembles.gui;

import jams.JAMS;
import jams.JAMSFileFilter;
import static jams.explorer.ensembles.gui.ClimateDataOverviewTab.logger;
import jams.explorer.ensembles.implementation.ClimateEnsemble;
import jams.explorer.ensembles.implementation.ClimateModel;
import jams.gui.JAMSLauncher;
import jams.gui.WorkerDlg;
import jams.tools.FileTools;
import jams.tools.XMLTools;
import java.awt.BorderLayout;
import java.awt.KeyEventDispatcher;
import java.awt.KeyboardFocusManager;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JToolBar;
import javax.swing.JTree;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.filechooser.FileFilter;
import javax.swing.tree.TreePath;
import org.w3c.dom.Document;

/**
 *
 * @author christian
 */
public class EnsembleOverview extends JPanel {

    private static final Logger logger = Logger.getLogger(EnsembleOverview.class.getName());

    {
        EnsembleControlPanel.registerLogHandler(logger);
    }
    private ClimateEnsemble dataset;
    private JAMSLauncher launcher;

    private EnsembleTree tree = new EnsembleTree();

    private final JFileChooser jfc = new JFileChooser(new File("").getAbsolutePath());

    private final JButton newEnsembleItem = new JButton(JAMS.getIcon("resources/images/ModelNew.png")),
            openEnsembleItem = new JButton(JAMS.getIcon("resources/images/ModelOpen.png")),
            saveEnsembleItem = new JButton(JAMS.getIcon("resources/images/ModelSave.png")),
            addModelItem = new JButton(JAMS.getIcon("jams/explorer/resources/images/add-icon.png")),
            createBatchFileItem = new JButton(JAMS.getIcon("jams/explorer/resources/images/bat.png")),
            startModelItem = new JButton(JAMS.getIcon("resources/images/ModelRun.png")),
            confModelItem = new JButton(JAMS.getIcon("resources/images/Preferences.png")),
            duplicateModelItem = new JButton(JAMS.getIcon("resources/images/Copy.png")),
            removeModelItem = new JButton(JAMS.getIcon("jams/explorer/resources/images/minus-icon.png"));

    private JToolBar toolbar = new JToolBar();

    ArrayList<EnsembleChangeListener> ensembleChangeListeners = new ArrayList<EnsembleChangeListener>();

    public static interface EnsembleChangeListener {

        public void changed(ClimateEnsemble dataset);
    }

    public EnsembleOverview(ClimateEnsemble dataset, JAMSLauncher launcher) {
        super();
        this.launcher = launcher;

        logger.entering(getClass().getName(), "EnsembleOverview");
        this.dataset = dataset;

        setLayout(new BorderLayout());
        this.add(toolbar, BorderLayout.NORTH);
        this.add(new JScrollPane(tree), BorderLayout.CENTER);

        KeyboardFocusManager.getCurrentKeyboardFocusManager()
                .addKeyEventDispatcher(new KeyEventDispatcher() {
                    @Override
                    public boolean dispatchKeyEvent(KeyEvent e) {
                        if (e.isControlDown() && e.getKeyCode() == KeyEvent.VK_L) {
                            loadEnsembleAction();
                        }
                        if (e.isControlDown() && e.getKeyCode() == KeyEvent.VK_S) {
                            saveEnsembleAction();
                        }
                        return false;
                    }
                });

        init();
        logger.exiting(getClass().getName(), "EnsembleOverview");
    }

    private void init() {
        logger.entering(getClass().getName(), "init");

        toolbar.setFloatable(false);
        toolbar.setRollover(true);

        toolbar.add(newEnsembleItem);
        newEnsembleItem.setToolTipText("Create a new ensemble ..");
        toolbar.add(openEnsembleItem);
        openEnsembleItem.setToolTipText("Load an existing ensemble ..");
        toolbar.add(saveEnsembleItem);
        saveEnsembleItem.setToolTipText("Save ensemble ..");
        toolbar.addSeparator();
        toolbar.add(createBatchFileItem);
        createBatchFileItem.setToolTipText("Create batch file ..");
        toolbar.add(startModelItem);
        startModelItem.setToolTipText("Start the selected model ..");
        toolbar.add(confModelItem);
        confModelItem.setToolTipText("Configure model ..");
        toolbar.addSeparator();
        toolbar.add(addModelItem);
        addModelItem.setToolTipText("Add a model ..");
        toolbar.add(removeModelItem);
        removeModelItem.setToolTipText("Remove selected model ..");
        toolbar.add(duplicateModelItem);
        duplicateModelItem.setToolTipText("Duplicate selected model ..");

        newEnsembleItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                newModelAction();
            }
        });

        addModelItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                WorkerDlg worker = new WorkerDlg(null, "Loading models");
                worker.setTask(new Runnable() {

                    @Override
                    public void run() {
                        try {
                            addModelAction();
                        } catch (Throwable t) {
                            logger.log(Level.SEVERE, "Sorry, I failed to add this model!", t);
                        }
                    }
                });
                worker.execute();
            }
        });

        removeModelItem.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                removeModelAction();
            }
        });

        duplicateModelItem.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                duplicateModelAction();
            }
        });

        startModelItem.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                startModelAction();
            }
        });
        createBatchFileItem.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                createBatchFileAction();
            }
        });

        saveEnsembleItem.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                saveEnsembleAction();
            }
        });

        openEnsembleItem.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                loadEnsembleAction();
            }
        });

        tree.addTreeSelectionListener(new TreeSelectionListener() {

            @Override
            public void valueChanged(TreeSelectionEvent e) {
                TreePath p = e.getNewLeadSelectionPath();
                if (p == null) {
                    setSelectionState(SelectionState.NOTHING_SELETED, null);
                    return;
                }
                Object o = p.getLastPathComponent();
                if (o instanceof ClimateEnsemble.ModelTreeNode) {
                    setSelectionState(SelectionState.MODEL_SELECTED, o);
                } else {
                    setSelectionState(SelectionState.ROOT_SELECTED, o);
                }

            }
        });

        tree.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getButton() == 3 && e.getClickCount() == 1) {
                    TreePath path = tree.getPathForLocation(e.getX(), e.getY());
                    if (path == null) {
                        return;
                    }
                    Object o = path.getLastPathComponent();
                    if (o != null && o instanceof ClimateEnsemble.OutputDirectoryTreeNode) {
                        ClimateEnsemble.OutputDirectoryTreeNode odtn = (ClimateEnsemble.OutputDirectoryTreeNode) o;
                        odtn.getModel().toggleOutputSelection(odtn.getOutputDirectory());
                    }
                    tree.repaint();
                }
            }
        });

        setClimateEnsembleChanged();

        logger.exiting(getClass().getName(), "init");
    }

    public void addEnsembleChangeListener(EnsembleChangeListener listener) {
        this.ensembleChangeListeners.add(listener);
    }

    public boolean removeEnsembleChangeListener(EnsembleChangeListener listener) {
        return this.ensembleChangeListeners.remove(listener);
    }

    enum SelectionState {

        ROOT_SELECTED, MODEL_SELECTED, NOTHING_SELETED
    }

    private void setSelectionState(SelectionState state, Object o) {
        logger.entering(getClass().getName(), "setSelectionState");
        addModelItem.setEnabled(true);
        createBatchFileItem.setEnabled(true);
        startModelItem.setEnabled(true);
        confModelItem.setEnabled(true);
        duplicateModelItem.setEnabled(false);
        removeModelItem.setEnabled(false);

        int n = tree.getSelectionCount();
        if (state == SelectionState.MODEL_SELECTED && n == 1) {
            startModelItem.setEnabled(true);
            duplicateModelItem.setEnabled(true);
            removeModelItem.setEnabled(true);
        }
        if (state != SelectionState.MODEL_SELECTED || n != 1) {
            startModelItem.setEnabled(false);
            duplicateModelItem.setEnabled(false);
            removeModelItem.setEnabled(true);
        }
        if (launcher == null) {
            confModelItem.setEnabled(false);
            startModelItem.setEnabled(false);
        }
        logger.exiting(getClass().getName(), "setSelectionState");
    }

    public ArrayList<ClimateModel> getSelectedClimateModels() {
        logger.entering(getClass().getName(), "getSelectedClimateModels");
        TreePath paths[] = tree.getSelectionPaths();
        ArrayList<ClimateModel> selectedModels = new ArrayList<ClimateModel>();
        if (paths != null) {
            for (TreePath p : paths) {
                Object o = p.getLastPathComponent();
                if (o instanceof ClimateEnsemble.ModelTreeNode) {
                    ClimateEnsemble.ModelTreeNode node = (ClimateEnsemble.ModelTreeNode) o;
                    selectedModels.add(node.getModel());
                }
            }
        }
        logger.exiting(getClass().getName(), "getSelectedClimateModels");
        return selectedModels;
    }

    public void setClimateEnsemble(ClimateEnsemble ce) {
        this.dataset = ce;
        setClimateEnsembleChanged();
    }

    public ClimateEnsemble getClimateEnsemble() {
        return dataset;
    }

    public void setClimateEnsembleChanged() {
        logger.entering(getClass().getName(), "setClimateEnsembleChanged");

        tree.setModel(dataset.getTreeModel());

        for (EnsembleChangeListener listener : ensembleChangeListeners) {
            listener.changed(dataset);
        }

        logger.exiting(getClass().getName(), "setClimateEnsembleChanged");
    }

    public JTree getTree() {
        return tree;
    }

    protected void newModelAction() {
        logger.entering(getClass().getName(), "newModelAction");

        dataset = new ClimateEnsemble("new ensemble");
        setClimateEnsembleChanged();

        logger.exiting(getClass().getName(), "newModelAction");
    }

    protected void addModelAction() {
        logger.entering(getClass().getName(), "addModelAction");

        jfc.setMultiSelectionEnabled(true);
        jfc.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
        jfc.setFileFilter(JAMSFileFilter.getXMLFilter());

        int result = jfc.showOpenDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            File[] files = jfc.getSelectedFiles();
            for (File f : files) {
                if (f.isDirectory()) {
                    File list[] = f.listFiles(new FilenameFilter() {

                        @Override
                        public boolean accept(File dir, String name) {
                            return name.endsWith("jam") || name.endsWith("xml");
                        }
                    });
                    if (list.length > 1) {
                        continue;
                    } else {
                        dataset.addModel(new ClimateModel(0, list[0]));
                    }
                } else {
                    dataset.addModel(new ClimateModel(0, f));
                }
            }
            setClimateEnsembleChanged();
        }

        logger.exiting(getClass().getName(), "addModelAction");
    }

    protected void duplicateModelAction() {
        logger.entering(getClass().getName(), "duplicateModelAction");

        ArrayList<ClimateModel> models = getSelectedClimateModels();
        if (models.size() > 1) {
            logger.log(Level.SEVERE, "Duplicate called for several selected nodes");
            return;
        }
        ClimateModel model = models.get(0);
        int numberOfDuplicates = 0;
        while (true) {
            String result = JOptionPane.showInputDialog(this, "Specify number of duplicates");
            //do nothing
            if (result == null) {
                break;
            }
            try {
                numberOfDuplicates = Integer.parseInt(result);
                if (numberOfDuplicates < 0) {
                    throw new NumberFormatException();
                }
                break;
            } catch (NumberFormatException nfe) {
                JOptionPane.showMessageDialog(tree, "Please enter a valid postive number or zero if you don't want to create any duplicates.");
            }
        }

        if (numberOfDuplicates > 0) {
            try {
                ClimateModel duplicates[] = model.createDuplicates(numberOfDuplicates);
                for (ClimateModel cm : duplicates) {
                    dataset.addModel(cm);
                }
                setClimateEnsembleChanged();
            } catch (IOException ioe) {
                logger.log(Level.SEVERE, "Sorry, I am not able to copy the directory", ioe);
            }
        }
        logger.exiting(getClass().getName(), "duplicateModelAction");
    }

    protected void removeModelAction() {
        logger.entering(getClass().getName(), "removeModelAction");

        ArrayList<ClimateModel> models = getSelectedClimateModels();
        for (ClimateModel model : models) {
            getClimateEnsemble().removeModel(model);
        }
        setClimateEnsembleChanged();

        logger.exiting(getClass().getName(), "removeModelAction");
    }

    protected void deleteModelAction() {
        logger.entering(getClass().getName(), "deleteModelAction");

        ArrayList<ClimateModel> models = getSelectedClimateModels();
        if (models.size() <= 0) {
            return;
        }
        int result = JOptionPane.showConfirmDialog(this, "Really delete %1 models?"
                .replace("%1", Integer.toString(models.size())),
                "Please confirm", JOptionPane.YES_NO_OPTION);
        if (result != JOptionPane.YES_OPTION) {
            return;
        }

        for (ClimateModel model : models) {
            try {
                getClimateEnsemble().deleteModel(model);
            } catch (IOException ioe) {
                if (model.getLocation() != null) {
                    logger.log(Level.SEVERE, "Sorry, I cannot delete the workspace of %1"
                            .replace("%1", model.getLocation().getAbsolutePath()), ioe);
                } else {
                    logger.log(Level.SEVERE, "Sorry, I cannot delete the workspace of an unlocated model!", ioe);
                }
            }
        }
        setClimateEnsembleChanged();

        logger.exiting(getClass().getName(), "deleteModelAction");
    }

    protected void startModelAction() {
        if (launcher == null) {
            logger.severe("Please start the Ensemble Manager from JAMS Launcher to enable model execution.");
            return;
        }
        logger.entering(getClass().getName(), "startModelAction");

        ArrayList<ClimateModel> models = getSelectedClimateModels();
        if (models.size() > 1) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, "Duplicate called for several selected nodes");
            return;
        }
        if (models.size() > 0) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, "Please select the model you want to start!");
            return;
        }
        ClimateModel model = models.get(0);

        File modelFile = model.getModelfile();
        Document doc = XMLTools.getDocument(modelFile.getAbsolutePath());

        JAMSLauncher myLauncher = new JAMSLauncher(launcher, launcher.getProperties(), doc, modelFile) {
            {
                init();
                runModel();
            }
        };
        //launcher.setVisible(true);

        logger.exiting(getClass().getName(), "startModelAction");
    }

    protected void createBatchFileAction() {
        logger.entering(getClass().getName(), "createBatchFileAction");

        ArrayList<ClimateModel> models = getSelectedClimateModels();
        //TODO

        logger.exiting(getClass().getName(), "createBatchFileAction");
    }

    protected void loadEnsembleAction() {
        logger.entering(getClass().getName(), "loadEnsembleAction");

        jfc.setFileFilter(new FileFilter() {

            @Override
            public boolean accept(File f) {
                if (f.isDirectory()) {
                    return true;
                }
                if (f.getAbsolutePath().endsWith(".edf")) {
                    return true;
                }
                return false;
            }

            @Override
            public String getDescription() {
                return "Ensemble Description Files (.edf)";
            }
        });
        jfc.setFileSelectionMode(JFileChooser.FILES_ONLY);

        int result = jfc.showOpenDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            File f = jfc.getSelectedFile();
            try {
                ClimateEnsemble ens = FileTools.deserializeObjectFromXMLwithJAXB(f, ClimateEnsemble.class);
                ens.setBasePath(f.getParentFile());
                setClimateEnsemble(ens);
            } catch (Throwable ex) {
                if (f == null) {
                    logger.log(Level.SEVERE, "Sorry, I am not able to load a null file, ex");
                } else {
                    logger.log(Level.SEVERE, "Sorry, I am not able to load the file %1".replace("%1", f.getAbsolutePath()), ex);
                }
            }
        }
        logger.exiting(getClass().getName(), "loadEnsembleAction");
    }

    protected void saveEnsembleAction() {
        logger.entering(getClass().getName(), "saveEnsembleAction");

        jfc.setFileFilter(new FileFilter() {

            @Override
            public boolean accept(File f) {
                if (f.isDirectory()) {
                    return true;
                }
                if (f.getAbsolutePath().endsWith(".edf")) {
                    return true;
                }
                return false;
            }

            @Override
            public String getDescription() {
                return "Ensemble Description Files (.edf)";
            }
        });
        jfc.setFileSelectionMode(JFileChooser.FILES_ONLY);

        int result = jfc.showSaveDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            File f = jfc.getSelectedFile();
            if (!f.getAbsolutePath().endsWith(".edf")) {
                f = new File(f.getParentFile(), f.getName() + ".edf");
            }
            if (f.exists()) {
                JOptionPane.showConfirmDialog(tree, "%1 already exists! Do you want to replace it?".replace("%1", f.getName()));
            }
            try {
                getClimateEnsemble().relocate(f.getParentFile());
                getClimateEnsemble().save();
                FileTools.serializeObjectToXMLwithJAXB(getClimateEnsemble(), f);

                JOptionPane.showMessageDialog(null, "Ensemble saved!");
            } catch (Exception ex) {
                Logger.getLogger(this.getClass().getName()).log(
                        Level.SEVERE, ex.toString(), ex);
            }
        }
        logger.exiting(getClass().getName(), "saveEnsembleAction");
    }
}
