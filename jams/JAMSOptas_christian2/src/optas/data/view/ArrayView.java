/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package optas.data.view;

/**
 *
 * @author christian
 */
public class ArrayView<T> extends AbstractListView<T, T[]> {
        
    public ArrayView(T[] input) {
        super(input);
    }

    @Override
    public int getSize() {
        return input.length;
    }

    @Override
    public T getValue(int i) {
        return input[i];
    }

    @Override
    public T setValue(int i, T value) {
        T oldValue = input[i];
        input[i] = value;
        return oldValue;
    }
    
    @Override
    public boolean isWritable(){
        return true;
    }
}
