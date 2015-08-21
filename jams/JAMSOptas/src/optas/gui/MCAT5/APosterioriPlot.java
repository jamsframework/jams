/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package optas.gui.MCAT5;

import jams.JAMS;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.util.ArrayList;
import javax.swing.BorderFactory;
import javax.swing.GroupLayout;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.JTextField;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import optas.data.Calculations;
import optas.data.DataSet;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYBarRenderer;
import org.jfree.data.Range;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import org.jfree.data.xy.XYBarDataset;
import optas.data.Efficiency;
import optas.data.EfficiencyEnsemble;
import optas.data.Parameter;
import optas.data.SimpleEnsemble;
import optas.tools.PatchedChartPanel;
import org.jfree.chart.renderer.xy.StandardXYBarPainter;

/**
 *
 * @author Christian Fischer
 */
public class APosterioriPlot extends MCAT5Plot {

    XYPlot plot = new XYPlot();
    PatchedChartPanel chartPanel = null;
    JPanel mainPanel;
    int boxCount = 20;
    JTextField groupCount = new JTextField(2);

    public APosterioriPlot() {
        this.addRequest(new SimpleRequest(JAMS.i18n("PARAMETER"), Parameter.class));
        this.addRequest(new SimpleRequest(JAMS.i18n("Efficiency"), Efficiency.class));
        init();
    }

    private void init() {
        JFreeChart chart = new JFreeChart(plot);
        chart.setTitle(JAMS.i18n("A_POSTERIO_PARAMETER_DISTRIBUTION"));
        chartPanel = new PatchedChartPanel(chart, true);

        chartPanel.setMinimumDrawWidth( 0 );
        chartPanel.setMinimumDrawHeight( 0 );
        chartPanel.setMaximumDrawWidth( MAXIMUM_WIDTH );
        chartPanel.setMaximumDrawHeight( MAXIMUM_HEIGHT );
        chart.getPlot().setBackgroundPaint(Color.white);
        
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
                APosterioriPlot.this.boxCount = slider.getValue();
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

    @Override
    public void refresh() throws NoDataException{
        if (!this.isRequestFulfilled()) {
            return;
        }

        ArrayList<DataSet> p[] = getData(new int[]{0, 1});
        SimpleEnsemble p1 = (SimpleEnsemble) p[0].get(0);
        EfficiencyEnsemble p2 = (EfficiencyEnsemble) p[1].get(0);

        double boxes[] = Calculations.calcPostioriDistribution(p1, p2, boxCount);

        double bounds[] = Calculations.calcBounds(p1, p2, boxCount,1.25);

        System.out.println("Recommend parameter range:" + "[" + bounds[0] + "<" + bounds[1] + "]");

        plot.setDomainAxis(new NumberAxis(p1.getName()));
        plot.setRangeAxis(new NumberAxis(JAMS.i18n("MEAN_OF_EFFICIENCY")));

        XYSeries dataset = new XYSeries(JAMS.i18n("MEAN_OF_EFFICIENCY"));
        
        double min = p1.getMin();
        double max = p1.getMax();

        for (int i = 0; i < boxes.length; i++) {
            dataset.add(min + ((max - min) / (boxes.length - 1)) * i, boxes[i] );
        }

        plot.setDataset(0, new XYBarDataset(new XYSeriesCollection(dataset), ((max - min) / (double) boxCount)));

        XYBarRenderer renderer = new XYBarRenderer(0.33 / (double) boxCount);        
        renderer.setShadowVisible(false);
        renderer.setBarPainter(new StandardXYBarPainter());
        renderer.setSeriesPaint(0, Color.DARK_GRAY);        
        plot.setRenderer(0, renderer);

        if (null != plot.getRangeAxis()) {
            plot.getRangeAxis().setAutoRange(true);
        }
        if (null != plot.getDomainAxis()) {
            plot.getDomainAxis().setRange(new Range(min, max));
        }
    }

    public JPanel getPanel() {
        return mainPanel;
    }
}
