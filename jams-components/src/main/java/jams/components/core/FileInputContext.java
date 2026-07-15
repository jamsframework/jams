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

/**
 *
 * @author Sven Kralisch <sven.kralisch at uni-jena.de>
 */
@JAMSComponentDescription(
        title = "FileInputContext",
        author = "Sven Kralisch",
        description = "Context that iterates over lines in a tab-separated "
        + "text file and offers the stored data in different "
        + "columns (attribute \"values\").",
        date = "2018-09-14",
        version = "1.0_1"
)
@VersionComments(entries = {
    @VersionComments.Entry(version = "1.0_0", comment = "Initial version"),
    @VersionComments.Entry(version = "1.0_1", date = "2018-09-14", comment = "Some improvements")
})
public class FileInputContext extends JAMSContext {

    /*
     *  Component attributes
     */
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "The name of the file to read from"
    )
    public Attribute.String fileName;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "The key word that indicates the start of the data section",
            defaultValue = "@start"
    )
    public Attribute.String startIndicator;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            description = "Values that were read from file"
    )
    public Attribute.Double[] values;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            description = "Current iteration number"
    )
    public Attribute.Double counter;

    transient private BufferedFileReader fileReader;
    private String line;
    private int counter_;

    @Override
    protected DataTracer createDataTracer(OutputDataStore store) {
        return new AbstractTracer(this, store, JAMSLong.class) {

            @Override
            public void trace() {
                // check for filters on other contexts first
                for (Filter filter : store.getFilters()) {
                    if (filter.getContext() != FileInputContext.this) {
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
                    if (filter.getContext() == FileInputContext.this) {
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
        try {
            fileReader = new BufferedFileReader(new FileInputStream(new File(this.getModel().getWorkspacePath(), fileName.getValue())), JAMS.getCharset());
            while ((line = fileReader.readLine()) != null) {
                if (line.startsWith(startIndicator.getValue())) {
                    break;
                }
            }
            line = fileReader.readLine();
            int i = 0;
            boolean first = true;
            for (String value : line.split("\\s+")) {
                values[i].setValue(value);
                i++;
            }

            counter_ = 0;
            counter.setValue(counter_);
        } catch (FileNotFoundException ex) {
            getModel().getRuntime().handle(ex);
        } catch (IOException ex) {
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
                    if (!nextComp) {
                        try {
                            line = fileReader.readLine();
                        } catch (IOException ex) {
                            getModel().getRuntime().handle(ex);
                        }
                    }
                    boolean nextLine = (line != null);
                    return (nextLine || nextComp);
                }

                @Override
                public boolean hasPrevious() {
                    boolean prevLine = false;
                    boolean prevComp = ce.hasPrevious();
                    return (prevLine || prevComp);
                }

                @Override
                public Component next() {
                    // check end of component elements list, if required switch to the next
                    // timestep start with the new Component list again
                    if (!ce.hasNext() && (line != null)) {
                        for (DataTracer dataTracer : getDataTracers()) {
                            dataTracer.trace();
                        }

                        int i = 0;
                        boolean first = true;
                        for (String value : line.split("\\s+")) {
                            values[i].setValue(value);
                            i++;
                        }
                        counter_++;
                        counter.setValue(counter_);

                        ce.reset();
                    }
                    return ce.next();
                }

                @Override
                public void reset() {
                    initFile();
                    ce.reset();
                }

                public Component previous() {
                    if (ce.hasPrevious()) {
                        return ce.previous();
                    } else {
                        return null;
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
        return 1;
    }

    @Override
    public String getTraceMark() {
        return Integer.toString(counter_);
    }
}
