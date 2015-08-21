/*
 * TSPlot.java
 * Created on 21. Juni 2006, 22:06
 *
 * This file is part of JAMS
 * Copyright (C) 2005 FSU Jena
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

package org.unijena.scs;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseEvent;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;

import javax.swing.AbstractButton;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.DateAxis;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.plot.DatasetRenderingOrder;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYAreaRenderer;
import org.jfree.chart.renderer.xy.XYBarRenderer;
import org.jfree.chart.renderer.xy.XYDifferenceRenderer;
import org.jfree.chart.renderer.xy.XYDotRenderer;
import org.jfree.chart.renderer.xy.XYItemRenderer;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.chart.renderer.xy.XYStepAreaRenderer;
import org.jfree.chart.renderer.xy.XYStepRenderer;
import org.jfree.data.time.Second;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;
import org.unijena.jams.data.Attribute.Boolean;
import org.unijena.jams.data.Attribute.Calendar;
import org.unijena.jams.data.Attribute.Double;
import org.unijena.jams.data.Attribute.Integer;
import org.unijena.jams.data.Attribute.String;
import org.unijena.jams.data.Attribute.StringArray;
import org.unijena.jams.model.JAMSGUIComponent;
import org.unijena.jams.model.JAMSVarDescription;

/**
 * plotting component reworked for JAMSSCS
 * @author S. Kralisch
 */
public class TSPlot extends JAMSGUIComponent {//implements MouseListener {
    
    /**
     * the plot's title string<br>
     * access: READ<br> 
     * update: INIT<br> 
     * unit: n/a
     */
    @JAMSVarDescription(
    access = JAMSVarDescription.AccessType.READ,
            description = "Title string for plot",
            defaultValue = ""
            )
            public Attribute.String plotTitle;
    
    /**
     * the static descriptor for the series displayed on the left Y-Axis<br>
     * access: READ<br> 
     * update: INIT<br> 
     * unit: n/a
     */
    @JAMSVarDescription(
    access = JAMSVarDescription.AccessType.READ,
            description = "Title strings for left graphs"
            )
            public Attribute.StringArray titleLeft;
    
    /**
     * a variable descriptor for the series displayed on the left Y-Axis<br>
     * access: READ<br> 
     * update: INIT<br> 
     * unit: n/a
     */
    @JAMSVarDescription(
    access = JAMSVarDescription.AccessType.READ,
            description = "Variable title strings for left graphs"
            )
            public Attribute.StringArray varTitleLeft;
    
    /**
     * the static descriptor for the series displayed on the right Y-Axis<br>
     * access: READ<br> 
     * update: INIT<br> 
     * unit: n/a
     */
    @JAMSVarDescription(
    access = JAMSVarDescription.AccessType.READ,
            description = "Title strings for right graphs"
            )
            public Attribute.StringArray titleRight;
    
    /**
     * a comma separated list of color names for the series drawn on the left Y-Axis<br>
     * access: READ<br> 
     * update: INIT<br> 
     * unit: n/a
     */
    @JAMSVarDescription(
    access = JAMSVarDescription.AccessType.READ,
            description = "Colors for left graphs (yellow, orange, red, pink, magenta, cyan, yellow, green, lightgray, gray, black)"
            )
            public Attribute.StringArray colorLeft;
    
    /**
     * a comma separated list of color names for the series drawn on the right Y-Axis<br>
     * access: READ<br> 
     * update: INIT<br> 
     * unit: n/a
     */
    @JAMSVarDescription(
    access = JAMSVarDescription.AccessType.READ,
            description = "Colors for right graphs (yellow, orange, red, pink, magenta, cyan, yellow, green, lightgray, gray, black)"
            )
            public Attribute.StringArray colorRight;
    
    /**
     * selector for the plot type drawn on the left axis<br>
     * valid entries: 0 lines, 1 bars, 2 area, 3 lines + shapes, 4 dots, 5 difference, 
     * 6 steps, 7 step area
     * access: READ<br> 
     * update: INIT<br> 
     * unit: n/a
     */
    @JAMSVarDescription(
    access = JAMSVarDescription.AccessType.READ,
            description = "Graph type for left y axis graphs",
            defaultValue = "1"
            )
            public Attribute.Integer typeLeft;
    
    /**
     * selector for the plot type drawn on the right axis<br>
     * valid entries: 0 lines, 1 bars, 2 area, 3 lines + shapes, 4 dots, 5 difference, 
     * 6 steps, 7 step area
     * access: READ<br> 
     * update: INIT<br> 
     * unit: n/a
     */
    @JAMSVarDescription(
    access = JAMSVarDescription.AccessType.READ,
            description = "Graph type for right y axis graphs",
            defaultValue = "1"
            )
            public Attribute.Integer typeRight;
    
