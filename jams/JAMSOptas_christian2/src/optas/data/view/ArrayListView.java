/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package optas.data.view;

import java.util.ArrayList;

/**
 *
 * @author christian
 */
public class ArrayListView<T> extends AbstractListView<T, ArrayList<T>> {
        
    public ArrayListView(ArrayList<T> input) {
        super(input);
    }

    @Override
    public int getSize() {
        return input.size();
    }

    @Override
    public T getValue(int i) {
        return input.get(i);
    }

    @Override
    public T setValue(int i, T value) {
        T oldValue = input.get(i);
        input.set(i, value);
        return oldValue;
    }
    
    @Override
    public boolean isWritable() {
        return true;
    }
}
