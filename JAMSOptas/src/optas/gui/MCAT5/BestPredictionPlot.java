/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package optas.gui.MCAT5;

import jams.JAMS;
import java.awt.BasicStroke;
import java.awt.Color;
import java.util.ArrayList;
import javax.swing.JPanel;
import optas.data.DataSet;
import optas.data.Efficiency;
import optas.data.EfficiencyEnsemble;
import optas.data.Measurement;
import optas.data.TimeSerie;
import optas.data.TimeSerieEnsemble;
import optas.tools.PatchedChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.DateAxis;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.labels.XYToolTipGenerator;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.time.Day;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;
import org.jfree.data.xy.XYDataset;


/**
 *
 * @author Christian Fischer
 */
public class BestPredictionPlot extends MCAT5Plot{
    XYPlot plot1 = new XYPlot();            
    PatchedChartPanel chartPanel1 = null;
        
    public BestPredictionPlot() {
        this.addRequest(new SimpleRequest(JAMS.i18n("SIMULATED_TIMESERIE"),TimeSerie.class));
        this.addRequest(new SimpleRequest(JAMS.i18n("OBSERVED_TIMESERIE"),Measurement.class));
        this.addRequest(new SimpleRequest(JAMS.i18n("Efficiency"),Efficiency.class,1,10));
        
        init();
    }

    private void init(){
        plot1.setDomainAxis(new DateAxis(JAMS.i18n("TIME")));
        plot1.setRangeAxis(new NumberAxis(JAMS.i18n("OUTPUT")));
        
        JFreeChart chart1 = new JFreeChart(plot1);
        chart1.setTitle(JAMS.i18n("BEST_PREDICTION_PLOT"));
        chartPanel1 = new PatchedChartPanel(chart1, true);

        chartPanel1.setMinimumDrawWidth( 0 );
        chartPanel1.setMinimumDrawHeight( 0 );
        chartPanel1.setMaximumDrawWidth( MAXIMUM_WIDTH );
        chartPanel1.setMaximumDrawHeight( MAXIMUM_HEIGHT );
        
        chartPanel1.getChart().getPlot().setBackgroundPaint(Color.white);        
        
        XYLineAndShapeRenderer renderer = new XYLineAndShapeRenderer();

        for (int i=0;i<7;i++){
            renderer.setSeriesLinesVisible(i, true);
            renderer.setSeriesShapesVisible(i, false);
        }

        renderer.setSeriesPaint(0,Color.BLACK);
        renderer.setSeriesPaint(1,Color.BLUE);
        renderer.setSeriesPaint(2,Color.RED);
        renderer.setSeriesPaint(3,Color.YELLOW);
        renderer.setSeriesPaint(4,Color.CYAN);
        renderer.setSeriesPaint(5,Color.GREEN);
        renderer.setSeriesPaint(6,Color.PINK);

        renderer.setStroke(new BasicStroke(1));
        plot1.setRenderer(renderer);
        redraw();
    }

    private class CustomXYToolTipGenerator implements XYToolTipGenerator{

        int index=0;
        String tooltip = "";
        public CustomXYToolTipGenerator(int index, String tooltip){
            this.tooltip = tooltip;
            this.index = index;
        }
        @Override
        public String generateToolTip(XYDataset xyd, int i, int i1) {
            return tooltip;
        }
        
    }
    @Override
    public void refresh() throws NoDataException {
        if (!this.isRequestFulfilled()) {
            return;
        }
        
        ArrayList<DataSet> p[] = getData(new int[]{0,1,2});
        TimeSerieEnsemble ts = (TimeSerieEnsemble)p[0].get(0);
        Measurement obs = (Measurement)p[1].get(0);
        ArrayList<DataSet> eff = p[2];

        long time_length =  obs.getTimeDomain().getNumberOfTimesteps();

        TimeSeries bestTSDataset[] = new TimeSeries[eff.size()+1];
           
        
        for (int i=0;i<eff.size();i++){
            EfficiencyEnsemble effEnsemble = (EfficiencyEnsemble)eff.get(i);
            int argbest = effEnsemble.findArgBest();

            bestTSDataset[i] = new TimeSeries("opt-"+eff.get(i).name);
            for (int j=0;j<time_length;j++){
                Day d = new Day(obs.getTime((int) j));
                bestTSDataset[i].add(d,ts.get(j, argbest));                
            }
            String tooltip = "<html><body>";
            for (int j=0;j<eff.size();j++){
                EfficiencyEnsemble effEnsemble2 = (EfficiencyEnsemble)eff.get(j);
                tooltip += effEnsemble2.getName() + ":" + String.format("%.2f", effEnsemble2.getValue(argbest));
                if (j < eff.size()-1){
                    tooltip += "<br>";
                }
            }
            tooltip+="</body></html>";
                        
            plot1.getRenderer().setSeriesToolTipGenerator(i, new CustomXYToolTipGenerator(i, tooltip) );
        }
        bestTSDataset[eff.size()] = new TimeSeries(JAMS.i18n("OBSERVED"));
        for (int j=0;j<time_length;j++){  
            Day d = new Day(obs.getTime((int) j));
            bestTSDataset[eff.size()].add(d,obs.getValue(j));
        }                      
                 
        TimeSeriesCollection bestSeries = new TimeSeriesCollection();
        for (TimeSeries bestTSDataset1 : bestTSDataset) {
            bestSeries.addSeries(bestTSDataset1);
        }                                                                              
        plot1.setDataset(bestSeries);        
                           
        if (plot1.getRangeAxis() != null) {
            plot1.getRangeAxis().setAutoRange(true);
        }        
        if (plot1.getDomainAxis()!= null) {
            plot1.getDomainAxis().setAutoRange(true);
        }
    }

    @Override
    public JPanel getPanel() {
        return chartPanel1;
    }
}
