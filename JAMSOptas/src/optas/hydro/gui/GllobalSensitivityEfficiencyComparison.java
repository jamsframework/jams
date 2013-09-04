/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package optas.hydro.gui;

import jams.JAMS;
import java.awt.Color;
import java.util.ArrayList;
import javax.swing.JPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYDotRenderer;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import optas.gui.MCAT5.MCAT5Plot;
import optas.hydro.calculations.SlopeCalculations;
import optas.data.DataSet;
import optas.data.Efficiency;
import optas.data.EfficiencyEnsemble;
import optas.data.Parameter;
import optas.data.SimpleEnsemble;
import optas.tools.PatchedChartPanel;

/**
 *
 * @author Christian Fischer
 */
public class GllobalSensitivityEfficiencyComparison extends MCAT5Plot {

    XYPlot plot = new XYPlot();
    PatchedChartPanel chartPanel = null;

    public GllobalSensitivityEfficiencyComparison() {
        this.addRequest(new SimpleRequest(JAMS.i18n("Efficiency"), Efficiency.class));
        this.addRequest(new SimpleRequest(JAMS.i18n("PARAMETER"), Parameter.class));

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
        chart.setTitle(JAMS.i18n("DOTTY_PLOT"));
        chartPanel = new PatchedChartPanel(chart, true);

        redraw();
    }

  
    

    @Override
    public void refresh() throws NoDataException {
        if (!this.isRequestFulfilled()) {
            return;
        }

        ArrayList<DataSet> p[] = getData(new int[]{0,1});
        EfficiencyEnsemble eff = (EfficiencyEnsemble)p[0].get(0);
        SimpleEnsemble param = (SimpleEnsemble)p[1].get(0);
        
        plot.setDomainAxis(new NumberAxis(eff.getName()));
        plot.setRangeAxis(new NumberAxis("slope"));
        
        XYSeries dataset[] = SlopeCalculations.calculateDerivative(eff,this.getDataSource());

        int c=-1;
        for (int i=0;i<dataset.length;i++){
            if (dataset[i].getDescription().equals(param.getName())){
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
