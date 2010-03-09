/*
 * JAMSTimePlot.java
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

package reg.spreadsheet;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Vector;
import javax.swing.JButton;
import javax.swing.JPanel;
import org.apache.xmlgraphics.java2d.ps.EPSDocumentGraphics2D;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.axis.AxisLocation;
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
import org.jfree.data.xy.*;

/**
 *
 * @author Robert Riedel
 */
public class JAMSXYPlot {

    Vector<GraphProperties> propVector;
    ValueAxis xAxis;
    ValueAxis axisLEFT;
    ValueAxis axisRIGHT;
    int graphCount=0;
    int graphCountRight=0;
    int graphCountLeft=0;

    String xAxisTitle;
    String leftAxisTitle;
    String rightAxisTitle;
    String title;

    ChartPanel chartPanel;
    
    XYSeriesCollection dataLeft = new XYSeriesCollection();
    XYSeriesCollection dataRight = new XYSeriesCollection();
    XYItemRenderer rightRenderer, leftRenderer;
    XYPlot plot;
    JFreeChart chart;
    JPanel panel;
    JButton saveButton;

    HashMap<String, Color> colorTable = new HashMap<String, Color>();
    
    public JAMSXYPlot() {
        
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
    
    public JAMSXYPlot(Vector<GraphProperties> propVector) {
        
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

            String xAxisTitle = "x axis title";
            String leftAxisTitle = "left axis title";
            String rightAxisTitle = "right axis title";

            String title = "XYPlot alpha";
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
    
    public BufferedImage getBufferedImage(int w, int h){
        BufferedImage bi = chart.createBufferedImage(w, h);
        return bi;
    }
    
    public void saveAsEPS( File outfile){
        
     try{ 
        
      OutputStream out = new java.io.FileOutputStream(outfile);
      EPSDocumentGraphics2D g2d = new EPSDocumentGraphics2D(false);
      g2d.setGraphicContext(new org.apache.xmlgraphics.java2d.GraphicContext());
      int width = 600;
      int height = 400;
      g2d.setupDocument(out, width, height); //400pt x 200pt
      this.chart.draw(g2d,new Rectangle(width,height));
      g2d.finish();
      out.flush();
      out.close();
      
      }catch(Exception fnfe){}
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
                XYBarRenderer br = new XYBarRenderer();
                br.setDrawBarOutline(true);
                
                //br.setBase(0.1);
                //br.setUseYInterval(true);
                br.setMargin(0.98);
                r = br;
                
                break;
                
            case 2:
                r = new XYAreaRenderer();
                break;
                
            case 3:
                lsr = new XYLineAndShapeRenderer();
                lsr.setBaseShapesVisible(true);
                lsr.setShapesFilled(true);
                lsr.setSeriesLinesVisible(0, false);
                //lsr.setDrawOutlines(false);
                //lsr.setLegendLine(new java.awt.Rectangle(5,5));
                r = lsr;
                break;
                
            case 4:
                XYDotRenderer dotR = new XYDotRenderer();
                dotR.setDefaultEntityRadius(2);
                dotR.setSeriesShape(0, new java.awt.Rectangle(5,5));
                //dotR.setSeriesShape(new java.awt.geom.RoundRectangle2D.Double());
                dotR.setDotHeight(5);
                dotR.setDotWidth(5);
                r = dotR;
                break;
      
            case 5:
                r = new XYStepRenderer();
                break;
                
            case 6:
                r = new XYStepAreaRenderer();
                break;
                
            case 7:
                r = new XYDifferenceRenderer();
                break;    
                
            default:
                lsr = new XYLineAndShapeRenderer();
                lsr.setBaseShapesVisible(false);
                r = lsr;
        }
        return r;
    }
    
    
    public void createPlot() {

        chart = ChartFactory.createXYLineChart(
                "title",
                "axis title default1",
                "axis Title default2",
                dataLeft,
                org.jfree.chart.plot.PlotOrientation.VERTICAL,
                true,
                false,
                false);
        
        chartPanel = new ChartPanel(chart, true);
        chartPanel.setBackground(Color.WHITE);
        
        panel = new JPanel();
            panel.setLayout(new BorderLayout());
        //panel.setBackground(Color.WHITE);
        panel.add(chartPanel, BorderLayout.CENTER);
        
        plot = chart.getXYPlot();
        
        xAxis = plot.getDomainAxis();
        plot.setRangeAxisLocation(AxisLocation.BOTTOM_OR_LEFT);
        
        axisLEFT = plot.getRangeAxis();
        axisRIGHT = new NumberAxis(rightAxisTitle);
        

    }
    
    public void setPropVector(Vector<GraphProperties> propVector){
        
        this.propVector = propVector;
    }
    
    public Vector<GraphProperties> getPropVector(){
        
        return this.propVector;
    }
        
    public void plotLeft(XYItemRenderer leftRenderer, String nameLeft, String xAxisTitle, boolean inverted){ //plotLeft(renderer, axisname, inverted)
        int plot_count = 0;
        int c = propVector.size();
        int corr = 0;
        dataLeft = new XYSeriesCollection();
        //ValueAxis xAxis = plot.getDomainAxis();
        
        axisLEFT.setInverted(inverted);
        axisLEFT.setLabel(nameLeft);
        xAxis.setLabel(xAxisTitle);
        
        //dataRight.removeAllSeries();
        
        //leftRenderer = getRenderer(renderer);
        
        for(int k=0; k<c; k++){ 
            
            if(!propVector.get(k).isXSeries()){
                if(propVector.get(k).getPosition().compareTo("left") == 0){
                    plot_count++;
                    GraphProperties prop = propVector.get(k);
                    dataLeft.addSeries(prop.getXYS());
                    //
                        dataRight.removeSeries(prop.getXYS());
                    //}
                    //leftRenderer.setSeriesPaint(k-corr,colorTable.get((String)prop.getColorChoice().getSelectedItem()));
                }else{
                    corr++;
                }
            }else{
                corr++;
            }
        }
        //if((plot_count<2 || plot_count>2) && renderer == 7) leftRenderer = getRenderer(0);
        
        if(corr == 0){
            dataRight.removeAllSeries();
            axisRIGHT.setVisible(false);
            axisLEFT.setVisible(true);
            plot.setRangeAxisLocation(0, AxisLocation.BOTTOM_OR_LEFT);
            plot.setRangeAxis(0, axisLEFT);
            plot.setDataset(0, dataLeft);
            plot.setRenderer(0, leftRenderer);
        }
        
        if(corr == 1){
            dataRight.removeAllSeries();
            axisRIGHT.setVisible(false);
            axisLEFT.setVisible(true);
            plot.setRangeAxisLocation(0, AxisLocation.BOTTOM_OR_LEFT);
            plot.setRangeAxis(0, axisLEFT);
            plot.setDataset(0, dataLeft);
            plot.setRenderer(0, leftRenderer);
        } else {
            axisRIGHT.setVisible(true);
            plot.setRangeAxisLocation(0, AxisLocation.BOTTOM_OR_LEFT);
            plot.setRangeAxis(0, axisLEFT);
            plot.setDataset(0, dataLeft);
            plot.setRenderer(0, leftRenderer);
            plot.mapDatasetToRangeAxis(0, 0);
        }
        
        plot.setDomainAxis(0, xAxis);
        //plot.mapDatasetToDomainAxis(0, 0); //dataset einer achse zuordnen!
        
    }
    
    public void plotEmpty(){
           
        int plot_count = 0;
        int c = propVector.size();
        int corr = 0;
        dataLeft = new XYSeriesCollection();
        //ValueAxis xAxis = plot.getDomainAxis();
        
        axisLEFT.setInverted(false);
        axisLEFT.setLabel("Left axis title");
        xAxis.setLabel(xAxisTitle);
        
        leftRenderer = getRenderer(0);
        
        dataLeft.removeAllSeries();
        dataRight.removeAllSeries();
              
        axisRIGHT.setVisible(false);
        axisLEFT.setVisible(true);
        plot.setRangeAxisLocation(0, AxisLocation.BOTTOM_OR_LEFT);
        plot.setRangeAxis(0, axisLEFT);
        plot.setDataset(0, dataLeft);
        plot.setRenderer(0, leftRenderer);
        
        plot.setDomainAxis(0, xAxis);
        plot.mapDatasetToRangeAxis(0, 0);
        plot.setDomainAxis(0, xAxis);
  
    }
    
    public void setTitle(String title){
        chart.setTitle(title);
    }
    
    public void plotRight(XYItemRenderer rightRenderer, String nameRight, String xAxisTitle, boolean inverted){
        int plot_count = 0;
        int c = propVector.size();
        int corr = 0;
        dataRight = new XYSeriesCollection();
        
        
        
        xAxis.setLabel(xAxisTitle);
        
        //rightRenderer = getRenderer(renderer);
        
        for(int k=0; k<c; k++){

            if(!propVector.get(k).isXSeries()){
                if(propVector.get(k).getPosition().compareTo("right") == 0){
                    plot_count++;
                    GraphProperties prop = propVector.get(k);
                    dataRight.addSeries(prop.getXYS());
                    //if(corr <=dataLeft.getSeriesCount()){
                        dataLeft.removeSeries(prop.getXYS());
                    //}
                    //rightRenderer.setSeriesPaint(k-corr,colorTable.get((String)prop.getColorChoice().getSelectedItem()));
                } else {
                    corr++;
                }
            } else {
                corr++;
            }
        }
        //if((plot_count<2 || plot_count>2) && renderer == 7) leftRenderer = getRenderer(0);
        
        if(corr == 0){
            dataLeft.removeAllSeries();
            axisLEFT.setVisible(false);
            axisRIGHT.setVisible(true);
            axisRIGHT.setInverted(inverted);
            axisRIGHT.setLabel(nameRight);
            plot.setRangeAxis(1, axisRIGHT);
            plot.setRangeAxisLocation(1, AxisLocation.BOTTOM_OR_RIGHT);
            plot.setDataset(1, dataRight);
            plot.setRenderer(1, rightRenderer);
        }
        if(corr == 1){
            dataLeft.removeAllSeries();
            axisLEFT.setVisible(false);
            axisRIGHT.setVisible(true);
            axisRIGHT.setInverted(inverted);
            axisRIGHT.setLabel(nameRight);
            plot.setRangeAxis(1, axisRIGHT);
            plot.setRangeAxisLocation(1, AxisLocation.BOTTOM_OR_RIGHT);
            plot.setDataset(1, dataRight);
            plot.setRenderer(1, rightRenderer);
        } else {
            axisLEFT.setVisible(true);
            axisRIGHT.setInverted(inverted);
            axisRIGHT.setLabel(nameRight);
            plot.setRangeAxis(1, axisRIGHT);
            plot.setRangeAxisLocation(1, AxisLocation.BOTTOM_OR_RIGHT);
            plot.setDataset(1, dataRight);
            plot.setRenderer(1, rightRenderer);
            plot.mapDatasetToRangeAxis(1, 1);
    
        }
        plot.setDatasetRenderingOrder(DatasetRenderingOrder.REVERSE);
        plot.setDomainAxis(0, xAxis);

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

