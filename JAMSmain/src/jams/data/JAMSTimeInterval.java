/*
 * JAMSTimeInterval.java
 * Created on 10. September 2005, 21:36
 *
 * This file is part of JAMS
 * Copyright (C) 2005 FSU Jena
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public License
 * as published by the Free Software Foundation; either version 3
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, see <http://www.gnu.org/licenses/>.
 *
 */
package jams.data;

import java.util.Calendar;
import java.util.NoSuchElementException;
import java.util.StringTokenizer;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author S. Kralisch
 */
public class JAMSTimeInterval implements Attribute.TimeInterval {

    protected Attribute.Calendar start = new JAMSCalendar();
    protected Attribute.Calendar end = new JAMSCalendar();
    protected int timeUnit = 6;
    protected int timeUnitCount = 1;
    private long timestepCount = -1;

    public JAMSTimeInterval() {
    }

    public JAMSTimeInterval(Attribute.Calendar start, Attribute.Calendar end, int timeUnit, int timeUnitCount) {
        this.start = start;
        this.end = end;
        this.timeUnit = timeUnit;
        this.timeUnitCount = timeUnitCount;
    }

    public void setValue(String value) {
        try {
            StringTokenizer st = new StringTokenizer(value);
            String startDate = st.nextToken();
            String startTime = st.nextToken();
            String endDate = st.nextToken();
            String endTime = st.nextToken();
            String unit = st.nextToken();
            String count = st.nextToken();

            start.setValue(startDate + " " + startTime);
            end.setValue(endDate + " " + endTime);
            timeUnit = Integer.parseInt(unit);
            timeUnitCount = Integer.parseInt(count);
        } catch (NoSuchElementException nsee) {
            Logger.getLogger(JAMSTimeInterval.class.getName()).log(Level.WARNING, "Unable to parse time interval " + value, nsee);
        } catch (Exception e) {
            Logger.getLogger(JAMSTimeInterval.class.getName()).log(Level.WARNING, "Errror while setting time interval " + value, e);
        }
    }

    public boolean encloses(Attribute.TimeInterval ti) {
        if ((this.start.compareTo(ti.getStart()) <= 0) && (this.end.compareTo(ti.getEnd()) >= 0)) {
            return true;
        } else {
            return false;
        }
    }

    public long getStartOffset(Attribute.TimeInterval ti) {

        if (!this.encloses(ti)) {
            return -1;
        }

        JAMSTimeInterval tmp = new JAMSTimeInterval();
        tmp.setValue(this.getValue());

        JAMSCalendar start = new JAMSCalendar();
        start.setValue(this.start);
        JAMSCalendar end = new JAMSCalendar();
        end.setValue(ti.getStart());
        tmp.setStart(start);
        tmp.setEnd(end);

        long offset = tmp.getNumberOfTimesteps() - 1;

        return offset;
    }

    public String getValue() {
        return start + " " + end + " " + timeUnit + " " + timeUnitCount;
    }

    public String toString() {
        return getValue();
    }

    public Attribute.Calendar getStart() {
        return start;
    }

    public void setStart(Attribute.Calendar start) {
        this.start = start;
        this.timestepCount = -1;
    }

    public Attribute.Calendar getEnd() {
        return end;
    }

    public void setEnd(Attribute.Calendar end) {
        this.end = end;
        this.timestepCount = -1;
    }

    public int getTimeUnit() {
        return timeUnit;
    }

    public void setTimeUnit(int timeUnit) {
        this.timeUnit = timeUnit;
        this.timestepCount = -1;
    }

    public int getTimeUnitCount() {
        return timeUnitCount;
    }

    public void setTimeUnitCount(int timeUnitCount) {
        this.timeUnitCount = timeUnitCount;
        this.timestepCount = -1;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof JAMSTimeInterval) {
            if (((JAMSTimeInterval) obj).start.compareTo(this.start, Calendar.MINUTE) == 0
                    || ((JAMSTimeInterval) obj).end.compareTo(this.end, Calendar.MINUTE) == 0) {
                return true;
            }
        }
        return false;
    }

    @Override
    public long getNumberOfTimesteps() {

        if (timestepCount < 0) {
            if (timeUnit <= Calendar.MONTH) {
                timestepCount = getNumberOfTimestepsByIteration();
            } else {
                timestepCount = getNumberOfTimestepsByCalculation();
            }
        }
        return timestepCount;
    }

    private long getNumberOfTimestepsByIteration() {

        Attribute.Calendar start = this.getStart().clone();
        Attribute.Calendar end = this.getEnd().clone();
//            end.setTimeInMillis(end.getTimeInMillis() + 1);

        long count = 1;
        start.add(timeUnit, timeUnitCount);

        while (!start.after(end)) {
            count++;
            start.add(timeUnit, timeUnitCount);
        }

        return count;
    }

    private long getNumberOfTimestepsByCalculation() {

        Attribute.Calendar start = this.getStart().clone();
        Attribute.Calendar end = this.getEnd().clone();

        double milli1 = end.getTimeInMillis() - start.getTimeInMillis();
        double milli2 = 0;

        start.set(1970, 0, 1, 0, 0, 0);
        end.set(1970, 0, 1, 0, 0, 0);
        end.add(timeUnit, timeUnitCount);

        milli2 = end.getTimeInMillis() - start.getTimeInMillis();

        return (long) (milli1 / milli2) + 1;
    }

    public static void main(String[] args) {

        JAMSTimeInterval ti = new JAMSTimeInterval();
        ti.setStart(new JAMSCalendar(0, 0, 1, 0, 0, 0));
        ti.setEnd(new JAMSCalendar(2018, 0, 1, 0, 0, 0));
        ti.setTimeUnit(Calendar.SECOND);
        ti.setTimeUnitCount(1);

        System.out.println(ti.getNumberOfTimesteps());

    }
}
