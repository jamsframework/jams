/*
 * KirchnerReader.java
 * Created on 04.09.2013, 14:25:39
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

package kirchner_methodology;

import jams.data.*;
import jams.model.*;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

/**
 *
 * @author Sven Kralisch <sven.kralisch at uni-jena.de>
 */
@JAMSComponentDescription(
    title="Title",
    author="Author",
    description="Description",
    date = "YYYY-MM-DD",
    version = "1.0_0"
)
@VersionComments(entries = {
    @VersionComments.Entry(version = "1.0_0", comment = "Initial version"),
    @VersionComments.Entry(version = "1.0_1", comment = "Some improvements")
})
public class KirchnerReader extends JAMSComponent {

    /*
     *  Component attributes
     */
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ)
            public Attribute.String fileName;
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE)
            public Attribute.Double simQ;
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE)
            public Attribute.Double obsQ;
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE)
            public Attribute.Double precip;
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE)
            public Attribute.Double actET;    
                
    
    /*
     *  Component run stages
     */
    
    BufferedReader reader;
    
    @Override
    public void init() {
        try {
            reader = new BufferedReader(new FileReader(fileName.getValue()));
            reader.readLine();
        } catch (IOException ex) {
            getModel().getRuntime().println("Error while reading from " + fileName.getValue() + ": " + ex.getMessage());
        }
    }

    @Override
    public void run() {
        try {
            String line = reader.readLine().trim();
            String[] result = line.split("\\s+");
            simQ.setValue(result[1]);
            obsQ.setValue(result[2]);
            precip.setValue(result[3]);
            actET.setValue(result[4]);
            
        } catch (IOException ex) {
            getModel().getRuntime().println("Error while reading from " + fileName.getValue() + ": " + ex.getMessage());
        }
    
    }

    @Override
    public void cleanup() {
        try {
            reader.close();
        } catch (IOException ex) {
            getModel().getRuntime().println("Error while reading from " + fileName.getValue() + ": " + ex.getMessage());
        }
    }
}