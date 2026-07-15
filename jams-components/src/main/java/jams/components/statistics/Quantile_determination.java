/*
 * EMD.java
 * Created on 14.07.2016, 20:59:38
 *
 * This file is part of JAMS
 * Copyright (C) FSU Jena
 *
 * JAMS is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public License
 * as published by the Free Software Foundation; either version 3
 * of the License, or (at your option) any later version.
 *
 * JAMS is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with JAMS. If not, see <http://www.gnu.org/licenses/>.
 *
 */
package jams.components.statistics;

import jams.JAMS;
import jams.components.analysis.*;
import jams.data.*;
import jams.model.*;
import java.util.Arrays;
import java.util.Locale;


/**
 *
 * @author Manfred Fink <manfred.fink at uni-jena.de>
 */
@JAMSComponentDescription(
        author = "Manfred Fink)",
        description = "Calculates a quantile according to a given percentage",
        date = "2018-07-14",
        version = "1.0_0"
)
@VersionComments(entries = {
    @VersionComments.Entry(version = "1.0_0", comment = "Initial version")
})
public class Quantile_determination extends JAMSComponent {

    /*
     *  Component attributes
     */
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "Value"
    )
    public Attribute.Double input;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = "current step as interger"
    )
    public Attribute.Integer timestep;
    
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            description = "quantile value result (only available in the cleanup stage)"
    )
    public Attribute.Double output;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "Time interval"
    )
    public Attribute.TimeInterval timeInterval;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = "Value array"
    )
    public Attribute.DoubleArray ValueArray;



    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "quantile in %",
            defaultValue = "0"
    )
    public Attribute.Double Quantile;



    
    private long maxSteps;

    @Override
    public void init() {
        maxSteps = timeInterval.getNumberOfTimesteps();
        double[] ValArray = new double[(int)maxSteps];
        ValueArray.setValue(ValArray);
        timestep.setValue(0);
        output.setValue(0); 
        
        
    }

    @Override
    public void run() {       
        maxSteps = timeInterval.getNumberOfTimesteps();
        int i = timestep.getValue();        
        double[] tempArray = ValueArray.getValue();
        tempArray[i] = input.getValue();
        ValueArray.setValue(tempArray);
        i = i + 1;
        timestep.setValue(i);
        output.setValue(0); 
        
        if (i ==  maxSteps){
            Arrays.sort(tempArray);
            Double quantilenumber = maxSteps * (Quantile.getValue()/100);
            int  j = (int)Math.round(quantilenumber);
            output.setValue(tempArray[j]);
        }
        
        
        
        
    }

    @Override
    public void cleanup() {        
        
    }

    /*
     *  Component run stages
     */


}
