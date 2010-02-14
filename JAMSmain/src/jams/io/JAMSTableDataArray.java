/*
 * JAMSTableDataArray.java
 *
 * Created on 5. Oktober 2005, 17:19
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

    private JAMSCalendar time;

    private String[] values;

    public JAMSTableDataArray(JAMSCalendar time, String[] values) {
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
            String timeString = parts[1];   // maybe we have time?
            int dataReadIndex = 2;
            String dateFormat = JAMSTools.DATE_FORMAT_PATTERN_DE;
            if (timeString.indexOf(":") > -1) {     // yes we have a time.
                dateString += " " + timeString;
                valueNumber = parts.length - 2;
                dateFormat = JAMSTools.DATE_TIME_FORMAT_PATTERN_DE;
            } else {                                // no time, but data
                dataReadIndex = 1;
            }

            JAMSCalendar cal = JAMSDataFactory.createCalendar();
            cal.setValue(dateString, dateFormat);
            this.setTime(cal);

            String[] theValues = new String[valueNumber];
            for (int i = 0; i < valueNumber; i++) {
                theValues[i] = parts[dataReadIndex];
                dataReadIndex++;
            }
            this.setValues(theValues);
        }
    }

    public JAMSCalendar getTime() {
        return time;
    }

    public void setTime(JAMSCalendar time) {
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


