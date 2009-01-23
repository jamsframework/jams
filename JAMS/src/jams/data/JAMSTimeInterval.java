/*
 * JAMSTimeInterval.java
 * Created on 10. September 2005, 21:36
 *
 * This file is part of JAMS
 * Copyright (C) 2005 FSU Jena
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA
 *
 */
package jams.data;

import java.io.Serializable;
import java.util.StringTokenizer;

/**
 *
 * @author S. Kralisch
 */
public class JAMSTimeInterval implements Attribute.TimeInterval, Serializable {

    protected JAMSCalendar start = new JAMSCalendar();
    protected JAMSCalendar end = new JAMSCalendar();
    protected int timeUnit = 6;
    protected int timeUnitCount = 1;
    private long timestepCount = -1;

    public JAMSTimeInterval() {
    }

    public JAMSTimeInterval(JAMSCalendar start, JAMSCalendar end, int timeUnit, int timeUnitCount) {
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
        } catch (Exception ex) {
        }
    }

    public boolean encloses(JAMSTimeInterval ti) {
        if ((this.start.compareTo(ti.start) <= 0) && (this.end.compareTo(ti.end) >= 0)) {
            return true;
        } else {
            return false;
        }
    }

    public long getStartOffset(JAMSTimeInterval ti) {

        if (!this.encloses(ti)) {
            return -1;
        }

        JAMSTimeInterval tmp = new JAMSTimeInterval();
        tmp.setValue(this.getValue());

        JAMSCalendar start = new JAMSCalendar();
        start.setValue(this.start);
        JAMSCalendar end = new JAMSCalendar();
        end.setValue(ti.start);
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

    public JAMSCalendar getStart() {
        return start;
    }

    public void setStart(JAMSCalendar start) {
        this.start = start;
        this.timestepCount = -1;
    }

    public JAMSCalendar getEnd() {
        return end;
    }

    public void setEnd(JAMSCalendar end) {
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

    public long getNumberOfTimesteps() {

        if (timestepCount < 0) {

            long count = 0;

            JAMSCalendar start = new JAMSCalendar();
            start.setValue(this.getStart().getValue());
            JAMSCalendar end = new JAMSCalendar();
            end.setValue(this.getEnd().getValue());
            end.setTimeInMillis(end.getTimeInMillis() + 1);

            while (start.before(end)) {
                start.add(timeUnit, timeUnitCount);
                count++;
            }

            timestepCount = count;
        }
        return timestepCount;
    }
}
