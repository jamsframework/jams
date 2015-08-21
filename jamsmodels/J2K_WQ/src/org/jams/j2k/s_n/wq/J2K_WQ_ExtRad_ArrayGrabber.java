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

/*
 <component class="org.unijena.j2k.J2KArrayGrabber" name="j2kArrayGrabber">
    <jamsvar name="time" provider="TemporalContext" providervar="current"/>
    <jamsvar name="LAIArray" provider="HRUContext" providervar="currentEntity.LAIArray"/>
    <jamsvar name="effHArray" provider="HRUContext" providervar="currentEntity.effHArray"/>
    <jamsvar name="slAsCfArray" provider="HRUContext" providervar="currentEntity.slAsCfArray"/>
    <jamsvar name="rsc0Array" provider="HRUContext" providervar="currentEntity.rsc0Array"/>
    <jamsvar name="extRadArray" provider="HRUContext" providervar="currentEntity.extRadArray"/>
    <jamsvar name="actLAI" provider="HRUContext" providervar="currentEntity.actLAI"/>
    <jamsvar name="actEffH" provider="HRUContext" providervar="currentEntity.actEffH"/>
    <jamsvar name="actSlAsCf" provider="HRUContext" providervar="currentEntity.actSlAsCf"/>
    <jamsvar name="actRsc0" provider="HRUContext" providervar="currentEntity.actRsc0"/>
    <jamsvar name="actExtRad" provider="HRUContext" providervar="currentEntity.actExtRad"/>
    <jamsvar name="tempRes" value="d"/>
</component>
 */

package org.jams.j2k.s_n.wq;

import jams.data.*;
import jams.model.*;

/**
 *
 * @author Peter Krause
 */
@JAMSComponentDescription(
        title="J2KArrayGrabber",
        author="Peter Krause",
        description=""
        )
        public class J2K_WQ_ExtRad_ArrayGrabber extends JAMSComponent {
    
    /*
     *  Component variables
     */
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            update = JAMSVarDescription.UpdateType.INIT,
            description = "temporal resolution [m | d | h]"
            )
            public JAMSString tempRes;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            update = JAMSVarDescription.UpdateType.RUN,
            description = "time"
            )
            public JAMSCalendar time;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            update = JAMSVarDescription.UpdateType.RUN,
            description = "extraTerrRadiationArray"
            )
            public JAMSDoubleArray extRadArray;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            update = JAMSVarDescription.UpdateType.RUN,
            description = "rsc0 Array"
            )
            public JAMSDoubleArray rsc0Array;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            update = JAMSVarDescription.UpdateType.RUN,
            description = "slopeAscpectCorrectionFactorArray"
            )
            public JAMSDoubleArray slAsCfArray;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            update = JAMSVarDescription.UpdateType.RUN,
            description = "actExtraTerrRadiation"
            )
            public JAMSDouble actExtRad;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            update = JAMSVarDescription.UpdateType.RUN,
            description = "actSlopeAscpectCorrectionFactor"
            )
            public JAMSDouble actSlAsCf;
    /*
     *  Component run stages
     */
    
    public void init() throws JAMSEntity.NoSuchAttributeException{
        
    }
    
    public void run() throws JAMSEntity.NoSuchAttributeException{
    	
        int monthCount = time.get(Attribute.Calendar.MONTH);
        int dayCount = time.get(Attribute.Calendar.DAY_OF_YEAR) - 1;
        int hourCount = time.get(Attribute.Calendar.HOUR_OF_DAY) + (24 * dayCount);
        
        double in_extRad = -9999;
        double in_scf = -9999;
        double in_rsc0 = -9999;
                
        
        
        if(this.tempRes.getValue().equals("m")){
            if(this.extRadArray != null)
                in_extRad = this.extRadArray.getValue()[monthCount];
            if(this.slAsCfArray != null)
                in_scf = this.slAsCfArray.getValue()[monthCount];
        }
        else if(this.tempRes.getValue().equals("d")){
            if(this.extRadArray != null)
                in_extRad = this.extRadArray.getValue()[dayCount];
            if(this.slAsCfArray != null)
                in_scf = this.slAsCfArray.getValue()[dayCount];
        }
        else if(this.tempRes.getValue().equals("h")){
            if(this.extRadArray != null)
                in_extRad = this.extRadArray.getValue()[hourCount];
            if(this.slAsCfArray != null)
                in_scf = this.slAsCfArray.getValue()[dayCount];
        }

        
        this.actSlAsCf.setValue(in_scf);
        this.actExtRad.setValue(in_extRad);
        
        
    }
    
    public void cleanup() {
        
    }
    
}
