/*
 * SedimentHeatBalance.java
 * Created on 28. December 2010, 14:43
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

package org.jams.j2k.s_n.wq.SedimentWaterHeatTransfer;

import org.jams.j2k.s_n.wq.*;
import java.io.*;
import jams.data.*;
import jams.model.*;

/**
 *
 * @author Marcel Wetzel
 */
@JAMSComponentDescription(title = "CalcSedimentHeatBalance",
author = "Marcel Wetzel",
description = "Calculates the heat balance for bottom sediment underlying a specific reach")

public class SedimentHeatBalance extends JAMSComponent {

    /*
     *  Component variables
     */


    @JAMSVarDescription(
    access = JAMSVarDescription.AccessType.READWRITE,
    update = JAMSVarDescription.UpdateType.INIT,
    description = "temperature of the bottom sediment for specific reach in °C")
    public JAMSDouble bottomsedtemp;

    @JAMSVarDescription(
    access = JAMSVarDescription.AccessType.READWRITE,
    update = JAMSVarDescription.UpdateType.INIT,
    description = "the sediment water heat flux in cal/(cm^2 * d)")
    public JAMSDouble sedheat1;

    @JAMSVarDescription(
    access = JAMSVarDescription.AccessType.WRITE,
    update = JAMSVarDescription.UpdateType.INIT,
    description = "temperature of the bottom sediment for specific reach in °C")
    public JAMSDouble sedtemp;
    
  
    /*
     *  Component run stages
     */


    public void init() throws JAMSEntity.NoSuchAttributeException {

    }
    public void run() throws JAMSEntity.NoSuchAttributeException, IOException {
            
         // calculation of a heat balance for bottom sediment underlying a specific reach
            // p the density of the sediment (g/cm^3)
            // Cps  the specific heat of the sediment (cal/(g°C))
            // Hsed the effective thickness of the sediment layer (cm)
            // sh the sediment water heat flux in cal/(cm^2 * d)
            // deltaSedT the timestep change (delta) of the sediment temperature in °C

            double sh = sedheat1.getValue();

            // values suggested from Q2K manual
            double p = 1.6;
            double Cps = 0.4;
            double Hsed = 10;
            double deltaSedT = 0;
            double SedT = 0;

            deltaSedT = (-1) * (sh / (p * Cps * Hsed));
            SedT = bottomsedtemp.getValue() + deltaSedT;
            bottomsedtemp.setValue(SedT);
            sedtemp.setValue(SedT);

            sedheat1.setValue(0);
            
    }

     public void cleanup() {

    }

}


    
