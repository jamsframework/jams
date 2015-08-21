/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package optas.data.view;

import java.util.Iterator;
import optas.data.api.DataView;

/**
 *
 * @author christian
 * @param <T>
 * @param <U>
 */
public abstract class AbstractListView<T, U> implements DataView<T> {

    protected U input;
    protected Object args[];

    public AbstractListView(U input, Object... args) {
        this.input = input;
        this.args = args;
    }

    @Override
    public Iterator<T> iterator() {
        return new BasicIterator(this);
    }

    @Override
    public FilteredListView<T> subset(DataView<Integer> subsetIndices) {
        return new FilteredListView<>(this, subsetIndices);
    }
    
    @Override
    public boolean isWritable() {
        return false;
    }

    @Override
    public T setValue(int i, T value) {
        throw new UnsupportedOperationException("List View is not writable");
    }

    @Override
    public boolean equals(Object obj) {
        if (!AbstractListView.class.isAssignableFrom(obj.getClass())) {
            return false;
        }
        AbstractListView arg = (AbstractListView) obj;

        if (arg.getSize() != getSize()) {
            return false;
        }

        for (int i = 0; i < getSize(); i++) {
            if (!getValue(i).equals(arg.getValue(i))) {
                return false;
            }
        }
        return true;
    }
}
