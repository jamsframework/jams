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
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Observable;
import java.util.Observer;
import javax.swing.AbstractAction;
import javax.swing.AbstractListModel;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.UIManager;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import reg.dsproc.DataMatrix;
import reg.dsproc.TimeSpaceProcessor;

/**
 *
 * @author Sven Kralisch <sven.kralisch at uni-jena.de>
 */
public class TimeSpaceDSPanel extends JPanel {

    private static final Dimension ACTION_BUTTON_DIM = new Dimension(150, 25),  LIST_DIMENSION = new Dimension(150, 240);

    private TimeSpaceProcessor tsproc;

    private GridBagLayout mainLayout;

    private JList timeList,  entityList,  monthList,  yearList;

    private CancelableWorkerDlg workerDlg;

    private Frame parent;

    private JTextField timeField;

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

    private Action timePoint = actions[0],  timeMean = actions[1],  spacePoint = actions[2],  spaceMean = actions[3],  monthMean = actions[4],  yearMean = actions[5];

    private Action cacheReset = new AbstractAction("Reset Caches") {

        @Override
        public void actionPerformed(ActionEvent e) {
            resetCaches();
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
                        freeTempMean.setEnabled(false);
                    } else if (timeList.getSelectedValues().length > 1) {
                        timePoint.setEnabled(false);
                        timeMean.setEnabled(true);
                        freeTempMean.setEnabled(true);
                    } else {
                        timePoint.setEnabled(false);
                        timeMean.setEnabled(false);
                        freeTempMean.setEnabled(false);
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
        TimeSpaceProcessor tsproc = new TimeSpaceProcessor("D:/jamsapplication/JAMS-Gehlberg/output/current/HRULoop_0.dat");
        tsproc.isTimeSpaceDatastore();
        tsp.setTsproc(tsproc);
    //tsproc.close();
    }

    /**
     * @return the tsproc
     */
    public TimeSpaceProcessor getTsproc() {
        return tsproc;
    }

    /**
     * @param tsproc the tsproc to set
     */
    public void setTsproc(TimeSpaceProcessor tsproc) throws SQLException, IOException {
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

        cacheReset.setEnabled(true);
        timeField.setEnabled(true);

        tsproc.addProcessingProgressObserver(new Observer() {

            public void update(Observable o, Object arg) {
                workerDlg.setProgress(Integer.parseInt(arg.toString()));
            }
        });

    }

    private void showTimeStep() {

        if ((timeList.getSelectedValues().length == 0) || (timeList.getSelectedValues().length > 1)) {
            return;
        }

        workerDlg.setInderminate(true);
        workerDlg.setTask(new CancelableRunnable() {

            public void run() {
                try {
                    JAMSCalendar date = (JAMSCalendar) timeList.getSelectedValue();
                    if (date == null) {
                        return;
                    }
                    DataMatrix m = tsproc.getTemporalData(date);
                    loadData(m);

                } catch (SQLException ex) {
                } catch (IOException ex) {
                }
            }

            public int cancel() {
                return 0;
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
        workerDlg.setTask(new CancelableRunnable() {

            public void run() {
                try {

                    int month = (Integer) monthList.getSelectedValue();

                    if (!tsproc.isMonthlyMeanExisiting()) {
                        tsproc.calcMonthlyMean();
                    }

                    workerDlg.setInderminate(true);

                    DataMatrix m = tsproc.getMonthlyMean(month);
                    loadData(m);

                } catch (SQLException ex) {
                } catch (IOException ex) {
                }
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
        workerDlg.setTask(new CancelableRunnable() {

            public void run() {
                try {

                    int year = (Integer) yearList.getSelectedValue();

                    if (!tsproc.isYearlyMeanExisiting()) {
                        tsproc.calcYearlyMean();
                    }

                    workerDlg.setInderminate(true);

                    DataMatrix m = tsproc.getYearlyMean(year);
                    loadData(m);

                } catch (SQLException ex) {
                } catch (IOException ex) {
                }
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
        workerDlg.setTask(new CancelableRunnable() {

            public void run() {
                try {

                    Object[] objects = timeList.getSelectedValues();

                    ArrayList<JAMSCalendar> dateList = new ArrayList<JAMSCalendar>();
                    for (Object o : objects) {
                        dateList.add((JAMSCalendar) o);
                    }
                    JAMSCalendar[] dates = dateList.toArray(new JAMSCalendar[dateList.size()]);

                    DataMatrix m = null;

                    // check if number of selected ids is equal to all ids
                    // if so, we better derive temp avg from monthly means
                    if (dates.length == timeList.getModel().getSize()) {
                        // check if cache tables are available
                        if (!tsproc.isMonthlyMeanExisiting()) {
                            tsproc.calcMonthlyMean();
                        }
                        workerDlg.setInderminate(true);


                        if (!tsproc.isMonthlyMeanExisiting()) {
                            return;
                        }

                        m = tsproc.getTemporalAvg();

                    } else {

                        m = tsproc.getTemporalMean(dates);

                    }
                    loadData(m);

                } catch (SQLException ex) {
                } catch (IOException ex) {
                }
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
        workerDlg.setTask(new CancelableRunnable() {

            public void run() {
                try {

                    Object[] objects = entityList.getSelectedValues();

                    long[] ids = new long[objects.length];
                    int c = 0;
                    for (Object o : objects) {
                        ids[c++] = (Long) o;
                    }

                    DataMatrix m = null;

                    // check if number of selected ids is equal to all ids
                    // if so, we better derive temp avg from monthly means
                    if (ids.length == entityList.getModel().getSize()) {
                        // check if cache tables are available
                        if (!tsproc.isSpatMeanExisiting()) {
                            tsproc.calcSpatialMean();
                        }
                        workerDlg.setInderminate(true);

                        if (!tsproc.isSpatMeanExisiting()) {
                            return;
                        }

                        m = tsproc.getSpatialMean();

                    } else {

                        m = tsproc.getSpatialMean(ids);

                    }
                    loadData(m);

                } catch (SQLException ex) {
                } catch (IOException ex) {
                }
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
        workerDlg.setTask(new CancelableRunnable() {

            public void run() {
                try {
                    String filter = timeField.getText();

                    DataMatrix m = tsproc.getTemporalMean(filter);

                    loadData(m);

                } catch (SQLException ex) {
                    ex.printStackTrace();
                } catch (IOException ex) {
                }
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
        }
    }

    private void loadData(DataMatrix m) {
        m.output();
    }
}
