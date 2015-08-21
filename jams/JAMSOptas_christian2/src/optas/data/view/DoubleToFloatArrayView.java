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
public class DoubleToFloatArrayView extends AbstractListView<Float, double[]> {
        
    public DoubleToFloatArrayView(double[] input) {
        super(input);
    }

    @Override
    public int getSize() {
        return input.length;
    }

    @Override
    public Float getValue(int i) {
        return (float)input[i];
    }

    @Override
    public Float setValue(int i, Float value) {
        float oldValue = (float)input[i];
        input[i] = value;
        return oldValue;
    }    
    
    @Override
    public boolean isWritable(){
        return true;
    }
}
