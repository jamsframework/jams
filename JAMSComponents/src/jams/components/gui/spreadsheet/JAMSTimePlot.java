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
import java.util.Vector;
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
import org.unijena.jams.data.JAMSBoolean;
import org.unijena.jams.data.JAMSCalendar;
import org.unijena.jams.data.JAMSDouble;
import org.unijena.jams.data.JAMSInteger;
import org.unijena.jams.data.JAMSString;
import org.unijena.jams.data.JAMSStringArray;
import org.unijena.jams.model.JAMSGUIComponent;
import org.unijena.jams.model.JAMSVarDescription;

/**
 *
 * @author Robert Riedel
 */
public class JAMSTimePlot {
    

    Vector<GraphProperties> propVector;
    ValueAxis axisLEFT;
    ValueAxis axisRIGHT;
    

    String xAxisTitle;
    String leftAxisTitle;
    String rightAxisTitle;
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
    
    public JAMSTimePlot() {
        
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
    
    public JAMSTimePlot(Vector<GraphProperties> propVector) {
        
        this.propVector = propVector;
        
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
//            String plotTitle = "Title";
//            String[] titleLeft = {"Title left"};
//            String[] varTitleLeft = {"Var Title left"};
//            String[] titleRight = {"Title right"};
//            String[] colorLeft = {"red","pink","magenta","orange","yellow"};
//            String[] colorRight = {"cyan","blue","green","gray","black"};
//            int typeLeft = 0;
//            int typeRight = 1;
            String xAxisTitle = "x axis title";
            String leftAxisTitle = "left axis title";
            String rightAxisTitle = "right axis title";
//            boolean rightAxisInverted = false;
//            String dateFormat = "dd/MM/yyyy"; //"dd-MM-yyyy"
            //public JAMSCalendar time;
            //double[] valueLeft;
            //double[] valueRight;
            String title = "CTSPlot ver. 0.10";
    }
    
//    public void setPlotTitle(String plotTitle){
//        this.plotTitle = plotTitle;
//    }
//    
//    public void setTitleLeft(String[] titleLeft){
//        this.titleLeft = titleLeft;
//    }
//
//    
//    public void setVarTitleLeft(String[] varTitleLeft){
//        this.varTitleLeft = varTitleLeft;
//    }
//        
//    public void setTitleRight(String[] titleRight){
//        this.titleRight = titleRight;
//    }    
//    
//     
//    public void setColorLeft(String[] colorLeft){
//        this.colorLeft = colorLeft;
//    }
//    
//    public void setColorRight(String[] colorRight){
//        this.colorRight = colorRight;
//    }
//    
//    public void setTypeLeft(int typeLeft){
//        this.typeLeft = typeLeft;
//    }
//    
//    public void setTypeRight(int typeRight){
//        this.typeRight = typeRight;
//    }
//    
//    public void setXAxisTitle(String xAxisTitle){
//        this.xAxisTitle = xAxisTitle;
//    }
//    
//    public void setLeftAxisTitle(String leftAxisTitle){
//        this.leftAxisTitle = leftAxisTitle;
//    }
//    
//    public void setRightAxisTitle(String rightAxisTitle){
//        this.rightAxisTitle = rightAxisTitle;
//    }
//    
//    public void setRightAxisInverted(boolean rightAxisInverted){
//        this.rightAxisInverted = rightAxisInverted;
//    }
//    
//    public void setLeftAxisInverted(boolean rightAxisInverted){
//        this.leftAxisInverted = leftAxisInverted;
//    }
//  
//    public void setDateFormat(String dateFormat){
//        this.dateFormat = dateFormat;
//    }
//    
//    public void setTitle(String title){
//        this.title = title;
//    }
//    
//    public void setGraphCountLeft(int graphCountLeft){
//        this.graphCountLeft = graphCountLeft;
//    }
//    
//    public void setGraphCountRight(int graphCountRight){
//        this.graphCountRight = graphCountRight;
//    }
      
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

        chart = ChartFactory.createTimeSeriesChart(
                "title",
                "xAxisTitle",
                "leftAxisTitle",
                propVector.get(0).getDataset(),
                false,
                false,
                false);
        
        chartPanel = new ChartPanel(chart, true);
        chartPanel.setBackground(Color.WHITE);
        
        panel = new JPanel();
        panel.setLayout(new GridLayout(1,1));
        //panel.setBackground(Color.WHITE);
        panel.add(chartPanel);
        
        plot = chart.getXYPlot();
        
        DateAxis dateAxis = (DateAxis) plot.getDomainAxis();
        dateAxis.setDateFormatOverride(new SimpleDateFormat("dd-MM-yyyy"));

        axisLEFT = plot.getRangeAxis();
        axisRIGHT = new NumberAxis(rightAxisTitle);
    

    }
    
    public void setPropVector(Vector<GraphProperties> propVector){
        
        this.propVector = propVector;
    }
      
    public void plot(int i){
        
        GraphProperties prop = propVector.get(i);
        XYItemRenderer renderer = getRenderer(prop.typechoice.getSelectedIndex());

        /* RENDERER & COLOR */
        renderer.setPaint(colorTable.get(prop.getColor())); 

        /* POSITION */
        if(prop.getPosChoice().getSelectedItem() == "left"){
            plot.setRangeAxis(i, axisLEFT);
            axisLEFT.setInverted(prop.getInvBox().isSelected());
            
        }else{
            plot.setRangeAxis(i, axisRIGHT);
            axisRIGHT.setInverted(prop.getInvBox().isSelected());
        }

        plot.mapDatasetToRangeAxis(1, 1);
        
        /* DATASET */
        plot.setDataset(i, prop.getDataset());
        plot.setRenderer(i, renderer);

    }
    
    public void plotAll(){
        
        for(int i=0; i<propVector.size(); i++){
            plot(i);
        }
        
    }
    

    
//    public void plot(JAMSCalendar time, double[] valueLeft, double[] valueRight) {
//        try {
//            for (i = 0; i < graphCountRight; i++) {
//                double value = valueRight[i];
//                if(value == -9999)
//                    value = 0;
//                //tsRight[i].add(new Hour(new Date(time.getTimeInMillis())), valueRight[i].getValue());
//                tsRight[i].add(new Second(new Date(time.getTimeInMillis())), value);
//            }
//            for (i = 0; i < graphCountLeft; i++) {
//                double value = valueLeft[i];
//                if(value == -9999)
//                    value = 0;
//                tsLeft[i].add(new Second(new Date(time.getTimeInMillis())), value);
//            }
//        } catch (Exception e) {} //caused by bugs in JFreeChart
//    }
    
    public void cleanup() {
//        saveButton.setEnabled(true);
    }
    
}

