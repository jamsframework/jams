/*
 * TSDataStore.java
 * Created on 23. Januar 2008, 15:53
 *
 * This file is part of JAMS
 * Copyright (C) FSU Jena
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
package rbis.virtualws.stores;

import org.unijena.jams.data.JAMSCalendar;
import rbis.virtualws.*;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import rbis.virtualws.datatypes.CalendarValue;
import rbis.virtualws.datatypes.DataValue;

/**
 *
 * @author Sven Kralisch
 */
public class TSDataStore extends TableDataStore {

    private CalendarValue calendar;
    private JAMSCalendar currentDate, endDate;
    private int timeUnit,  timeUnitCount;

    public TSDataStore(VirtualWorkspace ws, Document doc) {
        super(ws, doc);

        Element tiNode = (Element) doc.getElementsByTagName("timeinterval").item(0);
        Element startElement = (Element) tiNode.getElementsByTagName("start").item(0);
        Element endElement = (Element) tiNode.getElementsByTagName("end").item(0);
        Element stepsizeElement = (Element) tiNode.getElementsByTagName("stepsize").item(0);
        Element timeFormatElement = (Element) tiNode.getElementsByTagName("timeformat").item(0);

        String timeFormat = "%1$tY-%1$tm-%1$td %1$tH:%1$tM";
        if (timeFormatElement != null) {
            timeFormat = timeFormatElement.getAttribute("value");
        }

        JAMSCalendar startDate = new JAMSCalendar();
        startDate.setValue(startElement.getAttribute("value"));

        timeUnit = Integer.parseInt(stepsizeElement.getAttribute("unit"));
        timeUnitCount = Integer.parseInt(stepsizeElement.getAttribute("count"));        
        
        endDate = new JAMSCalendar();
        endDate.setValue(endElement.getAttribute("value"));
        endDate.add(timeUnit, -1*timeUnitCount);

        currentDate = new JAMSCalendar();
        currentDate.setFormatString(timeFormat);
        currentDate.setValue(startDate);
        currentDate.add(timeUnit, -1 * timeUnitCount);
        calendar = new CalendarValue(currentDate);


        int oldBufferSize = bufferSize;
        if (bufferSize < 2) {
            bufferSize = 2;
        }
        fillBuffer();
        if (maxPosition >= 2) {

            // check interval size for all columns
            for (int i = 0; i < dataIOArray.length; i++) {

                //get the timestamps of the first two rows
                long timeStamp1 = dataIOArray[i].getValues()[0].getData()[0].getLong();
                long timeStamp2 = dataIOArray[i].getValues()[1].getData()[0].getLong();

                //compare the two time stamps
                JAMSCalendar cal1 = new JAMSCalendar();
                cal1.setTimeInMillis(timeStamp1 * 1000);
                JAMSCalendar cal2 = new JAMSCalendar();
                cal2.setTimeInMillis(timeStamp2 * 1000);

                cal1.add(timeUnit, timeUnitCount);
                if (cal1.compareTo(cal2) != 0) {

                    JAMSCalendar cal = cal1.clone();
                    cal.add(timeUnit, -1 * timeUnitCount);
                    long demandedSeconds = Math.abs(cal1.getTimeInMillis() - cal.getTimeInMillis()) / 1000;
                    long currentSeconds = Math.abs(cal.getTimeInMillis() - cal2.getTimeInMillis()) / 1000;

                    this.ws.getRuntime().sendErrorMsg("Error in " + this.getClass().getName() + ": wrong time interval in column " + i + " (demanded interval = " + demandedSeconds + " sec, provided interval = " + currentSeconds + " sec)!");

                    dataIOSet.clear();
                    currentPosition = maxPosition;
                }

            }

            // check identical start date of all columns

            // for all but the first columns
            for (int i = 0; i < dataIOArray.length; i++) {

                long timeStamp2 = dataIOArray[i].getValues()[0].getData()[0].getLong();

                //compare the two time stamps
                JAMSCalendar cal = new JAMSCalendar();
                cal.setTimeInMillis(timeStamp2 * 1000);

                if (cal.compareTo(startDate, timeUnit) != 0) {

                    this.ws.getRuntime().sendErrorMsg("Error in " + this.getClass().getName() + ": wrong start time in column " + i + " (demanded = \"" + startDate + "\", provided = \"" + cal + "\")!");

                    dataIOSet.clear();
                    currentPosition = maxPosition;
                }
            }


        }
//        System.exit(-1);
        bufferSize = oldBufferSize;
    }

    @Override
    public boolean hasNext() {
        if (currentDate.after(endDate)) {
            return false;
        }
        return super.hasNext();
    }

    @Override
    public DataSet getNext() {

        DataSet result = new DataSet(positionArray.length + 1);

        currentDate.add(timeUnit, timeUnitCount);
        result.setData(0, calendar);

        for (int i = 0; i < dataIOArray.length; i++) {

            DataSet ds = dataIOArray[i].getValues()[currentPosition];
            DataValue[] values = ds.getData();
            result.setData(i + 1, values[positionArray[i]]);

        }

        currentPosition++;

        return result;
    }
}

