/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package optas.data;

import java.util.Arrays;
import java.util.TreeSet;

/**
 *
 * @author chris
 */
public class SimpleEnsemble extends Ensemble{
    protected double value[];
    int currentIndex = 0;

    public Ensemble clone(){
        SimpleEnsemble s = new SimpleEnsemble(name, size);
        for (int i=0;i<size;i++)
            s.set(i, this.getId(i));
        s.currentIndex = currentIndex;        
        s.parent = parent;
        s.value = value.clone();

        return s;
    }
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
	
	public double[] getValues() {
		return Arrays.copyOf(value, value.length);
	}
    /*public void setValue(int index, double value){
        this.value[index] = value;
    }*/


    /*public double getValue(String id){
        int index = this.getIndex(id);
        return this.getValue(index);
    }*/

    protected void set(int index, Integer id, double value){
        super.set(index, id);
        this.value[index] = value;
    }

    //returns ID of minimal element
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
        return this.getId(index);
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
        return this.getId(index);
    }

    public double getMin(){
       return this.getValue(findArgMin());
    }

    public double getMax(){
        return this.getValue(findArgMax());
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
                if (Double.isNaN(value))
                    return 1;
                
                if (Double.isNaN(eip.value))
                    return -1;
                    
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
            list[i] = new DataIdPair(value[i], this.getId(i));
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

    @Override
    public void removeId(Integer id){
        int index = getIndex(id);
        super.removeId(id);

        currentIndex--;
        this.value[index] = this.value[size];
    }

    public void calcPlus(double d){
        for (int i=0;i<this.size;i++)
            value[i] += d;
    }

    public void calcPlus(SimpleEnsemble d){
        for (int i=0;i<this.size;i++){
            int id = this.getId(i);
            this.value[i] += d.getValue(id);
        }
        
    }
    
    public void calcMul(double d){
        for (int i=0;i<this.size;i++)
            value[i] *= d;
    }

    public void calcAbs(){
        for (int i=0;i<this.size;i++)
            value[i] = Math.abs(value[i]);
    }
}
