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
import org.jfree.chart.renderer.xy.XYDifferenceRenderer;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import reg.gui.MCAT5.MCAT5Plot.SimpleRequest;
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
public class ParetoOutputUncertainty extends MCAT5Plot {
    XYPlot plot1 = new XYPlot();            
    ChartPanel chartPanel1 = null;
            
    String var_name = null;
        
    public ParetoOutputUncertainty() {
        this.addRequest(new SimpleRequest(java.util.ResourceBundle.getBundle("reg/resources/JADEBundle").getString("SIMULATED_TIMESERIE"), TimeSerie.class));
        this.addRequest(new SimpleRequest(java.util.ResourceBundle.getBundle("reg/resources/JADEBundle").getString("Efficiency"), Efficiency.class,1,10));
        this.addRequest(new SimpleRequest(java.util.ResourceBundle.getBundle("reg/resources/JADEBundle").getString("OBSERVED_TIMESERIE"),Measurement.class));

        init();
    }

    private void init(){
        plot1.setDomainAxis(new NumberAxis(java.util.ResourceBundle.getBundle("reg/resources/JADEBundle").getString("TIME")));

        JFreeChart chart1 = new JFreeChart(plot1);
        chart1.setTitle(java.util.ResourceBundle.getBundle("reg/resources/JADEBundle").getString("PARETO_OUTPUT_UNCERTAINTY"));
        //chart1.removeLegend();
        chartPanel1 = new ChartPanel(chart1, true);
        
        XYDifferenceRenderer renderer1 = new XYDifferenceRenderer(Color.LIGHT_GRAY,Color.LIGHT_GRAY,false);
        XYDifferenceRenderer renderer2 = new XYDifferenceRenderer(Color.CYAN,Color.CYAN,false);
        XYLineAndShapeRenderer renderer3 = new XYLineAndShapeRenderer(); 
        
        renderer1.setPaint(Color.LIGHT_GRAY);        
        renderer1.setSeriesFillPaint(0, Color.LIGHT_GRAY);
        
        renderer2.setPaint(Color.CYAN);
        renderer3.setBaseLinesVisible(true);
        renderer3.setBaseShapesVisible(false);
        renderer3.setOutlinePaint(Color.BLACK);
        renderer3.setPaint(Color.BLACK);
        renderer3.setStroke(new BasicStroke(1));
                                       
        plot1.setRenderer(0, renderer3);
        plot1.setRenderer(1, renderer2);
        plot1.setRenderer(2, renderer1);
        
        refresh();
    }
        
    boolean isParetoOptimal(double eff_actual[],EfficiencyEnsemble eff_set[]){
        int MC_PARAM = eff_set[0].getSize();
        for (int i=0;i<MC_PARAM;i++){
            boolean dominated = true;
            for (int j=0;j<eff_actual.length;j++){
                if (eff_set[j].isPositiveBest()){
                    if (eff_set[j].getValue(i)<=eff_actual[j]){
                        dominated = false;
                        break;
                    }
                }else{
                    if (eff_set[j].getValue(i)>=eff_actual[j]){
                        dominated = false;
                        break;
                    }
                }
            }
            if (dominated)
                return false;
        }
        return true;
    }
    
    double[][] getMinMaxParetoTS(TimeSerieEnsemble data, EfficiencyEnsemble eff[]){
        double minMaxOptimalTS[][] = new double[2][data.getTimesteps()];
                
        for (int i=0;i<data.getTimesteps();i++){
            minMaxOptimalTS[0][i] = Double.POSITIVE_INFINITY;
            minMaxOptimalTS[1][i] = Double.NEGATIVE_INFINITY;
        }
        
        for (int i=0;i<data.getSize();i++){
            double actualEffSet[] = new double[eff.length];
            for (int j=0;j<eff.length;j++)
                actualEffSet[j] = eff[j].getValue(new Integer(i));
            if (isParetoOptimal(actualEffSet,eff)){
                for (int t=0;t<data.getTimesteps();t++){
                    minMaxOptimalTS[0][t] = Math.min(minMaxOptimalTS[0][t],data.get(t, i) );
                    minMaxOptimalTS[1][t] = Math.max(minMaxOptimalTS[1][t],data.get(t, i) );
                }
            }
        }
        return minMaxOptimalTS;
    }
    
    public void refresh() {
        if (!this.isRequestFulfilled())
            return;

        TimeSerieEnsemble ts   = (TimeSerieEnsemble)getData(0);
        ArrayList<DataSet>  dataInEff     = (ArrayList<DataSet>)getMultipleData(1);
        Measurement obs = (Measurement) getData(2);

        EfficiencyEnsemble eff[] = new EfficiencyEnsemble[dataInEff.size()];
        for (int i=0;i<eff.length;i++)
            eff[i] = (EfficiencyEnsemble)dataInEff.get(i);

        plot1.setRangeAxis(new NumberAxis(ts.name));

        int time_length = ts.getTimesteps();
        
        TimeSerie maxTS = ts.getMax();
        TimeSerie minTS = ts.getMin();

        double minMaxOptimalTS[][] = getMinMaxParetoTS(ts,eff);
                        
        XYSeries minTSDataset = new XYSeries(java.util.ResourceBundle.getBundle("reg/resources/JADEBundle").getString("MINIMAL_VALUE_IN_DATASET"));
        XYSeries maxTSDataset = new XYSeries(java.util.ResourceBundle.getBundle("reg/resources/JADEBundle").getString("MAXIMAL_VALUE_IN_DATASET"));
        
        XYSeries minTSDataset_pareto = new XYSeries(java.util.ResourceBundle.getBundle("reg/resources/JADEBundle").getString("MINIMAL_VALUE_IN_PARETO_SET"));
        XYSeries maxTSDataset_pareto = new XYSeries(java.util.ResourceBundle.getBundle("reg/resources/JADEBundle").getString("MAXIMAL_VALUE_IN_PARETO_SET"));
                
        XYSeries observation = new XYSeries(obs.name);
    
        for (int i=0;i<time_length;i++){
            minTSDataset.add(i,minTS.getValue(i));
            maxTSDataset.add(i,maxTS.getValue(i));
            
            minTSDataset_pareto.add(i,minMaxOptimalTS[0][i]);
            maxTSDataset_pareto.add(i,minMaxOptimalTS[1][i]);
            
            observation.add(i,obs.getValue(i));
        }
        
        XYSeriesCollection dataInterval = new XYSeriesCollection();
        XYSeriesCollection dataInterval_pareto = new XYSeriesCollection();
        
        dataInterval.addSeries(minTSDataset);
        dataInterval.addSeries(maxTSDataset);
                
        dataInterval_pareto.addSeries(minTSDataset_pareto);
        dataInterval_pareto.addSeries(maxTSDataset_pareto);
                                        
        plot1.setDataset(2, dataInterval);
        plot1.setDataset(1, dataInterval_pareto);
        plot1.setDataset(0, new XYSeriesCollection(observation));
        
        if (plot1.getRangeAxis() != null)   plot1.getRangeAxis().setAutoRange(true);
        if (plot1.getDomainAxis() != null)  plot1.getDomainAxis().setAutoRange(true);
    }

    public JPanel getPanel() {
        return chartPanel1;
    }
}
