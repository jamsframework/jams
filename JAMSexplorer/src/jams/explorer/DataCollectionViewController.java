package jams.explorer;

import jams.data.Attribute.TimeInterval;
import java.util.EnumSet;
import java.util.Set;
import optas.data.DataCollection.DatasetChangeEvent;
import jams.explorer.gui.DataCollectionViewDelegate;
import optas.data.DataCollection;
import optas.data.Efficiency;
import optas.data.Measurement;
import optas.data.Parameter;
import optas.data.SimpleEnsemble;
import optas.data.StateVariable;
import optas.data.TimeSerie;
import optas.data.TimeSerieEnsemble;
import jams.explorer.gui.DataCollectionView;
import jams.explorer.gui.DataCollectionView.DataType;

public class DataCollectionViewController implements DataCollectionViewDelegate {
    private DataCollection collection = null;
    private DataCollectionView view = null;
    
    public DataCollectionViewController(DataCollection collection) {
        this.collection = collection;
        collection.addChangeListener(new DataCollection.DatasetChangeListener() {

            public void datasetChanged(DatasetChangeEvent dc) {
                view.refreshView();
            }
        });
        this.view = new DataCollectionView(this);
    }

    public DataCollectionView getView(){
        return view;
    }
    public DataCollection getDataCollection(){
        return collection;
    }
       
    @Override
    public DataType[] getAvailableDataTypes() {
        Set<Class> classes = collection.getDataSetTypes();
        EnumSet<DataType> types = EnumSet.noneOf(DataType.class);

        for (Class c : classes) {
            //the order is crucial
            if (Measurement.class.isAssignableFrom(c)) { //order is crucial here
                types.add(DataType.MEASUREMENT);
            }else if(TimeSerie.class.isAssignableFrom(c)) {
                types.add(DataType.TIME_SERIES);
            } else if (Efficiency.class.isAssignableFrom(c)) {
                types.add(DataType.OBJECTIVE);
            } else if (Parameter.class.isAssignableFrom(c)) {
                types.add(DataType.PARAMETER);
            } else if (StateVariable.class.isAssignableFrom(c)) {
                types.add(DataType.VARIABLE);
            } 
        }
        return types.toArray(new DataType[types.size()]);
    }

    @Override
    public String[] getItemIdentifiersForDataType(DataType type) {
        switch (type) {
            case TIME_SERIES:   return collection.getDatasets(TimeSerie.class).toArray(new String[0]);
            case MEASUREMENT:   return collection.getDatasets(Measurement.class).toArray(new String[0]);
            case OBJECTIVE:     return collection.getDatasets(Efficiency.class).toArray(new String[0]);            
            case PARAMETER:     return collection.getDatasets(Parameter.class).toArray(new String[0]);
            case VARIABLE:      return collection.getDatasets(StateVariable.class).toArray(new String[0]);
            default:            return new String[0];
        }
    }

    @Override
    public TimeInterval getTimeInterval(Object item) {
        //return ((TimeSerieEnsemble) collection.getDataSet((String) item)).getTimeInterval();
        return collection.getTimeDomain();
    }

    @Override
    public boolean hasTimeInterval(Object item) {
        return TimeSerie.class.isAssignableFrom(collection.getDatasetClass((String) item));
    }

    @Override
    public boolean isMultirun(Object item) {
        if (Measurement.class.isAssignableFrom(collection.getDatasetClass((String) item)))
            return false;
        return true;
    }

    @Override
    public Integer[] getSimulationIDs() {
        return this.collection.getModelrunIds();
    }

    @Override
    public void itemIsBeingDisplayed(Object item) {
    }

    public void filter(Object item, double low, double high, boolean inverse){
        Object o = getItemForIdentifier(item);
        if (o instanceof SimpleEnsemble){
            collection.filter(item.toString(), low, high, inverse);
        }
    }

    public void filterPercentil(Object item, double low, double high, boolean inverse){
        Object o = getItemForIdentifier(item);
        if (o instanceof SimpleEnsemble){
            collection.filterPercentil(item.toString(), low, high, inverse);
        }
    }

    public void clearTimeFilter(){
        collection.clearTimeDomainFilter();
    }

    public void clearIDFilter(){
        collection.clearIDFilter();
    }

    public void commitFilter(){
        collection.commitFilter();
    }

    @Override
    public Object getItemForIdentifier(Object identifier) {
        return collection.getDataSet((String) identifier);
    }
}
