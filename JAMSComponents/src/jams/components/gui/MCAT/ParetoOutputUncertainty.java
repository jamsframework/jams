/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package jams.components.gui.MCAT;

import jams.components.gui.MCAT.MCAT5.EfficiencyDataSet;
import jams.components.gui.MCAT.MCAT5.ObservationDataSet;
import jams.components.gui.MCAT.MCAT5.SimulationTimeSeriesDataSet;
import java.awt.BasicStroke;
import java.awt.Color;
import java.util.Vector;
import javax.swing.JPanel;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYDifferenceRenderer;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

/**
 *
 * @author Christian Fischer
 */
public class ParetoOutputUncertainty {   
    XYPlot plot1 = new XYPlot();            
    ChartPanel chartPanel1 = null;
    
    SimulationTimeSeriesDataSet sim = null;
    ObservationDataSet obs = null;
    EfficiencyDataSet eff[] = null;
    
    String var_name = null;
        
    public ParetoOutputUncertainty(SimulationTimeSeriesDataSet sim, ObservationDataSet obs, EfficiencyDataSet eff[]) {
        this.sim = sim;
        this.obs = obs;
        this.eff = eff;
        
        plot1.setDomainAxis(new NumberAxis(var_name));
        plot1.setRangeAxis(new NumberAxis(""));

        JFreeChart chart1 = new JFreeChart(plot1);
        chart1.setTitle("Pareto Output Uncertainty");        
        chart1.removeLegend();
        chartPanel1 = new ChartPanel(chart1, true);
        
        XYDifferenceRenderer renderer1 = new XYDifferenceRenderer(Color.LIGHT_GRAY,Color.LIGHT_GRAY,false);
        XYDifferenceRenderer renderer2 = new XYDifferenceRenderer(Color.CYAN,Color.CYAN,false);
        XYLineAndShapeRenderer renderer3 = new XYLineAndShapeRenderer(); 
        
        renderer1.setPaint(Color.BLACK);        
        renderer2.setPaint(Color.BLACK);
        renderer3.setBaseLinesVisible(true);
        renderer3.setBaseShapesVisible(false);
        renderer3.setOutlinePaint(Color.BLACK);
        renderer3.setPaint(Color.BLACK);
        renderer3.setStroke(new BasicStroke(1));
                                       
        plot1.setRenderer(0, renderer1);
        plot1.setRenderer(1, renderer2);
        plot1.setRenderer(2, renderer3);
        
        updateData();
    }
    
    double[] getMaxValues(SimulationTimeSeriesDataSet data){
        double[] max = new double[data.timeLength];
        
        for (int t=0;t<data.timeLength;t++){        
            max[t] = Double.NEGATIVE_INFINITY;
            for (int mc=0;mc<data.parent.numberOfRuns;mc++){
                max[t] = Math.max(data.set[t].set[mc],max[t]);
            }
        }
        return max;
    }
    
    double[] getMinValues(SimulationTimeSeriesDataSet data){
        double[] min = new double[data.timeLength];
        
        for (int t=0;t<data.timeLength;t++){        
            min[t] = Double.POSITIVE_INFINITY;
            for (int mc=0;mc<data.parent.numberOfRuns;mc++){
                min[t] = Math.min(data.set[t].set[mc],min[t]);
            }
        }
        return min;
    }
    
    boolean isParetoOptimal(double eff_actual[],EfficiencyDataSet eff_set[]){
        int MC_PARAM = eff_set[0].set.length;
        for (int i=0;i<MC_PARAM;i++){
            boolean dominated = true;
            for (int j=0;j<eff_actual.length;j++){
                if (eff_set[j].set[i]<=eff_actual[j]){
                    dominated = false;
                    break;
                }                    
            }
            if (dominated)
                return false;
        }
        return true;
    }
    
    double[][] getMinMaxParetoTS(SimulationTimeSeriesDataSet data, EfficiencyDataSet eff[]){
        double minMaxOptimalTS[][] = new double[2][data.timeLength];
                
        for (int i=0;i<data.timeLength;i++){
            minMaxOptimalTS[0][i] = Double.POSITIVE_INFINITY;
            minMaxOptimalTS[1][i] = Double.NEGATIVE_INFINITY;
        }
        
        for (int i=0;i<data.parent.numberOfRuns;i++){
            double actualEffSet[] = new double[eff.length];
            for (int j=0;j<eff.length;j++)
                actualEffSet[j] = eff[j].set[i];
            if (isParetoOptimal(actualEffSet,eff)){
                for (int t=0;t<data.timeLength;t++){
                    minMaxOptimalTS[0][t] = Math.min(minMaxOptimalTS[0][t],data.set[t].set[i] );
                    minMaxOptimalTS[1][t] = Math.max(minMaxOptimalTS[1][t],data.set[t].set[i] );
                }
            }
        }
        return minMaxOptimalTS;
    }
    
    public void updateData() {
        int time_length = this.obs.timeLength;
        
        double maxTS[] = getMaxValues(sim);
        double minTS[] = getMinValues(sim);
        
        double minMaxOptimalTS[][] = getMinMaxParetoTS(sim,eff);
                        
        XYSeries minTSDataset = new XYSeries("Minimal value in dataset");
        XYSeries maxTSDataset = new XYSeries("Maximal value in dataset");
        
        XYSeries minTSDataset_pareto = new XYSeries("Minimal value in pareto set");
        XYSeries maxTSDataset_pareto = new XYSeries("Maximal value in pareto set");
                
        XYSeries observation = new XYSeries(obs.name);
    
        for (int i=0;i<time_length;i++){
            minTSDataset.add(i,minTS[i]);
            maxTSDataset.add(i,maxTS[i]);
            
            minTSDataset_pareto.add(i,minMaxOptimalTS[0][i]);
            maxTSDataset_pareto.add(i,minMaxOptimalTS[1][i]);
            
            observation.add(i,obs.set[i]);
        }
        
        XYSeriesCollection dataInterval = new XYSeriesCollection();
        XYSeriesCollection dataInterval_pareto = new XYSeriesCollection();
        
        dataInterval.addSeries(minTSDataset);
        dataInterval.addSeries(maxTSDataset);
                
        dataInterval_pareto.addSeries(minTSDataset_pareto);
        dataInterval_pareto.addSeries(maxTSDataset_pareto);
                                        
        plot1.setDataset(0, dataInterval);
        plot1.setDataset(1, dataInterval_pareto);
        plot1.setDataset(2, new XYSeriesCollection(observation));
        
        if (plot1.getRangeAxis() != null)   plot1.getRangeAxis().setAutoRange(true);
        if (plot1.getDomainAxis() != null)  plot1.getDomainAxis().setAutoRange(true);
    }

    public JPanel getPanel1() {
        return chartPanel1;
    }
}
