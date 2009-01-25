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
package jams.workspace.stores;

import jams.data.JAMSCalendar;
import jams.workspace.*;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import jams.workspace.datatypes.CalendarValue;
import jams.workspace.datatypes.DataValue;
import jams.JAMS;
import jams.data.JAMSDataFactory;

/**
 *
 * @author Sven Kralisch
 */
public class TSDataStore extends TableDataStore {

    protected CalendarValue calendar;
    protected JAMSCalendar currentDate,  startDate,  endDate,  stopDate;
    protected int timeUnit,  timeUnitCount;
    protected String timeFormat;

    public TSDataStore(JAMSWorkspace ws) {
        super(ws);
        startDate = JAMSDataFactory.getCalendar();
        endDate = JAMSDataFactory.getCalendar();
        currentDate = JAMSDataFactory.getCalendar();
        calendar = new CalendarValue(currentDate);
    }

    public TSDataStore(JAMSWorkspace ws, String id, Document doc) throws ClassNotFoundException {
        super(ws, id, doc);

        Element tiNode = (Element) doc.getElementsByTagName("timeinterval").item(0);
        Element startElement = (Element) tiNode.getElementsByTagName("start").item(0);
        Element endElement = (Element) tiNode.getElementsByTagName("end").item(0);
        Element stepsizeElement = (Element) tiNode.getElementsByTagName("stepsize").item(0);
        Element timeFormatElement = (Element) tiNode.getElementsByTagName("dumptimeformat").item(0);

        timeFormat = JAMSCalendar.DATE_TIME_FORMAT;
        if (timeFormatElement != null) {
            timeFormat = timeFormatElement.getAttribute("value");
        }

        startDate = JAMSDataFactory.getCalendar();
        startDate.setValue(startElement.getAttribute("value"));

        timeUnit = Integer.parseInt(stepsizeElement.getAttribute("unit"));
        timeUnitCount = Integer.parseInt(stepsizeElement.getAttribute("count"));

        endDate = JAMSDataFactory.getCalendar();
        endDate.setValue(endElement.getAttribute("value"));
        stopDate = endDate.clone();
        stopDate.add(timeUnit, -1 * timeUnitCount);

        currentDate = JAMSDataFactory.getCalendar();
        currentDate.setDateFormat(timeFormat);
        currentDate.setValue(startDate);

        int oldBufferSize = bufferSize;
        if (bufferSize == 1) {
            bufferSize = 2;
        }
        fillBuffer();
        if (maxPosition >= 2) {

            // check interval size for all columns
            for (int i = 0; i < dataIOArray.length; i++) {

                //get the timestamps of the first two rows
                long timeStamp1 = dataIOArray[i].getData()[0].getData()[0].getLong();
                long timeStamp2 = dataIOArray[i].getData()[1].getData()[0].getLong();

                //compare the two time stamps
                JAMSCalendar cal1 = JAMSDataFactory.getCalendar();
                cal1.setTimeInMillis(timeStamp1 * 1000);
                JAMSCalendar cal2 = JAMSDataFactory.getCalendar();
                cal2.setTimeInMillis(timeStamp2 * 1000);

                cal1.add(timeUnit, timeUnitCount);
                if (cal1.compareTo(cal2) != 0) {

                    JAMSCalendar cal = cal1.clone();
                    cal.add(timeUnit, -1 * timeUnitCount);
                    long demandedSeconds = Math.abs(cal1.getTimeInMillis() - cal.getTimeInMillis()) / 1000;
                    long currentSeconds = Math.abs(cal.getTimeInMillis() - cal2.getTimeInMillis()) / 1000;

                    this.ws.getRuntime().sendErrorMsg(JAMS.resources.getString("Error_in_") + this.getClass().getName() + JAMS.resources.getString(":_wrong_time_interval_in_column_") + i + JAMS.resources.getString("_(demanded_interval_=_") + demandedSeconds + JAMS.resources.getString("_sec,_provided_interval_=_") + currentSeconds + JAMS.resources.getString("_sec)!"));

                    dataIOSet.clear();
                    currentPosition = maxPosition;
                }

            }

            // check identical start date of all columns

            // for all but the first columns
            boolean shifted = false;
            for (int i = 0; i < dataIOArray.length; i++) {

                long timeStamp2 = dataIOArray[i].getData()[0].getData()[0].getLong();

                //compare the two time stamps
                JAMSCalendar cal = JAMSDataFactory.getCalendar();
                cal.setTimeInMillis(timeStamp2 * 1000);

                if (cal.compareTo(startDate, timeUnit) != 0) {

                    this.ws.getRuntime().sendErrorMsg(JAMS.resources.getString("Error_in_") + this.getClass().getName() + JAMS.resources.getString(":_wrong_start_time_in_column_") + i + JAMS.resources.getString("_(demanded_=_") + startDate + JAMS.resources.getString(",_provided_=_") + cal + JAMS.resources.getString(")!"));

                    dataIOSet.clear();
                    currentPosition = maxPosition;
                } else if (!shifted) {
                    if (cal.compareTo(currentDate) != 0) {
                        currentDate.setValue(cal);
                    }
                    shifted = true;
                }
            }
        }

        currentDate.add(timeUnit, -1 * timeUnitCount);
        calendar = new CalendarValue(currentDate);

        bufferSize = oldBufferSize;
    }

    @Override
    public boolean hasNext() {
        if (currentDate.after(stopDate)) {
            return false;
        }
        return super.hasNext();
    }

    @Override
    public DataSet getNext() {

        if (!hasNext()) {
            return null;
        }

        DataSet result = new DataSet(positionArray.length + 1);

        currentDate.add(timeUnit, timeUnitCount);
        result.setData(0, calendar);

        for (int i = 0; i < dataIOArray.length; i++) {

            DataSet ds = dataIOArray[i].getData()[currentPosition];
            DataValue[] values = ds.getData();
            result.setData(i + 1, values[positionArray[i]]);

        }

        currentPosition++;

        return result;
    }

    public JAMSCalendar getStartDate() {
        return startDate;
    }

    public JAMSCalendar getEndDate() {
        return endDate;
    }

    public int getTimeUnit() {
        return timeUnit;
    }

    public int getTimeUnitCount() {
        return timeUnitCount;
    }

    public String getTimeFormat() {
        return timeFormat;
    }
}

