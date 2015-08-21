/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package optas.data.api;

/**
 *
 * @author Christian Fischer <christian.fischer.2@uni-jena.de>
 */
public interface DataView<T> extends Iterable<T> {

    int getSize();

    T getValue(int i);

    DataView<T> subset(DataView<Integer> subsetIndices);

    T setValue(int i, T value);
    
    boolean isWritable();
}
