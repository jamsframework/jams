/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package optas.data.ensemble.api;

import optas.data.api.DataSet;
import optas.data.api.DataSetContainer;
import optas.data.api.DataView;


/**
 *
 * @author Christian Fischer - christian.fischer.2@uni-jena.de
 * @param <T>
 */
public interface Ensemble<T> extends DataSet{
    
    public Integer getId(int index);
    public int getSize();    
    public DataView<Integer> ids();
    public DataView<T> values();
    public T getValue(Integer id);

    public Ensemble<T> clone();
    
    public Ensemble<T> filterIds(DataView<Integer> filter);
    public Ensemble<T> removeIds(Integer ids[]);
    public Ensemble<T> getInstance(String name, DataSetContainer parent, DataView<Integer> ids, DataView<T> values);
}
