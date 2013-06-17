/*
 * TemporalSumAggregator.java
 * Created on 19. Juli 2006, 11:57
 *
 * This file is part of JAMS
 * Copyright (C) 2005 FSU Jena
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public License
 * as published by the Free Software Foundation; either version 3
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, see <http://www.gnu.org/licenses/>.
 *
 */
package jams.components.aggregate;

import jams.model.*;
import jams.data.*;
import java.util.GregorianCalendar;

/**
 *
 * @author S. Kralisch
 */
@JAMSComponentDescription(title = "TemporalPeriodeAggregator",
author = "Christian Fischer",
date = "2013-05-13",
version = "1.0_0",
description = "Component to calculate monthly averages")
public class SubTimeStepAggregator extends JAMSComponent {

    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
    description = "Current time")
    public Attribute.Boolean subTimeStepStarted;
    
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
    description = "Current time")
    public Attribute.Double currentTimestepCount;
                
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
    description = "The value(s) to be aggregated")
    public Attribute.Double[] value;
    
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
    description = "A weight to be used to calculate the weighted aggregate",
    defaultValue="1")
    public Attribute.Double weight;
    
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READWRITE,
    description = "The resulting weighted aggregate(s) of the given values")
    public Attribute.Double[] sum;
            
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
    description = "Mode to aggregate 0 = sum, 1 = average, 2 = minimum, 3 = maximum",
    defaultValue = "1")
    public Attribute.Integer aggregationMode;
                            
    @Override
    public void run() { 
        
        if (subTimeStepStarted.getValue()){
            for (int i=0;i<sum.length;i++){
                switch(aggregationMode.getValue()){
                    case 0: sum[i].setValue(0.0); break;
                    case 1: sum[i].setValue(0.0); break;
                    case 2: sum[i].setValue(Double.POSITIVE_INFINITY); break;
                    case 3: sum[i].setValue(Double.NEGATIVE_INFINITY); break;
                }
            }
        }

        double w = weight.getValue();
        double T = currentTimestepCount.getValue();
        for (int i=0;i<value.length;i++){
            switch(aggregationMode.getValue()){
                //sum
                case 0: sum[i].setValue(value[i].getValue()/w+sum[i].getValue()); break;
                //average
                case 1: sum[i].setValue(value[i].getValue()/(T*w)+sum[i].getValue()); break;
                //min
                case 2: sum[i].setValue(Math.min(value[i].getValue()/w,sum[i].getValue())); break;
                //max
                case 3: sum[i].setValue(Math.max(value[i].getValue()/w,sum[i].getValue())); break;
            }             
        }                
    }        
}
