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
import java.lang.Math.*;

@JAMSComponentDescription(
        title = "InitJ2KReachStage",
        author = "Daniel Varga",
        description = "Initialisation for River Bed and Water Depth in Reaches",
        version="1.0_0",
        date="2011-01-10"
        )
public class InitJ2KReachStage extends JAMSComponent {

    /*
     *  Component variables
     */

    @JAMSVarDescription(
        access = JAMSVarDescription.AccessType.READWRITE,
        description = "river bed height",
        unit = "m", //[m ü NN] / [a.s.l.]
        lowerBound = 0,
        upperBound = Double.POSITIVE_INFINITY
        )
        public JAMSDouble reachBed;

    @JAMSVarDescription(
        access = JAMSVarDescription.AccessType.WRITE,
        description = "Reach Water Level",
        unit = "m", //[m ü NN] / [a.s.l.]
        lowerBound = 0,
        upperBound = Double.POSITIVE_INFINITY
        )
        public JAMSDouble waterTable_NN;

    @JAMSVarDescription(
        access = JAMSVarDescription.AccessType.READ,
        description = "reachBed adaptation",
        unit = "m",
        defaultValue = "0",
        lowerBound = 0,
        upperBound = Double.POSITIVE_INFINITY
        )
        public JAMSDouble rB_adapt;

    /*
     *  Component run stages
     */

    double run_rB, run_wT, run_rB_adapt;
       

    public void init() throws JAMSEntity.NoSuchAttributeException {

    }

    public void run() throws JAMSEntity.NoSuchAttributeException {

        run_rB = reachBed.getValue();
        run_rB_adapt = rB_adapt.getValue();
        
        run_rB = run_rB - run_rB_adapt;

        run_wT = run_rB + 0.1;
        //no calibration needed, initial water depth for all reaches 10 cm
        
        waterTable_NN.setValue(run_wT);
        reachBed.setValue(run_rB);
    }

    public void cleanup() {
    }
}
