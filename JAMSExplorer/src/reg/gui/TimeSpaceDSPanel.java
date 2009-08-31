/*
 * TimeSpaceDSPanel.java
 * Created on 12. Februar 2009, 09:18
 *
 * This file is part of JAMS
 * Copyright (C) FSU Jena
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA
 *
 */
package reg.gui;

import jams.data.JAMSCalendar;
import jams.gui.GUIHelper;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Observable;
import java.util.Observer;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.AbstractAction;
import javax.swing.AbstractListModel;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.SwingWorker;
import javax.swing.UIManager;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import reg.dsproc.DataMatrix;
import reg.dsproc.DataStoreProcessor;
import reg.dsproc.EnsembleTimeSeriesProcessor;
import reg.dsproc.Processor;
import reg.dsproc.TimeSpaceProcessor;
import reg.spreadsheet.JAMSSpreadSheet;

/**
 *
 * @author Sven Kralisch <sven.kralisch at uni-jena.de>
 */
public class TimeSpaceDSPanel extends JPanel {

    private static final Dimension ACTION_BUTTON_DIM = new Dimension(150, 25), LIST_DIMENSION = new Dimension(150, 250);

    private Processor proc;
    
    private DataStoreProcessor dsdb;

    private GridBagLayout mainLayout;

    private JList modelRunList, timeList, entityList, monthList, yearList;

    private CancelableWorkerDlg workerDlg;

    private Frame parent;

    private JTextField timeField;

    private JAMSSpreadSheet outputSpreadSheet;

    private File outputDSFile;

    private JPanel aggregationPanel;

    private GridBagLayout aggregationLayout;

    private AttribComboBox attribCombo;
        
//    private Action timeStepAction,  spaceEntityAction,  timeAvgAction,  spaceAvgAction,  monthlyAvgAction,  yearlyAvgAction,  resetCacheAction;
    private Action[] actions = {
        new AbstractAction("Time Step") {

            @Override
            public void actionPerformed(ActionEvent e) {
                showTimeStep();
            }
        },
        new AbstractAction("Temp. Mean") {

            @Override
            public void actionPerformed(ActionEvent e) {
                showTempMean();
            }
        },
        new AbstractAction("Spatial Entity") {

            @Override
            public void actionPerformed(ActionEvent e) {
                showSpatEntity();
            }
        },
        new AbstractAction("Spatial Mean") {

            @Override
            public void actionPerformed(ActionEvent e) {
                showSpatEntity();
            }
        },
        new AbstractAction("Monthly Mean") {

            @Override
            public void actionPerformed(ActionEvent e) {
                showMonthlyMean();
            }
        },
        new AbstractAction("Yearly Mean") {

            @Override
            public void actionPerformed(ActionEvent e) {
                showYearlyMean();
            }
        },
        new AbstractAction("Crossproduct") {

            @Override
            public void actionPerformed(ActionEvent e) {
                showCrossProduct();
            }
        },};

    private Action timePoint = actions[0], timeMean = actions[1], spacePoint = actions[2], spaceMean = actions[3], monthMean = actions[4], yearMean = actions[5], crossProduct = actions[6];

    private Action cacheReset = new AbstractAction("Reset Caches") {

        @Override
        public void actionPerformed(ActionEvent e) {
            resetCaches();
        }
    };

    private Action indexReset = new AbstractAction("Reload Index") {

        @Override
        public void actionPerformed(ActionEvent e) {
            resetIndex();
        }
    };

    private Action freeTempMean = new AbstractAction("Temp. Mean (filter)") {

        @Override
        public void actionPerformed(ActionEvent e) {
            showFreeTempMean();
        }
    };

    public TimeSpaceDSPanel() {
        init();
    }

