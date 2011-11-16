/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package optas.gui;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import optas.hydro.calculations.BaseFlow;
import optas.hydro.calculations.HydrographSection;
import optas.hydro.calculations.Peak;
import optas.hydro.calculations.RecessionCurve;
import optas.hydro.data.TimeFilter;
import optas.hydro.data.TimeSerie;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYDotRenderer;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.time.Day;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;

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

    ArrayList<Peak> peaks = null;
    ArrayList<RecessionCurve> recessionCurves = null;
    ArrayList<HydrographSection> groundwaterSections = null;

    int[] markedTimeSteps = null;
    TimeFilter filter = null;

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

        XYDotRenderer peakRenderer = new XYDotRenderer();
        peakRenderer.setBaseFillPaint(new Color(0, 0, 255));
        peakRenderer.setDotHeight(5);
        peakRenderer.setDotWidth(5);
        peakRenderer.setShape(new Rectangle2D.Double(0.0, 0.0, 5.0, 5.0));

        XYLineAndShapeRenderer hydrographRenderer = new XYLineAndShapeRenderer();
        hydrographRenderer.setBaseFillPaint(new Color(0, 0, 255));
        hydrographRenderer.setBaseLinesVisible(true);
        hydrographRenderer.setDrawSeriesLineAsPath(true);
        hydrographRenderer.setBaseOutlinePaint(new Color(0, 0, 255));
        hydrographRenderer.setBaseSeriesVisible(true);
        hydrographRenderer.setDrawOutlines(true);
        hydrographRenderer.setBaseShapesVisible(false);
        hydrographRenderer.setStroke(new BasicStroke(1.0f));

        XYLineAndShapeRenderer recessionRenderer = new XYLineAndShapeRenderer();
        recessionRenderer.setBaseFillPaint(new Color(0, 255, 0));
        recessionRenderer.setBaseLinesVisible(true);
        recessionRenderer.setDrawSeriesLineAsPath(true);
        recessionRenderer.setBaseOutlinePaint(new Color(0, 255, 0));
        recessionRenderer.setBaseSeriesVisible(true);
        recessionRenderer.setDrawOutlines(true);
        recessionRenderer.setBaseShapesVisible(false);
        recessionRenderer.setStroke(new BasicStroke(5.0f));

        XYLineAndShapeRenderer groundwaterRenderer = new XYLineAndShapeRenderer();
        groundwaterRenderer.setPaint(new Color(255, 200, 0));
        groundwaterRenderer.setBaseLinesVisible(true);
        groundwaterRenderer.setBaseShapesVisible(false);
        groundwaterRenderer.setDrawSeriesLineAsPath(true);
        groundwaterRenderer.setBaseSeriesVisible(true);

        XYLineAndShapeRenderer baseFlowRenderer = new XYLineAndShapeRenderer();
        baseFlowRenderer.setBaseFillPaint(new Color(128, 128, 128));
        baseFlowRenderer.setBaseLinesVisible(true);
        baseFlowRenderer.setDrawSeriesLineAsPath(true);
        baseFlowRenderer.setBaseOutlinePaint(new Color(128, 128, 128));
        baseFlowRenderer.setBaseSeriesVisible(true);
        baseFlowRenderer.setDrawOutlines(true);
        baseFlowRenderer.setBaseShapesVisible(false);
        baseFlowRenderer.setStroke(new BasicStroke(5.0f));

        XYLineAndShapeRenderer markRenderer = new XYLineAndShapeRenderer();
        markRenderer.setBaseFillPaint(new Color(255, 0, 0));
        markRenderer.setBaseLinesVisible(true);
        markRenderer.setDrawSeriesLineAsPath(true);
        markRenderer.setBaseOutlinePaint(new Color(255, 0, 0));
        markRenderer.setBaseSeriesVisible(true);
        markRenderer.setDrawOutlines(true);
        markRenderer.setBaseShapesVisible(false);
        markRenderer.setStroke(new BasicStroke(5.0f));
        
        XYLineAndShapeRenderer filterRenderer = new XYLineAndShapeRenderer();
        filterRenderer.setBaseFillPaint(new Color(255, 0, 0));
        filterRenderer.setBaseLinesVisible(true);
        filterRenderer.setDrawSeriesLineAsPath(true);
        filterRenderer.setBaseOutlinePaint(new Color(255, 0, 0));
        filterRenderer.setBaseSeriesVisible(true);
        filterRenderer.setDrawOutlines(true);
        filterRenderer.setBaseShapesVisible(false);
        filterRenderer.setStroke(new BasicStroke(5.0f));

        chart.getXYPlot().setRenderer(0, hydrographRenderer); //?

        chart.getXYPlot().setDataset(1, datasetPeaks);
        chart.getXYPlot().setRenderer(1, peakRenderer);
        chart.getXYPlot().setDataset(2, datasetRecessionCurves);
        chart.getXYPlot().setRenderer(2, recessionRenderer);
        chart.getXYPlot().setDataset(3, datasetGroundwater);
        chart.getXYPlot().setRenderer(3, groundwaterRenderer);
        chart.getXYPlot().setDataset(4, datasetBaseFlow);
        chart.getXYPlot().setRenderer(4, baseFlowRenderer);

        chart.getXYPlot().setDataset(5, datasetMark);
        chart.getXYPlot().setRenderer(5, markRenderer);

        chart.getXYPlot().setDataset(6, datasetFiltered);
        chart.getXYPlot().setRenderer(6, filterRenderer);
    }

    public void setHydrograph(TimeSerie hydrograph) {
        this.hydrograph = hydrograph;

        peaks = Peak.findPeaks(hydrograph);
        recessionCurves = RecessionCurve.findRecessionCurves(hydrograph);
        groundwater = BaseFlow.calculateGroundwater(hydrograph);
        groundwaterSections = BaseFlow.calculateBaseFlowPeriods(hydrograph, this.groundwaterThreshold);

        update();
    }

    public void setFilter(TimeFilter filter){
        this.filter = filter;
        update();
    }

    public void setMarkSerie(int[] markedTimeSteps){
        this.markedTimeSteps = markedTimeSteps;
        update();
    }

    public void setVisiblePeaks(int peakCount){
        this.peakCount = peakCount;
        update();
    }
    public void setVisibleRecessions(int recessionCount){
        this.recessionCount = recessionCount;
        update();
    }
    public int getPeakCount(){
        return this.peaks.size();
    }

    public JFreeChart getChart(){
        return chart;
    }

    public XYPlot getXYPlot(){
        return chart.getXYPlot();
    }

    public void setGroundwaterThreshold(double groundwaterThreshold){
        groundwaterSections = BaseFlow.calculateBaseFlowPeriods(hydrograph, groundwaterThreshold);
        update();
    }

    public void setGroundwaterWindowSize(int windowSize){
        groundwater = BaseFlow.calculateGroundwater(hydrograph, windowSize);
        groundwaterSections = BaseFlow.calculateBaseFlowPeriods(hydrograph, groundwaterThreshold, windowSize);
        update();
    }

    public void update(){
        //update peaks
        datasetHydrograph.removeAllSeries();
        datasetFiltered.removeAllSeries();
        TimeSeries seriesHydrograph = new TimeSeries("hydrograph");
        TimeSeries seriesFiltered = new TimeSeries("filtered");

        long n = hydrograph.getTimeDomain().getNumberOfTimesteps();
        long lastNonFiltered = 0;
        for (long i = 0; i < n; i++) {
            seriesHydrograph.add(new Day(hydrograph.getTime((int) i)), hydrograph.getValue((int) i));
            if (this.filter==null || this.filter.isFiltered(hydrograph.getTime((int) i))){
                seriesFiltered.add(new Day(hydrograph.getTime((int) i)), hydrograph.getValue((int) i));
                lastNonFiltered = i;
            }else if ( i - lastNonFiltered == 1)
                seriesFiltered.add(new Day(hydrograph.getTime((int) i)), Double.NaN);

        }
        datasetHydrograph.addSeries(seriesHydrograph);
        datasetFiltered.addSeries(seriesFiltered);


        chart.getXYPlot().setDataset(0, datasetHydrograph);
        chart.getXYPlot().setDataset(6, datasetFiltered);

        datasetPeaks.removeAllSeries();

        TimeSeries seriesPeaks = new TimeSeries("peaks");

        for (int i = 0; i < this.peakCount; i++) {
            int index = this.peaks.get(i).index;
            double value = this.peaks.get(i).value;
            seriesPeaks.add(new Day(hydrograph.getTime(index)), value);
        }
        datasetPeaks.addSeries(seriesPeaks);

        datasetRecessionCurves.removeAllSeries();

        TimeSeries seriesRecession = new TimeSeries("recession");

        for (int i = 0; i < this.recessionCount; i++) {
            for (int index = this.recessionCurves.get(i).startIndex; index < this.recessionCurves.get(i).endIndex; index++) {
                double value = this.recessionCurves.get(i).at(index);
                seriesRecession.addOrUpdate(new Day(hydrograph.getTime(index)), value);
            }
            seriesRecession.addOrUpdate(new Day(hydrograph.getTime(this.recessionCurves.get(i).endIndex)), Double.NaN);
        }
        datasetRecessionCurves.addSeries(seriesRecession);

        datasetGroundwater.removeAllSeries();

        TimeSeries groundwaterSeries = new TimeSeries("groundwater(est)");

        n = groundwater.getTimeDomain().getNumberOfTimesteps();
        for (long i = 0; i < n; i++) {
            groundwaterSeries.add(new Day(groundwater.getTime((int) i)), groundwater.getValue((int) i));
        }
        datasetGroundwater.addSeries(groundwaterSeries);

        datasetBaseFlow.removeAllSeries();

        TimeSeries groundwaterPeriodSeries = new TimeSeries("baseflowPeriod");

        for (int i = 0; i < this.groundwaterSections.size(); i++) {
            for (int index = this.groundwaterSections.get(i).startIndex; index < this.groundwaterSections.get(i).endIndex; index++) {
                double value = this.groundwaterSections.get(i).at(index);
                groundwaterPeriodSeries.addOrUpdate(new Day(hydrograph.getTime(index)), value);
            }
            groundwaterPeriodSeries.addOrUpdate(new Day(hydrograph.getTime(this.groundwaterSections.get(i).endIndex)), Double.NaN);
        }
        datasetBaseFlow.addSeries(groundwaterPeriodSeries);

        datasetMark.removeAllSeries();
        
        if (markedTimeSteps != null) {
            TimeSeries seriesMark = new TimeSeries("mark");
            int c=0;            
            for (int i=0;i<n;i++){
                if (c>=markedTimeSteps.length)
                    c=markedTimeSteps.length-1;
                int timeIndex = this.markedTimeSteps[c];
                if (i==timeIndex){
                    double value = this.hydrograph.getValue(timeIndex);
                    seriesMark.add(new Day(hydrograph.getTime(i)), value);
                    c++;
                }else if (i<timeIndex){
                    seriesMark.add(new Day(hydrograph.getTime(i)), Double.NaN);
                }else{
                    c++;
                }
            }
            for (int i = 0; i < markedTimeSteps.length; i++) {
                
            }
            datasetMark.addSeries(seriesMark);
            chart.getXYPlot().setDataset(5, datasetMark);
        }
    }

    public void showPeaks(boolean show) {
        //peakSlider.setMaximum(this.peaks.size());
        if (show) {
            chart.getXYPlot().setDataset(1, datasetPeaks);
        } else {
            chart.getXYPlot().setDataset(1, null);
        }
    }

    public void showRecessionCurve(boolean show) {
        //recessionSlider.setMaximum(this.recessionCurves.size());
        if (show) {
            chart.getXYPlot().setDataset(2, datasetRecessionCurves);
        } else {
            chart.getXYPlot().setDataset(2, null);
        }
    }

    public int getRecessionCount(){
        return recessionCurves.size();
    }

    public void showGroundwaterCurve(boolean show) {        
        if (show) {
            chart.getXYPlot().setDataset(3, datasetGroundwater);
        } else {
            chart.getXYPlot().setDataset(3, null);
        }
    }

    public void showBaseFlowPeriods(boolean show) {
        //recessionSlider.setMaximum(this.recessionCurves.size());
        if (show) {
            chart.getXYPlot().setDataset(4, datasetBaseFlow);
        } else {
            chart.getXYPlot().setDataset(4, null);
        }
    }
}
