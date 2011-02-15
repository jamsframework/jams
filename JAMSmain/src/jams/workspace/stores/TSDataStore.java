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
import jams.JAMS;
import jams.data.Attribute;
import jams.data.Attribute.Calendar;
import jams.data.JAMSDataFactory;
import jams.io.BufferedFileReader;
import jams.workspace.datatypes.DoubleValue;
import jams.workspace.datatypes.LongValue;
import jams.workspace.datatypes.ObjectValue;
import jams.workspace.datatypes.StringValue;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Set;
import java.util.StringTokenizer;

/**
 *
 * @author Sven Kralisch
 */
public class TSDataStore extends TableDataStore {

    public static final String NEWLINE = "\n";
    public static final String SEPARATOR = "\t";
    protected CalendarValue calendar;
    protected Attribute.Calendar currentDate, startDate, endDate, stopDate;
    protected int timeUnit, timeUnitCount;
    protected String timeFormat;
    File file = null;
    transient private BufferedFileReader dumpFileReader;
    private static final int DOUBLE = 0;
    private static final int LONG = 1;
    private static final int STRING = 2;
    private static final int TIMESTAMP = 3;
    private static final int OBJECT = 4;
    private int[] type;

    public TSDataStore(JAMSWorkspace ws) {
        super(ws);
        startDate = JAMSDataFactory.createCalendar();
        endDate = JAMSDataFactory.createCalendar();
        currentDate = JAMSDataFactory.createCalendar();
        calendar = new CalendarValue(currentDate);
    }

