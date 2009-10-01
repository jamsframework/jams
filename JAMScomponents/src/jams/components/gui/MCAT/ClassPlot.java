/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package jams.components.gui.MCAT;

import jams.components.gui.MCAT.MCAT5.ArrayComparator;
import jams.components.gui.MCAT.MCAT5.EfficiencyDataSet;
import jams.components.gui.MCAT.MCAT5.SimulationTimeSeriesDataSet;
import java.awt.Color;
import java.util.Arrays;
import javax.swing.JPanel;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

/**
 *
 * @author Christian Fischer
 */
@SuppressWarnings({"unchecked"})
public class ClassPlot {

    XYPlot plot = new XYPlot();    
    ChartPanel chartPanel = null;    
    SimulationTimeSeriesDataSet data = null;
    EfficiencyDataSet eff = null;
    double likelihood[][] = null;

    final int GROUPS = 10;
        
    public ClassPlot(SimulationTimeSeriesDataSet data, EfficiencyDataSet eff) {
        this.data = data;
        this.eff = eff;
        
        XYLineAndShapeRenderer renderer = new XYLineAndShapeRenderer();
        for (int i=0;i<GROUPS;i++){            
            renderer.setSeriesShapesVisible(i, false);            
            int c = (int)(i*255.0/GROUPS);
            renderer.setSeriesPaint(i, new Color(255-c,0,c));                           
            renderer.setSeriesVisibleInLegend(i, false);
        }               
        renderer.setSeriesVisibleInLegend(0, true);
        renderer.setSeriesVisibleInLegend(GROUPS-1, true);
        plot.setRenderer(renderer);
        plot.setDomainAxis(new NumberAxis("Time"));
        plot.setRangeAxis(new NumberAxis("Output"));

        JFreeChart chart = new JFreeChart(plot);
        chart.setTitle("Class Plot");
        chartPanel = new ChartPanel(chart, true);

        updateData();
    }
        
    public double[][] sortbyEff(SimulationTimeSeriesDataSet data,double likelihood[]) {
        int n = data.parent.numberOfRuns;
        int m = data.timeLength;
        double tmp_data[][] = new double[n][m+1];

        for (int i = 0; i < n; i++) {
            for (int j=0;j<m;j++)
                tmp_data[i][j] = data.set[j].set[i];
            tmp_data[i][m] = likelihood[i];
        }

        Arrays.sort(tmp_data, new ArrayComparator(1, true));
        return tmp_data;
    }
        
    public void updateData() {                     
        double sorted_data[][] = null;
        sorted_data = sortbyEff(data,Efficiencies.CalculateLikelihood(eff.set));
        XYSeriesCollection series = new XYSeriesCollection();
        for (int i=0;i<GROUPS;i++){   
            XYSeries dataset = new XYSeries("");
            if (i == 0)
                dataset = new XYSeries("High Likelihood");
            if (i == GROUPS-1)
                dataset = new XYSeries("Low Likelihood");
            int index = (int)((sorted_data.length / (double)GROUPS)*i);
            for (int j=0;j<sorted_data[0].length;j++){
                dataset.add(j,sorted_data[index][j]);                
            }                 
            series.addSeries(dataset);
        }                                                                                                                     
        
        plot.setDataset(series);            
                
        if (plot.getRangeAxis() != null)  plot.getRangeAxis().setAutoRange(true);        
        if (plot.getDomainAxis() != null) plot.getDomainAxis().setAutoRange(true);
    }

    public JPanel getPanel() {
        return chartPanel;
    }
}
