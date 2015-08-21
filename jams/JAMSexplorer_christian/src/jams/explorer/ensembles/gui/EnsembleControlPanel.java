/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jams.explorer.ensembles.gui;

import jams.JAMS;
import jams.aggregators.Aggregator;
import jams.data.Attribute.TimeInterval;
import jams.data.DefaultDataFactory;
import jams.explorer.ensembles.gui.EnsembleOverview.EnsembleChangeListener;
import jams.explorer.ensembles.implementation.ClimateEnsemble;
import jams.explorer.ensembles.implementation.ClimateEnsemble.ModelTreeNode;
import jams.explorer.ensembles.implementation.ClimateEnsemble.OutputDirectoryTreeNode;
import jams.explorer.ensembles.implementation.ClimateModel;
import jams.explorer.gui.CancelableSwingWorker;
import jams.explorer.gui.CancelableWorkerDlg;
import jams.gui.JAMSLauncher;
import jams.gui.ObserverWorkerDlg;
import jams.gui.input.TimeintervalInput;
import jams.logging.MsgBoxLogHandler;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.DefaultButtonModel;
import javax.swing.GroupLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.JToggleButton;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import javax.swing.UIManager.LookAndFeelInfo;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.filechooser.FileFilter;
import javax.swing.table.TableColumn;
import javax.swing.text.BadLocationException;
import javax.swing.tree.TreePath;

/**
 *
 * @author christian
 */
public class EnsembleControlPanel extends JPanel {

    static final Logger logger = Logger.getLogger(EnsembleControlPanel.class.getName());

    {
        EnsembleControlPanel.registerLogHandler(logger);
    }
    JPanel leftColumn = new JPanel(),
            middleColumn = new JPanel(),
            rightColumn = new JPanel();

    JLabel countLabel = new JLabel("count");

    EnsembleOverview ensembleTree = null;
    ClimateModel currentModel = null;

    JLabel ensembleMemberLabel = new JLabel("Ensemble-Member:");
    JLabel GCMLabel = new JLabel("GCM:");
    JLabel GCMMemberLabel = new JLabel("GCM Member:");
    JLabel RCMMemberLabel = new JLabel("RCM:");
    JLabel ScenarioLabel = new JLabel("Scenario:");
    JLabel TimePeriodLabel = new JLabel("TimePeriod:");
    JLabel FilePräfixLabel = new JLabel("File-Prefix:");
    JLabel LocationLabel = new JLabel("Location:");
    JLabel ShapeFileLabel = new JLabel("Shapefile:");

    JTextField ensembleMemberText = new JTextField(10) {
        {
            setEnabled(false);
        }
    };

    JTextField GCMText = new JTextField(10);
    JTextField GCMMemberText = new JTextField(10);
    JTextField RCMText = new JTextField(10);
    JTextField ScenarioText = new JTextField(10);
    JTextField TimePeriodText = new JTextField(10);
    JTextField FilePräfixText = new JTextField(10);
    JTextField LocationText = new JTextField(10) {
        {
            setEnabled(false);
        }
    };
    JTextField ShapeFileText = new JTextField(10) {
        {
            setEnabled(false);
        }
    };

    JButton selectLocation = new JButton(JAMS.getIcon("jams/explorer/resources/images/folder.png"));
    JButton selectShapeFileLocation = new JButton(JAMS.getIcon("jams/explorer/resources/images/folder.png"));

    JToggleButton showStatistics = new JToggleButton("Show Statistics", false);
    JButton selectAllBn = new JButton("Select All"),
            selectNoneBn = new JButton("Select None"),
            invertSelectionBn = new JButton("Invert Selection"),
            exportToShape = new JButton("Export Statistics");

    TimeintervalInput refPeriodField = new TimeintervalInput(true);
    boolean calculateDiffernce = false;
            
    boolean isShowingStatistics = showStatistics.isSelected();

    JFileChooser jfc = new JFileChooser();

    final static MsgBoxLogHandler myLogHandler = MsgBoxLogHandler.getInstance();