    private void init() {

        for (Action a : actions) {
            a.setEnabled(false);
        }

        freeTempMean.setEnabled(false);
        cacheReset.setEnabled(false);
        indexReset.setEnabled(false);

        mainLayout = new GridBagLayout();
        this.setLayout(mainLayout);

        modelRunList = new JList();
        JScrollPane modelRunListScroll = new JScrollPane(modelRunList);
        modelRunListScroll.setPreferredSize(LIST_DIMENSION);
        modelRunList.addListSelectionListener(new ListSelectionListener() {
            
            public void valueChanged(ListSelectionEvent e) {
                if (!e.getValueIsAdjusting()) {
                    if (modelRunList.getSelectedValues().length == 1) {
                        spacePoint.setEnabled(true);
                        spaceMean.setEnabled(false);
                        if (timeList.getSelectedValues().length > 0)
                            crossProduct.setEnabled(true);
                    } else if (modelRunList.getSelectedValues().length > 1) {
                        spacePoint.setEnabled(false);
                        spaceMean.setEnabled(true);
                        if (timeList.getSelectedValues().length > 0)
                            crossProduct.setEnabled(true);
                    } else {
                        spacePoint.setEnabled(false);
                        spaceMean.setEnabled(false);
                        crossProduct.setEnabled(false);
                    }
                }
            }
        });

        timeList = new JList();
        JScrollPane timeListScroll = new JScrollPane(timeList);
        timeListScroll.setPreferredSize(LIST_DIMENSION);
        timeList.addListSelectionListener(new ListSelectionListener() {
            
            public void valueChanged(ListSelectionEvent e) {
                if (!e.getValueIsAdjusting()) {
                    if (timeList.getSelectedValues().length == 1) {
                        timePoint.setEnabled(true);
                        timeMean.setEnabled(false);
                        if (modelRunList.getSelectedValues().length > 0 || 
                                entityList.getSelectedValues().length > 0)
                            crossProduct.setEnabled(true);
                    } else if (timeList.getSelectedValues().length > 1) {
                        timePoint.setEnabled(false);
                        timeMean.setEnabled(true);
                        if (modelRunList.getSelectedValues().length > 0 || 
                                entityList.getSelectedValues().length > 0)
                            crossProduct.setEnabled(true);
                    } else {
                        timePoint.setEnabled(false);
                        timeMean.setEnabled(false);
                        crossProduct.setEnabled(false);
                    }
                }
            }
        });

        entityList = new JList();
        JScrollPane entityListScroll = new JScrollPane(entityList);
        entityListScroll.setPreferredSize(new Dimension(LIST_DIMENSION.width - 50, LIST_DIMENSION.height));
        entityList.addListSelectionListener(new ListSelectionListener() {

            public void valueChanged(ListSelectionEvent e) {
                if (!e.getValueIsAdjusting()) {
                    if (entityList.getSelectedValues().length == 1) {
                        spacePoint.setEnabled(true);
                        spaceMean.setEnabled(false);
                        if (timeList.getSelectedValues().length > 0)
                            crossProduct.setEnabled(true);
                    } else if (entityList.getSelectedValues().length > 1) {
                        spacePoint.setEnabled(false);
                        spaceMean.setEnabled(true);
                        if (timeList.getSelectedValues().length > 0)
                            crossProduct.setEnabled(true);
                    } else {
                        spacePoint.setEnabled(false);
                        spaceMean.setEnabled(false);                        
                        crossProduct.setEnabled(false);
                    }
                }
            }
        });

        monthList = new JList();
        monthList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        JScrollPane monthListScroll = new JScrollPane(monthList);
        monthListScroll.setPreferredSize(new Dimension(LIST_DIMENSION.width - 100, LIST_DIMENSION.height));
        monthList.addListSelectionListener(new ListSelectionListener() {

            public void valueChanged(ListSelectionEvent e) {
                if (!e.getValueIsAdjusting()) {
                    if (monthList.getSelectedValues().length == 1) {
                        monthMean.setEnabled(true);
                    } else {
                        monthMean.setEnabled(false);
                    }
                }
            }
        });

        yearList = new JList();
        yearList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        JScrollPane yearListScroll = new JScrollPane(yearList);
        yearListScroll.setPreferredSize(new Dimension(LIST_DIMENSION.width - 100, LIST_DIMENSION.height));
        yearList.addListSelectionListener(new ListSelectionListener() {

            public void valueChanged(ListSelectionEvent e) {
                if (!e.getValueIsAdjusting()) {
                    if (yearList.getSelectedValues().length == 1) {
                        yearMean.setEnabled(true);
                    } else {
                        yearMean.setEnabled(false);
                    }
                }
            }
        });

        GUIHelper.addGBComponent(this, mainLayout, new JLabel("Attribute/Aggregation:"), 0, 10, 1, 1, 0, 0);

        aggregationLayout = new GridBagLayout();
        aggregationPanel = new JPanel();
        aggregationPanel.setLayout(aggregationLayout);
        JScrollPane aggregationScroll = new JScrollPane(aggregationPanel);
        aggregationScroll.setPreferredSize(new Dimension(LIST_DIMENSION.width + 100, LIST_DIMENSION.height));

        GUIHelper.addGBComponent(this, mainLayout, aggregationScroll, 0, 20, 1, 1, 0, 0);
        GUIHelper.addGBComponent(this, mainLayout, new JLabel("Model Runs:"), 10, 10, 1, 1, 0, 0);
        GUIHelper.addGBComponent(this, mainLayout, modelRunListScroll, 10, 20, 1, 1, 0, 0);
        GUIHelper.addGBComponent(this, mainLayout, new JLabel("Time Steps:"), 20, 10, 1, 1, 0, 0);
        GUIHelper.addGBComponent(this, mainLayout, timeListScroll, 20, 20, 1, 1, 0, 0);
        GUIHelper.addGBComponent(this, mainLayout, new JLabel("Entitiy IDs:"), 30, 10, 1, 1, 0, 0);
        GUIHelper.addGBComponent(this, mainLayout, entityListScroll, 30, 20, 1, 1, 0, 0);

        JPanel buttonPanelA = new JPanel();
        buttonPanelA.setPreferredSize(LIST_DIMENSION);
        JButton button;

        for (int i = 0; i <= 3; i++) {
            Action a = actions[i];
            button = new JButton(a);
            button.setPreferredSize(ACTION_BUTTON_DIM);
            buttonPanelA.add(button);
        }
                
        buttonPanelA.add(new JButton(crossProduct){
            {setPreferredSize(ACTION_BUTTON_DIM);}
        });

        JPanel filterPanel = new JPanel();
        filterPanel.setPreferredSize(new Dimension(LIST_DIMENSION.width, LIST_DIMENSION.height - 150));
        filterPanel.setBorder(BorderFactory.createEtchedBorder());

        filterPanel.add(new JLabel("Time Filter:"));
        timeField = new JTextField();
        timeField.setEnabled(false);
        timeField.setToolTipText("Date expression in SQL syntax, e.g. \"1992-11-%\" for all November values in 1992");
        timeField.setPreferredSize(new Dimension(ACTION_BUTTON_DIM.width - 20, timeField.getPreferredSize().height));
        timeField.getDocument().addDocumentListener(new DocumentListener() {

            public void insertUpdate(DocumentEvent e) {
                update();
            }

            public void removeUpdate(DocumentEvent e) {
                update();
            }

            public void changedUpdate(DocumentEvent e) {
                update();
            }

            private void update() {
                toggleFreeTempMeanButton();
            }
        });

        filterPanel.add(timeField);

        button = new JButton(freeTempMean);
        button.setPreferredSize(new Dimension(ACTION_BUTTON_DIM.width - 20, ACTION_BUTTON_DIM.height));
        filterPanel.add(button);

        buttonPanelA.add(filterPanel);

        GUIHelper.addGBComponent(this, mainLayout, buttonPanelA, 40, 20, 1, 1, 0, 0);

        GUIHelper.addGBComponent(this, mainLayout, new JLabel("Months:"), 60, 10, 1, 1, 0, 0);
        GUIHelper.addGBComponent(this, mainLayout, monthListScroll, 60, 20, 1, 1, 0, 0);
        GUIHelper.addGBComponent(this, mainLayout, new JLabel("Years:"), 70, 10, 1, 1, 0, 0);
        GUIHelper.addGBComponent(this, mainLayout, yearListScroll, 70, 20, 1, 1, 0, 0);

        JPanel buttonPanelB = new JPanel();
        buttonPanelB.setPreferredSize(LIST_DIMENSION);

        for (int i = 4; i < actions.length-1; i++) {
            Action a = actions[i];
            button = new JButton(a);
            button.setPreferredSize(ACTION_BUTTON_DIM);
            buttonPanelB.add(button);
        }

        buttonPanelB.add(new JPanel());

        button = new JButton(cacheReset);
        button.setPreferredSize(ACTION_BUTTON_DIM);
        buttonPanelB.add(button);

        button = new JButton(indexReset);
        button.setPreferredSize(ACTION_BUTTON_DIM);
        buttonPanelB.add(button);

        GUIHelper.addGBComponent(this, mainLayout, buttonPanelB, 80, 20, 1, 1, 0, 0);

    }

