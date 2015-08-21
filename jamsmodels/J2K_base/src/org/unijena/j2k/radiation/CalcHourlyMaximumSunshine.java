/*
 * CalcHourlyMaximumSunshine.java
 * Created on 09. December 2009, 11:57
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
package org.unijena.j2k.radiation;

import java.io.*;
import jams.tools.JAMSTools;
import jams.data.*;
import jams.model.*;

/**
 *
 * @author Peter Krause
 */
@JAMSComponentDescription(title = "CalcHourlyMaximumSunshine",
author = "Peter Krause",
description = "Calculates maximum possible sunshine duration for hours per day")
public class CalcHourlyMaximumSunshine extends JAMSComponent {

    /*
     *  Component variables
     */
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.WRITE,
        description = "maximum sunshine [0|1]")
        public Attribute.Double maxSunshine;
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
        description = "extraterrestic radiation [MJ/m²]")
        public Attribute.Double actExtRad;

    /*
     *  Component run stages
     */
    public void init() throws Attribute.Entity.NoSuchAttributeException, IOException {
    }

    public void run() throws Attribute.Entity.NoSuchAttributeException, IOException {
        double maximumSunshine = org.unijena.j2k.physicalCalculations.HourlySolarRadiationCalculationMethods.calc_HourlyMaxSunshine(this.actExtRad.getValue());
        maxSunshine.setValue(maximumSunshine);
    }

    public void cleanup() throws Attribute.Entity.NoSuchAttributeException, IOException {
    }
}
