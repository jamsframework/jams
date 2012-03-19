/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package reg.hydro.data;

import java.util.Arrays;

/**
 *
 * @author chris
 */
public class SimpleEnsemble extends Ensemble{
    protected double value[];
    int currentIndex = 0;

    public SimpleEnsemble(Ensemble e){
        super(e);
        this.value  = new double[e.size];
    }

    public SimpleEnsemble(SimpleEnsemble e){
        super(e);
        this.value  = e.value;        
    }

    public SimpleEnsemble(String name, int size) {
        super(size);
        this.name = name;                
        value = new double[size];
    }

    /*public void set(int index, Integer id, double value) {
        set(index,id);
        this.value[index] = value;
    }*/
    public void add(Integer id, double value){
        set(currentIndex,id);
        this.value[currentIndex] = value;
        currentIndex++;
    }

    /*public double getValue(int index){
        return this.value[index];
    }*/
    public double getValue(Integer id){
        return this.value[this.getIndex(id)];
    }
    public void setValue(Integer id, double value){
        this.value[this.getIndex(id)] = value;
    }
    /*public void setValue(int index, double value){
        this.value[index] = value;
    }*/


    /*public double getValue(String id){
        int index = this.getIndex(id);
        return this.getValue(index);
    }*/

    public int findArgMin(){
        double min = Double.POSITIVE_INFINITY;
        int index = -1;
        for (int i=0;i<size;i++){
            if (value[i]<min){
                min = value[i];
                index = i;
            }
        }
        if (index==-1 && value.length>0)
            return 0;
        return index;
    }

    public int findArgMax(){
        double max = Double.NEGATIVE_INFINITY;
        int index = -1;
        for (int i=0;i<size;i++){
            if (value[i]>max){
                max = value[i];
                index = i;
            }
        }
        if (index==-1 && value.length>0)
            return 0;
        return index;
    }

    public double getMin(){
       return value[findArgMin()];
    }

    public double getMax(){
        return value[findArgMax()];
    }

    class DataIdPair implements Comparable{
        double value;
        Integer id;
        public DataIdPair(double value, Integer id){
            this.value = value;
            this.id = id;
        }
        public int compareTo(Object obj){
            if (obj instanceof DataIdPair){
                DataIdPair eip = (DataIdPair)obj;
                if ( this.value < eip.value){
                    return -1;
                } else if (this.value > eip.value){
                    return 1;
                } else
                    return 0;
            }
            return 0;
        }
    }

    public Integer[] sort(){
        return sort(true);
    }
    public Integer[] sort(boolean ascending){
        DataIdPair[] list = new DataIdPair[size];
        for (int i=0;i<list.length;i++)
            list[i] = new DataIdPair(value[i], id[i]);
        Arrays.sort(list);

        Integer result[] = new Integer[size];
        if (ascending)
            for (int i = 0;i<size;i++)
                result[i] = list[i].id;
        else
            for (int i = 0;i<size;i++)
                result[i] = list[size-i-1].id;
        return result;
    }
}
