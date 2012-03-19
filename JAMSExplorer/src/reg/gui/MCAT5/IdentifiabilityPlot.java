/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package reg.gui.MCAT5;


import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.util.Arrays;
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
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.xy.XYBarDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import reg.hydro.data.Efficiency;
import reg.hydro.data.EfficiencyEnsemble;
import reg.hydro.data.Parameter;
import reg.hydro.data.SimpleEnsemble;


/**
 *
 * @author Christian Fischer
 */
@SuppressWarnings({"unchecked"})
public class IdentifiabilityPlot extends MCAT5Plot{
    XYPlot plot = new XYPlot();    
    ChartPanel chartPanel = null;
    JPanel mainPanel;

    int boxCount = 10;

    public IdentifiabilityPlot() {
        this.addRequest(new SimpleRequest(java.util.ResourceBundle.getBundle("reg/resources/JADEBundle").getString("PARAMETER"),Parameter.class));
        this.addRequest(new SimpleRequest(java.util.ResourceBundle.getBundle("reg/resources/JADEBundle").getString("Efficiency"),Efficiency.class));
        init();
    }

    private void init() {
        JFreeChart chart = new JFreeChart(plot);
        chart.removeLegend();
        chart.setTitle(java.util.ResourceBundle.getBundle("reg/resources/JADEBundle").getString("IDENTIFYABLITY_PLOT"));
        chartPanel = new ChartPanel(chart, true);

        XYLineAndShapeRenderer gradient_renderer = new XYLineAndShapeRenderer();
        gradient_renderer.setSeriesPaint(0, Color.BLACK);
        gradient_renderer.setBaseShapesVisible(false);
        plot.setRenderer(0, gradient_renderer);

        mainPanel = new JPanel(new BorderLayout());
        mainPanel.add(chartPanel, BorderLayout.NORTH);

        JPanel sliderPanel = new JPanel(new BorderLayout());
        sliderPanel.setMaximumSize(new Dimension(500, 100));
        sliderPanel.setPreferredSize(new Dimension(500, 100));
        sliderPanel.setMinimumSize(new Dimension(500, 100));

        JSlider slider = new JSlider();
        slider.setMinimum(1);
        slider.setMaximum(30);
        slider.setValue(boxCount);
        slider.addChangeListener(new ChangeListener() {

            public void stateChanged(ChangeEvent e) {
                JSlider slider = (JSlider) e.getSource();
                IdentifiabilityPlot.this.boxCount = slider.getValue();
                IdentifiabilityPlot.this.refresh();
            }
        });
        sliderPanel.add(new JLabel("number of boxes"), BorderLayout.WEST);
        sliderPanel.add(slider, BorderLayout.EAST);
        mainPanel.add(sliderPanel, BorderLayout.SOUTH);
    }
        
    public void refresh() {
        if (!this.isRequestFulfilled())
            return;
        
        SimpleEnsemble      p1 = (SimpleEnsemble)getData(0);
        EfficiencyEnsemble  p2 = (EfficiencyEnsemble)getData(1);

        plot.setDomainAxis(new NumberAxis(p1.getName()));
        plot.setRangeAxis(new NumberAxis(java.util.ResourceBundle.getBundle("reg/resources/JADEBundle").getString("CUMULATIVE_DISTRIBUTION")));

        XYSeries dataset_box[] = new XYSeries[boxCount];
        XYSeries dataset = new XYSeries(java.util.ResourceBundle.getBundle("reg/resources/JADEBundle").getString("CUMULATIVE_DISTRIBUTION"));
        for (int i = 0; i < boxCount; i++) {
            dataset_box[i] = new XYSeries("");
        }
            
        XYBarRenderer renderer = new XYBarRenderer(0.33 / (double)boxCount);

        Integer sortedIds[] = p2.sort();

        //double sortedData[][] = MCAT5Tools.sortbyEff(p1,p2);
        double threshold = p1.getSize() * 0.1;
        double best[] = new double[(int)threshold];
        double boxes[] = new double[boxCount];

        //sort after parameter value
        for (int i=0;i<threshold;i++){
            best[i] = p1.getValue(sortedIds[i]);
        }
        Arrays.sort(best);

        double value = 0.0;

        double min = best[0];
        double max = best[best.length - 1];

        for (int i = 0; i < (int)threshold; i++) {
            dataset.add(best[i], value);
            value += 1.0 / threshold;

            int index = (int)(((best[i] - min) / (max - min) * (boxes.length)) - 0.0001);
            boxes[index] += 1.0 / threshold;
        }

        for (int i = 0; i < boxCount; i++) {
            dataset_box[i].add((max - min) / (boxes.length - 1) * i + min, boxes[i]);
        }

        XYSeriesCollection XYBarSerie = new XYSeriesCollection();        
        for (int i = 0; i < boxCount; i++) {
            int color = (int)((1.0-boxes[i])*255.0);                     
            XYBarSerie.addSeries(dataset_box[i]);
            renderer.setSeriesPaint(i, new Color(color,color,color));                        
        }
        plot.setRenderer(1,renderer);
        //plot.setDataset(0,XYBarSerie);        
        plot.setDataset(1,new XYBarDataset(XYBarSerie,(max-min)/(double)boxCount));
        plot.setDataset(0,new XYSeriesCollection(dataset));
                                        
        if (plot.getRangeAxis() != null) plot.getRangeAxis().setAutoRange(true);        
        if (plot.getDomainAxis() != null)plot.getDomainAxis().setAutoRange(true);
        
    }

    public JPanel getPanel() {
        return mainPanel;
    }
}
