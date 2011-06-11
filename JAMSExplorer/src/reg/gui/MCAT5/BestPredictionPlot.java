/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package reg.gui.MCAT5;

import java.awt.BasicStroke;
import java.awt.Color;
import java.util.ArrayList;
import javax.swing.JPanel;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import reg.hydro.data.DataSet;
import reg.hydro.data.Efficiency;
import reg.hydro.data.EfficiencyEnsemble;
import reg.hydro.data.Measurement;
import reg.hydro.data.TimeSerie;
import reg.hydro.data.TimeSerieEnsemble;


/**
 *
 * @author Christian Fischer
 */
public class BestPredictionPlot extends MCAT5Plot{
    XYPlot plot1 = new XYPlot();            
    ChartPanel chartPanel1 = null;
    
    /*SimulationTimeSeriesDataSet sim = null;
    ObservationDataSet obs = null;
    EfficiencyDataSet eff[] = null;*/
                
    public BestPredictionPlot() {
        this.addRequest(new SimpleRequest(java.util.ResourceBundle.getBundle("reg/resources/JADEBundle").getString("SIMULATED_TIMESERIE"),TimeSerie.class));
        this.addRequest(new SimpleRequest(java.util.ResourceBundle.getBundle("reg/resources/JADEBundle").getString("OBSERVED_TIMESERIE"),Measurement.class));
        this.addRequest(new SimpleRequest(java.util.ResourceBundle.getBundle("reg/resources/JADEBundle").getString("Efficiency"),Efficiency.class,1,10));
        
        init();
    }

    private void init(){
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
        refresh();
    }

    public void refresh() {
        if (!this.isRequestFulfilled())
                return;
        
        TimeSerieEnsemble   ts = (TimeSerieEnsemble)getData(0);
        Measurement         obs = (Measurement)getData(1);
        ArrayList<DataSet>  eff = (ArrayList<DataSet>)getMultipleData(2);

        long time_length =  obs.getTimeDomain().getNumberOfTimesteps();

        XYSeries bestTSDataset[] = new XYSeries[eff.size()+1];
        
        for (int i=0;i<eff.size();i++){
            int argmin = ((EfficiencyEnsemble)eff.get(i)).findArgMin();
            int argmax = ((EfficiencyEnsemble)eff.get(i)).findArgMax();

            bestTSDataset[i] = new XYSeries("opt-"+eff.get(i).name);
            for (int j=0;j<time_length;j++){
                if (((EfficiencyEnsemble)eff.get(i)).isPositiveBest())
                    bestTSDataset[i].add(j,ts.get(j, argmax));
                else
                    bestTSDataset[i].add(j,ts.get(j, argmin));
            }
        }
        bestTSDataset[eff.size()] = new XYSeries(java.util.ResourceBundle.getBundle("reg/resources/JADEBundle").getString("OBSERVED"));
        for (int j=0;j<time_length;j++){            
            bestTSDataset[eff.size()].add(j,obs.getValue(j));
        }                      
                 
        XYSeriesCollection bestSeries = new XYSeriesCollection();
        for (int i=0;i<bestTSDataset.length;i++){
            bestSeries.addSeries(bestTSDataset[i]);
        }                                                                              
        plot1.setDataset(bestSeries);        
                                        
        if (plot1.getRangeAxis() != null)   plot1.getRangeAxis().setAutoRange(true);        
        if (plot1.getDomainAxis()!= null)   plot1.getDomainAxis().setAutoRange(true);
    }

    public JPanel getPanel() {
        return chartPanel1;
    }
}
