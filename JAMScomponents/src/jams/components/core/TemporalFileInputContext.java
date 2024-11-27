/*
 * FileInputContext.java
 * Created on 27.07.2017, 13:36:19
 *
 * This file is part of JAMS
 * Copyright (C) FSU Jena
 *
 * JAMS is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public License
 * as published by the Free Software Foundation; either version 3
 * of the License, or (at your option) any later version.
 *
 * JAMS is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with JAMS. If not, see <http://www.gnu.org/licenses/>.
 *
 */
package jams.components.core;

import jams.JAMS;
import jams.data.*;
import jams.dataaccess.DataAccessor;
import jams.io.BufferedFileReader;
import jams.io.datatracer.AbstractTracer;
import jams.io.datatracer.DataTracer;
import jams.model.*;
import jams.workspace.stores.Filter;
import jams.workspace.stores.OutputDataStore;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Sven Kralisch <sven.kralisch at uni-jena.de>
 */
@JAMSComponentDescription(
        title = "TemporalFileInputContext",
        author = "Sven Kralisch",
        description = "Context that iterates over lines in a tab-separated "
        + "text file and offers the current time step stored in the "
        + "first column (attribute \"time\") and the data in other "
        + "columns (attribute \"values\").",
        date = "2024-11-24",
        version = "1.1"
)
@VersionComments(entries = {
    @VersionComments.Entry(version = "1.0_0", date = "2018-09-12", comment = "Initial version"),
    @VersionComments.Entry(version = "1.1", date = "2024-11-24", comment = "Complete rework of the component")
})
public class TemporalFileInputContext extends JAMSContext {

