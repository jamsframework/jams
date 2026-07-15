/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package optas.data;

import jams.JAMS;
import jams.data.Attribute.TimeInterval;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashSet;
import java.util.TreeMap;
import optas.hydro.calculations.BaseFlow;
import optas.hydro.calculations.HydrographEvent;

/**
 *
 * @author chris
 */
public class TimeFilterFactory {
    static public class YearlyTimeFilter extends TimeFilter{
        boolean years[];
        boolean hydrologicYear;

        private YearlyTimeFilter(int years[], boolean hydrologicYear){
            this.years = new boolean[3000];
            for (int y : years){
                this.years[y] = true;
            }
            this.hydrologicYear = hydrologicYear;
        }
        public int[] getYears(){
            int counter = 0;
            for (int i=0;i<years.length;i++)
                if (years[i])
                    counter++;
            int list[] = new int[counter];
            counter = 0;
            for (int i=0;i<years.length;i++)
                if (years[i])
                    list[counter++] = i;
            return list;
        }
        public boolean isHydrologicYear(){
            return hydrologicYear;
        }

        public boolean isFiltered(Date date) {
            GregorianCalendar calendar = new GregorianCalendar();
            calendar.setTime(date);
            int year = calendar.get(Calendar.YEAR);
            if (this.hydrologicYear && calendar.get(Calendar.MONTH)>=10){
                year++;
            }
            return !(years[year]);
        }

        @Override
        public String toString(){
            return JAMS.i18n("Years") + ": " + Arrays.toString(getYears());
        }
    }

    @SuppressWarnings("deprecation")
    static public class MonthlyTimeFilter extends TimeFilter{
        boolean months[] = new boolean[12];

        private MonthlyTimeFilter(int[] months){
            for (int i=0;i<12;i++)
                this.months[i] = false;
            for (int m : months){
                this.months[m] = true;
            }
        }
        public boolean isFiltered(Date date) {
            return !months[date.getMonth()];
        }
        public int[] getMonths(){
            int c=0;
            for (int i=0;i<12;i++)
                if (this.months[i])
                    c++;
            int index[] = new int[c];
            c=0;
            for (int i=0;i<12;i++)
                if (this.months[i])
                    index[c++] = i;
            return index;
        }
        @Override
        public String toString(){
            return JAMS.i18n("Months") +": " + Arrays.toString(getMonths());
        }
    }

    static public class RangeTimeFilter extends TimeFilter{
        TimeInterval range;

        private RangeTimeFilter(TimeInterval range){
            this.range = range;
        }
        
        public TimeInterval getRange(){
            return range;
        }
        @Override
        public boolean isFiltered(Date date) {
            return date.before(range.getStart().getTime()) || date.after(range.getEnd().getTime());
        }
        
        @Override
        public String toString(){
            String s = JAMS.i18n("from") + " " + range.getStart().toString() + " " + JAMS.i18n("to") + " " + range.getEnd().toString();
            return s;
        }
    }

    static public class SelectiveTimeFilter extends TimeFilter{
        HashSet<Long> set = new HashSet<Long>();

        private SelectiveTimeFilter(Date nonFilteredDates[]){
            for (Date d : nonFilteredDates){
                set.add(d.getTime());
            }
        }
        public boolean isFiltered(Date date) {
            return !set.contains(date.getTime());
        }
    }

    @SuppressWarnings("deprecation")
    static public class YearlyRangeTimeFilter extends TimeFilter{
        int startDay, endDay;

        private YearlyRangeTimeFilter(int startDay, int endDay){
            this.startDay = startDay;
            this.endDay = endDay;
        }
        public boolean isFiltered(Date date) {
            int day = date.getDay();
            return day >= startDay && day <= endDay;
        }
        
        @Override
        public String toString(){
            String s = JAMS.i18n("from_day") + " " + startDay + " " + JAMS.i18n("to") + " " + endDay;            
            return s;
        }
    }

    static public class CombinedTimeFilter extends TimeFilter{        
        TimeFilter filter[] = null;
        
        CombinedTimeFilter(TimeFilter[] filter){
            this.filter = filter;            
        }
        @Override
        public boolean isFiltered(Date date) {
            boolean isFiltered = true;
            for (TimeFilter f : filter){
                if (!f.isEnabled)
                    continue;
                boolean value = f.isFiltered(date);
                if (f.isInverted)
                    value = !value;
                if (f.isAdditive)
                    isFiltered |= value;
                else
                    isFiltered &= value;
            }

            return isFiltered;
        }
        
        @Override
        public String toString(){
            String s = JAMS.i18n("combined_filter") + "\n";
            for (TimeFilter f : filter){
                s += f + "\n";
            }
            return s;
        }
    }


    static public class EventFilter extends TimeFilter{
        public enum EventType{Peak, Recession, RaisingEdge}

        public static int DEFAULT_WINDOWSIZE=10;
        TimeSerie m;        
        int windowSize = DEFAULT_WINDOWSIZE;
        EventType type;
        TreeMap<Date,Boolean> map = new TreeMap<Date, Boolean>();
        double qualityThreshold = 0.0;

        ArrayList<HydrographEvent> list = null;

        private EventFilter(TimeSerie m, EventType type, int windowSize ){
            this.m = m;
            this.type = type;
            list = HydrographEvent.findEvents(m, windowSize);
            updateFilter();
            this.windowSize = windowSize;
        }

