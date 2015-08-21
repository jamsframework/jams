
package optas.data;

import static com.google.common.base.Preconditions.checkNotNull;
import optas.data.api.DataSet;
import optas.data.api.ModelRun;

/**
 *
 * @author Christian Fischer - christian.fischer.2@uni-jena.de
 */
public class DefaultModelRun extends DefaultDataSetContainer<String, DataSet> implements ModelRun{    
    Integer id;

    public DefaultModelRun(String name, Integer id){        
        super(name);
        checkNotNull(id);
        this.id = id;
    }
    
    public DefaultModelRun(ModelRun obj, Integer id){
        super(obj);
        checkNotNull(id);
        this.id = id;
    }

    @Override
    public Integer getId(){
        return id;
    }
            
    @Override
    public DefaultModelRun addDataSet(DataSet set) {    
        super.addDataSet(set);
        return this;
    }

    @Override
    public DefaultModelRun removeDataSet(String name) {
        super.removeDataSet(name);
        return this;
    }
}
