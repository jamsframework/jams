/*
 * GenericTSReader.java
 * Created on 25. September 2012, 16:28
 *
 * This file is a JAMS component
 * Copyright (C) FSU Jena
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
@JAMSComponentDescription(title = "GenericTSReader",
author = "Sven Kralisch",
date = "2012-09-26",
version = "1.0_0",
description = "This component can be used obtain data from a time series "
+ "data store which contains only double values. Metadata are provided as a "
+ "sorted list of double values for each column.")
public class GenericTSReader extends JAMSComponent {

    /*
     *  Component variables
     */
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
    description = "Datastore ID")
    public Attribute.String id;
    
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
    description = "Array of column indices to be accessed [0..length-1]")
    public Attribute.IntegerArray columnIDs;

    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
    description = "The time interval within which the component shall read "
    + "data from the datastore")
    public Attribute.TimeInterval timeInterval;
    
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.WRITE,
    description = "Datastore metadata attribute names")
    public Attribute.StringArray metadataAttributes;

    @JAMSVarDescription(access = JAMSVarDescription.AccessType.WRITE,
    description = "Datastore metadata values - one DoubleArray for each column "
            + "indicated by columnIDs")
    public Attribute.StringArray[] metadataValues;

    @JAMSVarDescription(access = JAMSVarDescription.AccessType.WRITE,
    description = "Datastore data - one Double for each column indicated by "
            + "columnIDs")
    public Attribute.Double[] dataValues;
    
    private TSDataStore store;
    boolean shifted = false;

    @Override
    public void init() {
        shifted = false;
        InputDataStore is = null;
        if (id != null) {
            is = getModel().getWorkspace().getInputDataStore(id.getValue());
        }

        // check if store exists
        if (is == null) {
            getModel().getRuntime().sendHalt("Error accessing datastore \""
                    + id + "\" from " + getInstanceName() + ": Datastore could not be found!");
            return;
        }

        // check if this is a TSDataStore
        if (!(is instanceof TSDataStore)) {
            getModel().getRuntime().sendHalt("Error accessing datastore \""
                    + id + "\" from " + getInstanceName() + ": Datastore is not a time series datastore!");
            return;
        }

        store = (TSDataStore) is;

        // check if the store's time interval matches the provided time interval
        if (store.getStartDate().after(timeInterval.getStart()) && (store.getStartDate().compareTo(timeInterval.getStart(), timeInterval.getTimeUnit()) != 0)) {
            getModel().getRuntime().sendHalt("Error accessing datastore \""
                    + id + "\" from " + getInstanceName() + ": Start date of datastore ("
                    + store.getStartDate() + ") does not match given time interval ("
                    + timeInterval.getStart() + ")!");
            return;
        }

        if (store.getEndDate().before(timeInterval.getEnd()) && (store.getEndDate().compareTo(timeInterval.getEnd(), timeInterval.getTimeUnit()) != 0)) {
            getModel().getRuntime().sendHalt("Error accessing datastore \""
                    + id + "\" from " + getInstanceName() + ": End date of datastore ("
                    + store.getEndDate() + ") does not match given time interval ("
                    + timeInterval.getEnd() + ")!");
            return;
        }

        // extract some meta information
        DataSetDefinition dsDef = store.getDataSetDefinition();
        
        String[] metadataAttributesArray = dsDef.getAttributeNames().toArray(new String[dsDef.getAttributeNames().size()]);
        metadataAttributes.setValue(metadataAttributesArray);
        
        if (metadataValues.length != columnIDs.getValue().length) {
            getModel().getRuntime().sendErrorMsg("Mismatch of indicated columns and number of metadata "
                    + "attributes (metadataValues)! Reading of metadata will be skipped.");
        } else {        
            int i = 0;
            for (int colID : columnIDs.getValue()) {
                String[] metadataValuesArray = new String[metadataAttributesArray.length];
                ArrayList l = dsDef.getAttributeValues(colID);
                int j = 0;
                for (Object o : l) {
                    metadataValuesArray[j++] = o.toString();
                }
                metadataValues[i++].setValue(metadataValuesArray);
            }
        }
        
        if (dataValues.length != columnIDs.getValue().length) {
            getModel().getRuntime().sendHalt("Mismatch of indicated columns and number of data "
                    + "attributes! Will stop model execution.");
        }
        
        getModel().getRuntime().println("Datastore " + id + " initialized!", JAMS.VVERBOSE);
    }

    private void checkConsistency() {
        // check if we need to shift forward
        if (!shifted && store.getStartDate().before(timeInterval.getStart()) && (store.getStartDate().compareTo(timeInterval.getStart(), timeInterval.getTimeUnit()) != 0)) {
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
            if (timeUnit >= Attribute.Calendar.WEEK_OF_YEAR) {
                long diff = (targetDate.getTimeInMillis() - current.getTimeInMillis()) / 1000;
                int steps;
                switch (timeUnit) {
                    case Attribute.Calendar.DAY_OF_YEAR:
                        steps = (int) diff / 3600 / 24;
                        break;
                    case Attribute.Calendar.HOUR_OF_DAY:
                        steps = (int) diff / 3600;
                        break;
                    case Attribute.Calendar.WEEK_OF_YEAR:
                        steps = (int) diff / 3600 / 24 / 7;
                        break;
                    case Attribute.Calendar.MINUTE:
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

        int i = 0;
        for (int colID : columnIDs.getValue()) {
            // skip the first column
            dataValues[i++].setValue(data[colID+1].getDouble());
        }    

    }

    @Override
    public void cleanup() {
        store.close();
    }
}
