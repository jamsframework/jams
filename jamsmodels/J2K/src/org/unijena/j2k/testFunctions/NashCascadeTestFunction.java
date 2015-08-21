/*
 * LinearTestModel.java
 * Created on 08. December 2006, 17:15
 *
 * This file is part of JAMS
 * Copyright (C) 2005 S. Kralisch and P. Krause
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

package org.unijena.j2k.testFunctions;

import jams.model.*;
import jams.data.*;

/**
 *
 * @author S. Kralisch
 */
@JAMSComponentDescription(
        title="Hosaki Test Function",
        author="Peter Krause",
        description="A test function for optimizers which has a global and a local optimum"
        )
        
public class NashCascadeTestFunction extends JAMSComponent {
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "number of storages"
            )
            public Attribute.Double para_n;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "recession coefficient k"
            )
            public Attribute.Double para_k;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "input reduction value"
            )
            public Attribute.Double para_r;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = "parameter e"
            )
            public Attribute.DoubleArray storages;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "the input value"            
            )
            public Attribute.Double input;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            description = "the simulated response"            
            )
            public Attribute.Double simulation;
    
    public void init(){
        int n = (int)this.para_n.getValue();
        double[] stor = new double[n];
        for(int i = 0; i < n; i++)
            stor[i] = 0;
        
        this.storages.setValue(stor);
        
    }
    
    public void run() {
        int n = (int)this.para_n.getValue();
        double inp = this.input.getValue() * this.para_r.getValue();
        double k = this.para_k.getValue();
        double[] stor = this.storages.getValue();
        double y = nash(inp, k, stor);
        
        this.simulation.setValue(y);
    }
    
    public double nash(double inp, double k, double[] stor){
        int n = stor.length;
        double[] outflow = new double[n];
        double[] xend = new double[n+1];
        //first storage gets input
        stor[0] = stor[0] + inp;
        
        for(int i = 0; i < n; i++){
            try {
                outflow[i] = k * stor[i];
            } catch(java.lang.ArrayIndexOutOfBoundsException e) {
                System.out.println("Some error occured");
            }
            
            stor[i] = stor[i] - outflow[i];
            
            if(i > 0){
                //other storages get input from storage above
                stor[i] = stor[i] + outflow[i-1];
            }
        }
        //return outflow from last storage
        return(outflow[n-1]);
    }
    
    
}