    /**
     * a title string for the plot's X-axis<br>
     * access: READ<br> 
     * update: INIT<br> 
     * unit: n/a
     */
    @JAMSVarDescription(
    access = JAMSVarDescription.AccessType.READ,
            description = "Title string for x axis",
            defaultValue = ""
            )
            public Attribute.String xAxisTitle;
    
    /**
     * a title string for the plot's left Y-axis<br>
     * access: READ<br> 
     * update: INIT<br> 
     * unit: n/a
     */
    @JAMSVarDescription(
    access = JAMSVarDescription.AccessType.READ,
            description = "Title string for left y axis",
            defaultValue = ""
            )
            public Attribute.String leftAxisTitle;
    
    /**
     * a title string for the plot's right Y-axis<br>
     * access: READ<br> 
     * update: INIT<br> 
     * unit: n/a
     */
    @JAMSVarDescription(
    access = JAMSVarDescription.AccessType.READ,
            description = "Title string for right y axis",
            defaultValue = ""
            )
            public Attribute.String rightAxisTitle;
    
    /**
     * flag for inverting (low values up) the right Y-axis<br>
     * access: READ<br> 
     * update: INIT<br> 
     * unit: n/a
     */
    @JAMSVarDescription(
    access = JAMSVarDescription.AccessType.READ,
            description = "Paint inverted right y axis?",
            defaultValue = "true"
            )
            public Attribute.Boolean rightAxisInverted;
    
    /**
     * a format string for the display of time on the X-axis<br>
     * access: READ<br> 
     * update: INIT<br> 
     * unit: n/a
     */
    @JAMSVarDescription(
    access = JAMSVarDescription.AccessType.READ,
            description = "Date format",
            defaultValue = "DD.MM.YYYY hh:mm:ss"
            )
            public Attribute.String dateFormat; //"dd-MM-yyyy"
    
    /**
     * the model's current time step<br>
     * access: READ<br> 
     * update: RUN<br> 
     * unit: n/a
     */
    @JAMSVarDescription(
    access = JAMSVarDescription.AccessType.READ,
            description = "Current time"
            )
            public Attribute.Calendar time;
    
    /**
     * a comma separated list of variables displayed on the left Y-Axis<br>
     * access: READ<br> 
     * update: RUN<br> 
     * unit: n/a
     */
    @JAMSVarDescription(
    access = JAMSVarDescription.AccessType.READ,
            description = "Values to be plotted on left x-axis"
            )
            public Attribute.Double[] valueLeft;
    
    /**
     * a comma separated list of variables displayed on the right Y-Axis<br>
     * access: READ<br> 
     * update: RUN<br> 
     * unit: n/a
     */
    @JAMSVarDescription(
    access = JAMSVarDescription.AccessType.READ,
            description = "Values to be plotted on right x-axis"
            )
            public Attribute.Double[] valueRight;
    
    /**
     * the no-data-value of the variables<br>
     * access: READ<br> 
     * update: INIT<br> 
     * unit: n/a
     */
    @JAMSVarDescription(
    access = JAMSVarDescription.AccessType.READ,
            description = "Value for \"No data\" (shouldn't be plotted)",
            defaultValue = "-9999"
            )
            public Attribute.Double noDataValue;
    
