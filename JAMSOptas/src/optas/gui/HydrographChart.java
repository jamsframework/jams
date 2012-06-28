/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package optas.gui;

import java.awt.BasicStroke;
import java.awt.Color;
import java.util.ArrayList;
import optas.hydro.calculations.BaseFlow;
import optas.hydro.calculations.HydrographSection;
import optas.hydro.calculations.Peak;
import optas.hydro.calculations.RecessionCurve;
import optas.hydro.data.Measurement;
import optas.hydro.data.SimpleEnsemble;
import optas.hydro.data.TimeFilter;
import optas.hydro.data.TimeSerie;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.AxisLocation;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.StackedXYBarRenderer;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.Range;
import org.jfree.data.time.Day;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;
import org.jfree.data.xy.CategoryTableXYDataset;

/**
 *
 * @author chris
 */
public class HydrographChart {
    JFreeChart chart;

    TimeSeriesCollection datasetHydrograph = new TimeSeriesCollection();
    TimeSeriesCollection datasetFiltered = new TimeSeriesCollection();

    TimeSeriesCollection datasetPeaks, datasetRecessionCurves, datasetGroundwater, datasetBaseFlow, datasetMark;

    TimeSerie hydrograph;
    TimeSerie groundwater;
    
    int peakCount = 0;
    int recessionCount = 0;
    double groundwaterThreshold = 0;
    
    int[] markedTimeSteps = null;
    ArrayList<TimeFilter> filter = new ArrayList<TimeFilter>();

    public HydrographChart(){
        
        datasetPeaks = new TimeSeriesCollection();
        datasetRecessionCurves = new TimeSeriesCollection();
        datasetGroundwater = new TimeSeriesCollection();
        datasetBaseFlow = new TimeSeriesCollection();
        datasetMark = new TimeSeriesCollection();
        
         chart = ChartFactory.createTimeSeriesChart(
                "Hydrograph",
                "time",
                "runoff",
                datasetHydrograph,
                true,
                true,
                false);
        
        XYLineAndShapeRenderer hydrographRenderer = new XYLineAndShapeRenderer();
        hydrographRenderer.setBaseFillPaint(new Color(0, 0, 255));
        hydrographRenderer.setBaseLinesVisible(true);
        hydrographRenderer.setDrawSeriesLineAsPath(true);
        hydrographRenderer.setBaseOutlinePaint(new Color(0, 0, 255));
        hydrographRenderer.setBasePaint(new Color(0, 0, 255));
        hydrographRenderer.setOutlinePaint(new Color(0, 0, 255));
        hydrographRenderer.setPaint(new Color(0, 0, 255));
        hydrographRenderer.setBaseSeriesVisible(true);
        hydrographRenderer.setDrawOutlines(true);
        hydrographRenderer.setBaseShapesVisible(false);
        hydrographRenderer.setStroke(new BasicStroke(3.0f));
        
        chart.getXYPlot().setRenderer(0, hydrographRenderer); //?
        
        chart.getXYPlot().mapDatasetToRangeAxis(1, 1);
        NumberAxis axis2 = new NumberAxis("");
        axis2.setRange(new Range(0.0, 1.0));
        axis2.setVisible(false);
        chart.getXYPlot().setRangeAxis(1, axis2);
        chart.getXYPlot().setRangeAxisLocation(1, AxisLocation.TOP_OR_RIGHT);
    }

    private CategoryTableXYDataset buildCategoryDataset(double filter[][], TimeSerie obs){
        int n = filter.length;
        if (n==0)
            return null;

        int T = filter[0].length;

        CategoryTableXYDataset tableDataset = new CategoryTableXYDataset();
        for (int i = 0; i < T; i++) {
            for (int j = 0; j < n; j++) {
                if (filter[j][i]==0){
                    tableDataset.add(obs.getTime(i).getTime(), 1.0/n, Integer.toString(2*j),false);
                    tableDataset.add(obs.getTime(i).getTime(), 0, Integer.toString(2*j+1),false);
                }
                else{
                    tableDataset.add(obs.getTime(i).getTime(), 0, Integer.toString(2*j),false);
                    tableDataset.add(obs.getTime(i).getTime(), 1.0/n, Integer.toString(2*j+1),false);
                }
            }
        }
        //to create a notification ..
        tableDataset.setAutoWidth(true);
        return tableDataset;
    }

    public void setHydrograph(TimeSerie hydrograph) {
        this.hydrograph = hydrograph;        
        update();
    }

    public void clearFilter(){
        this.filter.clear();
    }

    public void addFilter(TimeFilter filter){
        this.filter.add(filter);
        update();
    }
    
    public JFreeChart getChart(){
        return chart;
    }

    public XYPlot getXYPlot(){
        return chart.getXYPlot();
    }
    
    public void update(){
        //update peaks
        datasetHydrograph.removeAllSeries();
        datasetFiltered.removeAllSeries();

        int dsCount = chart.getXYPlot().getDatasetCount();

        for (int i = 0; i < dsCount; i++) {
            chart.getXYPlot().setDataset(i, null);
        }


        TimeSeries seriesHydrograph = new TimeSeries("hydrograph");
        
        long n = hydrograph.getTimeDomain().getNumberOfTimesteps();
        
        long lastNonFiltered = 0;
        double filtered[][] = new double[filter.size()][(int)n];
        for (long i = 0; i < n; i++) {
            seriesHydrograph.add(new Day(hydrograph.getTime((int) i)), hydrograph.getValue((int) i));
            for (int j = 0; j < filter.size(); j++) {
                if (this.filter.get(j) == null || this.filter.get(j).isFiltered(hydrograph.getTime((int) i))) {
                    filtered[j][(int) i] = 1.0;
                    lastNonFiltered = i;
                } else if (i - lastNonFiltered == 1) {
                    filtered[j][(int) i] = 0.0;
                }
            }
        }
        
        datasetHydrograph.addSeries(seriesHydrograph);
        
        chart.getXYPlot().setDataset(0, datasetHydrograph);

        chart.getXYPlot().setDataset(1, buildCategoryDataset(filtered, hydrograph));
        StackedXYBarRenderer filterRenderer = new StackedXYBarRenderer(0.0);

        Color list[] = new Color[10];
        list[0] = Color.red;
        list[1] = Color.white;
        list[2] = Color.red;
        list[3] = Color.white;
        list[4] = Color.red;
        list[5] = Color.white;
        list[6] = Color.red;
        list[7] = Color.white;
        list[8] = Color.red;
        list[9] = Color.white;

        for (int i=0;i<10;i++){
            filterRenderer.setSeriesFillPaint(i,list[i]);
            filterRenderer.setSeriesPaint(i,list[i]);
        }
        
        //filterRenderer.setDrawBarOutline(false);
        filterRenderer.setBaseSeriesVisible(true);
        filterRenderer.setOutlinePaint(null);
        filterRenderer.setStroke(new BasicStroke(0.0f));
        
        chart.getXYPlot().setRenderer(1, filterRenderer);
    }
}
