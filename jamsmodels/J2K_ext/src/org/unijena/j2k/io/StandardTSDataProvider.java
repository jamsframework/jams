/*
 * StandardTSDataProvider.java
 * Created on 30. November 2005, 09:59
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
 * @author c0krpe
 */
@JAMSComponentDescription(
        title="Title",
        author="Author",
        description="Description"
        )
        public class StandardTSDataProvider extends JAMSComponent {
    
    /*
     *  Component variables
     */
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "Array of data values for current time step"
            )
            public Attribute.DoubleArray dataArray;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            description = "Spatial attribute to be set"
            )
            public Attribute.Double[] attribute;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "The column of the relevant data in the data file"
            )
            public Attribute.Integer[] column;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "constant correction factor",
            defaultValue = "1.0"
            )
            public Attribute.Double corr_factor;

    
    /*
     *  Component run stages
     */
    
    public void run() {
        int j=0;
        for (Attribute.Integer c : column){
            int i = c.getValue();            
            attribute[j].setValue(dataArray.getValue()[i - 1] * corr_factor.getValue());
            j++;
        }        
    }
    
}
