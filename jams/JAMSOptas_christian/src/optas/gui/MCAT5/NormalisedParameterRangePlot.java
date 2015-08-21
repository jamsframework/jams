/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package optas.gui.MCAT5;

import jams.JAMS;
import java.awt.Color;
import java.util.ArrayList;
import javax.swing.JPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYDifferenceRenderer;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
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
public class NormalisedParameterRangePlot extends MCAT5Plot{
    XYPlot plot1 = new XYPlot();            
    PatchedChartPanel chartPanel1 = null;
                    
    public NormalisedParameterRangePlot() {
        this.addRequest(new SimpleRequest(JAMS.i18n("PARAMETER"),Parameter.class,1,10));
        this.addRequest(new SimpleRequest(JAMS.i18n("Efficiency"),Efficiency.class,1,10));

        init();
    }
    private void init(){
        XYDifferenceRenderer renderer1 = new XYDifferenceRenderer(Color.LIGHT_GRAY,Color.LIGHT_GRAY,false);                
        renderer1.setPaint(Color.BLACK);                                                               
        plot1.setRenderer(0, renderer1);
        
        plot1.setDomainAxis(new NumberAxis(JAMS.i18n("PARAMETER")));
        plot1.setRangeAxis(new NumberAxis(JAMS.i18n("NORMALISED_RANGE")));

        JFreeChart chart1 = new JFreeChart(plot1);
        chart1.setTitle(JAMS.i18n("NORMALISED_PARAMETER_RANGE_PLOT"));
        chart1.removeLegend();
        chartPanel1 = new PatchedChartPanel(chart1, true);
                      
        chartPanel1.setMinimumDrawWidth( 0 );
        chartPanel1.setMinimumDrawHeight( 0 );
        chartPanel1.setMaximumDrawWidth( MAXIMUM_WIDTH );
        chartPanel1.setMaximumDrawHeight( MAXIMUM_HEIGHT );
        
        redraw();
    }
        
    boolean isParetoOptimal(double eff_actual[],EfficiencyEnsemble eff_set[]){
        int MC_PARAM = eff_set[0].getSize();
        for (int i=0;i<MC_PARAM;i++){
            boolean dominated = true;
            for (int j=0;j<eff_actual.length;j++){
                if (eff_set[j].isPositiveBest()){
                    if (eff_set[j].getValue(eff_set[j].getId(i))>=eff_actual[j]){
                        dominated = false;
                        break;
                    }
                }else{
                    if (eff_set[j].getValue(eff_set[j].getId(i))<=eff_actual[j]){
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
        
    double[][] getMinMaxParetoTS(SimpleEnsemble[]data, EfficiencyEnsemble eff[]){
        double minMaxOptimalTS[][] = new double[2][data.length];
                
        for (int i=0;i<data.length;i++){
            minMaxOptimalTS[0][i] = Double.POSITIVE_INFINITY;
            minMaxOptimalTS[1][i] = Double.NEGATIVE_INFINITY;
        }
        
        for (int i=0;i<data[0].getSize();i++){
            double actualEffSet[] = new double[eff.length];
            for (int j=0;j<eff.length;j++)
                actualEffSet[j] = eff[j].getValue(eff[j].getId(i));
            if (isParetoOptimal(actualEffSet,eff)){
                for (int t=0;t<data.length;t++){
                    minMaxOptimalTS[0][t] = Math.min(minMaxOptimalTS[0][t],data[t].getValue(data[t].getId(i)) );
                    minMaxOptimalTS[1][t] = Math.max(minMaxOptimalTS[1][t],data[t].getValue(data[t].getId(i)) );
                }
            }
        }
        return minMaxOptimalTS;
    }
    
    public void refresh() throws NoDataException {
        if (!this.isRequestFulfilled())
            return;

        ArrayList<DataSet> p[] = getData(new int[]{0,1});
        ArrayList<DataSet>  dataInParam = p[0];
        ArrayList<DataSet>  dataInEff = p[1];

        SimpleEnsemble[] params = new SimpleEnsemble[dataInParam.size()];
        EfficiencyEnsemble[] eff = new EfficiencyEnsemble[dataInEff.size()];

        for (int i=0;i<dataInEff.size();i++)
            eff[i] = (EfficiencyEnsemble)dataInEff.get(i);

        for (int i=0;i<dataInParam.size();i++)
            params[i] = (SimpleEnsemble)dataInParam.get(i);


        double minMaxparetoOptimal[][] = getMinMaxParetoTS(params,eff);
                                                                 
        XYSeries minTSDataset_pareto = new XYSeries(JAMS.i18n("MINIMAL_PARETO_OPTIMAL_VALUE"));
        XYSeries maxTSDataset_pareto = new XYSeries(JAMS.i18n("MAXIMAL_PARETO_OPTIMAL_VALUE"));

        for (int i=0;i<params.length;i++){
            minTSDataset_pareto.add(i,minMaxparetoOptimal[0][i]);
            maxTSDataset_pareto.add(i,minMaxparetoOptimal[1][i]);            
        }
                        
        XYSeriesCollection dataInterval_pareto = new XYSeriesCollection();
        dataInterval_pareto.addSeries(minTSDataset_pareto);
        dataInterval_pareto.addSeries(maxTSDataset_pareto);
                        
        plot1.setDataset(0, dataInterval_pareto);

        if (plot1.getRangeAxis() != null) plot1.getRangeAxis().setAutoRange(true);
        if (plot1.getDomainAxis() != null)plot1.getDomainAxis().setAutoRange(true);

    }

    public JPanel getPanel() {
        return chartPanel1;
    }
}
