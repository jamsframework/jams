/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package optas.gui.MCAT5;

import jams.JAMS;
import java.awt.Color;
import java.util.ArrayList;
import javax.swing.GroupLayout;
import javax.swing.JPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYBarRenderer;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.xy.XYBarDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import optas.gui.MCAT5.MCAT5Plot.SimpleRequest;
import optas.data.DataSet;
import optas.data.Efficiency;
import optas.data.EfficiencyEnsemble;
import optas.data.SimpleEnsemble;
import optas.data.StateVariable;
import optas.tools.PatchedChartPanel;
import org.jfree.chart.renderer.xy.StandardXYBarPainter;

/**
 *
 * @author Christian Fischer
 */
@SuppressWarnings({"unchecked"})
public class GLUEVariableUncertainty extends MCAT5Plot {
    XYPlot plot1 = new XYPlot();
    XYPlot plot2 = new XYPlot();
            
    PatchedChartPanel chartPanel1 = null;
    PatchedChartPanel chartPanel2 = null;

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
        XYBarRenderer renderer = new XYBarRenderer(0.00);
        renderer.setShadowVisible(false);
        renderer.setBarPainter(new StandardXYBarPainter());
        plot2.setRenderer(renderer);  
        
        JFreeChart chart1 = new JFreeChart(plot1);
        JFreeChart chart2 = new JFreeChart(plot2);
        chart1.setTitle(JAMS.i18n("CUMULATIVE_DENSITY_PLOT"));
        chart2.setTitle(JAMS.i18n("DENSITY_PLOT"));
                
        chart2.removeLegend();
        chartPanel1 = new PatchedChartPanel(chart1, true);
        chartPanel2 = new PatchedChartPanel(chart2, true);
               
        chartPanel1.setMinimumDrawWidth( 0 );
        chartPanel1.setMinimumDrawHeight( 0 );
        chartPanel1.setMaximumDrawWidth( MAXIMUM_WIDTH );
        chartPanel1.setMaximumDrawHeight( MAXIMUM_HEIGHT );
 
        chartPanel2.setMinimumDrawWidth( 0 );
        chartPanel2.setMinimumDrawHeight( 0 );
        chartPanel2.setMaximumDrawWidth( MAXIMUM_WIDTH );
        chartPanel2.setMaximumDrawHeight( MAXIMUM_HEIGHT );
        
        chart1.getPlot().setBackgroundPaint(Color.white);
        chart1.getXYPlot().setDomainGridlinePaint(Color.black);
        chart2.getPlot().setBackgroundPaint(Color.white);
        chart2.getXYPlot().setDomainGridlinePaint(Color.black);
        redraw();
    }
          
    @Override
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

        
        double sum = 0;        
        double conf = 0.05;
        int n = var.getSize();
        double lastValue = -100000000000.0, newvalue;
        for (int i=0;i<n;i++){
            if (sum < conf && sum+likelihood.getValue(sortedIds[i]) > conf){
                dataset3.add(var.getValue(sortedIds[i]),sum);
            }
            if (sum < 1.0-conf && sum+likelihood.getValue(sortedIds[i]) > 1.0-conf){
                dataset3.add(var.getValue(sortedIds[i]),sum);
            }
            newvalue = var.getValue(sortedIds[i]);
            if (lastValue == newvalue){
                newvalue += 0.00000001;
            }
            lastValue = newvalue;
            dataset1.add(newvalue,sum+=likelihood.getValue(sortedIds[i]));            
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
                GroupLayout gl = new GroupLayout(this);                
                this.setLayout(gl);
                gl.setHorizontalGroup(gl.createSequentialGroup()
                        .addComponent(chartPanel1)
                        .addComponent(chartPanel2));
                
                gl.setVerticalGroup(gl.createParallelGroup()
                        .addComponent(chartPanel1)
                        .addComponent(chartPanel2));
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
