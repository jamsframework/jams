/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package optas.gui.MCAT5;


import jams.JAMS;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.util.ArrayList;
import java.util.Arrays;
import javax.swing.BorderFactory;
import javax.swing.GroupLayout;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.JTextField;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import optas.data.DataSet;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYBarRenderer;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.xy.XYBarDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import optas.data.Efficiency;
import optas.data.EfficiencyEnsemble;
import optas.data.Parameter;
import optas.data.SimpleEnsemble;
import optas.tools.PatchedChartPanel;
import org.jfree.chart.renderer.xy.StandardXYBarPainter;
import org.jfree.chart.renderer.xy.XYBarPainter;
import org.jfree.chart.util.ShadowGenerator;


/**
 *
 * @author Christian Fischer
 */
@SuppressWarnings({"unchecked"})
public class IdentifiabilityPlot extends MCAT5Plot{
    XYPlot plot = new XYPlot();    
    PatchedChartPanel chartPanel = null;
    JPanel mainPanel;
    JTextField groupCount = new JTextField(2);
    int boxCount = 10;

    public IdentifiabilityPlot() {
        this.addRequest(new SimpleRequest(JAMS.i18n("PARAMETER"),Parameter.class));
        this.addRequest(new SimpleRequest(JAMS.i18n("Efficiency"),Efficiency.class));
        init();
    }

    private void init() {
        JFreeChart chart = new JFreeChart(plot);
        chart.removeLegend();
        chart.setTitle(JAMS.i18n("IDENTIFYABLITY_PLOT"));
        chartPanel = new PatchedChartPanel(chart, true);

        chartPanel.setMinimumDrawWidth( 0 );
        chartPanel.setMinimumDrawHeight( 0 );
        chartPanel.setMaximumDrawWidth( MAXIMUM_WIDTH );
        chartPanel.setMaximumDrawHeight( MAXIMUM_HEIGHT );
        
        chart.getPlot().setBackgroundPaint(Color.white);
        //chart.getXYPlot().setDomainGridlinePaint(Color.black);
        
        XYLineAndShapeRenderer gradient_renderer = new XYLineAndShapeRenderer();
        gradient_renderer.setSeriesPaint(0, Color.BLACK);
        gradient_renderer.setBaseShapesVisible(false);
        plot.setRenderer(0, gradient_renderer);

        JPanel sliderPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        sliderPanel.setMaximumSize(new Dimension(250, 60));
        sliderPanel.setPreferredSize(new Dimension(250, 60));
        sliderPanel.setMinimumSize(new Dimension(250, 60));

        JSlider slider = new JSlider();
        slider.setMinimum(2);
        slider.setMaximum(30);
        slider.setValue(boxCount);
        groupCount.setText(Integer.toString(boxCount));
        groupCount.setEnabled(false);
        
        slider.addChangeListener(new ChangeListener() {

            @Override
            public void stateChanged(ChangeEvent e) {
                JSlider slider = (JSlider) e.getSource();
                IdentifiabilityPlot.this.boxCount = slider.getValue();
                groupCount.setText(Integer.toString(boxCount));
                redraw();
            }
        });
        sliderPanel.setBorder(BorderFactory.createTitledBorder(JAMS.i18n("number_of_groups")));
        sliderPanel.add(slider);
        sliderPanel.add(groupCount);        
        groupCount.setMaximumSize(new Dimension(60, 60));
        mainPanel = new JPanel();
        GroupLayout layout = new GroupLayout(mainPanel);
        mainPanel.setLayout(layout);
        
        layout.setHorizontalGroup(layout.createParallelGroup(GroupLayout.Alignment.TRAILING)
                .addComponent(chartPanel)
                .addComponent(sliderPanel)
                );
        
        layout.setVerticalGroup(layout.createSequentialGroup()
                .addComponent(chartPanel)
                .addComponent(sliderPanel)
                );
        
        redraw();
    }
        
    public void refresh() throws NoDataException {
        if (!this.isRequestFulfilled())
            return;

        ArrayList<DataSet> p[] = getData(new int[]{0,1});
        SimpleEnsemble p1 = (SimpleEnsemble)p[0].get(0);
        EfficiencyEnsemble p2 = (EfficiencyEnsemble)p[1].get(0);


        plot.setDomainAxis(new NumberAxis(p1.getName()));
        plot.setRangeAxis(new NumberAxis(JAMS.i18n("CUMULATIVE_DISTRIBUTION")));

        XYSeries dataset_box[] = new XYSeries[boxCount];
        XYSeries dataset = new XYSeries(JAMS.i18n("CUMULATIVE_DISTRIBUTION"));
        for (int i = 0; i < boxCount; i++) {
            dataset_box[i] = new XYSeries(i);
        }
            
        XYBarRenderer renderer = new XYBarRenderer(0.33 / (double)boxCount);

        renderer.setShadowVisible(false);
        renderer.setBarPainter(new StandardXYBarPainter());
        Integer sortedIds[] = p2.sort();

        //double sortedData[][] = MCAT5Tools.sortbyEff(p1,p2);
        double threshold = p1.getSize() * 0.1;
        double best[] = new double[(int)threshold];
        double boxes[] = new double[boxCount];

        //sort after parameter value
        for (int i=0;i<(int)threshold;i++){
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
