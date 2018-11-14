/*
 * TemporalNestedContext.java
 * Created on 14.11.2018, 17:28:03
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

import jams.model.*;
import jams.workspace.stores.OutputDataStore;
import jams.JAMS;
import jams.data.*;
import jams.dataaccess.DataAccessor;
import jams.io.datatracer.DataTracer;
import jams.io.datatracer.AbstractTracer;
import jams.workspace.stores.Filter;

/**
 *
 * @author S. Kralisch
 */
@JAMSComponentDescription(title = "Nested temporal context",
        author = "Sven Kralisch",
        date = "2018-11-14",
        version = "1.0_0",
        description = "This component represents a JAMS context which can be "
                + "used to represent iteration over discrete time steps "
                + "typically used in conceptional environmental models. In "
                + "addition, this context can be nested within another "
                + "temporal context to iterate over sub-timesteps.")
@VersionComments(entries = {
    @VersionComments.Entry(version = "1.0_0", date = "2018-11-14", comment = "Initial Version")
})
public class TemporalNestedContext extends JAMSContext {

    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
            description = "Current outer date/time")
    public Attribute.Calendar outerTime;

    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
            description = "Significant component of outer date/time:\n"
                    + " 1 - YEAR\n"
                    + " 2 - MONTH\n"
                    + " 3 - WEEK\n"
                    + " 5 - DAY\n"
                    + "11 - HOUR\n"
                    + "12 - MINUTE\n"
                    + "13 - SECOND\n")
    public Attribute.Integer significantComponent;
    
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
            description = "Step size of inner date/time iteration:\n"
                    + " 1 - YEAR\n"
                    + " 2 - MONTH\n"
                    + " 3 - WEEK\n"
                    + " 5 - DAY\n"
                    + "11 - HOUR\n"
                    + "12 - MINUTE\n"
                    + "13 - SECOND\n")
    public Attribute.Integer stepSize;    

    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
            description = "Step count of inner date/time iteration",
            defaultValue = "1")
    public Attribute.Integer stepCount;

    @JAMSVarDescription(access = JAMSVarDescription.AccessType.WRITE,
            description = "Current date/time of temporal context")
    public Attribute.Calendar current;

    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
            description = "Print the current time every \"printTime\" time steps",
            defaultValue = "0")
    public Attribute.Integer printTime;

    private Attribute.Calendar lastValue, firstValue;
    private int counter = 0;

    public TemporalNestedContext() {
        super();
    }

    @Override
    protected DataTracer createDataTracer(OutputDataStore store) {
        return new AbstractTracer(this, store, JAMSLong.class) {

            @Override
            public void trace() {
                // check for filters on other contexts first
                for (Filter filter : store.getFilters()) {
                    if (filter.getContext() != TemporalNestedContext.this) {
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
                    if (filter.getContext() == TemporalNestedContext.this) {
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

    @Override
    public void init() {
        super.init();
        if (outerTime == null) {
            getModel().getRuntime().sendErrorMsg("No outer date/time provided");
        }
        if (current == null) {
            current = this.getModel().getRuntime().getDataFactory().createCalendar();
        }
    }

    @Override
    public void initAll() {
        super.initAll();
    }

    @Override
    public void cleanupAll() {
        super.cleanupAll();
    }

    @Override
    public void run() {
        
        firstValue = outerTime.clone();
        firstValue.removeUnsignificantComponents(significantComponent.getValue());
        
//!        lastValue.add(timeInterval.getTimeUnit(), -timeInterval.getTimeUnitCount());
        lastValue.add(Attribute.Calendar.MILLISECOND, 1);
        
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

                ComponentEnumerator ce = getTCChildrenEnumerator();
                //DataTracer dataTracers = getDataTracer();

                @Override
                public boolean hasNext() {
                    boolean nextTime = current.before(lastValue);
                    boolean nextComp = ce.hasNext();
                    return (nextTime || nextComp);
                }

                @Override
                public boolean hasPrevious() {
                    boolean prevTime = true;//!current.after(timeInterval.getStart());
                    boolean prevComp = ce.hasPrevious();
                    return (prevTime || prevComp);
                }

                @Override
                public Component next() {
                    // check end of component elements list, if required switch to the next
                    // timestep start with the new Component list again
                    if (!ce.hasNext() && current.before(lastValue)) {
                        for (DataTracer dataTracer : getDataTracers()) {
                            dataTracer.trace();
                        }
//!                        current.add(timeInterval.getTimeUnit(), timeInterval.getTimeUnitCount());
                        if (printTime.getValue() > 0) {
                            if ((counter % printTime.getValue()) == 0) {
                                counter = 0;
                                getModel().getRuntime().println(getInstanceName() + " " + current, JAMS.SILENT);
                            }
                            counter++;
                        }
                        ce.reset();
                    }
                    return ce.next();
                }

                @Override
                public void reset() {
//!                    current.setValue(timeInterval.getStart().getValue());
                    ce.reset();
                }

                public Component previous() {
                    if (ce.hasPrevious()) {
                        return ce.previous();
                    } else {
//!                        current.add(timeInterval.getTimeUnit(), -timeInterval.getTimeUnitCount());
                        while (ce.hasNext()) {
                            ce.next();
                        }
                        return ce.previous();
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

    private DataTracer[] getDataTracers() {
        return dataTracers;
    }

    private ComponentEnumerator getTCChildrenEnumerator() {
        return getChildrenEnumerator();
    }

    @Override
    public long getNumberOfIterations() {
//!        return timeInterval.getNumberOfTimesteps();
return 0;
    }

    @Override
    public String getTraceMark() {
        return current.toString();
    }
}
