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
package org.unijena.j2k.development;

import jams.data.*;
import jams.model.*;
import java.lang.Math.*;

/**
 *
 * @author c0krpe
 */
@JAMSComponentDescription(title = "J2KGroundwater",
author = "Peter Krause modifications Daniel Varga",
description = "Description")
public class InitJ2KReachStage_newest extends JAMSComponent {

    /*
     *  Component variables
     */

    @JAMSVarDescription(
    access = JAMSVarDescription.AccessType.READWRITE,
    description = "river bed height")
    public JAMSDouble reachBed;

    @JAMSVarDescription(
    access = JAMSVarDescription.AccessType.WRITE,
    description = "Reach Water Level")
    public JAMSDouble waterTable_NN;

    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
    update = JAMSVarDescription.UpdateType.RUN,
    description = "reachBed adaptation")
    public JAMSDouble rB_adapt;

    /*
     *  Component run stages
     */

    double run_rB, run_wT, run_rB_adapt;
       

    public void init() throws JAMSEntity.NoSuchAttributeException {

    }

    public void run() throws JAMSEntity.NoSuchAttributeException {

        run_rB = reachBed.getValue();                     //[m]
        run_rB_adapt = rB_adapt.getValue();               //[-]
        
        run_rB = run_rB - run_rB_adapt;                   //[m ü NN]

        run_wT = run_rB + 0.1;
        
        waterTable_NN.setValue(run_wT);                              //[m]
        reachBed.setValue(run_rB);
    }

    public void cleanup() {
    }
}
