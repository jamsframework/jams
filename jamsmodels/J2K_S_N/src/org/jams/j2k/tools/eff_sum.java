/*
 * newJAMSComponent.java
 * Created on 30. September 2008, 18:51
 *
 * This file is part of JAMS
 * Copyright (C) 2005 FSU Jena, c8fima
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

package org.jams.j2k.tools;

import jams.data.*;
import jams.model.*;


/**
 *
 * @author c8fima
 */
@JAMSComponentDescription(
        title="Sum up values",
        author="Manfred Fink",
        description="Sum up values (cleanup stage)"
        )
public class eff_sum extends JAMSComponent {

    /*
     *  Component variables
     */
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            description = "average or sum of objective function"
            )
            public Attribute.Double obj_sum;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "values objective function"
            )
            public Attribute.Double[] obj_values;
    
     @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "aggregation mode: 1 = average, 2 = sum"
            )
            public Attribute.Integer mode;
    
    
    /*
     *  Component run stages
     */
    
    public void init() {
        
    }

    public void run() {
      
    }

    public void cleanup() {
        double sum;
        int i;
      
        int length = obj_values.length;
     
                
      
      sum = 0;
            
      for (i = 0; i < length; i++){
          
          sum = sum +  obj_values[i].getValue();
          
      }
       
      if (mode.getValue() == 1){
        obj_sum.setValue(sum / length);  
      } else{
      obj_sum.setValue(sum);
      }
        
    }
}
