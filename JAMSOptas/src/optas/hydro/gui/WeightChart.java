/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package optas.hydro.gui;

import java.awt.Color;
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

    private CategoryTableXYDataset buildCategoryDataset(double weights[][], 
            SimpleEnsemble p[], TimeSerie obs, boolean enableList[], boolean showList[]){

        int T = (int)obs.getTimeDomain().getNumberOfTimesteps();
        int n = weights.length;
        double sum[] = new double[T];

        CategoryTableXYDataset tableDataset = new CategoryTableXYDataset();
        for (int i = 0; i < T; i++) {
            for (int j = 0; j < n; j++) {
                if (enableList[j])
                    sum[i] += weights[j][i];
            }
            for (int j = 0; j < n; j++) {
                if (showList[j])
                    tableDataset.add(obs.getTime(i).getTime(), weights[j][i] / sum[i], p[j].toString(),false);
            }
        }
        //to create a notification ..
        tableDataset.setAutoWidth(true);
        return tableDataset;
    }

    public void update(double[][] weights,SimpleEnsemble parameter[], TimeSerie obs, boolean enableList[], boolean showList[], Color[] colorList ){
        int dsCount = weightChart.getXYPlot().getDatasetCount();

        for (int i = 0; i < dsCount; i++) {
            weightChart.getXYPlot().setDataset(i, null);
        }

        CategoryTableXYDataset dataset = buildCategoryDataset(weights, parameter, obs, enableList, showList);

        Color list[] = new Color[enableList.length];
        int index = 0;
        for (int i=0;i<showList.length;i++){
            if (showList[i])
                list[index++] = colorList[i];
        }

        weightChart.getXYPlot().setDataset(10, dataset);
        weightChart.getXYPlot().setRenderer(10, weightBarRenderer);
        for (int i=0;i<colorList.length;i++){
            weightBarRenderer.setSeriesFillPaint(i,list[i]);
            weightBarRenderer.setSeriesPaint(i,list[i]);
        }
    }
}
