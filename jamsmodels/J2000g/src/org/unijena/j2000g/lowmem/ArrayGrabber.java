/*
 * J2KArrayGrabber.java
 * Created on 24. November 2005, 10:52
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

package org.unijena.j2000g.lowmem;

import jams.JAMS;
import jams.data.*;
import jams.model.*;

/**
 *
 * @author Peter Krause
 */
@JAMSComponentDescription(
        title="J2KArrayGrabber",
        author="Peter Krause",
        description="This component selects data values from arrays representing"
        + "a standard year.",
        version="1.0_0",
        date="2011-05-30"
        )
        public class ArrayGrabber extends JAMSComponent {
    
    /*
     *  Component variables
     */
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "temporal resolution [m | d | h]"
            )
            public Attribute.String tempRes;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "time"
            )
            public Attribute.Calendar time;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "extraTerrRadiationArray"
            )
            public Attribute.DoubleArray extRadArray;
        
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            description = "actExtraTerrRadiation"
            )
            public Attribute.Double actExtRad;
            
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            description = "actSlopeAscpectCorrectionFactor"
            )
            public Attribute.Double actSlAsCf;
    /*
     *  Component run stages
     */
    
    public void init() throws Attribute.Entity.NoSuchAttributeException{
        
    }
    
    public void run() throws Attribute.Entity.NoSuchAttributeException{
    	
        int monthCount = time.get(Attribute.Calendar.MONTH);
        int dayCount = time.get(Attribute.Calendar.DAY_OF_YEAR) - 1;
        int hourCount = time.get(Attribute.Calendar.HOUR_OF_DAY) + (24 * dayCount);
        
        double in_extRad = JAMS.getMissingDataValue();
        double in_scf = JAMS.getMissingDataValue();

        if(this.tempRes.getValue().equals("m")){
            if(this.extRadArray != null)
                in_extRad = this.extRadArray.getValue()[monthCount];
        }
        else if(this.tempRes.getValue().equals("d")){
            if(this.extRadArray != null)
                in_extRad = this.extRadArray.getValue()[dayCount];
        }
        else if(this.tempRes.getValue().equals("h")){
            if(this.extRadArray != null)
                in_extRad = this.extRadArray.getValue()[hourCount];
        }
        this.actSlAsCf.setValue(in_scf);
        this.actExtRad.setValue(in_extRad);        
    }
    
    public void cleanup() {
        
    }
    
}
