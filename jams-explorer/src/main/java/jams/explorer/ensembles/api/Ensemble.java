/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jams.explorer.ensembles.api;

import java.io.IOException;
import java.io.Serializable;

/**
 *
 * @author christian
 * @param <T>
 */
public interface Ensemble<T extends Model> extends Iterable<T>, Serializable{
    
    T  addModel(T m);
    int    getID(T m);
    String getName();
    void   setName(String name);
    T  getModel(int id);
    int    getSize();
    T  removeModel(T m);
    T  deleteModel(T m) throws IOException;
    T  duplicateModel(T m);    
}
