/*
 * DSPanel.java
 * Created on 7. September 2009, 08:41
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

import jams.gui.tools.GUIHelper;
import java.awt.Frame;
import java.io.File;
import java.util.ArrayList;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import reg.dsproc.DataStoreProcessor;
import reg.dsproc.DataMatrix;
import reg.spreadsheet.JAMSSpreadSheet;

/**
 *
 * @author Christian Fischer
 */
public abstract class DSPanel extends JPanel {

    protected DataStoreProcessor dsdb;
    protected JAMSSpreadSheet outputSpreadSheet;
    protected Frame parent;
    protected CancelableWorkerDlg workerDlg;
    protected File outputDSFile;
    protected AttribComboBox attribCombo;

    public void setParent(Frame parent) {
        this.parent = parent;
        workerDlg = new CancelableWorkerDlg(parent, "Processing data");
        workerDlg.setProgress(0);
        workerDlg.setProgressMax(100);
    }

    protected void loadData(DataMatrix m, boolean timeSeries) {

        if (m == null) {
            return;
        }

        //postProcess(m, timeSeries);

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

        ArrayList<DataStoreProcessor.AttributeData> attribs = dsdb.getAttributes();
        int j = 0;
        for (DataStoreProcessor.AttributeData attrib : attribs) {

            if (!attrib.isSelected()) {
                continue;
            }

            if (attrib.getAggregationType() != DataStoreProcessor.AttributeData.AGGREGATION_NONE) {

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
                    switch (attrib.getAggregationType()) {
                        case DataStoreProcessor.AttributeData.AGGREGATION_REL_WEIGHT:
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

    protected class AttribComboBox extends JComboBox {

        ArrayList<JCheckBox> checkBoxList;

        public AttribComboBox(ArrayList<JCheckBox> checkBoxList) {
            super();
            this.checkBoxList = checkBoxList;
        }
    }

    protected class AttribRadioButton extends JRadioButton {

        DataStoreProcessor.AttributeData attrib;
        int aggregationType;

        public AttribRadioButton(DataStoreProcessor.AttributeData attrib, int aggregationType) {
            super();
            this.attrib = attrib;
            this.aggregationType = aggregationType;
        }
    }

    protected class GroupCheckBox extends JCheckBox {

        ArrayList<JCheckBox> checkBoxList;

        public GroupCheckBox(String title, ArrayList<JCheckBox> checkBoxList) {
            super(title);
            this.checkBoxList = checkBoxList;
        }
    }

    protected class AttribCheckBox extends JCheckBox {

        DataStoreProcessor.AttributeData attrib;

        public AttribCheckBox(DataStoreProcessor.AttributeData attrib) {
            super(attrib.getName());
            this.attrib = attrib;
        }
    }
    
    public void setOutputSpreadSheet(JAMSSpreadSheet spreadsheet) {
        this.outputSpreadSheet = spreadsheet;
    }   
    
    abstract public void createProc(File file);
}
