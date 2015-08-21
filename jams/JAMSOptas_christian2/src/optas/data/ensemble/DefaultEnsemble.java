/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package optas.data.ensemble;

import static com.google.common.base.Preconditions.checkNotNull;
import java.util.Arrays;
import java.util.TreeSet;
import optas.data.DefaultMapDataSet;
import optas.data.api.DataSetContainer;
import optas.data.api.DataView;
import optas.data.ensemble.api.Ensemble;
import optas.data.view.ViewFactory;

/**
 *
 * @author chris
 */
public class DefaultEnsemble<T> extends DefaultMapDataSet<Integer, T> implements Ensemble<T>{

    public DefaultEnsemble(String name, DataSetContainer parent, DataView<Integer> ids, DataView<T> values){
        super(name, parent, ids, values);
    }
            
    public DefaultEnsemble(Ensemble e){
        super(e.getName(), e.getParent(), e.ids(), e.values());
        //copy base dataset attributes
        for (String key : e.getProperties()){
            this.setProperty(key, e.getProperty(key));
        }
    }

    @Override
    public Ensemble<T> clone(){
        return new DefaultEnsemble(this);
    }
    
    @Override
    public Integer getId(int index){
        return this.getKey(index);
    }
   
    @Override
    public DataView<Integer> ids(){
        return keys();
    }
        
    protected int[] getIndices(DataView<Integer> ids){
        int indicies[] = new int[ids.getSize()];
        for (int i=0;i<ids.getSize();i++){
            indicies[i] = getIndex(ids.getValue(i));
        }
        return indicies;
    }
    
    @Override
    public Ensemble<T> filterIds(DataView<Integer> ids){
        checkNotNull(ids, "Ids must not be null!");
        
        int indices[] = getIndices(ids);
        
        DataView<Integer> view = ViewFactory.createView(indices);
        return getInstance(getName(), getParent(), keys().subset(view), values().subset(view)); 
    }

    @Override
    public Ensemble<T> removeIds(Integer ids[]){
        checkNotNull(ids, "Ids must not be null!");
        
        TreeSet<Integer> set = new TreeSet<>();
        for (Integer id : ids()){
            set.add(id);
        }        
        set.removeAll(Arrays.asList(ids));
        
        int indices[] = getIndices(ViewFactory.createView(set.toArray(new Integer[0])));
        
        DataView<Integer> view = ViewFactory.createView(indices);        
        return getInstance(getName(), getParent(), keys().subset(view), values().subset(view)); 
    }
    
    @Override
    public Ensemble<T> getInstance(String name, DataSetContainer parent, DataView<Integer> ids, DataView<T> values){
        return new DefaultEnsemble<>(name, parent, ids, values);
    }
}
