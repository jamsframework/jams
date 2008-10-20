/*
 * TSDataStoreReader.java
 * Created on 16. Oktober 2008, 17:34
 *
 * This file is a JAMS component
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
package jams.components.io;

import jams.JAMS;
import jams.data.*;
import jams.model.*;
import jams.workspace.DataSet;
import jams.workspace.datatypes.DataValue;
import jams.workspace.stores.InputDataStore;
import jams.workspace.stores.TSDataStore;

/**
 *
 * @author Sven Kralisch <sven.kralisch at uni-jena.de>
 */
@JAMSComponentDescription(title = "TSDataStoreReader",
author = "Sven Kralisch",
description = "This component can be used obtain data from a time series " +
"data store which contains only double values.")
public class TSDataStoreReader extends JAMSComponent {

    /*
     *  Component variables
     */
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
    description = "Datastore ID")
    public JAMSString id;
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
    description = "The time interval within which the component shall read " +
    "data from the datastore")
    public JAMSTimeInterval timeinterval;
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
    description = "The current time within the timeinterval")
    public JAMSCalendar time;
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.WRITE,
    description = "Array of double values received from the datastore. Order " +
    "according to datastore")
    public JAMSDoubleArray doubleValues = new JAMSDoubleArray();
    private TSDataStore store;
    private double[] doubles;

    @Override
    public void init() {

        InputDataStore is = null;
        if (id != null) {
            is = getModel().getWorkspace().getInputDataStore(id.getValue());
        }

        // check if store exists
        if (is == null) {
            getModel().getRuntime().sendHalt("Error accessing datastore \"" +
                    id + "\" from " + getInstanceName() + ": Datastore could not be found!");
            return;
        }

        // check if this is a TSDataStore
        if (!(is instanceof TSDataStore)) {
            getModel().getRuntime().sendHalt("Error accessing datastore \"" +
                    id + "\" from " + getInstanceName() + ": Datastore is not a time series datastore!");
            return;
        }

        store = (TSDataStore) is;

        // check if the store's time interval matches the provided time interval
        if (store.getStartDate().after(timeinterval.getStart()) && (store.getStartDate().compareTo(timeinterval.getStart(), timeinterval.getTimeUnit()) != 0)) {
            getModel().getRuntime().sendHalt("Error accessing datastore \"" +
                    id + "\" from " + getInstanceName() + ": Start date of datastore (" +
                    store.getStartDate() + ") does not match given time interval (" +
                    timeinterval.getStart() + ")!");
            return;
        }

        if (store.getEndDate().before(timeinterval.getEnd()) && (store.getEndDate().compareTo(timeinterval.getEnd(), timeinterval.getTimeUnit()) != 0)) {
            getModel().getRuntime().sendHalt("Error accessing datastore \"" +
                    id + "\" from " + getInstanceName() + ": End date of datastore (" +
                    store.getEndDate() + ") does not match given time interval (" +
                    timeinterval.getEnd() + ")!");
            return;
        }

        // check if we need to shift forward
        if (store.getStartDate().before(timeinterval.getStart()) && (store.getStartDate().compareTo(timeinterval.getStart(), timeinterval.getTimeUnit()) != 0)) {

            DataSet ds = store.getNext();
            JAMSCalendar current = store.getStartDate().clone();
            JAMSCalendar targetDate = timeinterval.getStart().clone();
            int timeUnit = timeinterval.getTimeUnit();
            int timeUnitCount = timeinterval.getTimeUnitCount();

            // check if we can calculate offset correctly
            // this can be done of the step size can be directly calculated from
            // milliseconds representation, i.e. for weekly steps and below
            if (timeUnit >= JAMSCalendar.WEEK_OF_YEAR) {
                long diff = (long) targetDate.getTimeInMillis() / 1000 - current.getTimeInMillis() / 1000;
                int steps;
                switch (timeUnit) {
                    case JAMSCalendar.DAY_OF_YEAR:
                        steps = (int) diff / 3600 / 24;
                        break;
                    case JAMSCalendar.HOUR_OF_DAY:
                        steps = (int) diff / 3600;
                        break;
                    case JAMSCalendar.WEEK_OF_YEAR:
                        steps = (int) diff / 3600 / 24 / 7;
                        break;
                    case JAMSCalendar.MINUTE:
                        steps = (int) diff / 60;
                        break;
                    default:
                        steps = (int) diff;
                }
                steps = (int) steps / timeUnitCount;

                // just call getNext() step times
                for (int i = 0; i < steps; i++) {
                    store.getNext();
                }

            } else {

                // here we need to walk through time with a calendar object
                // this costs more runtime, but works for monthly and yearly 
                // steps as well
                targetDate.add(timeUnit, -1 * timeUnitCount);
                while (current.compareTo(targetDate, timeUnit) < 0) {
                    store.getNext();
                    current.add(timeUnit, timeUnitCount);
                }
            }
        }

        getModel().getRuntime().println("Datastore " + id + " initialized!", JAMS.VVERBOSE);
        doubles = new double[store.getDataSetDefinition().getColumnCount()];
        doubleValues.setValue(doubles);
    }

    @Override
    public void run() {
        DataSet ds = store.getNext();
        DataValue[] data = ds.getData();
        for (int i = 1; i < data.length; i++) {
            doubles[i - 1] = data[i].getDouble();
        }
    }

    @Override
    public void cleanup() {
        store.close();
    }
}
