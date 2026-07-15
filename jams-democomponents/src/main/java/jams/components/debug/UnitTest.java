/*
 * UnitTest.java
 * Created on 4. M^rz 2008, 13:06
 *
 * This file is a JAMS component
 * Copyright (C) FSU Jena
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public License
 * as published by the Free Software Foundation; either version 3
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, see <http://www.gnu.org/licenses/>.
 *
 */

package jams.components.debug;

import jams.data.*;
import jams.model.JAMSComponent;
import jams.model.JAMSComponentDescription;
import jams.model.JAMSVarDescription;

/**
 *
 * @author Sven Kralisch
 */
@JAMSComponentDescription(
title="Title",
        author="Author",
        description="Description"
        )
        public class UnitTest extends JAMSComponent {
    
    /*
     *  Component variables
     */
    
    @JAMSVarDescription(
    access = JAMSVarDescription.AccessType.READ,
            update = JAMSVarDescription.UpdateType.INIT,
            description = "Description",
            unit = "grad X"
            )
            public JAMSDouble x;
    
    
    /*
     *  Component run stages
     */
    
    public void init() {
        getModel().getRuntime().println("init");
    }
    
    public void run() {
        getModel().getRuntime().println("run");
    }
    
    public void cleanup() {
        getModel().getRuntime().println("cleanup");
    }


}
