/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package reg.gui.MCAT5;

import java.util.Arrays;
import javax.swing.JPanel;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYBarRenderer;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.xy.XYBarDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import reg.gui.MCAT5Toolbar.ArrayComparator;
import reg.gui.MCAT5Toolbar.EfficiencyDataSet;
import reg.gui.MCAT5Toolbar.SimulationDataSet;

/**
 *
 * @author Christian Fischer
 */
@SuppressWarnings({"unchecked"})
public class GLUEVariableUncertainty {   
    XYPlot plot1 = new XYPlot();
    XYPlot plot2 = new XYPlot();
            
    ChartPanel chartPanel1 = null;
    ChartPanel chartPanel2 = null;
    
    SimulationDataSet var = null;
    EfficiencyDataSet eff = null;
            
    final int GROUPS = 10;
    
    public GLUEVariableUncertainty(SimulationDataSet var, EfficiencyDataSet eff) {
        this.var = var;
        this.eff = eff;
        
        XYLineAndShapeRenderer renderer1 = new XYLineAndShapeRenderer();
        XYLineAndShapeRenderer renderer3 = new XYLineAndShapeRenderer();
        
        renderer1.setBaseShapesVisible(false);
        renderer3.setBaseLinesVisible(false);
        
        plot1.setRenderer(0,renderer1);   
        plot1.setRenderer(1,renderer3);
        plot2.setRenderer(new XYBarRenderer(0.0));                                
        
        plot1.setDomainAxis(new NumberAxis(var.name));
        plot1.setRangeAxis(new NumberAxis(""));
        plot2.setDomainAxis(new NumberAxis(var.name));
        plot2.setRangeAxis(new NumberAxis(""));

        JFreeChart chart1 = new JFreeChart(plot1);
        JFreeChart chart2 = new JFreeChart(plot2);
        chart1.setTitle("Cumulative Density Plot");
        chart2.setTitle("Density Plot");
                
        chart2.removeLegend();
        chartPanel1 = new ChartPanel(chart1, true);
        chartPanel2 = new ChartPanel(chart2, true);
                        
        updateData();
    }
                
    public double[][] sortbyEff(double data[],double likelihood[]) {
        int n = data.length;        
        double tmp_data[][] = new double[n][2];

        for (int i = 0; i < n; i++) {            
            tmp_data[i][0] = data[i];
            tmp_data[i][1] = likelihood[i];
        }

        Arrays.sort(tmp_data, new ArrayComparator(1, true));
        return tmp_data;
    }
    
    public void updateData() {                        
        XYSeries dataset1 = new XYSeries("Cumulative Density");
        XYSeries dataset2 = new XYSeries("no description");
        XYSeries dataset3 = new XYSeries("0.95 confidence interval");
        
        double bin_sum[] = new double[GROUPS];
        int bin_count[] = new int[GROUPS];
        double likelihood[] = Efficiencies.CalculateLikelihood(this.eff.set);        
        double sorted_data[][] = sortbyEff(likelihood,this.var.set);
        
        double sum = 0;        
        double conf = 0.05;
        
        for (int i=0;i<sorted_data.length;i++){
            if (sum < conf && sum+sorted_data[sorted_data.length-i-1][0] > conf){
                dataset3.add(sorted_data[sorted_data.length-i-1][1],sum);                
            }
            if (sum < 1.0-conf && sum+sorted_data[sorted_data.length-i-1][0] > 1.0-conf){
                dataset3.add(sorted_data[sorted_data.length-i-1][1],sum);                
            }
            dataset1.add(sorted_data[sorted_data.length-i-1][1],sum+=sorted_data[sorted_data.length-i-1][0]);
        }
                
        double min = sorted_data[0][1];
        double max = sorted_data[sorted_data.length-1][1];
                       
        for (int j=0;j<sorted_data.length;j++){            
            int index = (int)((sorted_data[j][1]-min)/(max-min)*(double)(GROUPS-1));            
            bin_sum[index] += sorted_data[j][0];            
            bin_count[index]++;
        }
        
        for (int i=0;i<GROUPS;i++){                        
            bin_sum[i] = bin_sum[i] / (double)bin_count[i];
            dataset2.add((i/(double)GROUPS)*(max-min)+min,bin_sum[i]);
        }
               
        plot1.setDataset(0, new XYBarDataset(new XYSeriesCollection(dataset1), 0.9*(max-min)/GROUPS));
        plot1.setDataset(1, new XYBarDataset(new XYSeriesCollection(dataset3), 0.9*(max-min)/GROUPS));
        plot2.setDataset(0, new XYBarDataset(new XYSeriesCollection(dataset2), 0.9*(max-min)/GROUPS));
                                
        if (plot1.getRangeAxis() != null) plot1.getRangeAxis().setAutoRange(true);
        if (plot1.getDomainAxis() != null)plot1.getDomainAxis().setAutoRange(true);
        
        if (plot2.getRangeAxis() != null) plot2.getRangeAxis().setAutoRange(true);
        if (plot2.getDomainAxis() != null)plot2.getDomainAxis().setAutoRange(true);
    }

    public JPanel getPanel1() {
        return chartPanel1;
    }
    public JPanel getPanel2() {
        return chartPanel2;
    }
}
