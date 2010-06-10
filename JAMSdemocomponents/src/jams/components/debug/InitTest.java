/*
 * InitTest.java
 * Created on 16. März 2007, 10:54
 *
 * This file is part of JAMS
 * Copyright (C) 2006 FSU Jena
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

package jams.components.debug;

import jams.data.*;
import jams.model.*;

/**
 *
 * @author Sven Kralisch
 */
@JAMSComponentDescription(
title="Title",
        author="Author",
        description="Description"
        )
        public class InitTest extends JAMSComponent {
    
    /*
     *  Component variables
     */
    
    @JAMSVarDescription(
    access = JAMSVarDescription.AccessType.READWRITE,
            update = JAMSVarDescription.UpdateType.RUN,
            description = "Description"
            )
            public Attribute.DoubleArray d;
    
    
    /*
     *  Component run stages
     */
    
    public void init() {
        double[] x = {1, 2, 3};
        if (d == null) {
            d = JAMSDataFactory.createDoubleArray();
            d.setValue(x);
        }
    }
    
    public void run() {
        getModel().getRuntime().println("running " + getInstanceName());
        getModel().getRuntime().println(""+d);
    }
    
    public void cleanup() {
        
    }
}
