/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package optas.gui.wizard;

import jams.JAMS;
import jams.gui.WorkerDlg;
import jams.gui.tools.GUIState;
import java.awt.BasicStroke;
import java.awt.Color;
import java.util.Calendar;
import java.util.HashMap;
import java.util.TimeZone;
import javax.swing.SwingUtilities;
import optas.data.TimeFilter;
import optas.data.TimeFilterCollection;
import optas.data.TimeFilterFactory;
import optas.data.TimeSerie;
import optas.tools.PatchedChartPanel;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.AxisLocation;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.StackedXYBarRenderer;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.Range;
import org.jfree.data.time.Day;
import org.jfree.data.time.Hour;
import org.jfree.data.time.Month;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;
import org.jfree.data.xy.CategoryTableXYDataset;

/**
 *
 * @author chris
 */
public class HydrographChart {
    static TimeZone GMT_TZ = TimeZone.getTimeZone("GMT");
    static HashMap<Integer, TimeSeries> HydrographBuffer = new HashMap<Integer, TimeSeries>();
    
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
        
    PatchedChartPanel panel = null;
    
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
        chart.getPlot().setBackgroundPaint(Color.white);
        chart.getXYPlot().setDomainGridlinePaint(Color.black);
            
        StackedXYBarRenderer filterRenderer1 = new StackedXYBarRenderer(0.0);

        Color list1[] = new Color[10];        
        for (int i=0;i<10;i++){
            if (i%4==0){
                list1[i] = Color.red;                
            }else if (i%4==1){
                list1[i] = new Color(1.0f,1.0f,1.0f,0.0f);         
            }else if (i%4==2){
                list1[i] = new Color(0.0f,1.0f,0.0f);                 
            }else if (i%4==3){
                list1[i] = Color.MAGENTA;
            }
        }
        
        for (int i=0;i<10;i++){
            filterRenderer1.setSeriesFillPaint(i,list1[i]);
            filterRenderer1.setSeriesPaint(i,list1[i]);  
            if (i%4 == 2){
                filterRenderer1.setSeriesOutlineStroke(i, new BasicStroke(20.0f));
                filterRenderer1.setSeriesStroke(i, new BasicStroke(20.0f));
            }
        }
        
