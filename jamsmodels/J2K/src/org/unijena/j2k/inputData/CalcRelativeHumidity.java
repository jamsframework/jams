/*
 * CalcRelativeHumidity.java
 * Created on 24. November 2005, 09:48
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

package org.unijena.j2k.inputData;

import jams.data.*;
import jams.model.*;

/**
 *
 * @author Peter Krause
 */
@JAMSComponentDescription(
        title="CalcRelativeHumidity",
        author="Peter Krause",
        description="Calculates relative humidity from temperature and absolute humidity",
        version="1.0_0",
        date="2011-05-30"
        )
        public class CalcRelativeHumidity extends JAMSComponent {
    
    /*
     *  Component variables
     */
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "state variable mean tempeature",
            unit = "°C"
            )
            public Attribute.Double tmean;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "state variable absolute humidity",
            unit = "g / m³"
            )
            public Attribute.Double ahum;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            description = "state variable relative humidity",
            unit = "%"
            )
            public Attribute.Double rhum;
      
    public void run() {
        
        double maxHum = org.unijena.j2k.physicalCalculations.ClimatologicalVariables.calc_maxHum(tmean.getValue());
        double rh = (ahum.getValue() / maxHum) * 100;
        
        //rhum should not be larger than 100%
        if(rh > 100)
            rh = 100;
        
        rhum.setValue(rh);
    }    
}
