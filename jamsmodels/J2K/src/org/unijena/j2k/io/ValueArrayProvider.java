/*
 * TimeSeriesCapturer.java
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

package org.unijena.j2k.io;

import jams.data.*;
import jams.model.*;

/**
 *
 * @author Peter Krause
 */
@JAMSComponentDescription(
title="Value array provider",
        author="Peter Krause",
        description="Retreives single variables from a context and stores them" +
        "in a value array for later processing"
        )
        public class ValueArrayProvider extends JAMSComponent {
    
    /*
     *  Component variables
     */
    
   @JAMSVarDescription(
    access = JAMSVarDescription.AccessType.READ,
            description = "the timeSeries values to capture"
            )
            public Attribute.Double theValue;
    
    @JAMSVarDescription(
    access = JAMSVarDescription.AccessType.READWRITE,
            description = "the captured time series"
            )
            public Attribute.DoubleArray theValueArray;
    
   
   
    
    /*
     *  Component run stages
     */
    
    public void init() {
        
        
    }
    
    public void run() {
        double[] newArray = null;
        double[] dataArray = this.theValueArray.getValue();
        if(dataArray == null){
            newArray = new double[1];
            newArray[0] = this.theValue.getValue();
        }
        else{
            newArray = new double[dataArray.length+1];
            for(int i = 0; i < dataArray.length; i++)
                newArray[i] = dataArray[i];
            newArray[dataArray.length] = this.theValue.getValue();
        }
        theValueArray.setValue(newArray);
    }
    
    public void cleanup() {
        
    }
}
