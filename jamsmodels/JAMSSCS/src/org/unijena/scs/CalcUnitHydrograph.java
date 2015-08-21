/*
 * CalcUnitHydrograph.java
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
 * This components calculates the unit-hydrographs
 * for the two storages of the SCS-method, based on the
 * recession coefficients k1 and k2. The first
 * unit hydrographs represent the quick runoff component
 * the second one the delayed runoff component
 * In addition, the effective precipitation is divided into two components
 * to serve as input for the two unit hydrographs. This is done by the coefficient beta,
 * which has to be provided as input for this component.
 * @author P. Krause
 */
@JAMSComponentDescription(
        title="SCS-UnitHydrograph",
        author="Peter Krause",
        description="This components calculates the unit-hydrographs" +
                    "for the two storages of the SCS-method, based on the " +
                    "recession coefficients k1 and k2. The first" +
                    "unit hydrographs represent the quick runoff component" +
                    "the second one the delayed runoff component" +
                    "In addition, the effective precipitation is divided into two components" +
                    "to serve as input for the two unit hydrographs. This is done by the coefficient beta," +
                    "which has to be provided as input for this component"
        )
public class CalcUnitHydrograph extends JAMSComponent {
    
    /**
     * the catchment's area in square meters<br>
     * access: READ<br> 
     * update: RUN<br> 
     * unit: m^2
     */
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            unit = "m^2",
            description = "the entire area of the catchment"
            )
            public Attribute.Double catchmentArea;
    
    /**
     * the catchment's area in square meters<br>
     * access: WRITE<br> 
     * update: RUN<br> 
     * unit: m^2
     */
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "timeInterval"
            )
            public Attribute.TimeInterval timeInterval;
    
    /**
     * the duration of the rainfall event<br>
     * access: READ<br> 
     * update: RUN<br> 
     * unit: s
     */
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            unit = "s",
            description = "duration of precip event"
            )
            public Attribute.Double precipDuration;
    
    /**
     * the effective input precipitation<br>
     * access: READ<br> 
     * update: RUN<br> 
     * unit: mm
     */
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            unit = "mm",
            description = "effective rainfall"
            )
            public Attribute.Double effectivePrecip;
    
    /**
     * flag for selection of temporal rainfall distribution. Valid values are:<br>
     * B uniform distribution<br>
     * M middle accented rainfall<br>
     * A start accented rainfall<br>
     * E end accented rainfall<br>
     * access: READ<br> 
     * update: RUN<br> 
     * unit: n/a
     */
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "precip distribution B = uniform, M = middle accented, A = start accented, E = end accented"
            )
            public Attribute.String precipDistribution;
    
    /**
     * the distribution factor beta which is responsible for the distribution of the
     * effective precipitation to the two Nash-cascades<br>
     * access: READ<br> 
     * update: RUN<br> 
     * unit: n/a
     */
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "distribution factor beta"
            )
            public Attribute.Double beta;
    
    /**
     * the retention coefficient for the quick Nash-cascade<br>
     * access: READ<br> 
     * update: RUN<br> 
     * unit: n/a
     */
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "retention factor k1"
            )
            public Attribute.Double k1;
    
    /**
     * the retention coefficient for the slow Nash-cascade<br>
     * access: READ<br> 
     * update: RUN<br> 
     * unit: n/a
     */
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "retention factor k2"
            )
            public Attribute.Double k2;
    
    /**
     * the unit hydrograph values from the quick Nash-cascade<br>
     * access: WRITE<br> 
     * update: RUN<br> 
     * unit: n/a
     */
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            description = "unit hydrograph 1"
            )
            public Attribute.DoubleArray uh1;
    
    /**
     * the unit hydrograph values from the slow Nash-cascade<br>
     * access: WRITE<br> 
     * update: RUN<br> 
     * unit: n/a
     */
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            description = "unit hydrograph 2"
            )
            public Attribute.DoubleArray uh2;
    
    /**
     * the input precipitation for the quick Nash-cascade<br>
     * access: WRITE<br> 
     * update: RUN<br> 
     * unit: n/a
     */
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            description = "precip for uh1"
            )
            public Attribute.DoubleArray hNe1;
    
    /**
     * the input precipitation for the slow Nash-cascade<br>
     * access: WRITE<br> 
     * update: RUN<br> 
     * unit: n/a
     */
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            description = "precip for uh2"
            )
            public Attribute.DoubleArray hNe2;
    
    /**
     * the elements of the unit hydrograph array. Equals precip duration divided by time step length
     * both in seconds<br>
     * access: WRITE<br> 
     * update: RUN<br> 
     * unit: n/a
     */
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            description = "array length"
            )
            public Attribute.Integer arrayLength;
    
    
    
    /**
     * the component's run method
     * @throws org.unijena.jams.data.Attribute.Entity.NoSuchAttributeException thrown when a model entity tries to access a non existent attribute
     */
   public void run() throws Attribute.Entity.NoSuchAttributeException {
        int nStor = 2;
        double pDur = this.precipDuration.getValue() * 60.0;        
        int timeSteps = (int)timeInterval.getNumberOfTimesteps()+1;
        
        double[] u1_arr = new double[timeSteps]; 
        double[] u2_arr = new double[timeSteps];
        double[] p1_arr = new double[timeSteps]; 
        double[] p2_arr = new double[timeSteps];
        
        int arrayLength = (int)(pDur / this.timeInterval.getTimeUnitCount());
        double hNe = 0;
        double hNe1 = 0;
        double hNe2 = 0;
        
        int faculty = 1;
        double tInterval = (double)this.timeInterval.getTimeUnitCount() / 3600.0;
        
        double counter = 0;
        //calculate the unit hydrograph for both cascades
        int pD = (int)this.precipDuration.getValue() * 60;
        int tC = this.timeInterval.getTimeUnitCount();
        
        double restPrecip = effectivePrecip.getValue();
        
        for(int i = 0; i < timeSteps; i++){
            int ts = i+1;
            //uniform precip distribution
            if(this.precipDistribution.getValue().equalsIgnoreCase("B")){
                if((ts * tC) <= pD){
                    hNe = effectivePrecip.getValue() / ((double)pD / (double)tC);
                    hNe1 = hNe * beta.getValue();
                    hNe2 = hNe * (1 - beta.getValue());
                    restPrecip = restPrecip - hNe;
                } else{
                    hNe = restPrecip;
                    hNe1 = hNe * beta.getValue();
                    hNe2 = hNe * (1 - beta.getValue());
                    restPrecip = restPrecip - hNe;
                }
            }
            //middle accented rainfall (20,50,30% in 30,20,50% duration)
            else if(this.precipDistribution.getValue().equalsIgnoreCase("M")){
                if((ts * tC) <= (pD * 0.166667)){
                    double rain = effectivePrecip.getValue() * 0.075;
                    double d_part = pD * 0.166667;
                    hNe = rain / ((double)d_part / (double)tC);
                    hNe1 = hNe * beta.getValue();
                    hNe2 = hNe * (1 - beta.getValue());
                    restPrecip = restPrecip - hNe;
                }
                else if((ts * tC) <= (pD * 0.333367)){
                    double rain = effectivePrecip.getValue() * 0.25;
                    double d_part = pD * 0.166667;
                    hNe = rain / ((double)d_part / (double)tC);
                    hNe1 = hNe * beta.getValue();
                    hNe2 = hNe * (1 - beta.getValue());
                    restPrecip = restPrecip - hNe;
                }
                else if((ts * tC) <= (pD * 0.500067)){
                    double rain = effectivePrecip.getValue() * 0.5;
                    double d_part = pD * 0.166667;
                    hNe = rain / ((double)d_part / (double)tC);
                    hNe1 = hNe * beta.getValue();
                    hNe2 = hNe * (1 - beta.getValue());
                    restPrecip = restPrecip - hNe;
                }
                else if((ts * tC) <= (pD * 0.666767)){
                    double rain = effectivePrecip.getValue() * 0.1;
                    double d_part = pD * 0.166667;
                    hNe = rain / ((double)d_part / (double)tC);
                    hNe1 = hNe * beta.getValue();
                    hNe2 = hNe * (1 - beta.getValue());
                    restPrecip = restPrecip - hNe;
                }
                else if((ts * tC) <= (pD * 0.833467)){
                    double rain = effectivePrecip.getValue() * 0.05;
                    double d_part = pD * 0.166667;
                    hNe = rain / ((double)d_part / (double)tC);
                    hNe1 = hNe * beta.getValue();
                    hNe2 = hNe * (1 - beta.getValue());
                    restPrecip = restPrecip - hNe;
                }
                else if((ts * tC) <= (pD)){
                    double rain = effectivePrecip.getValue() * 0.025;
                    double d_part = pD * 0.5;
                    hNe = rain / ((double)d_part / (double)tC);
                    hNe1 = hNe * beta.getValue();
                    hNe2 = hNe * (1 - beta.getValue());
                    restPrecip = restPrecip - hNe;
                }
                else{
                    hNe = restPrecip;
                    hNe1 = hNe * beta.getValue();
                    hNe2 = hNe * (1 - beta.getValue());
                    restPrecip = restPrecip - hNe;
                }
            }
            //left accented rainfall 
            else if(this.precipDistribution.getValue().equalsIgnoreCase("A")){
                if((ts * tC) <= (pD * 0.166667)){
                    double rain = effectivePrecip.getValue() * 0.5;
                    double d_part = pD * 0.166667;
                    hNe = rain / ((double)d_part / (double)tC);
                    hNe1 = hNe * beta.getValue();
                    hNe2 = hNe * (1 - beta.getValue());
                    restPrecip = restPrecip - hNe;
                }
                else if((ts * tC) <= (pD * 0.333367)){
                    double rain = effectivePrecip.getValue() * 0.25;
                    double d_part = pD * 0.166667;
                    hNe = rain / ((double)d_part / (double)tC);
                    hNe1 = hNe * beta.getValue();
                    hNe2 = hNe * (1 - beta.getValue());
                    restPrecip = restPrecip - hNe;
                }
                else if((ts * tC) <= (pD * 0.500067)){
                    double rain = effectivePrecip.getValue() * 0.1;
                    double d_part = pD * 0.166667;
                    hNe = rain / ((double)d_part / (double)tC);
                    hNe1 = hNe * beta.getValue();
                    hNe2 = hNe * (1 - beta.getValue());
                    restPrecip = restPrecip - hNe;
                }
                else if((ts * tC) <= (pD * 0.666767)){
                    double rain = effectivePrecip.getValue() * 0.075;
                    double d_part = pD * 0.166667;
                    hNe = rain / ((double)d_part / (double)tC);
                    hNe1 = hNe * beta.getValue();
                    hNe2 = hNe * (1 - beta.getValue());
                    restPrecip = restPrecip - hNe;
                }
                else if((ts * tC) <= (pD * 0.833467)){
                    double rain = effectivePrecip.getValue() * 0.05;
                    double d_part = pD * 0.166667;
                    hNe = rain / ((double)d_part / (double)tC);
                    hNe1 = hNe * beta.getValue();
                    hNe2 = hNe * (1 - beta.getValue());
                    restPrecip = restPrecip - hNe;
                }
                else if((ts * tC) <= (pD)){
                    double rain = effectivePrecip.getValue() * 0.025;
                    double d_part = pD * 0.5;
                    hNe = rain / ((double)d_part / (double)tC);
                    hNe1 = hNe * beta.getValue();
                    hNe2 = hNe * (1 - beta.getValue());
                    restPrecip = restPrecip - hNe;
                }
                else{
                    hNe = restPrecip;
                    hNe1 = hNe * beta.getValue();
                    hNe2 = hNe * (1 - beta.getValue());
                    restPrecip = restPrecip - hNe;
                }
            }
            //right accented rainfall 
            else if(this.precipDistribution.getValue().equalsIgnoreCase("E")){
                if((ts * tC) <= (pD * 0.166667)){
                    double rain = effectivePrecip.getValue() * 0.025;
                    double d_part = pD * 0.166667;
                    hNe = rain / ((double)d_part / (double)tC);
                    hNe1 = hNe * beta.getValue();
                    hNe2 = hNe * (1 - beta.getValue());
                    restPrecip = restPrecip - hNe;
                }
                else if((ts * tC) <= (pD * 0.333367)){
                    double rain = effectivePrecip.getValue() * 0.05;
                    double d_part = pD * 0.166667;
                    hNe = rain / ((double)d_part / (double)tC);
                    hNe1 = hNe * beta.getValue();
                    hNe2 = hNe * (1 - beta.getValue());
                    restPrecip = restPrecip - hNe;
                }
                else if((ts * tC) <= (pD * 0.500067)){
                    double rain = effectivePrecip.getValue() * 0.075;
                    double d_part = pD * 0.166667;
                    hNe = rain / ((double)d_part / (double)tC);
                    hNe1 = hNe * beta.getValue();
                    hNe2 = hNe * (1 - beta.getValue());
                    restPrecip = restPrecip - hNe;
                }
                else if((ts * tC) <= (pD * 0.666767)){
                    double rain = effectivePrecip.getValue() * 0.1;
                    double d_part = pD * 0.166667;
                    hNe = rain / ((double)d_part / (double)tC);
                    hNe1 = hNe * beta.getValue();
                    hNe2 = hNe * (1 - beta.getValue());
                    restPrecip = restPrecip - hNe;
                }
                else if((ts * tC) <= (pD * 0.833467)){
                    double rain = effectivePrecip.getValue() * 0.25;
                    double d_part = pD * 0.166667;
                    hNe = rain / ((double)d_part / (double)tC);
                    hNe1 = hNe * beta.getValue();
                    hNe2 = hNe * (1 - beta.getValue());
                    restPrecip = restPrecip - hNe;
                }
                else if((ts * tC) <= (pD)){
                    double rain = effectivePrecip.getValue() * 0.5;
                    double d_part = pD * 0.5;
                    hNe = rain / ((double)d_part / (double)tC);
                    hNe1 = hNe * beta.getValue();
                    hNe2 = hNe * (1 - beta.getValue());
                    restPrecip = restPrecip - hNe;
                }
                else{
                    hNe = restPrecip;
                    hNe1 = hNe * beta.getValue();
                    hNe2 = hNe * (1 - beta.getValue());
                    restPrecip = restPrecip - hNe;
                }
            }
            u1_arr[i] = (this.catchmentArea.getValue() / (3600*1000)) * (1/(this.k1.getValue() * faculty) * Math.pow(counter/this.k1.getValue(), nStor-1) * Math.exp(-counter/this.k1.getValue()));
            u2_arr[i] = (this.catchmentArea.getValue() / (3600*1000)) * (1/(this.k2.getValue() * faculty) * Math.pow(counter/this.k2.getValue(), nStor-1) * Math.exp(-counter/this.k2.getValue()));
            p1_arr[i] = hNe1;
            p2_arr[i] = hNe2;
            counter = counter + tInterval;
        }
        
        this.uh1.setValue(u1_arr);
        this.uh2.setValue(u2_arr);
        this.hNe1.setValue(p1_arr);
        this.hNe2.setValue(p2_arr);
        
        this.arrayLength.setValue(arrayLength);
        
    }
    
}
