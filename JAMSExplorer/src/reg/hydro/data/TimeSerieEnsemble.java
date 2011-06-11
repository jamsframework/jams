/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package reg.hydro.data;

/**
 *
 * @author chris
 */
public class TimeSerieEnsemble extends Ensemble{
    double value[][];
    int currentIndex = 0;

    public TimeSerieEnsemble(String name, int size) {
        super(size);
        this.name = name;        
        value = new double[size][];
    }

    /*public void set(int index, String id, double value[]) {
        set(index, id);
        this.id[index] = id;
        this.value[index] = value;
    }*/
    public void set(Integer id, double value[]) {
        set(currentIndex, id);
        this.id[currentIndex] = id;
        this.value[currentIndex] = value;
        currentIndex++;
    }

    public int getTimesteps(){
        return value[0].length;
    }
    /*public double get(int time, int index){
        return value[index][time];
    }*/
    public double get(int time, Integer id){
        return value[getIndex(id)][time];
    }

    public TimeSerie getMax(){
        double[] max = new double[getTimesteps()];

        for (int t=0;t<getTimesteps();t++){
            max[t] = Double.NEGATIVE_INFINITY;
            for (int mc=0;mc<getSize();mc++){
                max[t] = Math.max(this.get(t, mc),max[t]);
            }
        }
        try{
            return new TimeSerie(max, this.getTimesteps(), "max of " + this.name, this.parent);
        }catch (MismatchException me){
            return null;
        }
    }

     public TimeSerie getMin(){        
        double[] max = new double[getTimesteps()];

        for (int t=0;t<getTimesteps();t++){
            max[t] = Double.POSITIVE_INFINITY;
            for (int mc=0;mc<getSize();mc++){
                max[t] = Math.min(this.get(t, mc),max[t]);
            }
        }
        try{
            return new TimeSerie(max, this.getTimesteps(), "min of " + this.name, this.parent);
        }catch (MismatchException me){
            return null;
        }
    }
}
