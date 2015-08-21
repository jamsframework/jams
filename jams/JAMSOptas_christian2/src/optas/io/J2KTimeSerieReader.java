/*
 * TSDataReader.java
 * Created on 11. November 2005, 10:10
 *
 * This file is part of JAMS
 * Copyright (C) 2005 S. Kralisch and P. Krause
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
package optas.io;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import jams.JAMS;
import jams.data.Attribute;
import jams.data.DefaultDataFactory;
import jams.io.GenericDataReader;
import jams.io.JAMSTableDataArray;
import jams.workspace.stores.*;
import java.util.*;
import java.io.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import optas.data.api.DataView;
import optas.core.OPTASException;
import optas.data.time.api.TimeSerieReader;
import optas.data.view.ViewFactory;
import optas.io.J2KTimeSerieHeader.Dimension;

/**
 *
 * @author Christian Fischer <christian.fischer.2@uni-jena.de>
 * TODO: modify J2KTimeSerieReader to receive a Reader/Stream instead of file
 *       requires modification of GenericTableDataReader .. 
 */
public class J2KTimeSerieReader implements TimeSerieReader<J2KTimeSerie> {

    private static final String SEPARATOR = "\t";

    private int headerLineCount = 0;

    private File fileHandle = null;

    private String lineBuffer = "#";
    private String tokenBuffer[];

    private J2KTimeSerieHeader header;
    private J2KTimeSerie timeserie;
    BufferedReader reader = null;

    public J2KTimeSerieReader(File f) throws IOException {
        open(f);
    }
    

    protected void open(File file) throws IOException {
        checkNotNull(file, "The file must not be null");
        checkArgument(file.isFile() && file.exists(), "The file %s cannot be opened! It is either not existing or it is an directory.", file.getAbsolutePath());
        
        this.fileHandle = file;
        this.header = new J2KTimeSerieHeader();

        try {
            openFile();
            readHeader();
        } catch (Throwable t) {
            throw t; //just rethrow
        } finally {
            closeFile();
        }
    }
    
    private void openFile() throws IOException {
        reader = new BufferedReader(new InputStreamReader(
                      new FileInputStream(fileHandle), "UTF8"));
        reader.mark(0);
    }

