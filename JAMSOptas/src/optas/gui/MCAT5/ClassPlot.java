/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package optas.gui.MCAT5;

import jams.JAMS;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.util.ArrayList;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import optas.gui.MCAT5.MCAT5Plot.SimpleRequest;
import optas.data.DataSet;
import optas.data.Efficiency;
import optas.data.EfficiencyEnsemble;
import optas.data.TimeSerie;
import optas.data.TimeSerieEnsemble;

/**
 *
 * @author Christian Fischer
 */
@SuppressWarnings({"unchecked"})
public class ClassPlot extends MCAT5Plot {

    XYLineAndShapeRenderer renderer = new XYLineAndShapeRenderer();
    XYPlot plot = new XYPlot();
    JPanel mainPanel = new JPanel();
    int GROUPS = 10;

    public ClassPlot() {
        this.addRequest(new SimpleRequest(JAMS.i18n("SIMULATED_TIMESERIE"), TimeSerie.class));
        this.addRequest(new SimpleRequest(JAMS.i18n("Efficiency"), Efficiency.class));
        init();
    }

    private void init() {
        plot.setRenderer(renderer);
        plot.setDomainAxis(new NumberAxis(JAMS.i18n("TIME")));
        plot.setRangeAxis(new NumberAxis(JAMS.i18n("OUTPUT")));

        JFreeChart chart = new JFreeChart(plot);
        chart.setTitle(JAMS.i18n("CLASS_PLOT"));
        ChartPanel chartPanel = new ChartPanel(chart, true);

        mainPanel = new JPanel(new BorderLayout());
        mainPanel.add(chartPanel, BorderLayout.NORTH);

        JPanel sliderPanel = new JPanel(new BorderLayout());
        sliderPanel.setMaximumSize(new Dimension(500, 100));
        sliderPanel.setPreferredSize(new Dimension(500, 100));
        sliderPanel.setMinimumSize(new Dimension(500, 100));

        JSlider slider = new JSlider();
        slider.setMinimum(1);
        slider.setMaximum(30);
        slider.setValue(GROUPS);
        slider.addChangeListener(new ChangeListener() {

            public void stateChanged(ChangeEvent e) {
                JSlider slider = (JSlider) e.getSource();
                ClassPlot.this.GROUPS = slider.getValue();
                try {
                    refresh();
                } catch (NoDataException e1) {
                    JOptionPane.showMessageDialog(mainPanel, "Failed to show dataset. The data is incommensurate!");
                }
            }
        });
        sliderPanel.add(new JLabel("number of boxes"), BorderLayout.WEST);
        sliderPanel.add(slider, BorderLayout.EAST);
        mainPanel.add(sliderPanel, BorderLayout.SOUTH);

        try {
            refresh();
        } catch (NoDataException e) {
            JOptionPane.showMessageDialog(sliderPanel, "Failed to show dataset. The data is incommensurate!");
        }
    }

    public void refresh() throws NoDataException {
        if (!this.isRequestFulfilled()) {
            return;
        }

        ArrayList<DataSet> p[] = getData(new int[]{0, 1});
        TimeSerieEnsemble ts = (TimeSerieEnsemble) p[0].get(0);
        EfficiencyEnsemble eff = (EfficiencyEnsemble) p[1].get(0);


        for (int i = 0; i < GROUPS; i++) {
            renderer.setSeriesShapesVisible(i, false);
            int c = (int) (i * 255.0 / GROUPS);
            renderer.setSeriesPaint(i, new Color(255 - c, 0, c));
            renderer.setSeriesVisibleInLegend(i, false);
        }
        renderer.setSeriesVisibleInLegend(0, true);
        renderer.setSeriesVisibleInLegend(GROUPS - 1, true);

        EfficiencyEnsemble likelihood = eff.CalculateLikelihood();
        Integer sortedIds[] = likelihood.sort();

        int n = eff.getSize();
        int T = ts.getTimesteps();

        XYSeriesCollection series = new XYSeriesCollection();
        for (int i = 0; i < GROUPS; i++) {
            XYSeries dataset = new XYSeries("");
            if (i == 0) {
                dataset = new XYSeries(JAMS.i18n("HIGH_LIKELIHOOD"));
            }
            if (i == GROUPS - 1) {
                dataset = new XYSeries(JAMS.i18n("LOW_LIKELIHOOD"));
            }
            int index = (int) ((n / (double) GROUPS) * i);

            for (int j = 0; j < T; j++) {
                dataset.add(j, ts.get(j, sortedIds[index]));
            }
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
        return mainPanel;
    }
}
