/*
 * StandardDataWriter.java
 * Created on 21. November 2005, 11:05
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

package org.unijena.scs;

import jams.data.*;
import jams.model.*;
import jams.io.*;
import java.text.SimpleDateFormat;

/**
 * writes output to a user defined output file
 * @author Peter Krause
 */
public class StandardDataWriter extends JAMSComponent {
    
    /**
     * the model's workspace<br>
     * access: READ<br> 
     * update: INIT<br> 
     * unit: n/a
     */  
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "Data file directory name"
            )
            public Attribute.String dirName;
    
    /**
     * the name of the output file<br>
     * access: READ<br> 
     * update: INIT<br> 
     * unit: n/a
     */ 
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "Output file name"
            )
            public Attribute.String fileName;
    
    /**
     * the model's current time step <br>
     * access: READ<br> 
     * update: RUN<br> 
     * unit: n/a
     */ 
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "Current time"
            )
            public Attribute.Calendar time;
    
    /**
     * the header description for each variable written to the output file<br>
     * access: READ<br> 
     * update: INIT<br> 
     * unit: n/a
     */ 
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "Output file header descriptions"
            )
            public Attribute.StringArray headers;
    
     /**
     * additional information for the header of the output file<br>
     * access: WRITE<br>
     * update: RUN<br>
     * unit: n/a
     */
    @JAMSVarDescription(
    access = JAMSVarDescription.AccessType.READ,
            description = "outfile header"
            )
            public Attribute.String outfileHeader;
    
    /**
     * the set of output values written to the file<br>
     * access: READ<br> 
     * update: RUN<br> 
     * unit: n/a
     */ 
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "Output file attributes"
            )
            public Attribute.Double[] value;
    
    
    private GenericDataWriter writer;
    
    /*
     *  Component runstages
     */
    
    /**
     * the component's init() method
     */
    public void init() {
        writer = new GenericDataWriter(dirName.getValue()+"/"+fileName.getValue());
        writer.writeLine("#JAMSSCS-Einzelereignis");
        writer.writeLine(this.outfileHeader.getValue());
        
        //always write time
        writer.addColumn("Zeit");
        
        for (int i = 0; i < headers.getValue().length; i++) {
            writer.addColumn(headers.getValue()[i]);
        }
        
        writer.writeHeader();
    }
    
    /**
     * the component's run() method
     * @throws jams.data.Attribute.Entity.NoSuchAttributeException thrown when a model entity tries to access a non existent attribute
     */
    public void run() throws Attribute.Entity.NoSuchAttributeException {
        //always write time
        //the time also knows a toString() method with additional formatting parameters
        //e.g. time.toString("%1$tY-%1$tm-%1$td %1$tH:%1$tM")
        writer.addData(time.toString(new SimpleDateFormat("%1$tH:%1$tM:%1$tS")));
        
        for (int i = 0; i < value.length; i++) {
            writer.addData(value[i].getValue(), 3);
        }
        
        try {
            writer.writeData();
        } catch (jams.runtime.RuntimeException jre) {
            getModel().getRuntime().println(jre.getMessage());
        }
    }
    
    /**
     * the component's cleanup() method
     */
    public void cleanup() {
        writer.close();
    }
}
