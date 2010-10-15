/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package reg.gui.MCAT5;

import java.awt.BasicStroke;
import java.awt.Color;
import java.util.ArrayList;
import java.util.Arrays;
import javax.swing.JPanel;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import reg.gui.MCAT5Toolbar.ArrayComparator;
import reg.gui.MCAT5Toolbar.EfficiencyDataSet;
import reg.gui.MCAT5Toolbar.ParameterSet;

/**
 *
 * @author Christian Fischer
 */
@SuppressWarnings({"unchecked"})
public class RegionalSensitivityAnalyser {

    XYPlot plot = new XYPlot();    
    ChartPanel chartPanel = null;    
    ParameterSet data = null;
    EfficiencyDataSet eff = null;
    double likelihood[] = null;
    
    int GROUPS = 10;
        
    public RegionalSensitivityAnalyser(ParameterSet data, EfficiencyDataSet eff) {
        this.data = data;
        this.eff = eff;

        XYLineAndShapeRenderer renderer = new XYLineAndShapeRenderer();                
        for (int i=0;i<GROUPS;i++){            
            renderer.setSeriesShapesVisible(i, false);
            renderer.setSeriesVisibleInLegend(i, false);
            if (i == 0 || i == GROUPS-1){
                renderer.setSeriesStroke(i,new BasicStroke(5));
                renderer.setSeriesVisibleInLegend(i, true);
            }
            int c = (int)(i*255.0/GROUPS);
            renderer.setSeriesPaint(i, new Color(255-c,0,c));                           
        }               
        plot.setRenderer(renderer);
        plot.setRangeAxis(new NumberAxis(java.util.ResourceBundle.getBundle("reg/resources/JADEBundle").getString("LIKELIHOOD")));
        plot.setDomainAxis(new NumberAxis(data.name));

        JFreeChart chart = new JFreeChart(plot);
        chart.setTitle(java.util.ResourceBundle.getBundle("reg/resources/JADEBundle").getString("REGIONAL_SENSITIVITY_ANALYSIS"));
        //chart.removeLegend();
        chartPanel = new ChartPanel(chart, true);

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
        
    private void updateData() {
        XYSeriesCollection series = new XYSeriesCollection();
                
        ArrayList<double[]> boxes[]  = new ArrayList[GROUPS];
        for (int i=0;i<10;i++){
            boxes[i] = new ArrayList<double[]>();                    
        }

        this.likelihood = Efficiencies.CalculateLikelihood(eff.set);        
        double sorted_data[][] = sortbyEff(data.set,likelihood);
                                            
        double min = 0;
        double max = sorted_data[0][1];             
        double range_max = Double.NEGATIVE_INFINITY;
        double range_min = Double.POSITIVE_INFINITY;
        
        //sort data into boxes
        for (int i = 0; i < data.parent.numberOfRuns; i++) {
            int index = (int) Math.round((sorted_data[i][1] - min) / (max - min) * (boxes.length - 1));
            boxes[index].add(sorted_data[i]);
            range_max = Math.max(sorted_data[i][0],range_max);
            range_min = Math.min(sorted_data[i][0],range_min);
        }

        XYSeries dataset = null;
        for (int i = 0; i < boxes.length; i++) {                       
            if (i==0)                   dataset=new XYSeries("best group");
            else if(i == boxes.length - 1)      dataset = new XYSeries("worst group");
            else dataset = new XYSeries("");

            double box_data[][] = new double[boxes[i].size()][];
            for (int j=0;j<boxes[i].size();j++){
                box_data[j] = boxes[i].get(j);
            }
            Arrays.sort(box_data,new ArrayComparator(0,false));
                         
            dataset.add(range_min,0.0);
            for (int j=0;j<box_data.length;j++){
                dataset.add(box_data[j][0],(double)j / (double)box_data.length);                
            }
            dataset.add(range_max,1.0);                        
            series.addSeries(dataset);
        }
        plot.setDataset(series);
        if (plot.getRangeAxis() != null) plot.getRangeAxis().setAutoRange(true);
        if (plot.getDomainAxis() != null)plot.getDomainAxis().setAutoRange(true);

    }

    public JPanel getPanel() {
        return chartPanel;
    }
}