    /**
     * a number indicating how often the plot is updated during runtime<br>
     * access: READ<br> 
     * update: RUN<br> 
     * unit: n/a
     */
    @JAMSVarDescription(
    access = JAMSVarDescription.AccessType.READ,
            description = "Plot data after cacheSize values have been collected",
            defaultValue = "10"
            )
            public Attribute.Integer cacheSize;
    
    
    TimeSeries[] tsLeft, tsRight;
    TimeSeriesCollection dataset1, dataset2;
    XYItemRenderer rightRenderer, leftRenderer;
    XYPlot plot;
    JFreeChart chart;
    JButton saveButton;
    int i, graphCountLeft = 0, graphCountRight = 0;
    HashMap<String, Color> colorTable = new HashMap<String, Color>();
    double noDataValue_;
    int cacheSize_;
    long[] timeStamps;
    double[] dataValuesLeft;
    double[] dataValuesRight;
    int count;
    JPanel contentPanel = null;
    int seriesCount = 0;
    JPopupMenu pm = null;
    ChartPanel chartPanel = null;
    ChangeListener changeListener;
    ActionListener actionListener;
    ItemListener itemListener;
    
    
    public TSPlot() {
        colorTable.put("yellow", Color.yellow);
        colorTable.put("orange", Color.orange);
        colorTable.put("red", Color.red);
        colorTable.put("pink", Color.pink);
        colorTable.put("magenta", Color.magenta);
        colorTable.put("cyan", Color.cyan);
        colorTable.put("blue", Color.blue);
        colorTable.put("green", Color.green);
        colorTable.put("gray", Color.gray);
        colorTable.put("lightgray", Color.lightGray);
        colorTable.put("black", Color.black);
        
        actionListener = new ActionListener() {
            public void actionPerformed(ActionEvent actionEvent) {
                AbstractButton abstractButton = (AbstractButton)actionEvent.getSource();
                boolean selected = abstractButton.getModel().isSelected();
                
                //retrieve the selected series ID
                int idx = -1;
                for(int i = 0; i < plot.getDataset().getSeriesCount(); i++){
                    if(plot.getDataset().getSeriesKey(i).toString().equals(abstractButton.getText())){
                        idx = i;
                    }
                }
                if(selected){
                    XYLineAndShapeRenderer renderer = (XYLineAndShapeRenderer)plot.getRenderer();
                    renderer.setSeriesVisible(idx, true);
                    chart.fireChartChanged();
                } else{
                    XYLineAndShapeRenderer renderer = (XYLineAndShapeRenderer)plot.getRenderer();
                    renderer.setSeriesVisible(idx, false);
                    chart.fireChartChanged();
                }
            }
        };
        changeListener = new ChangeListener() {
            public void stateChanged(ChangeEvent changeEvent) {
                
                
            }
        };
        itemListener = new ItemListener() {
            public void itemStateChanged(ItemEvent itemEvent) {
                
                
            }
        };
    }
    
    
    
    /**
     * return the plot's panel
     * @return the plot panel
     */
    public JPanel getPanel() {
        
        dataset1 = new TimeSeriesCollection();
        dataset2 = new TimeSeriesCollection();
        
        chart = ChartFactory.createTimeSeriesChart(
                getInstanceName(),
                xAxisTitle.getValue(),
                leftAxisTitle.getValue(),
                dataset1,
                true,
                true,
                false);
        
        chartPanel = new ChartPanel(chart, true);
        
        
        //popup menu
        pm = new JPopupMenu();
        JMenu csMenu = new JMenu("Change series ...");
        pm.add(csMenu);
        
        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout());
        panel.add(chartPanel, BorderLayout.CENTER);
        
        contentPanel = new JPanel();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        contentPanel.setComponentPopupMenu(pm);
        contentPanel.setBorder(BorderFactory.createLineBorder(Color.black));
        contentPanel.add(new JLabel("Zeitreihen"));
        //contentPanel.addMouseListener(this);
        //panel.addMouseListener(this);
        panel.add(contentPanel, BorderLayout.EAST);
        
