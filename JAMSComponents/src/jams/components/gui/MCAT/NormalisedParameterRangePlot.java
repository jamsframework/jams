/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package jams.components.gui.MCAT;

import jams.components.gui.MCAT.MCAT5.EfficiencyDataSet;
import jams.components.gui.MCAT.MCAT5.ParameterSet;
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
public class NormalisedParameterRangePlot {   
    XYPlot plot1 = new XYPlot();            
    ChartPanel chartPanel1 = null;
        
    EfficiencyDataSet eff[] = null;
    ParameterSet param[] = null;
            
    public NormalisedParameterRangePlot(ParameterSet param[], EfficiencyDataSet eff[]) {        
        this.eff = eff;
        this.param = param;
        
        XYDifferenceRenderer renderer1 = new XYDifferenceRenderer(Color.LIGHT_GRAY,Color.LIGHT_GRAY,false);                
        renderer1.setPaint(Color.BLACK);                                                               
        plot1.setRenderer(0, renderer1);
        
        plot1.setDomainAxis(new NumberAxis("Parameter"));
        plot1.setRangeAxis(new NumberAxis("Normalised Range"));

        JFreeChart chart1 = new JFreeChart(plot1);
        chart1.setTitle("Pareto Output Uncertainty");        
        chart1.removeLegend();
        chartPanel1 = new ChartPanel(chart1, true);
                        
        updateData();
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
        
    double[][] getMinMaxParetoTS(ParameterSet[]data, EfficiencyDataSet eff[]){
        double minMaxOptimalTS[][] = new double[2][data.length];
                
        for (int i=0;i<data.length;i++){
            minMaxOptimalTS[0][i] = Double.POSITIVE_INFINITY;
            minMaxOptimalTS[1][i] = Double.NEGATIVE_INFINITY;
        }
        
        for (int i=0;i<data[0].parent.numberOfRuns;i++){
            double actualEffSet[] = new double[eff.length];
            for (int j=0;j<eff.length;j++)
                actualEffSet[j] = eff[j].set[i];
            if (isParetoOptimal(actualEffSet,eff)){
                for (int t=0;t<data.length;t++){
                    minMaxOptimalTS[0][t] = Math.min(minMaxOptimalTS[0][t],data[t].set[i] );
                    minMaxOptimalTS[1][t] = Math.max(minMaxOptimalTS[1][t],data[t].set[i] );
                }
            }
        }
        return minMaxOptimalTS;
    }
    
    public void updateData() {
        double minMaxparetoOptimal[][] = getMinMaxParetoTS(this.param,this.eff);
                                                                 
        XYSeries minTSDataset_pareto = new XYSeries("minimal pareto optimal value");
        XYSeries maxTSDataset_pareto = new XYSeries("maximal pareto optimal value");
                            
        for (int i=0;i<this.param.length;i++){            
            minTSDataset_pareto.add(i,minMaxparetoOptimal[0][i]);
            maxTSDataset_pareto.add(i,minMaxparetoOptimal[1][i]);            
        }
                        
        XYSeriesCollection dataInterval_pareto = new XYSeriesCollection();
        dataInterval_pareto.addSeries(minTSDataset_pareto);
        dataInterval_pareto.addSeries(maxTSDataset_pareto);
                        
        plot1.setDataset(0, dataInterval_pareto);

        if (plot1.getRangeAxis() != null) plot1.getRangeAxis().setAutoRange(true);
        if (plot1.getDomainAxis() != null)plot1.getDomainAxis().setAutoRange(true);

    }

    public JPanel getPanel1() {
        return chartPanel1;
    }
}
