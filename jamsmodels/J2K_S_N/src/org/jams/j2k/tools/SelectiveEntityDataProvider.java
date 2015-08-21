/*
 * SelectiveEntityWriter.java
 * Created on 21. March 2006, 11:05
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
package org.jams.j2k.tools;

import jams.tools.JAMSTools;
import jams.data.*;
import jams.model.*;
import jams.io.*;

/**
 *
 * @author M. Fink
 */
@JAMSComponentDescription(title = "SelectiveEntityDataProvider",
author = "Manfred Fink",
description = "Provides double values of a a double attribute of a single entity")
public class SelectiveEntityDataProvider extends JAMSComponent {

    /*
     *  Component variables
     */
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
    description = "EntitySet")
    public Attribute.EntityCollection entitySet;
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
    description = "Output file attribute name")
    public Attribute.String attributeName;
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
    description = "Output entities")
    public Attribute.Integer ID;
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
    description = "Output Variable")
    public Attribute.Double output;

    /*
     *  Component runstages
     */
    public void init() {
    }

    public void run() {

        int curID = (int) (int) entitySet.getCurrent().getDouble("ID");
        if (this.ID.getValue() == curID) {
                               
                double doub = entitySet.getCurrent().getDouble(attributeName.getValue());
                output.setValue(doub);

            }else{
                
            }


        
    }
    
    
    public void cleanup() {
    }
}
