/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package optas.data.view;

import optas.data.api.DataView;
import optas.core.OPTASException;

/**
 *
 * @author christian
 */
public class SelectionListView<T> extends AbstractListView<T, DataView<T[]>> {

    int column;

    public SelectionListView(DataView<T[]> input, int column) {
        super(input);
        if (column < 0 || column > input.getSize()) {
            throw new OPTASException("Cannot create data supplier. Selection out of bounds.");
        }
    }

    @Override
    public int getSize() {
        return input.getSize();
    }

    @Override
    public T getValue(int i) {
        return input.getValue(i)[column];
    }

    @Override
    public T setValue(int i, T value) {
        T oldValue = getValue(i);
        input.getValue(i)[column] = value;
        return oldValue;
    }

    @Override
    public boolean isWritable() {
        return true;
    }
}
