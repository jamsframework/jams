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

package hymod;

import jams.data.*;
import jams.io.GenericDataWriter;
import jams.model.JAMSComponent;
import jams.model.JAMSComponentDescription;
import jams.model.JAMSVarDescription;



/**
 *
 * @author P. Krause
 */
@JAMSComponentDescription(
        title="dataWriter",
        author="Peter Krause",
        description="The data writer for the JAMS-HYMOD model"
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
            description = "the precipitation value of the respective time step"
            )
            public Attribute.Double precip;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "the potential ET value of the respective time step"
            )
            public Attribute.Double pET;
        
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "the simulated actual ET value of the respective time step"
            )
            public Attribute.Double simET;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "the simulated direct flow component of the respective time step"
            )
            public Attribute.Double simDirectFlow;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "the simulated base flow component of the respective time step"
            )
            public Attribute.Double simBaseFlow;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "the simulated total runoff of the respective time step"
            )
            public Attribute.Double simRunoff;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "the observed of the respective time step"
            )
            public Attribute.Double obsRunoff;
        
    private GenericDataWriter writer;
    
    public void init(){
        //System.out.println("Init output ...");
        writer = new GenericDataWriter(outFileName.getValue());
        writer.addComment("hymod output");
        writer.addComment("");
        writer.addColumn("time");
        writer.addColumn("precip");
        writer.addColumn("obsRunoff");
        writer.addColumn("simRunoff");
        writer.addColumn("simDirectFlow");
        writer.addColumn("simBaseFlow");
        writer.addColumn("simET");
        writer.addColumn("pET");
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
