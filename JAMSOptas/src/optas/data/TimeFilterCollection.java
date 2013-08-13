/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package optas.data;

import jams.data.Attribute;
import jams.data.DefaultDataFactory;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Calendar;

/**
 *
 * @author christian
 */
public class TimeFilterCollection implements Serializable{
    ArrayList<TimeFilter> timeFilters = new ArrayList<TimeFilter>();
    
    public TimeFilterCollection(){
        
    }
    
    public void clear(){
        timeFilters.clear();
    }
    
    public int size(){
        return timeFilters.size();
    }
    
    public void add(TimeFilter tf){
        timeFilters.add(tf);
    }
    
    public ArrayList<TimeFilter> get(){
        return timeFilters;
    }
    
    public TimeFilter get(int j){
        return timeFilters.get(j);
    }
    
    public void set(ArrayList<TimeFilter> filters){
        this.timeFilters = filters;
    }
    
        
    public TimeFilter combine() {        
        return TimeFilterFactory.getCombinedTimeFilter(timeFilters.toArray(new TimeFilter[0]));
    }
    
    public void fromTimeIntervals(jams.data.Attribute.TimeInterval[] intervals){            
            for (jams.data.Attribute.TimeInterval I : intervals){
                TimeFilter rtf = TimeFilterFactory.getRangeFilter(I);                
                add(rtf);
            }
        }
    
    public void fromTimeIntervals(ArrayList<jams.data.Attribute.TimeInterval> intervals){            
            for (jams.data.Attribute.TimeInterval I : intervals){
                TimeFilter rtf = TimeFilterFactory.getRangeFilter(I);                
                add(rtf);
            }
        }
    
    public jams.data.Attribute.TimeInterval[] toTimeIntervals(Attribute.TimeInterval modelTimeInterval) {
        jams.data.Attribute.Calendar actStart = null;
        jams.data.Attribute.Calendar actEnd = null;
        int timeUnitCount = 1;
        int timeUnit      = 6;
        
        if (modelTimeInterval == null){
            actStart = DefaultDataFactory.getDataFactory().createCalendar();
            actEnd = DefaultDataFactory.getDataFactory().createCalendar();
            
            actStart.setTimeInMillis(Long.MAX_VALUE);
            actEnd.setTimeInMillis(Long.MIN_VALUE);
            
            for (TimeFilter f : timeFilters){
                if (f instanceof TimeFilterFactory.RangeTimeFilter){
                    TimeFilterFactory.RangeTimeFilter filter = (TimeFilterFactory.RangeTimeFilter)f;
                    if (filter.getRange().getStart().getTimeInMillis() < actStart.getTimeInMillis()){
                        actStart.setTimeInMillis(filter.getRange().getStart().getTimeInMillis());
                    }
                    if (filter.getRange().getEnd().getTimeInMillis() > actEnd.getTimeInMillis()){
                        actEnd.setTimeInMillis(filter.getRange().getEnd().getTimeInMillis());
                    }
                    if (filter.getRange().getTimeUnit() != timeUnit){
                        timeUnit = filter.getRange().getTimeUnit();
                    }
                    if (filter.getRange().getTimeUnitCount() != timeUnitCount){
                        timeUnitCount = filter.getRange().getTimeUnitCount();
                    }
                }
                if (f instanceof TimeFilterFactory.YearlyTimeFilter){
                    TimeFilterFactory.YearlyTimeFilter filter = (TimeFilterFactory.YearlyTimeFilter)f;
                    int years[] = filter.getYears();
                    for (int y : years){
                        if (actStart.get(Calendar.YEAR) > y){
                            actStart.set(y, 0, 1, 1, 1, 1);
                        }
                        if (actEnd.get(Calendar.YEAR) < y){
                            actStart.set(y, 11, 30, 23, 58, 58);
                        }
                    }
                }
                if (f instanceof TimeFilterFactory.EventFilter){
                    TimeFilterFactory.EventFilter filter = (TimeFilterFactory.EventFilter)f;
                    if (actStart.getTimeInMillis() > filter.getFirstDate().getTime()){
                        actStart.setTime(filter.getFirstDate());
                    }
                    if (actEnd.getTimeInMillis() < filter.getLastDate().getTime()){
                        actEnd.setTime(filter.getLastDate());
                    }
                }
            }           
        }else{
            actStart = modelTimeInterval.getStart();
            actEnd = modelTimeInterval.getEnd();
            timeUnitCount = modelTimeInterval.getTimeUnitCount();
            timeUnit = modelTimeInterval.getTimeUnit();
        }
        
        jams.data.Attribute.Calendar actTime = actStart.clone();
        ArrayList<jams.data.Attribute.TimeInterval> timeIntervals = new ArrayList<jams.data.Attribute.TimeInterval>();

        TimeFilter combinedFilter = combine();

        boolean lastStepFiltered = false;
        jams.data.Attribute.Calendar lastTimeStep = null;

        

        while (!actTime.after(actEnd)) {
            boolean actStepFiltered = combinedFilter.isFiltered(actTime.getTime());
            
            if (actStepFiltered && lastStepFiltered) {
                //fine nothing to do
            } else if (actStepFiltered && !lastStepFiltered && lastTimeStep != null) {
                //last interval has ended
                jams.data.Attribute.TimeInterval nextInterval = jams.data.DefaultDataFactory.getDataFactory().createTimeInterval();
                nextInterval.setValue(actStart.toString() + " " + lastTimeStep.toString() + " " + timeUnit + " " + timeUnitCount);
                timeIntervals.add(nextInterval);
            } else if (!actStepFiltered && !lastStepFiltered) {
                lastTimeStep = actTime;
            } else if (!actStepFiltered && lastStepFiltered) {
                actStart = actTime.clone();
            }
            lastStepFiltered = actStepFiltered;
            actTime.add(timeUnit, timeUnitCount);
        }
        if (!lastStepFiltered && lastTimeStep != null) {
            //last interval has ended
            jams.data.Attribute.TimeInterval nextInterval = jams.data.DefaultDataFactory.getDataFactory().createTimeInterval();
            nextInterval.setValue(actStart.toString() + " " + lastTimeStep.toString() + " " + timeUnit + " " + timeUnitCount);
            timeIntervals.add(nextInterval);
        }
        return timeIntervals.toArray(new jams.data.Attribute.TimeInterval[0]);
    }
}
