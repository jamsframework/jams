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
public class DoubleArrayView extends AbstractListView<Double, double[]> {
        
    public DoubleArrayView(double[] input) {
        super(input);
    }

    @Override
    public int getSize() {
        return input.length;
    }

    @Override
    public Double getValue(int i) {
        return input[i];
    }

    @Override
    public Double setValue(int i, Double value) {
        Double oldValue = input[i];
        input[i] = value;
        return oldValue;
    }
    
    @Override
    public boolean isWritable(){
        return true;
    }
}
