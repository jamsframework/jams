package reg;

import jams.data.Attribute.TimeInterval;
import java.util.EnumSet;
import java.util.Set;
import optas.hydro.data.DataCollection.DatasetChangeEvent;
import reg.gui.DataCollectionViewDelegate;
import optas.hydro.data.DataCollection;
import optas.hydro.data.Efficiency;
import optas.hydro.data.Measurement;
import optas.hydro.data.Parameter;
import optas.hydro.data.SimpleEnsemble;
import optas.hydro.data.StateVariable;
import optas.hydro.data.TimeSerie;
import optas.hydro.data.TimeSerieEnsemble;
import reg.gui.DataCollectionView;
import reg.gui.DataCollectionView.DataType;

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
            if (Measurement.class.isAssignableFrom(c)) {
                types.add(DataType.MEASUREMENT);
            }else if(TimeSerie.class.isAssignableFrom(c)) {
                types.add(DataType.TIME_SERIES);
            } else if (Efficiency.class.isAssignableFrom(c)) {
                types.add(DataType.OBJECTIVE);
            } else if (StateVariable.class.isAssignableFrom(c)) {
                types.add(DataType.VARIABLE);
            } else if (Parameter.class.isAssignableFrom(c)) {
                types.add(DataType.PARAMETER);
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
            case VARIABLE:      return collection.getDatasets(StateVariable.class).toArray(new String[0]);
            case PARAMETER:     return collection.getDatasets(Parameter.class).toArray(new String[0]);
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

    public void filter(Object item, double low, double high){
        Object o = getItemForIdentifier(item);
        if (o instanceof SimpleEnsemble){
            collection.filter(item.toString(), low, high);
        }
    }

    public void filterPercentil(Object item, double low, double high){
        Object o = getItemForIdentifier(item);
        if (o instanceof SimpleEnsemble){
            collection.filterPercentil(item.toString(), low, high);
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
