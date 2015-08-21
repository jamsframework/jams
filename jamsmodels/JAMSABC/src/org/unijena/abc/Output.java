/*
 * Output.java
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

package org.unijena.abc;

import org.unijena.jams.model.*;
import org.unijena.jams.data.*;
import org.unijena.jams.io.*;

/**
 *
 * @author S. Kralisch
 */
public class Output extends JAMSComponent {
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            )
            public Attribute.Double simRunoff;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            )
            public Attribute.Double precip;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            )
            public Attribute.Double simDirectFlow;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            )
            public Attribute.Double simBaseFlow;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            )
            public Attribute.Double simET;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            )
            public Attribute.Double pET;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            )
            public Attribute.Double snowStorage;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            )
            public Attribute.Double snowMelt;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            )
            public Attribute.Double storage;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            )
            public Attribute.Calendar time;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            )
            public Attribute.Double obsRunoff;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            )
            public Attribute.String workspace;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            )
            public Attribute.String fileName;
    
    
    private GenericDataWriter writer;
    
    public void init(){
        
        String fName = workspace.getValue() + java.io.File.separatorChar + fileName.getValue(); 
        writer = new GenericDataWriter(fName);
        
        writer.addComment("abc model output");
        writer.addComment("");
        writer.addColumn("time");
        writer.addColumn("precip");
        writer.addColumn("obsRunoff");
        writer.addColumn("simRunoff");
        writer.addColumn("simDirectFlow");
        writer.addColumn("simBaseFlow");
        writer.addColumn("simET");
        writer.addColumn("pET");
        writer.addColumn("storage");
        writer.addColumn("snowStorage");
        writer.addColumn("snowMelt");
        writer.writeHeader();
        
    }
    
    
    public void run(){
        
        writer.addData(time);
        writer.addData(precip);
        writer.addData(obsRunoff);
        writer.addData(simRunoff);
        writer.addData(simDirectFlow);
        writer.addData(simBaseFlow);
        writer.addData(simET);
        writer.addData(pET);
        writer.addData(storage);
        writer.addData(snowStorage);
        writer.addData(snowMelt);
        
        try {
            writer.writeData();
        } 
        catch (org.unijena.jams.runtime.RuntimeException jre){
            System.out.println(jre.getMessage());
        }
        
    }
    
    public void cleanup(){
        //System.out.println("cleanup output ...");
        writer.close();

    }
    
}
