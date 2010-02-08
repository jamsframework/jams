/*
 * JAMSTemporalContext.java
 * Created on 31. Juli 2005, 20:24
 *
 * This file is part of JAMS
 * Copyright (C) 2005 FSU Jena
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
package jams.model;

import jams.workspace.stores.OutputDataStore;
import java.util.regex.Matcher;
import jams.data.*;
import jams.dataaccess.DataAccessor;
import jams.io.datatracer.DataTracer;
import jams.io.datatracer.AbstractTracer;
import jams.workspace.stores.Filter;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

/**
 *
 * @author S. Kralisch
 */
@JAMSComponentDescription(title = "JAMS temporal context",
author = "Sven Kralisch",
date = "31. Juli 2005",
description = "This component represents a JAMS context which can be used to " +
"represent temporal contexts in environmental models")
public class JAMSTemporalContext extends JAMSContext {

    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
    description = "Time interval of temporal context")
    public JAMSTimeInterval timeInterval;
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.WRITE,
    description = "Current date of temporal context")
    public Attribute.Calendar current;
    private Attribute.Calendar lastValue;

    public JAMSTemporalContext() {
        super();
    }

    @Override
    protected DataTracer createDataTracer(OutputDataStore store) {
        return new AbstractTracer(this, store, Attribute.Calendar.class) {

            @Override
            public void trace() {

                // check for filters on other contexts first
                for (Filter filter : store.getFilters()) {
                    String s = filter.getContext().getTraceMark();
                    Matcher matcher = filter.getPattern().matcher(s);
                    if (!matcher.matches()) {
                        return;
                    }
                }

                output(current);
                
                for (DataAccessor dataAccessor : getAccessorObjects()) {
                    output(dataAccessor.getComponentObject());                
                }
                this.nextRow();
            }
        };
    }

    @Override
    public void init() {
        super.init();
        lastValue = timeInterval.getEnd().clone();
        lastValue.add(timeInterval.getTimeUnit(), -timeInterval.getTimeUnitCount());
        lastValue.add(JAMSCalendar.MILLISECOND, 1);
        if (current == null) {
            current = JAMSDataFactory.createCalendar();
        }
    }

    @Override
    public ComponentEnumerator getRunEnumerator() {
        // check if there are components to iterate on
        if (!components.isEmpty()) {
            // if yes, return standard enumerator
            return new RunEnumerator();
        } else {
            // if not, return empty enumerator
            return new RunEnumerator() {

                @Override
                public boolean hasNext() {
                    return false;
                }

                @Override
                public Component next() {
                    return null;
                }

                @Override
                public void reset() {
                }
            };
        }
    }

    @Override
    public long getNumberOfIterations() {
        return timeInterval.getNumberOfTimesteps();
    }

    @Override
    public String getTraceMark() {
        return current.toString();
    }

    public static class IteratorState implements Serializable {

            byte subState[];
            Attribute.Calendar current;
            Attribute.Calendar lastValue;
        };
        
    class RunEnumerator implements ComponentEnumerator {

        ComponentEnumerator ce = getChildrenEnumerator();
        //DataTracer dataTracers = getDataTracer();
        @Override
        public boolean hasNext() {
            boolean nextTime = current.before(lastValue);
            boolean nextComp = ce.hasNext();
            return (nextTime || nextComp);
        }

        @Override
        public Component next() {
            // check end of component elements list, if required switch to the next
            // timestep start with the new Component list again
            if (!ce.hasNext() && current.before(lastValue)) {
                for (DataTracer dataTracer : dataTracers) {
                    dataTracer.trace();
                }
                current.add(timeInterval.getTimeUnit(), timeInterval.getTimeUnitCount());
                ce.reset();
            }
            return ce.next();
        }

        @Override
        public void reset() {
            current.setValue(timeInterval.getStart().getValue());
            ce.reset();
        }
        
        @Override
        public byte[] getState() {
            IteratorState state = new IteratorState();
            state.subState = ce.getState();

            state.current = current.getValue();
            state.lastValue = lastValue.getValue();

            ByteArrayOutputStream outStream = new ByteArrayOutputStream();
            byte[] result = null;
            try {
                ObjectOutputStream objOut = new ObjectOutputStream(outStream);
                objOut.writeObject(state);

                result = outStream.toByteArray();

                objOut.close();
                outStream.close();
            } catch (Exception e) {
                getModel().getRuntime().println("could not save model state, because:" + e);
            }

            return result;
        }

        @Override
        public void setState(byte[] state) {
            ce = getChildrenEnumerator();
            
            ByteArrayInputStream inStream = new ByteArrayInputStream(state);
            try {
                ObjectInputStream objIn = new ObjectInputStream(inStream);
                inStream.close();

                IteratorState myState = (IteratorState) objIn.readObject();

                objIn.close();

                ce.setState(myState.subState);
                
                current.setValue(myState.current);
                lastValue.setValue(myState.lastValue);
                
            } catch (Exception e) {
            }
        }       
    }
    @Override
    public byte[] getIteratorState(){
        if (this.runEnumerator != null)
            return this.runEnumerator.getState();
        return null;
    }
    @Override
    public void setIteratorState(byte[]state){
        if (state == null)
            this.runEnumerator = null;
        else
            this.runEnumerator.setState(state);
    }
}
