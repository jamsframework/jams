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
public class IntegerArrayView extends AbstractListView<Integer, int[]> {

    public IntegerArrayView(int[] input) {
        super(input);
    }

    @Override
    public int getSize() {
        return input.length;
    }

    @Override
    public Integer getValue(int i) {
        return input[i];
    }

    @Override
    public Integer setValue(int i, Integer value) {
        Integer oldValue = input[i];
        input[i] = value;
        return oldValue;
    }

    @Override
    public boolean isWritable() {
        return true;
    }
}
