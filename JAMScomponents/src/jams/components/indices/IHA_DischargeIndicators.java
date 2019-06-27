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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
        title = "Title",
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

    private Rengine re;
//    private String[] efc_strings = {"extreme low flow", "high flow pulse", "large flood", "low flow", "low flow pulse", "small flood"};
    private String[] efc_strings = {"extreme low flow", "low flow", "low flow pulse", "high flow pulse", "small flood", "large flood"};

    /*
     *  Component run stages
     */
    @Override
    public void init() {

        re = (Rengine) JAMS.getObjectRepo().get("Rengine");
        if (re == null) {
            re = new Rengine(null, false, null);
            re.waitForR();
            JAMS.getObjectRepo().put("Rengine", re);
        }
        re.eval("library(flowregime)");
    }

    @Override
    public void run() {

        readTSData();
        REXP x;

        JSONObject json = new JSONObject();
        json.put("nColumns", values.length);
        json.put("efcStateNames", efc_strings);
        json.put("dates", dateStrings);
        JSONObject jsonColumn = new JSONObject();
        json.put("columns", jsonColumn);

//        System.out.println("##############################################");
//        StringBuilder b = new StringBuilder();
//        values[0].forEach((d) -> {
//            b.append(d);
//        });
//        
//        System.out.println(b.toString());
        String[] sArray = dateStrings.toArray(new String[dateStrings.size()]);
        re.assign("sarray", sArray);

        double[] array;
        // iterate over timeseries
        for (int c = 0; c < values.length; c++) {
            // create new single timeserie
            array = new double[values[c].size()];
            for (int i = 0; i < values[c].size(); i++) {
                array[i] = values[c].get(i);
            }

            JSONObject colStats = new JSONObject();
            jsonColumn.put(Integer.toString(c), colStats);
            JSONObject jsonEFCThresholds = new JSONObject();
            colStats.put("efcThresholds", jsonEFCThresholds);
            JSONObject jsonEFCStates = new JSONObject();
            colStats.put("efcStates", jsonEFCStates);

            double[] vArray = array;

            re.assign("varray", vArray);
            re.eval("dates <- as.Date(sarray)");
            re.eval("xts <- xts(x=varray, order.by=dates)");

//        System.out.println(re.eval("xts"));
//
//        x = re.eval("index(xts)");
//        System.out.println(x.getType());
//        double[] index = x.asDoubleArray();            
//        
//        x = re.eval("coredata(xts)");
//        System.out.println(x.getType());
//        double[] coredata = x.asDoubleArray();
//        RVector v = x.asVector();
//        for (int i = 0; i < v.size(); i++) {
//            System.out.print(v.getNames().get(i) + ": ");
//            System.out.println(v.at(i).asDouble());
//        }
//        x = re.eval("index(xts)");
//        String[] da = x.asStringArray();
//        for (String d : da) {
//            System.out.println(d);
//        }
//        re.eval("dat_zoo <- read.zoo(\"" + jsonFileName.getValue() + "\", index.column = 0, sep = \",\", format = \"%d.%m.%Y\")");
//        re.eval("dat_xts <- as.xts(dat_zoo)");
//        re.eval("dat_xts <- as.xts(data)");
//        re.eval("View(dat_xts)");
//        System.out.println(re.eval("dat_xts"));
//        System.out.println(re.eval("dat_zoo"));
            x = re.eval("build_EFC_thresholds(xts, method = \"advanced\")");
            RVector v = x.asVector();
            for (int i = 0; i < v.size(); i++) {
                jsonEFCThresholds.put(v.getNames().get(i).toString(), v.at(i).asDouble());
//                System.out.print(v.getNames().get(i) + ": ");
//                System.out.println(v.at(i).asDouble());
            }

            re.eval("efcs <- EFC(xts, method = \"advanced\")");
            re.eval("efc_strings<-c(\"extreme low flow\", \"low flow\", \"low flow pulse\", \"high flow pulse\", \"small flood\", \"large flood\")");
            re.eval("indices=0:(length(efc_strings)-1)");
            re.eval("names(indices)=efc_strings");
            x = re.eval("sort(unique(efcs))");
            String[] states = x.asStringArray();
            for (String state : states) {
//                System.out.println(state);
                x = re.eval("efcs==\"" + state + "\"");
                int[] mask = x.asIntArray();

                List<Double> l = new ArrayList(mask.length);

                for (int i = 0; i < mask.length; i++) {
                    if (mask[i] == 1) {
                        if (vArray[i] == JAMS.getMissingDataValue()) {
                            l.add(null);
                        } else {
                            l.add(vArray[i]);
                        }
                    } else {
                        l.add(null);
                    }
                }

                jsonEFCStates.put(state, l);

            }

//            x = re.eval("indices[efcs]");
////        System.out.println(x.getType());
//            int[] efcStates = x.asIntArray();
//
//            x = re.eval("EFC(xts, method = \"advanced\")");
//            String[] sa = x.asStringArray();
//            for (int i = 0; i < sa.length; i++) {
////            System.out.println(sa[i] + " - " + efc_strings[efcStates[i]]);
////            System.out.println(sa[i]);
//            }
        }

        re.end();
        
        
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
