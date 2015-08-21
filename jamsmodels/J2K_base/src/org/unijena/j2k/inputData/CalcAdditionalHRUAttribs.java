package org.unijena.j2k.inputData;
/*
 * CalcAdditionalHRUAttribs.java
 * Created on 24. November 2005, 11:46
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

import jams.JAMS;
import jams.data.*;
import jams.model.*;

/**
 *
 * @author Peter Krause
 */
@JAMSComponentDescription(
        title="CalcAdditionalHRUAttribs",
        author="Peter Krause",
        description="Calculates additional attributes from existent ones"
        )
        public class CalcAdditionalHRUAttribs extends JAMSComponent {
    
    /*
     *  Component variables
     */
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "The current hru entity"
            )
            public Attribute.Entity entity;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "attribute x-coordinate"
            )
            public Attribute.Double x;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "attribute y-coordinate"
            )
            public Attribute.Double y;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "attribute slope"
            )
            public Attribute.Double slope;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "attribute aspect"
            )
            public Attribute.Double aspect;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            description = "attribute latidute"
            )
            public Attribute.Double latitude;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            description = "attribute longitude"
            )
            public Attribute.Double longitude;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            description = "attribute slopeAspectCorrection factor"
            )
            public Attribute.Double slAsCf;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "Current time"
            )
            public Attribute.Calendar time;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "temporal resolution [d | h | m]"
            )
            public Attribute.String tempRes;
    
    int[] monthMean = {15,45,74,105,135,166,196,227,258,288,319,349};
    /*
     *  Component run stages
     */
    
    public void init() {
        double[] latLong = new double[2];
        latLong = org.unijena.j2k.geographicalCalculations.GKConversion.GK2LatLon(x.getValue(), y.getValue());
        latitude.setValue(latLong[0]);
        longitude.setValue(latLong[1]);
        getModel().getRuntime().println("slope:"+slope.getValue());
    }
    
    public void run() throws Attribute.Entity.NoSuchAttributeException{
        int julDay = 0; 
        
        if(this.tempRes == null || this.tempRes.equals("d") || this.tempRes.equals("h")){
            julDay = this.time.get(Attribute.Calendar.DAY_OF_YEAR);
        }
        else if(this.tempRes.equals("m")){
            int month = this.time.get(Attribute.Calendar.MONTH);
            julDay = this.monthMean[month];
        }
        double sloAspCorr = org.unijena.j2k.geographicalCalculations.CalcSlopeAspectCorrectionFactor.calc_slopeAspectCorrectionFactor(julDay, latitude.getValue(), slope.getValue(), aspect.getValue());
        slAsCf.setValue(sloAspCorr);
    }
    
    public void cleanup() {
        
    }
}