    /*
     *  Component attributes
     */
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
            description = "Time interval of temporal context")
    public Attribute.TimeInterval timeInterval;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "The name of the file to read from"
    )
    public Attribute.String fileName;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "The key word that indicates the start of the data section",
            defaultValue = "@dataVal"
    )
    public Attribute.String startIndicator;

    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
            description = "The format string for the date format given",
            defaultValue = "yyyy-MM-dd HH:mm")
    public Attribute.String dateFormatString;   

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "Read the values in each line of the input file?",
            defaultValue = "False"
    )
    public Attribute.Boolean readValues;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "Column index at which to start in case readValues is true ("
                    + "first column index is 0)",
            defaultValue = "1"
    )
    public Attribute.Integer firstColumn;
    
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
            description = "Print the current time every \"printTime\" time steps",
            defaultValue = "0")
    public Attribute.Integer printTime;    

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            description = "The values in each line of the input file"
    )
    public Attribute.Double[] values;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            description = "The current time, if \"parseDateTime\" was set to "
            + "\"true\""
    )
    public Attribute.Calendar current;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            description = "Lines skipped to reach timeInterval start"
    )
    public Attribute.Integer skipLines;

    transient private BufferedFileReader fileReader;
    private String line;
    private int counter;
    private List<Attribute.Calendar> dates = new ArrayList();
    private int calIndex;

    @Override
    protected DataTracer createDataTracer(OutputDataStore store) {
        return new AbstractTracer(this, store, JAMSLong.class) {

            @Override
            public void trace() {
                // check for filters on other contexts first
                for (Filter filter : store.getFilters()) {
                    if (filter.getContext() != TemporalFileInputContext.this) {
                        String s = filter.getContext().getTraceMark();
                        //Matcher matcher = filter.getPattern().matcher(s);
                        if (!filter.isFiltered(s)) {
                            return;
                        }
                    }
                }

                String traceMark = getTraceMark();

                // take care of filters in this context
                for (Filter filter : store.getFilters()) {
                    if (filter.getContext() == TemporalFileInputContext.this) {
                        //Matcher matcher = filter.getPattern().matcher(traceMark);
                        if (!filter.isFiltered(traceMark)) {
                            return;
                        }
                    }
                }

                // if we haven't output a mark so far, do it now
                if (!hasOutput()) {
                    setOutput(true);
                    startMark();
                }

                output(traceMark);
                for (DataAccessor dataAccessor : getAccessorObjects()) {
                    output(dataAccessor.getComponentObject());
                }
                nextRow();
                flush();
            }
        };
    }

    /*
     *  Component run stages
     */
    @Override
    public void init() {
//        this.timeInterval = getModel().getRuntime().getDataFactory().createTimeInterval();
//        this.timeInterval.setValue("1970-01-01 00:00 1970-01-01 00:00 1 1");
        super.init();
        initFile();
    }

    @Override
    public void initAll() {
        super.initAll();
    }

    @Override
    public void cleanupAll() {
        super.cleanupAll();
    }

    private void initFile() {
        
        DataFactory f = getModel().getRuntime().getDataFactory();
        Attribute.Calendar date = f.createCalendar();
        
        try {
            fileReader = new BufferedFileReader(new FileInputStream(new File(this.getModel().getWorkspacePath(), fileName.getValue())), JAMS.getCharset());
            while ((line = fileReader.readLine()) != null) {
                if (line.trim().equals(startIndicator.getValue())) {
                    break;
                }
            }
            line = fileReader.readLine();
            String[] cols = line.split("\t");
            date.setValue(cols[0], dateFormatString.getValue());

            if (date.compareTo(timeInterval.getStart(), timeInterval.getTimeUnit()) > 0) {
                getModel().getRuntime().sendHalt("Error: First date in data file is after start of timeInterval.");
            }

            int skipLines = 0;
            while (date.compareTo(timeInterval.getStart(), timeInterval.getTimeUnit()) < 0) {
                line = fileReader.readLine();
                cols = line.split("\t");               
                date.setValue(cols[0], dateFormatString.getValue());
                skipLines++;
            }
            
            while (!line.startsWith("#") && date.compareTo(timeInterval.getEnd(), timeInterval.getTimeUnit()) <= 0) {
                Attribute.Calendar newDate = f.createCalendar();
                newDate.setValue(date);
                dates.add(newDate);
                line = fileReader.readLine();
                if (!line.startsWith("#")) {
                    cols = line.split("\t");
                    date.setValue(cols[0], dateFormatString.getValue());                    
                }
            }
            
            getModel().getRuntime().println(dates.get(0) + " <-> " + dates.get(dates.size()-1), JAMS.VERBOSE);
                       
            calIndex = 0;
            current.setValue(dates.get(calIndex));

//            if (readValues.getValue()) {
//                int i = 0;
//                for (String value : line.split("\\s+")) {
//                    values[i].setValue(value);
//                    i++;
//                }
//            }

            counter = 0;
            this.skipLines.setValue(skipLines);
            
        } catch (FileNotFoundException ex) {
            getModel().getRuntime().handle(ex);
        } catch (IOException | ParseException ex) {
            getModel().getRuntime().handle(ex);
        }

    }

    @Override
    public void run() {
        super.run();
        if (!this.isPaused) {
            for (DataTracer dataTracer : dataTracers) {
                if (dataTracer.hasOutput()) {
                    dataTracer.endMark();
                    dataTracer.setOutput(false);
                }
            }
        }
    }

    @Override
    public void cleanup() {
        super.cleanup();
        try {
            fileReader.close();
        } catch (IOException ex) {
            getModel().getRuntime().handle(ex);
        }
    }

    @Override
    protected ComponentEnumerator getInitAllEnumerator() {
        return getInitEnumerator();
    }

    @Override
    protected ComponentEnumerator getCleanupAllEnumerator() {
        return getInitEnumerator();
    }

    @Override
    protected ComponentEnumerator getRunEnumerator() {
        // check if there are components to iterate on
        if (!components.isEmpty()) {
            // if yes, return standard enumerator
            return new ComponentEnumerator() {

                ComponentEnumerator ce = getFICChildrenEnumerator();
                //DataTracer dataTracers = getDataTracer();

                @Override
                public boolean hasNext() {

                    boolean nextComp = ce.hasNext();
                    boolean nextTime = calIndex < dates.size() - 1;
                    return (nextTime || nextComp);
                    
//                    if (!nextComp) {
//                        try {
//                            
//                            line = fileReader.readLine();
//                            
//                            if (line != null) {
//                                String[] cols = line.split("\t");
//                                date.setValue(cols[0], dateFormatString.getValue());
//                                nextLine = date.compareTo(timeInterval.getEnd(), timeInterval.getTimeUnit()) <= 0;
//                            } else {
//                                nextLine = false;
//                            }                           
//                            
//                        } catch (IOException | ParseException ex) {
//                            getModel().getRuntime().handle(ex);
//                        }
//                    }
//                    return (nextLine || nextComp);
                }

                @Override
                public boolean hasPrevious() {
                    boolean prevTime = calIndex > 0;
                    boolean prevComp = ce.hasPrevious();
                    return (prevTime || prevComp);
                }

                @Override
                public Component next() {
                    // check end of component elements list, if required switch to the next
                    // timestep start with the new Component list again
                    if (!ce.hasNext() && (calIndex < dates.size() - 1)) {
                        for (DataTracer dataTracer : getDataTracers()) {
                            dataTracer.trace();
                        }
                        calIndex++;
                        current.setValue(dates.get(calIndex));

//                        if (readValues.getValue()) {
//                            values[i].setValue(value);
//                            i++;
//                        }

                        printTime();
                        ce.reset();
                    }
                    return ce.next();
                }

                @Override
                public void reset() {
//                    initFile();
                    calIndex = 0;
                    printTime();
                    ce.reset();
                }

                public Component previous() {
                    if (ce.hasPrevious()) {
                        return ce.previous();
                    } else {
                        calIndex--;
                        current.setValue(dates.get(calIndex));
                        while (ce.hasNext()) {
                            ce.next();
                        }
                        return ce.previous();
                    }
                }

                private void printTime() {
                    if (printTime.getValue() > 0) {
                        if ((counter % printTime.getValue()) == 0) {
                            counter = 0;
                            getModel().getRuntime().println(getInstanceName() + " " + current, JAMS.SILENT);
                        }
                        counter++;
                    }
                }
            };
        } else {
            // if not, return empty enumerator
            return new ComponentEnumerator() {

                @Override
                public boolean hasNext() {
                    return false;
                }

                @Override
                public boolean hasPrevious() {
                    return false;
                }

                @Override
                public Component next() {
                    return null;
                }

                @Override
                public Component previous() {
                    return null;
                }

                @Override
                public void reset() {

                }
            };
        }
    }

    private ComponentEnumerator getFICChildrenEnumerator() {
        return getChildrenEnumerator();
    }

    private DataTracer[] getDataTracers() {
        return dataTracers;
    }

    @Override
    public long getNumberOfIterations() {
        return dates.size();
    }

    @Override
    public String getTraceMark() {
        return current.toString();
    }
}
