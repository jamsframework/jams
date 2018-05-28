/*
 * J2KTSDataStore.java
 * Created on 13. Oktober 2008, 17:22
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

import jams.JAMS;
import jams.data.Attribute;
import jams.data.DefaultDataFactory;
import jams.io.BufferedFileReader;
import jams.runtime.JAMSRuntime;
import jams.tools.StringTools;
import jams.workspace.DataSetDefinition;
import jams.workspace.DefaultDataSet;
import jams.workspace.DefaultDataSetDefinition;
import jams.workspace.JAMSWorkspace;
import jams.workspace.Workspace;
import jams.workspace.datatypes.CalendarValue;
import jams.workspace.datatypes.DoubleValue;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.StringTokenizer;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 *
 * @author Sven Kralisch <sven.kralisch at uni-jena.de>
 */
public class J2KTSDataStore extends TSDataStore {

    public static final String DATE_TIME_FORMAT_PATTERN_J2K = "dd.MM.yyyy HH:mm";
    public static final String TAGNAME_DATAEND = "dataEnd";
    public static final String TAGNAME_DATASETATTRIBS = "@dataSetAttribs";
    public static final String TAGNAME_DATASTART = "dataStart";
    public static final String TAGNAME_DATAVAL = "@dataVal";
    public static final String TAGNAME_DATAVALUEATTRIBS = "@dataValueAttribs";
    public static final String TAGNAME_MISSINGDATAVAL = "missingDataVal";
    public static final String TAGNAME_STATATTRIBVAL = "@statAttribVal";
    public static final String TAGNAME_TEMP_RES = "tres";
    private String cache = null;
    private int columnCount = 0;
    //private RandomAccessFile j2kTSFileReader;
    transient private BufferedFileReader j2kTSFileReader;
    private File sourceFile;
    private String relativPath = null;
    private boolean parseDate = false;
    private String charsetName;
    private double missingData;

    public J2KTSDataStore(JAMSWorkspace ws, String id, Document doc) throws IOException {

        super(ws);
        this.id = id;

        Element sourceElement = (Element) doc.getElementsByTagName("source").item(0);
        Element timeFormatElement = (Element) doc.getElementsByTagName("dumptimeformat").item(0);
        Element parseTimeElement = (Element) doc.getElementsByTagName("parsetime").item(0);
        Element charsetElement = (Element) doc.getElementsByTagName("charset").item(0);

        if (timeFormatElement != null) {
            timeFormat = timeFormatElement.getAttribute("value");
        } else {
            timeFormat = Attribute.Calendar.DEFAULT_FORMAT_PATTERN;
        }

        // check if an charset is defined. if not, use JAMS default
        if (charsetElement != null) {
            charsetName = charsetElement.getAttribute("value");
        } else {
            charsetName = JAMS.getCharset();
        }

        if (parseTimeElement != null) {
            parseDate = Boolean.parseBoolean(parseTimeElement.getAttribute("value"));
        } else {
            parseDate = false;
        }

        Node displaynameNode = doc.getDocumentElement().getElementsByTagName("displayname").item(0);
        if (displaynameNode != null) {
            this.displayName = displaynameNode.getTextContent();
        } else {
            this.displayName = id;
        }

        // set sourceFile to the default
        sourceFile = null;
        if (sourceElement != null) {
            String sourceFileName = sourceElement.getAttribute("value");
            if (sourceFileName != null) {
                sourceFile = new File(sourceFileName);
            }
        } else {
            sourceFile = new File(ws.getLocalInputDirectory(), id + ".dat");
        }

        //this.j2kTSFileReader = new RandomAccessFile(sourceFile,"r");
        this.relativPath = new File(this.ws.getDirectory().getAbsolutePath()).toURI().relativize(new File(sourceFile.getAbsolutePath()).toURI()).getPath();
        this.j2kTSFileReader = new BufferedFileReader(new FileInputStream(sourceFile), charsetName);
        readJ2KFile();

        this.columnCount = this.getDataSetDefinition().getColumnCount();

        currentDate = DefaultDataFactory.getDataFactory().createCalendar();
        currentDate.setDateFormat(timeFormat);
        currentDate.setValue(startDate);
        currentDate.add(timeUnit, -1 * timeUnitCount);
        calendar = new CalendarValue(currentDate);

        if (ws.getRuntime().getState() != JAMSRuntime.STATE_RUN) {
            return;
        }
    }

