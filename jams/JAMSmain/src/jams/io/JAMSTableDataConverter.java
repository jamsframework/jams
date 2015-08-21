/*
 * JAMSTableDataConverter.java
 *
 * Created on 5. Oktober 2005, 20:14
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
package jams.io;

import jams.data.*;
import java.util.*;

/**
 *
 * @author S. Kralisch
 */
public class JAMSTableDataConverter {
    
    
    public static double[] toDouble(JAMSTableDataArray a) {
        double[] d = new double[a.getValues().length];
        for (int i=0; i < a.getValues().length; i++) {
            d[i] = Double.parseDouble(a.getValues()[i]);
        }
        return d;
    }
    
    public static double[] toDouble(JAMSTableDataArray a, int start, int end) {
        double[] d = new double[end-start+1];
        int c = 0;
        for (int i=start-1; i < end; i++) {
            d[c++] = Double.parseDouble(a.getValues()[i]);
        }
        return d;
    }

    public static double[] toDouble(JAMSTableDataArray a, int start) {
        return toDouble(a, start, a.getLength());
    }
    
    public static int[] toInt(JAMSTableDataArray a) {
        int[] d = new int[a.getValues().length];
        for (int i=0; i < a.getValues().length; i++) {
            d[i] = Integer.parseInt(a.getValues()[i]);
        }
        return d;
    }
    
    public static Attribute.Calendar parseTime(String timeString) {
        
        //Array keeping values for year, month, day, hour, minute
        int maxTimeElements = 5;
        String[] timeArray = new String[maxTimeElements];
        timeArray[0] = "0";
        timeArray[1] = "1";
        timeArray[2] = "1";
        timeArray[3] = "00";
        timeArray[4] = "00";
        
        StringTokenizer st = new StringTokenizer(timeString, ".-/ :");
        int n = st.countTokens();
        if (n > maxTimeElements) {
            n = maxTimeElements;
        }
        
        for (int i = 0; i < n; i++) {
            timeArray[i] = st.nextToken();
        }
        
        Attribute.Calendar cal = DefaultDataFactory.getDataFactory().createCalendar();
        cal.setValue(timeArray[0]+"-"+timeArray[1]+"-"+timeArray[2]+" "+timeArray[3]+":"+timeArray[4]);
        return cal;
    }
}


