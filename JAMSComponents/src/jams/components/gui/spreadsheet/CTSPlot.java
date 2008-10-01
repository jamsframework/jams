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

package jams.components.gui.spreadsheet;

import java.awt.FlowLayout;
import java.awt.GridLayout;
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
import org.jfree.chart.axis.SegmentedTimeline;
import org.jfree.chart.axis.Timeline;
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
 * @author Robert Riedel
 */
public class CTSPlot {
    

            String plotTitle;
            String[] titleLeft;
            String[] varTitleLeft;
            String[] titleRight;
            String[] colorLeft;
            String[] colorRight;
            int typeLeft;
            int typeRight;
            String xAxisTitle;
            String leftAxisTitle;
            String rightAxisTitle;
            boolean rightAxisInverted;
            boolean leftAxisInverted;
            String dateFormat; //"dd-MM-yyyy"
            public JAMSCalendar time;
            double[] valueLeft;
            double[] valueRight;
            String title;
            
            ChartPanel chartPanel;
    
    
    TimeSeries[] tsLeft, tsRight;
    TimeSeriesCollection dataset1, dataset2;
    XYItemRenderer rightRenderer, leftRenderer;
    XYPlot plot;
    JFreeChart chart;
    JPanel panel;
    JButton saveButton;
    int i, graphCountLeft = 0, graphCountRight = 0;
    HashMap<String, Color> colorTable = new HashMap<String, Color>();
    
    public CTSPlot() {
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
        
        setDefaultValues();
        
    }
    
    public void setDefaultValues(){
            String plotTitle = "Title";
            String[] titleLeft = {"Title left"};
            String[] varTitleLeft = {"Var Title left"};
            String[] titleRight = {"Title right"};
            String[] colorLeft = {"red","pink","magenta","orange","yellow"};
            String[] colorRight = {"cyan","blue","green","gray","black"};
            int typeLeft = 0;
            int typeRight = 1;
            String xAxisTitle = "x axis title";
            String leftAxisTitle = "left axis title";
            String rightAxisTitle = "right axis title";
            boolean rightAxisInverted = false;
            String dateFormat = "dd/MM/yyyy"; //"dd-MM-yyyy"
            //public JAMSCalendar time;
            //double[] valueLeft;
            //double[] valueRight;
            String title = "CTSPlot ver. 0.10";
    }
    
    public void setPlotTitle(String plotTitle){
        this.plotTitle = plotTitle;
    }
    
    public void setTitleLeft(String[] titleLeft){
        this.titleLeft = titleLeft;
    }

    
    public void setVarTitleLeft(String[] varTitleLeft){
        this.varTitleLeft = varTitleLeft;
    }
        
    public void setTitleRight(String[] titleRight){
        this.titleRight = titleRight;
    }    
    
     
    public void setColorLeft(String[] colorLeft){
        this.colorLeft = colorLeft;
    }
    
    public void setColorRight(String[] colorRight){
        this.colorRight = colorRight;
    }
    
    public void setTypeLeft(int typeLeft){
        this.typeLeft = typeLeft;
    }
    
    public void setTypeRight(int typeRight){
        this.typeRight = typeRight;
    }
    
    public void setXAxisTitle(String xAxisTitle){
        this.xAxisTitle = xAxisTitle;
    }
    
    public void setLeftAxisTitle(String leftAxisTitle){
        this.leftAxisTitle = leftAxisTitle;
    }
    
    public void setRightAxisTitle(String rightAxisTitle){
        this.rightAxisTitle = rightAxisTitle;
    }
    
    public void setRightAxisInverted(boolean rightAxisInverted){
        this.rightAxisInverted = rightAxisInverted;
    }
    
    public void setLeftAxisInverted(boolean rightAxisInverted){
        this.leftAxisInverted = leftAxisInverted;
    }
  
    public void setDateFormat(String dateFormat){
        this.dateFormat = dateFormat;
    }
    
    public void setTitle(String title){
        this.title = title;
    }
    
    public void setGraphCountLeft(int graphCountLeft){
        this.graphCountLeft = graphCountLeft;
    }
    
    public void setGraphCountRight(int graphCountRight){
        this.graphCountRight = graphCountRight;
    }
    
    
    
    
    public ChartPanel getChartPanel(){
        //createPlot();
        
        return chartPanel;
    }
    
    public JPanel getPanel() {
        
        return panel;
    }
    
    public JFreeChart getChart(){
        return chart;
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
    
    
    public void createPlot() {
        
        dataset1 = new TimeSeriesCollection();
        dataset2 = new TimeSeriesCollection();
        
        chart = ChartFactory.createTimeSeriesChart(
                title,
                xAxisTitle,
                leftAxisTitle,
                dataset1,
                true,
                false,
                false);
        
        chartPanel = new ChartPanel(chart, true);
        chartPanel.setBackground(Color.WHITE);
        
        panel = new JPanel();
        panel.setLayout(new GridLayout(1,1));
        //panel.setBackground(Color.WHITE);
        panel.add(chartPanel);
        
        
        
        if (chart!=null) {
            plot = chart.getXYPlot();
            
            DateAxis dateAxis = (DateAxis) plot.getDomainAxis();
            dateAxis.setDateFormatOverride(new SimpleDateFormat("dd-MM-yyyy"));
            
            leftRenderer = getRenderer(typeLeft);
             plot.setRenderer(0, leftRenderer);
            
            tsLeft = new TimeSeries[graphCountLeft];
            
            for (i = 0; i < graphCountLeft; i++) {
                String legendEntry = titleLeft[i];
             /*   
                if(this.varTitleLeft != null){
                    legendEntry = legendEntry + varTitleLeft[i];
                }
             **/
                leftRenderer.setSeriesPaint(i,colorTable.get(colorLeft[i])); // colorTable.get(colorLeft[i])
                tsLeft[i] = new TimeSeries(legendEntry, Second.class);
                dataset1.addSeries(tsLeft[i]);
            }
            
            if (graphCountRight != 0) {
                ValueAxis axis2 = new NumberAxis(rightAxisTitle);
                axis2.setInverted(true);
                //axis2.setInverted(rightAxisInverted);
                plot.setRangeAxis(1, axis2);
                plot.setDataset(1, dataset2);
                plot.mapDatasetToRangeAxis(1, 1);
                
                rightRenderer = getRenderer(typeRight);
                plot.setRenderer(1, rightRenderer);
                
                plot.setDatasetRenderingOrder(DatasetRenderingOrder.REVERSE);
                
                tsRight = new TimeSeries[graphCountRight];
                for (i = 0; i < graphCountRight; i++) {
                    rightRenderer.setSeriesPaint(i, colorTable.get(colorRight[i]));
                    tsRight[i] = new TimeSeries(titleRight[i], Second.class);
                    dataset2.addSeries(tsRight[i]);
                }
            }
        }
    }

    
    public void plot(JAMSCalendar time, double[] valueLeft, double[] valueRight) {
        try {
            for (i = 0; i < graphCountRight; i++) {
                double value = valueRight[i];
                if(value == -9999)
                    value = 0;
                //tsRight[i].add(new Hour(new Date(time.getTimeInMillis())), valueRight[i].getValue());
                tsRight[i].add(new Second(new Date(time.getTimeInMillis())), value);
            }
            for (i = 0; i < graphCountLeft; i++) {
                double value = valueLeft[i];
                if(value == -9999)
                    value = 0;
                tsLeft[i].add(new Second(new Date(time.getTimeInMillis())), value);
            }
        } catch (Exception e) {} //caused by bugs in JFreeChart
    }
    
    public void cleanup() {
//        saveButton.setEnabled(true);
    }
    
}
