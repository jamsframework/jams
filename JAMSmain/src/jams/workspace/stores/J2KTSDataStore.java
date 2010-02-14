/*
 * J2KTSDataStore.java
 * Created on 13. Oktober 2008, 17:22
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

import jams.JAMS;
import jams.data.JAMSCalendar;
import jams.io.BufferedFileReader;
import jams.io.JAMSTableDataArray;
import jams.runtime.JAMSRuntime;
import jams.tools.JAMSTools;
import jams.tools.StringTools;
import jams.workspace.DefaultDataSet;
import jams.workspace.DefaultDataSetDefinition;
import jams.workspace.JAMSWorkspace;
import jams.workspace.datatypes.DoubleValue;
import jams.workspace.datatypes.StringValue;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
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

    private BufferedFileReader j2kTSFileReader;

    private File sourceFile;

    private boolean parseDate = false;

    public J2KTSDataStore(JAMSWorkspace ws, String id, Document doc) throws IOException {

        super(ws);
        this.id = id;

        Element sourceElement = (Element) doc.getElementsByTagName("source").item(0);
        Element timeFormatElement = (Element) doc.getElementsByTagName("dumptimeformat").item(0);
        Element parseTimeElement = (Element) doc.getElementsByTagName("parsetime").item(0);

        if (timeFormatElement != null) {
            timeFormat = timeFormatElement.getAttribute("value");
        } else {
            timeFormat = JAMSCalendar.DATE_TIME_FORMAT_PATTERN;
        }

        if (parseTimeElement != null) {
            parseDate = Boolean.parseBoolean(parseTimeElement.getAttribute("value"));
        } else {
            parseDate = false;
        }

        if (timeFormatElement != null) {
            timeFormat = timeFormatElement.getAttribute("value");
        } else {
            timeFormat = JAMSCalendar.DATE_TIME_FORMAT_PATTERN;
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
        this.j2kTSFileReader = new BufferedFileReader(new FileInputStream(sourceFile));
        readJ2KFile();

        this.columnCount = this.getDataSetDefinition().getColumnCount();

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

        // create a DefaultDataSetDefinition object

        StringTokenizer tok1 = new StringTokenizer(statAttribVal.toString(), NEWLINE);
        tok1.nextToken();
        StringTokenizer tok2 = new StringTokenizer(tok1.nextToken(), SEPARATOR);

        int attributeCount = tok2.countTokens() - 1;
        ArrayList<Class> dataTypes = new ArrayList<Class>();
        for (int i = 0; i < attributeCount; i++) {
            dataTypes.add(Double.class);
        }
        DefaultDataSetDefinition def = new DefaultDataSetDefinition(dataTypes);

        while (tok1.hasMoreTokens()) {

            String attributeName = tok2.nextToken();
            ArrayList<Object> values = new ArrayList<Object>();
            if (attributeName.equals("x") || attributeName.equals("y") || attributeName.equals("elevation")) {
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
            } else if (key.equalsIgnoreCase(TAGNAME_DATASTART) || key.equalsIgnoreCase(TAGNAME_DATAEND)) {
                String dateFormat = JAMSTools.DATE_FORMAT_PATTERN_DE;
                String dateString = tok2.nextToken();
                if (tok2.hasMoreTokens()) {
                    dateFormat = JAMSTools.DATE_TIME_FORMAT_PATTERN_DE;
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
                    ws.getRuntime().sendErrorMsg(JAMS.resources.getString("Could_not_parse_date_\"") + dateString + JAMS.resources.getString("\"_-_date_kept_unchanged!"));
                }
            } else if (key.equalsIgnoreCase(TAGNAME_TEMP_RES)) {
                String tres = tok2.nextToken();
                if (tres.equals("d")) {
                    timeUnit = JAMSCalendar.DAY_OF_YEAR;
                } else if (tres.equals("m")) {
                    timeUnit = JAMSCalendar.MONTH;
                } else if (tres.equals("h")) {
                    timeUnit = JAMSCalendar.HOUR_OF_DAY;
                }
            }
        }
        timeUnitCount = 1;
        currentDate.setValue(startDate);
        currentDate.setDateFormat(timeFormat);


        this.dsd = def;

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

        try {
            DefaultDataSet result = new DefaultDataSet(columnCount + 1);
            String[] values = StringTools.parseTSRow(cache);
            result.setData(0, new StringValue(values[0]));

            for (int i = 1; i < values.length; i++) {
                double d = Double.parseDouble(values[i]);
                result.setData(i, new DoubleValue(d));
            }

            cache = null;
            return result;

        } catch (ParseException e) {
            ws.getRuntime().handle(e);
        }

        cache = null;
        return null;
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
//                result.setData(0, new StringValue(cal.toString(JAMSCalendar.DATE_TIME_FORMAT_DE)));
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
    public void getState(java.io.ObjectOutputStream stream) throws IOException {
        super.getState(stream);
        stream.writeObject(this.cache);
        stream.writeBoolean(this.parseDate);
        stream.writeInt(this.columnCount);
        //serialize reader
        stream.writeObject(this.sourceFile.getAbsolutePath());
        stream.writeLong(this.j2kTSFileReader.getPosition());
    }

    @Override
    public void setState(java.io.ObjectInputStream stream) throws IOException, ClassNotFoundException {
        super.setState(stream);
        this.cache = (String) stream.readObject();
        this.parseDate = stream.readBoolean();
        this.columnCount = stream.readInt();

        this.sourceFile = new File((String) stream.readObject());
        if (j2kTSFileReader != null) {
            try {
                j2kTSFileReader.close();
            } catch (Exception e) {
            }
        }
        this.j2kTSFileReader = new BufferedFileReader(new FileInputStream(sourceFile));
        long offset = stream.readLong();
        j2kTSFileReader.setPosition(offset);
    }
}
