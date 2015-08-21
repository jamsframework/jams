/*
 * CalcDailyETP_PenmanMonteith.java
 * Created on 24. November 2005, 13:57
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

package org.unijena.j2k.regionWK.AP2;

import java.io.*;
import jams.data.*;
import jams.model.*;

/**
 *
 * @author Peter Krause
 */

    
    
@JAMSComponentDescription(
        title="CalcDailyETP_Haude",
        author="Peter Krause",
        description="Calculates daily potential ETP after Penman-Monteith"
        )
    
    public class Haude extends JAMSComponent {
    
        
    /*
     *  Component variables
     */
 @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "time"
            )
            public Attribute.Calendar time;
 
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "state variable mean temperature"
            )
            public Attribute.Double tmean;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "state variable maximum temperature"
            )
            public Attribute.Double tmax;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "state variable relative humidity"
            )
            public Attribute.Double rhum;

    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "attribute area"
            )
            public Attribute.Double area;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            description = "daily potential ETP [mm/d]"
            )
            public Attribute.Double pET;
    
     @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            description = "actHaudeFactor",
            defaultValue="0"
            )
            public Attribute.Double actHaudeFactor;
     
     @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "Haude factor array"
            )
            public Attribute.DoubleArray haudeFactorArray;
    
    
    
    /*
     *  Component run stages
     */
    
    public void init() throws Attribute.Entity.NoSuchAttributeException, IOException {
        
    }
    public void run() throws Attribute.Entity.NoSuchAttributeException, IOException {
        
        int monthCount = time.get(time.MONTH);
        int dayCount = time.get(time.DAY_OF_YEAR) - 1;
        int hourCount = time.get(time.HOUR_OF_DAY) + (24 * dayCount);
        
        double h_factor = -9999;
        
        if(this.haudeFactorArray != null)
            h_factor = this.haudeFactorArray.getValue()[monthCount];
        
        this.actHaudeFactor.setValue(h_factor);
        
            double tmeanVal = tmean.getValue();
            double tmaxVal = tmax.getValue();
            double rhumVal = rhum.getValue();
            double areaVal = area.getValue();

            
            double est = org.unijena.j2k.physicalCalculations.ClimatologicalVariables.calc_saturationVapourPressure(tmeanVal);
            //kPa -> hPa
            est = 10 * est;

            //compute maximum humidity
            double maxHum = est * 216.7 /(tmeanVal + 273.15);

            //compute absolute humidity (with tmean)
            double ahumVal = maxHum * (rhumVal / 100.);
            
            //compute rhum from tmax and ahum
            est = org.unijena.j2k.physicalCalculations.ClimatologicalVariables.calc_saturationVapourPressure(tmaxVal);
            est = 10 * est;
          
            maxHum = est * 216.7 /(tmaxVal + 273.15);
           
            rhumVal = (ahumVal/maxHum)*100;
          
            double pETP = est * (1 - (rhumVal/100.)) * h_factor;
            //double ea = est * rhumVal/100;
            //double pETP = (est - ea) * h_factor;
                                 
            //converting mm to litres
            pETP = pETP * areaVal;
            
            //avoiding negative potETPs
            if(pETP < 0){
                pETP = 0;
            }
            
            //conversion from daily to hourly values
            //pETP = pETP / 24;
            
            pET.setValue(pETP);
                   
        
    }
    
    public void cleanup()  throws IOException {
        
    }
}