    public TSDataStore(JAMSWorkspace ws, String id, Document doc) throws IOException, ClassNotFoundException {
        super(ws, id, doc);

        Element tiNode = (Element) doc.getElementsByTagName("timeinterval").item(0);
        Element startElement = (Element) tiNode.getElementsByTagName("start").item(0);
        Element endElement = (Element) tiNode.getElementsByTagName("end").item(0);
        Element stepsizeElement = (Element) tiNode.getElementsByTagName("stepsize").item(0);
        Element timeFormatElement = (Element) tiNode.getElementsByTagName("dumptimeformat").item(0);

        timeFormat = JAMSCalendar.DATE_TIME_FORMAT_PATTERN;
        if (timeFormatElement != null) {
            timeFormat = timeFormatElement.getAttribute("value");
        }

        startDate = JAMSDataFactory.createCalendar();
        startDate.setValue(startElement.getAttribute("value"));

        timeUnit = Integer.parseInt(stepsizeElement.getAttribute("unit"));
        timeUnitCount = Integer.parseInt(stepsizeElement.getAttribute("count"));

        endDate = JAMSDataFactory.createCalendar();
        endDate.setValue(endElement.getAttribute("value"));
        stopDate = endDate.clone();
        stopDate.add(timeUnit, -1 * timeUnitCount);

        currentDate = JAMSDataFactory.createCalendar();
        currentDate.setDateFormat(timeFormat);
        currentDate.setValue(startDate);

        int oldBufferSize = bufferSize;
        if (bufferSize == 1) {
            bufferSize = 2;
        }

        if (this.accessMode != InputDataStore.CACHE_MODE) {

            // check validity of the data, e.g. unique start dates
            // a tsdatastore assumes that all columns have synchronous time and
            // start/end at the same time step

            fillBuffer();
            if (maxPosition >= 2) {

                // check interval size for all columns
                for (int i = 0; i < dataIOArray.length; i++) {

                    //get the timestamps of the first two rows (in seconds)
                    long timeStamp1 = dataIOArray[i].getData()[0].getData()[0].getLong();
                    long timeStamp2 = dataIOArray[i].getData()[1].getData()[0].getLong();

                    //compare the two time stamps
                    Attribute.Calendar cal1 = JAMSDataFactory.createCalendar();
                    cal1.setTimeInMillis(timeStamp1 * 1000);
                    Attribute.Calendar cal2 = JAMSDataFactory.createCalendar();
                    cal2.setTimeInMillis(timeStamp2 * 1000);

                    cal1.add(timeUnit, timeUnitCount);
                    if (cal1.compareTo(cal2) != 0) {

                        Attribute.Calendar cal = cal1.clone();
                        cal.add(timeUnit, -1 * timeUnitCount);
                        long demandedSeconds = Math.abs(cal1.getTimeInMillis() - cal.getTimeInMillis()) / 1000;
                        long currentSeconds = Math.abs(cal.getTimeInMillis() - cal2.getTimeInMillis()) / 1000;

                        this.ws.getRuntime().sendErrorMsg(JAMS.i18n("Error_in_") + this.getClass().getName() + JAMS.i18n(":_wrong_time_interval_in_column_") + i + JAMS.i18n("_(demanded_interval_=_") + demandedSeconds + JAMS.i18n("_sec,_provided_interval_=_") + currentSeconds + JAMS.i18n("_sec)!"));

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
                    Attribute.Calendar cal = JAMSDataFactory.createCalendar();
                    cal.setTimeInMillis(timeStamp2 * 1000);

                    if (cal.compareTo(startDate, timeUnit) != 0) {

                        this.ws.getRuntime().sendErrorMsg(JAMS.i18n("Error_in_") + this.getClass().getName() + JAMS.i18n(":_wrong_start_time_in_column_") + i + JAMS.i18n("_(demanded_=_") + startDate + JAMS.i18n(",_provided_=_") + cal + JAMS.i18n(")!"));

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

        } else {

            file = new File(ws.getLocalDumpDirectory(), id + ".dump");
            if (!file.exists()) {
                throw new IOException("Dump file " + file.getPath() + " not found!");
            }

            //this.dumpFileReader = new BufferedReader(new FileReader(file));
            this.dumpFileReader = new BufferedFileReader(new FileInputStream(file));

            this.dsd = getDSDFromDumpFile();

//            @TODO: Check if this is needed
//            if (ws.getRuntime().getState() != JAMSRuntime.STATE_RUN) {
//                return;
//            }
        }

        currentDate.add(timeUnit, -1 * timeUnitCount);
        calendar = new CalendarValue(currentDate);

        bufferSize = oldBufferSize;
    }

    private DataSetDefinition getDSDFromDumpFile() throws IOException {

        String str;
        while ((str = dumpFileReader.readLine()) != null) {
            if (str.startsWith(DataSetDefinition.TYPE_ID)) {
                break;
            }
        }

        if (str == null) {
            return null;
        }

        StringTokenizer tok = new StringTokenizer(str, SEPARATOR);
        ArrayList<Class> dataTypes = new ArrayList<Class>();
        // drop the first token (TYPE_ID)
        tok.nextToken();

        while (tok.hasMoreTokens()) {
            String className = tok.nextToken();
            try {
                dataTypes.add(Class.forName(className));
            } catch (ClassNotFoundException ex) {
                ws.getRuntime().sendErrorMsg("Referenced type in datastore " + id
                        + " could not be found: " + className);
            }
        }

        DataSetDefinition def = new DefaultDataSetDefinition(dataTypes);

        type = new int[dataTypes.size()];
        int i = 0;
        for (Class clazz : dataTypes) {
            if (clazz.equals(Long.class)) {
                type[i] = LONG;
            } else if (clazz.equals(Double.class)) {
                type[i] = DOUBLE;
            } else if (clazz.equals(String.class)) {
                type[i] = STRING;
            } else if (clazz.equals(Timestamp.class)) {
                type[i] = TIMESTAMP;
            } else {
                type[i] = OBJECT;
            }
            i++;
        }

        while ((str = dumpFileReader.readLine()) != null) {
            if (str.startsWith(TSDumpProcessor.DATA_TAG)) {
                break;
            }

            tok = new StringTokenizer(str, SEPARATOR);

            String attributeName = tok.nextToken().substring(1);
            String className = tok.nextToken();
            ArrayList<Object> values = new ArrayList<Object>();
            Class clazz = null;

            try {
                clazz = Class.forName(className);
            } catch (ClassNotFoundException ex) {
                ws.getRuntime().sendErrorMsg("Referenced type in datastore " + id
                        + " could not be found: " + className);
                return null;
            }
            def.addAttribute(attributeName, clazz);

            while (tok.hasMoreTokens()) {
                String valueString = tok.nextToken();
                values.add(getDataValue(clazz, valueString));
            }
            def.setAttributeValues(attributeName, values);
        }
        return def;
    }

    private Object getDataValue(Class clazz, String valueString) {

        Object o = null;

        if (clazz.equals(Double.class)) {
            o = new Double(valueString);
        } else if (clazz.equals(Long.class)) {
            o = new Long(valueString);
        } else if (clazz.equals(Attribute.Calendar.class)) {
            Attribute.Calendar cal = JAMSDataFactory.createCalendar();
            cal.setValue(valueString);
            o = cal;
        } else if (clazz.equals(Timestamp.class)) {
            Attribute.Calendar cal = JAMSDataFactory.createCalendar();
            cal.setTimeInMillis(new Long(valueString) * 1000);
            o = cal;
        } else if (clazz.equals(String.class)) {
            o = new String(valueString);
        } else {
            o = new Object();
        }

        return o;
    }

    @Override
    public boolean hasNext() {

        if (currentDate.after(stopDate)) {
            return false;
        }

        if (this.accessMode != InputDataStore.CACHE_MODE) {

            return super.hasNext();

        } else {

            return true;

        }
    }

    public void skip(int count) {                
        if (this.accessMode != InputDataStore.CACHE_MODE) {
            //todo .. make this step efficient
            for (int i=0;i<count;i++)
                getNext();
        } else {
            try {
                for (int i=0;i<count;i++)
                    dumpFileReader.readLine();
                currentDate.add(timeUnit, timeUnitCount*count);
                if (currentDate.after(stopDate)) {
                    return;
                }
            } catch (IOException ex) {
                ws.getRuntime().sendErrorMsg("Premature end of dump file for datastore" + id);
                return;
            }
        }
    }

    @Override
    public DefaultDataSet getNext() {

        if (!hasNext()) {
            return null;
        }

        currentDate.add(timeUnit, timeUnitCount);
        DefaultDataSet result;

        if (this.accessMode != InputDataStore.CACHE_MODE) {

            result = new DefaultDataSet(positionArray.length + 1);
            result.setData(0, calendar);
            for (int i = 0; i < dataIOArray.length; i++) {

                DataSet ds = dataIOArray[i].getData()[currentPosition];
                DataValue[] values = ds.getData();
                result.setData(i + 1, values[positionArray[i]]);

            }

            currentPosition++;

        } else {

            try {

                String str = dumpFileReader.readLine();
                StringTokenizer tok = new StringTokenizer(str, SEPARATOR);

                int length = tok.countTokens();

                result = new DefaultDataSet(length);
                result.setData(0, calendar);

                // dump date since this is not evaluated!
                tok.nextToken();

                for (int i = 1; i < length; i++) {

                    DataValue value;
                    String valueString = tok.nextToken();
                    switch (type[i - 1]) {
                        case DOUBLE:
                            value = new DoubleValue(valueString);
                            break;
                        case LONG:
                            value = new LongValue(valueString);
                            break;
                        case STRING:
                            value = new StringValue(valueString);
                            break;
                        case TIMESTAMP:
                            Attribute.Calendar cal = JAMSDataFactory.createCalendar();
                            cal.setTimeInMillis(new Long(valueString));
                            value = new CalendarValue(cal);
                            break;
                        default:
                            value = new ObjectValue(valueString);
                    }

                    result.setData(i, value);
                }

            } catch (IOException ex) {
                ws.getRuntime().sendErrorMsg("Premature end of dump file for datastore" + id);
                return null;
            }
        }

        return result;
    }

    public Attribute.Calendar getStartDate() {
        return startDate;
    }

    public Attribute.Calendar getEndDate() {
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

    static public class TSDataStoreState extends TableDataStoreState {

        protected CalendarValue calendar;
        protected Calendar currentDate;
        protected Calendar startDate;
        protected Calendar endDate;
        protected Calendar stopDate;
        protected int[] type;
        protected String timeFormat;
        protected int timeUnit;
        protected int timeUnitCount;
        protected String fileName;
        protected long position;

        TSDataStoreState() {
        }

        public void fill(TSDataStoreState state) {
            super.fill(state);
            state.calendar = this.calendar;
            state.currentDate = this.currentDate;
            state.startDate = this.startDate;
            state.endDate = this.endDate;
            state.stopDate = this.stopDate;
            state.type = this.type;
            state.timeFormat = this.timeFormat;
            state.timeUnit = this.timeUnit;
            state.timeUnitCount = this.timeUnitCount;
            state.fileName = this.fileName;
            state.position = this.position;
        }

        TSDataStoreState(TableDataStoreState state) {
            state.fill(this);
        }
    }

    @Override
    public DataStoreState getState() {
        TSDataStoreState state = new TSDataStoreState((TableDataStoreState) super.getState());
        state.calendar = this.calendar;
        state.currentDate = this.currentDate;
        state.startDate = this.startDate;
        state.endDate = this.endDate;
        state.stopDate = this.stopDate;
        state.type = this.type;
        state.timeFormat = this.timeFormat;
        state.timeUnit = this.timeUnit;
        state.timeUnitCount = this.timeUnitCount;
        if (file != null) {
            state.fileName = file.getAbsolutePath();
            state.position = dumpFileReader.getPosition();
        }
        return state;
    }

    @Override
    public void setState(DataStoreState state) throws IOException {
        TSDataStoreState tsDataStore = (TSDataStoreState) state;
        super.setState(tsDataStore);

        calendar = tsDataStore.calendar;
        currentDate = tsDataStore.currentDate;
        startDate = tsDataStore.startDate;
        endDate = tsDataStore.endDate;
        stopDate = tsDataStore.stopDate;
        type = tsDataStore.type;
        timeFormat = tsDataStore.timeFormat;
        timeUnit = tsDataStore.timeUnit;
        timeUnitCount = tsDataStore.timeUnitCount;

        String fileName = (String) tsDataStore.fileName;
        //deserialize reader
        if (this.dumpFileReader != null) {
            try {
                this.dumpFileReader.close();
                dumpFileReader = null;
            } catch (Exception e) {
            }
        }

        if (fileName != null) {
            file = new File(fileName);
            this.dumpFileReader = new BufferedFileReader(new FileInputStream(file));
            this.dumpFileReader.setPosition(tsDataStore.position);
        }
    }

    public Set<DataReader> getDataIOs() {
        return this.dataIOSet;
    }
}

