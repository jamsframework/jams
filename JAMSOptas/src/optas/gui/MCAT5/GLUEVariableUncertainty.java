/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package optas.gui.MCAT5;

import jams.JAMS;
import java.awt.BorderLayout;
import java.util.ArrayList;
import javax.swing.JOptionPane;
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
import optas.gui.MCAT5.MCAT5Plot.SimpleRequest;
import optas.hydro.data.DataSet;
import optas.hydro.data.Efficiency;
import optas.hydro.data.EfficiencyEnsemble;
import optas.hydro.data.SimpleEnsemble;
import optas.hydro.data.StateVariable;

/**
 *
 * @author Christian Fischer
 */
@SuppressWarnings({"unchecked"})
public class GLUEVariableUncertainty extends MCAT5Plot {
    XYPlot plot1 = new XYPlot();
    XYPlot plot2 = new XYPlot();
            
    ChartPanel chartPanel1 = null;
    ChartPanel chartPanel2 = null;

    final int GROUPS = 10;
    
    public GLUEVariableUncertainty() {
        this.addRequest(new SimpleRequest(JAMS.i18n("ENSEMBLE_SIMULATED_VARIABLE"), StateVariable.class));
        this.addRequest(new SimpleRequest(JAMS.i18n("Efficiency"), Efficiency.class));

        init();
    }

    private void init(){
        XYLineAndShapeRenderer renderer1 = new XYLineAndShapeRenderer();
        XYLineAndShapeRenderer renderer3 = new XYLineAndShapeRenderer();
        
        renderer1.setBaseShapesVisible(false);
        renderer3.setBaseLinesVisible(false);
        
        plot1.setRenderer(0,renderer1);   
        plot1.setRenderer(1,renderer3);
        plot2.setRenderer(new XYBarRenderer(0.0));                                
                
        JFreeChart chart1 = new JFreeChart(plot1);
        JFreeChart chart2 = new JFreeChart(plot2);
        chart1.setTitle(JAMS.i18n("CUMULATIVE_DENSITY_PLOT"));
        chart2.setTitle(JAMS.i18n("DENSITY_PLOT"));
                
        chart2.removeLegend();
        chartPanel1 = new ChartPanel(chart1, true);
        chartPanel2 = new ChartPanel(chart2, true);
                        
        try{
            refresh();
        }catch(NoDataException e1){
            JOptionPane.showMessageDialog(chartPanel1, "Failed to show dataset. The data is incommensurate!");
        }
    }
          
    public void refresh() throws NoDataException{
        if (!this.isRequestFulfilled()){
            return;

        }
        ArrayList<DataSet> p[] = getData(new int[]{0,1});
        SimpleEnsemble var = (SimpleEnsemble)p[0].get(0);
        EfficiencyEnsemble eff = (EfficiencyEnsemble)p[1].get(0);

        plot1.setDomainAxis(new NumberAxis(var.name));
        plot1.setRangeAxis(new NumberAxis(""));
        plot2.setDomainAxis(new NumberAxis(var.name));
        plot2.setRangeAxis(new NumberAxis(""));

        XYSeries dataset1 = new XYSeries(JAMS.i18n("CUMULATIVE_DENSITY"));
        XYSeries dataset2 = new XYSeries(JAMS.i18n("NO_DESCRIPTION"));
        XYSeries dataset3 = new XYSeries(JAMS.i18n("0.95_CONFIDENCE_INTERVAL"));
        
        double bin_sum[] = new double[GROUPS];
        int bin_count[] = new int[GROUPS];


        EfficiencyEnsemble likelihood = eff.CalculateLikelihood();
        Integer sortedIds[] = var.sort();
        //double sorted_data[][] = sortbyEff(likelihood,this.var.set);
        
        double sum = 0;        
        double conf = 0.05;
        int n = var.getSize();

        for (int i=0;i<n;i++){
            if (sum < conf && sum+likelihood.getValue(sortedIds[i]) > conf){
                dataset3.add(var.getValue(sortedIds[i]),sum);
            }
            if (sum < 1.0-conf && sum+likelihood.getValue(sortedIds[i]) > 1.0-conf){
                dataset3.add(var.getValue(sortedIds[i]),sum);
            }
            dataset1.add(var.getValue(sortedIds[i]),sum+=likelihood.getValue(sortedIds[i]));
        }
                
        double min = var.getValue(sortedIds[0]);
        double max = var.getValue(sortedIds[n-1]);
                       
        for (int j=0;j<n;j++){
            int index = (int)((var.getValue(sortedIds[j])-min)/(max-min)*(double)(GROUPS));
            if (index >= GROUPS)
                index = GROUPS - 1;
            bin_sum[index] += likelihood.getValue(sortedIds[j]);
            bin_count[index]++;
        }

        double norm = 0;
        for (int i=0;i<GROUPS;i++){                        
            bin_sum[i] = bin_sum[i] / (double)bin_count[i];
            norm += bin_sum[i];
        }
        for (int i=0;i<GROUPS;i++){
            bin_sum[i] = bin_sum[i] / norm;
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

    public JPanel getPanel(){
        return new JPanel(){
            {
                this.setLayout(new BorderLayout());
                this.add(chartPanel1,BorderLayout.EAST);
                this.add(chartPanel2,BorderLayout.WEST);
            }
        };
    }
    public JPanel getPanel1() {
        return chartPanel1;
    }
    public JPanel getPanel2() {
        return chartPanel2;
    }
}
