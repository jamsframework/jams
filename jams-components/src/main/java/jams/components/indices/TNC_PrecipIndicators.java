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

import jams.JAMS;
import jams.components.aggregate.TSAggregator;
import jams.data.*;
import jams.model.*;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
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
public class TNC_PrecipIndicators extends TimeSeriesIndicators {

    /*
     *  Component attributes
     */
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
    public void run() {

        readTSData();

        List<String> years = new ArrayList();

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
        json.put("dates", dateStrings);
        json.put("hYStart", hydroYearStart.getValue());
        json.put("extremeThreshold", extremePrecip.getValue());
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

//                // if the value is missing, we use the current average value
//                if (array[j] == JAMS.getMissingDataValue()) {
//                    array[j] = 0;
//                }
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
            JSONObject jsonMissingDays = new JSONObject();
            colStats.put("missingTimeSteps", jsonMissingDays);
            jsonSPI.put("missingDataValue", StandardPrecipitationIndex.MISSING_DATA_VALUE);

            // calculate index values
            // SPI
            // calculate monthly aggregates
            TSAggregator aggr = new TSAggregator(array, dates, 0);
            TSAggregator.Aggregate aggrResult = aggr.toMonthly();
            double[] a = aggrResult.values;

            // output time steps of aggregates
            String[] dateArray = new String[aggrResult.dates.size()];
            for (int j = 0; j < aggrResult.dates.size(); j++) {
                dateArray[j] = aggrResult.dates.get(j).toString();
            }
            jsonSPI.put("dates", dateArray);

            // calc SPI for different moving means (1, 3, 12, 24, 48 months)
            double[] spi;

            spi = StandardPrecipitationIndex.calcSPI(Arrays.copyOf(a, a.length));
            round(spi);
            jsonSPI.put("m1", spi);

            spi = StandardPrecipitationIndex.calcSPIn(Arrays.copyOf(a, a.length), 3);
            round(spi);
            jsonSPI.put("m3", spi);

            spi = StandardPrecipitationIndex.calcSPIn(Arrays.copyOf(a, a.length), 12);
            round(spi);
            jsonSPI.put("m12", spi);

            spi = StandardPrecipitationIndex.calcSPIn(Arrays.copyOf(a, a.length), 24);
            round(spi);
            jsonSPI.put("m24", spi);

            spi = StandardPrecipitationIndex.calcSPIn(Arrays.copyOf(a, a.length), 48);
            round(spi);
            jsonSPI.put("m48", spi);

            // annual stats
            double sum = 0, count = 0;
            int year = -1, wetCount = 0, dryCount = 0, consWetCount = 0, consDryCount = 0, extremeCount = 0, missingCount = 0;
            boolean isWet = false, inMissingTI = false;//(array[0] > 0);
            List<Double> sumValues = new ArrayList();
            List<Integer> consDryValues = new ArrayList();
            List<Integer> consWetValues = new ArrayList();
            List<String[]> missingTIs = new ArrayList();

            for (int j = 0; j < values[i].size(); j++) {

                // get current date & value
                double d = array[j];

                // reset if new hydrol. year
                if (dates.get(j).compareTo(hStart, Attribute.Calendar.MONTH) == 0) {

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
                        jsonMissingDays.put(Integer.toString(year), missingCount);
                    }

                    // reset stats & start new year
                    sum = 0;
                    count = 0;
                    wetCount = 0;
                    dryCount = 0;
                    missingCount = 0;
                    extremeCount = 0;
                    sumValues.clear();
                    year = dates.get(j).get(Attribute.Calendar.YEAR);
                }

                if (d == JAMS.getMissingDataValue()) {

                    if (!inMissingTI) {

                        // new gap
                        String[] gap = new String[2];
                        gap[0] = dates.get(j).toString();
                        missingTIs.add(gap);
                        inMissingTI = true;
                    }

                } else {

                    if (inMissingTI) {

                        // gap finished
                        missingTIs.get(missingTIs.size() - 1)[1] = dates.get(j - 1).toString();
                        inMissingTI = false;
                    }

                }

                if (!inMissingTI) {

                    // accumulate 
                    count++;
                    sum += d;

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

                    // count extreme precip
                    if (d >= extremePrecip.getValue()) {
                        extremeCount++;
                    }

                    sumValues.add(((double) Math.round(sum * 100)) / 100);

                } else {

                    missingCount++;
                    consWetCount = 0;
                    consDryCount = 0;
                    isWet = false;

                    sumValues.add(null);

                }

                consDryValues.add(consDryCount);
                consWetValues.add(consWetCount);

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
                    jsonMissingDays.put(Integer.toString(year), missingCount);

                    // close the last gap if timeseries ends with a gap
                    String[] lastMissing = missingTIs.get(missingTIs.size() - 1);
                    if (lastMissing[1].isEmpty()) {
                        lastMissing[1] = dates.get(dates.size() - 1).toString();
                    }
                }
            }

            colStats.put("consDryDays", consDryValues);
            colStats.put("consWetDays", consWetValues);
            colStats.put("missingIntervals", missingTIs);

        }

        json.put("hYears", years);

        if (jsonFileName != null) {
            try {
                FileWriter writer = new FileWriter(new File(getModel().getWorkspace().getOutputDataDirectory(), jsonFileName.getValue()));
                writer.write(json.toString());
                writer.flush();
                writer.close();
            } catch (IOException ex) {
                Logger.getLogger(TNC_PrecipIndicators.class.getName()).log(Level.SEVERE, null, ex);
            }
        } else {
            System.out.println(json.toString());
        }
    }

    private void round(double[] a) {
        for (int n = 0; n < a.length; n++) {
            a[n] = ((double) Math.round(a[n] * 100)) / 100;
        }
    }
}
