/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package optas.data.api;

import optas.core.OPTASException;
import optas.data.time.api.TimeSerie;
import optas.data.time.api.TimeFilter;
import optas.data.time.api.TemporalSpatialDataSet;
import optas.data.ensemble.api.SimpleEnsemble;
import optas.data.ensemble.api.TimeSerieEnsemble;
import jams.data.Attribute.TimeInterval;
import java.io.File;
import java.io.Serializable;
import java.util.*;
import optas.data.DefaultDataCollection;
import optas.data.view.ArrayView;
import optas.optimizer.management.SampleFactory;

/**
 *
 * @author Christian Fischer - christian.fischer.2@uni-jena.de
 */
public interface DataCollection extends DataSet, Serializable, Cloneable {

    static public DataCollection getInstance(String name) {
        return new DefaultDataCollection(name);
    }

    static public DataCollection getInstance(File file) {
        return DefaultDataCollection.createFromFile(file);
    }

    public Collection<ModelRun> getModelRuns();
    public ModelRun getModelRun(Integer id);
    public ArrayView<Integer> getModelRunIds();
    
    public void addDataSet(DataSet s);    
    public void addDataSet(SimpleEnsemble ensemble);
    public void addDataSet(TimeSerieEnsemble ensemble);
    
    public Set<String> getDataSetNames();
    public Class getDatasetClass(String name);
    public Set<String> getDataSetNames(Class datasetType);
    public Set<String> getDataSetNames(Class datasetType, boolean forbidSubTypes);
    
    public <T extends DataSet> T getDataSet(Class<T> clazz, String name);
    
    public void renameDataSet(String oldname, String newname);

    public void removeDataSet(String name);
    
    

    public void addModelRun(ModelRun run) throws OPTASException;

    

    

    public void removeModelRun(Integer id);

    public boolean filter(String e, double low, double high, boolean inverse);

    public boolean filterPercentil(String e, double low, double high, boolean inverse);

    public void commitFilter();

    public void filterTimeDomain(TimeFilter f);

    public void clearTimeDomainFilter();

    public void clearIDFilter();

    public boolean unifyDataCollections(DataCollection dc);

    public void mergeDataCollections(DataCollection dc);

    

    public void setGlobalTimeDomain(TimeInterval interval);

    //this is highly inefficient!!
    // --> update time domain incrementally when new dataset is added?
    public void updateTimeDomain();

    public void addGlobalDataSet(TimeSerie s);

    public void addGlobalDataSet(TemporalSpatialDataSet s);

    public int getSimulationCount();

    public SimpleEnsemble getSimpleEnsemble(String dataset);

    public TimeSerieEnsemble getTimeserieEnsemble(String dataset);

    /**
     * Returns a set of all available data set types within the collection
     * denoted by their respective class names. Recommended for use in
     * conjunction with the getDataSets(Class c) method.
     *
     * @return set of data types
     *
     * @see #getDataSetNames(Class)
     */
    public Set<Class> getDataSetTypes();

    public TimeInterval getTimeDomain();

    public DataCollection clone();

    public void save(File file);

    public void constructSample(SampleFactory f);

    public DataCollection copyToMemory();
    
    //simple dump to save the datacollection NOW
    public void dump(File directory, boolean append);
}
