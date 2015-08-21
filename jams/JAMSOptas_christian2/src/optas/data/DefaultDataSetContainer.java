
package optas.data;

import static com.google.common.base.Preconditions.checkNotNull;
import java.util.ArrayList;
import java.util.HashMap;
import optas.data.api.DataSet;
import optas.data.api.DataSetContainer;
import optas.data.api.DataView;
import optas.data.view.ViewFactory;

/**
 *
 * @author chris
 */
public class DefaultDataSetContainer<K,T extends DataSet> extends DefaultDataSet implements DataSetContainer<K,T>{
    static final long serialVersionUID = -9046263815810640999L;
    
    private final ArrayList<T> datasets = new ArrayList<>();
    private final HashMap<Class, ArrayList<T>> map = new HashMap<>();    
       
    public DefaultDataSetContainer(String name){ 
        super(name);
    }

    public DefaultDataSetContainer(DataSetContainer<K,T> obj){ 
        super(obj.getName());
        for (T d : obj.getDataSets()){
            addDataSet(d);
        }
    }
    
    @Override
    public DataView<? extends T> getDataSets(){
        return ViewFactory.createView(datasets);
    }
    
    @Override
    public DataView<? extends T> getDataSets(Class clazz){
        return ViewFactory.createView(this.map.get(clazz));
    }
    
    @Override
    public T getDataSet(K name){
        checkNotNull(name);
        for (T d : datasets){
            if (d.getName().equals(name))
                return d;
        }
        return null;
    }
        
    @Override
    public DefaultDataSetContainer<K,T> addDataSet(T set) {
        checkNotNull(set);
        set.setParent(this);
        this.datasets.add(set);

        if (!this.map.containsKey(set.getClass()))
            this.map.put(set.getClass(), new ArrayList<>());
        this.map.get(set.getClass()).add(set);
        
        return this;
    }

    @Override
    public DefaultDataSetContainer<K,T> removeDataSet(String name) {
        checkNotNull(name);
        for (T d : this.datasets) {
            if (d.getName().equals(name)) {
                this.datasets.remove(d);
                for (ArrayList<T> list : this.map.values()) {
                    if (list.contains(d)){
                        list.remove(d);
                    }
                }
                break;
            }
        }
        return this;
    }
}
