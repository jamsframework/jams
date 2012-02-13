/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package optas.gui.MCAT5;

import java.awt.Color;
import java.util.ArrayList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import optas.hydro.data.DataSet;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYDotRenderer;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import optas.hydro.data.Efficiency;
import optas.hydro.data.Parameter;
import optas.hydro.data.SimpleEnsemble;

/**
 *
 * @author Christian Fischer
 */
public class DottyPlot extends MCAT5Plot {

    protected XYPlot plot = new XYPlot();
    protected ChartPanel chartPanel = null;

    private void init() {
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

        try {
            refresh();
        } catch (NoDataException e) {
            JOptionPane.showMessageDialog(chartPanel, "Failed to show dataset. The data is incommensurate!");
        }
    }

    public DottyPlot() {
        this.addRequest(new SimpleRequest(java.util.ResourceBundle.getBundle("reg/resources/JADEBundle").getString("PARAMETER"), Parameter.class));
        this.addRequest(new SimpleRequest(java.util.ResourceBundle.getBundle("reg/resources/JADEBundle").getString("Efficiency"), Efficiency.class));

        init();
    }

    public JPanel getPanel() {
        return this.chartPanel;
    }

    public void refresh() throws NoDataException{
        if (!this.isRequestFulfilled()) {
            return;
        }

        ArrayList<DataSet> p[] = getData(new int[]{0, 1});
        SimpleEnsemble p1 = (SimpleEnsemble) p[0].get(0);
        SimpleEnsemble p2 = (SimpleEnsemble) p[1].get(0);

        plot.setDomainAxis(new NumberAxis(p1.getName()));
        plot.setRangeAxis(new NumberAxis(p2.getName()));

        XYSeries dataset = new XYSeries(java.util.ResourceBundle.getBundle("reg/resources/JADEBundle").getString("DATA_POINT"));

        int n = p1.getSize();

        for (int i = 0; i < n; i++) {
            dataset.add(p1.getValue(p1.getId(i)), p2.getValue(p2.getId(i)));
        }
        plot.setDataset(0, new XYSeriesCollection(dataset));

        if (plot.getRangeAxis() != null) {
            plot.getRangeAxis().setRange(p2.getMin(), p2.getMax());
        }
        if (plot.getDomainAxis() != null) {
            plot.getDomainAxis().setRange(p1.getMin(), p1.getMax());
        }

    }
}
