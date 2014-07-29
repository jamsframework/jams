/*
 * TimeSpaceDSPanel.java
 * Created on 12. Februar 2009, 09:18
 *
 * This file is part of JAMS
 * Copyright (C) FSU Jena
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public License
 * as published by the Free Software Foundation; either version 3
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, see <http://www.gnu.org/licenses/>.
 *
 */
package jams.explorer.gui;

import jams.JAMS;
import jams.JAMSLogging;
import jams.data.JAMSCalendar;
import jams.gui.tools.GUIHelper;
import jams.tools.StringTools;
import jams.workspace.dsproc.AbstractDataStoreProcessor;
import jams.workspace.dsproc.AbstractDataStoreProcessor.AttributeData;
import jams.workspace.dsproc.DataMatrix;
import java.awt.Dimension;
import java.awt.GridBagLayout;
import java.awt.Image;
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
import java.util.logging.LogManager;
import java.util.logging.Logger;
import javax.swing.AbstractAction;
import javax.swing.AbstractListModel;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.DefaultComboBoxModel;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import jams.workspace.dsproc.DataStoreProcessor;
import jams.workspace.dsproc.TimeSpaceProcessor;
import jams.workspace.stores.ShapeFileDataStore;
import jams.worldwind.data.DataTransfer3D;
import jams.worldwind.ui.view.GlobeView;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.net.URI;
import javax.swing.SwingWorker;
import jams.explorer.spreadsheet.JAMSSpreadSheet;

/**
 *
 * @author Sven Kralisch <sven.kralisch at uni-jena.de>
 */
public class TimeSpaceDSPanel extends DSPanel {

    private static final Dimension LIST_DIMENSION = new Dimension(300, 250);
    private TimeSpaceProcessor proc;
    private GridBagLayout mainLayout;
    private JList timeList, entityList, monthList, yearList;
    private JTextField timeField;
    private JPanel outerPanel, aggregationPanel;
    private GridBagLayout aggregationLayout;
    private final HashMap<String, AttribRadioButton> defaultWeightingMap = new HashMap<String, AttribRadioButton>();
    private final Action[] actions = {
        new AbstractAction(JAMS.i18n("TIME_STEP")) {

            @Override
            public void actionPerformed(ActionEvent e) {
                showTimeStep();
            }
        },
        new AbstractAction(JAMS.i18n("TEMP._MEAN")) {

            @Override
            public void actionPerformed(ActionEvent e) {
                showTempMean();
            }
        },
        new AbstractAction(JAMS.i18n("SPATIAL_ENTITY")) {

            @Override
            public void actionPerformed(ActionEvent e) {
                showSpatEntity();
            }
        },
        new AbstractAction(JAMS.i18n("SPATIAL_MEAN")) {

            @Override
            public void actionPerformed(ActionEvent e) {
                showSpatEntity();
            }
        },
        new AbstractAction(JAMS.i18n("CROSSPRODUCT")) {

            @Override
            public void actionPerformed(ActionEvent e) {
                showCrossProduct();
            }
        },
        new AbstractAction(JAMS.i18n("TO_WW")) {

            @Override
            public void actionPerformed(ActionEvent e) {
                showInWorldWind();
            }
        },
        new AbstractAction(JAMS.i18n("MONTHLY_MEAN")) {

            @Override
            public void actionPerformed(ActionEvent e) {
                showMonthlyMean();
            }
        },
        new AbstractAction(JAMS.i18n("YEARLY_MEAN")) {

            @Override
            public void actionPerformed(ActionEvent e) {
                showYearlyMean();
            }
        }
    };
    private final Action timePoint = actions[0], timeMean = actions[1], 
            spacePoint = actions[2], spaceMean = actions[3], 
            crossProduct = actions[4], toWW = actions[5], 
            monthMean = actions[6], yearMean = actions[7];
    private final Action cacheReset = new AbstractAction(JAMS.i18n("RESET_CACHES")) {

        @Override
        public void actionPerformed(ActionEvent e) {
            resetCaches();
        }
    };
    private final Action indexReset = new AbstractAction(JAMS.i18n("RELOAD_INDEX")) {

        @Override
        public void actionPerformed(ActionEvent e) {
            resetIndex();
        }
    };
    private final Action freeTempMean = new AbstractAction(JAMS.i18n("TEMP._MEAN_(FILTER)")) {

        @Override
        public void actionPerformed(ActionEvent e) {
            showFreeTempMean();
        }
    };

    public TimeSpaceDSPanel() {
        init();
    }

