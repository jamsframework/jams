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
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Arrays;
import javax.swing.GroupLayout;
import javax.swing.JCheckBox;
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
import org.jfree.chart.annotations.XYTitleAnnotation;
import org.jfree.chart.block.BlockBorder;
import org.jfree.chart.title.LegendTitle;
import org.jfree.ui.RectangleAnchor;
import org.jfree.ui.RectangleEdge;

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

    JCheckBox showOnlyBestAndWorst = new JCheckBox("show only best and worst graph");
    JCheckBox blackAndWhite = new JCheckBox("b/w mode");

    public RegionalSensitivityAnalyser() {
        this.addRequest(new SimpleRequest(JAMS.i18n("PARAMETER"), Parameter.class));
        this.addRequest(new SimpleRequest(JAMS.i18n("Efficiency"), Efficiency.class));

        init();
    }

    private void init() {
        JFreeChart chart = new JFreeChart(plot);
        chart.setTitle(JAMS.i18n("REGIONAL_SENSITIVITY_ANALYSIS"));
        chartPanel = new PatchedChartPanel(chart, true);
        chartPanel.setMinimumDrawWidth(0);
        chartPanel.setMinimumDrawHeight(0);
        chartPanel.setMaximumDrawWidth(MAXIMUM_WIDTH);
        chartPanel.setMaximumDrawHeight(MAXIMUM_HEIGHT);

        plot.setRenderer(renderer);
        plot.setRangeAxis(new NumberAxis(JAMS.i18n("cumulative frequency distribution")));

        mainPanel = new JPanel();

        GroupLayout layout = new GroupLayout(mainPanel);
        mainPanel.setLayout(layout);

        LegendTitle lt = new LegendTitle(plot);
        lt.setItemFont(new Font("Dialog", Font.PLAIN, 14));
        lt.setBackgroundPaint(new Color(200, 200, 200, 100));
        lt.setFrame(new BlockBorder(Color.white));
        lt.setPosition(RectangleEdge.BOTTOM);
        XYTitleAnnotation ta = new XYTitleAnnotation(0.98, 0.02, lt, RectangleAnchor.BOTTOM_RIGHT);
        
        ta.setMaxWidth(0.48);
        plot.addAnnotation(ta);

        
        
        mainPanel.add(chartPanel, BorderLayout.NORTH);

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

        layout.setHorizontalGroup(layout.createParallelGroup()
                .addComponent(chartPanel)
                .addGroup(layout.createSequentialGroup()
                        .addComponent(showOnlyBestAndWorst)
                        .addComponent(blackAndWhite)
                        .addComponent(sliderPanel)
                )
        );

        layout.setVerticalGroup(layout.createSequentialGroup()
                .addComponent(chartPanel)
                .addGroup(layout.createParallelGroup()
                        .addComponent(showOnlyBestAndWorst)
                        .addComponent(blackAndWhite)
                        .addComponent(sliderPanel)
                )
        );

        showOnlyBestAndWorst.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                redraw();
            }
        });

        blackAndWhite.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                redraw();
            }
        });

        chart.getPlot().setBackgroundPaint(Color.white);
        chart.removeLegend();
    }

    public void refresh() throws NoDataException {
        if (!this.isRequestFulfilled()) {
            return;
        }

        ArrayList<DataSet> p[] = getData(new int[]{0, 1});
        SimpleEnsemble param = (SimpleEnsemble) p[0].get(0);
        EfficiencyEnsemble eff = (EfficiencyEnsemble) p[1].get(0);

        if (showOnlyBestAndWorst.isSelected()) {
            renderer.setSeriesShapesVisible(0, false);
            renderer.setSeriesShapesVisible(1, false);
            renderer.setSeriesStroke(0, new BasicStroke(3));
            renderer.setSeriesVisibleInLegend(0, true);
            renderer.setSeriesStroke(1, new BasicStroke(3));
            renderer.setSeriesVisibleInLegend(1, true);
            if (blackAndWhite.isSelected()) {
                renderer.setSeriesPaint(0, new Color(64, 64, 64));
                renderer.setSeriesPaint(1, new Color(172, 172, 172));
            } else {
                renderer.setSeriesPaint(0, new Color(0, 0, 255));
                renderer.setSeriesPaint(1, new Color(255, 0, 0));
            }
        } else {
            for (int i = 0; i < GROUPS; i++) {
                renderer.setSeriesShapesVisible(i, false);
                renderer.setSeriesVisibleInLegend(i, false);
                if ((i == 0 || i == GROUPS - 1)) {
                    renderer.setSeriesStroke(i, new BasicStroke(5));
                    renderer.setSeriesVisibleInLegend(i, true);
                } else {
                    renderer.setSeriesStroke(i, new BasicStroke(1));
                }
                if (blackAndWhite.isSelected()) {
                    int c = 64 + (int) (i * 128.0 / GROUPS);
                    renderer.setSeriesPaint(i, new Color(c, c, c));
                } else {
                    int c = (int) (i * 255.0 / GROUPS);
                    renderer.setSeriesPaint(i, new Color(255 - c, 0, c));
                }
            }
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
                dataset = new XYSeries("worst group");
            } else if (i == boxes.length - 1) {
                dataset = new XYSeries("best group");
            } else {
                dataset = new XYSeries(i);
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
            if (!showOnlyBestAndWorst.isSelected() || i == 0 || i == GROUPS - 1) {
                series.addSeries(dataset);
            }

        }
        plot.setDataset(series);
        if (plot.getRangeAxis() != null) {
            plot.getRangeAxis().setRange(0.0, 1.0);
        }
        if (plot.getDomainAxis() != null) {
            double r = range_max - range_min;
            int l = (int)Math.log(r);
            if (l>=0){
                double min = Math.floor(range_min*Math.pow(10, l+1)) / Math.pow(10, l+1);
                double max = Math.ceil(range_max*Math.pow(10, l+1)) / Math.pow(10, l+1)+1E-10;
                plot.getDomainAxis().setRange(min, max);
            }else{
                double min = Math.floor(range_min*Math.pow(10, -l-1)) / Math.pow(10, -l-1);
                double max = Math.ceil(range_max*Math.pow(10, -l-1)) / Math.pow(10, -l-1)+1E-10;
                plot.getDomainAxis().setRange(min, max);
            }
        }
    }

    public JPanel getPanel() {
        return this.mainPanel;
    }
}
