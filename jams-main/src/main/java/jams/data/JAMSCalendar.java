/*
 * Attribute.Calendar.java
 * Created on 31. Juli 2005, 20:38
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

import java.beans.BeanInfo;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 *
 * @author S. Kralisch
 */
public class JAMSCalendar extends GregorianCalendar implements Attribute.Calendar {

    transient private final static String FORMAT_PATTERN = Attribute.Calendar.DEFAULT_FORMAT_PATTERN;
    transient private final static TimeZone TIME_ZONE = Attribute.Calendar.DEFAULT_TIME_ZONE;
    private DateFormat dateFormat;

    {
        try {
            BeanInfo info = Introspector.getBeanInfo(JAMSCalendar.class);
            PropertyDescriptor[] propertyDescriptors
                    = info.getPropertyDescriptors();
            for (int i = 0; i < propertyDescriptors.length; ++i) {
                PropertyDescriptor pd = propertyDescriptors[i];
                if (!pd.getName().equals("milliSeconds")) {
                    pd.setValue("transient", Boolean.TRUE);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public JAMSCalendar() {
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
        this.setTimeZone(DEFAULT_TIME_ZONE);
        dateFormat = new SimpleDateFormat(FORMAT_PATTERN);
        dateFormat.setTimeZone(DEFAULT_TIME_ZONE);
    }

    @Override
    public void setTimeZone(TimeZone timezone) {
        super.setTimeZone(timezone);
        dateFormat = new SimpleDateFormat(FORMAT_PATTERN);
        dateFormat.setTimeZone(timezone);
    }

    @Override
    public JAMSCalendar clone() {
        return new JAMSCalendar(get(Attribute.Calendar.YEAR), get(Attribute.Calendar.MONTH), get(Attribute.Calendar.DAY_OF_MONTH), get(Attribute.Calendar.HOUR_OF_DAY), get(Attribute.Calendar.MINUTE), get(Attribute.Calendar.SECOND));
    }

    @Override
    public String toString() {
        Date date = new Date();
        date.setTime(this.getTimeInMillis());
        return dateFormat.format(date);
    }

    @Override
    public String toString(DateFormat dateFormat) {
        Date date = new Date();
        date.setTime(this.getTimeInMillis());
        dateFormat.setTimeZone(DEFAULT_TIME_ZONE);
        return dateFormat.format(date);
    }

    @Override
    public Attribute.Calendar getValue() {
        return clone();
    }

    /**
     * Compares the date represented by this object with the date represented by
     * another JAMSCalendar object while considering a given accuracy. Example:
     * The JAMSCalendar object repesenting the date "1979-07-04 07:30" equals
     * another object representing the date "1979-07-12 04:00", if the accuracy
     * is "MONTH", but is earlier if the accuracy is "DAY_OF_MONTH"
     *
     * @param cal The calendar object to compare with
     * @param accuracy The accuracy as field of the calendar object (e.g.
     * SECOND, MINUTE, HOUR_OF_DAY, DAY_OF_MONTH or MONTH)
     * @return -1 if this calendar represents an earlier date than cal, 0 if
     * this calendar equals cal or 1 if this calendar represents a later date
     * than cal, always leaving unsignificant fields unconsidered.
     */
    @Override
    public int compareTo(Attribute.Calendar cal, int accuracy) {
        Attribute.Calendar cal1 = this.clone();
        Attribute.Calendar cal2 = cal.clone();
        cal1.removeUnsignificantComponents(accuracy);
        cal2.removeUnsignificantComponents(accuracy);
        return cal1.compareTo(cal2);
    }

    @Override
    public void removeUnsignificantComponents(int accuracy) {
        if (accuracy < Attribute.Calendar.MILLISECOND) {
            this.set(Attribute.Calendar.MILLISECOND, 0);
        }
        if (accuracy < Attribute.Calendar.SECOND) {
            this.set(Attribute.Calendar.SECOND, 0);
        }
        if (accuracy < Attribute.Calendar.MINUTE) {
            this.set(Attribute.Calendar.MINUTE, 0);
        }
        if (accuracy < Attribute.Calendar.HOUR_OF_DAY) {
            this.set(Attribute.Calendar.HOUR_OF_DAY, 0);
        }
        if (accuracy < Attribute.Calendar.DAY_OF_MONTH) {
            this.set(Attribute.Calendar.DAY_OF_MONTH, 1);
        }
        if (accuracy < Attribute.Calendar.MONTH) {
            this.set(Attribute.Calendar.MONTH, 0);
        }
    }

    @Override
    public void setValue(Attribute.Calendar cal) {
        setTimeInMillis(cal.getTimeInMillis());
        set(Calendar.MILLISECOND, 0);
    }
    
    @Override
    public void setValue(String value) {
        
        int[] englishFormat = {2, 0, 1, 3, 4, 5, 6};
        int[] jamsFormat = {0, 1, 2, 3, 4, 5, 6};

        String[] dateStrings = {"1970", "1", "1", "0", "0", "0", "0"};
        String[] parts = value.split("\\s+|:|-|/|\\.");
        
        int[] index;
        if (value.contains("-")) {
            index = jamsFormat;
        } else {
            index = englishFormat;
        }
        
        for (int i = 0; i < parts.length; i++) {
            dateStrings[i] = parts[index[i]];
        }
        
        try {
            set(Attribute.Calendar.YEAR, Integer.parseInt(dateStrings[0]));
        } catch (NumberFormatException nfe) {
            jams.tools.JAMSTools.handle(nfe);
        }
        try {
            set(Attribute.Calendar.MONTH, Integer.parseInt(dateStrings[1]) - 1);
        } catch (NumberFormatException nfe) {
            jams.tools.JAMSTools.handle(nfe);
        }
        try {
            set(Attribute.Calendar.DAY_OF_MONTH, Integer.parseInt(dateStrings[2]));
        } catch (NumberFormatException nfe) {
            jams.tools.JAMSTools.handle(nfe);
        }

        try {
            set(Attribute.Calendar.HOUR_OF_DAY, Integer.parseInt(dateStrings[3]));
        } catch (NumberFormatException nfe) {
            jams.tools.JAMSTools.handle(nfe);
        }
        try {
            set(Attribute.Calendar.MINUTE, Integer.parseInt(dateStrings[4]));
        } catch (NumberFormatException nfe) {
            jams.tools.JAMSTools.handle(nfe);
        }
        try {
            set(Attribute.Calendar.SECOND, Integer.parseInt(dateStrings[5]));
        } catch (NumberFormatException nfe) {
            jams.tools.JAMSTools.handle(nfe);
        }
        try {
            set(Attribute.Calendar.MILLISECOND, Integer.parseInt(dateStrings[6]));
        } catch (NumberFormatException nfe) {
            jams.tools.JAMSTools.handle(nfe);
        }
    }    
    
    @Override
    public void setValue(String value, String format) throws ParseException {
        DateFormat fromFormat = new SimpleDateFormat(format);
        fromFormat.setTimeZone(this.getTimeZone());
        Date date = fromFormat.parse(value);
        this.setTimeInMillis(date.getTime());
    }

    @Override
    public void setDateFormat(String formatString) {
        dateFormat = new SimpleDateFormat(formatString);
        dateFormat.setTimeZone(this.getTimeZone());
    }

    public void setDateFormat(DateFormat dateFormat) {
        this.dateFormat = dateFormat;
    }

    @Override
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
