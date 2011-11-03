/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package optas.hydro.data;

import jams.data.Attribute.TimeInterval;
import java.util.Arrays;
import java.util.Date;
import java.util.TreeMap;

/**
 *
 * @author chris
 */
public class TimeFilterFactory {
    static public class YearlyTimeFilter extends TimeFilter{
        boolean years[];
        private YearlyTimeFilter(int years[]){
            this.years = new boolean[3000];
            for (int y : years){
                this.years[y] = true;
            }
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
        public boolean isFiltered(Date date) {
            return (years[date.getYear()]);
        }

        @Override
        public String toString(){
            return "Yearly filter, years [" + Arrays.toString(getYears()) + "]";
        }
    }

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
            return months[date.getMonth()];
        }
    }

    static public class RangeTimeFilter extends TimeFilter{
        TimeInterval range;

        private RangeTimeFilter(TimeInterval range){
            this.range = range;
        }
        public boolean isFiltered(Date date) {
            return date.after(range.getStart().getTime()) && date.before(range.getEnd().getTime());
        }
    }

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
    }

    static public class LowFlowTimeFilter extends TimeFilter{
        Measurement m;
        TreeMap<Date,Boolean> map = new TreeMap<Date, Boolean>();

        private LowFlowTimeFilter(Measurement m, double threshold){
            this.m = m;
            for (int i=0;i<m.getTimesteps();i++){
                Date d = m.getTime(i);
                if (m.getValue(i)<=threshold)
                    map.put(d, Boolean.TRUE);
                else
                    map.put(d, Boolean.FALSE);
            }
        }
        public boolean isFiltered(Date date) {
            return map.floorEntry(date).getValue();
        }
    }

    static public class RisingEdgeTimeFilter extends TimeFilter{
        Measurement m;
        TreeMap<Date,Boolean> map = new TreeMap<Date, Boolean>();

        private RisingEdgeTimeFilter(Measurement m){
            this.m = m;
            for (int i=0;i<m.getTimesteps();i++){
                Date d = m.getTime(i);
                
            }
        }
        public boolean isFiltered(Date date) {
            return map.floorEntry(date).getValue();
        }
    }

    public static TimeFilter getYearlyFilter(int year[]){
        return new YearlyTimeFilter(year);
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

    public static TimeFilter getLowFlowTimeFilter(Measurement m, double threshold){
        return new LowFlowTimeFilter(m, threshold);
    }

}
