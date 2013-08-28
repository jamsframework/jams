/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package optas.data;

import jams.data.Attribute;
import jams.data.Attribute.TimeInterval;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.TreeSet;

/**
 *
 * @author chris
 */
public class TimeSerieEnsemble extends Ensemble {

    double value[][];
    int currentIndex = 0;
    TimeInterval timeInterval = null;
    ArrayList<TimeFilter> filter = new ArrayList<TimeFilter>();
    ArrayList<Integer> timeMap = new ArrayList<Integer>();

    public Ensemble clone() {
        TimeSerieEnsemble s = new TimeSerieEnsemble(name, size, timeInterval);
        for (int i = 0; i < size; i++) {
            s.set(i, this.getId(i));
        }
        s.currentIndex = currentIndex;
        s.parent = parent;
        s.value = value.clone();
        s.timeMap = (ArrayList<Integer>)timeMap.clone();
        s.filter = (ArrayList<TimeFilter>) filter.clone();

        return s;
    }

    public TimeSerieEnsemble(String name, int size, TimeInterval timeInterval) {
        super(size);
        this.name = name;
        value = new double[size][];
        this.timeInterval = timeInterval;
    }

    public void addTimeFilter(TimeFilter filter) {
        this.filter.add(filter);
        buildTimeMapping();
    }

    public void removeTimeFilter(TimeFilter filter) {
        this.filter.remove(filter);
        buildTimeMapping();
    }

    public ArrayList<TimeFilter> getTimeFilters() {
        return filter;
    }

    private void buildTimeMapping() {
        timeMap.clear();
        Attribute.Calendar c1 = this.timeInterval.getStart().clone();
        for (int i = 0; i < value[0].length; i++) {            
            c1.add(timeInterval.getTimeUnit(), timeInterval.getTimeUnitCount());
            Date d = (Date)c1.getTime().clone();
            boolean isFiltered = false;
            for (TimeFilter f : this.filter) {
                if (f.isFiltered(d)) {
                    isFiltered = true;
                    break;
                }
            }
            if (!isFiltered) {
                this.timeMap.add(i);
            }
        }
    }

    public SimpleEnsemble sumTS(){
        SimpleEnsemble e = new SimpleEnsemble("sum of" + name, size);
        for (int i=0;i<this.size;i++){
            double v = 0;
            Integer id = this.getId(i);

            for (int t=0;t<this.getTimesteps();t++){
                v += this.get(t, id);
            }

            e.set(i, id , v);
        }
        return e;
    }

    /*public void set(int index, String id, double value[]) {
    set(index, id);
    this.id[index] = id;
    this.value[index] = value;
    }*/
    public void add(Integer id, double value[]) {
        super.set(currentIndex, id);
        this.value[currentIndex] = value;
        if (this.timeMap.isEmpty())
            buildTimeMapping();
        currentIndex++;

    }

    //this is critical, time filter should be applied ??!
    public Date getDate(int time) {
        int time2 = 0;
        if (this.filter.isEmpty()) {
            time2 = time;
        } else {
            time2 = this.timeMap.get(time);
        }
        Attribute.Calendar c1 = this.timeInterval.getStart().clone();
        c1.add(timeInterval.getTimeUnit(), timeInterval.getTimeUnitCount() * time2);
        return c1.getTime();
    }
    
    public int getTimesteps() {
        if (this.filter.isEmpty()) {
            return value[0].length;
        } else {
            return this.timeMap.size();
        }
    }

    public double get(int time, Integer id) {
        if (this.filter.isEmpty()) {
            return value[getIndex(id)][time];
        } else {
            return value[getIndex(id)][timeMap.get(time)];
        }
    }

    public SimpleEnsemble get(int time){
        SimpleEnsemble s = new SimpleEnsemble("timestep:" + time, this.size);
        s.parent = this;

        for (int i=0;i<size;i++){
            Integer id = this.getId(i);
            s.add(id, value[i][timeMap.get(time)]);
        }
        
        return s;
    }

    public double[] getValue(Integer id){
        int index = getIndex(id);
        return this.value[index];
    }

    public TimeSerie getTimeSerie(Integer id){
        try {
            return new TimeSerie(getValue(id), this.getTimesteps(), "max of " + this.name, this.parent);
        }catch(MismatchException me){
            return null;
        }
    }
    
    public TimeSerie getMax() {
        double[] max = new double[getTimesteps()];

        for (int t = 0; t < getTimesteps(); t++) {
            max[t] = Double.NEGATIVE_INFINITY;
            for (int mc = 0; mc < getSize(); mc++) {
                max[t] = Math.max(this.get(t, this.getId(mc)), max[t]);
            }
        }
        try {
            return new TimeSerie(max, this.getTimesteps(), "max of " + this.name, this.parent);
        } catch (MismatchException me) {
            return null;
        }
    }

    public TimeSerie getMin() {
        double[] max = new double[getTimesteps()];

        for (int t = 0; t < getTimesteps(); t++) {
            max[t] = Double.POSITIVE_INFINITY;
            for (int mc = 0; mc < getSize(); mc++) {
                max[t] = Math.min(this.get(t, this.getId(mc)), max[t]);
            }
        }
        try {
            return new TimeSerie(max, this.getTimesteps(), "min of " + this.name, this.parent);
        } catch (MismatchException me) {
            return null;
        }
    }

    public TimeInterval getTimeInterval() {
        return timeInterval;
    }
    
    @Override
    public void removeId(Integer id) {
        int index = getIndex(id);
        super.removeId(id);

        currentIndex--;
        this.value[index] = this.value[size];
    }
}