    public void setParent(Frame parent) {
        this.parent = parent;
        workerDlg = new CancelableWorkerDlg(parent, "Processing data");
        workerDlg.setProgress(0);
        workerDlg.setProgressMax(100);
    }

    public static void main(String[] args) throws Exception {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception evt) {
        }
        TimeSpaceDSPanel tsp = new TimeSpaceDSPanel();
        JFrame frame = new JFrame();
        JScrollPane scroll = new JScrollPane(tsp);
        frame.add(scroll);
        tsp.setParent(frame);
        //frame.setPreferredSize(new Dimension(300, 100));
        frame.pack();
        frame.setVisible(true);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        DataStoreProcessor dsdb = new DataStoreProcessor(new File("D:/jamsapplication/JAMS-Gehlberg/output/current/TestData.dat"));
        //dsdb.removeDB();
        dsdb.addImportProgressObserver(new Observer() {

            public void update(Observable o, Object arg) {
                System.out.println("Import progress: " + arg);
            }
        });

        if (!dsdb.existsH2DBFiles()) {
            dsdb.createDB();
        }

        TimeSpaceProcessor tsproc = new TimeSpaceProcessor(dsdb);
        //tsproc.isTimeSpaceDatastore();
        tsp.setTsProc(tsproc);
        //tsproc.close();
    }

    /**
     * @return the tsproc
     */
    public Processor getProc() {
        return proc;
    }
        
    private void createDB() {
        workerDlg.setInderminate(false);
        workerDlg.setTask(new CancelableSwingWorker() {

            public int cancel() {
                dsdb.cancelCreateIndex();
                return -1;
            }

            public Object doInBackground() {
                try {
                    dsdb.createDB();
                } catch (IOException ex) {
                    ex.printStackTrace();
                } catch (SQLException ex) {
                    ex.printStackTrace();
                } catch (ClassNotFoundException ex) {
                    ex.printStackTrace();
                }
                return null;
            }
        });

        try {
            if (!dsdb.existsH2DB()) {
                workerDlg.execute();
            }

            if (!dsdb.existsH2DB()) {
                clearPanel();
                System.out.println("Creation canceled");
            }

            if (dsdb.isTimeSpaceDatastore()) {
                this.setTsProc(new TimeSpaceProcessor(dsdb));
            }else if (dsdb.isEnsembleTimeSeriesDatastore()){
                this.setEnsembleTsProc(new EnsembleTimeSeriesProcessor(dsdb));
            }
        } catch (SQLException ex) {
        } catch (IOException ex) {
        }
    }

    private void resetIndex() {

        try {
            dsdb.clearDB();
        } catch (SQLException ex) {
            Logger.getLogger(TimeSpaceDSPanel.class.getName()).log(Level.SEVERE, null, ex);
        }

        createDB();
    }

    public void createTsProc(File file) {

        workerDlg.setTitle(workerDlg.getTitle() + " [" + file.getName() + "]");
        dsdb = new DataStoreProcessor(file);
        dsdb.addImportProgressObserver(new Observer() {

            public void update(Observable o, Object arg) {
                workerDlg.setProgress(Integer.parseInt(arg.toString()));
            }
        });
        createDB();

        this.outputDSFile = file;
    }

    private void setEnsembleTsProc(EnsembleTimeSeriesProcessor ensembleTsProc) throws SQLException, IOException {
        this.proc = ensembleTsProc;

        timeList.setModel(new AbstractListModel() {

            JAMSCalendar[] dates = ((EnsembleTimeSeriesProcessor)getProc()).getTimeSteps();

            public int getSize() {
                return dates.length;
            }

            public Object getElementAt(int i) {
                return dates[i];
            }
        });

        entityList.setModel(new AbstractListModel() {

            Long[] ids = new Long[0];

            public int getSize() {
                return ids.length;
            }

            public Object getElementAt(int i) {
                return ids[i];
            }
        });
        
        modelRunList.setModel(new AbstractListModel() {
            
            long[] ids_int = ((EnsembleTimeSeriesProcessor)getProc()).getModelRuns();
            Long[] ids = new Long[ids_int.length];
            {        
                for (int i=0;i<ids_int.length;i++){
                    ids[i] = new Long(ids_int[i]);
                }
            }
            public int getSize() {
                return ids.length;
            }

            public Object getElementAt(int i) {
                return ids[i];
            }
        });

        yearList.setModel(new AbstractListModel() {

            int[] years = ((EnsembleTimeSeriesProcessor)getProc()).getYears();

            public int getSize() {
                return years.length;
            }

            public Object getElementAt(int i) {
                return years[i];
            }
        });

        monthList.setModel(new AbstractListModel() {

            int[] months = {1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12};

            public int getSize() {
                return months.length;
            }

            public Object getElementAt(int i) {
                return months[i];
            }
        });


        // create the attribute panel for switching on/off attributes and
        // defining their aggregation weight
        JLabel label;

        label = new JLabel("Area attribute");
        label.setHorizontalAlignment(SwingConstants.LEFT);
        GUIHelper.addGBComponent(aggregationPanel, aggregationLayout, label, 5, 0, 1, 1, 0, 0);
        
        ArrayList<DataStoreProcessor.AttributeData> attribs = TimeSpaceDSPanel.this.getProc().getDataStoreProcessor().getAttributes();

        label = new JLabel("Aggregation weight");
        label.setHorizontalAlignment(SwingConstants.CENTER);
        GUIHelper.addGBComponent(aggregationPanel, aggregationLayout, label, 10, 3, 3, 1, 0, 0);
        label = new JLabel("1");
        label.setHorizontalAlignment(SwingConstants.CENTER);
        GUIHelper.addGBComponent(aggregationPanel, aggregationLayout, label, 10, 5, 1, 1, 0, 0);
        label = new JLabel("a/A");
        label.setHorizontalAlignment(SwingConstants.CENTER);
        GUIHelper.addGBComponent(aggregationPanel, aggregationLayout, label, 11, 5, 1, 1, 0, 0);
        label = new JLabel("l->mm");
        label.setHorizontalAlignment(SwingConstants.LEFT);
        GUIHelper.addGBComponent(aggregationPanel, aggregationLayout, label, 12, 5, 1, 1, 0, 0);

        int i = 0;
        ArrayList<JCheckBox> allChecks = new ArrayList<JCheckBox>();
        for (DataStoreProcessor.AttributeData attrib : attribs) {

            AttribCheckBox attribCheck = new AttribCheckBox(attrib);
            attribCheck.setSelected(attrib.isSelected());

            attribCheck.addItemListener(new ItemListener() {

                public void itemStateChanged(ItemEvent e) {
                    AttribCheckBox thisCheck = (AttribCheckBox) e.getSource();
                    if (!thisCheck.isSelected() && attribCombo.getSelectedItem().toString().equals(thisCheck.getText())) {
                        attribCombo.setSelectedIndex(0);
                        GUIHelper.showInfoDlg(parent, "Area attribute has been reset!", "Info");
                    }
                    thisCheck.attrib.setSelected(thisCheck.isSelected());
                }
            });

            allChecks.add(attribCheck);
            GUIHelper.addGBComponent(aggregationPanel, aggregationLayout, attribCheck, 5, i + 10, 1, 1, 0, 0);

            AttribRadioButton button1, button2, button3;
            button1 = new AttribRadioButton(attrib, DataStoreProcessor.AttributeData.AGGREGATION_NONE);
            button2 = new AttribRadioButton(attrib, DataStoreProcessor.AttributeData.AGGREGATION_WEIGHT);
            button3 = new AttribRadioButton(attrib, DataStoreProcessor.AttributeData.AGGREGATION_AREA);
            button1.setSelected(true);

            ItemListener attribRadioButtonListener = new ItemListener() {

                public void itemStateChanged(ItemEvent e) {
                    if (e.getStateChange() == ItemEvent.DESELECTED) {
                        return;
                    }
                    AttribRadioButton thisButton = (AttribRadioButton) e.getSource();
                    thisButton.attrib.setAggregationWeight(thisButton.aggregationType);
                    setCheckBox(thisButton.attrib.getName());

                }
            };

            button1.addItemListener(attribRadioButtonListener);
            button2.addItemListener(attribRadioButtonListener);
            button3.addItemListener(attribRadioButtonListener);

            ButtonGroup bGroup = new ButtonGroup();
            bGroup.add(button1);
            bGroup.add(button2);
            bGroup.add(button3);

            GUIHelper.addGBComponent(aggregationPanel, aggregationLayout, button1, 10, i + 10, 1, 1, 0, 0);
            GUIHelper.addGBComponent(aggregationPanel, aggregationLayout, button2, 11, i + 10, 1, 1, 0, 0);
            GUIHelper.addGBComponent(aggregationPanel, aggregationLayout, button3, 12, i + 10, 1, 1, 0, 0);

            i++;
        }

        String[] attribNames = new String[attribs.size() + 1];
        attribNames[0] = "[choose]";
        i = 1;
        for (DataStoreProcessor.AttributeData attrib : attribs) {
            attribNames[i++] = attrib.getName();
        }

        attribCombo = new AttribComboBox(allChecks);
        attribCombo.setModel(new DefaultComboBoxModel(attribNames));
        attribCombo.addItemListener(new ItemListener() {

            public void itemStateChanged(ItemEvent e) {
                AttribComboBox thisCombo = (AttribComboBox) e.getSource();
                setCheckBox(thisCombo.getSelectedItem().toString());
            }
        });
        GUIHelper.addGBComponent(aggregationPanel, aggregationLayout, attribCombo, 10, 0, 5, 1, 0, 0);

        GroupCheckBox allOnOffCheck = new GroupCheckBox("All on/off", allChecks);
        allOnOffCheck.setSelected(DataStoreProcessor.AttributeData.SELECTION_DEFAULT);

        GUIHelper.addGBComponent(aggregationPanel, aggregationLayout, allOnOffCheck, 5, 3, 1, 1, 0, 0);

        allOnOffCheck.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                GroupCheckBox thisCheck = (GroupCheckBox) e.getSource();
                boolean selected = thisCheck.isSelected();
                ArrayList<JCheckBox> allChecks = thisCheck.checkBoxList;
                for (JCheckBox checkBox : allChecks) {
                    checkBox.setSelected(selected);
                }
            }
        });

        aggregationPanel.updateUI();

        cacheReset.setEnabled(true);
        timeField.setEnabled(true);
        indexReset.setEnabled(true);

        proc.addProcessingProgressObserver(new Observer() {

            public void update(Observable o, Object arg) {
                workerDlg.setProgress(Integer.parseInt(arg.toString()));
            }
        });
        
        //rename buttons
        this.actions[2].putValue(Action.NAME, "ModelRun");
        this.actions[3].putValue(Action.NAME, "Ensemble Mean");

    }
    
    private void setTsProc(TimeSpaceProcessor tsproc) throws SQLException, IOException {
        this.proc = tsproc;

        timeList.setModel(new AbstractListModel() {

            JAMSCalendar[] dates = ((TimeSpaceProcessor)getProc()).getTimeSteps();

            public int getSize() {
                return dates.length;
            }

            public Object getElementAt(int i) {
                return dates[i];
            }
        });

        entityList.setModel(new AbstractListModel() {

            Long[] ids = ((TimeSpaceProcessor)getProc()).getEntityIDs();

            public int getSize() {
                return ids.length;
            }

            public Object getElementAt(int i) {
                return ids[i];
            }
        });

        yearList.setModel(new AbstractListModel() {

            int[] years = ((TimeSpaceProcessor)getProc()).getYears();

            public int getSize() {
                return years.length;
            }

            public Object getElementAt(int i) {
                return years[i];
            }
        });

        monthList.setModel(new AbstractListModel() {

            int[] months = {1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12};

            public int getSize() {
                return months.length;
            }

            public Object getElementAt(int i) {
                return months[i];
            }
        });


        // create the attribute panel for switching on/off attributes and
        // defining their aggregation weight
        JLabel label;

        label = new JLabel("Area attribute");
        label.setHorizontalAlignment(SwingConstants.LEFT);
        GUIHelper.addGBComponent(aggregationPanel, aggregationLayout, label, 5, 0, 1, 1, 0, 0);

        ArrayList<DataStoreProcessor.AttributeData> attribs = ((TimeSpaceProcessor)getProc()).getDataStoreProcessor().getAttributes();

        label = new JLabel("Aggregation weight");
        label.setHorizontalAlignment(SwingConstants.CENTER);
        GUIHelper.addGBComponent(aggregationPanel, aggregationLayout, label, 10, 3, 3, 1, 0, 0);
        label = new JLabel("1");
        label.setHorizontalAlignment(SwingConstants.CENTER);
        GUIHelper.addGBComponent(aggregationPanel, aggregationLayout, label, 10, 5, 1, 1, 0, 0);
        label = new JLabel("a/A");
        label.setHorizontalAlignment(SwingConstants.CENTER);
        GUIHelper.addGBComponent(aggregationPanel, aggregationLayout, label, 11, 5, 1, 1, 0, 0);
        label = new JLabel("l->mm");
        label.setHorizontalAlignment(SwingConstants.LEFT);
        GUIHelper.addGBComponent(aggregationPanel, aggregationLayout, label, 12, 5, 1, 1, 0, 0);

        int i = 0;
        ArrayList<JCheckBox> allChecks = new ArrayList<JCheckBox>();
        for (DataStoreProcessor.AttributeData attrib : attribs) {

            AttribCheckBox attribCheck = new AttribCheckBox(attrib);
            attribCheck.setSelected(attrib.isSelected());

            attribCheck.addItemListener(new ItemListener() {

                public void itemStateChanged(ItemEvent e) {
                    AttribCheckBox thisCheck = (AttribCheckBox) e.getSource();
                    if (!thisCheck.isSelected() && attribCombo.getSelectedItem().toString().equals(thisCheck.getText())) {
                        attribCombo.setSelectedIndex(0);
                        GUIHelper.showInfoDlg(parent, "Area attribute has been reset!", "Info");
                    }
                    thisCheck.attrib.setSelected(thisCheck.isSelected());
                }
            });

            allChecks.add(attribCheck);
            GUIHelper.addGBComponent(aggregationPanel, aggregationLayout, attribCheck, 5, i + 10, 1, 1, 0, 0);

            AttribRadioButton button1, button2, button3;
            button1 = new AttribRadioButton(attrib, DataStoreProcessor.AttributeData.AGGREGATION_NONE);
            button2 = new AttribRadioButton(attrib, DataStoreProcessor.AttributeData.AGGREGATION_WEIGHT);
            button3 = new AttribRadioButton(attrib, DataStoreProcessor.AttributeData.AGGREGATION_AREA);
            button1.setSelected(true);

            ItemListener attribRadioButtonListener = new ItemListener() {

                public void itemStateChanged(ItemEvent e) {
                    if (e.getStateChange() == ItemEvent.DESELECTED) {
                        return;
                    }
                    AttribRadioButton thisButton = (AttribRadioButton) e.getSource();
                    thisButton.attrib.setAggregationWeight(thisButton.aggregationType);
                    setCheckBox(thisButton.attrib.getName());

                }
            };

            button1.addItemListener(attribRadioButtonListener);
            button2.addItemListener(attribRadioButtonListener);
            button3.addItemListener(attribRadioButtonListener);

            ButtonGroup bGroup = new ButtonGroup();
            bGroup.add(button1);
            bGroup.add(button2);
            bGroup.add(button3);

            GUIHelper.addGBComponent(aggregationPanel, aggregationLayout, button1, 10, i + 10, 1, 1, 0, 0);
            GUIHelper.addGBComponent(aggregationPanel, aggregationLayout, button2, 11, i + 10, 1, 1, 0, 0);
            GUIHelper.addGBComponent(aggregationPanel, aggregationLayout, button3, 12, i + 10, 1, 1, 0, 0);

            i++;
        }

        String[] attribNames = new String[attribs.size() + 1];
        attribNames[0] = "[choose]";
        i = 1;
        for (DataStoreProcessor.AttributeData attrib : attribs) {
            attribNames[i++] = attrib.getName();
        }

        attribCombo = new AttribComboBox(allChecks);
        attribCombo.setModel(new DefaultComboBoxModel(attribNames));
        attribCombo.addItemListener(new ItemListener() {

            public void itemStateChanged(ItemEvent e) {
                AttribComboBox thisCombo = (AttribComboBox) e.getSource();
                setCheckBox(thisCombo.getSelectedItem().toString());
            }
        });
        GUIHelper.addGBComponent(aggregationPanel, aggregationLayout, attribCombo, 10, 0, 5, 1, 0, 0);

        GroupCheckBox allOnOffCheck = new GroupCheckBox("All on/off", allChecks);
        allOnOffCheck.setSelected(DataStoreProcessor.AttributeData.SELECTION_DEFAULT);

        GUIHelper.addGBComponent(aggregationPanel, aggregationLayout, allOnOffCheck, 5, 3, 1, 1, 0, 0);

        allOnOffCheck.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                GroupCheckBox thisCheck = (GroupCheckBox) e.getSource();
                boolean selected = thisCheck.isSelected();
                ArrayList<JCheckBox> allChecks = thisCheck.checkBoxList;
                for (JCheckBox checkBox : allChecks) {
                    checkBox.setSelected(selected);
                }
            }
        });

        aggregationPanel.updateUI();

        cacheReset.setEnabled(true);
        timeField.setEnabled(true);
        indexReset.setEnabled(true);

        tsproc.addProcessingProgressObserver(new Observer() {

            public void update(Observable o, Object arg) {
                workerDlg.setProgress(Integer.parseInt(arg.toString()));
            }
        });
        //rename button
        this.actions[2].putValue(Action.NAME, "Spatial Entity");
        this.actions[3].putValue(Action.NAME, "Spatial Mean");
    }

    private boolean setCheckBox(String theLabel) {

        for (JCheckBox check : attribCombo.checkBoxList) {
            if (theLabel.equals(check.getText())) {
                check.setSelected(true);
                return true;
            }
        }
        return false;
    }

    private class AttribComboBox extends JComboBox {

        ArrayList<JCheckBox> checkBoxList;

        public AttribComboBox(ArrayList<JCheckBox> checkBoxList) {
            super();
            this.checkBoxList = checkBoxList;
        }
    }

    private class AttribRadioButton extends JRadioButton {

        DataStoreProcessor.AttributeData attrib;

        int aggregationType;

        public AttribRadioButton(DataStoreProcessor.AttributeData attrib, int aggregationType) {
            super();
            this.attrib = attrib;
            this.aggregationType = aggregationType;
        }
    }

    private class GroupCheckBox extends JCheckBox {

        ArrayList<JCheckBox> checkBoxList;

        public GroupCheckBox(String title, ArrayList<JCheckBox> checkBoxList) {
            super(title);
            this.checkBoxList = checkBoxList;
        }
    }

    private class AttribCheckBox extends JCheckBox {

        DataStoreProcessor.AttributeData attrib;

        public AttribCheckBox(DataStoreProcessor.AttributeData attrib) {
            super(attrib.getName());
            this.attrib = attrib;
        }
    }

    private void clearPanel() {
        timeList.setEnabled(false);
        entityList.setEnabled(false);
        yearList.setEnabled(false);
        monthList.setEnabled(false);
        cacheReset.setEnabled(false);
        timeField.setEnabled(false);
        indexReset.setEnabled(false);
    }

    private void showTimeStep() {

        if ((timeList.getSelectedValues().length == 0) || (timeList.getSelectedValues().length > 1)) {
            return;
        }

        workerDlg.setInderminate(true);

        workerDlg.setTask(new SwingWorker<Object, Void>() {

            DataMatrix m = null;

            public Object doInBackground() {
                JAMSCalendar date = (JAMSCalendar) timeList.getSelectedValue();

                if (date == null) {
                    return m;
                }

                try {
                    if (getProc() instanceof TimeSpaceProcessor)
                        m = ((TimeSpaceProcessor)getProc()).getTemporalData(date);
                    if (getProc() instanceof EnsembleTimeSeriesProcessor)
                        m = ((EnsembleTimeSeriesProcessor)getProc()).getTemporalData(date);
                } catch (SQLException ex) {
                } catch (IOException ex) {
                }
                return m;
            }

            @Override
            public void done() {
                loadData(m, false);
            }
        });

        workerDlg.execute();
    }

    private void showMonthlyMean() {

        if (monthList.getSelectedValues().length == 0) {
            return;
        }

        workerDlg.setInderminate(false);
        workerDlg.setProgress(0);
        workerDlg.setTask(new CancelableSwingWorker() {

            DataMatrix m;

            public Object doInBackground() {
                try {

                    int month = (Integer) monthList.getSelectedValue();

                    workerDlg.setInderminate(true);
                    
                    if (getProc() instanceof TimeSpaceProcessor){
                        TimeSpaceProcessor tsproc = (TimeSpaceProcessor)getProc();
                        if (!tsproc.isMonthlyMeanExisiting()) {
                            tsproc.calcMonthlyMean();
                        }
                        m = tsproc.getMonthlyMean(month);
                    }
                    if (getProc() instanceof EnsembleTimeSeriesProcessor){
                        EnsembleTimeSeriesProcessor ensembleTsproc = (EnsembleTimeSeriesProcessor)getProc();
                        if (!ensembleTsproc.isMonthlyMeanExisiting()) {
                            ensembleTsproc.calcMonthlyMean();
                        }
                        m = ensembleTsproc.getMonthlyMean(month);
                    }
                } catch (SQLException ex) {
                    System.out.println(ex);
                    ex.printStackTrace();
                } catch (IOException ex) {
                    System.out.println(ex);
                    ex.printStackTrace();
                }
                return null;
            }

            @Override
            public void done() {
                loadData(m, false);
            }

            public int cancel() {
                proc.sendAbortOperation();
                return 0;
            }
        });
        workerDlg.execute();
    }

    private void showYearlyMean() {

        if (yearList.getSelectedValues().length == 0) {
            return;
        }

        workerDlg.setInderminate(false);
        workerDlg.setProgress(0);
        workerDlg.setTask(new CancelableSwingWorker() {

            DataMatrix m;

            public Object doInBackground() {
                try {

                    int year = (Integer) yearList.getSelectedValue();
                                                                
                    if ( getProc() instanceof TimeSpaceProcessor ){
                        TimeSpaceProcessor tsproc = null;
                        tsproc = (TimeSpaceProcessor) getProc();
                        if (!tsproc.isYearlyMeanExisiting()) {
                            tsproc.calcYearlyMean();
                        }
                        m = tsproc.getYearlyMean(year);
                    }else if ( getProc() instanceof EnsembleTimeSeriesProcessor ){
                        EnsembleTimeSeriesProcessor ensembleTimeSeriesproc = 
                                (EnsembleTimeSeriesProcessor) getProc();
                        
                        m = ensembleTimeSeriesproc.getYearlyMean(year);
                    }

                    workerDlg.setInderminate(true);
                    
                } catch (SQLException ex) {
                    ex.printStackTrace();
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
                return null;
            }

            @Override
            public void done() {
                loadData(m, false);
            }

            public int cancel() {
                proc.sendAbortOperation();
                return 0;
            }
        });
        workerDlg.execute();
    }

    private void showTempMean() {

        if (timeList.getSelectedValues().length == 0) {
            return;
        }

        workerDlg.setInderminate(false);
        workerDlg.setProgress(0);
        workerDlg.setTask(new CancelableSwingWorker() {

            DataMatrix m = null;

            public Object doInBackground() {
                try {

                    Object[] objects = timeList.getSelectedValues();

                    ArrayList<JAMSCalendar> dateList = new ArrayList<JAMSCalendar>();
                    for (Object o : objects) {
                        dateList.add((JAMSCalendar) o);
                    }
                    JAMSCalendar[] dates = dateList.toArray(new JAMSCalendar[dateList.size()]);

                    TimeSpaceProcessor tsproc = null;
                    EnsembleTimeSeriesProcessor ensembleTsproc = null;
                    
                    if ( getProc() instanceof TimeSpaceProcessor ){
                        tsproc = (TimeSpaceProcessor)getProc();
                        
                        // check if number of selected ids is equal to all ids
                        // if so, we better derive temp avg from monthly means
                        if (dates.length == timeList.getModel().getSize()) {
                            // check if cache tables are available
                            if (!tsproc.isMonthlyMeanExisiting()) {
                                tsproc.calcMonthlyMean();
                            }
                            workerDlg.setInderminate(true);


                            if (!tsproc.isMonthlyMeanExisiting()) {
                                return null;
                            }

                            m = tsproc.getTemporalAvg();                            
                        } else {
                            m = tsproc.getTemporalMean(dates);
                        }
                    }
                    else if ( getProc() instanceof EnsembleTimeSeriesProcessor ){
                        ensembleTsproc = (EnsembleTimeSeriesProcessor)getProc();
                        
                        // check if number of selected ids is equal to all ids
                        // if so, we better derive temp avg from monthly means
                        if (dates.length == timeList.getModel().getSize()) {                            
                            workerDlg.setInderminate(true);

                            m = ensembleTsproc.getTemporalMean();                            
                        } else {
                            m = ensembleTsproc.getTemporalMean(dates);
                        }
                    }
                    else 
                        return null;                    
                } catch (SQLException ex) {
                    System.out.println(ex);
                    ex.printStackTrace();
                } catch (IOException ex) {
                    System.out.println(ex);
                    ex.printStackTrace();
                }
                return null;
            }

            @Override
            public void done() {
                loadData(m, false);
            }

            public int cancel() {
                proc.sendAbortOperation();
                return 0;
            }
        });
        workerDlg.execute();
    }

    private void showSpatEntity() {
        if (modelRunList.getSelectedValues().length == 0) {
            return;
        }

        workerDlg.setInderminate(false);
        workerDlg.setProgress(0);
        workerDlg.setTask(new CancelableSwingWorker() {

            DataMatrix m = null;

            public Object doInBackground() {
                try {

                    Object[] objects = modelRunList.getSelectedValues();

                    long[] ids = new long[objects.length];
                    int c = 0;
                    for (Object o : objects) {
                        ids[c++] = (Long) o;
                    }
                    
                    if (getProc() instanceof TimeSpaceProcessor){
                        TimeSpaceProcessor tsproc = (TimeSpaceProcessor)getProc();                        
                        // check if number of selected ids is equal to all ids
                        // if so, we better derive temp avg from monthly means
                        if (ids.length == modelRunList.getModel().getSize()) {
                            // check if cache tables are available
                            if (!tsproc.isSpatSumExisiting()) {
                                tsproc.calcSpatialSum();
                            }
                            workerDlg.setInderminate(true);

                            if (!tsproc.isSpatSumExisiting()) {
                                return null;
                            }

                            m = tsproc.getSpatialSum();

                        } else {
                            m = tsproc.getSpatialSum(ids);
                        }                        
                    }else if (getProc() instanceof EnsembleTimeSeriesProcessor){
                        EnsembleTimeSeriesProcessor ensembleProc = (EnsembleTimeSeriesProcessor)getProc();                        
                        m = ensembleProc.getEnsembleMean(ids);                                                
                    }
                        return null;
                    
                } catch (SQLException ex) {
                } catch (IOException ex) {
                }
                return null;
            }

            @Override
            public void done() {
                loadData(m, true);
            }

            public int cancel() {
                getProc().sendAbortOperation();
                return 0;
            }
        });
        workerDlg.execute();
    }

    private void showFreeTempMean() {

        String filter = timeField.getText();
        if (!filter.contains("%") && !filter.contains("?")) {
            return;
        }

        workerDlg.setInderminate(false);
        workerDlg.setProgress(0);
        workerDlg.setTask(new CancelableSwingWorker() {

            DataMatrix m;

            public Object doInBackground() {
                try {
                    String filter = timeField.getText();

                    if (getProc() instanceof TimeSpaceProcessor)
                        m = ((TimeSpaceProcessor)getProc()).getTemporalMean(filter);
                    else
                        return null;

                } catch (SQLException ex) {
                    ex.printStackTrace();
                } catch (IOException ex) {
                }
                return null;
            }

            public void done() {
                loadData(m, false);
            }

            public int cancel() {
                getProc().sendAbortOperation();
                return 0;
            }
        });
        workerDlg.execute();
    }

    private void showCrossProduct() {        
        workerDlg.setInderminate(false);
        workerDlg.setProgress(0);
        workerDlg.setTask(new CancelableSwingWorker() {

            DataMatrix m;

            public Object doInBackground() {
                try {
                    Object[] objects1 = modelRunList.getSelectedValues();

                    long[] ids1 = new long[objects1.length];
                    for (int c=0;c<ids1.length;c++){
                        ids1[c] = (Long) objects1[c];
                    }
                    
                    Object[] objects2 = timeList.getSelectedValues();

                    String[] ids2 = new String[objects2.length];
                    for (int c=0;c<ids2.length;c++){
                        ids2[c] = ((JAMSCalendar) objects2[c]).toString();
                    }
                    
                    Object[] objects3 = entityList.getSelectedValues();

                    long[] ids3 = new long[objects3.length];
                    for (int c=0;c<ids3.length;c++){
                        ids3[c] = (Long) objects3[c];
                    }
                    
                    if (getProc() instanceof EnsembleTimeSeriesProcessor)
                        m = ((EnsembleTimeSeriesProcessor)getProc()).getCrossProduct(ids1, ids2);
                    else if (getProc() instanceof TimeSpaceProcessor)
                        m = ((TimeSpaceProcessor)getProc()).getCrossProduct(ids3, ids2);
                    else
                        return null;

                } catch (SQLException ex) {
                    ex.printStackTrace();
                } catch (IOException ex) {
                }
                return null;
            }

            public void done() {
                loadData(m, true);
            }

            public int cancel() {
                getProc().sendAbortOperation();
                return 0;
            }
        });
        workerDlg.execute();
    }
    
    private void toggleFreeTempMeanButton() {
        String filter = timeField.getText();
        if (!filter.contains("%") && !filter.contains("?")) {
            freeTempMean.setEnabled(false);
        } else {
            freeTempMean.setEnabled(true);
        }
    }

    private void resetCaches() {
        try {
            if (getProc() instanceof EnsembleTimeSeriesProcessor)
                ((EnsembleTimeSeriesProcessor)getProc()).deleteCache();
            else if (getProc() instanceof TimeSpaceProcessor)
                ((TimeSpaceProcessor)getProc()).deleteCache();
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    public void setOutputSpreadSheet(JAMSSpreadSheet spreadsheet) {
        this.outputSpreadSheet = spreadsheet;
    }

    private void loadData(DataMatrix m, boolean timeSeries) {
                System.out.println("loading data");

        if (m == null) {
            return;
        }

        postProcess(m, timeSeries);

        //m.output();

        if (false) {
            return;
        }

        if (m.getAttributeIDs() == null) {
            m.setAttributeIDs(dsdb.getSelectedDoubleAttribs());
        }

        if (this.outputSpreadSheet != null) {
            this.outputSpreadSheet.loadMatrix(m, outputDSFile.getParentFile(), timeSeries);
        } else {
            m.output();
        }
    }

    private void postProcess(DataMatrix m, boolean timeSeries) {

        double[] weights = null;
        double area = 0;
        double[][] data = m.getArray();

        ArrayList<DataStoreProcessor.AttributeData> attribs = this.getProc().getDataStoreProcessor().getAttributes();
        int j = 0;
        for (DataStoreProcessor.AttributeData attrib : attribs) {

            if (!attrib.isSelected()) {
                continue;
            }

            if (attrib.getAggregationWeight() != DataStoreProcessor.AttributeData.AGGREGATION_NONE) {

                if (attribCombo.getSelectedIndex() == 0) {
                    GUIHelper.showInfoDlg(parent, "No area attribute has been chosen! Skipping weighted aggregation for attribute \"" +
                            attrib.getName() + "\".", "Info");
                    continue;
                }

                if (weights == null) {

                    // calculate normalized weights
                    weights = new double[data.length];

                    String weightAttribName = attribCombo.getSelectedItem().toString();

                    int attribIndex = 0;
                    for (DataStoreProcessor.AttributeData attrib2 : attribs) {
                        if (attrib2.getName().equals(weightAttribName)) {
                            break;
                        }
                        boolean selected = attrib2.isSelected();
                        if (selected) {
                            attribIndex++;
                        }
                    }

                    // calculate the overall area
                    for (int i = 0; i < data.length; i++) {
                        area += data[i][attribIndex];
                    }
                    if (timeSeries) {
                        area /= data.length;
                    }

                    // calc weights
                    for (int i = 0; i < data.length; i++) {
                        weights[i] = data[i][attribIndex];
                    }
                }

                for (int i = 0; i < data.length; i++) {
                    switch (attrib.getAggregationWeight()) {
                        case DataStoreProcessor.AttributeData.AGGREGATION_AREA:
                            if (timeSeries) {
                                data[i][j] /= area;
                            } else {
                                data[i][j] /= weights[i];
                            }
                            break;
                        case DataStoreProcessor.AttributeData.AGGREGATION_WEIGHT:
                            data[i][j] *= (weights[i] / area);
                            break;
                    }
                }
            }
            j++;
        }
    }
}
