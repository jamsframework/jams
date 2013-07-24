/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package optas.gui.wizard;

import jams.JAMS;
import java.awt.BasicStroke;
import java.awt.Color;
import optas.hydro.data.TimeFilter;
import optas.hydro.data.TimeFilterCollection;
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
    
    TimeSeriesCollection datasetPeaks, datasetRecessionCurves, datasetGroundwater, datasetBaseFlow, datasetMark;

    TimeSerie hydrograph;
    TimeSerie groundwater;
    
    int peakCount = 0;
    int recessionCount = 0;
    double groundwaterThreshold = 0;
    
    public enum FilterMode{
        SINGLE_ROW,
        MULTI_ROW
    }
    FilterMode filterMode = FilterMode.MULTI_ROW;    
    
    TimeFilterCollection filters = new TimeFilterCollection();
    TimeFilter selectedTimeFilter = null;
        
    public HydrographChart(){
                        
         chart = ChartFactory.createTimeSeriesChart(
                "Hydrograph",
                "time",
                "runoff",
                datasetHydrograph,
                false,
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
        
        StackedXYBarRenderer filterRenderer1 = new StackedXYBarRenderer(0.0);

        Color list1[] = new Color[10];        
        for (int i=0;i<10;i++){
            if (i%3==0){
                list1[i] = Color.red;                
            }else if (i%3==1){
                list1[i] = Color.white;                
            }else if (i%3==2){
                list1[i] = Color.green;                
            }
        }
        
        for (int i=0;i<10;i++){
            filterRenderer1.setSeriesFillPaint(i,list1[i]);
            filterRenderer1.setSeriesPaint(i,list1[i]);            
        }
        
        filterRenderer1.setBaseSeriesVisible(true);
        filterRenderer1.setOutlinePaint(null);
        filterRenderer1.setStroke(new BasicStroke(0.0f));
                                                
        chart.getXYPlot().setRenderer(1, filterRenderer1);                
                
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
                    tableDataset.add(obs.getTime(i).getTime(), 1.0/n, Integer.toString(3*j),false);
                    tableDataset.add(obs.getTime(i).getTime(), 0, Integer.toString(3*j+1),false);
                    tableDataset.add(obs.getTime(i).getTime(), 0, Integer.toString(3*j+2),false);
                }else if (filter[j][i]==1){
                    tableDataset.add(obs.getTime(i).getTime(), 0.0, Integer.toString(3*j),false);
                    tableDataset.add(obs.getTime(i).getTime(), 1.0/n, Integer.toString(3*j+1),false);
                    tableDataset.add(obs.getTime(i).getTime(), 0, Integer.toString(3*j+2),false);
                }else{
                    tableDataset.add(obs.getTime(i).getTime(), 0.0, Integer.toString(3*j),false);
                    tableDataset.add(obs.getTime(i).getTime(), 0.0, Integer.toString(3*j+1),false);
                    tableDataset.add(obs.getTime(i).getTime(), 1.0/n, Integer.toString(3*j+2),false);
                }
            }
        }        
        double width = obs.getTime(1).getTime() - obs.getTime(0).getTime();
        tableDataset.setIntervalWidth(width);
        /*tableDataset.getEndX(0, 0)
        tableDataset.getStartX(0, 0)*/        
        
        //to create a notification ..
        //tableDataset.setAutoWidth(true);
        return tableDataset;
    }

    public void setHydrograph(TimeSerie hydrograph) {
        this.hydrograph = hydrograph;        
        update();
    }

    public void clearTimeFilter(){
        filters.clear();
        selectedTimeFilter = null;
    }

    public void addTimeFilter(TimeFilter filter){
        this.filters.add(filter);
        update();
    }
    
    public void setTimeFilters(TimeFilterCollection timeFilters){
        clearTimeFilter();
        addTimeFilters(timeFilters);        
    }
    
    public void addTimeFilters(TimeFilterCollection timeFilters){
        for (TimeFilter f : timeFilters.get()){
            this.filters.add(f);
        }
        update();
    }
    
    public void setFilterMode(FilterMode filterMode){
        this.filterMode = filterMode;
    }
    
    public FilterMode getFilterMode(){
        return this.filterMode;
    }
    
    public JFreeChart getChart(){
        return chart;
    }

    public XYPlot getXYPlot(){
        return chart.getXYPlot();
    }
    
    public void setSelectedTimeFilter(TimeFilter f){        
        if (selectedTimeFilter==null || f==null || f.toString().compareTo(selectedTimeFilter.toString())!=0){        
            selectedTimeFilter = f;
            update();
        }
    }
    
    public void update(){
        //update peaks
        datasetHydrograph.removeAllSeries();

        int dsCount = chart.getXYPlot().getDatasetCount();

        for (int i = 0; i < dsCount; i++) {
            chart.getXYPlot().setDataset(i, null);
        }

        System.out.println("Hydrograph Update!!");
        TimeSeries seriesHydrograph = new TimeSeries("hydrograph");
        
        if (hydrograph == null)
            return;
        
        long n = hydrograph.getTimeDomain().getNumberOfTimesteps();
        
        long lastNonFiltered = 0;        
        double filtered[][] = null;
        
        if (this.filterMode == FilterMode.SINGLE_ROW){
            filtered = new double[1][(int)n];
        }else{
            filtered = new double[filters.size()][(int)n];
        }
        
        TimeFilter combinedFilter = filters.combine();
        
        for (int i = 0; i < n; i++) {
            if ( hydrograph.getValue((int) i) == JAMS.getMissingDataValue()){
                seriesHydrograph.add(new Day(hydrograph.getTime((int) i)), Double.NaN);
            }else{
                seriesHydrograph.add(new Day(hydrograph.getTime((int) i)), hydrograph.getValue((int) i));
            }
            
            if (filterMode != FilterMode.SINGLE_ROW) {
                for (int j = 0; j < filters.size(); j++) {
                    if (filters.get(j) == null || filters.get(j).isFiltered(hydrograph.getTime((int) i))) {
                        filtered[j][(int) i] = 1.0;
                        lastNonFiltered = i;
                    } else if (i - lastNonFiltered == 1) {
                        filtered[j][(int) i] = 0.0;
                    }
                }
            }else{
                filtered[0][i] = 1.0;                
                if (!combinedFilter.isFiltered(hydrograph.getTime(i))){
                    filtered[0][i] = 0.0;
                }         
                if ( (selectedTimeFilter!=null && !selectedTimeFilter.isFiltered(hydrograph.getTime(i)))){
                    filtered[0][i] = 2.0;
                }                
            }
        }
                
        datasetHydrograph.addSeries(seriesHydrograph);
        
        chart.getXYPlot().setDataset(0, datasetHydrograph);
        chart.getXYPlot().setDataset(1, buildCategoryDataset(filtered, hydrograph));             
    }
}