    private void readJ2KFile() throws IOException {

        // read header information from the J2K time series file
        String line = j2kTSFileReader.readLine();
        //skip comment lines
        while (line.charAt(0) == '#') {
            this.description += line.substring(1) + NEWLINE;
            line = j2kTSFileReader.readLine();
        }

        StringBuffer dataValueAttribs = new StringBuffer();
        while (!line.startsWith(TAGNAME_DATASETATTRIBS)) {
            dataValueAttribs.append(line + NEWLINE);
            line = j2kTSFileReader.readLine();
        }

        StringBuffer dataSetAttribs = new StringBuffer();
        while (!line.startsWith(TAGNAME_STATATTRIBVAL)) {
            dataSetAttribs.append(line + NEWLINE);
            line = j2kTSFileReader.readLine();
        }

        StringBuffer statAttribVal = new StringBuffer();
        while (!line.startsWith(TAGNAME_DATAVAL)) {
            statAttribVal.append(line + NEWLINE);
            line = j2kTSFileReader.readLine();
        }

        // create a DataSetDefinition object
        StringTokenizer tok1 = new StringTokenizer(statAttribVal.toString(), NEWLINE);
        tok1.nextToken();
        StringTokenizer tok2 = new StringTokenizer(tok1.nextToken(), SEPARATOR);

        int attributeCount = tok2.countTokens() - 1;
        ArrayList<Class> dataTypes = new ArrayList<Class>();
        for (int i = 0; i < attributeCount; i++) {
            dataTypes.add(Double.class);
        }
        DataSetDefinition def = new DefaultDataSetDefinition(dataTypes);

        while (tok1.hasMoreTokens()) {

            String attributeName = tok2.nextToken().toUpperCase(StringTools.STANDARD_LOCALE);
            ArrayList<Object> values = new ArrayList<Object>();
            if (attributeName.equals("X") || attributeName.equals("Y") || attributeName.equals("ELEVATION")) {
                def.addAttribute(attributeName, Double.class);
                while (tok2.hasMoreTokens()) {
                    values.add(Double.parseDouble(tok2.nextToken()));
                }
            } else {
                def.addAttribute(attributeName, String.class);
                while (tok2.hasMoreTokens()) {
                    values.add(tok2.nextToken());
                }
            }
            def.setAttributeValues(attributeName, values);
            tok2 = new StringTokenizer(tok1.nextToken());
        }

        // process dataValueAttribs
        tok1 = new StringTokenizer(dataValueAttribs.toString());
        tok1.nextToken(); // skip the "@"-tag
        String parameterName = tok1.nextToken();
        String parameterString = "PARAMETER";
        def.addAttribute(parameterString, String.class);
        def.setAttributeValues(parameterString, parameterName);

        // process dataSetAttribs
        tok1 = new StringTokenizer(dataSetAttribs.toString(), NEWLINE);
        tok1.nextToken(); // skip the "@"-tag

        while (tok1.hasMoreTokens()) {
            tok2 = new StringTokenizer(tok1.nextToken());
            String key = tok2.nextToken();
            if (key.equalsIgnoreCase(TAGNAME_MISSINGDATAVAL)) {
                missingDataValue = tok2.nextToken();
                missingData = Double.parseDouble(missingDataValue);
            } else if (key.equalsIgnoreCase(TAGNAME_DATASTART) || key.equalsIgnoreCase(TAGNAME_DATAEND)) {

                String dateString = tok2.nextToken();
                String dateFormat;
                if (dateString.contains("-")) {
                    dateFormat = Attribute.Calendar.DEFAULT_FORMAT_PATTERN;
                } else {
                    dateFormat = DATE_TIME_FORMAT_PATTERN_J2K;
                }

                if (tok2.hasMoreTokens()) {
                    dateString += " " + tok2.nextToken();
                }
                try {
                    if (key.equalsIgnoreCase(TAGNAME_DATASTART)) {
                        startDate.setValue(dateString, dateFormat);
                    }
                    if (key.equalsIgnoreCase(TAGNAME_DATAEND)) {
                        endDate.setValue(dateString, dateFormat);
                    }

                } catch (ParseException pe) {
                    ws.getRuntime().sendErrorMsg(JAMS.i18n("Could_not_parse_date_") + dateString + JAMS.i18n("_date_kept_unchanged!"));
                }
            } else if (key.equalsIgnoreCase(TAGNAME_TEMP_RES)) {
                String tres = tok2.nextToken();
                if (tres.endsWith("d")) {
                    timeUnit = Attribute.Calendar.DAY_OF_YEAR;
                } else if (tres.endsWith("m")) {
                    timeUnit = Attribute.Calendar.MONTH;
                } else if (tres.endsWith("h")) {
                    timeUnit = Attribute.Calendar.HOUR_OF_DAY;
                } else if (tres.endsWith("n") || tres.endsWith("min")) {
                    timeUnit = Attribute.Calendar.MINUTE;
                } else if (tres.endsWith("y")) {
                    timeUnit = Attribute.Calendar.YEAR;
                } else {
                    timeUnit = Attribute.Calendar.SECOND;
                }
                if (tres.length() > 1) {
                    try {
                        timeUnitCount = Integer.parseInt(tres.substring(0, tres.length() - 1));
                    } catch (NumberFormatException nfe) {
                        ws.getRuntime().handle(nfe);
                        timeUnitCount = 1;
                    }
                } else {
                    timeUnitCount = 1;
                }
            }
        }

        currentDate.setValue(startDate);
        currentDate.setDateFormat(timeFormat);

        this.dsd = def;

    }

