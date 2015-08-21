/*
 * J2KProcessInterception.java
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

 */
package org.unijena.j2k.lake;

import jams.data.*;
import jams.model.*;

/**
 *
 * @author Peter Krause
 */
@JAMSComponentDescription(
        title="J2KProcessLake",
        author="Peter Krause",
        description="A module for integration of lakes"
        )
        public class J2KProcessLake extends JAMSComponent {
    
    /*
     *  Component variables
     */
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "HRU attribute name area"
            )
            public Attribute.Double area;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = "state variable rain"
            )
            public Attribute.Double precip;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "state variable potET"
            )
            public Attribute.Double potET;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = "state variable actET"
            )
            public Attribute.Double actET;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = "state variable lake storage"
            )
            public Attribute.Double lakeStorage;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = "state variable lake storage"
            )
            public Attribute.Double lakeChange;
    /*
     *  Component run stages
     */
    
    public void init() throws Attribute.Entity.NoSuchAttributeException{
        this.lakeStorage.setValue(0);
    }
    
    public void run() throws Attribute.Entity.NoSuchAttributeException{
        double ls = this.lakeStorage.getValue();
        double run_precip = precip.getValue() * this.area.getValue();
        
        ls = ls + run_precip;
        ls = ls - potET.getValue();

        actET.setValue(potET.getValue());
        precip.setValue(run_precip);
        lakeStorage.setValue(ls);
        lakeChange.setValue(run_precip - potET.getValue());
        
    }
    
    public void cleanup() {
        this.lakeStorage.setValue(0);
    }
    
}
