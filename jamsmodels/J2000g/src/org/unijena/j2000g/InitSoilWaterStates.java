/*
 * InitSoilWaterStates.java
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

package org.unijena.j2000g;

import jams.data.*;
import jams.model.*;

/**
 *
 * @author Peter Krause
 */
@JAMSComponentDescription(
        title="InitSoilWaterStates",
        author="Peter Krause",
        description="Defines soil water attributes for each HRU"
        )
        public class InitSoilWaterStates extends JAMSComponent {
    
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
            description = "infiltration capacity adaptation",
            defaultValue = "1.0"
            )
            public Attribute.Double maxInfAdaptation;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = "infiltration capacity adaptation",
            defaultValue = "Infinity"
            )
            public Attribute.Double maxInf;
    
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
            description = "HRU attribute maximum percolation rate"
            )
            public Attribute.Double maxPerc;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            description = "HRU state var actual MPS"
            )
            public Attribute.Double actMPS;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "relative start content"
            )
            public Attribute.Double initMPS;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "maximum percolation adaptation factor"
            )
            public Attribute.Double maxPercAdaptation;
    
    
    
    /*
     *  Component run stages
     */
    
    public void init() {
        
        
    }
    
    public void run() {
        
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
        this.maxInf.setValue(this.maxInf.getValue()*this.maxInfAdaptation.getValue());
        this.actMPS.setValue(initMPS.getValue()*mxMPS);
        
        double mxPerc = entity.getDouble("mxPerc") * this.maxPercAdaptation.getValue() * this.area.getValue();
        
        this.maxPerc.setValue(mxPerc);
        
        
    }
    
    public void cleanup() {
        
    }
    
    
}
