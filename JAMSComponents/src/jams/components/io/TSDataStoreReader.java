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
import jams.components.efficiencies.Regression;
import jams.data.*;
import jams.model.*;
import jams.workspace.DataSetDefinition;
import jams.workspace.DataValue;
import jams.workspace.DefaultDataSet;
import jams.workspace.stores.InputDataStore;
import jams.workspace.stores.TSDataStore;
import java.util.ArrayList;

/**
 *
 * @author Sven Kralisch <sven.kralisch at uni-jena.de>
 */
@JAMSComponentDescription(title = "TSDataStoreReader",
author = "Sven Kralisch",
date = "2008-10-16",
version = "1.0_0",
description = "This component can be used obtain data from a time series " +
"data store which contains only double values.")
public class TSDataStoreReader extends JAMSComponent {

    /*
     *  Component variables
     */
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
    description = "Datastore ID")
    public Attribute.String id;

    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
    description = "The time interval within which the component shall read " +
    "data from the datastore")
    public Attribute.TimeInterval timeInterval;

    @JAMSVarDescription(access = JAMSVarDescription.AccessType.WRITE,
    description = "Descriptive name of the dataset (equals datastore ID)")
    public Attribute.String dataSetName;

    /*
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
    description = "The current time within the timeinterval")
    public JAMSCalendar time;
     */
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.WRITE,
    description = "Array of double values received from the datastore. Order " +
    "according to datastore")
    public Attribute.DoubleArray dataArray;

    @JAMSVarDescription(access = JAMSVarDescription.AccessType.WRITE,
    description = "Array of station elevations")
    public Attribute.DoubleArray elevation;

    @JAMSVarDescription(access = JAMSVarDescription.AccessType.WRITE,
    description = "Array of station's x coordinate")
    public Attribute.DoubleArray xCoord;

    @JAMSVarDescription(access = JAMSVarDescription.AccessType.WRITE,
    description = "Array of station's y coordinate")
    public Attribute.DoubleArray yCoord;

    @JAMSVarDescription(access = JAMSVarDescription.AccessType.WRITE,
    description = "Regression coefficients")
    public Attribute.DoubleArray regCoeff;

    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
    description = "Caching configuration: 0 - write cache, 1 - use cache, 2 - caching off",
    defaultValue = "0")
    public Attribute.Integer dataCaching;

    private TSDataStore store;

    private double[] doubles;

    private double[] elevationArray;

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
        if (store.getStartDate().after(timeInterval.getStart()) && (store.getStartDate().compareTo(timeInterval.getStart(), timeInterval.getTimeUnit()) != 0)) {
            getModel().getRuntime().sendHalt("Error accessing datastore \"" +
                    id + "\" from " + getInstanceName() + ": Start date of datastore (" +
                    store.getStartDate() + ") does not match given time interval (" +
                    timeInterval.getStart() + ")!");
            return;
        }

        if (store.getEndDate().before(timeInterval.getEnd()) && (store.getEndDate().compareTo(timeInterval.getEnd(), timeInterval.getTimeUnit()) != 0)) {
            getModel().getRuntime().sendHalt("Error accessing datastore \"" +
                    id + "\" from " + getInstanceName() + ": End date of datastore (" +
                    store.getEndDate() + ") does not match given time interval (" +
                    timeInterval.getEnd() + ")!");
            return;
        }

        // extract some meta information
        DataSetDefinition dsDef = store.getDataSetDefinition();
        if (dsDef.getAttributeValues("X")==null){
            getModel().getRuntime().sendHalt("Error in data set definition \"" +
                    id + "\" from " + getInstanceName() + ": x coordinate not specified");
        }
        if (dsDef.getAttributeValues("Y")==null){
            getModel().getRuntime().sendHalt("Error in data set definition \"" +
                    id + "\" from " + getInstanceName() + ": y coordinate not specified");
        }
        if (dsDef.getAttributeValues("ELEVATION")==null){
            getModel().getRuntime().sendHalt("Error in data set definition \"" +
                    id + "\" from " + getInstanceName() + ": elevation not specified");
        }
        xCoord.setValue(listToDoubleArray(dsDef.getAttributeValues("X")));
        yCoord.setValue(listToDoubleArray(dsDef.getAttributeValues("Y")));
        elevation.setValue(listToDoubleArray(dsDef.getAttributeValues("ELEVATION")));
        elevationArray = elevation.getValue();
        dataSetName.setValue(id.getValue());
        
        getModel().getRuntime().println("Datastore " + id + " initialized!", JAMS.VVERBOSE);
        doubles = new double[store.getDataSetDefinition().getColumnCount()];
        dataArray.setValue(doubles);
    }

    private double[] listToDoubleArray(ArrayList<Object> list) {
        double[] result = new double[list.size()];
        int i = 0;
        for (Object o : list) {
            result[i] = ((Double) o).doubleValue();
            i++;
        }
        return result;
    }

    boolean shifted = false;
    private void checkConsistency(){
        // check if we need to shift forward
        if (store.getStartDate().before(timeInterval.getStart()) && (store.getStartDate().compareTo(timeInterval.getStart(), timeInterval.getTimeUnit()) != 0) && !shifted) {
            shifted = true;
            Attribute.Calendar current = store.getStartDate().clone();
            Attribute.Calendar targetDate = timeInterval.getStart().clone();
            current.removeUnsignificantComponents(timeInterval.getTimeUnit());
            targetDate.removeUnsignificantComponents(timeInterval.getTimeUnit());
            int timeUnit = timeInterval.getTimeUnit();
            int timeUnitCount = timeInterval.getTimeUnitCount();

            // check if we can calculate offset
            // this can be done if the step size can be calculated directly from
            // milliseconds representation, i.e. for weekly steps and below
            // ps: this is evil :]
            if (timeUnit >= JAMSCalendar.WEEK_OF_YEAR) {
                long diff = (targetDate.getTimeInMillis() - current.getTimeInMillis()) / 1000;
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

                store.skip(steps);
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
    }

    @Override
    public void run() {
        checkConsistency();
        
        DefaultDataSet ds = store.getNext();
        DataValue[] data = ds.getData();
        for (int i = 1; i < data.length; i++) {
            doubles[i - 1] = data[i].getDouble();
        }

        dataArray.setValue(doubles);
        if (dataCaching.getValue() != 1) {
            regCoeff.setValue(Regression.calcLinReg(elevationArray, doubles));
        }
    }

    @Override
    public void cleanup() {
        store.close();
    }
}
