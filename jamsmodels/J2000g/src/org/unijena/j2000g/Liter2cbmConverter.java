/*
 * J2KProcessReachRouting.java
 * Created on 28. November 2005, 10:01
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

package org.unijena.j2000g;

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
        public class Liter2cbmConverter extends JAMSComponent {
    
    /*
     *  Component variables
     */        
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "input"
            )
            public Attribute.Double[] in;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            description = "output in cms"
            )
            public Attribute.Double[] out;
            
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            description = "temporal resolution [h;d;m]"
            )
            public Attribute.String tempRes;
    
        
    @Override
    public void run() {
        //conversion from liters to m^3/time
        for (int i=0;i<in.length;i++){
            if(tempRes.getValue().equalsIgnoreCase("h")){
                this.out[i].setValue((in[i].getValue()) / (3600. * 1000));
            }                
            if(tempRes.getValue().equalsIgnoreCase("d")){
                this.out[i].setValue((in[i].getValue()) / (86400. * 1000));
            }                
            if(tempRes.getValue().equalsIgnoreCase("m")){
                this.out[i].setValue((in[i].getValue()) / (30. * 86400. * 1000));
            }                
        }        
    }   
}