    private void init() {

        JAMSLogging.registerLogger(Logger.getLogger(TimeSpaceDSPanel.class.getName()));
        
        for (Action a : actions) {
            a.setEnabled(false);
        }

        freeTempMean.setEnabled(false);
        cacheReset.setEnabled(false);
        indexReset.setEnabled(false);

        outerPanel = new JPanel();

        mainLayout = new GridBagLayout();
        outerPanel.setLayout(mainLayout);

        timeList = new JList();
        JScrollPane timeListScroll = new JScrollPane(timeList);
//        timeListScroll.setPreferredSize(LIST_DIMENSION);
        timeList.addListSelectionListener(new ListSelectionListener() {

            public void valueChanged(ListSelectionEvent e) {
                if (!e.getValueIsAdjusting()) {
                    if (timeList.getSelectedValues().length == 1) {
                        timePoint.setEnabled(true);
                        timeMean.setEnabled(false);
                        if (entityList.getSelectedValues().length > 0) {
                            crossProduct.setEnabled(true);
                            toWW.setEnabled(true);
                        }
                    } else if (timeList.getSelectedValues().length > 1) {
                        timePoint.setEnabled(false);
                        timeMean.setEnabled(true);
                        if (entityList.getSelectedValues().length > 0) {
                            crossProduct.setEnabled(true);
                            toWW.setEnabled(true);
                        }
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
//        entityListScroll.setPreferredSize(new Dimension(LIST_DIMENSION.width - 50, LIST_DIMENSION.height));
        entityList.addListSelectionListener(new ListSelectionListener() {

            public void valueChanged(ListSelectionEvent e) {
                if (!e.getValueIsAdjusting()) {
                    if (entityList.getSelectedValues().length == 1) {
                        spacePoint.setEnabled(true);
                        spaceMean.setEnabled(false);
                        if (timeList.getSelectedValues().length > 0) {
                            crossProduct.setEnabled(true);
                            toWW.setEnabled(true);
                        }
                    } else if (entityList.getSelectedValues().length > 1) {
                        spacePoint.setEnabled(false);
                        spaceMean.setEnabled(true);
                        if (timeList.getSelectedValues().length > 0) {
                            crossProduct.setEnabled(true);
                            toWW.setEnabled(true);
                        }
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
//        monthListScroll.setPreferredSize(new Dimension(LIST_DIMENSION.width - 100, LIST_DIMENSION.height));
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
//        yearListScroll.setPreferredSize(new Dimension(LIST_DIMENSION.width - 100, LIST_DIMENSION.height));
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

        GUIHelper.addGBComponent(outerPanel, mainLayout, new JLabel(JAMS.i18n("ATTRIBUTE/AGGREGATION:")), 0, 10, 1, 1, 0, 0);

        aggregationLayout = new GridBagLayout();
        aggregationPanel = new JPanel();
        aggregationPanel.setLayout(aggregationLayout);
        JScrollPane aggregationScroll = new JScrollPane(aggregationPanel);
        aggregationScroll.setPreferredSize(LIST_DIMENSION);

        GUIHelper.addGBComponent(outerPanel, mainLayout, aggregationScroll, 0, 20, 1, 1, 0, 0);
        GUIHelper.addGBComponent(outerPanel, mainLayout, new JLabel(JAMS.i18n("TIME_STEPS:")), 10, 10, 1, 1, 0, 0);
        GUIHelper.addGBComponent(outerPanel, mainLayout, timeListScroll, 10, 20, 1, 1, 0, 0);
        GUIHelper.addGBComponent(outerPanel, mainLayout, new JLabel(JAMS.i18n("ENTITIY_IDS:")), 20, 10, 1, 1, 0, 0);
        GUIHelper.addGBComponent(outerPanel, mainLayout, entityListScroll, 20, 20, 1, 1, 0, 0);

        JPanel buttonPanelA = new JPanel();
        GridBagLayout panelALayout = new GridBagLayout();
        buttonPanelA.setLayout(panelALayout);
//        buttonPanelA.setPreferredSize(LIST_DIMENSION);
        JButton button;

        for (int i = 0; i <= 5; i++) {
            Action a = actions[i];
            button = new JButton(a);
            GUIHelper.addGBComponent(buttonPanelA, panelALayout, button, 0, i, 1, 1, 0, 0);
        }

        JPanel filterPanel = new JPanel();
        GridBagLayout filterPanelLayout = new GridBagLayout();
        filterPanel.setLayout(filterPanelLayout);
//        filterPanel.setPreferredSize(new Dimension(LIST_DIMENSION.width, LIST_DIMENSION.height - 150));
        filterPanel.setBorder(BorderFactory.createEtchedBorder());

        GUIHelper.addGBComponent(filterPanel, filterPanelLayout, new JLabel(JAMS.i18n("TIME_FILTER:")), 0, 0, 1, 1, 0, 0);
        timeField = new JTextField();
        timeField.setEnabled(false);
        timeField.setToolTipText(JAMS.i18n("DATE_EXPRESSION_IN_SQL_SYNTAX,_E.G._1992-11-%_FOR_ALL_NOVEMBER_VALUES_IN_1992"));
//        timeField.setPreferredSize(new Dimension(ACTION_BUTTON_DIM.width - 20, timeField.getPreferredSize().height));
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

        GUIHelper.addGBComponent(filterPanel, filterPanelLayout, timeField, 0, 10, 1, 1, 0, 0);

        button = new JButton(freeTempMean);
        //button.setPreferredSize(new Dimension(ACTION_BUTTON_DIM.width - 20, ACTION_BUTTON_DIM.height));
        GUIHelper.addGBComponent(filterPanel, filterPanelLayout, button, 0, 20, 1, 1, 0, 0);

        GUIHelper.addGBComponent(buttonPanelA, panelALayout, filterPanel, 0, 10, 1, 1, 0, 0);

        GUIHelper.addGBComponent(outerPanel, mainLayout, buttonPanelA, 40, 20, 1, 1, 0, 0);

        /*        
         GUIHelper.addGBComponent(outerPanel, mainLayout, new JLabel(JAMS.i18n("MONTHS:")), 60, 10, 1, 1, 0, 0);
         GUIHelper.addGBComponent(outerPanel, mainLayout, monthListScroll, 60, 20, 1, 1, 0, 0);
         GUIHelper.addGBComponent(outerPanel, mainLayout, new JLabel(JAMS.i18n("YEARS:")), 70, 10, 1, 1, 0, 0);
         GUIHelper.addGBComponent(outerPanel, mainLayout, yearListScroll, 70, 20, 1, 1, 0, 0);

         JPanel buttonPanelB = new JPanel();
         GridBagLayout panelBLayout = new GridBagLayout();
         buttonPanelB.setLayout(panelBLayout);
         //        buttonPanelB.setPreferredSize(LIST_DIMENSION);

         for (int i = 5; i < actions.length; i++) {
         Action a = actions[i];
         button = new JButton(a);
         GUIHelper.addGBComponent(buttonPanelB, panelBLayout, button, 0, i, 1, 1, 0, 0);
         }

         button = new JButton(cacheReset);
         //        button.setPreferredSize(ACTION_BUTTON_DIM);
         //        GUIHelper.addGBComponent(buttonPanelB, panelBLayout, button, 0, 10, 1, 1, 0, 0);

         button = new JButton(indexReset);
         //        button.setPreferredSize(ACTION_BUTTON_DIM);
         GUIHelper.addGBComponent(buttonPanelB, panelBLayout, button, 0, 20, 1, 1, 0, 0);

         GUIHelper.addGBComponent(outerPanel, mainLayout, buttonPanelB, 80, 20, 1, 1, 0, 0);
         */
        this.add(outerPanel);

    }

//    private void syncButton(ArrayList<JButton> buttonList) {
//
//        int maxW = Integer.MIN_VALUE;
//        int maxH = Integer.MIN_VALUE;
//        for (JButton b : buttonList) {
//            maxW = Math.max(maxW, b.getPreferredSize().width);
//            maxH = Math.max(maxH, b.getPreferredSize().height);
//        }
//
//        Dimension d = new Dimension(maxW, maxH);
//
//        for (JButton b : buttonList) {
//            b.setPreferredSize(d);
//        }
//    }
    public static void main(String[] args) throws Exception {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception evt) {
        }
        TimeSpaceDSPanel tsp = new TimeSpaceDSPanel();
        JFrame frame = new JFrame();
        JScrollPane scroll = new JScrollPane(tsp);
        frame.add(scroll);
//        tsp.setExplorer(frame);
        frame.setPreferredSize(new Dimension(300, 100));
        frame.pack();
        frame.setVisible(true);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        LogManager.getLogManager().reset();
        Logger globalLogger = Logger.getLogger("");
        GUIHelper.setupLogHandler(globalLogger, null);

        DataStoreProcessor dsdb = new DataStoreProcessor(new File("e:/jamsapplication/JAMS-Gehlberg/output/current/HRULoop.dat"));
        //dsdb.removeDB();
        dsdb.addImportProgressObserver(new Observer() {

            public void update(Observable o, Object arg) {
                System.out.println("IMPORT PROGRESS: " + arg);
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
    public TimeSpaceProcessor getProc() {
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
                    Logger.getLogger(TimeSpaceDSPanel.class.getName()).log(Level.SEVERE, null, ex);
                } catch (SQLException ex) {
                    Logger.getLogger(TimeSpaceDSPanel.class.getName()).log(Level.SEVERE, null, ex);
                } catch (ClassNotFoundException ex) {
                    Logger.getLogger(TimeSpaceDSPanel.class.getName()).log(Level.SEVERE, null, ex);
                }
                return null;
            }
        });

        try {
            if (!dsdb.existsH2DB() || dsdb.isDBObsolete()) {
                workerDlg.execute();
            }

            if (!dsdb.existsH2DB()) {
                clearPanel();
            }

            this.setTsProc(new TimeSpaceProcessor(dsdb));

        } catch (SQLException ex) {
            Logger.getLogger(TimeSpaceDSPanel.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(TimeSpaceDSPanel.class.getName()).log(Level.SEVERE, null, ex);
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

    private void setTsProc(TimeSpaceProcessor tsproc) throws SQLException, IOException {
        this.proc = tsproc;

        timeList.setModel(new AbstractListModel() {

            JAMSCalendar[] dates = ((TimeSpaceProcessor) getProc()).getTimeSteps();

            public int getSize() {
                return dates.length;
            }

            public Object getElementAt(int i) {
                return dates[i];
            }
        });

        entityList.setModel(new AbstractListModel() {

            Long[] ids = ((TimeSpaceProcessor) getProc()).getEntityIDs();

            public int getSize() {
                return ids.length;
            }

            public Object getElementAt(int i) {
                return ids[i];
            }
        });

        yearList.setModel(new AbstractListModel() {

            int[] years = ((TimeSpaceProcessor) getProc()).getYears();

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

        label = new JLabel(JAMS.i18n("AREA_ATTRIBUTE"));
        label.setHorizontalAlignment(SwingConstants.LEFT);
        GUIHelper.addGBComponent(aggregationPanel, aggregationLayout, label, 5, 0, 1, 1, 0, 0);

        ArrayList<DataStoreProcessor.AttributeData> attribs = getProc().getDataStoreProcessor().getAttributes();

//        label = new JLabel(JAMS.i18n("AGGREGATION_WEIGHT"));
//        label.setHorizontalAlignment(SwingConstants.CENTER);
//        GUIHelper.addGBComponent(aggregationPanel, aggregationLayout, label, 10, 3, 2, 1, 0, 0);
        Image image;
        float scale = 0.8f;

        image = new ImageIcon(getClass().getResource("jams/explorer/resources/images/jade_mean.png")).getImage();
        image = image.getScaledInstance((int) Math.round(image.getWidth(null) * scale), (int) Math.round(image.getHeight(null) * scale), Image.SCALE_SMOOTH);
        label = new JLabel(new ImageIcon(image));
//        label.setPreferredSize(labelDim);
        GUIHelper.addGBComponent(aggregationPanel, aggregationLayout, label, 10, 5, 1, 1, 0, 0);

        image = new ImageIcon(getClass().getResource("jams/explorer/resources/images/jade_sum.png")).getImage();
        image = image.getScaledInstance((int) Math.round(image.getWidth(null) * scale), (int) Math.round(image.getHeight(null) * scale), Image.SCALE_SMOOTH);
        label = new JLabel(new ImageIcon(image));
//        label.setPreferredSize(labelDim);
        GUIHelper.addGBComponent(aggregationPanel, aggregationLayout, label, 11, 5, 1, 1, 0, 0);

        image = new ImageIcon(getClass().getResource("jams/explorer/resources/images/jade_x.png")).getImage();
        image = image.getScaledInstance((int) Math.round(image.getWidth(null) * scale), (int) Math.round(image.getHeight(null) * scale), Image.SCALE_SMOOTH);
        label = new JLabel(new ImageIcon(image));
        GUIHelper.addGBComponent(aggregationPanel, aggregationLayout, label, 12, 5, 1, 1, 0, 0);

        image = new ImageIcon(getClass().getResource("jams/explorer/resources/images/jade_xdiva.png")).getImage();
        image = image.getScaledInstance((int) Math.round(image.getWidth(null) * scale), (int) Math.round(image.getHeight(null) * scale), Image.SCALE_SMOOTH);
        label = new JLabel(new ImageIcon(image));
        GUIHelper.addGBComponent(aggregationPanel, aggregationLayout, label, 13, 5, 1, 1, 0, 0);

        image = new ImageIcon(getClass().getResource("jams/explorer/resources/images/jade_xtimesa.png")).getImage();
        image = image.getScaledInstance((int) Math.round(image.getWidth(null) * scale), (int) Math.round(image.getHeight(null) * scale), Image.SCALE_SMOOTH);
        label = new JLabel(new ImageIcon(image));
        GUIHelper.addGBComponent(aggregationPanel, aggregationLayout, label, 14, 5, 1, 1, 0, 0);

        int i = 0;
        ArrayList<JCheckBox> allChecks = new ArrayList<JCheckBox>();
        for (DataStoreProcessor.AttributeData attrib : attribs) {

            AttribCheckBox attribCheck = new AttribCheckBox(attrib, attrib.getName());
            attribCheck.setSelected(attrib.isSelected());

            attribCheck.addItemListener(new ItemListener() {

                public void itemStateChanged(ItemEvent e) {
                    AttribCheckBox thisCheck = (AttribCheckBox) e.getSource();
                    if (!thisCheck.isSelected() && attribCombo.getSelectedItem().toString().equals(thisCheck.getText())) {
                        attribCombo.setSelectedIndex(0);
                        GUIHelper.showInfoDlg(parent, JAMS.i18n("AREA_ATTRIBUTE_HAS_BEEN_RESET!"), JAMS.i18n("INFO"));
                    }
                    thisCheck.attrib.setSelected(thisCheck.isSelected());
                }
            });

            allChecks.add(attribCheck);
            GUIHelper.addGBComponent(aggregationPanel, aggregationLayout, attribCheck, 5, i + 10, 1, 1, 0, 0);

            AttribRadioButton aggregationButton1, aggregationButton2, weightingButton1, weightingButton2, weightingButton3;
            aggregationButton1 = new AttribRadioButton(attrib, DataStoreProcessor.AttributeData.AGGREGATION_MEAN);
            aggregationButton2 = new AttribRadioButton(attrib, DataStoreProcessor.AttributeData.AGGREGATION_SUM);
            weightingButton1 = new AttribRadioButton(attrib, DataStoreProcessor.AttributeData.WEIGHTING_NONE);
            weightingButton2 = new AttribRadioButton(attrib, DataStoreProcessor.AttributeData.WEIGHTING_DIV_AREA);
            weightingButton3 = new AttribRadioButton(attrib, DataStoreProcessor.AttributeData.WEIGHTING_TIMES_AREA);

            aggregationButton1.setSelected(true);
            defaultWeightingMap.put(attrib.getName(), weightingButton1);
            weightingButton1.setSelected(true);

            ItemListener aggregationButtonListener = new ItemListener() {

                public void itemStateChanged(ItemEvent e) {
                    if (e.getStateChange() == ItemEvent.DESELECTED) {
                        return;
                    }
                    AttribRadioButton thisButton = (AttribRadioButton) e.getSource();
                    thisButton.attrib.setAggregationType(thisButton.processingType);
                    setCheckBox(thisButton.attrib.getName());

                }
            };

            ItemListener weightingButtonListener = new ItemListener() {

                public void itemStateChanged(ItemEvent e) {
                    if (e.getStateChange() == ItemEvent.DESELECTED) {
                        return;
                    }

                    AttribRadioButton thisButton = (AttribRadioButton) e.getSource();

                    if ((attribCombo.getSelectedIndex() == 0) && (thisButton.processingType != DataStoreProcessor.AttributeData.WEIGHTING_NONE)) {
                        AttribRadioButton defaultButton = defaultWeightingMap.get(thisButton.attrib.getName());

                        GUIHelper.showInfoDlg(parent, String.format(JAMS.i18n("NO_AREA_ATTRIBUTE_HAS_BEEN_CHOSEN!_SKIPPING_WEIGHTED_AGGREGATION_FOR_ATTRIBUTE"), thisButton.attrib.getName()));
                        if (defaultButton != null) {
                            defaultButton.setSelected(true);
                        }
                        return;
                    }
                    thisButton.attrib.setWeightingType(thisButton.processingType);
                    setCheckBox(thisButton.attrib.getName());
                }
            };

            aggregationButton1.addItemListener(aggregationButtonListener);
            aggregationButton2.addItemListener(aggregationButtonListener);
            weightingButton1.addItemListener(weightingButtonListener);
            weightingButton2.addItemListener(weightingButtonListener);
            weightingButton3.addItemListener(weightingButtonListener);

            ButtonGroup bGroup1 = new ButtonGroup();
            bGroup1.add(aggregationButton1);
            bGroup1.add(aggregationButton2);

            ButtonGroup bGroup2 = new ButtonGroup();
            bGroup2.add(weightingButton1);
            bGroup2.add(weightingButton2);
            bGroup2.add(weightingButton3);

            GUIHelper.addGBComponent(aggregationPanel, aggregationLayout, aggregationButton1, 10, i + 10, 1, 1, 0, 0);
            GUIHelper.addGBComponent(aggregationPanel, aggregationLayout, aggregationButton2, 11, i + 10, 1, 1, 0, 0);
            GUIHelper.addGBComponent(aggregationPanel, aggregationLayout, weightingButton1, 12, i + 10, 1, 1, 0, 0);
            GUIHelper.addGBComponent(aggregationPanel, aggregationLayout, weightingButton2, 13, i + 10, 1, 1, 0, 0);
            GUIHelper.addGBComponent(aggregationPanel, aggregationLayout, weightingButton3, 14, i + 10, 1, 1, 0, 0);

            i++;
        }

        String[] attribNames = new String[attribs.size() + 1];
        attribNames[0] = JAMS.i18n("[CHOOSE]");
        i = 1;
        for (DataStoreProcessor.AttributeData attrib : attribs) {
            attribNames[i++] = attrib.getName();
        }

        attribCombo = new AttribComboBox(allChecks);
        attribCombo.setModel(new DefaultComboBoxModel(attribNames));
        attribCombo.addItemListener(new ItemListener() {

            public void itemStateChanged(ItemEvent e) {
                AttribComboBox thisCombo = (AttribComboBox) e.getSource();
                if (thisCombo.getSelectedIndex() != 0) {
                    setCheckBox(thisCombo.getSelectedItem().toString());
                } else {
                    for (AttribRadioButton b : defaultWeightingMap.values()) {
                        b.setSelected(true);
                    }
                }
            }
        });
        GUIHelper.addGBComponent(aggregationPanel, aggregationLayout, attribCombo, 10, 0, 5, 1, 0, 0);

        GroupCheckBox allOnOffCheck = new GroupCheckBox(JAMS.i18n("ALL_ON/OFF"), allChecks);
        allOnOffCheck.setSelected(DataStoreProcessor.AttributeData.SELECTION_DEFAULT);

        GUIHelper.addGBComponent(aggregationPanel, aggregationLayout, allOnOffCheck, 5, 5, 1, 1, 0, 0);

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

    private void clearPanel() {
        timeList.setEnabled(false);
        entityList.setEnabled(false);
        yearList.setEnabled(false);
        monthList.setEnabled(false);
        cacheReset.setEnabled(false);
        timeField.setEnabled(false);
        indexReset.setEnabled(false);
        toWW.setEnabled(false);
    }

    private void showInWorldWind() {
        workerDlg.setInderminate(false);
        workerDlg.setProgress(0);
        final ArrayList<JCheckBox> attribs = attribCombo.checkBoxList;

        workerDlg.setTask(new CancelableSwingWorker() {

            int progress;
            DataMatrix[] m;
            DataTransfer3D transfer;
            ArrayList<String> attributeNames = new ArrayList();

            @Override
            public Void doInBackground() {
                addPropertyChangeListener(new PropertyChangeListener() {

                    @Override
                    public void propertyChange(PropertyChangeEvent evt) {
                        if (evt.getPropertyName().equals("progress")) {
                            workerDlg.setProgress((Integer) evt.getNewValue());
                        }
                    }
                });

                ArrayList<AttributeData> attribList = proc.getDataStoreProcessor().getAttributes();

                //save selected attributes
                for (int i = 0; i < attribs.size(); i++) {
                    JCheckBox cbox = (JCheckBox) attribs.get(i);
                    if (cbox.isSelected()) {
                        for (AbstractDataStoreProcessor.AttributeData attribute : attribList) {
                            if (attribute.getName().equals(cbox.getText())) {
                                attributeNames.add(cbox.getText());
                            }
                        }
                    }
                }

                if (attributeNames.isEmpty()) {
                    GUIHelper.showInfoDlg(parent, "Please select one or more attributes!", "SELECT ATTRIBUTES");
                    return null;
                }

                progress = 5;
                setProgress(progress);

                try {

                    Object[] entities = entityList.getSelectedValuesList().toArray();
                    Object[] times = timeList.getSelectedValuesList().toArray();
                    String[] entitiesString = new String[entities.length];
                    String[] attribs = attributeNames.toArray(new String[attributeNames.size()]);

                    int k = 0;
                    long[] entityIds = new long[entities.length];
                    for (Object id : entities) {
                        entityIds[k] = Long.parseLong(id.toString());
                        entitiesString[k] = id.toString();
                        k++;
                    }

                    k = 0;
                    String[] dateIds = new String[times.length];
                    for (Object date : times) {
                        dateIds[k++] = (String) date.toString();
                    }

                    m = new DataMatrix[attributeNames.size()];
                    for (int j = 0; j < attributeNames.size(); j++) {
                        for (AbstractDataStoreProcessor.AttributeData attribute : attribList) {
                            if (attribute.getName().equals(attributeNames.get(j))) {
                                attribute.setSelected(true);
                                m[j] = proc.getCrossProduct(entityIds, dateIds);
                                attribute.setSelected(false);
                            }
                        }
                        progress = progress + ((j + 1) * (50 - progress)) / attributeNames.size();
                        setProgress(progress);
                    }

                    transfer = new DataTransfer3D(m, entitiesString, dateIds, attribs);
                    setProgress(100);

                } catch (SQLException ex) {
                    explorer.getRuntime().handle(ex);
                } catch (IOException ex) {
                    explorer.getRuntime().handle(ex);
                }
                return null;
            }

            @Override
            public void done() {

                java.awt.EventQueue.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        
                        if (transfer == null) {
                            return;
                        }

                        String selectedShape = (String) outputSpreadSheet.getShapeSelector().getSelectedItem();
                        if (StringTools.isEmptyString(selectedShape)) {
                            Logger.getLogger(JAMSSpreadSheet.class.getName()).log(Level.WARNING, "No shape selected.");
                            return;  // errorMessage?
                        }

                        ShapeFileDataStore dataStore = (ShapeFileDataStore) explorer.getWorkspace().getInputDataStore(selectedShape);
                        if (dataStore == null) {
                            Logger.getLogger(JAMSSpreadSheet.class.getName()).log(Level.WARNING, "No datastore found.");
                            return;
                        }

                        URI uri = dataStore.getUri();
                        if (uri == null) {
                            Logger.getLogger(JAMSSpreadSheet.class.getName()).log(Level.WARNING, "error: can't access shapefile! path is: "
                                    + dataStore.getShapeFile().getAbsolutePath());
                            return;
                        }

                        // we want to log, but without graphical output..
                        JAMSLogging.unregisterLogger(Logger.getLogger(JAMSSpreadSheet.class.getName()));
                        Logger.getLogger(JAMSSpreadSheet.class.getName()).log(Level.INFO, "Using shape file >" + selectedShape + "< / KEY COLUMN: " + dataStore.getKeyColumn());
                        JAMSLogging.registerLogger(Logger.getLogger(JAMSSpreadSheet.class.getName()));

                        transfer.setShapeFileDataStore(dataStore);

                        //ShapeFileDataStore shapeDataStore = (ShapeFileDataStore) explorer.getWorkspace().;
                        GlobeView view = GlobeView.getInstance();
                        boolean result = view.addJAMSExplorerData(transfer);
                        if (result) {
                            view.show();
                        }
                    }
                });

            }

            @Override
            public int cancel() {
                proc.sendAbortOperation();
                return 0;
            }
        }
        );
        workerDlg.execute();
    }

    private void showTimeStep() {

        if ((timeList.getSelectedValues().length == 0) || (timeList.getSelectedValues().length > 1)) {
            return;
        }

        workerDlg.setInderminate(true);

        workerDlg.setTask(new SwingWorker<Object, Void>() {

            DataMatrix m = null;
            int weightAttribIndex = -1;

            public Object doInBackground() {
                JAMSCalendar date = (JAMSCalendar) timeList.getSelectedValue();
                JAMSCalendar[] dates = {date};

                if (date == null) {
                    return m;
                }

                if (attribCombo.getSelectedIndex() != 0) {
                    weightAttribIndex = 0;
                    String weightAttribName = attribCombo.getSelectedItem().toString();
                    for (DataStoreProcessor.AttributeData attrib : dsdb.getAttributes()) {
                        if (attrib.getName().equals(weightAttribName)) {
                            break;
                        }
                        if (attrib.isSelected()) {
                            weightAttribIndex++;
                        }
                    }
                }

                try {

                    m = getProc().getTemporalAggregate(dates, weightAttribIndex);

                } catch (SQLException ex) {
                    Logger.getLogger(TimeSpaceDSPanel.class
                            .getName()).log(Level.SEVERE, null, ex);
                } catch (IOException ex) {
                    Logger.getLogger(TimeSpaceDSPanel.class
                            .getName()).log(Level.SEVERE, null, ex);
                } catch (Throwable ex) {
                    Logger.getLogger(TimeSpaceDSPanel.class
                            .getName()).log(Level.SEVERE, null, ex);
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

    private void showTempMean() {

        if (timeList.getSelectedValues().length == 0) {
            return;
        }

        workerDlg.setInderminate(false);
        workerDlg.setProgress(0);
        workerDlg.setTask(new CancelableSwingWorker() {

            DataMatrix m = null;
            int weightAttribIndex = -1;

            public Object doInBackground() {
                try {

                    Object[] objects = timeList.getSelectedValues();

                    ArrayList<JAMSCalendar> dateList = new ArrayList<JAMSCalendar>();
                    for (Object o : objects) {
                        dateList.add((JAMSCalendar) o);
                    }
                    JAMSCalendar[] dates = dateList.toArray(new JAMSCalendar[dateList.size()]);

                    if (attribCombo.getSelectedIndex() != 0) {
                        weightAttribIndex = 0;
                        String weightAttribName = attribCombo.getSelectedItem().toString();
                        for (DataStoreProcessor.AttributeData attrib : dsdb.getAttributes()) {
                            if (attrib.getName().equals(weightAttribName)) {
                                break;
                            }
                            if (attrib.isSelected()) {
                                weightAttribIndex++;
                            }
                        }
                    }

                    TimeSpaceProcessor tsproc = getProc();

                    // check if number of selected ids is equal to all ids
                    // if so, we better derive temp avg from monthly means
                    if (false && dates.length == timeList.getModel().getSize()) {

                        tsproc.deleteCache();

                        // check if cache tables are available
                        if (!tsproc.isMonthlyMeanExisiting()) {
                            tsproc.calcMonthlyMean();
                        }
                        workerDlg.setInderminate(true);

                        if (!tsproc.isMonthlyMeanExisiting()) {
                            return null;
                        }

                        m = tsproc.getTemporalMean();
                    } else {

                        m = tsproc.getTemporalAggregate(dates, weightAttribIndex);

                    }
                } catch (SQLException ex) {
                    Logger.getLogger(TimeSpaceDSPanel.class
                            .getName()).log(Level.SEVERE, null, ex);
                } catch (IOException ex) {
                    Logger.getLogger(TimeSpaceDSPanel.class
                            .getName()).log(Level.SEVERE, null, ex);
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

                    m = getProc().getMonthlyMean(month);
//                    workerDlg.setInderminate(true);

                } catch (SQLException ex) {
                    Logger.getLogger(TimeSpaceDSPanel.class
                            .getName()).log(Level.SEVERE, null, ex);
                } catch (IOException ex) {
                    Logger.getLogger(TimeSpaceDSPanel.class
                            .getName()).log(Level.SEVERE, null, ex);
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

            @Override
            public Object doInBackground() {
                try {

                    int year = (Integer) yearList.getSelectedValue();

//                    if (!proc.isYearlyMeanExisiting()) {
//                        proc.calcYearlyMean();
//                    }
                    m = proc.getYearlyMean(year);
//                    workerDlg.setInderminate(true);

                } catch (SQLException ex) {
                    Logger.getLogger(TimeSpaceDSPanel.class
                            .getName()).log(Level.SEVERE, null, ex);
                } catch (IOException ex) {
                    Logger.getLogger(TimeSpaceDSPanel.class
                            .getName()).log(Level.SEVERE, null, ex);
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
        if (entityList.getSelectedValues().length == 0) {
            return;
        }

        workerDlg.setInderminate(false);
        workerDlg.setProgress(0);
        workerDlg.setTask(new CancelableSwingWorker() {

            DataMatrix m = null;
            int weightAttribIndex = -1;

            public Object doInBackground() {
                try {
                    TimeSpaceProcessor tsproc = getProc();
                    // check if number of selected ids is equal to all ids
                    // if so, we better derive temp avg from monthly means
                    Object[] objects = entityList.getSelectedValues();

                    // get the position of the weight attribute, if existing
                    if (attribCombo.getSelectedIndex() != 0) {
                        weightAttribIndex = 0;
                        String weightAttribName = attribCombo.getSelectedItem().toString();
                        for (DataStoreProcessor.AttributeData attrib : dsdb.getAttributes()) {
                            if (attrib.getName().equals(weightAttribName)) {
                                break;
                            }
                            if (attrib.isSelected()) {
                                weightAttribIndex++;
                            }
                        }
                    }

                    long[] ids = new long[objects.length];
                    int c = 0;
                    for (Object o : objects) {
                        ids[c++] = (Long) o;
                    }

                    if (false && ids.length == entityList.getModel().getSize()) {

                        tsproc.deleteCache();

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
                        m = tsproc.getSpatialSum(ids, weightAttribIndex);

                    }
                } catch (SQLException ex) {
                    Logger.getLogger(TimeSpaceDSPanel.class
                            .getName()).log(Level.SEVERE, null, ex);
                } catch (IOException ex) {
                    Logger.getLogger(TimeSpaceDSPanel.class
                            .getName()).log(Level.SEVERE, null, ex);
                }
                return null;
            }

            @Override
            public
                    void done() {

                if (m == null) {

                    if (weightAttribIndex < 0) {
                        Logger.getLogger(TimeSpaceDSPanel.class
                                .getName()).log(Level.WARNING, "A weight attribute must be chosen!");
                    } else {
                        Logger.getLogger(TimeSpaceDSPanel.class
                                .getName()).log(Level.WARNING, "An error occured during data extraction!");
                    }
                }

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
            int weightAttribIndex = -1;

            public Object doInBackground() {
                try {
                    String filter = timeField.getText();

                    if (attribCombo.getSelectedIndex() != 0) {
                        weightAttribIndex = 0;
                        String weightAttribName = attribCombo.getSelectedItem().toString();
                        for (DataStoreProcessor.AttributeData attrib : dsdb.getAttributes()) {
                            if (attrib.getName().equals(weightAttribName)) {
                                break;
                            }
                            if (attrib.isSelected()) {
                                weightAttribIndex++;
                            }
                        }
                    }

                    m = getProc().getTemporalAggregate(filter, weightAttribIndex);

                } catch (SQLException ex) {
                    Logger.getLogger(TimeSpaceDSPanel.class
                            .getName()).log(Level.SEVERE, null, ex);
                } catch (IOException ex) {
                    Logger.getLogger(TimeSpaceDSPanel.class
                            .getName()).log(Level.SEVERE, null, ex);
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
                    Object[] objects2 = timeList.getSelectedValues();

                    String[] ids2 = new String[objects2.length];
                    for (int c = 0; c < ids2.length; c++) {
                        ids2[c] = ((JAMSCalendar) objects2[c]).toString();
                    }

                    Object[] objects3 = entityList.getSelectedValues();

                    long[] ids3 = new long[objects3.length];
                    for (int c = 0; c < ids3.length; c++) {
                        ids3[c] = (Long) objects3[c];
                    }
                    m = getProc().getCrossProduct(ids3, ids2);

                } catch (SQLException ex) {
                    Logger.getLogger(TimeSpaceDSPanel.class
                            .getName()).log(Level.SEVERE, null, ex);
                } catch (IOException ex) {
                    Logger.getLogger(TimeSpaceDSPanel.class
                            .getName()).log(Level.SEVERE, null, ex);
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
//        if (StringTools.isEmptyString(filter)) {
        if (!filter.contains("%") && !filter.contains("?")) {
            freeTempMean.setEnabled(false);
        } else {
            freeTempMean.setEnabled(true);
        }
    }

    private void resetCaches() {
        try {
            getProc().deleteCache();

        } catch (SQLException ex) {
            Logger.getLogger(TimeSpaceDSPanel.class
                    .getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void createProc(File file) {

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
}
