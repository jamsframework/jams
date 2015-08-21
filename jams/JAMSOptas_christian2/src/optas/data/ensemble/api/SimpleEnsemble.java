/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package optas.data.ensemble.api;

import optas.data.SimpleDataSet;
import optas.data.api.DataSetContainer;
import optas.data.api.DataView;
import optas.data.ensemble.DefaultSimpleEnsemble;

/**
 *
 * @author Christian Fischer - christian.fischer.2@uni-jena.de
 * @param <T>
 */
public interface SimpleEnsemble extends Ensemble<Double>{
    
    public int findArgMin();

    public int findArgMax();

    public Double getMin();

    public Double getMax();

    public Integer[] sort();

    public Integer[] sort(boolean ascending);

    @Override
    public SimpleEnsemble getInstance(String name, DataSetContainer parent, DataView<Integer> ids, DataView<Double> values);
    
    @Override
    public SimpleEnsemble clone();
    
    @Override
    public SimpleEnsemble filterIds(DataView<Integer> filter);
    
    @Override
    public SimpleEnsemble removeIds(Integer ids[]);
}
