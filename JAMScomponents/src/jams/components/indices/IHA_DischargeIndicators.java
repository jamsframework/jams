/*
 * IHA_DischargeIndicators.java
 * Created on 16.05.2019, 13:36:43
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
import jams.data.*;
import jams.model.*;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.json.JSONObject;
import org.rosuda.JRI.REXP;
import org.rosuda.JRI.RVector;
import org.rosuda.JRI.Rengine;

/**
 *
 * @author Sven Kralisch <sven.kralisch@uni-jena.de>
 */
@JAMSComponentDescription(
        title = "IHA_DischargeIndicators",
        author = "Sven Kralisch",
        description = "Calculates various discharge indicators based on "
        + "the Indicators of Hydrological Alteration (IHA) approach. To "
        + "provide the required environment, run the following commands at "
        + "the R command line:"
        + "> install.packages(\"devtools\")\n"
        + "> library(devtools)\n"
        + "> install_github(\"mkoohafkan/flowregime\")",
        date = "2019-05-16",
        version = "1.0_0"
)
@VersionComments(entries = {
    @VersionComments.Entry(version = "1.0_0", comment = "Initial version")
})
public class IHA_DischargeIndicators extends TimeSeriesIndicators {

    /*
     *  Component attributes
     */
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
            description = "Name of JSON output file (leave empty to diable)")
    public Attribute.String jsonFileName;

    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
            description = "Use a local package dir? If yes, component will try"
            + " to load all packages from the temp dir of the workspave"
            + " and - if required - will try to download packages"
            + " automatically from the mirror defined by 'cranMirror'.",
            defaultValue = "false")
    public Attribute.Boolean localPackageDir;

    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
            description = "CRAN mirror to use for packages",
            defaultValue = "https://cran.uni-muenster.de")
    public Attribute.String cranMirror;

    private Rengine re;
    private final String[] efc_strings = {"extreme low flow", "low flow", "low flow pulse", "high flow pulse", "small flood", "large flood"};
    private boolean inMissingTI = false;

    /*
     *  Component run stages
     */
    @Override
    public void init() {

        // create / get the Rengine object
        re = (Rengine) JAMS.getObjectRepo().get("Rengine");
        if (re == null) {
            String args[] = {"--no-save"};
            re = new Rengine(args, false, null);
            re.waitForR();
            JAMS.getObjectRepo().put("Rengine", re);
        }

        // setup package dir
        if (localPackageDir.getValue()) {
            File libFolder = new File(getModel().getWorkspace().getTempDirectory(), "R");
            if (!libFolder.exists()) {
                if (!libFolder.mkdirs()) {
                    getModel().getRuntime().sendHalt("Could not create local R package repo in " + libFolder.getAbsolutePath());
                }
            }
            String libPath = libFolder.getAbsolutePath().replace("\\", "/");
            re.eval(".libPaths( c('" + libPath + "', .libPaths() ) )");            
        }

        // check whether the flowregime package is installed
        REXP x = re.eval("library(flowregime)");
        // if package is missing, try to install it
        if (x == null) {
            if (localPackageDir.getValue()) {
                re.eval("install.packages('devtools', repos='" + cranMirror.getValue() + "')");
                re.eval("library(devtools)");
                re.eval("install_github('mkoohafkan/flowregime')");
                x = re.eval("library(flowregime)");
                if (x == null) {
                    getModel().getRuntime().sendHalt("Could not install required R packages");
                }
            } else {
                getModel().getRuntime().sendHalt("Could not load required package 'flowregime'");
            }
        }
    }

    @Override
    public void run() {

        // read the timeseries data
        readTSData();
        REXP x;

        // create JSPON doc
        JSONObject json = new JSONObject();
        json.put("nColumns", values.length);
        json.put("efcStateNames", efc_strings);
        json.put("dates", dateStrings);
        JSONObject jsonColumn = new JSONObject();
        json.put("columns", jsonColumn);

        // assign array of date strings to R
        String[] sArray = dateStrings.toArray(new String[dateStrings.size()]);
        re.assign("sarray", sArray);

        // iterate over all timeseries
        for (int c = 0; c < values.length; c++) {

            // create JSON element for current timeserie
            JSONObject colStats = new JSONObject();
            jsonColumn.put(Integer.toString(c), colStats);
            JSONObject jsonEFCThresholds = new JSONObject();
            colStats.put("efcThresholds", jsonEFCThresholds);
            JSONObject jsonEFCStates = new JSONObject();
            colStats.put("efcStates", jsonEFCStates);

            // create lists of data values and dates for non-null values
            List<Double> valueList = new ArrayList<>();
            List<String> dateList = new ArrayList<>();
            List<String[]> missingTIs = new ArrayList();
            for (int i = 0; i < values[c].size(); i++) {
                double d = values[c].get(i);

                // store all missing data time intervals
                if (d == JAMS.getMissingDataValue()) {
                    if (!inMissingTI) {
                        // new gap
                        String[] gap = new String[2];
                        gap[0] = sArray[i];
                        missingTIs.add(gap);
                        inMissingTI = true;
                    }
                } else {
                    if (inMissingTI) {
                        // gap finished
                        missingTIs.get(missingTIs.size() - 1)[1] = sArray[i - 1];
                        inMissingTI = false;
                    }
                }

                if (!inMissingTI) {
                    valueList.add(d);
                    dateList.add(sArray[i]);
                }

            }

            // close the last gap if timeseries ends with a gap
            String[] lastMissing = missingTIs.get(missingTIs.size() - 1);
            if (lastMissing[1].isEmpty()) {
                lastMissing[1] = sArray[sArray.length - 1];
            }

            // add list of gaps to JSON
            colStats.put("missingIntervals", missingTIs);

            // convert value/date lists to arrays
            double[] valueArray = new double[valueList.size()];
            for (int i = 0; i < valueList.size(); i++) {
                valueArray[i] = valueList.get(i);
            }
            String[] dateArray = new String[dateList.size()];
            for (int i = 0; i < dateList.size(); i++) {
                dateArray[i] = dateList.get(i);
            }

            // assign both arrays to R
            re.assign("varray", valueArray);
            re.assign("darray", dateArray);

            // create xts object in R
            re.eval("dates <- as.Date(darray)");
            re.eval("xts <- xts(x=varray, order.by=dates)");

            // calculate EFC thresholds in R and store them in JSON doc
            x = re.eval("build_EFC_thresholds(xts, method = \"advanced\")");
            RVector v = x.asVector();
            for (int i = 0; i < v.size(); i++) {
                jsonEFCThresholds.put(v.getNames().get(i).toString(), v.at(i).asDouble());

                // fetch high flow threshold and compute longest high flow duration
                if (v.getNames().get(i).toString().equals("high flow")) {
                    double highFlow = v.at(i).asDouble();
                    x = re.eval("longest_high_flow_duration(xts, " + highFlow + ")");
                    colStats.put("efcHighFlowDuration", x.asDouble());
                }
            }

            // analyse timeseries data
            re.eval("efcs <- EFC(xts, method = \"advanced\")");
//            String[] sa = x.asStringArray();
//            for (String s : sa) {
//                System.out.println(s);
//            }

//            re.eval("efc_strings<-c(\"extreme low flow\", \"low flow\", \"low flow pulse\", \"high flow pulse\", \"small flood\", \"large flood\")");
//            re.eval("indices=0:(length(efc_strings)-1)");
//            re.eval("names(indices)=efc_strings");
            // extract timeseries for each flow component
            x = re.eval("sort(unique(efcs))");
            String[] states = x.asStringArray();
            for (String state : states) {

                x = re.eval("efcs==\"" + state + "\"");
                int[] mask = x.asIntArray();

                List<Double> l = new ArrayList(values[c].size());
                int j = 0;

                for (int i = 0; i < values[c].size(); i++) {
                    if (values[c].get(i) == JAMS.getMissingDataValue()) {
                        l.add(null);
                    } else {
                        if (mask[j] == 1) {
                            l.add(values[c].get(i));
                        } else {
                            l.add(null);
                        }
                        j++;
                    }
                }

                // store JSON element of current timeserie in JSON doc
                jsonEFCStates.put(state, l);

            }
        }

        // finish R processing
        re.end();

        // output JSON doc to file or command line
        if (jsonFileName != null) {
            try {
                FileWriter writer = new FileWriter(new File(getModel().getWorkspace().getOutputDataDirectory(), jsonFileName.getValue()));
                writer.write(json.toString());
                writer.flush();
                writer.close();
            } catch (IOException ex) {
                Logger.getLogger(IHA_DischargeIndicators.class.getName()).log(Level.SEVERE, null, ex);
            }
        } else {
            System.out.println(json.toString());
        }
    }

}
