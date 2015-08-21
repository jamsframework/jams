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

package org.unijena.abc;

import jams.data.*;
import jams.model.JAMSComponent;
import jams.model.JAMSComponentDescription;
import jams.model.JAMSVarDescription;
import java.io.*;

/**
 *
 * @author Peter Krause
 */

    
    
@JAMSComponentDescription(
        title="CalcDailyETP_Haude",
        author="Peter Krause",
        description="Calculates potential ETP after Penman-Monteith"
        )
    
    public class ABCETHaude extends JAMSComponent {
    
        
    /*
     *  Component variables
     */
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "Current time"
            )
            public Attribute.Calendar time;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "state variable mean temperature"
            )
            public Attribute.Double temperature;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "state variable relative humidity"
            )
            public Attribute.Double rhum;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "multiplier to adapt et rates"
            )
            public Attribute.Double et_adaptation;
    
        @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            description = "daily potential ET [mm/d]"
            )
            public Attribute.Double pET;
    
    
    
    
    /*
     *  Component run stages
     */
    
    public void init() throws Attribute.Entity.NoSuchAttributeException, IOException {
        
    }
    
    public void run() throws Attribute.Entity.NoSuchAttributeException, IOException {
           double[] haudeFactors ={0.08,0.04,0.14,0.35,0.39,0.34,0.31,0.25,0.2,0.13,0.07,0.05}; 
           int month = time.get(Attribute.Calendar.MONTH);
           double temp = temperature.getValue();
           double rh = rhum.getValue();
           double hf = haudeFactors[month];
           double est = 0.6108 * Math.exp((17.27 * temp)/(237.3 + temp));
           //kPa -> hPa
           est = 10 * est;
           
           
           double pETP = est * (1 - (rh/100.)) * hf;
           
           pETP = pETP * this.et_adaptation.getValue();
                   
           
           //avoiding negative potETPs
           if(pETP < 0){
               pETP = 0;
           }
           
           int days = time.getActualMaximum(Attribute.Calendar.DAY_OF_MONTH);
           
           //conversion from daily to hourly values
           pETP = pETP * days;
           
           this.pET.setValue(pETP);
        
    }
    
    public void cleanup()  throws IOException {
        
    }
}
