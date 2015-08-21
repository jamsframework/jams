
package optas.data.api;

import optas.data.DefaultModelRun;

/**
 *
 * @author Christian Fischer - christian.fischer.2@uni-jena.de
 */
public interface ModelRun extends DataSetContainer<String, DataSet>{    
    
    static public ModelRun getInstance(String name, Integer id){        
        return new DefaultModelRun(name, id);
    }
    
    static public ModelRun getInstance(ModelRun obj, Integer id){        
        return new DefaultModelRun(obj, id);
    }
        
    public Integer getId();    
    @Override
    public ModelRun addDataSet(DataSet set);
    
    @Override
    public ModelRun removeDataSet(String name);
}
