/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package optas.data.ensemble.api;

import jams.data.Attribute;
import java.util.Date;
import optas.data.api.DataView;
import optas.data.time.api.TimeFilter;
import optas.data.time.api.TimeSerie;

/**
 *
 * @author Christian Fischer - christian.fischer.2@uni-jena.de
 */
public interface TimeSerieEnsemble extends Ensemble<TimeSerie<Double>> {
    
    Date getDate(int timeIndex);
    Double getValue(int timeIndex, Integer id);

    Ensemble getEnsemble(int timeIndex);    
    TimeSerie<Double> getMax();
    TimeSerie<Double> getMin();
    
    Attribute.TimeInterval getTemporalDomain();
    int getNumberOfTimesteps();
           
    @Override
    TimeSerieEnsemble clone();
    
    @Override
    TimeSerieEnsemble filterIds(DataView<Integer> filter);
    TimeSerieEnsemble filter(TimeFilter f);
    
    @Override
    TimeSerieEnsemble removeIds(Integer ids[]);
    
}
