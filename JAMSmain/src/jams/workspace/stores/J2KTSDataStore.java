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
import jams.runtime.JAMSRuntime;
import jams.workspace.DefaultDataSet;
import jams.workspace.DefaultDataSetDefinition;
import jams.workspace.JAMSWorkspace;
import jams.workspace.datatypes.DoubleValue;
import jams.workspace.datatypes.StringValue;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.StringTokenizer;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 *
 * @author Sven Kralisch <sven.kralisch at uni-jena.de>
 */
public class J2KTSDataStore extends TSDataStore {

    private String cache = null;
    private int columnCount = 0;
    private BufferedReader j2kTSFileReader;
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
            timeFormat = JAMSCalendar.DATE_TIME_FORMAT;
        }

        if (parseTimeElement != null) {
            parseDate = Boolean.parseBoolean(parseTimeElement.getAttribute("value"));
        } else {
            parseDate = false;
        }

        if (timeFormatElement != null) {
            timeFormat = timeFormatElement.getAttribute("value");
        } else {
            timeFormat = JAMSCalendar.DATE_TIME_FORMAT;
        }

        // set sourceFile to the default
        File sourceFile = null;
        if (sourceElement != null) {
            String sourceFileName = sourceElement.getAttribute("value");
            if (sourceFileName != null) {
                sourceFile = new File(sourceFileName);
            }
        } else {
            sourceFile = new File(ws.getLocalInputDirectory(), id + ".dat");
        }

        this.j2kTSFileReader = new BufferedReader(new FileReader(sourceFile));

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
            this.description += line.substring(1) + "\n";
            line = j2kTSFileReader.readLine();
        }

        StringBuffer dataValueAttribs = new StringBuffer();
        while (!line.startsWith("@dataSetAttribs")) {
            dataValueAttribs.append(line + "\n");
            line = j2kTSFileReader.readLine();
        }

        StringBuffer dataSetAttribs = new StringBuffer();
        while (!line.startsWith("@statAttribVal")) {
            dataSetAttribs.append(line + "\n");
            line = j2kTSFileReader.readLine();
        }

        StringBuffer statAttribVal = new StringBuffer();
        while (!line.startsWith("@dataVal")) {
            statAttribVal.append(line + "\n");
            line = j2kTSFileReader.readLine();
        }

        // create a DefaultDataSetDefinition object

        StringTokenizer tok1 = new StringTokenizer(statAttribVal.toString(), "\n");
        tok1.nextToken();
        StringTokenizer tok2 = new StringTokenizer(tok1.nextToken(), "\t");

        int attributeCount = tok2.countTokens() - 1;
        ArrayList<Class> dataTypes = new ArrayList<Class>();
        for (int i = 0; i < attributeCount; i++) {
            dataTypes.add(Double.class);
        }
        DefaultDataSetDefinition def = new DefaultDataSetDefinition(dataTypes);

        while (tok1.hasMoreTokens()) {

            String attributeName = tok2.nextToken().toUpperCase();
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
        tok1 = new StringTokenizer(dataSetAttribs.toString(), "\n");
        tok1.nextToken(); // skip the "@"-tag

        while (tok1.hasMoreTokens()) {
            tok2 = new StringTokenizer(tok1.nextToken());
            String key = tok2.nextToken();
            if (key.equals("missingDataVal")) {
                missingDataValue = tok2.nextToken();
            } else if (key.equals("dataStart")) {
                String startString = tok2.nextToken() + " " + tok2.nextToken();
                try {
                    startDate.setValue(startString, "dd.MM.yyyy HH:mm");
                } catch (ParseException pe) {
                    ws.getRuntime().sendErrorMsg(JAMS.resources.getString("Could_not_parse_date_\"") + startString + JAMS.resources.getString("\"_-_date_kept_unchanged!"));
                }
            } else if (key.equals("dataEnd")) {
                String endString = tok2.nextToken() + " " + tok2.nextToken();
                try {
                    endDate.setValue(endString, "dd.MM.yyyy HH:mm");
                } catch (ParseException pe) {
                    ws.getRuntime().sendErrorMsg(JAMS.resources.getString("Could_not_parse_date_\"") + endString + JAMS.resources.getString("\"_-_date_kept_unchanged!"));
                }
            } else if (key.equals("tres")) {
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

        DefaultDataSet result = new DefaultDataSet(columnCount + 1);
        StringTokenizer tok = new StringTokenizer(cache, "\t");

        String dateTimeString = tok.nextToken() + " " + tok.nextToken();
        /*if (parseDate) {
        try {
        currentDate.setValue(dateTimeString, "dd.MM.yyyy HH:mm");
        result.setData(0, new CalendarValue(currentDate));
        } catch (ParseException pe) {
        ws.getRuntime().sendErrorMsg("Could not parse date \"" + dateTimeString + "\" - date kept unchanged!");
        }
        } else {*/
        result.setData(0, new StringValue(dateTimeString));
//        }

        int i = 1;
        while (tok.hasMoreTokens()) {
            double d = Double.parseDouble(tok.nextToken());
            result.setData(i, new DoubleValue(d));
            i++;
        }

        cache = null;
        return result;
    }

    @Override
    public void close() {
        try {
            j2kTSFileReader.close();
        } catch (IOException ioe) {
            ws.getRuntime().handle(ioe);
        }
    }
}
