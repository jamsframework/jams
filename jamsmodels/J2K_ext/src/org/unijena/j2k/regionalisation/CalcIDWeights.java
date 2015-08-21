/*
 * CalcNidwWeights.java
 * Created on 27. Januar 2006, 09:50
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

package org.unijena.j2k.regionalisation;

import jams.data.*;
import jams.model.*;
import org.unijena.j2k.statistics.IDW;

/**
 *
 * @author Peter Krause
 */
@JAMSComponentDescription(
        title="CalcIDWeights",
        author="Peter Krause",
        description="Calculates inverse distance weights for the regionalisation procedure"
        )
        public class CalcIDWeights extends JAMSComponent {
    
    /*
     *  Component variables
     */
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "entity x-coordinate"
            )
            public Attribute.Double entityX;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "entity y-coordinate"
            )
            public Attribute.Double entityY;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "Array of station's x coordinates"
            )
            public Attribute.DoubleArray statX;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "Array of station's y coordinates"
            )
            public Attribute.DoubleArray statY;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "Power of IDW function"
            )
            public Attribute.Double pidw;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            description = "weights for IDW part of regionalisation"
            )
            public Attribute.DoubleArray statIDWeights;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            description = "position array to determine best weights"
            )
            public Attribute.IntegerArray statOrder;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "Doug Boyle's famous function"
            )
            public Attribute.Boolean equalWeights; 
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "Calculation with geographical coordinates LL"
            )
            public Attribute.Boolean latLong; 
        
    IDW idw = new IDW();
    
    public void run() throws Attribute.Entity.NoSuchAttributeException{        
        double weights[] = statIDWeights.getValue();
        int wA[] = statOrder.getValue();
        
        int n = statX.getValue().length;
        
        if (weights == null || weights.length != n) {
            weights = new double[n];
        }
        if (wA == null || wA.length != n) {
            wA = new int[n];
        }
        
        if(equalWeights == null || !equalWeights.getValue()){            
            if(latLong == null || !latLong.getValue()){
                idw.init(statX.getValue(), statY.getValue(), null, (int)pidw.getValue(), IDW.Projection.ANY);                
            }
            else{
                idw.init(statX.getValue(), statY.getValue(), null, (int)pidw.getValue(), IDW.Projection.LATLON);                
            }
            idw.getIDW(entityX.getValue(), entityY.getValue(), null, 0);
            
            System.arraycopy(idw.getWeights(), 0, weights, 0, n);
            System.arraycopy(idw.getWeightOrder(), 0, wA, 0, n);
        }
        else if(equalWeights.getValue()){
            for (int i=0;i<n;i++){
                weights[i] = 1. / n;
                wA[i] = i;
            }
        }
        statIDWeights.setValue(weights);
        statOrder.setValue(wA);
    }
    
    public void cleanup() throws Attribute.Entity.NoSuchAttributeException{
        int nstat = statIDWeights.getValue().length;
        double[] sw = new double[nstat];
        for(int i = 0; i < nstat; i++)
            sw[i] = 0;
        
        statIDWeights.setValue(sw);
        
    }
}
