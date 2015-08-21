/*
 * J2KProcessReachRouting.java
 * Created on 28. November 2005, 10:01
 *
 * This file is part of JAMS
 * Copyright (C) 2005 FSU Jena, c0krpe
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

package org.unijena.j2k.routing;

import jams.data.*;
import jams.model.*;

/**
 *
 * @author c0krpe
 */
@JAMSComponentDescription(
        title="Title",
        author="Author",
        description="Description"
        )
        public class J2KReservoirModule extends JAMSComponent {
    
    /*
     *  Component variables
     */
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "The reach collection"
            )
            public Attribute.EntityCollection reaches;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "The receiver reach ID"
            )
            public Attribute.Double outReachID;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "data array read from file"
            )
            public Attribute.DoubleArray inDataArray;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "var names in data array"
            )
            public Attribute.StringArray inDataNames;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = "the outflow from the data file or another model"
            )
            public Attribute.Double resOutflow;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = "the reservoir area read in from file"
            )
            public Attribute.Double resArea;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = "the reservoir volume read in from file"
            )
            public Attribute.Double resObsVolume;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            description = "the calculated reservoir volume"
            )
            public Attribute.Double resCalcVolume;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = "the reservoir depth read in from file"
            )
            public Attribute.Double resDepth;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = "component RD1"
            )
            public Attribute.Double compRD1;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = "component RD2"
            )
            public Attribute.Double compRD2;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = "component RG1"
            )
            public Attribute.Double compRG1;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = "component RG2"
            )
            public Attribute.Double compRG2;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = "outflow RD1"
            )
            public Attribute.Double outRD1;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = "outflow RD2"
            )
            public Attribute.Double outRD2;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = "outflow RG1"
            )
            public Attribute.Double outRG1;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = "outflow RG2"
            )
            public Attribute.Double outRG2;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "the precipitation"
            )
            public Attribute.Double precip;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "the potential et"
            )
            public Attribute.Double pET;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = "the actual et"
            )
            public Attribute.Double actET;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "startVolume"
            )
            public Attribute.Double startVolume;
    
    int nComp = 4;
    double[] relComp;
    double[] runComp;
    double[] outComp;
    Attribute.Entity recReach = null;
    double currVolume = 0;
    
    /*
     *  Component run stages
     */
    
    public void init() {
        relComp = new double[nComp];
        runComp = new double[nComp];
        outComp = new double[nComp];
        
        this.resCalcVolume.setValue(this.startVolume.getValue());
        
        //find the recReachObject
        int nEnts = this.reaches.getEntityArray().length;
        for(int i = 0; i < nEnts; i++){
            if(this.reaches.getEntityArray()[i].getDouble("ID") == this.getContext().getEntities().getCurrent().getDouble("to_reach")){
                recReach = this.reaches.getEntityArray()[i];
            }
        }
        
    }
    
    public void run() {
        
        //passing the file values to state vars
        for(int i = 0; i < this.inDataArray.getValue().length; i++){
            if(this.inDataNames.getValue()[i].equals("runoff")){
                this.resOutflow.setValue(this.inDataArray.getValue()[i]);
            }
            else if(this.inDataNames.getValue()[i].equals("depth")){
                this.resDepth.setValue(this.inDataArray.getValue()[i]);
            }
            else if(this.inDataNames.getValue()[i].equals("area")){
                this.resArea.setValue(this.inDataArray.getValue()[i]);
            }
            else if(this.inDataNames.getValue()[i].equals("volume")){
                this.resObsVolume.setValue(this.inDataArray.getValue()[i]);
            }
            else{
                System.out.println("Error, wrong attribute name in reservoir file!");
            }
                  
        }
        
        //set states
        
        currVolume = this.resCalcVolume.getValue();
        
        double test = this.compRD1.getValue();
        test = test + this.compRD2.getValue();
        test = test + this.compRG1.getValue();
        test = test + this.compRG2.getValue();
        
        System.out.println("test: " + test + " currVolume: " + currVolume);
        if(test < currVolume){
            this.compRD1.setValue(currVolume * 0.1);
            this.compRD2.setValue(currVolume * 0.3);
            this.compRG1.setValue(currVolume * 0.3);
            this.compRG2.setValue(currVolume * 0.3);
        }
        else{
            calcRelComponents();
        }
        
        //get states and pass them to local vars
        this.runComp[0] = this.compRD1.getValue();
        this.runComp[1] = this.compRD2.getValue();
        this.runComp[2] = this.compRG1.getValue();
        this.runComp[3] = this.compRG2.getValue();
        
        
        double runArea = this.resArea.getValue();
        double runPET = this.pET.getValue() * runArea;
        
        //add precip to comp RD1
        this.runComp[0] = this.runComp[0] + this.precip.getValue() * runArea;
        
        
        //recalculate relative parts
        calcRelComponents();
        
        //substract et
        if(currVolume < runPET){
            this.actET.setValue(currVolume);
            for(int i = 0; i < runComp.length; i++)
                runComp[i] = 0;
        }else{
            this.actET.setValue(runPET);
            for(int i = 0; i < runComp.length; i++)
                runComp[i] = runComp[i] - runPET * relComp[i];  
        }
        
        //recalculate relative parts and volume
        calcRelComponents();
        
        //substract outflow
        double runOutflow = this.resOutflow.getValue() * 1000 * 86400;
        
        if(currVolume < runOutflow){
            for(int i = 0; i < runComp.length; i++){
                outComp[i] = runOutflow * relComp[i];
                runComp[i] = 0;
                relComp[i] = 0;
            }
            currVolume = 0;
        }else{
            for(int i = 0; i < runComp.length; i++){
                outComp[i] = runOutflow * relComp[i];
                runComp[i] = runComp[i] - outComp[i];
            }
            currVolume = currVolume - runOutflow;
        }
        
        //recalculate relative parts and volume
        calcRelComponents();
        
        //route outflow to receiver reach
        recReach.setDouble("inRD1", outComp[0] + recReach.getDouble("inRD1"));
        recReach.setDouble("inRD2", outComp[1] + recReach.getDouble("inRD2"));
        recReach.setDouble("inRG1", outComp[2] + recReach.getDouble("inRG1"));
        recReach.setDouble("inRG2", outComp[3] + recReach.getDouble("inRG2"));
        
        //mapping vars back
        this.resCalcVolume.setValue(this.currVolume);
        this.compRD1.setValue(this.runComp[0]);
        this.compRD2.setValue(this.runComp[1]);
        this.compRG1.setValue(this.runComp[2]);
        this.compRG2.setValue(this.runComp[3]);
        
        System.out.println(""+ this.currVolume + "\t" + this.actET.getValue()/runArea + "\t" + this.resOutflow.getValue());
    }
    
    public void cleanup() {
        
    }
    
    private void calcRelComponents(){
        currVolume = 0;
        for(int i = 0; i < nComp; i++){
            currVolume = currVolume + runComp[i];
        }
        for(int i = 0; i < nComp; i++){
            if(currVolume > 0)
                relComp[i] = runComp[i] / currVolume;
            else
                relComp[i] = 0;
        }
    }
    
    
}
