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
package jams.components.tools;

import jams.workspace.stores.OutputDataStore;
import java.util.regex.Matcher;
import jams.data.*;
import jams.dataaccess.DataAccessor;
import jams.io.DataTracer.DataTracer;
import jams.io.DataTracer.AbstractTracer;
import jams.model.Component;
import jams.model.ComponentEnumerator;
import jams.model.JAMSComponentDescription;
import jams.model.JAMSContext;
import jams.model.JAMSVarDescription;
import jams.workspace.DataReader;
import jams.workspace.plugins.PollingSQL;
import jams.workspace.stores.Filter;
import jams.workspace.stores.InputDataStore;
import jams.workspace.stores.J2KTSDataStore;
import jams.workspace.stores.TSDataStore;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;

/**
 *
 * @author S. Kralisch
 */
@JAMSComponentDescription(title = "JAMS temporal context",
author = "Chritian Fischer",
date = "01. Decembre 2009",
description = "this is specific to a TLUG project" +
        "This component executes the model in a first time interval and saves the model state" + 
        "the remaining time interval is executed multiple times with different forecasting data")
public class JAMSTLUGProjectTemporalContext extends JAMSContext {

    @JAMSVarDescription(access = JAMSVarDescription.AccessType.WRITE,
    description = "Current date of temporal context")
    public Attribute.TimeInterval timeInterval;
    
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
    description = "time at which model execution is restarted, in days before now")
    public Attribute.Integer maxForecastedDays;
        
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.WRITE,
    description = "Current date of temporal context")
    public Attribute.Calendar current;
    
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
    description = "number of iterations")
    public Attribute.Integer iterationCount;
    
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
    description = "time between iterations in seconds")
    public Attribute.Integer pollingDelay;
    
    private Attribute.Calendar lastValue;
    private Attribute.Calendar iterationStartTime;
    
    JAMSModelSnapshot snapshot;
    int iterationCounter = 0;
    
    public JAMSTLUGProjectTemporalContext() {
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
                output("\t");
                for (DataAccessor dataAccessor : getAccessorObjects()) {
                    output(dataAccessor.getComponentObject());
                    output("\t");
                }
                output("\n");
            }
        };
    }

    public void updateTimeInterval(){
        //determine last value by asking the input data stores for their last date
        lastValue = JAMSDataFactory.createCalendar();        
        lastValue.setTime(new Date(2100,11,0));
                
        Iterator<InputDataStore> iter = this.getModel().getWorkspace().getRegisteredInputDataStores().iterator();
        while(iter.hasNext()){
            InputDataStore store = iter.next();            
            if (store instanceof TSDataStore){
                TSDataStore tsStore = (TSDataStore)store;
                Iterator<DataReader> readerIter = tsStore.getDataIOs().iterator();
                while (readerIter.hasNext()){
                    DataReader reader = readerIter.next();
                    if (reader instanceof PollingSQL){
                        PollingSQL poll = (PollingSQL)reader;
                        Attribute.Calendar cal = poll.getLastDate();
                        if (lastValue.after(cal)){
                            lastValue = cal;                            
                        }
                    }
                }
            }
        }
        lastValue.add(timeInterval.getTimeUnit(), -timeInterval.getTimeUnitCount());
        lastValue.add(JAMSCalendar.MILLISECOND, 1);            
    }
    
    @Override
    public void init() {
        super.init();
        
        if (current == null) {
            current = JAMSDataFactory.createCalendar();
        }
        
        snapshot = new JAMSModelSnapshot();
        snapshot.setContext(this);
        snapshot.setModel(getModel());

        snapshot.enable = JAMSDataFactory.createBoolean();
        snapshot.enable.setValue(true);
        
        snapshot.holdInMemory = JAMSDataFactory.createBoolean();
        snapshot.holdInMemory.setValue(true);
        
        snapshot.saveIterator = JAMSDataFactory.createBoolean();
        snapshot.saveIterator.setValue(true);                         
        
        snapshot.loadSnapshot = JAMSDataFactory.createBoolean();
        snapshot.takeSnapshot = JAMSDataFactory.createBoolean();
        
        snapshot.data = JAMSDataFactory.createEntity();
        
        updateTimeInterval();
        
        iterationCounter = 0;
        iterationStartTime = JAMSDataFactory.createCalendar();
        iterationStartTime.setTimeInMillis(lastValue.getTimeInMillis());
        iterationStartTime.add(Calendar.DAY_OF_YEAR, -this.maxForecastedDays.getValue());                
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
        updateTimeInterval();
        int unitInMillis = 0;
        switch (timeInterval.getTimeUnit()) {
            case Calendar.DAY_OF_YEAR: unitInMillis = 24*60*60*1000;
            case Calendar.HOUR: unitInMillis = 60*60*1000;
            case Calendar.MONTH: unitInMillis = 24*60*60*1000*30;
            case Calendar.YEAR: unitInMillis = 24*60*60*1000*365;
            default: unitInMillis = 60*60*1000;
        }
        long count = (this.lastValue.getTimeInMillis()-this.timeInterval.getStart().getTimeInMillis()) / unitInMillis;        
        return count;
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
            boolean nextIter = iterationCounter < iterationCount.getValue()-1;
            return ( nextIter || nextTime || nextComp);
        }

        @Override
        public Component next() {            
            // check end of component elements list, if required switch to the next
            // timestep start with the new Component list again
            if (!ce.hasNext() && current.before(lastValue)) {
                for (DataTracer dataTracer : dataTracers) {
                    dataTracer.trace();
                }
                boolean breakNotReached = false;
                if (current.before(iterationStartTime)){
                    breakNotReached = true;
                }      
                current.add(timeInterval.getTimeUnit(), timeInterval.getTimeUnitCount());
                ce.reset();
                
                //breakpoint crossed?                
                if (!current.before(iterationStartTime) && breakNotReached){
                    snapshot.loadSnapshot.setValue(false);
                    snapshot.takeSnapshot.setValue(true);
                    snapshot.run();
                }                                                                                
            }else{
                if (!ce.hasNext() && !current.before(lastValue)){
                    try{
                        Thread.sleep(pollingDelay.getValue()*1000);                        
                    }catch(Exception e){
                        getModel().getRuntime().println("could not sleep, because:" + e);
                    }
                    snapshot.loadSnapshot.setValue(true);
                    snapshot.takeSnapshot.setValue(false);
                    snapshot.run();   
                    updateTimeInterval();
                    iterationCounter++;
                    if (iterationCounter == iterationCount.getValue()){
                        return null;
                    }                    
                }                
            }
            
            return ce.next();
        }

        @Override
        public void reset() {
            current.setValue(timeInterval.getStart());
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
                //milliseconds are getting lost .. 
                lastValue.add(JAMSCalendar.MILLISECOND, 1);
                
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
