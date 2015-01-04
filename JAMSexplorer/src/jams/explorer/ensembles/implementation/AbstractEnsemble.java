/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jams.explorer.ensembles.implementation;

import jams.explorer.ensembles.api.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;

/**
 *
 * @author christian
 * @param <T>
 */
@XmlAccessorType(XmlAccessType.FIELD)
public abstract class AbstractEnsemble<T extends Model<T>> implements Ensemble<T> {
       
    @XmlElement(type=ClimateModel.class) //not so nice
    List<T> modelSet = null;
    
    @XmlElement
    String name;

    public AbstractEnsemble() {
        this("");
    }

    public AbstractEnsemble(String name) {
        this.name = name;
        modelSet = new ArrayList<T>();
    }

    @Override
    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String getName() {
        return name;
    }

    public void setModelSet(List<T> modelSet) {
        this.modelSet = modelSet;
    }

    
    public List<T> getModelSet() {
        return modelSet;
    }

    public boolean isIDValid(int id) {
        for (T m : modelSet) {
            if (m.getID() == id) {
                return false;
            }
        }
        return true;
    }

    @Override
    public T addModel(T m) {
        modelSet.add(m);
        int id = 0;
        while (isIDValid(id) == false) {
            id++;
        }
        m.setID(id);
        return m;
    }

    @Override
    public T removeModel(T m) {
        modelSet.remove(m);
        return m;
    }

    @Override
    public T deleteModel(T m) throws IOException {
        m.delete();
        return removeModel(m);
    }

    @Override
    public T getModel(int index) {
        return modelSet.get(index);
    }

    @Override
    public int getSize() {
        return modelSet.size();
    }

    @Override
    public int getID(Model m) {
        for (int i = 0; i < modelSet.size(); i++) {
            if (modelSet.get(i).equals(m)) {
                return i;
            }
        }
        return -1;
    }

    @Override
    public T duplicateModel(T m) {
        return addModel(m.copy());
    }

    @Override
    public Iterator<T> iterator() {
        return modelSet.iterator();
    }
}
