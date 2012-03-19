/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package reg.gui.MCAT5;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYBarRenderer;
import org.jfree.data.Range;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import org.jfree.data.xy.XYBarDataset;
import reg.hydro.data.Efficiency;
import reg.hydro.data.EfficiencyEnsemble;
import reg.hydro.data.Parameter;
import reg.hydro.data.SimpleEnsemble;


/**
 *
 * @author Christian Fischer
 */
public class APosterioriPlot extends MCAT5Plot{
        XYPlot plot = new XYPlot();        
        ChartPanel chartPanel = null;
        JPanel mainPanel;
        int boxCount = 20;
                    
        public APosterioriPlot(){                  
            this.addRequest(new SimpleRequest(java.util.ResourceBundle.getBundle("reg/resources/JADEBundle").getString("PARAMETER"),Parameter.class));
            this.addRequest(new SimpleRequest(java.util.ResourceBundle.getBundle("reg/resources/JADEBundle").getString("Efficiency"),Efficiency.class));
            init();
        }

        private void init(){            
            JFreeChart chart = new JFreeChart(plot);
            chart.setTitle(java.util.ResourceBundle.getBundle("reg/resources/JADEBundle").getString("A_POSTERIO_PARAMETER_DISTRIBUTION"));
            chartPanel = new ChartPanel(chart, true);

            mainPanel = new JPanel(new BorderLayout());
            mainPanel.add(chartPanel,BorderLayout.NORTH);

            JPanel sliderPanel = new JPanel(new BorderLayout());
            sliderPanel.setMaximumSize(new Dimension(300,100));
            sliderPanel.setPreferredSize(new Dimension(300,100));
            sliderPanel.setMinimumSize(new Dimension(300,100));

            JSlider slider = new JSlider();
            slider.setMinimum(1);
            slider.setMaximum(30);
            slider.setValue(boxCount);
            slider.addChangeListener(new ChangeListener(){
               public void stateChanged(ChangeEvent e){
                   JSlider slider = (JSlider)e.getSource();
                   APosterioriPlot.this.boxCount = slider.getValue();
                    APosterioriPlot.this.refresh();
               }
            });
            sliderPanel.add(new JLabel("number of boxes"),BorderLayout.WEST);
            sliderPanel.add(slider,BorderLayout.EAST);
            mainPanel.add(sliderPanel, BorderLayout.SOUTH);
        }

        public void refresh(){
            if (!this.isRequestFulfilled())
                return;

            SimpleEnsemble  p1 = (SimpleEnsemble)getData(0);
            EfficiencyEnsemble p2 = (EfficiencyEnsemble)getData(1);
            p2 = p2.CalculateLikelihood();

            plot.setDomainAxis(new NumberAxis(p1.getName()));
            plot.setRangeAxis(new NumberAxis(java.util.ResourceBundle.getBundle("reg/resources/JADEBundle").getString("MEAN_OF_EFFICIENCY")));

            XYSeries dataset = new XYSeries(java.util.ResourceBundle.getBundle("reg/resources/JADEBundle").getString("MEAN_OF_EFFICIENCY"));
            
            double boxes[] = new double[boxCount];
            double boxes_count[] = new double[boxCount];
            
            double min = p1.getMin();
            double max = p1.getMax();

            double sum = 0;

            for (int i=0;i<p1.getSize();i++){
                int index = (int)(((p1.getValue(i) - min) / (max-min) * (boxes.length))-0.001);
                boxes[index] += p2.getValue(i);
                boxes_count[index] += 1.0;
            }
            for (int i=0;i<boxes.length;i++){
                sum += boxes[i]/boxes_count[i];
            }
            for (int i=0;i<boxes.length;i++){
                dataset.add(min+((max-min)/(boxes.length-1))*i,(boxes[i]/boxes_count[i])/sum);
            }
            
            plot.setDataset(0, new XYBarDataset(new XYSeriesCollection(dataset),((max-min)/(double)boxCount)));

            XYBarRenderer renderer = new XYBarRenderer(0.33 / (double)boxCount);
            renderer.setSeriesPaint(0, Color.DARK_GRAY);
            plot.setRenderer(0, renderer);

            if (null != plot.getRangeAxis())   plot.getRangeAxis().setAutoRange(true);
            if (null != plot.getDomainAxis())  plot.getDomainAxis().setRange(new Range(min,max));
        }
        
        public JPanel getPanel(){
            return mainPanel;
        }
    }
