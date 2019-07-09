/*
 * TNC_GroundwaterIndicators.java
 * Created on 09.07.2019, 16:49:27
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
import java.util.logging.Level;
import java.util.logging.Logger;
import org.json.JSONObject;

/**
 *
 * @author Sven Kralisch <sven.kralisch@uni-jena.de>
 */
@JAMSComponentDescription(
        title = "TNC_GroundwaterIndicators",
        author = "sven Kralisch",
        description = "Calculates variance and standard deviation from groundwater "
        + "levels",
        date = "2019-07-09",
        version = "1.0_0"
)
@VersionComments(entries = {
    @VersionComments.Entry(version = "1.0_0", comment = "Initial version")
})
public class TNC_GroundwaterIndicators extends TimeSeriesIndicators {

    /*
     *  Component attributes
     */
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
            description = "Name of JSON output file (leave empty to diable)")
    public Attribute.String jsonFileName;

    /*
     *  Component run stages
     */
    @Override
    public void run() {

        readTSData();

        // create JSON object and fill it with some data
        JSONObject json = new JSONObject();
        json.put("nColumns", values.length);
        JSONObject jsonColumn = new JSONObject();
        json.put("columns", jsonColumn);

        // iterate over timeseries
        for (int i = 0; i < values.length; i++) {
            // create new single timeserie

            double mean = 0, var = 0;
            long c = 0;

            for (double d : values[i]) {

                // if the value is not missing...
                if (d != JAMS.getMissingDataValue()) {
                    mean += d;
                    c++;
                }
            }
                    
            mean /= (c-1);

            for (double d : values[i]) {
                // if the value is not missing...
                if (d != JAMS.getMissingDataValue()) {
                    var += Math.pow(d - mean, 2);
                }
            }
            var /= (c-1);

            JSONObject colStats = new JSONObject();
            jsonColumn.put(Integer.toString(i), colStats);
            colStats.put("variance", var);
            colStats.put("stddev", Math.sqrt(var));
            colStats.put("mean", mean);

        }

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

    @Override
    public void cleanup() {
    }
}
