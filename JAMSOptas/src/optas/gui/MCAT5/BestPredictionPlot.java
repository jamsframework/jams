/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package optas.gui.MCAT5;

import java.awt.BasicStroke;
import java.awt.Color;
import java.util.ArrayList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import optas.hydro.data.DataSet;
import optas.hydro.data.Efficiency;
import optas.hydro.data.EfficiencyEnsemble;
import optas.hydro.data.Measurement;
import optas.hydro.data.TimeSerie;
import optas.hydro.data.TimeSerieEnsemble;


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
        try{
            refresh();
        }catch(NoDataException e){
            JOptionPane.showMessageDialog(chartPanel1, "Failed to show dataset. The data is incommensurate!");
        }
    }

    public void refresh() throws NoDataException {
        if (!this.isRequestFulfilled())
            return;
        
        ArrayList<DataSet> p[] = getData(new int[]{0,1,2});
        TimeSerieEnsemble ts = (TimeSerieEnsemble)p[0].get(0);
        Measurement obs = (Measurement)p[1].get(0);
        ArrayList<DataSet> eff = p[2];

        long time_length =  obs.getTimeDomain().getNumberOfTimesteps();

        XYSeries bestTSDataset[] = new XYSeries[eff.size()+1];
        
        for (int i=0;i<eff.size();i++){
            EfficiencyEnsemble effEnsemble = (EfficiencyEnsemble)eff.get(i);
            int argmin = effEnsemble.findArgMin();
            int argmax = effEnsemble.findArgMax();

            bestTSDataset[i] = new XYSeries("opt-"+eff.get(i).name);
            for (int j=0;j<time_length;j++){
                if (effEnsemble.isPositiveBest())
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
