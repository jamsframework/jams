/*
 * InitJ2KProcessLumpedSoilWaterStates.java
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

package org.unijena.j2k.soilWater;

import jams.data.*;
import jams.model.*;

/**
 *
 * @author Peter Krause
 */
@JAMSComponentDescription(
        title="InitJ2KProcessLumpedSoilWaterStates",
        author="Peter Krause",
        description="Initalises the states of the lumpedSoilWater module",
        version="1.0_0",
        date="2010-10-29"
        )
        public class InitJ2KProcessLumpedSoilWaterStates extends JAMSComponent {
    
    /*
     *  Component variables
     */
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "The model entity set"
            )
            public Attribute.EntityCollection entities;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "attribute area",
            unit="m^2"
            )
            public Attribute.Double area;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "field capacity adaptation factor",
            unit="n/a",
            lowerBound = 0,
            //upperBound = 1000000,
            defaultValue = "1.0"
            )
            public Attribute.Double FCAdaptation;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "air capacity adaptation factor",
            unit="n/a",
            lowerBound = 0,
            //upperBound = 1000000,
            defaultValue = "1.0"
            )
            public Attribute.Double ACAdaptation;
    
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "HRU statevar rooting depth",
            unit="dm",
            lowerBound = 0
            //upperBound = 1000000
            )
            public Attribute.Double rootDepth;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            description = "HRU attribute maximum MPS",
            unit="L",
            lowerBound = 0
            //upperBound = 1000000
            )
            public Attribute.Double maxMPS;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            description = "HRU attribute maximum LPS",
            unit="L",
            lowerBound = 0
            //upperBound = 1000000
            )
            public Attribute.Double maxLPS;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            description = "HRU state var actual MPS",
            unit="L",
            lowerBound = 0
            //upperBound = 1000000
            )
            public Attribute.Double actMPS;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            description = "HRU state var actual LPS",
            unit="L",
            lowerBound = 0
            //upperBound = 1000000
            )
            public Attribute.Double actLPS;
   
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            description = "HRU state var saturation of MPS",
            unit="n/a",
            lowerBound = 0,
            upperBound = 1
            )
            public Attribute.Double satMPS;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            description = "HRU state var saturation of LPS",
            unit="n/a",
            lowerBound = 0,
            upperBound = 1
            )
            public Attribute.Double satLPS;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            description = "HRU state var saturation of whole soil",
            unit="n/a",
            lowerBound = 0,
            upperBound = 1
            )
            public Attribute.Double satSoil;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "start saturation of LPS",
            unit="n/a",
            lowerBound = 0,
            upperBound = 1,
            defaultValue = "0.0"
            )
            public Attribute.Double satStartLPS;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "start saturation of MPS",
            unit="n/a",
            lowerBound = 0,
            upperBound = 1,
            defaultValue = "0.0"
            )
            public Attribute.Double satStartMPS;
    
    
    
    
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
            try {
                double mpsVal = entity.getDouble(mpsDesc);
                mxMPS = mxMPS + mpsVal;
            } catch (Attribute.Entity.NoSuchAttributeException nsae) {
                getModel().getRuntime().sendErrorMsg(nsae.getMessage() + "\n"
                        + "This problem typically occurs if the root depht of "
                        + "current land-use is higer than the soil depht.\n"
                        + "Please provide additional field capacity (fc_*) "
                        + "columns in your soil parameter file or reduce the "
                        + "root depht!");
            }
        }
        mxMPS = mxMPS * this.area.getValue();
        mxMPS = mxMPS * this.FCAdaptation.getValue();
        if (mxMPS == 0){
            mxMPS = 1;
        }
        
        double mxLPS = entity.getDouble("aircap") * area.getValue();
        mxLPS = mxLPS * this.ACAdaptation.getValue();    
        if (mxLPS == 0){
            mxLPS = 1;
        }



        if(satStartLPS != null){
        	this.actLPS.setValue(mxLPS * this.satStartLPS.getValue());
        }
        
        if(satStartMPS != null){
        	this.actMPS.setValue(mxMPS * this.satStartMPS.getValue());
        }
        
        this.maxMPS.setValue(mxMPS);
        this.maxLPS.setValue(mxLPS);
        this.satMPS.setValue(this.actMPS.getValue()/mxMPS);
        this.satLPS.setValue(this.actLPS.getValue()/mxLPS);
        this.satSoil.setValue((this.actMPS.getValue()+this.actLPS.getValue()) / (mxMPS+mxLPS));
    }
    
    public void cleanup() {
        
    }
    
    
}
