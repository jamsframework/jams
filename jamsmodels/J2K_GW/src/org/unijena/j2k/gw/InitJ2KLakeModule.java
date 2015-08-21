/*
 * InitJ2KProcessGroundwater.java
 * Created on 25. November 2005, 16:54
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

package org.unijena.j2k.gw;

import jams.data.*;
import jams.model.*;

/**
 *
 * @author c0krpe
 */
@JAMSComponentDescription(
        title="J2KGroundwater",
        author="Peter Krause",
        description="Description"
        )
        public class InitJ2KLakeModule extends JAMSComponent {
    
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
            access = JAMSVarDescription.AccessType.WRITE,
            description = "maximum RG1 storage"
            )
            public Attribute.Double maxRG1;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            description = "maximum RG2 storage"
            )
            public Attribute.Double maxRG2;
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            description = "actual RG1 storage"
            )
            public Attribute.Double actRG1;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            description = "actual RG2 storage"
            )
            public Attribute.Double actRG2;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "relative initial RG1 storage"
            )
            public Attribute.Double initRG1;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "relative initial RG2 storage"
            )
            public Attribute.Double initRG2;

        @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = "maximum Volume of Lake"
            )
            public Attribute.Double maxLakeStor;

        @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            description = "actual Volume of Lake"
            )
            public Attribute.Double actLakeStor;
        
        @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "init Volume of Lake"
            )
            public Attribute.Double initLakeStor;
        
        @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "maximum Depth of Lake"
            )
            public Attribute.Double maxLakeDepth;
        
        @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            description = "actual Depth of Lake"
            )
            public Attribute.Double actLakeDepth;        
    /*
     *  Component run stages
     */
    
    public void init() throws Attribute.Entity.NoSuchAttributeException {
        
    }
    
    public void run() throws Attribute.Entity.NoSuchAttributeException {
        
        Attribute.Entity entity = entities.getCurrent();
        
            maxLakeStor.setValue(maxLakeDepth.getValue() * area.getValue() * 1000);
            actLakeStor.setValue(maxLakeStor.getValue() * initLakeStor.getValue());
            actLakeDepth.setValue(actLakeStor.getValue() / area.getValue() / 1000);
       
        /*maxRG1.setValue(entity.getDouble("RG1_max") * area.getValue());
        maxRG2.setValue(entity.getDouble("RG2_max") * area.getValue());
        
        actRG1.setValue(maxRG1.getValue() * initRG1.getValue());
        actRG2.setValue(maxRG2.getValue() * initRG2.getValue());*/       
    }
    
    public void cleanup() {
        
    }
    
   
}
