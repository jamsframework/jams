
package optas.data.api;

import jams.data.Attribute.Entity;

public interface EntityDataSet<T> extends MapDataSet<Entity, T> {
                 
    Entity getEntity(int index);    
    T getValue(Entity entity);
    DataView<Entity> entities();   
}
