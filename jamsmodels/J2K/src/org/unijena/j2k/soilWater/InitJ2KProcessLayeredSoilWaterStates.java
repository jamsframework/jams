/*
 * InitJ2KProcessLayeredSoilWaterStates.java
 * Created on 25. November 2005, 13:21
 *
 * This file is part of JAMS
 * Copyright (C) 2005 FSU Jena, Peter Krause
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
        title="InitJ2KProcessLayeredSoilWaterStates",
        author="Peter Krause",
        description="Calculates soil water balance for each HRU without vertical layers"
        )
        public class InitJ2KProcessLayeredSoilWaterStates extends JAMSComponent {
    
    /*
     *  Component variables
     */
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "The hru entities"
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
            description = "air capacity adaptation factor"
            )
            public Attribute.Double ACAdaptation;
    
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "HRU statevar rooting depth"
            )
            public Attribute.Double rootDepth;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "number of horizons"
            )
            public Attribute.Double horizons;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            description = "soil horizon depths"
            )
            public Attribute.DoubleArray depth_h;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            description = "HRU attribute maximum MPS"
            )
            public Attribute.DoubleArray maxMPS_h;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            description = "HRU attribute maximum LPS"
            )
            public Attribute.DoubleArray maxLPS_h;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            description = "HRU state var actual MPS"
            )
            public Attribute.DoubleArray actMPS_h;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            description = "HRU state var actual LPS"
            )
            public Attribute.DoubleArray actLPS_h;
   
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            description = "HRU state var saturation of MPS"
            )
            public Attribute.DoubleArray satMPS_h;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            description = "HRU state var saturation of LPS"
            )
            public Attribute.DoubleArray satLPS_h;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            description = "HRU state var saturation of whole soil"
            )
            public Attribute.Double satSoil_h;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            description = "RD2 inflow"
            )
            public Attribute.DoubleArray inRD2_h;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "initial saturation for all horizons"
            )
            public Attribute.Double initSat;
    
   
    /*
     *  Component run stages
     */
    
    public void init() {
        
        
    }
    
    public void run() {
        Attribute.Entity entity = entities.getCurrent();
        int horizons = (int)this.horizons.getValue();
        double rootDepth = this.rootDepth.getValue() * 10;
        double remRD = rootDepth;
        double[] mxMPS = new double[horizons];
        double[] mxLPS = new double[horizons];
        double[] acMPS = new double[horizons];
        double[] acLPS = new double[horizons];
        double[] stMPS = new double[horizons];
        double[] stLPS = new double[horizons];
        double[] inRD2 = new double[horizons];
        double[] depth = new double[horizons];
        String aNameFC = "fieldcapacity_h";
        String aNameAC = "aircapacity_h";
        String depthName = "depth_h";
       
        for(int h = 0; h < horizons; h++){
            depth[h] = entity.getDouble(depthName+h);
/*             if(remRD >= depth[h] && remRD > 0){
                mxMPS[h] = entity.getDouble(aNameFC+h);
                mxMPS[h] = mxMPS[h] * this.area.getValue();
                remRD = remRD - depth[h];
            }
            else if(remRD > 0){
                double frac = remRD / depth[h];
                mxMPS[h] = entity.getDouble(aNameFC+h) * frac;
                mxMPS[h] = mxMPS[h] * this.area.getValue();
                remRD = remRD - depth[h];
            }
*/            
            acMPS[h] = 0;
            
            mxMPS[h] = entity.getDouble(aNameFC+h) * area.getValue() * this.FCAdaptation.getValue();    
            mxLPS[h] = entity.getDouble(aNameAC+h) * area.getValue() * this.ACAdaptation.getValue();
            acMPS[h] = initSat.getValue() * mxMPS[h];
            stMPS[h] = initSat.getValue();
            stLPS[h] = 0;
            
            inRD2[h] = 0;
        }
   
        this.maxMPS_h.setValue(mxMPS);
        this.maxLPS_h.setValue(mxLPS);
        this.actMPS_h.setValue(acMPS);
        this.actLPS_h.setValue(acLPS);
        this.satMPS_h.setValue(stMPS);
        this.satLPS_h.setValue(stLPS);
        this.inRD2_h.setValue(inRD2);
        this.depth_h.setValue(depth);
        this.satSoil_h.setValue(0);
        
        
    }
    
    public void cleanup() {
        
    }
    
    
}
