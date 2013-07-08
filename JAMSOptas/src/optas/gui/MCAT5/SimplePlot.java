/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package optas.gui.MCAT5;

import jams.JAMS;
import java.awt.Color;
import javax.swing.JPanel;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYDotRenderer;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;

/**
 *
 * @author Christian Fischer
 */
public class SimplePlot{

    protected XYPlot plot = new XYPlot();
    protected ChartPanel chartPanel = null;

    String xAxis[];
    double xData[];
    double yData[][];

    String xLabel,yLabel;
    private void init() {
        //setup renderer
        XYDotRenderer renderer = new XYDotRenderer();
        renderer.setSeriesPaint(0, Color.BLUE);
        renderer.setDotHeight(3);
        renderer.setDotWidth(3);
        //setup plot
        plot.setRenderer(renderer);
        //setup chart
        JFreeChart chart = new JFreeChart(plot);
        chart.setTitle(JAMS.i18n("DOTTY_PLOT"));
        chartPanel = new ChartPanel(chart, true);

        refresh();
    }

    public SimplePlot(String xAxis[], double xData[], double yData[][], String xLabel, String yLabel) {
        this.xAxis = xAxis;
        this.xData = xData;
        this.yData = yData;
        this.xLabel = xLabel;
        this.yLabel = yLabel;

        init();
    }

    public JPanel getPanel() {
        return this.chartPanel;
    }

    public void refresh() {
        int m = yData.length;
        int n = xData.length;

        for (int j = 0; j < m; j++) {
            XYSeries dataset = new XYSeries(JAMS.i18n("DATA_POINT"));

            for (int i = 0; i < n; i++) {
                dataset.add(xData[i],yData[j][i]);
            }
            plot.setDataset(j, new XYSeriesCollection(dataset));
        }

        if (m==1){
            XYDotRenderer renderer = new XYDotRenderer();
            renderer.setSeriesPaint(0, Color.BLUE);
            renderer.setDotHeight(3);
            renderer.setDotWidth(3);
            //setup plot
            plot.setRenderer(renderer);
        }else{
            XYLineAndShapeRenderer renderer = new XYLineAndShapeRenderer(true, false);
            plot.setRenderer(renderer);
        }

        if (plot.getRangeAxis() != null) {
            plot.getRangeAxis().setAutoRange(true);
        }
        if (plot.getDomainAxis() != null) {
            plot.getDomainAxis().setAutoRange(true);
        }

    }
}
