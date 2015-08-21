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
        title="CalcNidwWeights",
        author="Peter Krause",
        description="Calculates weights for the regionalisation procedure"
        )
        public class CalcNidwWeights extends JAMSComponent {
    
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
            description = "Number of IDW stations"
            )
            public Attribute.Integer nidw;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "Power of IDW function"
            )
            public Attribute.Double pidw;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            description = "weights for IDW part of regionalisation"
            )
            public Attribute.DoubleArray statWeights;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "Doug Boyle's famous function"
            )
            public Attribute.Boolean equalWeights;  
    
    /*
     *  Component run stages
     */       
    IDW idw = new IDW();
    public void run() throws Attribute.Entity.NoSuchAttributeException{                
        double w[] = statWeights.getValue();
        
        int n = statX.getValue().length;
        
        if (w == null || w.length != n){
            w = new double[n];
        }
        
        if(equalWeights == null || !equalWeights.getValue()){
            idw.init(statX.getValue(), statY.getValue(), null, (int)pidw.getValue(), IDW.Projection.ANY);
            idw.getIDW(entityX.getValue(), entityY.getValue(), null, nidw.getValue());
            
            System.arraycopy(idw.getWeights(), 0, w, 0, n);
            
        	//idwWeights.setValue(org.unijena.j2k.statistics.IDW2.calcNidwWeights(entityX.getValue(), entityY.getValue(), statX.getValue(), statY.getValue(), pidw.getValue(), nidw.getValue()));
        }
        else if(equalWeights.getValue()){
            for (int i=0;i<n;i++){
                w[i] = 1./(double)n;
            }
        	//idwWeights.setValue(org.unijena.j2k.statistics.IDW2.equalWeights(nidw.getValue()));
        }
        	
        statWeights.setValue(w);
        
    }
    
    @Override
    public void cleanup() throws Attribute.Entity.NoSuchAttributeException{
        int nstat = statWeights.getValue().length;        
        for(int i = 0; i < nstat; i++){
            statWeights.getValue()[i] = 0; 
        }
    }
}
