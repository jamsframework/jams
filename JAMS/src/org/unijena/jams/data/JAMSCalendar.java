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

package org.unijena.jams.data;

import java.util.*;

/**
 *
 * @author S. Kralisch
 */

public class JAMSCalendar extends GregorianCalendar  implements JAMSData {
    
    static final long serialVersionUID = -1728624836010418234L;
    
    public JAMSCalendar() {
        super();
        this.setTimeInMillis(0);
        java.util.SimpleTimeZone stz = new java.util.SimpleTimeZone(3600000, "MEZsimple");
        this.setTimeZone(stz);
    }
    
    public JAMSCalendar(int year, int month, int dayOfMonth, int hourOfDay, int minute, int second) {
        super(year, month, dayOfMonth, hourOfDay, minute, second);
        java.util.SimpleTimeZone stz = new java.util.SimpleTimeZone(3600000, "MEZsimple");
        this.setTimeZone(stz);
    }
    
    public JAMSCalendar clone() {
        return new JAMSCalendar(get(YEAR), get(MONTH), get(DAY_OF_MONTH), get(HOUR_OF_DAY), get(MINUTE), get(SECOND));
    }
    
    public String toString() {
        return String.format("%1$tY-%1$tm-%1$td %1$tH:%1$tM", this);
    }
    
    public String toString(String formatString) {
        return String.format(formatString, this);
    }
    
    public GregorianCalendar getValue() {
        return clone();
    }
    
    public int compareTo(JAMSCalendar cal, int accuracy) {
        
        JAMSCalendar clone = this.clone();
        
        //we won't ever be interested in milliseconds ;)
        clone.set(JAMSCalendar.MILLISECOND, cal.get(JAMSCalendar.MILLISECOND));
        
        if(accuracy < JAMSCalendar.SECOND)
            clone.set(JAMSCalendar.SECOND, cal.get(JAMSCalendar.SECOND));
        if(accuracy < JAMSCalendar.MINUTE)
            clone.set(JAMSCalendar.MINUTE, cal.get(JAMSCalendar.MINUTE));
        if(accuracy < JAMSCalendar.HOUR_OF_DAY)
            clone.set(JAMSCalendar.HOUR_OF_DAY, cal.get(JAMSCalendar.HOUR_OF_DAY));
        if(accuracy < JAMSCalendar.DAY_OF_YEAR)
            clone.set(JAMSCalendar.DAY_OF_YEAR, cal.get(JAMSCalendar.DAY_OF_YEAR));
        if(accuracy < JAMSCalendar.MONTH)
            clone.set(JAMSCalendar.MONTH, cal.get(JAMSCalendar.MONTH));
        
        return clone.compareTo(cal);
    }
    
    public void setValue(GregorianCalendar cal) {
        setTimeInMillis(cal.getTimeInMillis());
    }
    
    public void setValue(String value) {
        
        String year = "1900";
        String month = "1";
        String day = "1";
        String hour = "0";
        String minute = "0";
        String second = "0";
        String millisecond = "0";
        
        StringTokenizer st = new StringTokenizer(value, " :-/");
        year = st.nextToken();
        if (st.hasMoreTokens())
            month = st.nextToken().trim();
        if (st.hasMoreTokens())
            day = st.nextToken().trim();
        if (st.hasMoreTokens())
            hour = st.nextToken().trim();
        if (st.hasMoreTokens())
            minute = st.nextToken().trim();
        if (st.hasMoreTokens())
            second = st.nextToken().trim();
        if (st.hasMoreTokens())
            millisecond = st.nextToken().trim();
        
        try {
            set(YEAR, Integer.parseInt(year));
            set(MONTH, Integer.parseInt(month)-1);
            set(DAY_OF_MONTH, Integer.parseInt(day));
            set(HOUR_OF_DAY, Integer.parseInt(hour));
            set(MINUTE, Integer.parseInt(minute));
            set(SECOND, Integer.parseInt(second));
            set(MILLISECOND, Integer.parseInt(millisecond));
        } catch (NumberFormatException nfe) {
            org.unijena.jams.JAMS.handle(nfe);
        }
    }
    
}
