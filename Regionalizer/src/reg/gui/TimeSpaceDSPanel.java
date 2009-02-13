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
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import reg.dsproc.DataMatrix;
import reg.dsproc.TimeSpaceProcessor;

/**
 *
 * @author Sven Kralisch <sven.kralisch at uni-jena.de>
 */
public class TimeSpaceDSPanel extends JPanel {

    private static final Dimension ACTION_BUTTON_DIM = new Dimension(140, 25);

    private TimeSpaceProcessor tsproc;

    private GridBagLayout mainLayout;

    private JList timeList,  entityList;

    private Action timeStepAction,  spaceEntityAction,  timeAvgAction,  spaceAvgAction,  monthlyAvgAction,  resetCacheAction;

    private CancelWorkerDlg workerDlg;

    private Frame parent;

    private Action[] actions = {
        timeStepAction = new AbstractAction("Time Step") {

    @Override
    public void actionPerformed(ActionEvent e) {
        showTimeStep();
    }
},
        spaceEntityAction = new AbstractAction("Spatial Entity") {

    @Override
    public void actionPerformed(ActionEvent e) {
    }
},
        timeAvgAction = new AbstractAction("Temporal Average") {

    @Override
    public void actionPerformed(ActionEvent e) {
        showTimeAvg();
    }
},
        spaceAvgAction = new AbstractAction("Spatial Average") {

    @Override
    public void actionPerformed(ActionEvent e) {
    }
},
        monthlyAvgAction = new AbstractAction("Monthly Average") {

    @Override
    public void actionPerformed(ActionEvent e) {
    }
},
        resetCacheAction = new AbstractAction("Reset Caches") {

    @Override
    public void actionPerformed(ActionEvent e) {
        resetCaches();
    }
}
    };

    public TimeSpaceDSPanel() {
        init();
    }

    private void init() {

        for (Action a : actions) {
            a.setEnabled(false);
        }

        mainLayout = new GridBagLayout();
        this.setLayout(mainLayout);

        timeList = new JList();
        JScrollPane timeListScroll = new JScrollPane(timeList);
        timeListScroll.setPreferredSize(new Dimension(150, 200));

        entityList = new JList();
        JScrollPane entityListScroll = new JScrollPane(entityList);
        entityListScroll.setPreferredSize(new Dimension(150, 200));

        LHelper.addGBComponent(this, mainLayout, new JLabel("Time Steps:"), 10, 10, 1, 1, 0, 0);
        LHelper.addGBComponent(this, mainLayout, timeListScroll, 10, 20, 1, 1, 0, 0);
        LHelper.addGBComponent(this, mainLayout, new JLabel("Spatial Entitiy IDs:"), 20, 10, 1, 1, 0, 0);
        LHelper.addGBComponent(this, mainLayout, entityListScroll, 20, 20, 1, 1, 0, 0);

        JPanel buttonPanel = new JPanel();
        buttonPanel.setPreferredSize(new Dimension(150, 200));

        for (Action a : actions) {
            JButton button = new JButton(a);
            button.setPreferredSize(ACTION_BUTTON_DIM);
            buttonPanel.add(button);
        }

        LHelper.addGBComponent(this, mainLayout, new JLabel("Data Selection:"), 30, 10, 1, 1, 0, 0);
        LHelper.addGBComponent(this, mainLayout, buttonPanel, 30, 20, 1, 1, 0, 0);
    }

    public void setParent(Frame parent) {
        this.parent = parent;
        workerDlg = new CancelWorkerDlg(parent, "Processing data");
        workerDlg.setProgress(0);
        workerDlg.setProgressMax(100);
    }

    public static void main(String[] args) throws Exception {
        TimeSpaceDSPanel tsp = new TimeSpaceDSPanel();
        JFrame frame = new JFrame();
        frame.add(tsp);
        tsp.setParent(frame);
        //frame.setPreferredSize(new Dimension(300, 100));
        frame.pack();
        frame.setVisible(true);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        TimeSpaceProcessor tsproc = new TimeSpaceProcessor("D:/jamsapplication/JAMS-Gehlberg/output/current/HRULoop_0.dat");
        tsproc.isTimeSpaceDatastore();
        tsp.setTsproc(tsproc);
        tsproc.close();
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

            Long[] dates = TimeSpaceDSPanel.this.getTsproc().getEntityIDs();

            public int getSize() {
                return dates.length;
            }

            public Object getElementAt(int i) {
                return dates[i];
            }
        });

        for (Action a : actions) {
            a.setEnabled(true);
        }

        tsproc.addProcessingProgressObserver(new Observer() {

            public void update(Observable o, Object arg) {
                System.out.println(arg);
                workerDlg.setProgress(Integer.parseInt(arg.toString()));
            }
        });

    }

    private void showTimeStep() {
        JAMSCalendar date = (JAMSCalendar) timeList.getSelectedValue();
        if (date == null) {
            return;
        }
        try {
            DataMatrix m = tsproc.getSpatialData(date);
            m.output();
        } catch (SQLException ex) {
        } catch (IOException ex) {
        }
    }

    private void showTimeAvg() {

        workerDlg.setInderminate(false);
        workerDlg.setProgress(0);

        if (timeList.getSelectedValues().length == 0) {
            return;
        }

        workerDlg.setTask(new Runnable() {

            public void run() {
                try {

                    Object[] objects = timeList.getSelectedValues();

                    ArrayList<JAMSCalendar> dateList = new ArrayList<JAMSCalendar>();
                    for (Object o : objects) {
                        dateList.add((JAMSCalendar) o);
                    }
                    JAMSCalendar[] dates = dateList.toArray(new JAMSCalendar[dateList.size()]);

                    DataMatrix m = null;

                    // check if number of selected dates is equal to all dates
                    // if so, we better derive temp avg from monthly means
                    if (dates.length == timeList.getModel().getSize()) {
                        // check if cache tables are available
                        if (!tsproc.isMonthlyAvgExisiting()) {
                            tsproc.calcMonthlyAvg();
                        } else {
                            workerDlg.setInderminate(true);
                        }

                        if (!tsproc.isMonthlyAvgExisiting()) {
                            return;
                        }

                        m = tsproc.getTemporalAvg();

                    } else {

                        m = tsproc.getTemporalAvg(dates);

                    }
                    m.output();
                } catch (SQLException ex) {
                } catch (IOException ex) {
                }
            }
        });
        workerDlg.execute();
    }

    private void resetCaches() {
        try {
            tsproc.deleteCache();
        } catch (SQLException ex) {
            
        }
    }
}
