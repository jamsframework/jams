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
import org.jfree.chart.renderer.xy.XYDotRenderer;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import reg.gui.MCAT5Toolbar.EfficiencyDataSet;
import reg.gui.MCAT5Toolbar.ParameterSet;

/**
 *
 * @author Christian Fischer
 */
public class DottyPlot{
                                        
        XYPlot plot = new XYPlot();        
        ChartPanel chartPanel = null;
                
        ParameterSet data = null;
        EfficiencyDataSet eff = null;        
              
        void init(ParameterSet data,EfficiencyDataSet eff){
            //copy data
            this.data = data;
            this.eff = eff;            
                       
            //setup renderer
            XYDotRenderer renderer = new XYDotRenderer();                        
            renderer.setSeriesPaint(0, Color.BLUE);
            renderer.setDotHeight(3);
            renderer.setDotWidth(3);            
            //setup plot
            plot.setRenderer(renderer);                                                              
            plot.setDomainAxis(new NumberAxis(data.name));            
            plot.setRangeAxis(new NumberAxis(eff.name));
            //setup chart
            JFreeChart chart = new JFreeChart(plot);
            chart.setTitle("Dotty Plot");
            chartPanel = new ChartPanel(chart, true);
                                                                        
            updateData();
        }
        
        public DottyPlot(EfficiencyDataSet data,EfficiencyDataSet eff){ 
            ParameterSet dummy = new ParameterSet();
            dummy.set = data.set;
            dummy.name = data.name;
            dummy.parent = eff.parent;
            init(dummy,eff);
            
        }
        public DottyPlot(ParameterSet data,EfficiencyDataSet eff){      
            init(data,eff);
        }
        
        public void updateData(){     
            XYSeries dataset = new XYSeries("data point");
            
            for (int i=0;i<data.parent.numberOfRuns;i++){                
                dataset.add(data.set[i],eff.set[i]);            
            }
            plot.setDataset(new XYSeriesCollection(dataset));
            
            if (plot.getRangeAxis() != null)    plot.getRangeAxis().setAutoRange(true);
            if (plot.getDomainAxis() != null)   plot.getDomainAxis().setAutoRange(true);
        }
        
        public JPanel getPanel(){
            return chartPanel;
        }
    }
