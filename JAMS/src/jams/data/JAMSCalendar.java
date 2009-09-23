/*
 * JAMSCalendar.java
 * Created on 31. Juli 2005, 20:38
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

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 *
 * @author S. Kralisch
 */
public class JAMSCalendar extends GregorianCalendar implements Attribute.Calendar {

    public final static String DATE_TIME_FORMAT = "yyyy-MM-dd HH:mm";

    public final static TimeZone STANDARD_TIME_ZONE = new SimpleTimeZone(0, "GMT");

    private DateFormat dateFormat;

    JAMSCalendar() {
        super();
        this.setTimeInMillis(0);
        initTZ();
    }

    JAMSCalendar(int year, int month, int dayOfMonth, int hourOfDay, int minute, int second) {
        super(year, month, dayOfMonth, hourOfDay, minute, second);
        set(Calendar.MILLISECOND, 0);
        initTZ();
    }

    private void initTZ() {
        this.setTimeZone(STANDARD_TIME_ZONE);
        dateFormat = new SimpleDateFormat(DATE_TIME_FORMAT);
        dateFormat.setTimeZone(STANDARD_TIME_ZONE);
    }

    public JAMSCalendar clone() {
        return new JAMSCalendar(get(YEAR), get(MONTH), get(DAY_OF_MONTH), get(HOUR_OF_DAY), get(MINUTE), get(SECOND));
    }

    public String toString() {
        Date date = new Date();
        date.setTime(this.getTimeInMillis());
        return dateFormat.format(date);
    }

    public String toString(DateFormat dateFormat) {
        Date date = new Date();
        date.setTime(this.getTimeInMillis());
        return dateFormat.format(date);
    }

    public Attribute.Calendar getValue() {
        return clone();
    }
    
    /**
     * Compares the date represented by this object with the date represented by 
     * another JAMSCalendar object while considering a given accuracy. Example: 
     * The JAMSCalendar object repesenting the date "1979-07-04 07:30" equals 
     * another object representing the date "1979-07-12 04:00", if the accuracy
     * is "MONTH", but is earlier if the accuracy is "DAY_OF_MONTH"
     * @param cal The calendar object to compare with
     * @param accuracy The accuracy as field of the calendar object (e.g. 
     * SECOND, MINUTE, HOUR_OF_DAY, DAY_OF_MONTH or MONTH)
     * @return -1 if this calendar represents an earlier date than cal, 0 if 
     * this calendar equals cal or 1 if this calendar represents a later date 
     * than cal, always leaving unsignificant fields unconsidered.
     */
    public int compareTo(Attribute.Calendar cal, int accuracy) {
        Attribute.Calendar cal1 = this.clone();
        Attribute.Calendar cal2 = cal.clone();
        cal1.removeUnsignificantComponents(accuracy);
        cal2.removeUnsignificantComponents(accuracy);
        return cal1.compareTo(cal2);
    }

    @Override
    public void removeUnsignificantComponents(int accuracy) {
        if (accuracy < JAMSCalendar.SECOND) {
            this.set(JAMSCalendar.SECOND, 0);
        }
        if (accuracy < JAMSCalendar.MINUTE) {
            this.set(JAMSCalendar.MINUTE, 0);
        }
        if (accuracy < JAMSCalendar.HOUR_OF_DAY) {
            this.set(JAMSCalendar.HOUR_OF_DAY, 0);
        }
        if (accuracy < JAMSCalendar.DAY_OF_MONTH) {
            this.set(JAMSCalendar.DAY_OF_MONTH, 1);
        }
        if (accuracy < JAMSCalendar.MONTH) {
            this.set(JAMSCalendar.MONTH, 0);
        }
    }

    @Override
    public void setValue(Attribute.Calendar cal) {
        setTimeInMillis(cal.getTimeInMillis());
        set(Calendar.MILLISECOND, 0);
    }

    @Override
    public void setValue(String value) {

        String year = "1900";
        String month = "1";
        String day = "1";
        String hour = "0";
        String minute = "0";
        String second = "0";
        String millisecond = "0";

        StringTokenizer st = new StringTokenizer(value, " :-/.");
        year = st.nextToken();
        if (st.hasMoreTokens()) {
            month = st.nextToken().trim();
        }
        if (st.hasMoreTokens()) {
            day = st.nextToken().trim();
        }
        if (st.hasMoreTokens()) {
            hour = st.nextToken().trim();
        }
        if (st.hasMoreTokens()) {
            minute = st.nextToken().trim();
        }
        if (st.hasMoreTokens()) {
            second = st.nextToken().trim();
        }
        if (st.hasMoreTokens()) {
            millisecond = st.nextToken().trim();
        }

        try {
            set(YEAR, Integer.parseInt(year));
        } catch (NumberFormatException nfe) {
            jams.JAMSTools.handle(nfe);
        }
        try {
            set(MONTH, Integer.parseInt(month) - 1);
        } catch (NumberFormatException nfe) {
            jams.JAMSTools.handle(nfe);
        }
        try {
            set(DAY_OF_MONTH, Integer.parseInt(day));
        } catch (NumberFormatException nfe) {
            jams.JAMSTools.handle(nfe);
        }
        try {
            set(HOUR_OF_DAY, Integer.parseInt(hour));
        } catch (NumberFormatException nfe) {
            jams.JAMSTools.handle(nfe);
        }
        try {
            set(MINUTE, Integer.parseInt(minute));
        } catch (NumberFormatException nfe) {
            jams.JAMSTools.handle(nfe);
        }
        try {
            set(SECOND, Integer.parseInt(second));
        } catch (NumberFormatException nfe) {
            jams.JAMSTools.handle(nfe);
        }
        try {
            set(MILLISECOND, Integer.parseInt(millisecond));
        } catch (NumberFormatException nfe) {
            jams.JAMSTools.handle(nfe);
        }
    }

    public void setValue(String value, String format) throws ParseException {
        DateFormat fromFormat = new SimpleDateFormat(format);
        fromFormat.setTimeZone(this.getTimeZone());
        Date date = fromFormat.parse(value);
        this.setTimeInMillis(date.getTime());
    }

    public void setDateFormat(String formatString) {
        dateFormat = new SimpleDateFormat(formatString);
        dateFormat.setTimeZone(this.getTimeZone());
    }

    public DateFormat getDateFormat() {
        return dateFormat;
    }
    
    @Override
    public boolean after(Attribute.Calendar calendar) {
        return super.after(calendar);
    }

    @Override
    public boolean before(Attribute.Calendar calendar) {
        return super.before(calendar);
    }

    @Override
    public int compareTo(Attribute.Calendar cal) {
        long thisTime = this.getTimeInMillis();
        long calTime = cal.getTimeInMillis();
	return (thisTime > calTime) ? 1 : (thisTime == calTime) ? 0 : -1;
    }

    @Override
    public int get(int field) {
        return super.get(field);
    }

    @Override
    public void add(int field, int amount) {
        super.add(field, amount);
    }
}
