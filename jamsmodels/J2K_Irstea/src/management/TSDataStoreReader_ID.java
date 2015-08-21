/*
 * SewerOverflowDevice.java
 * Created on 05. October 2012, 17:02
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
package management;

import jams.JAMS;
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
 * @author Francois Tilmant
 */
@JAMSComponentDescription(title = "TSDataStoreReader_ID",
        author = "Francois Tilmant",
        description = "TSDataStoreReader with name of the stations in an Array",
        version = "1.0_0",
        date = "2014-06-03")
public class TSDataStoreReader_ID extends JAMSComponent {

    /*
     *  Component variables
     */
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
            description = "Datastore ID")
    public Attribute.String id;

    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
            description = "The time interval within which the component shall read "
            + "data from the datastore")
    public Attribute.TimeInterval timeInterval;

    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
            description = "Aggregate multiple datastore entries to averages or sums?",
            defaultValue = "true")
    public Attribute.Boolean calcAvg;

    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
            description = "The current model time - needed in case of aggregation over irregular time steps (e.g. months). "
                    + "Aggregation is disabled if this value is not set.")
    public Attribute.Calendar time;

    @JAMSVarDescription(access = JAMSVarDescription.AccessType.WRITE,
            description = "Descriptive name of the dataset (equals datastore ID)")
    public Attribute.String dataSetName;

    @JAMSVarDescription(access = JAMSVarDescription.AccessType.WRITE,
            description = "Array of double values received from the datastore. Order "
            + "according to datastore")
    public Attribute.DoubleArray dataArray;

    @JAMSVarDescription(access = JAMSVarDescription.AccessType.WRITE,
            description = "Array of station elevations")
    public Attribute.DoubleArray elevation;
    
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.WRITE,
            description = "Array of station names")
    public Attribute.DoubleArray names;

    @JAMSVarDescription(access = JAMSVarDescription.AccessType.WRITE,
            description = "Array of station's x coordinate")
    public Attribute.DoubleArray xCoord;

    @JAMSVarDescription(access = JAMSVarDescription.AccessType.WRITE,
            description = "Array of station's y coordinate")
    public Attribute.DoubleArray yCoord;

    @JAMSVarDescription(access = JAMSVarDescription.AccessType.WRITE,
            description = "Regression coefficients")
    public Attribute.DoubleArray regCoeff;

    private TSDataStore store;
    private double[] doubles;
    private double[] elevationArray;
    private double[] namesArray;
    boolean shifted = false;
    int tsRatio = 1;
    Attribute.Calendar storeDate;
    int storeUnit, storeUnitCount, targetUnit, targetUnitCount;

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
        if (dsDef.getAttributeValues("X") == null) {
            getModel().getRuntime().sendHalt("Error in data set definition \""
                    + id + "\" from " + getInstanceName() + ": x coordinate not specified");
        }
        if (dsDef.getAttributeValues("Y") == null) {
            getModel().getRuntime().sendHalt("Error in data set definition \""
                    + id + "\" from " + getInstanceName() + ": y coordinate not specified");
        }
        if (dsDef.getAttributeValues("ELEVATION") == null) {
            getModel().getRuntime().sendHalt("Error in data set definition \""
                    + id + "\" from " + getInstanceName() + ": elevation not specified");
        }
        if (dsDef.getAttributeValues("ID") == null) {
            getModel().getRuntime().sendHalt("Error in data set definition \""
                    + id + "\" from " + getInstanceName() + ": name not specified");
        }
        xCoord.setValue(listToDoubleArray(dsDef.getAttributeValues("X")));
        yCoord.setValue(listToDoubleArray(dsDef.getAttributeValues("Y")));
        elevation.setValue(listToDoubleArray(dsDef.getAttributeValues("ELEVATION")));
        elevationArray = elevation.getValue();
        names.setValue(listToDoubleArray(dsDef.getAttributeValues("X")));
        namesArray = names.getValue();
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

    private void checkConsistency() {

        // check if we need to shift forward
        Attribute.Calendar targetDate = timeInterval.getStart().clone();
        targetUnit = timeInterval.getTimeUnit();
        targetUnitCount = timeInterval.getTimeUnitCount();
        storeDate = store.getStartDate().clone();
        storeUnit = store.getTimeUnit();
        storeUnitCount = store.getTimeUnitCount();

        storeDate.removeUnsignificantComponents(storeUnit);
        targetDate.removeUnsignificantComponents(targetUnit);

        int offset = storeDate.compareTo(targetDate, targetUnit);

        if (offset > 0) {

            getModel().getRuntime().sendHalt("Time series data read by " + this.getInstanceName() + " start after model start time!"
                    + "\n(" + store.getStartDate() + " vs " + timeInterval.getStart() + ")");

        } else if (offset < 0) {

            // check if we can calculate offset directly
            // this can be done if the step size can be calculated directly from
            // milliseconds representation, i.e. for weekly time steps and below
            // else we calculate offset by iterating in time (less efficient)
            long diff = (targetDate.getTimeInMillis() - storeDate.getTimeInMillis()) / 1000;
            int steps;
            switch (storeUnit) {
                case Attribute.Calendar.DAY_OF_YEAR:
                    steps = (int) (diff / 3600 / 24 / storeUnitCount);
                    storeDate.add(storeUnit, storeUnitCount * steps);
                    break;
                case Attribute.Calendar.HOUR_OF_DAY:
                    steps = (int) (diff / 3600 / storeUnitCount);
                    storeDate.add(storeUnit, storeUnitCount * steps);
                    break;
                case Attribute.Calendar.WEEK_OF_YEAR:
                    steps = (int) (diff / 3600 / 24 / 7 / storeUnitCount);
                    storeDate.add(storeUnit, storeUnitCount * steps);
                    break;
                case Attribute.Calendar.MINUTE:
                    steps = (int) (diff / 60 / storeUnitCount);
                    storeDate.add(storeUnit, storeUnitCount * steps);
                    break;
                case Attribute.Calendar.SECOND:
                    steps = (int) (diff / storeUnitCount);
                    storeDate.add(storeUnit, storeUnitCount * steps);
                    break;
                default:
                    steps = iterateStoreDate(targetDate);
            }

            // skip forward datastore to required start time
            store.skip(steps);

        }

        // check if we have different step size in store and model
        if (storeUnit != targetUnit || storeUnitCount != targetUnitCount) {

            // if both units have a constant duration, calculate this duration and the related ratio
            if (storeUnit > Attribute.Calendar.MONTH && targetUnit > Attribute.Calendar.MONTH) {
                int storeMS = getMilliseconds(storeUnit);
                int targetMS = getMilliseconds(targetUnit);
                double dRatio = (double) (targetMS * targetUnitCount) / (storeMS * storeUnitCount);
                int ratio = (int) Math.floor(dRatio);
                if (ratio != dRatio) {
                    getModel().getRuntime().sendHalt("Time steps in datastore " + store.getID() + " and model are incompatible. "
                            + "Please adapt your datastore first!");
                }

                tsRatio = ratio;
            } else {
                tsRatio = -1;
            }

        }
    }

    private int getMilliseconds(int unit) {
        int ms = 0;
        switch (unit) {
            case Attribute.Calendar.DAY_OF_YEAR:
                ms = 1000 * 3600 * 24;
                break;
            case Attribute.Calendar.HOUR_OF_DAY:
                ms = 1000 * 3600;
                break;
            case Attribute.Calendar.WEEK_OF_YEAR:
                ms = 1000 * 3600 * 24 * 7;
                break;
            case Attribute.Calendar.MINUTE:
                ms = 1000 * 60;
                break;
            case Attribute.Calendar.SECOND:
                ms = 1000;
                break;
            case Attribute.Calendar.MILLISECOND:
                ms = 1;
                break;
            default:
                getModel().getRuntime().sendHalt("Cannot calculate constant time unit duration!");
        }
        return ms;
    }

    private int iterateStoreDate(Attribute.Calendar date) {
        int steps = 0;
        while (storeDate.compareTo(date, storeUnit) < 0) {
            storeDate.add(storeUnit, storeUnitCount);
            steps++;
        }
        return steps;
    }

    @Override
    public void initAll() {
        checkConsistency();
    }

    @Override
    public void run() {

        if (tsRatio == 1 || time == null) {

            DefaultDataSet ds = store.getNext();
            DataValue[] data = ds.getData();
            for (int i = 1; i < data.length; i++) {
                doubles[i - 1] = data[i].getDouble();
            }

            dataArray.setValue(doubles);


        } else {

            int n;

            // get the ratio (fixed or dynamic)
            if (tsRatio < 0) {
                Attribute.Calendar nextTime = time.clone();
                nextTime.add(targetUnit, targetUnitCount);
                n = iterateStoreDate(nextTime);
            } else {
                n = tsRatio;
            }

            // calc the aggregated values based on the ratio
            for (int i = 0; i < doubles.length; i++) {
                doubles[i] = 0;
            }

            for (int j = 0; j < n; j++) {
                DefaultDataSet ds = store.getNext();
                DataValue[] data = ds.getData();
                for (int i = 1; i < data.length; i++) {
                    doubles[i - 1] += data[i].getDouble();
                }
            }

            if (calcAvg.getValue()) {
                for (int i = 0; i < doubles.length; i++) {
                    doubles[i] /= n;
                }
            }

            dataArray.setValue(doubles);


            // create some output
//            String s = store.getID() + " ";
//            if (time != null) {
//                s += time + " ";
//            }
//            for (int i = 0; i < doubles.length; i++) {
//                s += doubles[i] + " ";
//            }
//            getModel().getRuntime().println(s, JAMS.VVERBOSE);

        }

    }

    @Override
    public void cleanup() {
        store.close();
    }
}
