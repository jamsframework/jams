/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package optas.data.ensemble;

import java.util.Arrays;
import optas.data.api.DataSetContainer;
import optas.data.api.DataView;
import optas.data.ensemble.api.SimpleEnsemble;

/**
 *
 * @author chris
 */
public class DefaultSimpleEnsemble extends DefaultEnsemble<Double> implements SimpleEnsemble {
    
    public DefaultSimpleEnsemble(SimpleEnsemble e){
        super(e);
    }
    
    public DefaultSimpleEnsemble(String name, DataSetContainer parent, DataView<Integer> ids, DataView<Double> values){
        super(name, parent, ids, values);
    }
    
    @Override
    public SimpleEnsemble clone(){
        return new DefaultSimpleEnsemble(this);        
    }
    
    //returns ID of minimal element
    @Override
    public int findArgMin() {
        double min = Double.POSITIVE_INFINITY;
        int index = -1;
        for (int i = 0; i < getSize(); i++) {
            double value = getValue(i);
            if (value < min) {
                min = value;
                index = i;
            }
        }
        if (index < 0) {
            return -1;
        }
        return this.getId(index);
    }

    @Override
    public int findArgMax() {
        double max = Double.NEGATIVE_INFINITY;
        int index = -1;
        for (int i = 0; i < getSize(); i++) {
            double value = getValue(i);
            if (value > max) {
                max = value;
                index = i;
            }
        }
        if (index < 0) {
            return -1;
        }
        return this.getId(index);
    }

    @Override
    public Double getMin() {
        return this.getValue(findArgMin());
    }

    @Override
    public Double getMax() {
        return this.getValue(findArgMax());
    }

    static class DataIdPair implements Comparable {

        double value;
        Integer id;

        public DataIdPair(double value, Integer id) {
            this.value = value;
            this.id = id;
        }

        @Override
        public int compareTo(Object obj) {
            if (obj instanceof DataIdPair) {
                DataIdPair eip = (DataIdPair) obj;
                if (Double.isNaN(value)) {
                    return 1;
                }

                if (Double.isNaN(eip.value)) {
                    return -1;
                }

                if (this.value < eip.value) {
                    return -1;
                } else if (this.value > eip.value) {
                    return 1;
                } else {
                    return 0;
                }
            }
            return 0;
        }
    }
    
    @Override
    public Integer[] sort() {
        return sort(true);
    }

    @Override
    public Integer[] sort(boolean ascending) {
        DataIdPair[] list = new DataIdPair[getSize()];
        for (int i = 0; i < list.length; i++) {
            list[i] = new DataIdPair(this.getValue(i), this.getId(i));
        }
        Arrays.sort(list);

        Integer result[] = new Integer[getSize()];
        if (ascending) {
            for (int i = 0; i < getSize(); i++) {
                result[i] = list[i].id;
            }
        } else {
            for (int i = 0; i < getSize(); i++) {
                result[i] = list[getSize() - i - 1].id;
            }
        }
        return result;
    }
     
    @Override
    public SimpleEnsemble filterIds(DataView<Integer> filter){
        //this works because filterIds relies on getInstance .. 
        return (SimpleEnsemble)super.filterIds(filter);
    }
    
    @Override
    public SimpleEnsemble removeIds(Integer ids[]){
        //this works because removeIds relies on getInstance .. 
        return (SimpleEnsemble)super.removeIds(ids);
    }
    
    public SimpleEnsemble getInstance(String name, DataSetContainer parent, DataView<Integer> ids, DataView<Double> values){
        return new DefaultSimpleEnsemble(name, parent, ids, values);
    }

    /*@Override
    public void removeId(Integer id) {
        int index = getIndex(id);
        super.removeId(id);

        currentIndex--;
        this.value[index] = this.value[size];
    }
   
    @Override
    public void calcPlus(double d) {
        for (int i = 0; i < this.size; i++) {
            value[i] += d;
        }
    }

    @Override
    public void calcPlus(optas.data.api.SimpleEnsemble d) {
        for (int i = 0; i < this.size; i++) {
            int id = this.getId(i);
            this.value[i] += d.getValue(id);
        }

    }

    public void calcMul(double d) {
        for (int i = 0; i < this.size; i++) {
            value[i] *= d;
        }
    }

    public void calcAbs() {
        for (int i = 0; i < this.size; i++) {
            value[i] = Math.abs(value[i]);
        }
    }*/
}
