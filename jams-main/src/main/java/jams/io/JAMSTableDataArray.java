/*
 * JAMSTableDataArray.java
 *
 * Created on 5. Oktober 2005, 17:19
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

import java.io.Serializable;
import jams.data.*;
import jams.tools.JAMSTools;
import java.text.ParseException;

/**
 *
 * @author S. Kralisch
 */
public class JAMSTableDataArray implements Serializable {

    private Attribute.Calendar time;

    private String[] values;

    public JAMSTableDataArray(Attribute.Calendar time, String[] values) {
        this.time = time;
        this.setValues(values);
    }

    /**
     * constructor with dataLine
     * @param dataLine (e.g.  01.11.1990	6.391	6.525	6.003)
     * @throws ParseException
     */
    public JAMSTableDataArray(String dataLine) throws ParseException {

        String[] parts = dataLine.split("\\s+"); // split with whitespaces
        int valueNumber = parts.length - 1;
        if (parts.length > 1) {
            String dateString = parts[0];   // date
            String timeString = parts[1];   // time
            int dataReadIndex = 2;
            
            dateString += " " + timeString;
            valueNumber = parts.length - 2;

            Attribute.Calendar cal = DefaultDataFactory.getDataFactory().createCalendar();
            cal.setValue(dateString, Attribute.Calendar.DEFAULT_FORMAT_PATTERN);
            this.setTime(cal);

            String[] theValues = new String[valueNumber];
            for (int i = 0; i < valueNumber; i++) {
                theValues[i] = parts[dataReadIndex];
                dataReadIndex++;
            }
            this.setValues(theValues);
        }
    }

    public Attribute.Calendar getTime() {
        return time;
    }

    public void setTime(Attribute.Calendar time) {
        this.time = time;
    }

    public String[] getValues() {
        return values;
    }

    public void setValues(String[] values) {
        this.values = values;
    }

    public int getLength() {
        return values.length;
    }

    @Override
    public String toString() {
        String result = "";
        if (time == null) {
            result += "no time\n";
        } else {
            result += "time  : " + time.toString() + "\n";
        }
        if (values == null) {
            result += "no values\n";
        } else {
            result += "values: ";
            boolean firstValue = true;
            for (String value : values) {
                if (firstValue) {
                    firstValue = false;
                } else {
                    result += ",";
                }
                result += "<" + value + ">";
            }
        }
        return result;
    }
}


