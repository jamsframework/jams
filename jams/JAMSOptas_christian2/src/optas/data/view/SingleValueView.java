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
public class SingleValueView<T> extends AbstractListView<T, T> {

    public SingleValueView(T input) {
        super(input);
    }

    @Override
    public int getSize() {
        return 1;
    }

    @Override
    public T getValue(int i) {
        return input;
    }

    @Override
    public T setValue(int i, T value) {
        T oldValue = input;
        input = value;
        return oldValue;
    }

    @Override
    public boolean isWritable() {
        return true;
    }
}
