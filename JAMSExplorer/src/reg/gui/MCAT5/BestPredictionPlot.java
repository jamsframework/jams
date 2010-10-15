/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package reg.gui.MCAT5;

import java.awt.BasicStroke;
import java.awt.Color;
import javax.swing.JPanel;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import reg.gui.MCAT5Toolbar.EfficiencyDataSet;
import reg.gui.MCAT5Toolbar.ObservationDataSet;
import reg.gui.MCAT5Toolbar.SimulationTimeSeriesDataSet;

/**
 *
 * @author Christian Fischer
 */
public class BestPredictionPlot {   
    XYPlot plot1 = new XYPlot();            
    ChartPanel chartPanel1 = null;
    
    SimulationTimeSeriesDataSet sim = null;    
    ObservationDataSet obs = null;
    EfficiencyDataSet eff[] = null;
                
    public BestPredictionPlot(SimulationTimeSeriesDataSet sim, ObservationDataSet obs, EfficiencyDataSet eff[]) {
        this.sim = sim;        
        this.eff = eff;
        this.obs = obs;
                
        plot1.setDomainAxis(new NumberAxis(java.util.ResourceBundle.getBundle("reg/resources/JADEBundle").getString("TIME")));
        plot1.setRangeAxis(new NumberAxis(java.util.ResourceBundle.getBundle("reg/resources/JADEBundle").getString("OUTPUT")));

        JFreeChart chart1 = new JFreeChart(plot1);
        chart1.setTitle(java.util.ResourceBundle.getBundle("reg/resources/JADEBundle").getString("BEST_PREDICTION_PLOT"));
        chartPanel1 = new ChartPanel(chart1, true);
                        
        XYLineAndShapeRenderer renderer = new XYLineAndShapeRenderer(); 
                
        for (int i=0;i<7;i++){
            renderer.setSeriesLinesVisible(i, true);
            renderer.setSeriesShapesVisible(i, false);        
        }
                
        renderer.setSeriesPaint(0,Color.BLACK);
        renderer.setSeriesPaint(1,Color.BLUE);
        renderer.setSeriesPaint(2,Color.RED);
        renderer.setSeriesPaint(3,Color.YELLOW);
        renderer.setSeriesPaint(4,Color.CYAN);
        renderer.setSeriesPaint(5,Color.GREEN);
        renderer.setSeriesPaint(6,Color.PINK);
        
        renderer.setStroke(new BasicStroke(1));
        plot1.setRenderer(renderer);
        updateData();
    }
            
    public void updateData() {
        int time_length = this.obs.timeLength;
                        
        XYSeries bestTSDataset[] = new XYSeries[eff.length+1];
        
        for (int i=0;i<eff.length;i++){
            double max = Double.NEGATIVE_INFINITY;
            double min = Double.POSITIVE_INFINITY;
            int argmax=0,argmin=0;
            
            for (int j=0;j<eff[i].parent.numberOfRuns;j++){
                if (max < eff[i].set[j]){
                    max = eff[i].set[j];
                    argmax = j;
                }
                if (min > eff[i].set[j]){
                    min = eff[i].set[j];
                    argmin = j;
                }                
            }  
            bestTSDataset[i] = new XYSeries("opt-"+eff[i].name);
            for (int j=0;j<time_length;j++){
                if (eff[i].isPositveEff)
                    bestTSDataset[i].add(j,this.sim.set[j].set[argmax]);
                else
                    bestTSDataset[i].add(j,this.sim.set[j].set[argmin]);
            }
        }
        bestTSDataset[eff.length] = new XYSeries(java.util.ResourceBundle.getBundle("reg/resources/JADEBundle").getString("OBSERVED"));
        for (int j=0;j<time_length;j++){            
            bestTSDataset[eff.length].add(j,obs.set[j]);
        }                      
                 
        XYSeriesCollection bestSeries = new XYSeriesCollection();
        for (int i=0;i<bestTSDataset.length;i++){
            bestSeries.addSeries(bestTSDataset[i]);
        }                                                                              
        plot1.setDataset(bestSeries);        
                                        
        if (plot1.getRangeAxis() != null)   plot1.getRangeAxis().setAutoRange(true);        
        if (plot1.getDomainAxis()!= null)   plot1.getDomainAxis().setAutoRange(true);
    }

    public JPanel getPanel1() {
        return chartPanel1;
    }
}
