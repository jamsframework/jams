/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package optas.hydro.gui;

import optas.hydro.calculations.SlopeCalculations;
import optas.hydro.data.DataCollection;
import optas.hydro.data.SimpleEnsemble;
import optas.hydro.data.TimeSerie;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.StackedXYBarRenderer;
import org.jfree.data.time.TimeSeriesCollection;
import org.jfree.data.xy.CategoryTableXYDataset;

/**
 *
 * @author chris
 */
public class WeightChart {
    JFreeChart weightChart;
    StackedXYBarRenderer weightBarRenderer = new StackedXYBarRenderer(0.33);
    
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

    private CategoryTableXYDataset buildCategoryDataset(double weights[][], TimeSerie obs, DataCollection d){
        SimpleEnsemble p[] = SlopeCalculations.getParameterEnsembles(d);

        int T = (int)obs.getTimeDomain().getNumberOfTimesteps();
        int n = weights.length;
        double sum[] = new double[T];

        CategoryTableXYDataset tableDataset = new CategoryTableXYDataset();
        for (int i = 0; i < T; i++) {
            for (int j = 0; j < n; j++) {
                sum[i] += weights[j][i];
            }
            for (int j = 0; j < n; j++) {
                tableDataset.add(obs.getTime(i).getTime(), weights[j][i] / sum[i], p[j].toString(),false);
            }
        }
        //to create a notification ..
        tableDataset.setAutoWidth(true);
        return tableDataset;
    }

    public void update(double[][] weights, TimeSerie obsTS, DataCollection ensemble){
        int dsCount = weightChart.getXYPlot().getDatasetCount();

        for (int i = 0; i < dsCount; i++) {
            weightChart.getXYPlot().setDataset(i, null);
        }

        CategoryTableXYDataset dataset = buildCategoryDataset(weights, obsTS, ensemble);

        weightChart.getXYPlot().setDataset(10, dataset);
        weightChart.getXYPlot().setRenderer(10, weightBarRenderer);
    }
}
