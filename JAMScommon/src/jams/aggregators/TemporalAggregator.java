/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jams.aggregators;

import jams.data.Attribute;
import jams.data.Attribute.Calendar;
import jams.data.Attribute.TimeInterval;
import jams.data.DefaultDataFactory;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;

/**
 *
 * @author christian
 */
public abstract class TemporalAggregator<T>{
    private Calendar time;
    
    AggregationTimePeriod timePeriod;
    ArrayList<Consumer> consumers = new ArrayList<Consumer>();    
    Collection<TimeInterval> customTimePeriods;
    
    public enum AggregationTimePeriod{
        HOURLY, 
        DAILY, 
        YEARLY, 
        SEASONAL, 
        MONTHLY, 
        DECADLY, 
        HALFYEAR, 
        HYDHALFYEAR, 
        CUSTOM;
        
        public static AggregationTimePeriod fromString(String s){
            for (AggregationTimePeriod iter : AggregationTimePeriod.values()){
                if (s.compareToIgnoreCase(iter.name()) == 0)
                    return iter;
            }
            return null;
        }
    };
    
    public interface Consumer<T>{
        public void consume(Calendar c, T v);
    }
            
    protected TemporalAggregator(TemporalAggregator<T> copy){
        this.time = copy.time;
        this.timePeriod = copy.timePeriod;
        this.consumers = (ArrayList)copy.consumers.clone();
        if (copy.customTimePeriods != null){
            this.customTimePeriods = new ArrayList<TimeInterval>();
            customTimePeriods.addAll(copy.customTimePeriods);
        }else{
            customTimePeriods = null;
        }
    }
          
    public TemporalAggregator(AggregationTimePeriod timePeriod){
        this(timePeriod, null);
    }
    
    public TemporalAggregator(AggregationTimePeriod timePeriod, Collection<TimeInterval> customTimePeriods){
        this.timePeriod = timePeriod;
        this.customTimePeriods = customTimePeriods;
    }
    
    public void addConsumer(Consumer consumer){
        this.consumers.add(consumer);
    }
    public void removeConsumer(Consumer consumer){
        this.consumers.remove(consumer);
    }
            
    protected void consume(Calendar time, T v) {
        //is there anything to consume?
        if (time == null)
            return;
        for (Consumer c : consumers) {
            c.consume(time, v);
        }
    }
    
    public abstract TemporalAggregator<T> copy();
    
    public abstract void init();
    
    public abstract void aggregate(Calendar timeStep, T next);
    
    public boolean isNextTimeStep(Calendar timeStep){
        Calendar newTime = roundToTimePeriod(timeStep, timePeriod);
        //outside of time intervals, so skip it anyway
        if (newTime == null){
            return true;
        }
        if (time == null){
            time = newTime;
            return false;
        }
        //this is interesting .. comparing calendars in that way, 
        //avoids internal cloning
        return newTime.getTimeInMillis()>(time.getTimeInMillis());
    }
    
    public void setTimeStep(Calendar timeStep){
        time = roundToTimePeriod(timeStep, timePeriod);
    }   
    
    public void finish(){
        time = null;
    }
    
    public AggregationTimePeriod getTimePeriod(){
        return this.timePeriod;
    }
    
    protected Calendar currentTimeStep(){
        return time;
    }
            
    protected TimeInterval getTotalTimePeriod(){
        if (customTimePeriods == null || customTimePeriods.isEmpty()){
            return null;
        }
        TimeInterval totalTimePeriod = DefaultDataFactory.getDataFactory().createTimeInterval();
        totalTimePeriod.getStart().set(5000, 1, 1, 0, 0, 0);
        totalTimePeriod.getEnd().set(1, 1, 1, 0, 0, 0);
                
        for (TimeInterval ti : customTimePeriods){
            if (totalTimePeriod.getStart().after(ti.getStart())){
                totalTimePeriod.setStart(ti.getStart().clone());
            }
            if (totalTimePeriod.getEnd().after(ti.getEnd())){
                totalTimePeriod.setEnd(ti.getEnd().clone());
            }
        }
        return totalTimePeriod;
    }
    //TODO find a better caching mechanism
    static HashMap<AggregationTimePeriod, Calendar[]> cacheMap = new HashMap<AggregationTimePeriod, Calendar[]>();
    
    //cloning of calendar is expansive
    protected Attribute.Calendar roundToTimePeriod(Calendar in, AggregationTimePeriod timeUnitID){
        Calendar cache[] = cacheMap.get(timeUnitID);
        if (cache != null && in.getTimeInMillis() == cache[0].getTimeInMillis()){
            return cache[1];
        }
        if (cache == null){
            cache = new Calendar[2];
            cacheMap.put(timeUnitID, cache);
        }
        Attribute.Calendar out = in.clone();
        cache[0] = in.clone();        
        switch (timeUnitID){
            case HOURLY: out.removeUnsignificantComponents(Attribute.Calendar.HOUR_OF_DAY); break;
            case DAILY: out.removeUnsignificantComponents(Attribute.Calendar.DAY_OF_MONTH); break;
            case MONTHLY: out.removeUnsignificantComponents(Attribute.Calendar.MONTH); break;
            case YEARLY: out.removeUnsignificantComponents(Attribute.Calendar.YEAR); break;
            case DECADLY: out.removeUnsignificantComponents(Attribute.Calendar.YEAR); 
                    int yearInDekade = (out.get(Attribute.Calendar.YEAR)-1) % 10;
                    out.set(out.get(Attribute.Calendar.YEAR)-yearInDekade, 0, 1, 12, 0, 0);
                    break;
            case SEASONAL: {out.removeUnsignificantComponents(Attribute.Calendar.MONTH); 
                    int month = out.get(Attribute.Calendar.MONTH);
                    int year  = out.get(Attribute.Calendar.YEAR);
                    if (month < 2){
                        month = 12;
                        year = year - 1;
                    }else if (month < 5){
                        month = 2;
                    }else if (month < 8){
                        month = 5;
                    }else if (month < 11){
                        month = 8;
                    }else{
                        month = 12;
                    }
                    out.set(year, month, 1, 12, 0, 0);}
                    break;
            case HALFYEAR: {out.removeUnsignificantComponents(Attribute.Calendar.MONTH); 
                    int month = out.get(Attribute.Calendar.MONTH);
                    if (month < 6){
                        month = 0;
                    }else
                        month = 6;
                    out.set(out.get(Attribute.Calendar.YEAR), month, 1, 12, 0, 0);}
                break;
            case HYDHALFYEAR:
                {out.removeUnsignificantComponents(Attribute.Calendar.MONTH); 
                    int month = out.get(Attribute.Calendar.MONTH);
                    if (month >= 4 && month <= 9){
                        month = 4;
                        out.set(out.get(Attribute.Calendar.YEAR), month, 1, 12, 0, 0);
                    }else if (month < 4){
                        month = 10;
                        out.set(out.get(Attribute.Calendar.YEAR)-1, month, 1, 12, 0, 0);
                    }else{
                        month = 10;
                        out.set(out.get(Attribute.Calendar.YEAR), month, 1, 12, 0, 0);
                    }}
                break;
            case CUSTOM:{
                boolean isConsidered = false;
                for (Attribute.TimeInterval ti : customTimePeriods){
                    if (!ti.getStart().after(in) && !ti.getEnd().before(in)){
                        out.setValue(ti.getStart().toString());
                        isConsidered = true;
                        break;
                    }
                }
                if (!isConsidered)
                    return null;
                break;
            }
        }
        cache[1] = out;
        return out;
    }
}
