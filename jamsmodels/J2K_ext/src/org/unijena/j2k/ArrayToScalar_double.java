/*
 * ArrayToScalar.java
 * Created on 04.04.2013, 17:23:31
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

package org.unijena.j2k;

import jams.data.*;
import jams.model.*;

/**
 *
 * @author Sven Kralisch <sven.kralisch at uni-jena.de>
 */
 @JAMSComponentDescription(
        title="ArrayToScalar",
        author="Sven Kralisch",
        description="Extracts single scalar doubles from an array of double values",
        date = "2013-09-13",
        version = "1.0_1"
        )
@VersionComments(entries = {
    @VersionComments.Entry(version = "1.0_0", comment = "Initial version"),
    @VersionComments.Entry(version = "1.0_1", comment = "Extended to handle multiple values")
}) 
public class ArrayToScalar_double extends JAMSComponent {

    /*
     *  Component attributes
     */
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,  
            description = "array of values to extract from" 
            )
            public Attribute.DoubleArray dataArray;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,  
            description = "the index of the value to be extracted" 
            )
            public Attribute.Double[] arrayIndex;      
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,  
            description = "optional correction factor",
            defaultValue = "1"
            )
            public Attribute.Double factor;    
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,  
            description = "extracted value" 
            )
            public Attribute.Double[] dataValue;

    @Override
    public void run() {
        for (int i = 0; i < arrayIndex.length; i++) {
            int j =  (int)arrayIndex[i].getValue();
            dataValue[i].setValue(dataArray.getValue()[j] * factor.getValue());
        }
    }

}