    HashMap<JTextField, String> textFields = new HashMap<JTextField, String>() {
        {
            put(GCMText, ClimateModel.GCM);
            put(GCMMemberText, ClimateModel.GCM_MEMBER);
            put(RCMText, ClimateModel.RCM);
            put(ScenarioText, ClimateModel.SCENARIO);
            put(FilePräfixText, ClimateModel.FILE_PREFIX);
        }
    };

    JTabbedPane statisticsPane = new JTabbedPane();

    List<AbstractClimateDataTab> tabSet = new ArrayList<AbstractClimateDataTab>() {
        {
            add(new ClimateDataOverviewTab("Overview (absolute)"));
            add(new ClimateDataAggregationTab("Average", Aggregator.AggregationMode.AVERAGE, null, EnsembleControlPanel.this));
            add(new ClimateDataAggregationTab("Median", Aggregator.AggregationMode.MEDIAN, null, EnsembleControlPanel.this));
            add(new ClimateDataAggregationTab("Q5", Aggregator.AggregationMode.MEDIAN, 0.05, EnsembleControlPanel.this));
            add(new ClimateDataAggregationTab("Q95", Aggregator.AggregationMode.MEDIAN, 0.95, EnsembleControlPanel.this));
            add(new ClimateDataAggregationTab("Variance", Aggregator.AggregationMode.VARIANCE, null, EnsembleControlPanel.this));
        }
    };

    JAMSLauncher launcher;

    private final EnsembleTable outputTable = new EnsembleTable(null);

    public EnsembleControlPanel() {
        this(null);
    }

    public EnsembleControlPanel(JAMSLauncher launcher) {
        this.launcher = launcher;
        ensembleTree = new EnsembleOverview(new ClimateEnsemble("unbenanntes Ensemble"), launcher);
        init();
        initActions();
    }