        filterRenderer1.setBaseSeriesVisible(true);
        filterRenderer1.setOutlinePaint(null);
        filterRenderer1.setStroke(new BasicStroke(0.0f));
        filterRenderer1.setShadowVisible(false);

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
                    tableDataset.add(obs.getTime(i).getTime(), 1.0/n, Integer.toString(4*j),false);
                    tableDataset.add(obs.getTime(i).getTime(), 0, Integer.toString(4*j+1),false);
                    tableDataset.add(obs.getTime(i).getTime(), 0, Integer.toString(4*j+2),false);
                    tableDataset.add(obs.getTime(i).getTime(), 0, Integer.toString(4*j+3),false);
                }else if (filter[j][i]==1){
                    tableDataset.add(obs.getTime(i).getTime(), 0.0, Integer.toString(4*j),false);
                    tableDataset.add(obs.getTime(i).getTime(), 1.0/n, Integer.toString(4*j+1),false);
                    tableDataset.add(obs.getTime(i).getTime(), 0, Integer.toString(4*j+2),false);
                    tableDataset.add(obs.getTime(i).getTime(), 0, Integer.toString(4*j+3),false);
                }else if (filter[j][i]==2){
                    tableDataset.add(obs.getTime(i).getTime(), 0.0, Integer.toString(4*j),false);
                    tableDataset.add(obs.getTime(i).getTime(), 0.0, Integer.toString(4*j+1),false);
                    tableDataset.add(obs.getTime(i).getTime(), 1.0/n, Integer.toString(4*j+2),false);
                    tableDataset.add(obs.getTime(i).getTime(), 0.0, Integer.toString(4*j+3),false);
                }else{
                    tableDataset.add(obs.getTime(i).getTime(), 0.0, Integer.toString(4*j),false);
                    tableDataset.add(obs.getTime(i).getTime(), 0.0, Integer.toString(4*j+1),false);
                    tableDataset.add(obs.getTime(i).getTime(), 0.0, Integer.toString(4*j+2),false);
                    tableDataset.add(obs.getTime(i).getTime(), 1.0/n, Integer.toString(4*j+3),false);
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
        if (this.hydrograph!=hydrograph){
            this.hydrograph = hydrograph;        
            update();
        }
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
        setTimeFilters(timeFilters, false);
    }
    public void setTimeFilters(TimeFilterCollection timeFilters, boolean forceUpdate){
        boolean isTheSame = true;
        if (timeFilters.size() == this.filters.size()){
            for (int i=0;i<timeFilters.size();i++){
                if (timeFilters.get(i).equals(this.filters.get(i))){
                    isTheSame = false;
                }
            }
        }else {
            isTheSame = false;
        }
        
        if (!isTheSame || forceUpdate){
            clearTimeFilter();
            addTimeFilters(timeFilters);
        }
    }
    
    public void addTimeFilters(TimeFilterCollection timeFilters){
        if (timeFilters.size()==0) {
            return;
        }
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
    
    public PatchedChartPanel getChartPanel(){
        if (panel == null){
            panel = new PatchedChartPanel(chart, true);
            panel.setMinimumDrawWidth( 0 );
            panel.setMinimumDrawHeight( 0 );
            panel.setMaximumDrawWidth( 2000 );
            panel.setMaximumDrawHeight( 2000 );
        }
        return panel;
    }

    public XYPlot getXYPlot(){
        return chart.getXYPlot();
    }
    
    public void setSelectedTimeFilter(TimeFilter f){  
        if (selectedTimeFilter == f)
            return;
        
        if (selectedTimeFilter==null || f==null || f.toString().compareTo(selectedTimeFilter.toString())!=0){        
            selectedTimeFilter = f;
            if (f instanceof TimeFilterFactory.RangeTimeFilter) {
                update();
            }
        }
    }
    
    public void update(){       
        Runnable r = new Runnable() {
            @Override
            public void run() {
                //update peaks
                datasetHydrograph.removeAllSeries();

                int dsCount = chart.getXYPlot().getDatasetCount();
                
                for (int i = 0; i < dsCount; i++) {
                    chart.getXYPlot().setDataset(i, null);
                }

                if (hydrograph == null) {
                    return;
                }
                                
                //System.out.println("Hydrograph Update!!");                                
                long n = hydrograph.getTimeDomain().getNumberOfTimesteps();

                long lastNonFiltered = 0;
                double filtered[][] = null;

                if (filterMode == FilterMode.SINGLE_ROW) {
                    filtered = new double[1][(int) n];
                } else {
                    filtered = new double[filters.size()][(int) n];
                }

                TimeFilter localSelection = null;
                if (selectedTimeFilter instanceof TimeFilterFactory.RangeTimeFilter) {
                    localSelection = selectedTimeFilter;
                }

                TimeFilter combinedFilter = filters.combine();
                TimeFilterCollection filtersWithoutSelection = new TimeFilterCollection();
                for (TimeFilter tf : filters.get()) {
                    if (tf != localSelection) {
                        filtersWithoutSelection.add(tf);
                    }
                }
                TimeFilter combinedFilter2 = filtersWithoutSelection.combine();
                                
                int hashKey = hydrograph.hashCode();
                TimeSeries seriesHydrograph = HydrographBuffer.get(hashKey);
                if (seriesHydrograph == null){
                    seriesHydrograph = new TimeSeries("Hydrograph");
                    for (int i = 0; i < n; i++) {
                        if (hydrograph.getValue((int) i) == JAMS.getMissingDataValue()) {
                            if (hydrograph.getTimeDomain().getTimeUnit() == Calendar.MONTH) {
                                seriesHydrograph.add(new Month(hydrograph.getTime((int) i), GMT_TZ), Double.NaN);
                            } else if (hydrograph.getTimeDomain().getTimeUnit() == Calendar.DAY_OF_YEAR) {
                                seriesHydrograph.add(new Day(hydrograph.getTime((int) i), GMT_TZ), Double.NaN);
                            } else if (hydrograph.getTimeDomain().getTimeUnit() == Calendar.HOUR_OF_DAY) {
                                seriesHydrograph.add(new Hour(hydrograph.getTime((int) i), GMT_TZ), Double.NaN);
                            }
                        } else {
                            if (hydrograph.getTimeDomain().getTimeUnit() == Calendar.MONTH) {
                                seriesHydrograph.add(new Month(hydrograph.getTime((int) i), GMT_TZ), hydrograph.getValue((int) i));
                            } else if (hydrograph.getTimeDomain().getTimeUnit() == Calendar.DAY_OF_YEAR) {
                                seriesHydrograph.add(new Day(hydrograph.getTime((int) i), GMT_TZ), hydrograph.getValue((int) i));
                            } else if (hydrograph.getTimeDomain().getTimeUnit() == Calendar.HOUR_OF_DAY) {
                                seriesHydrograph.add(new Hour(hydrograph.getTime((int) i), GMT_TZ), hydrograph.getValue((int) i));
                            }
                        }
                    }
                    HydrographBuffer.put(hashKey, seriesHydrograph);
                }
                for (int i = 0; i < n; i++) {
                    if (filterMode != FilterMode.SINGLE_ROW) {
                        for (int j = 0; j < filters.size(); j++) {
                            if (filters.get(j) == null || filters.get(j).isFiltered(hydrograph.getTime((int) i))) {
                                filtered[j][(int) i] = 1.0;
                                lastNonFiltered = i;
                            } else if (i - lastNonFiltered == 1) {
                                filtered[j][(int) i] = 0.0;
                            }
                        }
                    } else {
                        //I. rot -> es wird gefiltert und es wird nicht von der selektion gefiltert
                        //II. grün -> es wird nur von der selektion gefiltert
                        //III. overlay -> es wird nicht nur von der selektion gefiltert

                        filtered[0][i] = 1.0;
                        //es wird gefiltert und es wird auch ohne selektion gefiltert -> III
                        if (!combinedFilter.isFiltered(hydrograph.getTime(i)) && !combinedFilter2.isFiltered(hydrograph.getTime(i))) {
                            if (localSelection == null || localSelection.isFiltered(hydrograph.getTime(i))) {
                                filtered[0][i] = 0.0;
                            } else {
                                filtered[0][i] = 3.0;
                            }
                            //es wird gefiltert und es wird ohne selektion nicht gefiltert -> II.
                        } else if (!combinedFilter.isFiltered(hydrograph.getTime(i)) && combinedFilter2.isFiltered(hydrograph.getTime(i))) {
                            filtered[0][i] = 2.0;
                        }

                    }
                }

                datasetHydrograph.addSeries(seriesHydrograph);

                chart.getXYPlot().setDataset(0, datasetHydrograph);
                chart.getXYPlot().setDataset(1, buildCategoryDataset(filtered, hydrograph));
            }
        };
        if (hydrograph == null) {
            datasetHydrograph.removeAllSeries();

            int dsCount = chart.getXYPlot().getDatasetCount();

            for (int i = 0; i < dsCount; i++) {
                chart.getXYPlot().setDataset(i, null);
            }
        }else if (panel != null && !panel.isShowing()){
            SwingUtilities.invokeLater(r);
        }else{
            WorkerDlg progress = new WorkerDlg(GUIState.getMainWindow(), "Updating plot");
            progress.setInderminate(true);
            progress.setTask(r);
            progress.execute();
        }
        
    }
}
