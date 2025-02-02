/*
 * TSDataStoreReader.java
 * Created on 16. Oktober 2008, 17:34
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
import java.time.LocalDate;
import java.time.Year;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;

/**
 *
 * @author Sven Kralisch <sven.kralisch at uni-jena.de>
 */
@JAMSComponentDescription(title = "TSDataStoreReader",
        author = "Sven Kralisch",
        date = "2025-02-01",
        version = "1.3",
        description = "This component can be used to obtain data from a time series "
        + "data store which contains only double values and a number of "
        + "station-specific metadata. Additional functions:\n"
        + "- automated time shift if start date of datastore is before start date of model\n"
        + "- automated aggregation if time steps of data store and model differ")
@VersionComments(entries = {
    @VersionComments.Entry(version = "1.0_0", date = "2008-10-20", comment = "Initial version"),
    @VersionComments.Entry(version = "1.0_1", date = "2013-06-17", comment = "Cache functions removed, minor bug fixes"),
    @VersionComments.Entry(version = "1.1", date = "2014-02-16", comment = "- Aggregation functions if time steps of data store and model differ\n"
            + "- Fixed wrong time shift in case of monthly data\n"),
    @VersionComments.Entry(version = "1.1_1", date = "2014-05-14", comment = "Fixed bug that caused wrong forward skipping "
            + "if time offset was very long (> 68 years of daily data)"),
    @VersionComments.Entry(version = "1.2", date = "2014-06-20", comment = "Added attributes to output"
            + " column names and columns IDs for further use"),
    @VersionComments.Entry(version = "1.3", date = "2025-02-01", comment = "Added option to switch between three calendar types "
            + "(Gregorian, Gregorian w/o leap days, 360-days). Works only for daily time steps.")
})
public class TSDataStoreReader extends JAMSComponent {

