/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package optas.hydro.data;

import jams.data.Attribute;
import jams.data.Attribute.TimeInterval;
import java.util.ArrayList;
import java.util.Date;

/**
 *
 * @author chris
 */
public class TimeSerieEnsemble extends Ensemble{
    double value[][];
    int currentIndex = 0;
    TimeInterval timeInterval = null;
    ArrayList<TimeFilter> filter = new ArrayList<TimeFilter>();
    ArrayList<Integer> timeMap = new ArrayList<Integer>();

    public Ensemble clone(){
        TimeSerieEnsemble s = new TimeSerieEnsemble(name, size, timeInterval);
        for (int i=0;i<size;i++)
            s.set(i, this.getId(i));
        s.currentIndex = currentIndex;
        s.id = id.clone();
        s.parent = parent;
        s.value = value.clone();
        s.filter = (ArrayList<TimeFilter>)filter.clone();

        return s;
    }

    public TimeSerieEnsemble(String name, int size, TimeInterval timeInterval) {
        super(size);
        this.name = name;        
        value = new double[size][];
        this.timeInterval = timeInterval;
    }

    public void addTimeFilter(TimeFilter filter){
        this.filter.add(filter);
        buildTimeMapping();
    }
    public void removeTimeFilter(TimeFilter filter){
        this.filter.remove(filter);
        buildTimeMapping();
    }
    public ArrayList<TimeFilter> getTimeFilters(){
        return filter;
    }

    private void buildTimeMapping(){
        timeMap.clear();
        for (int i=0;i<value[0].length;i++){
            Date d = getDate(i);
            boolean isFiltered = false;
            for (TimeFilter f : this.filter){
                if (f.isFiltered(d)){
                    isFiltered = true;
                    break;
                }
            }
            if (!isFiltered){
                this.timeMap.add(i);
            }
        }
    }

    /*public void set(int index, String id, double value[]) {
        set(index, id);
        this.id[index] = id;
        this.value[index] = value;
    }*/
    public void set(Integer id, double value[]) {
        set(currentIndex, id);        
        this.value[currentIndex] = value;
        currentIndex++;
    }

    private Date getDate(int time){
        Attribute.Calendar c1 = this.timeInterval.getStart().clone();
        c1.add(timeInterval.getTimeUnit(), timeInterval.getTimeUnitCount()*time);
        return c1.getTime();
    }

    public int getTimesteps(){
        if (this.filter.isEmpty())
            return value[0].length;
        else
            return this.timeMap.size();
    }
    /*public double get(int time, int index){
        return value[index][time];
    }*/
    public double get(int time, Integer id){
        if (this.filter.isEmpty())
            return value[getIndex(id)][time];
        else{
            int index = this.timeMap.get(time);
            return value[getIndex(id)][time];
        }
    }

    public TimeSerie getMax(){
        double[] max = new double[getTimesteps()];

        for (int t=0;t<getTimesteps();t++){
            max[t] = Double.NEGATIVE_INFINITY;
            for (int mc=0;mc<getSize();mc++){
                max[t] = Math.max(this.get(t, this.getId(mc)),max[t]);
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
                max[t] = Math.min(this.get(t, this.getId(mc)),max[t]);
            }
        }
        try{
            return new TimeSerie(max, this.getTimesteps(), "min of " + this.name, this.parent);
        }catch (MismatchException me){
            return null;
        }
    }

     public TimeInterval getTimeInterval() {
         return timeInterval;
     }

    @Override
    public void removeId(Integer id){
        int index = getIndex(id);
        super.removeId(id);

        currentIndex--;
        this.value[index] = this.value[size];
    }
}
