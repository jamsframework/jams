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
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYDifferenceRenderer;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import optas.gui.MCAT5.MCAT5Plot.SimpleRequest;
import optas.data.DataSet;
import optas.data.Efficiency;
import optas.data.EfficiencyEnsemble;
import optas.data.Measurement;
import optas.data.TimeSerie;
import optas.data.TimeSerieEnsemble;
import optas.tools.PatchedChartPanel;
import org.jfree.chart.axis.DateAxis;
import org.jfree.data.time.Day;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;

/**
 *
 * @author Christian Fischer
 */
public class ParetoOutputUncertainty extends MCAT5Plot {
    XYPlot plot1 = new XYPlot();            
    PatchedChartPanel chartPanel1 = null;
            
    String var_name = null;
        
    public ParetoOutputUncertainty() {
        this.addRequest(new SimpleRequest(JAMS.i18n("SIMULATED_TIMESERIE"), TimeSerie.class));
        this.addRequest(new SimpleRequest(JAMS.i18n("Efficiency"), Efficiency.class,1,10));
        this.addRequest(new SimpleRequest(JAMS.i18n("OBSERVED_TIMESERIE"),Measurement.class));

        init();
    }

    private void init(){
        plot1.setDomainAxis(new DateAxis(JAMS.i18n("TIME")));

        JFreeChart chart1 = new JFreeChart(plot1);
        chart1.setTitle(JAMS.i18n("PARETO_OUTPUT_UNCERTAINTY"));

        chartPanel1 = new PatchedChartPanel(chart1, true);
        chartPanel1.setMinimumDrawWidth( 0 );
        chartPanel1.setMinimumDrawHeight( 0 );
        chartPanel1.setMaximumDrawWidth( MAXIMUM_WIDTH );
        chartPanel1.setMaximumDrawHeight( MAXIMUM_HEIGHT );
        
        XYDifferenceRenderer renderer1 = new XYDifferenceRenderer(Color.LIGHT_GRAY,Color.LIGHT_GRAY,false);
        XYDifferenceRenderer renderer2 = new XYDifferenceRenderer(Color.CYAN,Color.CYAN,false);
        XYLineAndShapeRenderer renderer3 = new XYLineAndShapeRenderer(); 
        
        renderer1.setPaint(Color.LIGHT_GRAY);        
        renderer1.setSeriesFillPaint(0, Color.LIGHT_GRAY);
        
        renderer2.setPaint(Color.CYAN);
        renderer3.setBaseLinesVisible(true);
        renderer3.setBaseShapesVisible(false);
        renderer3.setOutlinePaint(Color.BLACK);
        renderer3.setPaint(Color.BLACK);
        renderer3.setStroke(new BasicStroke(1));
                                       
        plot1.setRenderer(0, renderer3);
        plot1.setRenderer(1, renderer2);
        plot1.setRenderer(2, renderer1);
        
        chart1.getPlot().setBackgroundPaint(Color.white);
        chart1.getXYPlot().setDomainGridlinePaint(Color.black);
        
        redraw();
    }
        
    boolean isParetoOptimal(double eff_actual[],EfficiencyEnsemble eff_set[]){
        int MC_PARAM = eff_set[0].getSize();
        for (int i=0;i<MC_PARAM;i++){
            boolean dominated = true;
            for (int j=0;j<eff_actual.length;j++){
                if (eff_set[j].isPositiveBest()){
                    if (eff_set[j].getValue(eff_set[j].getId(i))<=eff_actual[j]){
                        dominated = false;
                        break;
                    }
                }else{
                    if (eff_set[j].getValue(eff_set[j].getId(i))>=eff_actual[j]){
                        dominated = false;
                        break;
                    }
                }
            }
            if (dominated)
                return false;
        }
        return true;
    }
    
