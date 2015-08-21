/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package optas.io;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import jams.workspace.dsproc.AbstractDataStoreProcessor;
import jams.workspace.dsproc.AbstractDataStoreProcessor.AttributeData;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import optas.data.api.DataView;
import optas.data.view.AbstractListView;

/**
 *
 * @author Christian Fischer - christian.fischer.2@uni-jena.de
 */
public abstract class J2KImporter<T> implements DataCollectionImporter<T> {

    Map<String, T> datasetType = new HashMap<>();

    protected void setDataSetType(T type) {
        datasetType.clear();
        for (String s : getDataSetNames()) {
            datasetType.put(s, type);
        }
    }

    protected void selectAllAttributes(AbstractDataStoreProcessor dataStoreProcessor) {
        //force to get all attributes!
        for (AttributeData a : dataStoreProcessor.getAttributes()) {
            a.setSelected(true);
        }
    }

    protected int getColumnOfAttribute(AbstractDataStoreProcessor dataStoreProcessor, String attribute) {
        int counter = 0;
        for (AttributeData a : dataStoreProcessor.getAttributes()) {
            if (!a.isSelected()) {
                continue;
            }
            if (a.getName().equals(attribute)) {
                return counter;
            }
            counter++;
        }
        return -1;
    }

    protected long[] toPrimitive(Long[] array) {
        long ids[] = new long[array.length];
        for (int i1 = 0; i1 < ids.length; i1++) {
            ids[i1] = array[i1];
        }
        return ids;
    }
    
    protected DataView<String> getDataSetNames(AbstractDataStoreProcessor dataStoreProcessor) {
        checkNotNull(dataStoreProcessor, "AbstractDataStoreProcessor must not be null!");

        return new AbstractListView<String, List<AttributeData>>(dataStoreProcessor.getAttributes()) {

            @Override
            public int getSize() {
                return input.size();
            }

            @Override
            public String getValue(int i) {
                return input.get(i).getName();
            }
        };
    }

    @Override
    public T getDataSetType(String name) {
        return datasetType.get(name);
    }

    @Override
    public void setDataSetType(String name, T type) {
        checkArgument(datasetType.containsKey(name), "Cannot set type of unknown dataset %s!", name);
        datasetType.put(name, type);
    }
}
