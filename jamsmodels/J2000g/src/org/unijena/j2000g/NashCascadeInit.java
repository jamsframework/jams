/*
 * NashCascade.java
 * Created on 20. June 2007, 16:54
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

package org.unijena.j2000g;

import jams.data.*;
import jams.model.*;

/**
 *
 * @author c0krpe
 */
@JAMSComponentDescription(
title="NashCascade",
        author="Peter Krause",
        description="Description"
        )
        public class NashCascadeInit extends JAMSComponent {
    /*
     *  Component variables
     */
    @JAMSVarDescription(
    access = JAMSVarDescription.AccessType.READ,
            description = "number of tanks"
            )
            public Attribute.Integer nTanks;
            
    @JAMSVarDescription(
    access = JAMSVarDescription.AccessType.WRITE,
            description = "the tanks itself"
            )
            public Attribute.DoubleArray storages;
    
            
    /*
     *  Component run stages
     */
    
    public void init() {        
    }
    
    public void run() {
        
        //build the cascade and initalize all tanks with a value of zero
        double[] tanks = new double[this.nTanks.getValue()];
        for(int i = 0; i < this.nTanks.getValue(); i++)
            tanks[i] = 0;
        //this.storages = getModel().getRuntime().getDataFactory().createDoubleArray();
        this.storages.setValue(tanks);
    }
    
    public void cleanup() {
        
    }    
}
