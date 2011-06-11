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
import reg.hydro.data.Efficiency;
import reg.hydro.data.Parameter;
import reg.hydro.data.SimpleEnsemble;


/**
 *
 * @author Christian Fischer
 */
public class DottyPlot extends MCAT5Plot{
                                        
        protected XYPlot plot = new XYPlot();
        protected ChartPanel chartPanel = null;

        private void init(){
            //setup renderer
            XYDotRenderer renderer = new XYDotRenderer();                        
            renderer.setSeriesPaint(0, Color.BLUE);
            renderer.setDotHeight(3);
            renderer.setDotWidth(3);            
            //setup plot
            plot.setRenderer(renderer);                                                                          
            //setup chart
            JFreeChart chart = new JFreeChart(plot);
            chart.setTitle(java.util.ResourceBundle.getBundle("reg/resources/JADEBundle").getString("DOTTY_PLOT"));
            chartPanel = new ChartPanel(chart, true);
            
            refresh();
        }
        
        public DottyPlot(){
            this.addRequest(new SimpleRequest(java.util.ResourceBundle.getBundle("reg/resources/JADEBundle").getString("PARAMETER"),Parameter.class));
            this.addRequest(new SimpleRequest(java.util.ResourceBundle.getBundle("reg/resources/JADEBundle").getString("Efficiency"),Efficiency.class));

            init();
        }

        public JPanel getPanel(){
            return this.chartPanel;
        }
        public void refresh(){
            if (!this.isRequestFulfilled())
                return;

            SimpleEnsemble  p1 = (SimpleEnsemble)getData(0),
                            p2 = (SimpleEnsemble)getData(1);
            
            plot.setDomainAxis(new NumberAxis(p1.getName()));
            plot.setRangeAxis(new NumberAxis(p2.getName()));

            XYSeries dataset = new XYSeries(java.util.ResourceBundle.getBundle("reg/resources/JADEBundle").getString("DATA_POINT"));

            int n = p1.getSize();

            for (int i=0;i<n;i++){
                dataset.add(p1.getValue(new Integer(i)),p2.getValue(new Integer(i)));
            }
            plot.setDataset(0,new XYSeriesCollection(dataset));
            
            if (plot.getRangeAxis() != null)    plot.getRangeAxis().setAutoRange(true);
            if (plot.getDomainAxis() != null)   plot.getDomainAxis().setAutoRange(true);

        }               
    }