    private String[] parseTSRow(String row) throws ParseException {

        StringTokenizer tok = new StringTokenizer(row);
        int n = tok.countTokens();

        if (n > 1) {

            String dateString = tok.nextToken();
            String[] data;
            int i;

            String s = tok.nextToken();
            if (s.contains(":")) {
                data = new String[n - 1];
                data[0] = dateString + " " + s;
                i = 1;
            } else {
                data = new String[n];
                data[0] = dateString;
                data[1] = s;
                i = 2;
            }

            while (tok.hasMoreTokens()) {
                data[i++] = tok.nextToken();
            }

            return data;

        } else {

            return null;

        }
    }

    @Override
    public boolean hasNext() {
        if (cache != null) {
            return true;
        } else {
            try {
                cache = j2kTSFileReader.readLine();
                return ((cache != null) && (!cache.startsWith("#")));
            } catch (IOException ioe) {
                return false;
            }
        }
    }

    @Override
    public DefaultDataSet getNext() {
        if (!hasNext()) {
            return null;
        }

        DefaultDataSet result = new DefaultDataSet(columnCount + 1);
        String[] values = cache.split("\\s+");

//        result.setData(0, new StringValue(values[0] + " " + values[1]));
        currentDate.add(timeUnit, timeUnitCount);
        result.setData(0, calendar);

        for (int i = 2; i < values.length; i++) {
            double d = Double.parseDouble(values[i]);
            if (d == missingData) {
                d = JAMS.getMissingDataValue();
            }
            result.setData(i - 1, new DoubleValue(d));
        }

        cache = null;
        return result;
    }

    @Override
    public void skip(int count) {
        for (int i = 0; i < count; i++) {
            getNext();
        }
    }

//    @Override
//    public DefaultDataSet getNext() {
//        if (!hasNext()) {
//            return null;
//        }
//
//        try {
//            DefaultDataSet result = new DefaultDataSet(columnCount + 1);
//            JAMSTableDataArray jamstda = new JAMSTableDataArray(cache);
//            JAMSCalendar cal = jamstda.getTime();
//            if (cal != null) {
//                result.setData(0, new StringValue(cal.toString(Attribute.Calendar.DATE_TIME_FORMAT_DE)));
//            }
//            int i = 1; double d;
//            for (String value : jamstda.getValues()) {
//                d = Double.parseDouble(value);
//                result.setData(i, new DoubleValue(d));
//                i++;
//            }
//            cache = null;
//            return result;
//
//        } catch (ParseException e) {
//            ws.getRuntime().handle(e);
//        }
//
//        cache = null;
//        return null;
//    }
    @Override
    public void close() {
        try {
            j2kTSFileReader.close();
        } catch (IOException ioe) {
            ws.getRuntime().handle(ioe);
        }
    }

    @Override
    public void setWorkspace(Workspace ws) throws IOException {
        this.ws = ws;
        long position = j2kTSFileReader.getPosition();
        if (j2kTSFileReader != null) {
            try {
                j2kTSFileReader.close();
            } catch (Exception e) {
            }
        }

        this.j2kTSFileReader = new BufferedFileReader(new FileInputStream(new File(ws.getDirectory(), this.relativPath)));
        j2kTSFileReader.setPosition(position);
    }

    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        in.defaultReadObject();
        long position = in.readLong();
        if (j2kTSFileReader != null) {
            try {
                j2kTSFileReader.close();
            } catch (Exception e) {
            }
        }
        this.j2kTSFileReader = new BufferedFileReader(new FileInputStream(sourceFile));
        j2kTSFileReader.setPosition(position);
    }

    private void writeObject(ObjectOutputStream out) throws IOException {
        out.defaultWriteObject();
        try {
            out.writeLong(j2kTSFileReader.getPosition());
        } catch (Throwable t) {
            out.writeLong(0);
        }
    }
}
