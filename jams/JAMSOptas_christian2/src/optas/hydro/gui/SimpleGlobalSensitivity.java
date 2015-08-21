/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package optas.hydro.gui;

import java.util.ArrayList;
import optas.data.api.DataSet;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import optas.gui.MCAT5.DottyPlot;
import optas.hydro.calculations.SlopeCalculations;
import optas.data.ensemble.DefaultEfficiencyEnsemble;
import optas.data.ensemble.DefaultSimpleEnsemble;



/**
 *
 * @author Christian Fischer
 */
public class SimpleGlobalSensitivity extends DottyPlot{                        
        public SimpleGlobalSensitivity(){
            super();
        }
        
        @Override
        public void refresh() throws NoDataException{
            if (!this.isRequestFulfilled())
                return;

            ArrayList<DataSet> p[] = getData(new int[]{0,1});
            DefaultSimpleEnsemble p1 = (DefaultSimpleEnsemble)p[0].get(0);
            DefaultSimpleEnsemble p2 = (DefaultSimpleEnsemble)p[1].get(0);
                        
            plot.setDomainAxis(new NumberAxis(p1.getName()));
            plot.setRangeAxis(new NumberAxis("slope"));

            XYSeries dataset[] = SlopeCalculations.calculateDerivative((DefaultEfficiencyEnsemble)p2,this.getDataSource());
            int c=-1;
            for (int i=0;i<dataset.length;i++){
                if (dataset[i].getDescription().equals(p1.getName())){
                    c=i;
                    break;
                }
            }
            if (c==-1)
                return;

            plot.setDataset(0,new XYSeriesCollection(dataset[c]));

            if (plot.getRangeAxis() != null)    plot.getRangeAxis().setAutoRange(true);
            if (plot.getDomainAxis() != null)   plot.getDomainAxis().setAutoRange(true);
        }
    }
