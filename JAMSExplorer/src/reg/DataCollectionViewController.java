package reg;

import jams.data.Attribute.TimeInterval;
import java.util.Set;
import optas.hydro.data.DataCollection.DatasetChangeEvent;
import reg.gui.DataCollectionView.DataType;
import reg.gui.DataCollectionViewDelegate;
import optas.hydro.data.DataCollection;
import optas.hydro.data.Efficiency;
import optas.hydro.data.Measurement;
import optas.hydro.data.Parameter;
import optas.hydro.data.StateVariable;
import optas.hydro.data.TimeSerie;
import optas.hydro.data.TimeSerieEnsemble;
import reg.gui.DataCollectionView;

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
        DataType[] types = new DataType[classes.size()];
        int i = 0;
        for (Class c : classes) {
            if (c.getName().equals(TimeSerie.class.getCanonicalName())) {
                types[i] = DataType.TIME_SERIES;
            } else if (c.getName().equals(Measurement.class.getCanonicalName())) {
                types[i] = DataType.MEASUREMENT;
            } else if (c.getName().equals(Efficiency.class.getCanonicalName())) {
                types[i] = DataType.OBJECTIVE;
            } else if (c.getName().equals(StateVariable.class.getCanonicalName())) {
                types[i] = DataType.VARIABLE;
            } else if (c.getName().equals(Parameter.class.getCanonicalName())) {
                types[i] = DataType.PARAMETER;
            }
            i++;            
        }
        return types;
    }

    @Override
    public Object[] getItemIdentifiersForDataType(DataType type) {
        
        Set<String> dataSetIdentifiers = null;

        Class c = null;
        switch (type) {
            case TIME_SERIES:
                c = TimeSerie.class;
                break;
            case MEASUREMENT:
                c = Measurement.class;
                break;
            case OBJECTIVE:
                c = Efficiency.class;
                break;
            case VARIABLE:
                c = StateVariable.class;
                break;
            case PARAMETER:
                c = Parameter.class;
                break;
            default:
        }

        dataSetIdentifiers = collection.getDatasets(c);

        return dataSetIdentifiers.toArray();
    }

    @Override
    public TimeInterval getTimeInterval(Object item) {
        return ((TimeSerieEnsemble) collection.getDataSet((String) item)).getTimeInterval();
    }

    @Override
    public boolean hasTimeInterval(Object item) {
        return collection.getDataSet((String) item) instanceof TimeSerieEnsemble;
    }

    @Override
    public void itemIsBeingDisplayed(Object item) {
    }

    @Override
    public Object getItemForIdentifier(Object identifier) {
        return collection.getDataSet((String) identifier);
    }
}
