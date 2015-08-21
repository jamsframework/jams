/*
 * StandardEntityReader.java
 * Created on 2. November 2005, 15:49
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

package org.unijena.j2k.io;

import org.unijena.j2k.*;
import jams.data.*;
import jams.model.*;
import jams.JAMS;
import jams.tools.JAMSTools;
import java.util.ArrayList;


/**
 *
 * @author S. Kralisch
 */
public class StandardEntityReader_1 extends JAMSComponent {

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "entity parameter file name"
            )
            public Attribute.String entityFileName;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            description = "entity collection"
            )
            public Attribute.EntityCollection entities;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            description = "Name of identifier",
            defaultValue = "ID"
            )
            public Attribute.String identName;
    
    public void init() {
       
        //read entity parameter        
        ArrayList<Attribute.Entity> collection = J2KFunctions.readParas(JAMSTools.CreateAbsoluteFileName(getModel().getWorkspaceDirectory().getPath(),entityFileName.getValue()), getModel());        
        //first initialize ids .. otherwise it will be too late
        for (Attribute.Entity e : collection) {
            try {
                e.setId((long) e.getDouble(identName.getValue()));
            } catch (Attribute.Entity.NoSuchAttributeException nsae) {
                getModel().getRuntime().sendErrorMsg("Couldn't find attribute \"" + identName + "\" while reading J2K HRU parameter file (" + entityFileName.getValue() + ")!");
            }
        }
        entities.setEntities(collection);
        int nEnt = entities.getEntityArray().length;
        getModel().getRuntime().println("Entities read and created successfull! ("+nEnt+")", JAMS.STANDARD);
        
        
    }
    
    
}
