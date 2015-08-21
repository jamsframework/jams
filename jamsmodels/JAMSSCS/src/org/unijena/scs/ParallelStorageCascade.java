/*
 * ParallelStorageCascade.java
 * Created on 17. July 2006, 17:15
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
import jams.model.*;
import jams.data.*;

/**
 * computes the catchment's runoff based on two unit hydrographs and specific 
 * precipitation inputs for each UH.
 * @author P. Krause
 */
@JAMSComponentDescription(
        title="Parallel storage cascade",
        author="Peter Krause",
        description="simulates runoff in a parallel linear storage cascade"
        )
public class ParallelStorageCascade extends JAMSComponent {
    
    /**
     * the model's time interval<br>
     * access: READ<br> 
     * update: INIT<br> 
     * unit: n/a
     */
   @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "timeInterval"
            )
            public Attribute.TimeInterval timeInterval;
    
    /**
     * the catchment's area<br>
     * access: READ<br> 
     * update: run<br> 
     * unit: m�
     */
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            unit = "km^2",
            description = "the entire area of the catchment"
            )
            public Attribute.Double catchmentArea;
    
    /**
     * the runoff at the current time step<br>
     * access: WRITE<br> 
     * update: RUN<br> 
     * unit: m^3/s
     */
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            unit = "m�/s",
            description = "runoff"
            )
            public Attribute.Double runoff;
    /**
     * the maximum peak runoff produced by the model<br>
     * access: READWRITE<br> 
     * update: RUN<br> 
     * unit: m^3/s
     */
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            unit = "m�/s",
            description = "max runoff"
            )
            public Attribute.Double maxRunoff;
    
    /**
     * the cumulated runoff<br>
     * access: READWRITE<br> 
     * update: RUN<br> 
     * unit: million m^3
     */
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            unit = "m^3",
            description = "volume"
            )
            public Attribute.Double volume;
    
    /**
     * an array containg the single runoff values for each time step<br>
     * access: READWRITE<br> 
     * update: RUN<br> 
     * unit: m^3/s
     */
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            unit = "m�/s",
            description = "runoff_arr"
            )
            public Attribute.DoubleArray runoff_arr;
    
    /**
     * an array containg the single cumulated runoff volumes for each time step<br>
     * access: READWRITE<br> 
     * update: RUN<br> 
     * unit: Million m^3
     */
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            unit = "mm",
            description = "volume_arr"
            )
            public Attribute.DoubleArray volume_arr;
    
    /**
     * the unit hydrograph values from the quick Nash-cascade<br>
     * access: READ<br> 
     * update: RUN<br> 
     * unit: n/a
     */
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "unit hydrograph 1"
            )
            public Attribute.DoubleArray uh1;
    
    /**
     * the unit hydrograph values from the slow Nash-cascade<br>
     * access: READ<br> 
     * update: RUN<br> 
     * unit: n/a
     */
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "unit hydrograph 2"
            )
            public Attribute.DoubleArray uh2;
    
    /**
     * the input precipitation for the quick Nash-cascade<br>
     * access: READ<br> 
     * update: RUN<br> 
     * unit: n/a
     */
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "precip for uh1"
            )
            public Attribute.DoubleArray hNe1;
    
    /**
     * the input precipitation for the slow Nash-cascade<br>
     * access: READ<br> 
     * update: RUN<br> 
     * unit: n/a
     */
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "precip for uh2"
            )
            public Attribute.DoubleArray hNe2;
    
    /**
     * the elements of the unit hydrograph array. Equals precip duration divided by time step length
     * both in seconds<br>
     * access: READ<br> 
     * update: RUN<br> 
     * unit: n/a
     */
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "array length"
            )
            public Attribute.Integer arrayLength;
    
    
    double[] u1_arr, u2_arr; 
    double[] vol_arr, run_arr;
    int timeStepCounter = 0;
    int start = 0;
    
    /**
     * the component's init() routine
     * @throws org.unijena.jams.data.Attribute.Entity.NoSuchAttributeException thrown when a model entity tries to access a non existent attribute
     */
    public void init() throws Attribute.Entity.NoSuchAttributeException {
        this.timeStepCounter = 0;
        int timeSteps = (int)timeInterval.getNumberOfTimesteps()+1;
        vol_arr = new double[timeSteps]; 
        run_arr = new double[timeSteps];
        this.maxRunoff.setValue(0);
        this.volume.setValue(0);
    } 
    
    /**
     * the components() run routine
     * @throws org.unijena.jams.data.Attribute.Entity.NoSuchAttributeException thrown when a model entity tries to access a non existent attribute
     */
    public void run() throws Attribute.Entity.NoSuchAttributeException {
        //let it run until no relevant outflow occurs
    	this.u1_arr = this.uh1.getValue();
        this.u2_arr = this.uh2.getValue();
        
        //let it run until no relevant outflow occurs
        double runoff = 0;
        double volume = this.volume.getValue();
        int arrayLength = this.arrayLength.getValue();
       
        if(this.timeStepCounter < arrayLength){
            start = 0;
        } else{
            start++;
        }
        int pIdx = this.timeStepCounter;
        
        for(int j = 0; j <= this.timeStepCounter; j++){
            runoff = runoff + u1_arr[j] * hNe1.getValue()[pIdx];
            runoff = runoff + u2_arr[j] * hNe2.getValue()[pIdx];
            
            pIdx = pIdx - 1;
        }
        volume = volume + (runoff * this.timeInterval.getTimeUnitCount());
        
        run_arr[this.timeStepCounter] = runoff;
        vol_arr[this.timeStepCounter] = volume;
        
        if(runoff > this.maxRunoff.getValue())
            this.maxRunoff.setValue(runoff);
        
        this.runoff.setValue(runoff);
        this.volume.setValue(volume);
        
        this.runoff_arr.setValue(run_arr);
        
        runoff = 0;
        this.timeStepCounter++;
        
    }
}
