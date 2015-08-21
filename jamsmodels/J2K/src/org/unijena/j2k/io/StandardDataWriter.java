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

package org.unijena.j2k.io;

import java.io.IOException;
import jams.tools.JAMSTools;
import jams.data.*;
import jams.model.*;
import jams.io.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;

/**
 *
 * @author S. Kralisch
 */
public class StandardDataWriter extends JAMSComponent {
    
    /*
     *  Component variables
     */
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "time interval"
            )
            public Attribute.TimeInterval timeInterval;

    @JAMSVarDescription(
    access = JAMSVarDescription.AccessType.READ,
            description = "Output file name"
            )
            public Attribute.String fileName;
    
    @JAMSVarDescription(
    access = JAMSVarDescription.AccessType.READ,
            description = "Current time"
            )
            public Attribute.Calendar time;
    
    @JAMSVarDescription(
    access = JAMSVarDescription.AccessType.READ,
            description = "Output file header descriptions"
            )
            public Attribute.StringArray headers;
    
    @JAMSVarDescription(
    access = JAMSVarDescription.AccessType.READ,
            description = "Output file attributes"
            )
            public Attribute.Double[] value;
    
    @JAMSVarDescription(
    access = JAMSVarDescription.AccessType.READ,
            description = "Output data precision"
            )
            public Attribute.Integer precision;    
    
    private GenericDataWriter writer;
    private int prec;
    private DateFormat dateFormat;
    
    /*
     *  Component runstages
     */
    
    public void init() {
        writer = new GenericDataWriter(JAMSTools.CreateAbsoluteFileName(getModel().getWorkspace().getOutputDataDirectory().getPath(),fileName.getValue()));
        
        writer.addComment("J2K model output");
        writer.addComment("");
        
        //always write time
        writer.addColumn("date");
        
        for (int i = 0; i < headers.getValue().length; i++) {
            writer.addColumn(headers.getValue()[i]);
        }
        
        writer.writeHeader();
        
        if (precision == null) {
            prec = 3;
        } else {
            prec = precision.getValue();
        }
        
        int tu = this.timeInterval.getTimeUnit();
        String timeFormat = "%1$tY-%1$tm-%1$td %1$tH:%1$tM";
        dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        //hourly values
        if(tu == 11) {
            timeFormat = "%1$td.%1$tm.%1$tY %1$tH:%1$tM";
            dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        //daily values
        } else if(tu == 6) {
            timeFormat = "%1$td.%1$tm.%1$tY";
            dateFormat = new SimpleDateFormat("dd.MM.yyyy");
        //monthly values
        } else if(tu == 2) {
            timeFormat = "%1$tm/%1$tY";
            dateFormat = new SimpleDateFormat("MM/yyyy");
        //annual values
        } else if(tu == 1) {
            timeFormat = "%1$tY";        
            dateFormat = new SimpleDateFormat("yyyy");
        }
        dateFormat.setTimeZone(Attribute.Calendar.DEFAULT_TIME_ZONE);
    }
    
    public void run() {
        //always write time
        //the time also knows a toString() method with additional formatting parameters
        //e.g. time.toString("%1$tY-%1$tm-%1$td %1$tH:%1$tM")

        
        //System.out.println(tu + " " + timeFormat);
        writer.addData(time.toString(dateFormat));
        
        for (int i = 0; i < value.length; i++) {
            
            writer.addData(value[i].getValue(), prec);
        }
        
        try {
            writer.writeData();
        } catch (jams.runtime.RuntimeException jre) {
            getModel().getRuntime().println(jre.getMessage());
        }
    }
    
    public void cleanup() {
        try {
            writer.writer.flush();
            writer.writer.close();
        } catch (IOException ex) {
        }
    }
}
