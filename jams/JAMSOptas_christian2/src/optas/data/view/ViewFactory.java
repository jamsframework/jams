/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package optas.data.view;

import java.util.ArrayList;
import optas.data.api.DataView;

/**
 *
 * @author Christian Fischer <christian.fischer.2@uni-jena.de>
 */
public class ViewFactory {
    static public <T> ArrayView<T> createView(T[] list){
        return new ArrayView<>(list);
    }
    
    static public DoubleArrayView createView(double d[]){
        return new DoubleArrayView(d);
    }
    
    static public IntegerArrayView createView(int d[]){
        return new IntegerArrayView(d);
    }
    
    static public DoubleToFloatArrayView createFloatView(double d[]){
        return new DoubleToFloatArrayView(d);
    }
    
    static public <T> ArrayListView<T> createView(ArrayList<T> list){
        return new ArrayListView<>(list);
    }
    
    static public <T> T[] toArray(DataView<T> view){
        T result[] = (T[])new Object[view.getSize()];
        for (int i=0;i<view.getSize();i++){
            result[i] = view.getValue(i);
        }
        return result;
    }
}
