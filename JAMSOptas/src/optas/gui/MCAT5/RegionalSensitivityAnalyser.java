/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package optas.gui.MCAT5;

import jams.JAMS;
import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.util.ArrayList;
import java.util.Arrays;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import optas.data.DataSet;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import optas.data.Efficiency;
import optas.data.EfficiencyEnsemble;
import optas.data.Parameter;
import optas.data.SimpleEnsemble;
import optas.tools.PatchedChartPanel;

/**
 *
 * @author Christian Fischer
 */
@SuppressWarnings({"unchecked"})
public class RegionalSensitivityAnalyser extends MCAT5Plot {

    XYPlot plot = new XYPlot();
    XYLineAndShapeRenderer renderer = new XYLineAndShapeRenderer();
    PatchedChartPanel chartPanel = null;
    JPanel mainPanel = null;
    int GROUPS = 10;

    public RegionalSensitivityAnalyser() {
        this.addRequest(new SimpleRequest(JAMS.i18n("PARAMETER"), Parameter.class));
        this.addRequest(new SimpleRequest(JAMS.i18n("Efficiency"), Efficiency.class));

        init();
    }

    private void init() {
        JFreeChart chart = new JFreeChart(plot);
        chart.setTitle(JAMS.i18n("REGIONAL_SENSITIVITY_ANALYSIS"));
        chartPanel = new PatchedChartPanel(chart, true);
        chartPanel.setMinimumDrawWidth( 0 );
        chartPanel.setMinimumDrawHeight( 0 );
        chartPanel.setMaximumDrawWidth( MAXIMUM_WIDTH );
        chartPanel.setMaximumDrawHeight( MAXIMUM_HEIGHT );
        
        plot.setRenderer(renderer);
        plot.setRangeAxis(new NumberAxis(JAMS.i18n("LIKELIHOOD")));

        mainPanel = new JPanel(new BorderLayout());
        mainPanel.add(chartPanel, BorderLayout.NORTH);

        chart.getPlot().setBackgroundPaint(Color.white);
        chart.getXYPlot().setDomainGridlinePaint(Color.black);
        
        JPanel sliderPanel = new JPanel(new BorderLayout());
        sliderPanel.setMaximumSize(new Dimension(300, 100));
        sliderPanel.setPreferredSize(new Dimension(300, 100));
        sliderPanel.setMinimumSize(new Dimension(300, 100));

        JSlider slider = new JSlider();
        slider.setMinimum(1);
        slider.setMaximum(30);
        slider.setValue(GROUPS);
        slider.addChangeListener(new ChangeListener() {

            public void stateChanged(ChangeEvent e) {
                JSlider slider = (JSlider) e.getSource();
                RegionalSensitivityAnalyser.this.GROUPS = slider.getValue();
                redraw();

            }
        });
        sliderPanel.add(new JLabel("number of boxes"), BorderLayout.WEST);
        sliderPanel.add(slider, BorderLayout.EAST);
        mainPanel.add(sliderPanel, BorderLayout.SOUTH);
    }

    public void refresh() throws NoDataException {
        if (!this.isRequestFulfilled()) {
            return;
        }

        ArrayList<DataSet> p[] = getData(new int[]{0, 1});
        SimpleEnsemble param = (SimpleEnsemble) p[0].get(0);
        EfficiencyEnsemble eff = (EfficiencyEnsemble) p[1].get(0);


        for (int i = 0; i < GROUPS; i++) {
            renderer.setSeriesShapesVisible(i, false);
            renderer.setSeriesVisibleInLegend(i, false);
            if (i == 0 || i == GROUPS - 1) {
                renderer.setSeriesStroke(i, new BasicStroke(5));
                renderer.setSeriesVisibleInLegend(i, true);
            } else {
                renderer.setSeriesStroke(i, new BasicStroke(1));
            }
            int c = (int) (i * 255.0 / GROUPS);
            renderer.setSeriesPaint(i, new Color(255 - c, 0, c));
        }

        plot.setDomainAxis(new NumberAxis(param.name));

        XYSeriesCollection series = new XYSeriesCollection();

        ArrayList<Integer> boxes[] = new ArrayList[GROUPS];
        for (int i = 0; i < GROUPS; i++) {
            boxes[i] = new ArrayList<Integer>();
        }

        EfficiencyEnsemble likelihood = eff.CalculateLikelihood();
        Integer sortedIds[] = likelihood.sort();

        double range_max = param.getMax();
        double range_min = param.getMin();

        //sort data into boxes
        for (int i = 0; i < param.getSize(); i++) {
            int index = (int) (((double) i / (double) param.getSize()) * (boxes.length));// (int) Math.round((sorted_data[i][1] - min) / (max - min) * (boxes.length - 1));
            boxes[index].add(sortedIds[i]);
        }

        XYSeries dataset = null;
        for (int i = 0; i < boxes.length; i++) {
            if (i == 0) {
                dataset = new XYSeries("best group");
            } else if (i == boxes.length - 1) {
                dataset = new XYSeries("worst group");
            } else {
                dataset = new XYSeries("");
            }

            double box_data[] = new double[boxes[i].size()];
            for (int j = 0; j < boxes[i].size(); j++) {
                box_data[j] = param.getValue(boxes[i].get(j));
            }
            Arrays.sort(box_data);

            dataset.add(range_min, 0.0);
            for (int j = 0; j < box_data.length; j++) {
                dataset.add(box_data[j], (double) j / (double) box_data.length);
            }
            dataset.add(range_max, 1.0);
            series.addSeries(dataset);
        }
        plot.setDataset(series);
        if (plot.getRangeAxis() != null) {
            plot.getRangeAxis().setAutoRange(true);
        }
        if (plot.getDomainAxis() != null) {
            plot.getDomainAxis().setAutoRange(true);
        }
    }

    public JPanel getPanel() {
        return this.mainPanel;
    }
}
