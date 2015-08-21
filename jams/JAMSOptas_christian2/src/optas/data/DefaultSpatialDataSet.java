package optas.data;

import optas.data.api.DataView;
//import optas.data.api.SpatialDataSet;
/*import org.opengis.feature.simple.SimpleFeature;

public class DefaultSpatialDataSet<T> extends DefaultMapDataSet<SimpleFeature, T> implements SpatialDataSet<T> {

    public DefaultSpatialDataSet(String name, DataSupplier<SimpleFeature> features, DataSupplier<T> values) {
        super(name, features, values);
    }

    @Override
    public SimpleFeature getFeature(int index){
        return this.getKey(index);
    }

    @Override
    public T getValue(SimpleFeature feature){
        int index = this.getIndex(feature);
        if (index == -1)
            return null;
        return getValue(index);
    }

    @Override
    public DataSupplier<SimpleFeature> entities(){
        return keys();
    }
}
*/