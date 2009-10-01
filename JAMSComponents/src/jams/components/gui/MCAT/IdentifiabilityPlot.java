/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package jams.components.gui.MCAT;

import jams.components.gui.MCAT.MCAT5.ArrayComparator;
import jams.components.gui.MCAT.MCAT5.EfficiencyDataSet;
import jams.components.gui.MCAT.MCAT5.ParameterSet;
import java.awt.Color;
import java.util.Arrays;
import javax.swing.JPanel;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYBarRenderer;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

/**
 *
 * @author Christian Fischer
 */
@SuppressWarnings({"unchecked"})
public class IdentifiabilityPlot {   
    XYPlot plot = new XYPlot();    
    ChartPanel chartPanel = null;
    
    ParameterSet data = null;
    EfficiencyDataSet eff = null;
         
    final int BOX_COUNT = 10;
    public IdentifiabilityPlot(ParameterSet data, EfficiencyDataSet eff) {
        this.data = data;
        this.eff = eff;
                
        plot.setDomainAxis(new NumberAxis(data.name));
        plot.setRangeAxis(new NumberAxis("cumulative distribution"));

        JFreeChart chart = new JFreeChart(plot);
        chart.removeLegend();
        chart.setTitle("Identifyablity Plot");        
        chartPanel = new ChartPanel(chart, true);        
        
        XYLineAndShapeRenderer gradient_renderer = new XYLineAndShapeRenderer();
        gradient_renderer.setSeriesPaint(0, Color.BLACK);
        gradient_renderer.setBaseShapesVisible(false);
        plot.setRenderer(1,gradient_renderer);
        updateData();                
    }
    
    public double[][] sortbyEff(ParameterSet data,EfficiencyDataSet eff) {
        int n = data.parent.numberOfRuns;
        
        double tmp_data[][] = new double[n][2];

        for (int i = 0; i < n; i++) {            
            tmp_data[i][0] = data.set[i];
            tmp_data[i][1] = eff.set[i];
        }

        Arrays.sort(tmp_data, new ArrayComparator(1, eff.isPositveEff));
        return tmp_data;
    }

    public void updateData() {                        
        XYSeries dataset_box[] = new XYSeries[BOX_COUNT];
        XYSeries dataset = new XYSeries("cumulative distribution");
        for (int i = 0; i < BOX_COUNT; i++) {
            dataset_box[i] = new XYSeries("");
        }
            
        XYBarRenderer renderer = new XYBarRenderer(0.2);
                
        double sortedData[][] = sortbyEff(this.data,this.eff);
        double threshold = sortedData.length * 0.1;
        double best[][] = new double[(int)threshold][];

        double boxes[] = new double[BOX_COUNT];

        //sort after parameter value
        for (int i = 0; i < threshold; i++) {
            best[i] = sortedData[i];
        }
        Arrays.sort(best, new ArrayComparator(0, false));

        double value = 0.0;

        double min = best[0][0];
        double max = best[best.length - 1][0];

        for (int i = 0; i < threshold; i++) {
            dataset.add(best[i][0], value);
            value += 1.0 / threshold;

            int index = (int)((best[i][0] - min) / (max - min) * (boxes.length - 1));
            boxes[index] += 1.0 / threshold;
        }

        for (int i = 0; i < BOX_COUNT; i++) {
            dataset_box[i].add((max - min) / (boxes.length - 1) * i + min, boxes[i]);
        }

        XYSeriesCollection XYBarSerie = new XYSeriesCollection();        
        for (int i = 0; i < BOX_COUNT; i++) {
            int color = (int)((1.0-boxes[i])*255.0);                     
            XYBarSerie.addSeries(dataset_box[i]);
            renderer.setSeriesPaint(i, new Color(color,color,color));                        
        }
        plot.setRenderer(0,renderer);
        plot.setDataset(0,XYBarSerie);        
        plot.setDataset(1,new XYSeriesCollection(dataset));
                                        
        if (plot.getRangeAxis() != null) plot.getRangeAxis().setAutoRange(true);        
        if (plot.getDomainAxis() != null)plot.getDomainAxis().setAutoRange(true);
        
    }

    public JPanel getPanel() {
        return chartPanel;
    }
}
