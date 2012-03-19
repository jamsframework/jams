/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package reg.hydro.gui;

import java.awt.Color;
import javax.swing.JPanel;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYDotRenderer;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import reg.gui.MCAT5.MCAT5Plot;
import reg.hydro.calculations.SlopeCalculations;
import reg.hydro.data.DataCollection;
import reg.hydro.data.Parameter;
import reg.hydro.data.SimpleEnsemble;
import reg.hydro.data.TimeSerie;
import reg.hydro.data.TimeSerieEnsemble;

/**
 *
 * @author Christian Fischer
 */
public class SimpleGlobalSensitivityAtPoint extends MCAT5Plot {

    XYPlot plot = new XYPlot();
    ChartPanel chartPanel = null;

    int timeIndex = 0;
    public SimpleGlobalSensitivityAtPoint(int index) {
        timeIndex = index;

        this.addRequest(new SimpleRequest(java.util.ResourceBundle.getBundle("reg/resources/JADEBundle").getString("SIMULATED_TIMESERIE"), TimeSerie.class));        
        this.addRequest(new SimpleRequest(java.util.ResourceBundle.getBundle("reg/resources/JADEBundle").getString("PARAMETER"), Parameter.class));
        init();
    }

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

        refresh();
    }
    
    @Override
    public void refresh() {
        if (!this.isRequestFulfilled()) {
            return;
        }

        TimeSerieEnsemble p1 = (TimeSerieEnsemble) getData(0);
        SimpleEnsemble p2 = (SimpleEnsemble) getData(1);
        
        plot.setDomainAxis(new NumberAxis(p1.getName()));
        plot.setRangeAxis(new NumberAxis("slope"));

        int c=-1;
        XYSeries dataset[] = SlopeCalculations.calculateGradientAtTime(this.getDataSource(), p1, timeIndex);
        for (int i=0;i<dataset.length;i++){
            if (dataset[i].getDescription().equals(p2.name)){
                c=i;
                break;
            }
        }
        if (c==-1)
            return;

        plot.setDataset(0, new XYSeriesCollection(dataset[c]));

        if (plot.getRangeAxis() != null) {
            plot.getRangeAxis().setAutoRange(true);
        }
        if (plot.getDomainAxis() != null) {
            plot.getDomainAxis().setAutoRange(true);
        }
    }
    public JPanel getPanel(){
        return this.chartPanel;
    }
}