    private void closeFile() {
        try {
            reader.close();
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }
    
    public J2KTimeSerieHeader getHeader() {
        return this.header;
    }
    
    @Override
    public J2KTimeSerie getData() {
        if (this.timeserie == null) {
            readData();
        }
        return timeserie;
    }

    private void readData() {
        GenericDataReader store = new GenericDataReader(this.fileHandle.getAbsolutePath(), false, headerLineCount + 1);

        ArrayList<JAMSTableDataArray> doubleArray = store.getAll();

        int n = header.getColumnCount();

        double data[][] = new double[doubleArray.size()][n];

        int timeColums = determineTimeColumnCount(doubleArray.get(0).getValues());

        for (int i = 0; i < doubleArray.size(); i++) {
            String record[] = doubleArray.get(i).getValues();
            for (int j = timeColums; j < record.length; j++) {
                if (record[j].compareTo(header.getMissingDataString()) == 0) {
                    data[i][j-timeColums] = JAMS.getMissingDataValue();
                } else {
                    try {
                        data[i][j-timeColums] = Double.parseDouble(record[j]);
                    } catch (NumberFormatException nfe) {
                        throw new OPTASException("Unable to parse number in column " + j + " of record " + doubleArray.get(i).getTime(), nfe);
                    }
                }
            }
        }

        DataView<double[]> dataSupplier = ViewFactory.createView(data);
        timeserie = new J2KTimeSerie("observation", header, dataSupplier);

        store.close();
    }

    private int determineTimeColumnCount(String[] firstLine) {
        if (firstLine.length == 0) {
            return 0;
        }

        try {
            Double.parseDouble(firstLine[0]);
        } catch (NumberFormatException nfe) {
            if (firstLine.length == 1) {
                return 1;
            }
            try {
                Double.parseDouble(firstLine[1]);
            } catch (NumberFormatException nfe2) {
                return 2;
            }
            return 1;
        }
        return 0;
    }
    
    private void readHeader() throws IOException {
        boolean isDataValueAttribValid = false,
                isDatasetAttribValid = false,
                isStatAttribValid = false;

        //reset to starting position
        reader.reset();
        getNextHeaderLine();
        do {
            String tag = getTokensOfCurrentLine()[0];
            if (tag.equalsIgnoreCase(J2KTSDataStore.TAGNAME_DATAVALUEATTRIBS)) {
                isDataValueAttribValid = readDataValueAttribs();
            } else if (tag.equalsIgnoreCase(J2KTSDataStore.TAGNAME_DATASETATTRIBS)) {
                isDatasetAttribValid = readDataSetAttribs();
            } else if (tag.equalsIgnoreCase(J2KTSDataStore.TAGNAME_STATATTRIBVAL)) {
                isStatAttribValid = readStationAttribs();
            } else if (tag.equalsIgnoreCase(J2KTSDataStore.TAGNAME_DATAVAL)) {
                break;
            } else {
                getNextHeaderLine();
            }
        } while (true);

        if (!isDataValueAttribValid) {
            throw new OPTASException("Failed to read file " + this.fileHandle.getAbsolutePath() + " . Missing " + J2KTSDataStore.TAGNAME_DATAVALUEATTRIBS + " section!");
        }
        if (!isDatasetAttribValid) {
            throw new OPTASException("Failed to read file " + this.fileHandle.getAbsolutePath() + " . Missing " + J2KTSDataStore.TAGNAME_DATASETATTRIBS + " section!");
        }
        if (!isStatAttribValid) {
            throw new OPTASException("Failed to read file " + this.fileHandle.getAbsolutePath() + " . Missing " + J2KTSDataStore.TAGNAME_STATATTRIBVAL + " section!");
        }
    }

    private boolean readDataValueAttribs() throws IOException {
        getNextHeaderLine();
        if (getTokensOfCurrentLine().length >= 2) {
            String dataName = getTokensOfCurrentLine()[0];
            double lowerBound = Double.parseDouble(getTokensOfCurrentLine()[1]);
            double upperBound = Double.parseDouble(getTokensOfCurrentLine()[2]);
        } else {
            throw new IOException("Unexpected count of tokens in " + J2KTSDataStore.TAGNAME_DATAVALUEATTRIBS + ". Expected 3 but found only " + getTokensOfCurrentLine().length);
        }
        getNextHeaderLine();
        return true;
    }

    private boolean readDataSetAttribs() throws IOException {
        boolean isMissingDataValueValid = false,
                isStartDateValid = false,
                isEndDateValid = false,
                isTemporalResolutionValid = false;

        while (!getNextHeaderLine().startsWith("@")) {
            String tokens[] = getTokensOfCurrentLine();
            String tag = tokens[0];

            if (tag.equalsIgnoreCase(J2KTSDataStore.TAGNAME_MISSINGDATAVAL)) {
                if (tokens.length >= 2) {
                    header.setMissingDataString(tokens[1]);
                    isMissingDataValueValid = true;
                }
            } else if (tag.equalsIgnoreCase(J2KTSDataStore.TAGNAME_DATASTART)) {
                if (tokens.length >= 2) {
                    String startDate = tokens[1]; //date part
                    if (tokens.length > 2) {
                        startDate = startDate + " " + tokens[2];
                    }
                    header.setStartTime(parseJ2KTime(startDate).getTime());
                    isStartDateValid = true;
                }
            } else if (tag.equalsIgnoreCase(J2KTSDataStore.TAGNAME_DATAEND)) {
                if (tokens.length >= 2) {
                    String endDate = tokens[1]; //date part
                    if (tokens.length > 2) {
                        endDate = endDate + " " + tokens[2];
                    }
                    header.setEndTime(parseJ2KTime(endDate).getTime());
                    isEndDateValid = true;
                }
            } else if (tag.equalsIgnoreCase(J2KTSDataStore.TAGNAME_TEMP_RES)) {
                if (tokens.length >= 2) {
                    header.setTimeUnit(tokens[1]);
                    isTemporalResolutionValid = true;
                }
            }
        }
        if (!isMissingDataValueValid) {
            throw new OPTASException("Missing data tag is missing is file " + this.fileHandle.getAbsolutePath());
        }
        if (!isStartDateValid) {
            throw new OPTASException("Start date data tag is missing is file " + this.fileHandle.getAbsolutePath());
        }
        if (!isEndDateValid) {
            throw new OPTASException("End date tag is missing is file " + this.fileHandle.getAbsolutePath());
        }
        if (!isTemporalResolutionValid) {
            throw new OPTASException("Temporal resolution tag is missing is file " + this.fileHandle.getAbsolutePath());
        }
        return true;
    }

    private boolean readStationAttribs() throws IOException {
        boolean isNameTagValid = false,
                isIDTagValid = false,
                isElevationTagValid = false,
                isXTagValid = false,
                isYTagValid = false,
                isDataColumTagValid = false;

        boolean isInit = false;
        do {
            getNextHeaderLine();
            int nstat = getTokensOfCurrentLine().length - 1;
            String tag = getTokensOfCurrentLine()[0];

            if (getCurrentHeaderLine().startsWith("@")) {
                break;
            }

            if (!isInit) {
                header.init(nstat);
                isInit = true;
            } else {
                if (nstat != header.getColumnCount()) {
                    throw new OPTASException("Number of columns does not agree. I expect " + header.getColumnCount() + " oolumns, but found " + nstat + " columns in line " + headerLineCount + " of file " + fileHandle.getAbsolutePath());
                }
            }

            if (tag.equalsIgnoreCase("name")) {
                for (int j = 1; j <= nstat; j++) {
                    header.setAttributeName(j - 1, getTokensOfCurrentLine()[j]);
                }
                isNameTagValid = true;

            } else if (tag.equalsIgnoreCase("id")) {
                for (int j = 1; j <= nstat; j++) {
                    header.setId(j - 1, Double.parseDouble(getTokensOfCurrentLine()[j]));
                }
                isIDTagValid = true;

            } else if (tag.equalsIgnoreCase("elevation")) {
                for (int j = 1; j <= nstat; j++) {
                    header.setElevation(j - 1, Double.parseDouble(getTokensOfCurrentLine()[j]));
                }
                isElevationTagValid = true;

            } else if (tag.equalsIgnoreCase("x") || tag.equalsIgnoreCase("lat")) {
                header.setNameOfDimension(Dimension.X, tag.toLowerCase());
                for (int j = 1; j <= nstat; j++) {
                    header.setLocation(j - 1, Double.parseDouble(getTokensOfCurrentLine()[j]),
                            header.getLocation(j - 1)[0]);
                }
                isXTagValid = true;

            } else if (tag.equalsIgnoreCase("y") || tag.equalsIgnoreCase("lon")) {
                header.setNameOfDimension(Dimension.Y, tag.toLowerCase());
                for (int j = 1; j <= nstat; j++) {
                    header.setLocation(j - 1, Double.parseDouble(getTokensOfCurrentLine()[j]),
                            header.getLocation(j - 1)[1]);
                }
                isYTagValid = true;

            } else if (tag.equalsIgnoreCase("datacolumn")) {
                //do nothing for the moment
                isDataColumTagValid = true;
            }
        } while (true);

        if (!isNameTagValid) {
            throw new OPTASException("Name tag is missing is file " + this.fileHandle.getAbsolutePath());
        }
        if (!isIDTagValid) {
            throw new OPTASException("ID tag is missing is file " + this.fileHandle.getAbsolutePath());
        }
        if (!isElevationTagValid) {
            throw new OPTASException("Elevation tag is missing is file " + this.fileHandle.getAbsolutePath());
        }
        if (!isXTagValid || !isYTagValid) {
            throw new OPTASException("Location tag is missing is file " + this.fileHandle.getAbsolutePath());
        }
        if (!isDataColumTagValid) {
            throw new OPTASException("Data column tag is missing is file " + this.fileHandle.getAbsolutePath());
        }
        return true;
    }

    private String getNextHeaderLine() throws IOException {
        do {
            lineBuffer = reader.readLine();
            if (lineBuffer == null) {
                throw new IOException("Unexpected end of file. File " + fileHandle.getAbsolutePath() + " is corrupt.");
            }
            headerLineCount++;
            fillTokenBuffer();
        } while (lineBuffer.length() == 0 || lineBuffer.charAt(0) == '#');

        return lineBuffer;
    }

    private String getCurrentHeaderLine() {
        return lineBuffer;
    }

    private void fillTokenBuffer() {
        StringTokenizer strTok = new StringTokenizer(lineBuffer, SEPARATOR);

        int tokenCount = strTok.countTokens();
        tokenBuffer = new String[tokenCount];
        for (int i = 0; i < tokenCount; i++) {
            tokenBuffer[i] = cleanToken(strTok.nextToken());
        }
    }

    private String cleanToken(String token) {
        while (token.endsWith(" ") || token.endsWith("\t")) {
            token = token.substring(0, token.length() - 1);
        }
        return token;
    }

    private String[] getTokensOfCurrentLine() {
        return tokenBuffer;
    }

    protected int getHeaderLineCount() {
        return this.headerLineCount;
    }

    private Attribute.Calendar parseJ2KTime(String timeString) {
        SimpleDateFormat sdf1 = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        SimpleDateFormat sdf2 = new SimpleDateFormat("dd.MM.yyyy HH:mm");
        sdf1.setTimeZone(TimeZone.getTimeZone("GMT"));
        sdf2.setTimeZone(TimeZone.getTimeZone("GMT"));

        Date d = null;
        try {
            d = sdf1.parse(timeString);
        } catch (ParseException pe) {
            try {
                d = sdf2.parse(timeString);
            } catch (ParseException pe2) {
                return null;
            }
        }
        Attribute.Calendar cal = DefaultDataFactory.getDataFactory().createCalendar();
        cal.setTime(d);
        return cal;
    }
}
