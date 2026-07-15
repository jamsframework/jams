/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jams.data;

/**
 *
 * @author christian
 */
public class DoubleArrayDataSupplier extends AbstractDataSupplier<Double, double[]> {
        
    public DoubleArrayDataSupplier(double[] input) {
        super(input);
    }

    @Override
    public int size() {
        return input.length;
    }

    @Override
    public Double get(int i) {
        return input[i];
    }
}
