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

import java.io.Serializable;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 *
 * @author S. Kralisch
 */
public class JAMSCalendar extends GregorianCalendar implements JAMSData, Serializable {

    public final static String DATE_TIME_FORMAT = "yyyy-MM-dd HH:mm";

    public final static TimeZone STANDARD_TIME_ZONE = new SimpleTimeZone(0, "GMT");

    private DateFormat dateFormat;

    public JAMSCalendar() {
        super();
        this.setTimeInMillis(0);
        initTZ();
    }

    public JAMSCalendar(int year, int month, int dayOfMonth, int hourOfDay, int minute, int second) {
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

    public GregorianCalendar getValue() {
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
    public int compareTo(JAMSCalendar cal, int accuracy) {
        JAMSCalendar cal1 = this.clone();
        JAMSCalendar cal2 = cal.clone();
        cal1.removeUnsignificantComponents(accuracy);
        cal2.removeUnsignificantComponents(accuracy);
        return cal1.compareTo(cal2);
    }

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

    public static void main(String[] args) throws Exception {

        JAMSCalendar cal1 = new JAMSCalendar();
        cal1.setValue("4.7.1979 07:30", "dd.MM.yyyy HH:mm");
        JAMSCalendar cal2 = new JAMSCalendar();
        cal2.setValue("12.7.1979 04:00", "dd.MM.yyyy HH:mm");        
        System.out.println(cal1.compareTo(cal2, DAY_OF_MONTH));
        System.out.println(cal1);
        System.out.println(cal2);
        
//        for (int i = 0; i < 100000; i++) {
//            cal.toString();
//            //cal.toString("%1$tY-%1$tm-%1$td %1$tH:%1$tM");
//        }       
    }

    public void setValue(GregorianCalendar cal) {
        setTimeInMillis(cal.getTimeInMillis());
        set(Calendar.MILLISECOND, 0);
    }

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
            set(MONTH, Integer.parseInt(month) - 1);
            set(DAY_OF_MONTH, Integer.parseInt(day));
            set(HOUR_OF_DAY, Integer.parseInt(hour));
            set(MINUTE, Integer.parseInt(minute));
            set(SECOND, Integer.parseInt(second));
            set(MILLISECOND, Integer.parseInt(millisecond));
        } catch (NumberFormatException nfe) {
            jams.JAMS.handle(nfe);
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
}
