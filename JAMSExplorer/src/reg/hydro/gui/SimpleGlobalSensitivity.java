/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package reg.hydro.gui;

import org.jfree.chart.axis.NumberAxis;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import reg.gui.MCAT5.DottyPlot;
import reg.hydro.calculations.SlopeCalculations;
import reg.hydro.data.EfficiencyEnsemble;
import reg.hydro.data.SimpleEnsemble;



/**
 *
 * @author Christian Fischer
 */
public class SimpleGlobalSensitivity extends DottyPlot{                        
        public SimpleGlobalSensitivity(){
            super();
        }
        
        @Override
        public void refresh(){
            if (!this.isRequestFulfilled())
                return;

            SimpleEnsemble  p1 = (SimpleEnsemble)getData(0),
                            p2 = (SimpleEnsemble)getData(1);

            plot.setDomainAxis(new NumberAxis(p1.getName()));
            plot.setRangeAxis(new NumberAxis("slope"));

            XYSeries dataset[] = SlopeCalculations.calculateDerivative((EfficiencyEnsemble)p2,this.getDataSource());
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
