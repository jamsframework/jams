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

package org.jams.j2k.s_n;

import jams.data.*;
import jams.model.*;

/**
 *
 * @author c0krpe
 */
@JAMSComponentDescription(
        title="J2KOutsideadd",
        author="Manfred Fink",
        description="Adds values to an existing variable"
        )
        public class J2KOutsideadd extends JAMSComponent {
    
    /*
     *  Component variables
     */
  

    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "the inflow from the data file or another model"
            )
            public Attribute.Double inflow;
      
   
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = "the actual storage of the additional inflow of the specific entitie"
            )
            public Attribute.Double actAddIn;
  

    
    public void run() {
             
        this.actAddIn.setValue(actAddIn.getValue() + inflow.getValue());
       
            
        
        
        
    }
    

    

}
