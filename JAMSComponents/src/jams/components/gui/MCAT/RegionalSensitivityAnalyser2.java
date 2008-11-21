/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package jams.components.gui.MCAT;

import jams.components.gui.MCAT.MCAT5.ArrayComparator;
import jams.components.gui.MCAT.MCAT5.EfficiencyDataSet;
import jams.components.gui.MCAT.MCAT5.ParameterSet;
import java.awt.Color;
import java.util.ArrayList;
import java.util.Arrays;
import javax.swing.JPanel;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.LegendItemSource;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

/**
 *
 * @author Christian Fischer
 */
@SuppressWarnings({"unchecked"})
public class RegionalSensitivityAnalyser2 {

    XYPlot plot = new XYPlot();    
    ChartPanel chartPanel = null;    
    ParameterSet data = null;
    EfficiencyDataSet eff[] = null;
    double likelihood[][] = null;
    
    final int GROUPS = 10;
      
    public RegionalSensitivityAnalyser2(ParameterSet data, EfficiencyDataSet eff[]) {
        this.data = data;
        this.eff = eff;

        XYLineAndShapeRenderer renderer[] = new XYLineAndShapeRenderer[GROUPS];
        for (int i=0;i<eff.length;i++){
            renderer[i] = new XYLineAndShapeRenderer();
            renderer[i].setBaseShapesVisible(false);
            int c = (int)(i*255.0/eff.length);
            renderer[i].setSeriesPaint(0, new Color(255-c,0,c));
               
            plot.setRenderer(i, renderer[i]);
        }               
        plot.setDomainAxis(new NumberAxis(data.name));
        plot.setRangeAxis(new NumberAxis("Likelihood"));

        JFreeChart chart = new JFreeChart(plot);
        chart.setTitle("Regional Sensitivity Analysis II");
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
        
    public void updateData() {        
        int numberOfObjFct = eff.length;
        XYSeries dataset[] = new XYSeries[numberOfObjFct];
        double sorted_data[][][] = new double[numberOfObjFct][][];
                                        
        for (int i=0;i<numberOfObjFct;i++){
            dataset[i] = new XYSeries(eff[i].name);
            sorted_data[i] = sortbyEff(data.set,Efficiencies.CalculateLikelihood(this.eff[i].set));
            ArrayList<double[]> boxes[] = new ArrayList[numberOfObjFct];
            
            for (int j=0;j<boxes.length;j++)
                boxes[j] = new ArrayList<double[]>();
            
            double min = 0;
            double max = sorted_data[i][0][1];
            double range_max = Double.NEGATIVE_INFINITY;
            double range_min = Double.POSITIVE_INFINITY;
            
            int index = 0,counter=0;
            do{
                index = (int) Math.round((sorted_data[i][counter][1] - min) / (max - min) * 9);
                boxes[i].add(sorted_data[i][counter]);
                range_max = Math.max(sorted_data[i][counter][0],range_max);
                range_min = Math.min(sorted_data[i][counter][0],range_min);
                counter++;
            }while (index == GROUPS-1);
                        
            double box_data[][] = new double[boxes[i].size()][];
            for (int j=0;j<boxes[i].size();j++){
                box_data[j] = boxes[i].get(j);
            }
            Arrays.sort(box_data,new ArrayComparator(0,false));
                         
            dataset[i].add(range_min,0.0);
            for (int j=0;j<box_data.length;j++){
                dataset[i].add(box_data[j][0],(double)j / (double)box_data.length);                
            }
            dataset[i].add(range_max,1.0);
            
            plot.setDataset(i, new XYSeriesCollection(dataset[i]));            
        }                                                                                                                     
        
        if (plot.getRangeAxis() != null)    plot.getRangeAxis().setAutoRange(true);
        if (plot.getDomainAxis() != null)   plot.getDomainAxis().setAutoRange(true);

    }

    public JPanel getPanel() {
        return chartPanel;
    }
}
