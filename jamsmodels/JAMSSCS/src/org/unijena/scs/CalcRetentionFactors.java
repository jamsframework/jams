/*
 * CalcRetentionFactors.java
 * Created on 17. July 2006, 17:15
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
package org.unijena.scs;
import jams.data.Attribute;
import jams.model.JAMSComponent;
import jams.model.JAMSComponentDescription;
import jams.model.JAMSVarDescription;

/**
 * Calculates the recession coefficients k1 and k2
 * for the unit hydrographs, based on a length-slope factor of the
 * main stream in the catchment.
 * In addition, the factor beta for the distribution of the effective
 * precipitation for the two unit-hydrographs is calculated here, also
 * based on the length-slope factor.
 * @author P. Krause
 */

@JAMSComponentDescription(
        title="Calc retention factors",
        author="Peter Krause",
        description="Calculates the recession coefficients k1 and k2" +
        "for the unit hydrographs, based on a length-slope factor of the" +
        "main stream in the catchment." +
        "In addition, the factor beta for the distribution of the effective" +
        "precipitation for the two unit-hydrographs is calculated here, also" +
        "based on the length-slope factor"
        )
public class CalcRetentionFactors extends JAMSComponent {
    
    /**
     * the distribution factor beta which is responsible for the distribution of the
     * effective precipitation to the two Nash-cascades<br>
     * access: WRITE<br> 
     * update: RUN<br> 
     * unit: n/a
     */
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            description = "distribution factor beta"
            )
            public Attribute.Double beta;
    
    /**
     * the retention coefficient for the fast Nash-cascade<br>
     * access: WRITE<br> 
     * update: RUN<br> 
     * unit: n/a
     */
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            description = "recession factor k1"
            )
            public Attribute.Double k1;
    
    /**
     * the retention coefficient for the slow Nash-cascade<br>
     * access: WRITE<br> 
     * update: RUN<br> 
     * unit: n/a
     */
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            description = "recession factor k2"
            )
            public Attribute.Double k2;
    
    /**
     * the slope of the main channel<br>
     * access: READ<br> 
     * update: RUN<br> 
     * unit: %
     */
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "stream slope"
            )
            public Attribute.Double streamSlope;
    
    /**
     * the length of the main channel<br>
     * access: READ<br> 
     * update: RUN<br> 
     * unit: km
     */
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "stream length",
            unit = "km^2"
            )
            public Attribute.Double streamLength;
    
    
    /**
     * the components run method
     * @throws org.unijena.jams.data.Attribute.Entity.NoSuchAttributeException thrown when a model entity tries to access a non existent attribute
     */
    public void run() throws Attribute.Entity.NoSuchAttributeException {
        /*the length slope factor of the catchment */
        double lengthSlope = this.streamLength.getValue() / Math.sqrt(this.streamSlope.getValue());
        
        /*factor beta for precip distribution*/
        double beta = 0;
        if(lengthSlope < 10.0){
            beta = 1 - 0.02425 * Math.pow(Math.log(lengthSlope),3.2444);
        }else{
            beta = 0.1 + 3.91 / (Math.pow(lengthSlope, 0.86));
        }
        
        /*the recession coefficients k1 and k2 */
        double k1 = 0.555 / Math.pow(lengthSlope, 0.61) + 0.511 * Math.log(lengthSlope) - 0.355;
        double k2 = 3 * Math.pow(k1, 1.3);
        
        this.beta.setValue(beta);
        this.k1.setValue(k1);
        this.k2.setValue(k2);
    }
    
}