    /*
     *  Component variables
     */
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
            description = "Datastore ID")
    public Attribute.String id;

    @JAMSVarDescription(access = JAMSVarDescription.AccessType.WRITE,
            description = "Descriptive name of the dataset (equals datastore ID)")
    public Attribute.String dataSetName;

    @JAMSVarDescription(access = JAMSVarDescription.AccessType.WRITE,
            description = "Array of double values received from the datastore. Order "
            + "according to datastore")
    public Attribute.DoubleArray dataArray;

    @JAMSVarDescription(access = JAMSVarDescription.AccessType.WRITE,
            description = "Array of column names")
    public Attribute.StringArray name;

    @JAMSVarDescription(access = JAMSVarDescription.AccessType.WRITE,
            description = "Array of column IDs")
    public Attribute.StringArray columnID;

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
            description = "The time interval within which the component shall read "
            + "data from the datastore")
    public Attribute.TimeInterval timeInterval;

    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
            description = "Aggregate multiple datastore entries to averages or sums?",
            defaultValue = "true")
    public Attribute.Boolean calcAvg;

    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
            description = "The current model time - needed in case of aggregation over "
            + "irregular time steps (e.g. months). Aggregation is disabled if "
            + "this value is not set.")
    public Attribute.Calendar time;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "Lines skipped to reach timeInterval start"
    )
    public Attribute.Integer skipLines;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "Type of calendar:\n"
            + "0 - standard calendar\n"
            + "1 - 360-day calendar\n"
            + "2 - standard calendar w/o leap years",
            defaultValue = "0"
    )
    public Attribute.Integer calendarType;

    private TSDataStore store;
    private double[] doubles;
    private double[] elevationArray;
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

        Attribute.Calendar storeEnd = store.getEndDate();
        if ((calendarType.getValue() == 1) && (storeEnd.get(Attribute.Calendar.DAY_OF_MONTH) == 30)) {
            storeEnd.add(Attribute.Calendar.DAY_OF_YEAR, 1);
        }
        if (storeEnd.before(timeInterval.getEnd()) && (store.getEndDate().compareTo(timeInterval.getEnd(), timeInterval.getTimeUnit()) != 0)) {
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
        xCoord.setValue(listToDoubleArray(dsDef.getAttributeValues("X")));
        yCoord.setValue(listToDoubleArray(dsDef.getAttributeValues("Y")));
        elevation.setValue(listToDoubleArray(dsDef.getAttributeValues("ELEVATION")));
        name.setValue(listToStringArray(dsDef.getAttributeValues("NAME")));
        columnID.setValue(listToStringArray(dsDef.getAttributeValues("ID")));
        elevationArray = elevation.getValue();
        dataSetName.setValue(id.getValue());

        getModel().getRuntime().println("Datastore " + id + " initialized!", JAMS.VVERBOSE);
        doubles = new double[store.getDataSetDefinition().getColumnCount()];
        dataArray.setValue(doubles);
    }

    private double[] listToDoubleArray(ArrayList<Object> list) {
        if (list == null) {
            return null;
        }
        double[] result = new double[list.size()];
        int i = 0;
        for (Object o : list) {
            result[i] = (Double) o;
            i++;
        }
        return result;
    }

    private String[] listToStringArray(ArrayList<Object> list) {
        if (list == null) {
            return null;
        }
        String[] result = new String[list.size()];
        int i = 0;
        for (Object o : list) {
            result[i] = o.toString();
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
        int leapOffset = 0;
        
        if (calendarType.getValue() == 2) {
            leapOffset = countLeapDays(storeDate, targetDate);
        }

        if (skipLines == null) {

            storeDate.removeUnsignificantComponents(storeUnit);
            targetDate.removeUnsignificantComponents(targetUnit);

            int offset = storeDate.compareTo(targetDate, targetUnit);

            if (offset > 0) {

                getModel().getRuntime().sendHalt("Time series data read by " + this.getInstanceName() + " start after model start time!"
                        + "\n(" + store.getStartDate() + " vs " + timeInterval.getStart() + ")");

            } else if (offset < 0) {

                int steps;
                
                if ((calendarType.getValue() == 0) || (calendarType.getValue() == 2)) {

                    // check if we can calculate offset directly
                    // this can be done if the step size can be calculated directly from
                    // milliseconds representation, i.e. for weekly time steps and below
                    // else we calculate offset by iterating in time (less efficient)
                    long diff = (targetDate.getTimeInMillis() - storeDate.getTimeInMillis()) / 1000;

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
                    
                    steps -= leapOffset;

                } else {

                    // create two 360 days calendars for store date and time interval start and calculate the offset in days
                    CustomCalendar360 targetDate360 = new CustomCalendar360(targetDate.get(Attribute.Calendar.YEAR), targetDate.get(Attribute.Calendar.MONTH) + 1, targetDate.get(Attribute.Calendar.DAY_OF_MONTH));
                    CustomCalendar360 storeDate360 = new CustomCalendar360(storeDate.get(Attribute.Calendar.YEAR), storeDate.get(Attribute.Calendar.MONTH) + 1, storeDate.get(Attribute.Calendar.DAY_OF_MONTH));
                    steps = (int) targetDate360.calculateOffset(storeDate360);
                    if (steps < 0) {
                        getModel().getRuntime().sendHalt("Time series data read by " + this.getInstanceName() + " start after model start time!"
                                + "\n(" + store.getStartDate() + " vs " + timeInterval.getStart() + ")");
                    }
                }

                // skip forward datastore to required start time
                store.skip(steps);

            }

        } else {
            store.skip(skipLines.getValue());
        }

        // check if we have different step size in store and model
        if (storeUnit != targetUnit || storeUnitCount != targetUnitCount) {

            if (time == null) {
                getModel().getRuntime().sendHalt("Time steps in datastore " + store.getID() + " and model are different while time is not set!"
                        + " Please set the time atrtibute or adapt your datastore");
            }

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

        if (tsRatio == 1) {

            if (calendarType.getValue() == 0) {
                DefaultDataSet ds = store.getNext();
                if (ds == null) {
                    getModel().getRuntime().sendHalt("Empty dataset found in "
                            + "component " + this.getInstanceName() + " (" + time + ")");
                }

                DataValue[] data = ds.getData();
                for (int i = 1; i < data.length; i++) {
                    doubles[i - 1] = data[i].getDouble();
                }

            } else if (calendarType.getValue() == 1) {

                int year = time.get(Attribute.Calendar.YEAR);
                boolean isLeap = Year.of(year).isLeap();
                int day = time.get(Attribute.Calendar.DAY_OF_YEAR);

                if (isLeap) {
                    day--;
                }

                // if this is one of the following days, just reuse the values from last time step
                if (!((isLeap && day == 59) || day == 151 || day == 212 || day == 243 || day == 304 || day == 365)) {
                    DefaultDataSet ds = store.getNext();
                    if (ds == null) {
                        getModel().getRuntime().sendHalt("Empty dataset found in "
                                + "component " + this.getInstanceName() + " (" + time + ")");
                    }

                    DataValue[] data = ds.getData();
                    for (int i = 1; i < data.length; i++) {
                        doubles[i - 1] = data[i].getDouble();
                    }
                }
//                System.out.println(time.toString() + " " + day + " - " + doubles[0]); 
            } else {

                int year = time.get(Attribute.Calendar.YEAR);
                boolean isLeap = Year.of(year).isLeap();
                int day = time.get(Attribute.Calendar.DAY_OF_YEAR);
                
                if (!(isLeap && day == 60)) {
                    DefaultDataSet ds = store.getNext();
                    if (ds == null) {
                        getModel().getRuntime().sendHalt("Empty dataset found in "
                                + "component " + this.getInstanceName() + " (" + time + ")");
                    }

                    DataValue[] data = ds.getData();
                    for (int i = 1; i < data.length; i++) {
                        doubles[i - 1] = data[i].getDouble();
                    }
                }
            }

            dataArray.setValue(doubles);
            regCoeff.setValue(Regression.calcLinReg(elevationArray, doubles));

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
            regCoeff.setValue(Regression.calcLinReg(elevationArray, doubles));

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
    
    public static int countLeapDays(Attribute.Calendar start, Attribute.Calendar end) {
        int count = 0;

        // Normalize the start and end dates to the same day of the year
        int startYear = start.get(Calendar.YEAR);
        int endYear = end.get(Calendar.YEAR);

        for (int year = startYear; year <= endYear; year++) {
            if (Year.of(year).isLeap()) {
                // Check if February 29 is within the range
                GregorianCalendar leapDay = new GregorianCalendar(year, Calendar.FEBRUARY, 29);
                if (!leapDay.before(start) && !leapDay.after(end)) {
                    count++;
                }
            }
        }
        return count;
    }    

    public class CustomCalendar360 {

        private static final int DAYS_PER_MONTH = 30;
        private static final int MONTHS_PER_YEAR = 12;
        private static final int DAYS_PER_YEAR = DAYS_PER_MONTH * MONTHS_PER_YEAR;
        private static final int BASE_YEAR = 1970;
        private final LocalDate CUSTOM_EPOCH = LocalDate.of(BASE_YEAR, 1, 1); // Reference date

        private int year;
        private int month;
        private int day;

        public CustomCalendar360(int year, int month, int day) {
            if (month < 1 || month > MONTHS_PER_YEAR || day < 1 || day > DAYS_PER_MONTH) {
                throw new IllegalArgumentException("Invalid date in custom calendar");
            }
            this.year = year;
            this.month = month;
            this.day = day;
        }

        public CustomCalendar360 fromGregorian(LocalDate date) {
            long daysSinceEpoch = date.toEpochDay() - CUSTOM_EPOCH.toEpochDay(); // Days since the custom epoch
            int customYear = (int) (daysSinceEpoch / DAYS_PER_YEAR) + BASE_YEAR;
            int remainingDays = (int) (daysSinceEpoch % DAYS_PER_YEAR);
            if (remainingDays < 0) {
                customYear--;
                remainingDays += DAYS_PER_YEAR;
            }
            int customMonth = remainingDays / DAYS_PER_MONTH + 1;
            int customDay = remainingDays % DAYS_PER_MONTH + 1;

            return new CustomCalendar360(customYear, customMonth, customDay);
        }

        public LocalDate toGregorian() {
            long totalDays = (long) (year - BASE_YEAR) * DAYS_PER_YEAR + (month - 1) * DAYS_PER_MONTH + (day - 1);
            return CUSTOM_EPOCH.plusDays(totalDays);
        }

        public void addDays(int days) {
            long totalDays = (long) (year - BASE_YEAR) * DAYS_PER_YEAR + (month - 1) * DAYS_PER_MONTH + (day - 1) + days;
            year = (int) (totalDays / DAYS_PER_YEAR) + BASE_YEAR;
            int remainingDays = (int) (totalDays % DAYS_PER_YEAR);
            if (remainingDays < 0) {
                year--;
                remainingDays += DAYS_PER_YEAR;
            }
            month = remainingDays / DAYS_PER_MONTH + 1;
            day = remainingDays % DAYS_PER_MONTH + 1;
        }

        public long getTimeInMillis() {
            // Convert the custom calendar date to a Gregorian date
            LocalDate gregorianDate = toGregorian();

            // Convert the Gregorian date to milliseconds since epoch
            return gregorianDate.atStartOfDay().toInstant(ZoneOffset.UTC).toEpochMilli();
        }

        public long calculateOffset(CustomCalendar360 other) {
            // Calculate the total days for both instances
            long totalDaysThis = (long) (this.year - BASE_YEAR) * DAYS_PER_YEAR
                    + (this.month - 1) * DAYS_PER_MONTH
                    + (this.day - 1);

            long totalDaysOther = (long) (other.year - BASE_YEAR) * DAYS_PER_YEAR
                    + (other.month - 1) * DAYS_PER_MONTH
                    + (other.day - 1);

            // Return the difference
            return totalDaysThis - totalDaysOther;
        }

        public String toCustomDateString() {
            return String.format("%04d-%02d-%02d", year, month, day);
        }
    }

}
