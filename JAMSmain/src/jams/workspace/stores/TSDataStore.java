/*
 * TSDataStore.java
 * Created on 23. Januar 2008, 15:53
 *
 * This file is part of JAMS
 * Copyright (C) FSU Jena
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
package jams.workspace.stores;

import jams.workspace.*;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import jams.workspace.datatypes.CalendarValue;
import jams.JAMS;
import jams.JAMSProperties;
import jams.data.Attribute;
import jams.data.DefaultDataFactory;
import jams.io.SerializableBufferedReader;
import jams.runtime.JAMSRuntime;
import jams.runtime.StandardRuntime;
import jams.workspace.datatypes.DoubleValue;
import jams.workspace.datatypes.LongValue;
import jams.workspace.datatypes.ObjectValue;
import jams.workspace.datatypes.StringValue;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.TimeZone;

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
    private SerializableBufferedReader dumpFileReader;
    private static final int DOUBLE = 0;
    private static final int LONG = 1;
    private static final int STRING = 2;
    private static final int TIMESTAMP = 3;
    private static final int OBJECT = 4;
    private int[] type;
    private RingBuffer<Long>[] latestTimesteps = null;
    protected Attribute.Calendar[] startDates;
    protected boolean[] endReached;
    protected double missingDateValueAsDouble;

    public class RingBuffer<T> {

        int index = 0;
        int size = 0;
        int fillsize = 0;

        ArrayList<T> ring = null;

        public RingBuffer(int size) {
            this.size = size;
            ring = new ArrayList<T>(size);
            for (int i = 0; i < size; i++) {
                ring.add(null);
            }
        }

        public T get(int relativIndex) {
            if (relativIndex < 0) {
                relativIndex += ((int) (-relativIndex / size) + 1) * (size);
            }
            return ring.get((index + relativIndex) % size);
        }

        public void push(T o) {
            index = (index + 1) % size;
            ring.set(index, o);
            fillsize = Math.min(size, fillsize + 1);
        }
    }

    public TSDataStore(JAMSWorkspace ws) {
        super(ws);
        startDate = DefaultDataFactory.getDataFactory().createCalendar();
        endDate = DefaultDataFactory.getDataFactory().createCalendar();
        currentDate = DefaultDataFactory.getDataFactory().createCalendar();
        calendar = new CalendarValue(currentDate);
    }

    @SuppressWarnings("unchecked")
    public TSDataStore(JAMSWorkspace ws, String id, Document doc) throws IOException, ClassNotFoundException {
        super(ws, id, doc);

        Element tiNode = (Element) doc.getElementsByTagName("timeinterval").item(0);
        Element startElement = (Element) tiNode.getElementsByTagName("start").item(0);
        Element endElement = (Element) tiNode.getElementsByTagName("end").item(0);
        Element stepsizeElement = (Element) tiNode.getElementsByTagName("stepsize").item(0);
        Element timeFormatElement = (Element) tiNode.getElementsByTagName("dumptimeformat").item(0);

        timeFormat = Attribute.Calendar.DEFAULT_FORMAT_PATTERN;
        if (timeFormatElement != null) {
            timeFormat = timeFormatElement.getAttribute("value");
        }

        startDate = DefaultDataFactory.getDataFactory().createCalendar();
        startDate.setValue(startElement.getAttribute("value"));

        timeUnit = Integer.parseInt(stepsizeElement.getAttribute("unit"));
        timeUnitCount = Integer.parseInt(stepsizeElement.getAttribute("count"));

        endDate = DefaultDataFactory.getDataFactory().createCalendar();
        endDate.setValue(endElement.getAttribute("value"));
        stopDate = endDate.clone();
        stopDate.add(timeUnit, -1 * timeUnitCount);

        currentDate = DefaultDataFactory.getDataFactory().createCalendar();
        currentDate.setDateFormat(timeFormat);
        currentDate.setValue(startDate);

        int oldBufferSize = bufferSize;
        if (bufferSize == 1) {
            bufferSize = 2;
        }
        //if no cache should be read, access datastore directy
        if (!readCache()) {

            latestTimesteps = new RingBuffer[dataIOArray.length];
            startDates = new Attribute.Calendar[dataIOArray.length];
            endReached = new boolean[dataIOArray.length];

            for (int i = 0; i < dataIOArray.length; i++) {

                latestTimesteps[i] = new RingBuffer<Long>(2);

                fillBuffer(i);

                startDates[i] = DefaultDataFactory.getDataFactory().createCalendar();

                DataSet[] ds = dataIOArray[i].getData();
                if (ds.length == 0) {
                    startDates[i].setTimeInMillis(Long.MAX_VALUE);
                    endReached[i] = true;
                } else {
                    long timeStamp = ds[0].getData()[0].getLong();
                    startDates[i].setTimeInMillis(timeStamp * 1000);
                    endReached[i] = false;
                }

            }
        }

        currentDate.add(timeUnit, -1 * timeUnitCount);
        calendar = new CalendarValue(currentDate);
        //cache should be written
        if (writeCache) {
            TSDumpProcessor asciiConverter = new TSDumpProcessor();
            File file = new File(ws.getLocalDumpDirectory(), getID() + ".dump");
            asciiConverter.toASCIIFile((TSDataStore) this, file);
            ws.getRuntime().sendInfoMsg(JAMS.i18n("Dumped_input_datastore_1") + getID() + JAMS.i18n("Dumped_input_datastore_2") + file + JAMS.i18n("Dumped_input_datastore_3"));
            writeCache = false;
        }
        //override setting if cache should be read
        if (readCache()) {
            File file = new File(ws.getLocalDumpDirectory(), getID() + ".dump");
            this.dumpFileReader = new SerializableBufferedReader((file));
            this.dsd = getDSDFromDumpFile();
        }

        bufferSize = oldBufferSize;
        missingDateValueAsDouble = Double.parseDouble(this.getMissingDataValue());

    }

    //TODO: we should add some checks if dump file is consistent with xml description
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
            Attribute.Calendar cal = DefaultDataFactory.getDataFactory().createCalendar();
            cal.setValue(valueString);
            o = cal;
        } else if (clazz.equals(Timestamp.class)) {
            Attribute.Calendar cal = DefaultDataFactory.getDataFactory().createCalendar();
            cal.setTimeInMillis(new Long(valueString) * 1000);
            o = cal;
        } else if (clazz.equals(String.class)) {
            o = new String(valueString);
        } else {
            o = new Object();
        }

        return o;
    }

    public void skip(int count) {
        if (this.accessMode != InputDataStore.CACHE_MODE) {
            //todo .. make this step efficient
            for (int i = 0; i < count; i++) {
                getNext();
            }
        } else {
            try {
                for (int i = 0; i < count; i++) {
                    dumpFileReader.readLine();
                }
                currentDate.add(timeUnit, timeUnitCount * count);
                if (currentDate.after(stopDate)) {
                    return;
                }
            } catch (IOException ex) {
                ws.getRuntime().sendErrorMsg("Premature end of dump file for datastore" + id);
                return;
            }
        }
    }

    /*
     * checks for correct step size of the data, i.e. for a unique time interval
     */
    private boolean checkStepSize(int i) {
        // check interval size for all columns
        //get the timestamps of the first two rows (in seconds)
        this.latestTimesteps[i].push(dataIOArray[i].getData()[currentPosition[i]].getData()[0].getLong());
        if (this.latestTimesteps[i].fillsize < 2) {
            return true;
        }
        long timeStamp1 = this.latestTimesteps[i].get(-1);
        long timeStamp2 = this.latestTimesteps[i].get(0);

        //compare the two time stamps
        Attribute.Calendar cal1 = DefaultDataFactory.getDataFactory().createCalendar();
        cal1.setTimeInMillis(timeStamp1 * 1000);
        Attribute.Calendar cal2 = DefaultDataFactory.getDataFactory().createCalendar();
        cal2.setTimeInMillis(timeStamp2 * 1000);

        cal1.add(timeUnit, timeUnitCount);
        if (cal1.compareTo(cal2, timeUnit) != 0) {

            Attribute.Calendar cal = cal1.clone();
            cal.add(timeUnit, -1 * timeUnitCount);
            long demandedSeconds = Math.abs(cal1.getTimeInMillis() - cal.getTimeInMillis()) / 1000;
            long currentSeconds = Math.abs(cal2.getTimeInMillis() - cal.getTimeInMillis()) / 1000;

            this.ws.getRuntime().sendErrorMsg(JAMS.i18n("Error_in_") + this.getClass().getName() + JAMS.i18n(":_wrong_time_interval_in_column_") + i + JAMS.i18n("_(demanded_interval_=_") + demandedSeconds + JAMS.i18n("_sec,_provided_interval_=_") + currentSeconds + JAMS.i18n("_sec)!"));

            dataIOSet.clear();
            currentPosition[i] = maxPosition[i];
            return false;
        }
        return true;
    }

    @Override
    public boolean hasNext() {

        if (currentDate.after(stopDate)) {
            return false;
        }

//        boolean allEndReached = true;
        if (!readCache()) {

            for (int i = 0; i < dataIOArray.length; i++) {

                if (!(startDates[i].after(currentDate)) && (currentPosition[i] >= maxPosition[i])) {
                    fillBuffer(i);
                    if (currentPosition[i] >= maxPosition[i]) {
                        endReached[i] = true;
                    }
                }
//                allEndReached &= endReached[i];
            }
        }

        return true;
    }

    @Override
    public DefaultDataSet getNext() {

        if (!hasNext()) {
            return null;
        }

        currentDate.add(timeUnit, timeUnitCount);
        DefaultDataSet result;

        if (!readCache()) {

            result = new DefaultDataSet(positionArray.length + 1);
            result.setData(0, calendar);

            for (int i = 0; i < dataIOArray.length; i++) {

                boolean outOfInterval = startDates[i].after(currentDate) || endReached[i];

                if (outOfInterval) {

                    result.setData(i + 1, new DoubleValue(this.getMissingDataValue()));

                } else {

                    DataSet ds = dataIOArray[i].getData()[currentPosition[i]];
                    DataValue[] values = ds.getData();
                    result.setData(i + 1, values[positionArray[i]]);
                    checkStepSize(i);
                    currentPosition[i]++;

                }

            }

        } else {

            try {

                String str = dumpFileReader.readLine();

                // remove all empty trailing elements typically inserted by Excel
                while (str.endsWith("\t")) {
                    str = str.substring(0, str.length() - 1);
                }

                String a[] = str.split(SEPARATOR, -1);

                result = new DefaultDataSet(a.length);
                result.setData(0, calendar);
                if (a.length > type.length + 1) {
                    ws.getRuntime().sendErrorMsg("dump file:" + id + "\nnumber of values in line " + str + "\ndoes not match header information");
                }
                // dump date since this is not evaluated!
                for (int i = 1; i < a.length; i++) {

                    DataValue value;
                    String valueString = a[i];

                    switch (type[i - 1]) {
                        case DOUBLE:
                            value = new DoubleValue(valueString);
                            if (value.getDouble() == missingDateValueAsDouble) {
                                value.setDouble((Double) JAMS.getMissingDataValue(Double.class));
                            }
//                            if (valueString.equals(this.getMissingDataValue())) {
//                                value.setDouble((Double) JAMS.getMissingDataValue(Double.class));
//                            } else {
//                                value.setString(valueString);
//                            }

                            break;
                        case LONG:
                            value = new LongValue(valueString);
                            if (value.getLong() == missingDateValueAsDouble) {
                                value.setLong((Long) JAMS.getMissingDataValue(Long.class));
                            }
//                            value = new LongValue(0);
//                            if (valueString.equals(this.getMissingDataValue())) {
//                                value.setLong((Long) JAMS.getMissingDataValue(Long.class));
//                            } else {
//                                value.setString(valueString);
//                            }
                            break;
                        case STRING:
                            value = new StringValue(valueString);
                            if (valueString.equals(this.getMissingDataValue())) {
                                value.setString((String) JAMS.getMissingDataValue(String.class));
                            }
                            break;
                        case TIMESTAMP:
                            Attribute.Calendar cal = DefaultDataFactory.getDataFactory().createCalendar();
                            cal.setTimeInMillis(new Long(valueString));
                            value = new CalendarValue(cal);
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

    public Set<DataReader> getDataIOs() {
        return this.dataIOSet;
    }

    @Override
    public void close() {
        if (this.dumpFileReader != null) {
            try {
                this.dumpFileReader.close();
            } catch (IOException ioe) {
                ioe.printStackTrace();
            }
        }
    }

    private static void convertTSDataStoreToJ2KFile(Workspace ws, String id) {
        InputDataStore store = ws.getInputDataStore(id);
        //writing j2k file
        File outputFile = new File(ws.getLocalDumpDirectory(), id + ".dat");
        File xmlDescFile = new File(ws.getLocalDumpDirectory(), id + ".xml");
        if (outputFile.exists()) {
            System.out.println("Output file allready existing!");
            return;
        }
        if (!(store instanceof TSDataStore)) {
            System.out.println("Store is not a TSDataStore!");
            return;
        }

        DateFormat dateFormat = new SimpleDateFormat("dd.MM.YYYY hh:mm");
        dateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));

        TSDataStore tsStore = (TSDataStore) store;
        DataSetDefinition dsd = store.getDataSetDefinition();
        BufferedWriter writer = null;
        BufferedWriter xmlWriter = null;
        try {
            writer = new BufferedWriter(new FileWriter(outputFile));
            xmlWriter = new BufferedWriter(new FileWriter(xmlDescFile));

            writer.write("@dataValueAttribs\n");
            writer.write(tsStore.getDisplayName() + "\t" + Double.MIN_VALUE + "\t" + Double.MAX_VALUE + "\t?\n");
            writer.write("@dataSetAttribs\n");
            writer.write("missingDataVal\t" + tsStore.getMissingDataValue() + "\n");
            writer.write("dataStart\t" + dateFormat.format(tsStore.getStartDate().getTime()) + "\n");
            writer.write("dataEnd\t" + dateFormat.format(tsStore.getEndDate().getTime()) + "\n");
            if (tsStore.getTimeUnit() == 6) {
                writer.write("tres\td\n");
            }
            if (tsStore.getTimeUnit() == 2) {
                writer.write("tres\tm\n");
            }
            if (tsStore.getTimeUnit() == 1) {
                writer.write("tres\ty\n");
            }
            if (tsStore.getTimeUnit() == 11) {
                writer.write("tres\th\n");
            }
            writer.write("@statAttribVal\n");
            String stationNames = "name";
            String stationIDs = "ID";
            String stationElevation = "elevation";
            String stationX = "x";
            String stationY = "y";
            String dataColumn = "dataColumn";

            ArrayList<Object> xList = dsd.getAttributeValues("X");
            ArrayList<Object> yList = dsd.getAttributeValues("Y");
            ArrayList<Object> elevList = dsd.getAttributeValues("ELEVATION");
            ArrayList<Object> nameList = dsd.getAttributeValues("NAME");
            ArrayList<Object> idList = dsd.getAttributeValues("ID");

            for (int i = 0; i < dsd.getColumnCount(); i++) {
                stationNames += "\t" + nameList.get(i);
                stationIDs += "\t" + idList.get(i);
                stationElevation += "\t" + elevList.get(i);
                stationX += "\t" + xList.get(i);
                stationY += "\t" + yList.get(i);
                dataColumn += "\t" + (i + 1);
            }
            writer.write(stationNames + "\n");
            writer.write(stationIDs + "\n");
            writer.write(stationElevation + "\n");
            writer.write(stationX + "\n");
            writer.write(stationY + "\n");
            writer.write(dataColumn + "\n");

            writer.write("@dataVal\n");

            DefaultDataSet dds = null;
            while ((dds = tsStore.getNext()) != null) {
                String line = "";
                for (int i = 0; i < dds.getData().length; i++) {
                    if (i == 0) {
                        line += dateFormat.format(dds.getData()[i].getCalendar().getTime()) + "\t";
                    } else {
                        line += dds.getData()[i].getObject() + "\t";
                    }
                }
                line += "\n";
                writer.write(line);
            }
            xmlWriter.write("<?xml version=\"1.0\" encoding=\"ISO-8859-1\" standalone=\"no\"?>\n"
                    + "<j2ktsdatastore>\n"
                    + "<parsetime value=\"false\" />\n"
                    + "<dumptimeformat value=\"yyyy-MM-dd HH:mm\" />"
                    + "<charset value=\"ISO-8859-1\" />"
                    + "</j2ktsdatastore>");
        } catch (IOException ioe) {
            System.out.println("Error while writing file!");
            return;
        } finally {
            try {
                writer.close();
                xmlWriter.close();
            } catch (IOException ioe) {

            }
        }
    }

    //convert dump into j2k file
    public static void main(String[] args) throws IOException {
        if (args.length < 1) {
            System.out.println("Usage dump2j2kFile workspaceDir [id]");
        }
        File workspaceDirectory = new File(args[0]);
        if (!workspaceDirectory.exists()) {
            System.out.println("Error: directory " + workspaceDirectory.getAbsolutePath() + " does not exist!");
            return;
        }
        JAMSRuntime runtime = new StandardRuntime(JAMSProperties.createProperties());
        Workspace ws = new JAMSWorkspace(workspaceDirectory, runtime, false);
        try {
            ws.init();
        } catch (InvalidWorkspaceException iwe) {
            System.out.println("Invalid workspace!\n" + iwe.toString());
        }
        if (args.length > 1) {
            String id = args[1];
//            convertTSDataStoreToJ2KFile(ws, id);
            TSDumpProcessor asciiConverter = new TSDumpProcessor();
            System.out.println(asciiConverter.toASCIIString((TSDataStore) ws.getInputDataStore(id)));
        } else {
            Set<String> inputDataStores = ws.getInputDataStoreIDs();
            for (String id : inputDataStores) {
                convertTSDataStoreToJ2KFile(ws, id);
            }
        }

    }
}