    private void init() {
        logger.entering(this.getClass().getName(), "init()");

        GroupLayout mainLayout = new GroupLayout(this);
        this.setLayout(mainLayout);

        JScrollPane tableScroller = new JScrollPane(outputTable);

        JPanel refPeriodPanel = new JPanel(new BorderLayout());
        refPeriodPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.black), "Reference Period"));
        refPeriodField.setEnabled(false);
        refPeriodPanel.add(new JCheckBox(new AbstractAction("Calculate change"){

            @Override
            public void actionPerformed(ActionEvent e) {
                JCheckBox source = ((JCheckBox)e.getSource());
                calculateDiffernce = source.isSelected();
                refPeriodField.setEnabled(calculateDiffernce);
                outputTable.fireSelectionChangeNotification();
            }            
            
        }), BorderLayout.NORTH);
        refPeriodPanel.add(refPeriodField, BorderLayout.CENTER);
        
        mainLayout.setHorizontalGroup(mainLayout.createSequentialGroup()
                .addComponent(leftColumn)
                .addGroup(mainLayout.createParallelGroup()
                        .addComponent(middleColumn)
                        .addGroup(mainLayout.createSequentialGroup()
                                .addComponent(tableScroller)
                                .addGap(5, 10, 15)
                                .addGroup(mainLayout.createParallelGroup()
                                        .addComponent(showStatistics, 300, 325, 350)
                                        .addComponent(selectAllBn, 300, 325, 350)
                                        .addComponent(selectNoneBn, 300, 325, 350)
                                        .addComponent(invertSelectionBn, 300, 325, 350)
                                        .addComponent(exportToShape, 300, 325, 350)
                                        .addComponent(refPeriodPanel, 300, 325, 350)
                                )
                                .addGap(5,10,15)
                        )
                )
        //.addComponent(rightColumn)
        );

        mainLayout.setVerticalGroup(mainLayout.createParallelGroup()
                .addComponent(leftColumn)
                .addGroup(mainLayout.createSequentialGroup()
                        .addComponent(middleColumn)
                        .addGroup(mainLayout.createParallelGroup()
                                .addComponent(tableScroller)
                                .addGroup(mainLayout.createSequentialGroup()
                                        .addComponent(showStatistics)
                                        .addGap(0, 5, 5)
                                        .addComponent(selectAllBn)
                                        .addGap(0, 5, 5)
                                        .addComponent(selectNoneBn)
                                        .addGap(0, 5, 5)
                                        .addComponent(invertSelectionBn)
                                        .addGap(15, 20, 25)
                                        .addComponent(exportToShape)
                                        .addGap(15, 20, 25)
                                        .addComponent(refPeriodPanel)
                                )
                        )
                )
        //.addComponent(rightColumn)
        );

        refPeriodField.setValue("1971-01-01 06:30 2000-12-31 06:30 6 1");
        
        leftColumn.setBorder(BorderFactory.createTitledBorder("Ensembles"));
        leftColumn.setMinimumSize(new Dimension(400, 400));
        leftColumn.setMaximumSize(new Dimension(400, 1200));
        leftColumn.setLayout(new BorderLayout());
        leftColumn.add(ensembleTree, BorderLayout.CENTER);

        JPanel southPanel = new JPanel();
        GroupLayout leftColumnSouthLayout = new GroupLayout(southPanel);
        southPanel.setLayout(leftColumnSouthLayout);
        JSeparator sep = new JSeparator(SwingConstants.HORIZONTAL);
        leftColumnSouthLayout.setHorizontalGroup(leftColumnSouthLayout.createParallelGroup()
                .addComponent(countLabel)
                .addComponent(sep)
        );
        leftColumnSouthLayout.setVerticalGroup(leftColumnSouthLayout.createSequentialGroup()
                .addComponent(countLabel)
                .addComponent(sep)
        );

        leftColumn.add(rightColumn, BorderLayout.SOUTH);

        middleColumn.setBorder(BorderFactory.createTitledBorder("Outputs"));
        middleColumn.setLayout(new BorderLayout());
        middleColumn.add(statisticsPane, BorderLayout.CENTER);

        rightColumn.setBorder(BorderFactory.createTitledBorder("Information"));
        GroupLayout rightLayout = new GroupLayout(rightColumn);
        rightColumn.setLayout(rightLayout);
        rightColumn.setMinimumSize(new Dimension(400, 400));
        rightColumn.setMaximumSize(new Dimension(400, 400));

        for (JTextField f : textFields.keySet()) {
            f.setMaximumSize(new Dimension(350, 25));
        }
        LocationText.setMaximumSize(new Dimension(350, 25));
        ShapeFileText.setMaximumSize(new Dimension(350, 25));
        ensembleMemberText.setMaximumSize(new Dimension(350, 25));

        rightLayout.setHorizontalGroup(rightLayout.createParallelGroup(GroupLayout.Alignment.TRAILING)
                .addGroup(rightLayout.createSequentialGroup()
                        .addComponent(ensembleMemberLabel)
                        .addGap(5, 10, 15)
                        .addComponent(ensembleMemberText)
                )
                .addGap(5, 10, 15)
                .addGroup(rightLayout.createSequentialGroup()
                        .addComponent(GCMLabel)
                        .addGap(5, 10, 15)
                        .addComponent(GCMText)
                )
                .addGap(5, 10, 15)
                .addGroup(rightLayout.createSequentialGroup()
                        .addComponent(GCMMemberLabel)
                        .addGap(5, 10, 15)
                        .addComponent(GCMMemberText)
                )
                .addGap(5, 10, 15)
                .addGroup(rightLayout.createSequentialGroup()
                        .addComponent(RCMMemberLabel)
                        .addGap(5, 10, 15)
                        .addComponent(RCMText)
                )
                .addGap(5, 10, 15)
                .addGroup(rightLayout.createSequentialGroup()
                        .addComponent(ScenarioLabel)
                        .addGap(5, 10, 15)
                        .addComponent(ScenarioText)
                )
                .addGap(5, 10, 15)
                .addGroup(rightLayout.createSequentialGroup()
                        .addComponent(FilePräfixLabel)
                        .addGap(5, 10, 15)
                        .addComponent(FilePräfixText)
                )
                .addGap(5, 10, 15)
                .addGroup(rightLayout.createSequentialGroup()
                        .addComponent(LocationLabel)
                        .addGap(5, 10, 15)
                        .addComponent(LocationText)
                        .addComponent(selectLocation)
                )
                .addGap(5, 10, 15)
                .addGroup(rightLayout.createSequentialGroup()
                        .addComponent(ShapeFileLabel)
                        .addGap(5, 10, 15)
                        .addComponent(ShapeFileText)
                        .addComponent(selectShapeFileLocation)
                )
        );

        rightLayout.setVerticalGroup(rightLayout.createSequentialGroup()
                .addGroup(rightLayout.createParallelGroup(GroupLayout.Alignment.CENTER)
                        .addComponent(ensembleMemberLabel)
                        .addComponent(ensembleMemberText)
                )
                .addGap(5, 10, 15)
                .addGroup(rightLayout.createParallelGroup(GroupLayout.Alignment.CENTER)
                        .addComponent(GCMLabel)
                        .addComponent(GCMText)
                )
                .addGap(5, 10, 15)
                .addGroup(rightLayout.createParallelGroup(GroupLayout.Alignment.CENTER)
                        .addComponent(GCMMemberLabel)
                        .addComponent(GCMMemberText)
                )
                .addGap(5, 10, 15)
                .addGroup(rightLayout.createParallelGroup(GroupLayout.Alignment.CENTER)
                        .addComponent(RCMMemberLabel)
                        .addComponent(RCMText)
                )
                .addGap(5, 10, 15)
                .addGroup(rightLayout.createParallelGroup(GroupLayout.Alignment.CENTER)
                        .addComponent(ScenarioLabel)
                        .addComponent(ScenarioText)
                )
                .addGap(5, 10, 15)
                .addGroup(rightLayout.createParallelGroup(GroupLayout.Alignment.CENTER)
                        .addComponent(FilePräfixLabel)
                        .addComponent(FilePräfixText)
                )
                .addGap(5, 10, 15)
                .addGroup(rightLayout.createParallelGroup(GroupLayout.Alignment.CENTER)
                        .addComponent(LocationLabel)
                        .addComponent(LocationText)
                        .addComponent(selectLocation)
                )
                .addGap(5, 10, 15)
                .addGroup(rightLayout.createParallelGroup(GroupLayout.Alignment.CENTER)
                        .addComponent(ShapeFileLabel)
                        .addComponent(ShapeFileText)
                        .addComponent(selectShapeFileLocation)
                )
        );

        addTabsToPane();
        setEnabledForTextFields(false);

        logger.exiting(this.getClass().getName(), "init()");
    }
    
    private void addTabsToPane(){
        statisticsPane.removeAll();
        for (AbstractClimateDataTab a : tabSet) {
            statisticsPane.addTab(a.getName(), a);
        }
    }

    private class IdentfiableDocumentListener implements DocumentListener {

        String key;

        public IdentfiableDocumentListener(String key) {
            this.key = key;
        }

        @Override
        public void insertUpdate(DocumentEvent e) {
            saveChange(e);
        }

        @Override
        public void removeUpdate(DocumentEvent e) {
            saveChange(e);
        }

        @Override
        public void changedUpdate(DocumentEvent e) {
            saveChange(e);
        }

        public void saveChange(DocumentEvent e) {
            if (currentModel != null) {
                try {
                    int n = e.getDocument().getLength();
                    String s = e.getDocument().getText(0, n);
                    currentModel.setProperty(key, s);

                    if (ensembleTree != null && ensembleTree.getClimateEnsemble() != null) {
                        if (ensembleTree.getClimateEnsemble().getShapeFileTemplate() != null) {
                            exportToShape.setEnabled(true);
                        } else {
                            exportToShape.setEnabled(false);
                        }
                    }
                } catch (BadLocationException ble) {

                }
            }
        }
    };

    private void initActions() {
        logger.entering(this.getClass().getName(), "initActions()");

        outputTable.getSelectionModel().addListSelectionListener(new ListSelectionListener() {

            @Override
            public void valueChanged(ListSelectionEvent e) {
                logger.entering("outputTable->ListSelectionListener", "valueChanged");

                if (e.getValueIsAdjusting()) {
                    return;
                }

                if (outputTable.getSelectedRow() == -1 || !isShowingStatistics) {
                    for (AbstractClimateDataTab tab : tabSet) {
                        tab.setOutput(null);
                    }
                    return;
                }
                for (AbstractClimateDataTab tab : tabSet) {
                    String output = outputTable.getOutput(outputTable.getSelectedRow());
                    if (output != null) {
                        tab.setOutput(output);
                    }
                }
                logger.exiting("outputTable->ListSelectionListener", "valueChanged");
            }
        });

        ensembleTree.getTree().addTreeSelectionListener(new TreeSelectionListener() {

            @Override
            public void valueChanged(TreeSelectionEvent e) {
                logger.entering("ensembleTree->TreeSelectionListener", "valueChanged");

                setCurrentModel(null);
                TreePath p = e.getNewLeadSelectionPath();
                if (p == null) {
                    return;
                }
                Object o = p.getLastPathComponent();
                if (o instanceof ModelTreeNode) {
                    ModelTreeNode mtn = (ModelTreeNode) o;
                    ClimateModel climModel = mtn.getModel();
                    setCurrentModel(climModel);
                }

                if (o instanceof ClimateEnsemble.OutputDirectoryTreeNode) {
                    OutputDirectoryTreeNode odtn = (OutputDirectoryTreeNode) o;
                    setCurrentModel(odtn.getModel());
                }

                if (ensembleTree != null && ensembleTree.getClimateEnsemble() != null) {
                    if (ensembleTree.getClimateEnsemble().getShapeFileTemplate() != null) {
                        exportToShape.setEnabled(true);
                    } else {
                        exportToShape.setEnabled(false);
                    }
                }

                countLabel.setText("count: " + ensembleTree.getClimateEnsemble().getSize());

                logger.exiting("ensembleTree->TreeSelectionListener", "valueChanged");
            }
        });

        ensembleTree.addEnsembleChangeListener(new EnsembleChangeListener() {

            @Override
            public void changed(ClimateEnsemble dataset) {
                changeEnsembleDataset(dataset);
            }

        });

        for (JTextField f : textFields.keySet()) {
            f.getDocument().addDocumentListener(new IdentfiableDocumentListener(
                    textFields.get(f)
            ));
        }

        selectLocation.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                selectLocation();
            }
        });

        selectShapeFileLocation.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                selectShapeFileLocation();
            }
        });

        selectAllBn.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                outputTable.selectAll();
            }
        });

        selectNoneBn.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                outputTable.deselectAll();
            }
        });

        invertSelectionBn.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                outputTable.invertSelection();
            }
        });

        showStatistics.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                toggleStatisticsAction();
            }
        });
        for (ActionListener a : ((DefaultButtonModel) showStatistics.getModel()).getActionListeners()) {
            a.actionPerformed(new ActionEvent(showStatistics, 0, "init"));
        }

        exportToShape.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                exportToShapeAction();
            }
        });

        logger.exiting(this.getClass().getName(), "initActions()");
    }

    public TimeInterval getRefPeriod(){
        if (calculateDiffernce){
            return this.refPeriodField.getTimeInterval();
        }
        return null;
    }
    
    private void changeEnsembleDataset(ClimateEnsemble dataset) {
        logger.entering(getClass().getName(), "changeEnsembleDataset");

        tabSet.clear();
        
        tabSet.add(new ClimateDataOverviewTab("Overview (absolute)"));
        tabSet.add(new ClimateDataAggregationTab("Average", Aggregator.AggregationMode.AVERAGE, null, EnsembleControlPanel.this));
        tabSet.add(new ClimateDataAggregationTab("Median", Aggregator.AggregationMode.MEDIAN, null, EnsembleControlPanel.this));
        tabSet.add(new ClimateDataAggregationTab("Q5", Aggregator.AggregationMode.MEDIAN, 0.05, EnsembleControlPanel.this));
        tabSet.add(new ClimateDataAggregationTab("Q95", Aggregator.AggregationMode.MEDIAN, 0.95, EnsembleControlPanel.this));
        tabSet.add(new ClimateDataAggregationTab("Variance", Aggregator.AggregationMode.VARIANCE, null, EnsembleControlPanel.this));
        
        int i=0;
        for (ClimateModel clm : dataset.getModelSet()){
            tabSet.add(new ClimateDataAggregationTab(clm.toString(), Aggregator.AggregationMode.INDEX, (double)i, EnsembleControlPanel.this));
            i++;
        }
                
        addTabsToPane();        
        
        for (AbstractClimateDataTab a : tabSet) {
            a.setClimateEnsemble(dataset);
        }
        outputTable.setEnsemble(dataset);

        Enumeration<TableColumn> e = outputTable.getColumnModel().getColumns();
        try {
            e.nextElement().setMaxWidth(20);
            e.nextElement().setMinWidth(40);
            e.nextElement().setMinWidth(60);
            e.nextElement().setMinWidth(80);
            e.nextElement().setMinWidth(20);
        } catch (NoSuchElementException nsee) {
            logger.log(Level.SEVERE, "Error during layout of table", nsee);
        }

        logger.exiting(getClass().getName(), "changeEnsembleDataset");
    }

    private void toggleStatisticsAction() {
        logger.entering(getClass().getName(), "toggleStatisticsAction");

        if (showStatistics.isSelected()) {
            showStatistics.setText("Statistics are ON");
            showStatistics.setBackground(new Color(128, 255, 128));
        } else {
            showStatistics.setText("Statistics are OFF");
            showStatistics.setBackground(new Color(255, 128, 128));
        }
        isShowingStatistics = showStatistics.isSelected();
        outputTable.fireSelectionChangeNotification();

        logger.exiting(getClass().getName(), "toggleStatisticsAction");
    }

    private class ExportWorker extends ObserverWorkerDlg {

        File targetDir = null;

        public ExportWorker() {
            super(new CancelableWorkerDlg(null, "I am busy"));
            setTask();
            this.getWorkerDlg().setSize(600, 120);
        }

        public void setTargetDir(File dir) {
            this.targetDir = dir;
        }

        private void setTask() {
            ((CancelableWorkerDlg) this.getWorkerDlg()).setTask(new CancelableSwingWorker() {

                @Override
                protected Object doInBackground() throws Exception {
                    logger.entering("EnsembleControlPanel$CancelableWorkerDlg", "doInBackground");

                    ClimateEnsemble ensemble = ensembleTree.getClimateEnsemble();

                    for (String selectedOutput : outputTable.getSelectedOutputs()) {
                        ExportWorker.this.update(null, "<html><div align=\"center\">Exporting</div><br><div align=\"center\">Average of " + selectedOutput + "</div></html>");
                        try {
                            ensemble.aggregateEnsembleToFile(targetDir, selectedOutput, Aggregator.AggregationMode.AVERAGE, null, getRefPeriod());
                        } catch (Throwable ioe) {
                            if (selectedOutput == null) {
                                logger.log(Level.SEVERE, "Sorry, I failed to save a dataset, since it is null", ioe);
                            }
                            if (targetDir == null) {
                                logger.log(Level.SEVERE, "Sorry, I failed to save the dataset %1, since the target is null".replace("%1", selectedOutput), ioe);
                            } else {
                                logger.log(Level.SEVERE, "Sorry, I failed to save the dataset %1 to the shapefile %2".replace("%1", selectedOutput).replace("%2", targetDir.getAbsolutePath()), ioe);
                            }
                        }
                    }

                    for (String selectedOutput : outputTable.getSelectedOutputs()) {
                        ExportWorker.this.update(null, "<html><div align=\"center\">Exporting</div><br><div align=\"center\">Median of " + selectedOutput + "</div></html>");
                        try {
                            ensemble.aggregateEnsembleToFile(targetDir, selectedOutput, Aggregator.AggregationMode.MEDIAN, null, getRefPeriod());
                        } catch (Throwable ioe) {
                            if (selectedOutput == null) {
                                logger.log(Level.SEVERE, "Sorry, I failed to save a dataset, since it is null", ioe);
                            }
                            if (targetDir == null) {
                                logger.log(Level.SEVERE, "Sorry, I failed to save the dataset %1, since the target is null".replace("%1", selectedOutput), ioe);
                            } else {
                                logger.log(Level.SEVERE, "Sorry, I failed to save the dataset %1 to the shapefile %2".replace("%1", selectedOutput).replace("%2", targetDir.getAbsolutePath()), ioe);
                            }
                        }
                    }

                    for (String selectedOutput : outputTable.getSelectedOutputs()) {
                        ExportWorker.this.update(null, "<html><div align=\"center\">Exporting</div><br><div align=\"center\">Q5 of " + selectedOutput + "</div></html>");
                        try {
                            ensemble.aggregateEnsembleToFile(targetDir, selectedOutput, Aggregator.AggregationMode.MEDIAN, 0.05, getRefPeriod());
                        } catch (Throwable ioe) {
                            if (selectedOutput == null) {
                                logger.log(Level.SEVERE, "Sorry, I failed to save a dataset, since it is null", ioe);
                            }
                            if (targetDir == null) {
                                logger.log(Level.SEVERE, "Sorry, I failed to save the dataset %1, since the target is null".replace("%1", selectedOutput), ioe);
                            } else {
                                logger.log(Level.SEVERE, "Sorry, I failed to save the dataset %1 to the shapefile %2".replace("%1", selectedOutput).replace("%2", targetDir.getAbsolutePath()), ioe);
                            }
                        }
                    }

                    for (String selectedOutput : outputTable.getSelectedOutputs()) {
                        ExportWorker.this.update(null, "<html><div align=\"center\">Exporting</div><br><div align=\"center\">Q95 of " + selectedOutput + "</div></html>");
                        try {
                            ensemble.aggregateEnsembleToFile(targetDir, selectedOutput, Aggregator.AggregationMode.MEDIAN, 0.95, getRefPeriod());
                        } catch (Throwable ioe) {
                            if (selectedOutput == null) {
                                logger.log(Level.SEVERE, "Sorry, I failed to save a dataset, since it is null", ioe);
                            }
                            if (targetDir == null) {
                                logger.log(Level.SEVERE, "Sorry, I failed to save the dataset %1, since the target is null".replace("%1", selectedOutput), ioe);
                            } else {
                                logger.log(Level.SEVERE, "Sorry, I failed to save the dataset %1 to the shapefile %2".replace("%1", selectedOutput).replace("%2", targetDir.getAbsolutePath()), ioe);
                            }
                        }
                    }

                    for (String selectedOutput : outputTable.getSelectedOutputs()) {
                        ExportWorker.this.update(null, "<html><div align=\"center\">Exporting</div><br><div align=\"center\">Variance of " + selectedOutput + "</div></html>");
                        try {
                            ensemble.aggregateEnsembleToFile(targetDir, selectedOutput, Aggregator.AggregationMode.VARIANCE, null, getRefPeriod());
                        } catch (Throwable ioe) {
                            if (selectedOutput == null) {
                                logger.log(Level.SEVERE, "Sorry, I failed to save a dataset, since it is null", ioe);
                            }
                            if (targetDir == null) {
                                logger.log(Level.SEVERE, "Sorry, I failed to save the dataset %1, since the target is null".replace("%1", selectedOutput), ioe);
                            } else {
                                logger.log(Level.SEVERE, "Sorry, I failed to save the dataset %1 to the shapefile %2".replace("%1", selectedOutput).replace("%2", targetDir.getAbsolutePath()), ioe);
                            }
                        }
                    }

                    logger.exiting("EnsembleControlPanel$CancelableWorkerDlg", "doInBackground");

                    return null;
                }

                @Override
                public int cancel() {
                    logger.info("EnsembleControlPanel$CancelableWorkerDlg is canceled");
                    return super.cancel(true) ? 1 : 0;
                }
            });
        }
    }

    private void exportToShapeAction() {
        if (ensembleTree.getClimateEnsemble() == null) {
            return;
        }

        logger.entering(getClass().getName(), "exportToShapeAction");

        this.jfc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        int result = jfc.showSaveDialog(EnsembleControlPanel.this);
        if (result == JFileChooser.APPROVE_OPTION) {
            File targetDir = jfc.getSelectedFile();

            ExportWorker worker = new ExportWorker();
            worker.setTargetDir(targetDir);
            worker.getWorkerDlg().execute();
        };
        logger.exiting(getClass().getName(), "exportToShapeAction");
    }

    private void selectLocation() {
        logger.entering(getClass().getName(), "selectLocation");

        jfc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        int result = jfc.showOpenDialog(EnsembleControlPanel.this);
        if (result == JFileChooser.APPROVE_OPTION) {
            File f = jfc.getSelectedFile();
            ((ClimateModel) this.currentModel).setLocation(f);
        }

        logger.exiting(getClass().getName(), "selectLocation");
    }

    private void selectShapeFileLocation() {
        logger.entering(getClass().getName(), "selectShapeFileLocation");

        jfc.setFileSelectionMode(JFileChooser.FILES_ONLY);
        jfc.setFileFilter(new FileFilter() {

            @Override
            public boolean accept(File f) {
                if (f.isDirectory() || f.getName().endsWith(".shp")) {
                    return true;
                }
                return false;
            }

            @Override
            public String getDescription() {
                return "Shapefile (*.shp)";
            }
        });
        int result = jfc.showOpenDialog(EnsembleControlPanel.this);
        if (result == JFileChooser.APPROVE_OPTION) {
            File f = jfc.getSelectedFile();
            ensembleTree.getClimateEnsemble().setShapeFileTemplate(f);
        }

        logger.exiting(getClass().getName(), "selectShapeFileLocation");
    }

    protected void setCurrentModel(ClimateModel model) {
        logger.entering(getClass().getName(), "setCurrentModel");

        this.currentModel = model;
        setEnabledForTextFields(model != null);
        if (model == null) {
            this.FilePräfixText.setText("");
            this.GCMMemberText.setText("");
            this.LocationText.setText("");
            this.RCMText.setText("");
            this.ScenarioText.setText("");
            this.TimePeriodText.setText("");
            this.ensembleMemberText.setText("");
            this.GCMText.setText("");
        } else {

            this.FilePräfixText.setText(model.getFilePrefix());
            this.GCMMemberText.setText(model.getGCMMember());
            if (model.getLocation() != null) {
                File f = model.getLocation();
                this.LocationText.setText(f.getName());
            } else {
                this.LocationText.setText("");
            }

            if (ensembleTree.getClimateEnsemble().getShapeFileTemplate() != null) {
                File f = ensembleTree.getClimateEnsemble().getShapeFileTemplate();
                this.ShapeFileText.setText(f.getName());
            } else {
                this.ShapeFileText.setText("");
            }

            this.RCMText.setText(model.getRCM());
            this.ScenarioText.setText(model.getScenario());
            this.TimePeriodText.setText(model.getTimePeriod().getValue());
            this.ensembleMemberText.setText("NAN");
            this.GCMText.setText(model.getGCM());
            int id = this.ensembleTree.getClimateEnsemble().getID(model);
            this.ensembleMemberText.setText(Integer.toString(id));
        }
        logger.exiting(getClass().getName(), "setCurrentModel");
    }

    protected void setEnabledForTextFields(boolean isEnabled) {
        for (JTextField f : textFields.keySet()) {
            f.setEnabled(isEnabled);
        }
    }

    public static void registerLogHandler(Logger log) {
        log.removeHandler(myLogHandler);
        log.addHandler(myLogHandler);
    }

    public static void main(String[] args) {
        EnsembleControlPanel ecp = new EnsembleControlPanel();
        JFrame frame = new JFrame("Ensemble Management");

        try {
            for (LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (Exception e) {
            // If Nimbus is not available, you can set the GUI to another look and feel.
        }
        //setCurrentDirectory("C:/Arbeit/Projekte/J2000Klima/JAMS/data/Ensembe Hasel");
        frame.add(ecp);
        frame.pack();
        frame.setVisible(true);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    }
}
