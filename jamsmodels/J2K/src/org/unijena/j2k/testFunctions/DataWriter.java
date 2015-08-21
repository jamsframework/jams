/*
 * DataWriter.java
 * Created on 30. September 2005, 11:37
 *
 * This file is part of JAMS
 * Copyright (C) 2005 S. Kralisch and P. Krause
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

package org.unijena.j2k.testFunctions;

import jams.model.*;
import jams.data.*;
import jams.io.*;

/**
 *
 * @author P. Krause
 */
@JAMSComponentDescription(
        title="dataWriter",
        author="Peter Krause",
        description="The data writer for the test cases model"
        )
public class DataWriter extends JAMSComponent {
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "full qualified path name for model output file"
            )
            public Attribute.String outFileName;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "the current date provided by the temporal context"
            )
            public Attribute.Calendar time;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "the input value of the respective time step"
            )
            public Attribute.Double input;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "the simulation value of the respective time step"
            )
            public Attribute.Double simulation;
        
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "the observation value of the respective time step"
            )
            public Attribute.Double observation;
    
    
        
    private GenericDataWriter writer;
    
    public void init(){
        //System.out.println("Init output ...");
        writer = new GenericDataWriter(outFileName.getValue());
        writer.addComment("test output");
        writer.addComment("");
        writer.addColumn("time");
        writer.addColumn("input");
        writer.addColumn("simulation");
        writer.addColumn("observation");
        writer.writeHeader();
        
    }
    
    
    public void run(){
        
        writer.addData(time);
        writer.addData(input);
        writer.addData(simulation);
        writer.addData(observation);
       
        try {
            writer.writeData();
        } catch (jams.runtime.RuntimeException jre) {
            System.out.println(jre.getMessage());
        }
        
    }
    
    public void cleanup(){
        //System.out.println("cleanup output ...");
        writer.close();

    }
    
}
