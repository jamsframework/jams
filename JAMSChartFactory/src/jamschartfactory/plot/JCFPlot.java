/*
 * JCFPlot.java
 *
 * Created on 20. April 2006, 14:31
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package jamschartfactory.plot;

import org.jfree.chart.*;
import org.jfree.chart.axis.*;
import org.jfree.chart.plot.*;
import org.jfree.chart.renderer.xy.*;
import org.jfree.data.time.*;
import org.jfree.data.xy.*;

/**
 *
 * @author c0krpe
 */
public class JCFPlot {
    public JFCChartPanel cp = null;
    /**
     * Creates a new instance of JCFPlot
     */
    
    public JCFPlot(){
        
    }
    public void addTSSubPlot(int[][] dateMatrix, double[][] dataMatrix, int[] seriesNo, String[] dataCols, ChartPanel chart, String type){
        NumberAxis yAxis = new NumberAxis("value");
        DateAxis xAxis = (DateAxis)((CombinedDomainXYPlot)chart.getChart().getPlot()).getDomainAxis();
        XYPlot xyPlot = this.createTSPlot(dateMatrix, dataMatrix, seriesNo, dataCols, type, xAxis, yAxis);
        ((CombinedDomainXYPlot)chart.getChart().getPlot()).add(xyPlot);
        
    }
    
    public void addTSPlot(int[][] dateMatrix, double[][] dataMatrix, int[] seriesNo, String[] dataCols, ChartPanel chart, String type){
        int len = seriesNo.length;
        int tsLen = dataMatrix[seriesNo[0]].length;
        int noSeries = ((XYPlot)((CombinedDomainXYPlot)chart.getChart().getPlot()).getSubplots().get(0)).getDatasetCount();
        
        for(int i = 0; i < len; i++){
            TimeSeriesCollection tsc_dataset = new TimeSeriesCollection();
            TimeSeries dataset = new TimeSeries(dataCols[seriesNo[i]+1], Hour.class);
            for(int t = 0; t < tsLen; t++){
                dataset.add(new Hour(dateMatrix[3][t], dateMatrix[2][t], dateMatrix[1][t], dateMatrix[0][t]), dataMatrix[seriesNo[i]][t]);
            }
            tsc_dataset.addSeries(dataset);
            if(type.equals("line")){
                XYLineAndShapeRenderer renderer = new XYLineAndShapeRenderer(true, false);
                renderer.setSeriesLinesVisible(0, true);
                renderer.setSeriesShapesVisible(0, false);
                int idx = noSeries + i;
                ((XYPlot)((CombinedDomainXYPlot)chart.getChart().getPlot()).getSubplots().get(0)).setRangeAxis(idx,new NumberAxis("axis " + idx));
                ((XYPlot)((CombinedDomainXYPlot)chart.getChart().getPlot()).getSubplots().get(0)).mapDatasetToRangeAxis(idx,idx);
                ((XYPlot)((CombinedDomainXYPlot)chart.getChart().getPlot()).getSubplots().get(0)).setDataset(idx, tsc_dataset);
                ((XYPlot)((CombinedDomainXYPlot)chart.getChart().getPlot()).getSubplots().get(0)).setRenderer(idx, renderer, true);
                
                System.out.println("ChartAreaX: "+chart.getChartRenderingInfo().getChartArea().getWidth());
                
                JFreeChart tempChart = chart.getChart();
                chart.getChart().fireChartChanged();
                //chart.setChart(tempChart);
                
                System.out.println("ChartAreaX: "+chart.getChartRenderingInfo().getChartArea().getWidth());
            } else if(type.equals("bar")){
                //double barWidth = (1.0 / tsc_dataset.getSeriesCount());
                XYBarRenderer renderer = new XYBarRenderer();
                int idx = noSeries + i;
                ((XYPlot)((CombinedDomainXYPlot)chart.getChart().getPlot()).getSubplots().get(0)).setDataset(idx, tsc_dataset);
                ((XYPlot)((CombinedDomainXYPlot)chart.getChart().getPlot()).getSubplots().get(0)).setRenderer(idx, renderer, true);
            } else if(type.equals("dot")){
                XYLineAndShapeRenderer renderer = new XYLineAndShapeRenderer(false, true);
                renderer.setLinesVisible(false);
                renderer.setShapesVisible(true);
                int idx = noSeries + i;
                ((XYPlot)((CombinedDomainXYPlot)chart.getChart().getPlot()).getSubplots().get(0)).setRangeAxis(idx,new NumberAxis("axis " + idx));
                ((XYPlot)((CombinedDomainXYPlot)chart.getChart().getPlot()).getSubplots().get(0)).mapDatasetToRangeAxis(idx,idx);
                ((XYPlot)((CombinedDomainXYPlot)chart.getChart().getPlot()).getSubplots().get(0)).setDataset(idx, tsc_dataset);
                ((XYPlot)((CombinedDomainXYPlot)chart.getChart().getPlot()).getSubplots().get(0)).setRenderer(idx, renderer, true);
            }
        }
        
        System.out.println("ChartAreaX: "+chart.getChartRenderingInfo().getChartArea().getX());
        
    }
    
    public void createNewTSPlot(int[][] dateMatrix, double[][] dataMatrix, int[] seriesNo, String[] dataCols, String type){
        DateAxis xAxis = new DateAxis("date/time");
        NumberAxis yAxis = new NumberAxis("value");
        XYPlot xyPlot = this.createTSPlot(dateMatrix, dataMatrix, seriesNo, dataCols, type, xAxis, yAxis);
        CombinedDomainXYPlot cplot = new CombinedDomainXYPlot(xAxis);
        cplot.add(xyPlot);
        JFreeChart chart = new JFreeChart(cplot);
        
        cp = new JFCChartPanel(chart);
        
    }
    
