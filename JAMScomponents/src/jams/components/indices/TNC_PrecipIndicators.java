/*
 * SPI_Calc.java
 * Created on 06.03.2019, 11:50:58
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
package jams.components.indices;

import jams.data.*;
import jams.model.*;
import jams.workspace.DataValue;
import jams.workspace.DefaultDataSet;
import jams.workspace.stores.InputDataStore;
import jams.workspace.stores.TSDataStore;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.json.JSONObject;

/**
 *
 * @author Sven Kralisch <sven.kralisch@uni-jena.de>
 *
 * based on SPI Generator v1.7.5 National Drought Mitigation Center - UNL
 * 11/29/2018
 *
 */
@JAMSComponentDescription(
        title = "TNC_Precip_Indicators",
        author = "Sven Kralisch",
        description = "Calculates various time series indicators such as:"
        + "- Standard Precipitation Index (SPI)"
        + "- accumulated annual rainfall"
        + "- total number of days with/without rain in a hydrological year"
        + "- number of consecutive days with/without rain"
        + "- number of days with extreme precipitation (e.g.  >100 mm/day)",
        date = "2019-03-06",
        version = "1.0_0"
)
@VersionComments(entries = {
    @VersionComments.Entry(version = "1.0_0", comment = "Initial version")
})
public class TNC_PrecipIndicators extends JAMSComponent {

