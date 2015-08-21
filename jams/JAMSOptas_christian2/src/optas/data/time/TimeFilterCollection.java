/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package optas.data.time;

import static com.google.common.base.Preconditions.checkNotNull;
import jams.data.Attribute;
import jams.data.Attribute.TimeInterval;
import jams.data.DefaultDataFactory;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Calendar;

/**
 *
 * @author christian
 */
public class TimeFilterCollection implements Serializable {

    ArrayList<DefaultTimeFilter> timeFilters = new ArrayList<DefaultTimeFilter>();

    public TimeFilterCollection() {

    }

    public void clear() {
        timeFilters.clear();
    }

    public int size() {
        return timeFilters.size();
    }

    public void add(DefaultTimeFilter tf) {
        checkNotNull(tf);
        timeFilters.add(tf);
    }

    public ArrayList<DefaultTimeFilter> get() {
        return timeFilters;
    }

    public DefaultTimeFilter get(int j) {
        return timeFilters.get(j);
    }

    public void set(ArrayList<DefaultTimeFilter> filters) {
        checkNotNull(filters);
        this.timeFilters = filters;
    }

    public DefaultTimeFilter combine() {
        return TimeFilterFactory.getCombinedTimeFilter(timeFilters.toArray(new DefaultTimeFilter[0]));
    }

    public void fromTimeIntervals(jams.data.Attribute.TimeInterval[] intervals) {
        checkNotNull(intervals);
        for (jams.data.Attribute.TimeInterval I : intervals) {
            DefaultTimeFilter rtf = TimeFilterFactory.getRangeFilter(I);
            add(rtf);
        }
    }

    public void fromTimeIntervals(ArrayList<jams.data.Attribute.TimeInterval> intervals) {
        checkNotNull(intervals);
        for (jams.data.Attribute.TimeInterval I : intervals) {
            DefaultTimeFilter rtf = TimeFilterFactory.getRangeFilter(I);
            add(rtf);
        }
    }

    public jams.data.Attribute.TimeInterval[] toTimeIntervals() {
        jams.data.Attribute.Calendar actStart = DefaultDataFactory.getDataFactory().createCalendar();
        jams.data.Attribute.Calendar actEnd = DefaultDataFactory.getDataFactory().createCalendar();
        int timeUnitCount = 1;
        int timeUnit = 6;

        actStart.set(10000, 1, 1, 1, 1, 1);
        actEnd.set(-2000, 1, 1, 1, 1, 1);

        for (DefaultTimeFilter f : timeFilters) {
            if (f instanceof TimeFilterFactory.RangeTimeFilter) {
                TimeFilterFactory.RangeTimeFilter filter = (TimeFilterFactory.RangeTimeFilter) f;
                if (filter.getRange().getStart().getTimeInMillis() < actStart.getTimeInMillis()) {
                    actStart.setTimeInMillis(filter.getRange().getStart().getTimeInMillis());
                }
                if (filter.getRange().getEnd().getTimeInMillis() > actEnd.getTimeInMillis()) {
                    actEnd.setTimeInMillis(filter.getRange().getEnd().getTimeInMillis());
                }
                if (filter.getRange().getTimeUnit() != timeUnit) {
                    timeUnit = filter.getRange().getTimeUnit();
                }
                if (filter.getRange().getTimeUnitCount() != timeUnitCount) {
                    timeUnitCount = filter.getRange().getTimeUnitCount();
                }
            }
            if (f instanceof TimeFilterFactory.YearlyTimeFilter) {
                TimeFilterFactory.YearlyTimeFilter filter = (TimeFilterFactory.YearlyTimeFilter) f;
                int years[] = filter.getYears();
                for (int y : years) {
                    if (actStart.get(Calendar.YEAR) > y) {
                        actStart.set(y, 0, 1, 1, 1, 1);
                    }
                    if (actEnd.get(Calendar.YEAR) < y) {
                        actEnd.set(y, 11, 30, 23, 58, 58);
                    }
                }
            }
            if (TimeFilterFactory.EventFilter.class.isAssignableFrom(f.getClass())) {
                TimeFilterFactory.EventFilter filter = (TimeFilterFactory.EventFilter) f;
                if (actStart.getTimeInMillis() > filter.getFirstDate().getTime()) {
                    actStart.setTime(filter.getFirstDate());
                }
                if (actEnd.getTimeInMillis() < filter.getLastDate().getTime()) {
                    actEnd.setTime(filter.getLastDate());
                }
            }
        }
        TimeInterval ti = DefaultDataFactory.getDataFactory().createTimeInterval();
        ti.setStart(actStart);
        ti.setEnd(actEnd);
        ti.setTimeUnit(timeUnit);
        ti.setTimeUnitCount(timeUnitCount);
        
        return toTimeIntervals(ti);
    }

    public jams.data.Attribute.TimeInterval[] toTimeIntervals(Attribute.TimeInterval modelTimeInterval) {
        jams.data.Attribute.Calendar actStart = modelTimeInterval.getStart();
        jams.data.Attribute.Calendar actEnd = modelTimeInterval.getEnd();
        int timeUnitCount = modelTimeInterval.getTimeUnitCount();
        int timeUnit = modelTimeInterval.getTimeUnit();

        jams.data.Attribute.Calendar actTime = actStart.clone();
        ArrayList<jams.data.Attribute.TimeInterval> timeIntervals = new ArrayList<>();

        DefaultTimeFilter combinedFilter = combine();

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
            }
            if (!actStepFiltered) {
                lastTimeStep = actTime;
            }
            if (!actStepFiltered && lastStepFiltered) {
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
