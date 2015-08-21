package optas.data;

import jams.data.Attribute.Entity;
import optas.data.api.DataSetContainer;
import optas.data.api.DataView;
import optas.data.api.EntityDataSet;

public class DefaultEntityDataSet<T> extends DefaultMapDataSet<Entity, T> implements EntityDataSet<T> {

    public DefaultEntityDataSet(String name, DataSetContainer parent, DataView<Entity> entities, DataView<T> values) {
        super(name, parent, entities, values);
    }

    @Override
    public Entity getEntity(int index){
        return this.getKey(index);
    }

    @Override
    public T getValue(Entity feature){
        int index = this.getIndex(feature);
        if (index == -1)
            return null;
        return getValue(index);
    }

    @Override
    public DataView<Entity> entities(){
        return keys();
    }
}
