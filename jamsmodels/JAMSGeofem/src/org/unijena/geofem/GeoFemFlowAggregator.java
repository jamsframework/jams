/*
 * J2KProcessReachRouting.java
 * Created on 28. November 2005, 10:01
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
 * @author c0krpe
 */
@JAMSComponentDescription(
        title="Title",
        author="Author",
        description="Description"
        )
        public class GeoFemFlowAggregator extends JAMSComponent {
    
    /*
     *  Component variables
     */
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "hru area"
            )
            public Attribute.Double area;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "surface runoff"
            )
            public Attribute.Double sro;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "subsurface runoff"
            )
            public Attribute.Double ssro;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "ground water runoff"
            )
            public Attribute.Double gwro;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            description = "surface runoff"
            )
            public Attribute.Double c_sro;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            description = "subsurface runoff"
            )
            public Attribute.Double c_ssro;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            description = "ground water runoff"
            )
            public Attribute.Double c_gwro;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            description = "total runoff in mm"
            )
            public Attribute.Double totRunoff_mm;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            description = "total runoff in cms"
            )
            public Attribute.Double totRunoff_cms;
    
    /*
     *  Component run stages
     */
    
    public void init() throws Attribute.Entity.NoSuchAttributeException {
        
    }
    
    public void run() throws Attribute.Entity.NoSuchAttributeException {
        double totOut = this.sro.getValue() + this.ssro.getValue() + this.gwro.getValue();
        this.totRunoff_mm.setValue(totOut / area.getValue());
        //conversion from l/d to m³/s
        this.totRunoff_cms.setValue(totOut / (86400 * 1000));
        this.c_sro.setValue(sro.getValue() / (86400 * 1000));
        this.c_ssro.setValue(ssro.getValue() / (86400 * 1000));
        this.c_gwro.setValue(gwro.getValue() / (86400 * 1000));
    }
    
    public void cleanup() {
        
    }
    
    
}
