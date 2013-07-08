/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package optas.SA;

import jams.JAMS;
import optas.gui.MCAT5.*;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.util.ArrayList;
import java.util.Set;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import optas.hydro.data.Calculations;
import optas.hydro.data.DataSet;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.XYPlot;
import org.jfree.data.Range;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import optas.hydro.data.Efficiency;
import optas.hydro.data.EfficiencyEnsemble;
import optas.hydro.data.Parameter;
import optas.hydro.data.SimpleEnsemble;
import org.jfree.chart.renderer.xy.XYDotRenderer;

/**
 *
 * @author Christian Fischer
 */
public class APosterioriPlot extends MCAT5Plot {

    XYPlot plot = new XYPlot();
    ChartPanel chartPanel = null;
    JPanel mainPanel;
    int boxCount = 20;

    public APosterioriPlot() {
        this.addRequest(new SimpleRequest(JAMS.i18n("PARAMETER"), Parameter.class));
        this.addRequest(new SimpleRequest(JAMS.i18n("Efficiency"), Efficiency.class));
        init();
    }

    private void init() {
        JFreeChart chart = new JFreeChart(plot);
        chart.setTitle(JAMS.i18n("A_POSTERIO_PARAMETER_DISTRIBUTION"));
        chartPanel = new ChartPanel(chart, true);

        mainPanel = new JPanel(new BorderLayout());
        mainPanel.add(chartPanel, BorderLayout.NORTH);

        JPanel sliderPanel = new JPanel(new BorderLayout());
        sliderPanel.setMaximumSize(new Dimension(300, 100));
        sliderPanel.setPreferredSize(new Dimension(300, 100));
        sliderPanel.setMinimumSize(new Dimension(300, 100));

        JSlider slider = new JSlider();
        slider.setMinimum(1);
        slider.setMaximum(30);
        slider.setValue(boxCount);
        slider.addChangeListener(new ChangeListener() {

            public void stateChanged(ChangeEvent e) {
                JSlider slider = (JSlider) e.getSource();
                APosterioriPlot.this.boxCount = slider.getValue();
                try {
                    refresh();
                } catch (NoDataException e1) {
                    JOptionPane.showMessageDialog(chartPanel, "Failed to show dataset. The data is incommensurate!");
                }
            }
        });
        sliderPanel.add(new JLabel("number of boxes"), BorderLayout.WEST);
        sliderPanel.add(slider, BorderLayout.EAST);
        mainPanel.add(sliderPanel, BorderLayout.SOUTH);
    }

    public void refresh() throws NoDataException{
        if (!this.isRequestFulfilled()) {
            return;
        }

        ArrayList<DataSet> p[] = getData(new int[]{0, 1});
        SimpleEnsemble p1 = (SimpleEnsemble) p[0].get(0);
        EfficiencyEnsemble p2 = (EfficiencyEnsemble) p[1].get(0);

        UniversalSensitivityAnalyzer sa = new UniversalSensitivityAnalyzer();
        sa.setMethod(UniversalSensitivityAnalyzer.SAMethod.RSA);
        sa.setSampleCount(8192);
        sa.setUseANNRegression(true);
        Set<String> sets = this.getDataSource().getDatasets(Parameter.class);
        SimpleEnsemble se[] = new SimpleEnsemble[sets.size()];
        int k=0;
        for (String s : sets){
            se[k] = (SimpleEnsemble)this.getDataSource().getDataSet(s);
            k++;
        }
        sa.setup(se, p2);

        String name = p1.name+"(*)";
        SimpleEnsemble e[] = sa.getXDataSet();
        int u=0;
        while(!e[u].name.equals(name))
            u++;

        double boxes[] = Calculations.calcPostioriDistribution(e[u], sa.getYDataSet(), boxCount);

        double bounds[] = Calculations.calcBounds(e[u], sa.getYDataSet(), boxCount,1.25);

        System.out.println("Recommend parameter range:" + "[" + bounds[0] + "<" + bounds[1] + "]");

        plot.setDomainAxis(new NumberAxis(p1.getName()));
        plot.setRangeAxis(new NumberAxis(JAMS.i18n("MEAN_OF_EFFICIENCY")));

        double min = p1.getMin();
        double max = p1.getMax();
        
        /*XYSeries dataset = new XYSeries(JAMS.i18n("MEAN_OF_EFFICIENCY"));
        
        double min = p1.getMin();
        double max = p1.getMax();

        for (int i = 0; i < boxes.length; i++) {
            dataset.add(min + ((max - min) / (boxes.length - 1)) * i, boxes[i] );
            //System.out.println(boxes[i]);
        }*/

        //plot.setDataset(0, new XYBarDataset(new XYSeriesCollection(dataset), ((max - min) / (double) boxCount)));
        XYSeries dataset = new XYSeries(JAMS.i18n("DATA_POINT"));

        int n = e[u].getSize();

        for (int i = 0; i < n; i++) {
            dataset.add(e[u].getValue(e[u].getId(i)), sa.getYDataSet().getValue(sa.getYDataSet().getId(i)));
        }
        plot.setDataset(0, new XYSeriesCollection(dataset));

        XYDotRenderer renderer = new XYDotRenderer();
        renderer.setSeriesPaint(0, Color.BLUE);
        renderer.setDotHeight(3);
        renderer.setDotWidth(3);
        //setup plot
        plot.setRenderer(renderer);
        //setup chart
        JFreeChart chart = new JFreeChart(plot);
        chart.setTitle(JAMS.i18n("DOTTY_PLOT"));
        chartPanel = new ChartPanel(chart, true);

        /*XYBarRenderer renderer = new XYBarRenderer(0.33 / (double) boxCount);
        renderer.setSeriesPaint(0, Color.DARK_GRAY);
        plot.setRenderer(0, renderer);*/

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
