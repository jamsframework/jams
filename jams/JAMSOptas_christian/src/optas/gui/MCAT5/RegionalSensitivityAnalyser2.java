/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package optas.gui.MCAT5;

import jams.JAMS;
import java.awt.Color;
import java.util.ArrayList;
import java.util.Arrays;
import javax.swing.JPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import optas.gui.MCAT5.MCAT5Plot.SimpleRequest;
import optas.gui.MCAT5.MCAT5Toolbar.ArrayComparator;
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
@SuppressWarnings({"unchecked"})
public class RegionalSensitivityAnalyser2 extends MCAT5Plot{

    XYPlot plot = new XYPlot();    
    PatchedChartPanel chartPanel = null;        
    
    final int GROUPS = 10;
      
    public RegionalSensitivityAnalyser2() {
        this.addRequest(new SimpleRequest(JAMS.i18n("PARAMETER"), Parameter.class));
        this.addRequest(new SimpleRequest(JAMS.i18n("Efficiency"), Efficiency.class,1,10));

        init();
    }

    private void init(){
        plot.setRangeAxis(new NumberAxis(JAMS.i18n("LIKELIHOOD")));

        JFreeChart chart = new JFreeChart(plot);
        chart.setTitle(JAMS.i18n("REGIONAL_SENSITIVITY_ANALYSIS_II"));
        chartPanel = new PatchedChartPanel(chart, true);
        chartPanel.setMinimumDrawWidth( 0 );
        chartPanel.setMinimumDrawHeight( 0 );
        chartPanel.setMaximumDrawWidth( MAXIMUM_WIDTH );
        chartPanel.setMaximumDrawHeight( MAXIMUM_HEIGHT );
        
        chart.getPlot().setBackgroundPaint(Color.white);
        chart.getXYPlot().setDomainGridlinePaint(Color.black);
        
        redraw();
    }
        
    public double[][] sortbyEff(double data[],double likelihood[]) {
        int n = data.length;
        double tmp_data[][] = new double[n][2];

        for (int i = 0; i < n; i++) {
            tmp_data[i][0] = data[i];
            tmp_data[i][1] = likelihood[i];
        }

        Arrays.sort(tmp_data, new ArrayComparator(1, true));
        return tmp_data;
    }
        
    @Override
    public void refresh() throws NoDataException {
        if (!this.isRequestFulfilled()){
            return;
        }

        ArrayList<DataSet> p[] = getData(new int[]{0,1});
        SimpleEnsemble  param = (SimpleEnsemble)p[0].get(0);
        ArrayList<DataSet> effIn = ( ArrayList<DataSet>)p[1];

        EfficiencyEnsemble eff[] = new EfficiencyEnsemble[effIn.size()];
        for (int i=0;i<effIn.size();i++){
            eff[i] = (EfficiencyEnsemble)effIn.get(i);
        }

        XYLineAndShapeRenderer renderer[] = new XYLineAndShapeRenderer[GROUPS];
        for (int i=0;i<eff.length;i++){
            renderer[i] = new XYLineAndShapeRenderer();
            renderer[i].setBaseShapesVisible(false);
            int c = (int)(i*255.0/eff.length);
            renderer[i].setSeriesPaint(0, new Color(255-c,0,c));

            plot.setRenderer(i, renderer[i]);
        }
        plot.setDomainAxis(new NumberAxis(param.name));

        int numberOfObjFct = eff.length;
        XYSeries dataset[] = new XYSeries[numberOfObjFct];
        //double sorted_data[][][] = new double[numberOfObjFct][][];
                  
        for (int i=0;i<plot.getDatasetCount();i++){
            plot.setDataset(i, null);
        }
        for (int i=0;i<numberOfObjFct;i++){
            dataset[i] = new XYSeries(eff[i].name);
            EfficiencyEnsemble likelihood = eff[i].CalculateLikelihood();
            Integer sortedIds[] = likelihood.sort();
            //sorted_data[i] = sortbyEff(data.set,Efficiencies.CalculateLikelihood(this.eff[i].set));
            ArrayList<Integer> boxes[] = new ArrayList[numberOfObjFct];
            
            for (int j=0;j<boxes.length;j++)
                boxes[j] = new ArrayList<Integer>();
                        
            double range_min = param.getMin();
            double range_max = param.getMax();
            
            int index = 0,counter=0;
            do{
                //index = (int) Math.round((sorted_data[i][counter][1] - min) / (max - min) * 9);
                index = (int) (((double) counter / (double) param.getSize()) * (GROUPS));
                //index = (int)((likelihood.getValue(sortedIds[counter]) - min)/(max - min) * GROUPS);
                if (index == GROUPS)
                    index = GROUPS - 1;
                boxes[i].add(sortedIds[counter]);
                //boxes[i].add(sorted_data[i][counter]);
                //range_max = Math.max(sorted_data[i][counter][0],range_max);
                //range_min = Math.min(sorted_data[i][counter][0],range_min);
                counter++;
            }while (index == 0);
                        
            double box_data[] = new double[boxes[i].size()];
            for (int j=0;j<boxes[i].size();j++){
                box_data[j] = param.getValue(boxes[i].get(j));
            }
            Arrays.sort(box_data);
                         
            dataset[i].add(range_min,0.0);
            for (int j=0;j<box_data.length;j++){
                dataset[i].add(box_data[j],(double)j / (double)box_data.length);                
            }
            dataset[i].add(range_max,1.0);
            
            plot.setDataset(i, new XYSeriesCollection(dataset[i]));            
        }   
        
        if (plot.getRangeAxis() != null)    plot.getRangeAxis().setAutoRange(true);
        if (plot.getDomainAxis() != null)   plot.getDomainAxis().setAutoRange(true);

    }

    public JPanel getPanel() {
        return chartPanel;
    }
}
