/*
 * DoubleTransferFraction.java
 * Created on 27. September 2012, 22:02
 *
 * This file is part of JAMS
 * Copyright (C) FSU Jena
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

package jams.components.datatransfer;

import jams.data.*;
import jams.model.*;

/**
 *
 * @author Sven Kralisch
 */
@JAMSComponentDescription(
        title="DoubleTransferFraction",
        author="Sven Kralisch",
        description="Component for simply transferring multiple double "
        + "attributes) to a target entity. In addition, options to transfer "
        + "only fractions of source data and optional modification of source "
        + "data are possible. Can be used to implement a simple routing "
        + "mechanism (e.g. HRU to HRU or HRU to reach) by taking a source "
        + "entity's double data and moving it to specified.",
        version="1.0_0",
        date="2012-09-27"
        )
        public class DoubleTransferFraction extends JAMSComponent {
    
    /*
     *  Component variables
     */
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "Target entity"
            )
            public Attribute.Entity target;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = "Data to be transferred"
            )
            public Attribute.Double[] values;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,    
            description = "Target entity's receiving attributes"
            )
            public Attribute.String[] inNames;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,    
            description = "Fractions of data to be transferred"
            )
            public Attribute.Double[] fraction;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,    
            description = "Remove transferred data from source values?",
            defaultValue= "false"
            )
            public Attribute.Boolean removeFromSource;    
    /*
     *  Component run stages
     */
    
    public void run() {

        if(target.getValue() != null){
            for (int i = 0; i < values.length; i++) {            
                double x = values[i].getValue() * fraction[i].getValue();
                target.setDouble(inNames[i].getValue(),target.getDouble(inNames[i].getValue()) + x);
                if (removeFromSource.getValue()) {
                    values[i].setValue(values[i].getValue() - x);
                }
            }   
        }
        
    }
}