        private void updateFilter(){
            if (list == null)
                return;

            for (int i=0;i<m.getTimesteps();i++){
                Date d = m.getTime(i);
                map.put(d, Boolean.TRUE);
            }
            for (HydrographEvent evt : list){
                if (evt.getQuality() < qualityThreshold)
                    continue;

                if (type == EventType.RaisingEdge){
                    for (int i=evt.getRaisingEdge().startIndex;i<evt.getRaisingEdge().endIndex;i++){
                        Date d = m.getTime(i);
                        map.put(d, Boolean.FALSE);
                    }
                }else if (type == EventType.Recession){
                    for (int i=evt.getFallingEdge().startIndex;i<evt.getFallingEdge().endIndex;i++){
                        Date d = m.getTime(i);
                        map.put(d, Boolean.FALSE);
                    }
                }else if (type == EventType.Peak){
                    Date d = m.getTime(evt.getPeak().index);
                    map.put(d, Boolean.FALSE);
                }
            }
        }

        public Date getFirstDate(){
            return map.firstKey();
        }
        
        public Date getLastDate(){
            return map.lastKey();
        }
        
        @Override
        public boolean isFiltered(Date date) {
            return map.floorEntry(date).getValue();
        }

        public EventType getFilteredEventType(){
            return this.type;
        }
        public TimeSerie getTimeSerie(){
            return this.m;
        }        
        public int getWindowSize(){
            return this.windowSize;
        }
        public double getQualityThreshold(){
            return qualityThreshold;
        }
        public void setQualityThreshold(double qualityThreshold){
            this.qualityThreshold = qualityThreshold;
            this.updateFilter();
        }
        public double getMinQuality(){
            double minQuality = Double.MAX_VALUE;
            double maxQuality = Double.MIN_VALUE;

            for (HydrographEvent evt : list){
                minQuality = Math.min(minQuality, evt.getQuality());
                maxQuality = Math.max(maxQuality, evt.getQuality());
            }
            return minQuality;
        }
        public double getMaxQuality(){
            double maxQuality = Double.MIN_VALUE;

            for (HydrographEvent evt : list){
                maxQuality = Math.max(maxQuality, evt.getQuality());
            }
            return maxQuality;
        }

        @Override
        public String toString(){
            return JAMS.i18n("Hydrograph_event_filter_with_window_size:") + " " + windowSize + " " + JAMS.i18n("filtered_for:") + " " + type;
        }
    }

    static public class BaseFlowTimeFilter extends TimeFilter{        
        public enum Method{Fixed, HYSEPLocalMinimum};

        TimeSerie m;
        Method method;
        double threshold;
        TreeMap<Date,Boolean> map = new TreeMap<Date, Boolean>();

        private BaseFlowTimeFilter(TimeSerie m, Method method, double threshold ){
            this.m = m;
            this.method = method;
            switch (method){
                case Fixed:
                    for (int i=0;i<m.getTimesteps();i++){
                        Date d = m.getTime(i);
                        if (m.getValue(i)<=threshold)
                            map.put(d, Boolean.TRUE);
                        else
                            map.put(d, Boolean.FALSE);
                    }
                    break;
                case HYSEPLocalMinimum:
                    double[] result = BaseFlow.groundwaterWindowMethod(m);
                    for (int i=0;i<m.getTimesteps();i++){
                        Date d = m.getTime(i);
                        if (Math.abs(m.getValue(i)-result[i])<=threshold)
                            map.put(d, Boolean.TRUE);
                        else
                            map.put(d, Boolean.FALSE);
                    }
                    break;
            }
        }
        public boolean isFiltered(Date date) {
            return !map.floorEntry(date).getValue();
        }

        public TimeSerie getTimeSerie(){
            return this.m;
        }
        public Method getMethod(){
            return this.method;
        }
        public double getThreshold(){
            return this.threshold;
        }
        @Override
        public String toString(){
            return JAMS.i18n("Baseflowfilter_of")+ " " + this.m.name+"("+this.method+"t=" + this.threshold + ")";
        }
    }
    
    public static TimeFilter getYearlyFilter(int year[], boolean hydrologicYear){
        return new YearlyTimeFilter(year, hydrologicYear);
    }

    public static TimeFilter getMonthlyFilter(int months[]){
        return new MonthlyTimeFilter(months);
    }

    public static TimeFilter getRangeFilter(TimeInterval range){
        return new RangeTimeFilter(range);
    }

    public static TimeFilter getYearlyRangeTimeFilter(int dayStart, int dayEnd){
        return new YearlyRangeTimeFilter(dayStart, dayEnd);
    }

    static public EventFilter getEventFilter(TimeSerie m, EventFilter.EventType type, int windowSize){
        return new EventFilter(m, type, windowSize);
    }

    public static TimeFilter getBaseFlowTimeFilter(TimeSerie m, BaseFlowTimeFilter.Method method, double threshold){
        return new BaseFlowTimeFilter(m, method, threshold);
    }

    public static TimeFilter getCombinedTimeFilter(TimeFilter[]filter){
        return new CombinedTimeFilter(filter);
    }

    public static TimeFilter getSelectiveTimeFilter(Date[] dates){
        return new SelectiveTimeFilter(dates);
    }
}
