/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package reg.gui.MCAT5;

import java.awt.Color;
import javax.swing.JPanel;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYBarRenderer;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import org.jfree.data.xy.XYBarDataset;
import reg.gui.MCAT5Toolbar.EfficiencyDataSet;
import reg.gui.MCAT5Toolbar.ParameterSet;

/**
 *
 * @author Christian Fischer
 */
public class APosterioriPlot{                        
        XYPlot plot = new XYPlot();        
        ChartPanel chartPanel = null;
                
        ParameterSet data;
        EfficiencyDataSet eff;
                       
        final int BOX_COUNT = 20;
                    
        public APosterioriPlot(ParameterSet data, EfficiencyDataSet eff){                  
            this.data = data;
            this.eff = eff;
            
            //renderer.set            
            XYBarRenderer renderer = new XYBarRenderer(0.33 / (double)BOX_COUNT);                        
            renderer.setSeriesPaint(0, Color.DARK_GRAY);            
            plot.setRenderer(0, renderer);                                                              
                        
            plot.setDomainAxis(new NumberAxis(data.name));
            plot.setRangeAxis(new NumberAxis(eff.name));
                                
            JFreeChart chart = new JFreeChart(plot);
            chart.setTitle(java.util.ResourceBundle.getBundle("reg/resources/JADEBundle").getString("A_POSTERIO_PARAMETER_DISTRIBUTION"));
            chartPanel = new ChartPanel(chart, true);
                                              
            updateData();
        }
        
        public void updateData(){ 
            XYSeries dataset = new XYSeries(java.util.ResourceBundle.getBundle("reg/resources/JADEBundle").getString("MEAN_OF_EFFICIENCY"));
            
            double boxes[] = new double[BOX_COUNT];
            double boxes_count[] = new double[BOX_COUNT];
            
            double min = MCAT5Tools.FindMinimalParameterValue(data);
            double max = MCAT5Tools.FindMaximalParameterValue(data);
            
            for (int i=0;i<this.data.set.length;i++){
                int index = (int)((data.set[i] - min) / (max-min) * (boxes.length-1));
                boxes[index] += eff.set[i];
                boxes_count[index] += 1.0;
            }
            for (int i=0;i<boxes.length;i++){
                dataset.add(((max-min)/(boxes.length-1))*i,boxes[i]/boxes_count[i]);
            }
            
            plot.setDataset(0, new XYBarDataset(new XYSeriesCollection(dataset),(max-min)/(double)BOX_COUNT));
            
            if (null != plot.getRangeAxis())   plot.getRangeAxis().setAutoRange(true);
            if (null != plot.getDomainAxis())  plot.getDomainAxis().setAutoRange(true);            
        }
        
        public JPanel getPanel(){
            return chartPanel;
        }
    }