        return panel;
    }
    
    private XYItemRenderer getRenderer(int type) {
        XYItemRenderer r;
        switch (type) {
            case 0:
                XYLineAndShapeRenderer lsr = new XYLineAndShapeRenderer();
                lsr.setBaseShapesVisible(false);
                r = lsr;
                break;
                
            case 1:
                r = new XYBarRenderer();
                break;
                
            case 2:
                r = new XYAreaRenderer();
                break;
                
            case 3:
                lsr = new XYLineAndShapeRenderer();
                lsr.setBaseShapesVisible(true);
                r = lsr;
                break;
                
            case 4:
                XYDotRenderer dotR = new XYDotRenderer();
                dotR.setDefaultEntityRadius(2);
                r = dotR;
                break;
                
            case 5:
                r = new XYDifferenceRenderer();
                break;
                
            case 6:
                r = new XYStepRenderer();
                break;
                
            case 7:
                r = new XYStepAreaRenderer();
                break;
                
            default:
                lsr = new XYLineAndShapeRenderer();
                lsr.setBaseShapesVisible(false);
                r = lsr;
        }
        return r;
    }
    
    /**
     * the model's init() method
     */
    public void init() {
        
        
        noDataValue_ = noDataValue.getValue();
        
        if (chart!=null) {
            plot = chart.getXYPlot();
            
            DateAxis dateAxis = (DateAxis) plot.getDomainAxis();
            dateAxis.setDateFormatOverride(new SimpleDateFormat(dateFormat.getValue()));
            
            leftRenderer = getRenderer(typeLeft.getValue());
            plot.setRenderer(0, leftRenderer);
            
            graphCountLeft = valueLeft.length;
            tsLeft = new TimeSeries[graphCountLeft];
            for (i = 0; i < graphCountLeft; i++) {
                String legendEntry = titleLeft.getValue()[i];
                
                if(this.varTitleLeft != null){
                    legendEntry = legendEntry + getModel().getRuntime().getDataHandles().get(varTitleLeft.getValue()[i]);
                }
                leftRenderer.setSeriesPaint(i, colorTable.get(colorLeft.getValue()[i]));
                tsLeft[i] = new TimeSeries(legendEntry, Second.class);
                dataset1.addSeries(tsLeft[i]);
                
                //series selection panel
                JCheckBox cb = new JCheckBox(legendEntry);
                cb.addActionListener(actionListener);
                cb.addChangeListener(changeListener);
                cb.addItemListener(itemListener);
                cb.setSelected(true);
                contentPanel.add(cb);
                contentPanel.repaint();
            }
            
            if (valueRight != null) {
                ValueAxis axis2 = new NumberAxis(rightAxisTitle.getValue());
                axis2.setInverted(rightAxisInverted.getValue());
                plot.setRangeAxis(1, axis2);
                plot.setDataset(1, dataset2);
                plot.mapDatasetToRangeAxis(1, 1);
                
                rightRenderer = getRenderer(typeRight.getValue());
                plot.setRenderer(1, rightRenderer);
                
                plot.setDatasetRenderingOrder(DatasetRenderingOrder.REVERSE);
                
                graphCountRight = valueRight.length;
                tsRight = new TimeSeries[graphCountRight];
                for (i = 0; i < graphCountRight; i++) {
                    rightRenderer.setSeriesPaint(i, colorTable.get(colorRight.getValue()[i]));
                    tsRight[i] = new TimeSeries(titleRight.getValue()[i], Second.class);
                    dataset2.addSeries(tsRight[i]);
                }
            }
        }
        
        cacheSize_ = cacheSize.getValue();
        timeStamps = new long[cacheSize_];
        dataValuesRight = new double[cacheSize_*graphCountRight];
        dataValuesLeft = new double[cacheSize_*graphCountLeft];
        count = 0;
    }
    
    /**
     * the model's run() method
     */
    public void run() {
        if(this.plotTitle != null)
            chartPanel.getChart().setTitle(this.plotTitle.getValue());
        timeStamps[count] = time.getTimeInMillis();
        int offsetRight = count * graphCountRight;
        int offsetLeft = count * graphCountLeft;
        
        for (i = 0; i < graphCountRight; i++) {
            double value = valueRight[i].getValue();
            if (value == noDataValue_) {
                value = 0;
            }
            dataValuesRight[offsetRight+i] = value;
        }
        
        for (i = 0; i < graphCountLeft; i++) {
            double value = valueLeft[i].getValue();
            if (value == noDataValue_) {
                value = 0;
            }
            dataValuesLeft[offsetLeft+i] = value;
        }
        
        if (count == cacheSize_-1) {
            plotData();
            count = 0;
        } else {
            count++;
        }
    }
    
    public void plotData() {
        try {
            
            for (int i = 0; i <= count; i++) {
                
                Second second = new Second(new Date(timeStamps[i]));
                for (int j = 0; j < graphCountRight; j++) {
                    tsRight[j].add(second, dataValuesRight[i*graphCountRight+j]);
                }
                for (int j = 0; j < graphCountLeft; j++) {
                    tsLeft[j].add(second, dataValuesLeft[i*graphCountLeft+j]);
                }
            }
            
        } catch (Exception e) {} //caused by bugs in JFreeChart
    }
    
    /*public void run_() {
        try {
            for (i = 0; i < graphCountRight; i++) {
                double value = valueRight[i].getValue();
                if (value == noDataValue_) {
                    value = 0;
                }
                tsRight[i].add(new Second(new Date(time.getTimeInMillis())), value);
            }
            for (i = 0; i < graphCountLeft; i++) {
                double value = valueLeft[i].getValue();
                if (value == noDataValue_) {
                    value = 0;
                }
                tsLeft[i].add(new Second(new Date(time.getTimeInMillis())), value);
            }
        } catch (Exception e) {} //caused by bugs in JFreeChart
    }*/
    
    /**
     * the model's cleanup() method
     */
    public void cleanup() {
        plotData();
        
        seriesCount++;
    }
    
    public void mouseExited(MouseEvent m) {
        
    }
    
    public void mouseEntered(MouseEvent m) {
        
    }
    
    public void mousePressed(MouseEvent e){
        if(e.getButton() == MouseEvent.BUTTON3){
            this.pm.show(contentPanel, e.getX(), e.getY());
        }
    }
    
    public void mouseReleased(MouseEvent m) {
        
    }
    
    public void mouseClicked(MouseEvent m) {
        System.out.println("Mouse clicked ...");
    }
    
}