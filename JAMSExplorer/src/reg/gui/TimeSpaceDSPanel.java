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
import jams.gui.LHelper;
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
import java.util.HashMap;
import java.util.Observable;
import java.util.Observer;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.AbstractAction;
import javax.swing.AbstractListModel;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SwingWorker;
import javax.swing.UIManager;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import reg.dsproc.DataMatrix;
import reg.dsproc.DataStoreProcessor;
import reg.dsproc.TimeSpaceProcessor;
import reg.spreadsheet.JAMSSpreadSheet;

/**
 *
 * @author Sven Kralisch <sven.kralisch at uni-jena.de>
 */
public class TimeSpaceDSPanel extends JPanel {

    private static final Dimension ACTION_BUTTON_DIM = new Dimension(150, 25), LIST_DIMENSION = new Dimension(150, 240);

    private TimeSpaceProcessor tsproc;

    private DataStoreProcessor dsdb;

    private GridBagLayout mainLayout;

    private JList timeList, entityList, monthList, yearList;

    private CancelableWorkerDlg workerDlg;

    private Frame parent;

    private JTextField timeField;

    private JAMSSpreadSheet outputSpreadSheet;

    private File outputDSFile;

    private JPanel aggregationPanel;

    private GridBagLayout aggregationLayout;

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
        },};

    private Action timePoint = actions[0], timeMean = actions[1], spacePoint = actions[2], spaceMean = actions[3], monthMean = actions[4], yearMean = actions[5];

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

        timeList = new JList();
        JScrollPane timeListScroll = new JScrollPane(timeList);
        timeListScroll.setPreferredSize(LIST_DIMENSION);
        timeList.addListSelectionListener(new ListSelectionListener() {

            public void valueChanged(ListSelectionEvent e) {
                if (!e.getValueIsAdjusting()) {
                    if (timeList.getSelectedValues().length == 1) {
                        timePoint.setEnabled(true);
                        timeMean.setEnabled(false);
                    } else if (timeList.getSelectedValues().length > 1) {
                        timePoint.setEnabled(false);
                        timeMean.setEnabled(true);
                    } else {
                        timePoint.setEnabled(false);
                        timeMean.setEnabled(false);
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
                    } else if (entityList.getSelectedValues().length > 1) {
                        spacePoint.setEnabled(false);
                        spaceMean.setEnabled(true);
                    } else {
                        spacePoint.setEnabled(false);
                        spaceMean.setEnabled(false);
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

        LHelper.addGBComponent(this, mainLayout, new JLabel("Attribute/Aggregation:"), 0, 10, 1, 1, 0, 0);

        aggregationLayout = new GridBagLayout();
        aggregationPanel = new JPanel();
        aggregationPanel.setLayout(aggregationLayout);
        JScrollPane aggregationScroll = new JScrollPane(aggregationPanel);
        aggregationScroll.setPreferredSize(new Dimension(LIST_DIMENSION.width + 100, LIST_DIMENSION.height));

        LHelper.addGBComponent(this, mainLayout, aggregationScroll, 0, 20, 1, 1, 0, 0);


        LHelper.addGBComponent(this, mainLayout, new JLabel("Time Steps:"), 10, 10, 1, 1, 0, 0);
        LHelper.addGBComponent(this, mainLayout, timeListScroll, 10, 20, 1, 1, 0, 0);
        LHelper.addGBComponent(this, mainLayout, new JLabel("Entitiy IDs:"), 20, 10, 1, 1, 0, 0);
        LHelper.addGBComponent(this, mainLayout, entityListScroll, 20, 20, 1, 1, 0, 0);

        JPanel buttonPanelA = new JPanel();
        buttonPanelA.setPreferredSize(LIST_DIMENSION);
        JButton button;

        for (int i = 0; i <= 3; i++) {
            Action a = actions[i];
            button = new JButton(a);
            button.setPreferredSize(ACTION_BUTTON_DIM);
            buttonPanelA.add(button);
        }

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

        LHelper.addGBComponent(this, mainLayout, buttonPanelA, 30, 20, 1, 1, 0, 0);

        LHelper.addGBComponent(this, mainLayout, new JLabel("Months:"), 50, 10, 1, 1, 0, 0);
        LHelper.addGBComponent(this, mainLayout, monthListScroll, 50, 20, 1, 1, 0, 0);
        LHelper.addGBComponent(this, mainLayout, new JLabel("Years:"), 60, 10, 1, 1, 0, 0);
        LHelper.addGBComponent(this, mainLayout, yearListScroll, 60, 20, 1, 1, 0, 0);

        JPanel buttonPanelB = new JPanel();
        buttonPanelB.setPreferredSize(LIST_DIMENSION);

        for (int i = 4; i < actions.length; i++) {
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

        LHelper.addGBComponent(this, mainLayout, buttonPanelB, 70, 20, 1, 1, 0, 0);

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

        DataStoreProcessor dsdb = new DataStoreProcessor(new File("D:/jamsapplication/JAMS-Gehlberg/output/current/HRULoop_0.dat"));
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
        tsproc.isTimeSpaceDatastore();
        tsp.setTsProc(tsproc);
        //tsproc.close();
    }

    /**
     * @return the tsproc
     */
    public TimeSpaceProcessor getTsproc() {
        return tsproc;
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

            TimeSpaceProcessor proc = new TimeSpaceProcessor(dsdb);

            if (proc.isTimeSpaceDatastore()) {
                this.setTsProc(proc);
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

    private void setTsProc(TimeSpaceProcessor tsproc) throws SQLException, IOException {
        this.tsproc = tsproc;

        timeList.setModel(new AbstractListModel() {

            JAMSCalendar[] dates = TimeSpaceDSPanel.this.getTsproc().getTimeSteps();

            public int getSize() {
                return dates.length;
            }

            public Object getElementAt(int i) {
                return dates[i];
            }
        });

        entityList.setModel(new AbstractListModel() {

            Long[] ids = TimeSpaceDSPanel.this.getTsproc().getEntityIDs();

            public int getSize() {
                return ids.length;
            }

            public Object getElementAt(int i) {
                return ids[i];
            }
        });

        yearList.setModel(new AbstractListModel() {

            int[] years = TimeSpaceDSPanel.this.getTsproc().getYears();

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

        ArrayList<DataStoreProcessor.AttributeData> attribs = TimeSpaceDSPanel.this.getTsproc().getDataStoreProcessor().getAttributes();
        String[] attribNames = new String[attribs.size() + 1];
        attribNames[0] = DataStoreProcessor.AttributeData.SELECTION_NONE;
        int i = 1;
        for (DataStoreProcessor.AttributeData attrib : attribs) {
            attribNames[i++] = attrib.getName();
        }

        i = 0;
        ArrayList<JCheckBox> allChecks = new ArrayList<JCheckBox>();
        for (DataStoreProcessor.AttributeData attrib : attribs) {
            //LHelper.addGBComponent(aggregationPanel, aggregationLayout, new JLabel(attrib.getName()), 0, i + 10, 1, 1, 0, 0);

            AttribCheckBox attribCheck = new AttribCheckBox(attrib);
            attribCheck.setSelected(attrib.isSelected());

            attribCheck.addItemListener(new ItemListener() {

                public void itemStateChanged(ItemEvent e) {
                    AttribCheckBox thisCheck = (AttribCheckBox) e.getSource();
                    thisCheck.getAttrib().setSelected(thisCheck.isSelected());
                }
            });

            allChecks.add(attribCheck);
            LHelper.addGBComponent(aggregationPanel, aggregationLayout, attribCheck, 5, i + 10, 1, 1, 0, 0);

            AttribCombo attribCombo = new AttribCombo(attrib);
            attribCombo.setModel(new DefaultComboBoxModel(attribNames));
            attribCombo.addItemListener(new ItemListener() {

                public void itemStateChanged(ItemEvent e) {
                    if (e.getStateChange() == ItemEvent.DESELECTED) {
                        return;
                    }
                    AttribCombo thisCombo = (AttribCombo) e.getSource();
                    thisCombo.getAttrib().setAggregationWeight(thisCombo.getSelectedItem().toString());
                }
            });
            LHelper.addGBComponent(aggregationPanel, aggregationLayout, attribCombo, 10, i + 10, 1, 1, 0, 0);

            i++;
        }

        GroupCheckBox allOnOffCheck = new GroupCheckBox("All on/off", allChecks);
        allOnOffCheck.setSelected(DataStoreProcessor.AttributeData.SELECTION_DEFAULT);
        //LHelper.addGBComponent(aggregationPanel, aggregationLayout, new JLabel("All on/off"), 0, 0, 1, 1, 0, 0);
        LHelper.addGBComponent(aggregationPanel, aggregationLayout, allOnOffCheck, 5, 0, 1, 1, 0, 0);

        allOnOffCheck.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                GroupCheckBox thisCheck = (GroupCheckBox) e.getSource();
                boolean selected = thisCheck.isSelected();
                ArrayList<JCheckBox> allChecks = thisCheck.getCheckBoxList();
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

    }

    private class AttribCombo extends JComboBox {

        private DataStoreProcessor.AttributeData attrib;

        public AttribCombo(DataStoreProcessor.AttributeData attrib) {
            super();
            this.attrib = attrib;
        }

        /**
         * @return the attrib
         */
        public DataStoreProcessor.AttributeData getAttrib() {
            return attrib;
        }
    }

    private class GroupCheckBox extends JCheckBox {

        private ArrayList<JCheckBox> checkBoxList;

        public GroupCheckBox(String title, ArrayList<JCheckBox> checkBoxList) {
            super(title);
            this.checkBoxList = checkBoxList;
        }

        /**
         * @return the checkBoxList
         */
        public ArrayList<JCheckBox> getCheckBoxList() {
            return checkBoxList;
        }
    }

    private class AttribCheckBox extends JCheckBox {

        private DataStoreProcessor.AttributeData attrib;

        public AttribCheckBox(DataStoreProcessor.AttributeData attrib) {
            super(attrib.getName());
            this.attrib = attrib;
        }

        /**
         * @return the attrib
         */
        public DataStoreProcessor.AttributeData getAttrib() {
            return attrib;
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
                    m = tsproc.getTemporalData(date);
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

                    if (!tsproc.isMonthlyMeanExisiting()) {
                        tsproc.calcMonthlyMean();
                    }

                    workerDlg.setInderminate(true);

                    m = tsproc.getMonthlyMean(month);

                } catch (SQLException ex) {
                } catch (IOException ex) {
                }
                return null;
            }

            @Override
            public void done() {
                loadData(m, false);
            }

            public int cancel() {
                tsproc.sendAbortOperation();
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

                    if (!tsproc.isYearlyMeanExisiting()) {
                        tsproc.calcYearlyMean();
                    }

                    workerDlg.setInderminate(true);

                    m = tsproc.getYearlyMean(year);

                } catch (SQLException ex) {
                } catch (IOException ex) {
                }
                return null;
            }

            @Override
            public void done() {
                loadData(m, false);
            }

            public int cancel() {
                tsproc.sendAbortOperation();
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

                } catch (SQLException ex) {
                } catch (IOException ex) {
                }
                return null;
            }

            @Override
            public void done() {
                loadData(m, false);
            }

            public int cancel() {
                tsproc.sendAbortOperation();
                return 0;
            }
        });
        workerDlg.execute();
    }

    private void showSpatEntity() {
        if (entityList.getSelectedValues().length == 0) {
            return;
        }

        workerDlg.setInderminate(false);
        workerDlg.setProgress(0);
        workerDlg.setTask(new CancelableSwingWorker() {

            DataMatrix m = null;

            public Object doInBackground() {
                try {

                    Object[] objects = entityList.getSelectedValues();

                    long[] ids = new long[objects.length];
                    int c = 0;
                    for (Object o : objects) {
                        ids[c++] = (Long) o;
                    }

                    // check if number of selected ids is equal to all ids
                    // if so, we better derive temp avg from monthly means
                    if (ids.length == entityList.getModel().getSize()) {
                        // check if cache tables are available
                        if (!tsproc.isSpatMeanExisiting()) {
                            tsproc.calcSpatialMean();
                        }
                        workerDlg.setInderminate(true);

                        if (!tsproc.isSpatMeanExisiting()) {
                            return null;
                        }

                        m = tsproc.getSpatialMean();

                    } else {

                        m = tsproc.getSpatialMean(ids);

                    }

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
                tsproc.sendAbortOperation();
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

                    m = tsproc.getTemporalMean(filter);

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
                tsproc.sendAbortOperation();
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
            tsproc.deleteCache();
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    public void setOutputSpreadSheet(JAMSSpreadSheet spreadsheet) {
        this.outputSpreadSheet = spreadsheet;
    }

    private void loadData(DataMatrix m, boolean timeSeries) {

        if (m == null) {
            return;
        }

        postProcess(m);

        m.output();

        if (true) {
            //return;
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

    private void postProcess(DataMatrix m) {

        HashMap<String, double[]> weightMap = new HashMap<String, double[]>();
        double[][] data = m.getArray();

        ArrayList<DataStoreProcessor.AttributeData> attribs = this.getTsproc().getDataStoreProcessor().getAttributes();
        int j = 0;
        for (DataStoreProcessor.AttributeData attrib : attribs) {

            if (!attrib.isSelected()) {
                continue;
            }

            if (attrib.getAggregationWeight() != null) {

                System.out.println("weighting " + attrib.getName());
                double[] weights = weightMap.get(attrib.getAggregationWeight());
                if (weights == null) {

                    // calculate normalized weights
                    weights = new double[data.length];

                    // calc sum
                    double sum = 0;
                    for (int i = 0; i < data.length; i++) {
                        System.out.println(i + " " + j);
                        sum += data[i][j];
                    }

                    // calc weights
                    for (int i = 0; i < data.length; i++) {
                        weights[i] = data[i][j] / sum;
                    }

                    weightMap.put(attrib.getAggregationWeight(), weights);
                }

                for (int i = 0; i < data.length; i++) {
                    data[i][j] *= weights[i];
                    System.out.println(data[i][j]);
                }
            }

            j++;

        }
    }
}
