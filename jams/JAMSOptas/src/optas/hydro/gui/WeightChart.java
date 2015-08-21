/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package optas.hydro.gui;

import java.awt.BasicStroke;
import java.awt.Color;
import optas.data.SimpleEnsemble;
import optas.data.TimeSerie;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.AxisLocation;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.labels.XYToolTipGenerator;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.StackedXYBarRenderer;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.time.Day;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;
import org.jfree.data.xy.CategoryTableXYDataset;
import org.jfree.data.xy.XYDataset;

/**
 *
 * @author chris
 */
public class WeightChart {
    JFreeChart weightChart;
    StackedXYBarRenderer weightBarRenderer = new StackedXYBarRenderer(0.33);

    XYLineAndShapeRenderer qualityRenderer = new XYLineAndShapeRenderer();
    SimpleEnsemble parameter[];

    public WeightChart() {
        TimeSeriesCollection dataset1 = new TimeSeriesCollection();
        
        this.weightChart = ChartFactory.createTimeSeriesChart(
                "Hydrograph",
                "time",
                "weights",
                dataset1,
                true,
                true,
                false);
    }



    public JFreeChart getChart(){
        return weightChart;
    }

    public XYPlot getXYPlot(){
        return this.weightChart.getXYPlot();
    }

    private TimeSeriesCollection buildQualityDataset(TimeSerie obs, double quality[]){
        TimeSeriesCollection seriesQuality = new TimeSeriesCollection();

        TimeSeries cvError = new TimeSeries("CV - Error");

        for (int i = 0; i < quality.length; i++) {
            cvError.add(new Day(obs.getTime(i)), quality[i]);
        }
        seriesQuality.addSeries(cvError);

        return seriesQuality;
    }

    private CategoryTableXYDataset buildCategoryDataset(double weights[][],
            SimpleEnsemble p[], TimeSerie obs, boolean enableList[], boolean showList[]){

        int T = (int)obs.getTimeDomain().getNumberOfTimesteps();
        int n = weights.length;
        double sum[] = new double[T];

        CategoryTableXYDataset tableDataset = new CategoryTableXYDataset();
        double maxSum = 0;
        for (int i = 0; i < T; i++) {
            for (int j = 0; j < n; j++) {
                if (enableList[j]){
                    sum[i] += weights[j][i];
                }
            }
            maxSum = Math.max(maxSum,sum[i]);
        }
        for (int i = 0; i < T; i++) {
            for (int j = 0; j < n; j++) {
                if (showList[j]){
                    if (weights[j][i] / sum[i] < 0.025 && weights[j][i] / maxSum < 0.025) {
                        tableDataset.add(obs.getTime(i).getTime(), 0, p[j].toString(),false);
                    }
                    else {
                        tableDataset.add(obs.getTime(i).getTime(), weights[j][i] / sum[i], p[j].toString(),false);
                    }
                }
            }
        }
        //to create a notification ..
        tableDataset.setAutoWidth(true);
        return tableDataset;
    }

    public void update(double[][] weights, double quality[], SimpleEnsemble parameter[], TimeSerie obs, boolean enableList[], boolean showList[], Color[] colorList ){
        this.parameter = parameter;
        int dsCount = weightChart.getXYPlot().getDatasetCount();

        for (int i = 0; i < dsCount; i++) {
            weightChart.getXYPlot().setDataset(i, null);
        }

        CategoryTableXYDataset dataset = buildCategoryDataset(weights, parameter, obs, enableList, showList);

        Color list[] = new Color[enableList.length];
        int index = 0;
        for (int i=0;i<showList.length;i++){
            if (showList[i]) {
                list[index++] = colorList[i];
            }
        }

        qualityRenderer.setBaseFillPaint(new Color(255, 255, 255));
        qualityRenderer.setBaseLinesVisible(true);
        qualityRenderer.setDrawSeriesLineAsPath(true);
        qualityRenderer.setBaseOutlinePaint(new Color(255, 255, 255));
        qualityRenderer.setBaseSeriesVisible(true);
        qualityRenderer.setDrawOutlines(true);
        qualityRenderer.setPaint(new Color(0, 0, 0));
        qualityRenderer.setBaseShapesVisible(false);

        qualityRenderer.setStroke(new BasicStroke(5.0f));

        if (quality != null){
            TimeSeriesCollection qualityDataset = buildQualityDataset(obs, quality);

            weightChart.getXYPlot().setDataset(9, qualityDataset);
            weightChart.getXYPlot().setRenderer(9, qualityRenderer);
             // AXIS 2
            NumberAxis axis2 = new NumberAxis("CV Error");
            axis2.setAutoRangeIncludesZero(false);            
            weightChart.getXYPlot().setRangeAxis(1, axis2);
            weightChart.getXYPlot().setRangeAxisLocation(1, AxisLocation.TOP_OR_RIGHT);
            weightChart.getXYPlot().mapDatasetToRangeAxis(9, 1);

        }
        weightChart.getXYPlot().setDataset(10, dataset);
        weightChart.getXYPlot().setRenderer(10, weightBarRenderer);

        for (int i=0;i<list.length;i++){
            weightBarRenderer.setSeriesFillPaint(i,list[i]);
            weightBarRenderer.setSeriesPaint(i,list[i]);
        }

        weightBarRenderer.setToolTipGenerator(new XYToolTipGenerator() {

            @Override
            public String generateToolTip(XYDataset xyd, int i, int i1) {

                Number nx = xyd.getX(i, i1);
                Number ny = xyd.getY(i, i1);

                if (WeightChart.this.parameter!=null && WeightChart.this.parameter.length > i){
                    String name = WeightChart.this.parameter[i].getName();

                    return name + ":" + ny;
                }
                return "";
            }
        });
    }
}