    /*
     *  Component attributes
     */
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
            description = "Datastore ID")
    public Attribute.String id;

    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
            description = "DAY/MONTH indicating start of hydrological year",
            defaultValue = "01/11")
    public Attribute.String hydroYearStart;

    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
            description = "Treshold for extreme precip",
            defaultValue = "100")
    public Attribute.Double extremePrecip;

    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
            description = "Name of JSON output file (leave empty to diable)")
    public Attribute.String jsonFileName;

    /*
     *  Component run stages
     */
    @Override
    public void init() {
    }

    @Override
    public void run() {

        InputDataStore is = getModel().getWorkspace().getInputDataStore(id.getValue());
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

        TSDataStore store = (TSDataStore) is;
        Attribute.Calendar storeDate = store.getStartDate().clone();
        int storeUnit = store.getTimeUnit();
        int storeUnitCount = store.getTimeUnitCount();

        List<Double>[] values = new List[store.getDataSetDefinition().getColumnCount()];
        for (int i = 0; i < values.length; i++) {
            values[i] = new ArrayList();
        }

        List<String> days = new ArrayList();
        List<String> years = new ArrayList();
        List<Attribute.Calendar> dates = new ArrayList();

        DefaultDataSet ds;
        while ((ds = store.getNext()) != null) {
            dates.add(storeDate.clone());
            days.add(storeDate.toString());
            DataValue[] data = ds.getData();
            for (int i = 1; i < data.length; i++) {
                values[i - 1].add(data[i].getDouble());
            }
            storeDate.add(storeUnit, storeUnitCount);
        }

        Attribute.Calendar lastPlusOne = storeDate;

        // calculate date of the first hydrol. year start
        String[] s = hydroYearStart.getValue().split("/");
        Attribute.Calendar hStart = dates.get(0).clone();
        hStart.set(Attribute.Calendar.DAY_OF_MONTH, Integer.parseInt(s[0]));
        hStart.set(Attribute.Calendar.MONTH, Integer.parseInt(s[1]) - 1);
        if (hStart.before(dates.get(0))) {
            hStart.add(Attribute.Calendar.YEAR, 1);
        }

        // create JSON object and fill it with some data
        JSONObject json = new JSONObject();
        json.put("days", days);
        json.put("hYStart", hydroYearStart.getValue());
        json.put("nColumns", values.length);
        JSONObject jsonColumn = new JSONObject();
        json.put("columns", jsonColumn);

        double[] array;
        // iterate over timeseries
        for (int i = 0; i < values.length; i++) {
            // create new single timeserie
            array = new double[values[i].size()];
            for (int j = 0; j < values[i].size(); j++) {
                array[j] = values[i].get(j);
            }

            JSONObject colStats = new JSONObject();
            jsonColumn.put(Integer.toString(i), colStats);
            JSONObject jsonPprecipSum = new JSONObject();
            colStats.put("precipSum", jsonPprecipSum);
            JSONObject jsonDryDays = new JSONObject();
            colStats.put("dryDays", jsonDryDays);
            JSONObject jsonWetDays = new JSONObject();
            colStats.put("wetDays", jsonWetDays);
            JSONObject jsonExtremeDays = new JSONObject();
            colStats.put("extremeDays", jsonExtremeDays);
            JSONObject jsonSPI = new JSONObject();
            colStats.put("spi", jsonSPI);
            JSONObject jsonSPI1 = new JSONObject();
            jsonSPI.put("m1", jsonSPI1);
            JSONObject jsonSPI12 = new JSONObject();
            jsonSPI.put("m12", jsonSPI12);
            JSONObject jsonSPI24 = new JSONObject();
            jsonSPI.put("m24", jsonSPI24);
            JSONObject jsonSPI48 = new JSONObject();
            jsonSPI.put("m48", jsonSPI48);

            // calculate index values
            // SPI
            double[] spi = StandardPrecipitationIndex.parse(array);

            // annual stats
            double sum = 0, count = 0;
            int year = -1, wetCount = 0, dryCount = 0, consWetCount = 0, consDryCount = 0, extremeCount = 0;
            boolean isWet = false;//(array[0] > 0);
            List<Double> sumValues = new ArrayList();
            List<Integer> consDryValues = new ArrayList();
            List<Integer> consWetValues = new ArrayList();

            for (int j = 0; j < values[i].size(); j++) {

                // get current date & value
                double d = array[j];

                // reset if new hydrol. year
                if (dates.get(j).compareTo(hStart, Attribute.Calendar.DAY_OF_MONTH) == 0) {

                    // set next start of hydrol. year
                    hStart.add(Attribute.Calendar.YEAR, 1);

                    // store stats of old year
                    if (year > 0) {
                        // collect complete hydrological years (only for first column)
                        if (i == 0) {
                            years.add(Integer.toString(year));
                        }
                        jsonPprecipSum.put(Integer.toString(year), sumValues);
                        jsonDryDays.put(Integer.toString(year), dryCount);
                        jsonWetDays.put(Integer.toString(year), wetCount);
                        jsonExtremeDays.put(Integer.toString(year), extremeCount);
                    }

                    // reset stats & start new year
                    sum = 0;
                    count = 0;
                    wetCount = 0;
                    dryCount = 0;
                    extremeCount = 0;
                    sumValues.clear();
                    year = dates.get(j).get(Attribute.Calendar.YEAR);
                }

                // accumulate 
                count++;
                sum += d;
                sumValues.add(sum);

                // check if there is a switch
                if (isWet && !(d > 0)) {
                    consWetCount = 0;
                    isWet = false;
                } else if (!isWet && (d > 0)) {
                    consDryCount = 0;
                    isWet = true;
                }

                // count wet/dry days and periods
                if (d > 0) {
                    wetCount++;
                    consWetCount++;
                } else {
                    dryCount++;
                    consDryCount++;
                }

                consDryValues.add(consDryCount);
                consWetValues.add(consWetCount);

                // count extreme precip
                if (d >= extremePrecip.getValue()) {
                    extremeCount++;
                }

            }

            // clean up 
            if (lastPlusOne.compareTo(hStart, Attribute.Calendar.DAY_OF_MONTH) == 0) {
                // store stats of old year
                if (year > 0) {
                    // collect complete hydrological years (only for first column)
                    if (i == 0) {
                        years.add(Integer.toString(year));
                    }
                    jsonPprecipSum.put(Integer.toString(year), sumValues);
                    jsonDryDays.put(Integer.toString(year), dryCount);
                    jsonWetDays.put(Integer.toString(year), wetCount);
                    jsonExtremeDays.put(Integer.toString(year), extremeCount);
                }
            }

            colStats.put("consDryDays", consDryValues);
            colStats.put("consWetDays", consWetValues);

        }

        json.put("hYears", years);

        if (jsonFileName != null) {
            try {
                FileWriter writer = new FileWriter(new File(getModel().getWorkspace().getOutputDataDirectory(), jsonFileName.getValue()));
                writer.write(json.toString());
            } catch (IOException ex) {
                Logger.getLogger(TNC_PrecipIndicators.class.getName()).log(Level.SEVERE, null, ex);
            }
        } else {
            System.out.println(json.toString(4));
        }
    }

    @Override
    public void cleanup() {
    }

    public static void main(String[] args) {
        String jsonString = new JSONObject()
                .put("JSON1", "Hello World!")
                .put("JSON2", "Hello my World!")
                .put("JSON3", new JSONObject()
                        .put("key1", "value1")).toString();

        JSONObject json = new JSONObject();
        List<String> dates = new ArrayList();
        dates.add("1.1.2019");
        dates.add("2.1.2019");
        dates.add("3.1.2019");
        dates.add("4.1.2019");
        json.put("Dates", dates);
        double[] d = {1, 2, 3, 4};
        List<Double>[] values = new List[2];
        values[0] = new ArrayList();
        values[1] = new ArrayList();
        values[0].add(1d);
        values[0].add(2d);
        values[0].add(3d);
        values[1].add(10d);
        values[1].add(20d);
        values[1].add(30d);
        json.put("Values", values);

        System.out.println(json);
    }

}
