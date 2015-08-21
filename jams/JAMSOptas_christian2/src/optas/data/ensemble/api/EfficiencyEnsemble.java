/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package optas.data.ensemble.api;

import optas.data.ensemble.NegativeEfficiencyEnsemble;
import optas.data.time.DefaultTimeFilter;
import optas.data.time.api.TimeSerie;
import optas.efficiencies.EfficiencyCalculator;

/**
 *
 * @author Christian Fischer - christian.fischer.2@uni-jena.de
 */
public interface EfficiencyEnsemble extends SimpleEnsemble{
                
    public boolean isPositiveBest();
    
    public int findArgBest();
    public int findArgWorst();
    
    EfficiencyEnsemble transformToLikelihood();    
    NegativeEfficiencyEnsemble calcEfficiency(TimeSerie<Double> obs, TimeSerieEnsemble sim, EfficiencyCalculator c, DefaultTimeFilter filter);
}
