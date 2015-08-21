/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package optas.data;

import jams.JAMS;
import jams.data.Attribute.Calendar;
import jams.data.Attribute.TimeInterval;
import jams.data.DefaultDataFactory;
import jams.workspace.stores.J2KTSDataStore;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;

/**
 *
 * @author chris
 */
public class TimeSerie extends DataSet{
    private float set[];
    private int timeLength;
    private TimeInterval range;

    ArrayList<TimeFilter> filter = new ArrayList<TimeFilter>();
    ArrayList<Integer> timeMap = new ArrayList<Integer>();

    public TimeSerie(TimeSerie set){
        this.set = set.set;
        this.timeLength = set.timeLength;
        this.range = set.range;
        this.name = set.name;
    }

    public TimeSerie(double []value, TimeInterval range, String name, DataSet parent) throws MismatchException{
        set = new float[value.length];
        this.name = name;
        for (int i=0;i<value.length;i++)
            this.set[i] = (float)value[i];
        this.parent = parent;
        this.range = range;
        timeLength = (int)range.getNumberOfTimesteps();
        if (set.length != timeLength){
            throw new MismatchException("mismatch between timeInterval:" + range.toString() + " and provided number of data values:" + set.length + "(expected: " + timeLength + ")");
        }
    }

    public TimeSerie(double []value, int range, String name, DataSet parent) throws MismatchException{
        set = new float[value.length];
        this.name = name;
        for (int i=0;i<value.length;i++)
            this.set[i] = (float)value[i];
        this.parent = parent;
        timeLength = range;
        if (set.length != timeLength){
            throw new MismatchException("mismatch between timeInterval:" + range + " and provided number of data values:" + set.length);
        }
    }
        
    public void addTimeFilter(TimeFilter filter){
        this.filter.add(filter);
        buildTimeMapping();
    }
    public void removeTimeFilter(TimeFilter filter){
        if (filter==null)
            this.filter.clear();
        this.filter.remove(filter);
        buildTimeMapping();
    }

    private void buildTimeMapping(){
        timeMap.clear();
        for (int i=0;i<set.length;i++){
            Date d = this.getUnfilteredTime(i);
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

    public ArrayList<TimeFilter> getTimeFilters(){
        return filter;
    }

    public int getTimesteps(){
        if (this.filter.isEmpty())
            return set.length;
        else
            return this.timeMap.size();
    }

    public int findArgMin(){
        double min = Double.POSITIVE_INFINITY;
        int index = -1;
        for (int i=0;i<timeLength;i++){
            if (set[i] != JAMS.getMissingDataValue() && set[i]<min){
                min = set[i];
                index = i;
            }
        }
        if (index==-1 && set.length>0)
            return 0;
        return index;
    }

    public int findArgMax(){
        double max = Double.NEGATIVE_INFINITY;
        int index = -1;
        for (int i=0;i<timeLength;i++){
            if (set[i] != JAMS.getMissingDataValue() && set[i]>max){
                max = set[i];
                index = i;
            }
        }
        if (index==-1 && set.length>0)
            return 0;
        return index;
    }

    public double getMin(){
       return set[findArgMin()];
    }

    public double getMax(){
        return set[findArgMax()];
    }

    public double getValue(int time){
        if (this.filter.isEmpty())
            return set[time];
        else
            return set[this.timeMap.get(time)];
    }

    private Date getUnfilteredTime(int time){
        Calendar c = this.range.getStart().clone();
        c.add(range.getTimeUnit(), time*range.getTimeUnitCount());
        return c.getTime();
    }

    public Date getTime(int time){
        if (!this.filter.isEmpty()){
            time = this.timeMap.get(time);
        }
        Calendar c = this.range.getStart().clone();
        c.add(range.getTimeUnit(), time*range.getTimeUnitCount());
        return c.getTime();
    }

    public TimeInterval getTimeDomain(){
        if (this.filter.isEmpty())
            return range;
        else{
            TimeInterval range = DefaultDataFactory.getDataFactory().createTimeInterval();
            range.setTimeUnit(this.range.getTimeUnit());
            range.setTimeUnitCount(this.range.getTimeUnitCount());
            range.getStart().setTime(getTime(0));
            range.getEnd().setTime(getTime(this.getTimesteps()-1));
            return range;
        }
        
    }
    
    @Override
    public int hashCode() {
        int hash = 7;
        hash = 53 * hash + Arrays.hashCode(this.set);
        hash = 53 * hash + this.timeLength;
        hash = 53 * hash + (this.range != null ? this.range.hashCode() : 0);
        return hash;
    }
    
    @Override
    public String toString() {
        return name;// + JAMS.i18n("_TIMESTEPS:") + timeLength;
    }

    public static TimeSerie createFromJ2KTSDataStore(J2KTSDataStore store, int index, String name){
        TimeInterval range = DefaultDataFactory.getDataFactory().createTimeInterval();
        range.setStart(store.getStartDate());
        range.setEnd(store.getEndDate());
        range.setTimeUnit(store.getTimeUnit());
        range.setTimeUnitCount(store.getTimeUnitCount());

        long count = range.getNumberOfTimesteps();
        double value[] = new double[(int)count];
        int i=0;
        while(store.hasNext())
            value[i++] = store.getNext().getData()[index].getDouble();

        try{
            return new TimeSerie(value,range, name, null);
        }catch(MismatchException me){
            //should not happen
            return null;
        }
    }

    public static TimeSerie createFromJ2KTSDataStore(J2KTSDataStore store, String selectedAttribute, String name){
        ArrayList<String> list = store.getDataSetDefinition().getAttributeNames();
        int index = list.indexOf(name);
        if (index == -1)
            return null;

        return createFromJ2KTSDataStore(store, index, name);
    }
}
