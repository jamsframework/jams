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

package jams.components.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import javax.swing.JButton;
import javax.swing.JPanel;
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
import jams.data.JAMSBoolean;
import jams.data.JAMSCalendar;
import jams.data.JAMSDouble;
import jams.data.JAMSInteger;
import jams.data.JAMSString;
import jams.data.JAMSStringArray;
import jams.model.JAMSGUIComponent;
import jams.model.JAMSVarDescription;

/**
 *
 * @author S. Kralisch
 */
public class TSPlot extends JAMSGUIComponent {
    
    @JAMSVarDescription(
    access = JAMSVarDescription.AccessType.READ,
            update = JAMSVarDescription.UpdateType.INIT,
            description = "Title string for plot"
            )
            public JAMSString plotTitle;
    
    @JAMSVarDescription(
    access = JAMSVarDescription.AccessType.READ,
            update = JAMSVarDescription.UpdateType.INIT,
            description = "Title strings for left graphs"
            )
            public JAMSStringArray titleLeft;
    
    @JAMSVarDescription(
    access = JAMSVarDescription.AccessType.READ,
            update = JAMSVarDescription.UpdateType.INIT,
            description = "Variable title strings for left graphs"
            )
            public JAMSStringArray varTitleLeft;
    
    @JAMSVarDescription(
    access = JAMSVarDescription.AccessType.READ,
            update = JAMSVarDescription.UpdateType.INIT,
            description = "Title strings for right graphs"
            )
            public JAMSStringArray titleRight;
    
    @JAMSVarDescription(
    access = JAMSVarDescription.AccessType.READ,
            update = JAMSVarDescription.UpdateType.INIT,
            description = "Colors for left graphs (yellow, orange, red, pink, magenta, cyan, yellow, green, lightgray, gray, black)"
            )
            public JAMSStringArray colorLeft;
    
    @JAMSVarDescription(
    access = JAMSVarDescription.AccessType.READ,
            update = JAMSVarDescription.UpdateType.INIT,
            description = "Colors for right graphs (yellow, orange, red, pink, magenta, cyan, yellow, green, lightgray, gray, black)"
            )
            public JAMSStringArray colorRight;
    
    @JAMSVarDescription(
    access = JAMSVarDescription.AccessType.READ,
            update = JAMSVarDescription.UpdateType.INIT,
            description = "Graph type for left y axis graphs"
            )
            public JAMSInteger typeLeft;
    
    @JAMSVarDescription(
    access = JAMSVarDescription.AccessType.READ,
            update = JAMSVarDescription.UpdateType.INIT,
            description = "Graph type for right y axis graphs"
            )
            public JAMSInteger typeRight;
    
    @JAMSVarDescription(
    access = JAMSVarDescription.AccessType.READ,
            update = JAMSVarDescription.UpdateType.INIT,
            description = "Title string for x axis"
            )
            public JAMSString xAxisTitle;
    
    @JAMSVarDescription(
    access = JAMSVarDescription.AccessType.READ,
            update = JAMSVarDescription.UpdateType.INIT,
            description = "Title string for left y axis"
            )
            public JAMSString leftAxisTitle;
    
    @JAMSVarDescription(
    access = JAMSVarDescription.AccessType.READ,
            update = JAMSVarDescription.UpdateType.INIT,
            description = "Title string for right y axis"
            )
            public JAMSString rightAxisTitle;
    
    @JAMSVarDescription(
    access = JAMSVarDescription.AccessType.READ,
            update = JAMSVarDescription.UpdateType.INIT,
            description = "Paint inverted right y axis?"
            )
            public JAMSBoolean rightAxisInverted;
    
    @JAMSVarDescription(
    access = JAMSVarDescription.AccessType.READ,
            update = JAMSVarDescription.UpdateType.INIT,
            description = "Date format"
            )
            public JAMSString dateFormat; //"dd-MM-yyyy"
    
    @JAMSVarDescription(
    access = JAMSVarDescription.AccessType.READ,
            update = JAMSVarDescription.UpdateType.RUN,
            description = "Current time"
            )
            public JAMSCalendar time;
    
    @JAMSVarDescription(
    access = JAMSVarDescription.AccessType.READ,
            update = JAMSVarDescription.UpdateType.RUN,
            description = "Values to be plotted on left x-axis"
            )
            public JAMSDouble[] valueLeft;
    
    @JAMSVarDescription(
    access = JAMSVarDescription.AccessType.READ,
            update = JAMSVarDescription.UpdateType.RUN,
            description = "Values to be plotted on right x-axis"
            )
            public JAMSDouble[] valueRight;
    
    @JAMSVarDescription(
    access = JAMSVarDescription.AccessType.READ,
            description = "Value for \"No data\" (shouldn't be plotted)",
            defaultValue = "-9999"
            )
            public JAMSDouble noDataValue;
    
    @JAMSVarDescription(
    access = JAMSVarDescription.AccessType.READ,
            description = "Plot data after cacheSize values have been collected",
            defaultValue = "10"
            )
            public JAMSInteger cacheSize;
    
    
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
    }
    
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
        
        ChartPanel chartPanel = new ChartPanel(chart, true);
        
        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout());
        panel.add(chartPanel, BorderLayout.CENTER);
        
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
    
    public void run() {
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
    
    private void plotData() {
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
    
    public void run_() {
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
    }
    
    public void cleanup() {
        plotData();
    }
    
}