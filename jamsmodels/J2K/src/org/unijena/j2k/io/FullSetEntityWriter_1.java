/*
 * SelectiveEntityWriter.java
 * Created on 21. March 2006, 11:05
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

import jams.tools.JAMSTools;
import jams.data.*;
import jams.model.*;
import jams.io.*;

/**
 *
 * @author S. Kralisch
 */
public class FullSetEntityWriter_1 extends JAMSComponent {
    
    /*
     *  Component variables
     */
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "EntitySet"
            )
            public Attribute.EntityCollection entitySet;

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
            description = "time interval"
            )
            public Attribute.TimeInterval timeInterval;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "Output file header descriptions"
            )
            public Attribute.String header;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "Output file attribute names"
            )
            public Attribute.String attributeName;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "entity attribute name for weight [attName | none]"
            )
            public Attribute.String weight;
    
    private GenericDataWriter writer;
    private String[] attrs;
    private boolean headerWritten;
    int nEnts = 0;
    int tsteps = 0;
    double[][] valArray;
    int tcounter = 0;
    String[] dateStr;
    
    /*
     *  Component runstages
     */
    
    public void init() {
        writer = new GenericDataWriter(JAMSTools.CreateAbsoluteFileName(getModel().getWorkspaceDirectory().getPath(),fileName.getValue()));
        
        writer.addComment("J2K model output"+header.getValue());
        
        writer.addComment("");
        
        nEnts = this.entitySet.getEntityArray().length;
        tsteps = (int)this.timeInterval.getNumberOfTimesteps();
        valArray = new double[nEnts][tsteps];
        dateStr = new String[tsteps];
    }
    
    public void run() {
       
        dateStr[tcounter] = this.time.toString();
        entitySet.getEntityEnumerator().reset();
        
        int setCounter = 0;
        int entCounter = 0;
        boolean cont = true;
        while(cont){
            double weightVal = 1.0;
            if(!this.weight.getValue().equals("none")){
                weightVal = (((Attribute.Double)entitySet.getCurrent().getObject(this.weight.getValue())).getValue());
            }
            Object ob = entitySet.getCurrent().getObject(this.attributeName.getValue());
            if(ob.getClass().getName().contains("DoubleArray")){
                //System.out.println("HRUNo: " +((Attribute.Double)entitySet.getCurrent().getObject("ID")).getValue());
                double[] da = ((Attribute.DoubleArray)entitySet.getCurrent().getObject(this.attributeName.getValue())).getValue();
                for(int i = 0; i < da.length; i++){
                    double val = da[i] / weightVal;
                    this.valArray[entCounter][tcounter] = val;
                    //writer.addData(""+val);
                }
            } else{
                //System.out.println("Primitive");
                double da = ((Attribute.Double)entitySet.getCurrent().getObject(this.attributeName.getValue())).getValue();
                double val = da / weightVal;
                this.valArray[entCounter][tcounter] = val;
                //writer.addData(""+val);
            }
            if(setCounter < (nEnts - 1)){
                setCounter++;
            }

            //writer.addData(""+entitySet.getCurrent().getDouble(this.attributeName.getValue()));
            if(entitySet.getEntityEnumerator().hasNext() && (setCounter < nEnts)){
                entitySet.getEntityEnumerator().next();
                cont = true;
                entCounter++;
            }else
                cont = false;
        }
        this.tcounter++;
        
    }
    
    public void cleanup() {
        try{
            //always write time
            writer.addColumn("ID");
            for(int i = 0; i < tcounter; i++){
                writer.addColumn(dateStr[i]);
            }
            writer.writeHeader();
                
            for(int e = 0; e < nEnts; e++){
                writer.addData(entitySet.getEntityArray()[e].getDouble("ID"));
                for(int t = 0; t < tcounter; t++){
                    writer.addData(valArray[e][t]);
                }
                writer.writeData();
            }
            
        } catch (jams.runtime.RuntimeException jre) {
            this.getModel().getRuntime().handle(jre);
        }
        writer.flush();
        writer.close();
    }
}
