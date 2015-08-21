/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package optas.data.ensemble;

import com.google.common.base.Preconditions;
import optas.data.SimpleDataSet;
import optas.data.api.DataSetContainer;
import optas.data.api.DataView;
import optas.data.ensemble.api.EfficiencyEnsemble;
import optas.data.ensemble.api.SimpleEnsemble;
import optas.data.time.DefaultTimeFilter;
import optas.data.time.api.*;
import optas.data.ensemble.api.*;
import optas.data.time.TimeFilterFactory;
import optas.data.view.ViewFactory;
import optas.efficiencies.*;
import org.apache.commons.lang3.ArrayUtils;

/**
 *
 * @author chris
 */
public class DefaultEfficiencyEnsemble extends DefaultSimpleEnsemble implements EfficiencyEnsemble {

    boolean isPostiveBest = true;
    
    double rangeMin = Double.NEGATIVE_INFINITY, rangeMax = Double.POSITIVE_INFINITY;

    public DefaultEfficiencyEnsemble(EfficiencyEnsemble e){
        super(e);
        this.isPostiveBest = e.isPositiveBest();        
    }
    
    public DefaultEfficiencyEnsemble(SimpleEnsemble e, boolean isPostiveBest){
        super(e);
        this.isPostiveBest = isPostiveBest;
    }
    
    public DefaultEfficiencyEnsemble(String name, DataSetContainer parent, DataView<Integer> ids, DataView<Double> values){
        super(name, parent, ids, values);
    }
    
    @Override
    public NegativeEfficiencyEnsemble calcEfficiency(TimeSerie<Double> obs, TimeSerieEnsemble sim, EfficiencyCalculator c, DefaultTimeFilter filter) {
        //check preconditions
        Preconditions.checkNotNull(obs, "Observed Timeserie must not be null!");
        Preconditions.checkNotNull(sim, "Simulated Timeserie Ensemble must not be null!");
        Preconditions.checkNotNull(c, "EfficiencyCalculator must not be null!");
        
        Preconditions.checkArgument(obs.getTemporalDomain().equals(sim.getTemporalDomain()), 
                "Can't calculate efficiency of datasets with different temporal domain. The domain of %s is %s where the domain of %s is %s.", 
                obs.getName(), obs.getTemporalDomain(), 
                sim.getName(), sim.getTemporalDomain());
        
        Preconditions.checkArgument(obs.getNumberOfTimesteps()==sim.getNumberOfTimesteps(), "Can't calculate efficiency of datasets with different number of timesteps. %s has %s timesteps where %s has %s timesteps.",
                obs.getName(), obs.getNumberOfTimesteps(), 
                sim.getName(), sim.getNumberOfTimesteps());
        
        if (filter == null){
            filter = TimeFilterFactory.getDummyFilter();
        }
        
        //do calculation
        double[] obsValues = ArrayUtils.toPrimitive(ViewFactory.toArray(obs.filter(filter).values()));
        Double[] effValues = new Double[sim.getSize()];
        
        int cursor = 0;
        for (Integer id : sim.ids()){
            double[] simValues = ArrayUtils.toPrimitive(ViewFactory.toArray(
                    sim.getValue(id).filter(filter).values()));
            
            effValues[cursor++] = c.calcNormative(obsValues, simValues);
        }
    
        DataView<Double> effView = ViewFactory.createView(effValues);
        NegativeEfficiencyEnsemble eff = new NegativeEfficiencyEnsemble(c.toString(), parent, sim.ids(), effView);
        eff.setParent(getParent());
        return eff;
    }
    
    @Override
    public DefaultEfficiencyEnsemble transformToLikelihood() {
        double[] likelihood = new double[getSize()];

        for (int i=0;i<getSize();i++){
            likelihood[i] = this.getValue(i);
        }
        
        if (this.isPostiveBest) {
            double Lmin = this.getMin();

            for (int i = 0; i < getSize(); i++) {
                likelihood[i] = likelihood[i] - Lmin;
            }

        } else {
            double Lmin = 1.0 - this.getMax();

            for (int i = 0; i < getSize(); i++) {
                likelihood[i] = (1.0 - likelihood[i]) - Lmin;
            }
        }
        
        double sum = 0;
        for (int i = 0; i < getSize(); i++) {
            sum += likelihood[i];
        }

        for (int i = 0; i < getSize(); i++) {
            likelihood[i] /= sum;
        }

        PositiveEfficiencyEnsemble eff = new PositiveEfficiencyEnsemble("Likelihood", parent, ids(), ViewFactory.createView(likelihood));
        eff.setParent(getParent());
        
        return eff;
    }
    
    @Override
    public DefaultEfficiencyEnsemble clone(){
        return new DefaultEfficiencyEnsemble(this);        
    }

    @Override
    public int findArgBest() {
        if (isPostiveBest) {
            return super.findArgMax();
        }
        return super.findArgMin();
    }

    @Override
    public int findArgWorst() {
        if (isPostiveBest) {
            return super.findArgMin();
        }
        return super.findArgMax();
    }
    
    @Override
    public Integer[] sort() {
        return sort(!this.isPostiveBest);
    }

    @Override
    public boolean isPositiveBest() {
        return this.isPostiveBest;
    }    
}
