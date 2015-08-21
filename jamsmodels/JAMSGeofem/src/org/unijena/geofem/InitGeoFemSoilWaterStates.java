/*
 * InitGeoFemSoilWaterStates.java
 * Created on 25. November 2005, 13:21
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

package org.unijena.geofem;

import jams.data.*;
import jams.model.*;

/**
 *
 * @author Peter Krause
 */
@JAMSComponentDescription(
        title="InitGeoFemSoilWaterStates",
        author="Peter Krause",
        description="Defines soil water attributes for each HRU"
        )
        public class InitGeoFemSoilWaterStates extends JAMSComponent {
    
    /*
     *  Component variables
     */
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "The current hru entity"
            )
            public Attribute.EntityCollection entities;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "attribute area"
            )
            public Attribute.Double area;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "field capacity adaptation factor"
            )
            public Attribute.Double FCAdaptation;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "HRU statevar rooting depth"
            )
            public Attribute.Double rootDepth;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            description = "HRU attribute maximum MPS"
            )
            public Attribute.Double maxMPS;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            description = "HRU state var actual MPS"
            )
            public Attribute.Double actMPS;
    
    
    
    
    
    /*
     *  Component run stages
     */
    
    public void init() throws Attribute.Entity.NoSuchAttributeException {
        
        
    }
    
    public void run() throws Attribute.Entity.NoSuchAttributeException {
        
        Attribute.Entity entity = entities.getCurrent();
        
        double rootDepth = this.rootDepth.getValue();
        double mxMPS = 0;
        String aNameFC = "fc_";
        for(int d = 0; d < rootDepth; d++){
            int count = d + 1;
            String mpsDesc = aNameFC + count;
            double mpsVal = entity.getDouble(mpsDesc);
            mxMPS = mxMPS + mpsVal;
            
        }
        
        mxMPS = mxMPS * this.area.getValue();
        mxMPS = mxMPS * this.FCAdaptation.getValue();
        
        this.maxMPS.setValue(mxMPS);
        
    }
    
    public void cleanup() {
        
    }
    
    
}
