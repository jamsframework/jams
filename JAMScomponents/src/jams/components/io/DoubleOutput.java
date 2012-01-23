/*
 * DoubleOutput.java
 * Created on 21. M^rz 2007, 17:26
 *
 * This file is part of JAMS
 * Copyright (C) 2006 FSU Jena
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

package jams.components.io;

import jams.JAMS;
import jams.data.*;
import jams.model.*;

/**
 *
 * @author Sven Kralisch
 */
@JAMSComponentDescription(
title="DoubleOutput",
        author="Sven Kralisch",
        description="Output value of double data at run stage"
        )
        public class DoubleOutput extends JAMSComponent {
    
    /*
     *  Component variables
     */
    
    @JAMSVarDescription(
    access = JAMSVarDescription.AccessType.READ,
            update = JAMSVarDescription.UpdateType.RUN,
            description = "Data to be output"
            )
            public JAMSDouble value;
    
    
    /*
     *  Component run stages
     */    
    public void run() {
        getModel().getRuntime().println(this.getInstanceName()+ ": " + value.toString(), JAMS.STANDARD);
    }
    
}