    double[][] getMinMaxParetoTS(TimeSerieEnsemble data, EfficiencyEnsemble eff[]){
        double minMaxOptimalTS[][] = new double[2][data.getTimesteps()];
                
        for (int i=0;i<data.getTimesteps();i++){
            minMaxOptimalTS[0][i] = Double.POSITIVE_INFINITY;
            minMaxOptimalTS[1][i] = Double.NEGATIVE_INFINITY;
        }
        
        for (int i=0;i<data.getSize();i++){
            double actualEffSet[] = new double[eff.length];
            for (int j=0;j<eff.length;j++)
                actualEffSet[j] = eff[j].getValue(eff[j].getId(i));
            if (isParetoOptimal(actualEffSet,eff)){
                for (int t=0;t<data.getTimesteps();t++){
                    minMaxOptimalTS[0][t] = Math.min(minMaxOptimalTS[0][t],data.get(t, data.getId(i)) );
                    minMaxOptimalTS[1][t] = Math.max(minMaxOptimalTS[1][t],data.get(t, data.getId(i)) );
                }
            }
        }
        return minMaxOptimalTS;
    }
    
    public void refresh() throws NoDataException {
        if (!this.isRequestFulfilled())
            return;

        ArrayList<DataSet> p[] = getData(new int[]{0,1,2});
        TimeSerieEnsemble ts = (TimeSerieEnsemble)p[0].get(0);
        ArrayList<DataSet>  dataInEff   = (ArrayList<DataSet>)p[1];
        Measurement obs = (Measurement) p[2].get(0);


        EfficiencyEnsemble eff[] = new EfficiencyEnsemble[dataInEff.size()];
        for (int i=0;i<eff.length;i++)
            eff[i] = (EfficiencyEnsemble)dataInEff.get(i);

        plot1.setRangeAxis(new NumberAxis(ts.name));

        int time_length = ts.getTimesteps();
        
        TimeSerie maxTS = ts.getMax();
        TimeSerie minTS = ts.getMin();

        double minMaxOptimalTS[][] = getMinMaxParetoTS(ts,eff);
                        
        TimeSeries minTSDataset = new TimeSeries(JAMS.i18n("MINIMAL_VALUE_IN_DATASET"));
        TimeSeries maxTSDataset = new TimeSeries(JAMS.i18n("MAXIMAL_VALUE_IN_DATASET"));
        
        TimeSeries minTSDataset_pareto = new TimeSeries(JAMS.i18n("MINIMAL_VALUE_IN_PARETO_SET"));
        TimeSeries maxTSDataset_pareto = new TimeSeries(JAMS.i18n("MAXIMAL_VALUE_IN_PARETO_SET"));
                
        TimeSeries observation = new TimeSeries(obs.name);
    
        for (int i=0;i<time_length;i++){
            Day d = new Day(ts.getDate((int) i));
            minTSDataset.add(d,minTS.getValue(i));
            maxTSDataset.add(d,maxTS.getValue(i));
            
            minTSDataset_pareto.add(d,minMaxOptimalTS[0][i]);
            maxTSDataset_pareto.add(d,minMaxOptimalTS[1][i]);
            
            observation.add(d,obs.getValue(i));
        }
        
        TimeSeriesCollection dataInterval = new TimeSeriesCollection();
        TimeSeriesCollection dataInterval_pareto = new TimeSeriesCollection();
        
        dataInterval.addSeries(minTSDataset);
        dataInterval.addSeries(maxTSDataset);
                
        dataInterval_pareto.addSeries(minTSDataset_pareto);
        dataInterval_pareto.addSeries(maxTSDataset_pareto);
                                        
        plot1.setDataset(2, dataInterval);
        plot1.setDataset(1, dataInterval_pareto);
        plot1.setDataset(0, new TimeSeriesCollection(observation));
        
        if (plot1.getRangeAxis() != null) {
            plot1.getRangeAxis().setAutoRange(true);
        }
        if (plot1.getDomainAxis() != null) {
            plot1.getDomainAxis().setAutoRange(true);
        }
    }

    public JPanel getPanel() {
        return chartPanel1;
    }
}