    public void createNewScatterPlot(double[][] dataMatrix, int[] seriesNo, int tSteps, String[] dataCols){
        XYPlot plot = createScatterPlot(dataMatrix, seriesNo, tSteps, dataCols);
        cp = new JFCChartPanel(new JFreeChart(plot));
        
        //the 1:1 Line
        double maxValue = -9999;
        double minValue = 9999999;
        for (int i = 0; i < tSteps; i++){
            if(dataMatrix[seriesNo[0]][i] > maxValue)
                maxValue = dataMatrix[seriesNo[0]][i];
            if(dataMatrix[seriesNo[1]][i] > maxValue)
                maxValue = dataMatrix[seriesNo[1]][i];
            if(dataMatrix[seriesNo[0]][i] < minValue)
                minValue = dataMatrix[seriesNo[0]][i];
            if(dataMatrix[seriesNo[1]][i] < minValue)
                minValue = dataMatrix[seriesNo[1]][i];
        }
        XYSeriesCollection dataset = new XYSeriesCollection();
        XYSeries xyData = new XYSeries("1:1 line");
        xyData.add(minValue,minValue);
        xyData.add(maxValue, maxValue);
        dataset.addSeries(xyData);
        
        ((XYPlot)cp.getChart().getPlot()).setDataset(1, dataset);
        System.out.println("no of datasets:" + ((XYPlot)cp.getChart().getPlot()).getDatasetCount());
        plot.setRenderer(1, new XYLineAndShapeRenderer(true, true), true);
    }
    
    public XYPlot createScatterPlot(double[][] dataMatrix, int[] seriesNo, int tSteps, String[] dataCols){
        NumberAxis xAxis = new NumberAxis(dataCols[seriesNo[0]+1]);
        NumberAxis yAxis = new NumberAxis(dataCols[seriesNo[1]+1]);
        
        XYSeriesCollection dataset = new XYSeriesCollection();
        XYSeries xyData = new XYSeries("xySeries");
        for (int i = 0; i < tSteps; i++){
            xyData.add(dataMatrix[seriesNo[0]][i], dataMatrix[seriesNo[1]][i]);
        }
        dataset.addSeries(xyData);
        XYLineAndShapeRenderer renderer = new XYLineAndShapeRenderer(false, true);
        renderer.setShapesFilled(false);
        XYPlot plot = new XYPlot(dataset, xAxis, yAxis, renderer);
        
        return plot;
    }
    
    public XYPlot createTSPlot(int[][] dateMatrix, double[][] dataMatrix, int[] seriesNo, String[] dataCols, String type, DateAxis xAxis, NumberAxis yAxis){
        int len = seriesNo.length;
        //  int tsLen = dataMatrix[seriesNo[0]].length; // Hour
        int tsLen = dataMatrix[seriesNo[0]].length; // Day
        
        XYPlot plot = new XYPlot();
        plot.setDomainAxis(xAxis);
        //plot.setRangeAxis(yAxis);
        
        TimeSeriesCollection tsc_bar_dataset = new TimeSeriesCollection();
        for(int i = 0; i < len; i++){
            TimeSeriesCollection tsc_dataset = new TimeSeriesCollection();
       /*     TimeSeries dataset = new TimeSeries(dataCols[seriesNo[i]+1], Hour.class);
           for(int t = 0; t < tsLen; t++){
                dataset.add(new Hour(dateMatrix[3][t], dateMatrix[2][t], dateMatrix[1][t], dateMatrix[0][t]), dataMatrix[seriesNo[i]][t]);
            } */
            TimeSeries dataset = new TimeSeries(dataCols[seriesNo[i]+1], Day.class);
            for(int t = 0; t < tsLen; t++){
                dataset.add(new Day(dateMatrix[2][t], dateMatrix[1][t], dateMatrix[0][t]), dataMatrix[seriesNo[i]][t]);
            }
            
            tsc_dataset.addSeries(dataset);
            tsc_bar_dataset.addSeries(dataset);
            if(type.equals("line")){
                XYLineAndShapeRenderer renderer = new XYLineAndShapeRenderer(true, false);
                renderer.setUseOutlinePaint(true);
                renderer.setUseFillPaint(true);
                renderer.setSeriesLinesVisible(0, true);
                renderer.setSeriesShapesVisible(0, false);
                
                plot.setDataset(i, tsc_dataset);
                plot.setRangeAxis(i,new NumberAxis("axis " + i));
                plot.mapDatasetToRangeAxis(i,i);
                plot.setRenderer(i,renderer,true);
            } else if(type.equals("bar")){
                //double barWidth = (1.0 / tsc_dataset.getSeriesCount());
                XYBarRenderer renderer = new XYBarRenderer();
                // plot.setDataset(i, tsc_dataset);
                plot.setRangeAxis(i,new NumberAxis("axis " + i));
                plot.mapDatasetToRangeAxis(i,i);
                //plot.setRenderer(i,renderer,true);
            } else if(type.equals("dot")){
                XYLineAndShapeRenderer renderer = new XYLineAndShapeRenderer(false, true);
                renderer.setSeriesLinesVisible(0, false);
                renderer.setSeriesShapesVisible(0, true);
                plot.setDataset(i, tsc_dataset);
                plot.setRangeAxis(i,new NumberAxis("axis " + i));
                plot.mapDatasetToRangeAxis(i,i);
                plot.setRenderer(i,renderer,true);
            }
            
        }
        
        if(type.equals("bar")){
            plot.setDataset(tsc_bar_dataset);
            plot.setRenderer(new ClusteredXYBarRenderer());
        }
        
        return plot;
        
    }
    
}
