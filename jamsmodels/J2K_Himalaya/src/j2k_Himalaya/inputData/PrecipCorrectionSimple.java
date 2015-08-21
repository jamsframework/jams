/*
 * RainCorrectionRichter.java
 * Created on 24. November 2005, 09:48
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
package j2k_Himalaya.inputData;

import jams.JAMS;
import jams.data.*;
import jams.io.GenericDataWriter;
import jams.model.*;

/**
 *
 * @author Peter Krause
 */
@JAMSComponentDescription(title = "PrecipCorrection",
author = "Peter Krause",
description = "A simple method to increase or decrease precipitation by certain % factor")
public class PrecipCorrectionSimple extends JAMSComponent {

    /*
     *  Component variables
     */
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
    description = "the precip values")
    public Attribute.DoubleArray inputValues;
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.WRITE,
    description = "the precip values")
    public Attribute.DoubleArray outputValues;
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
    description = "Array of temperature station elevations",
    defaultValue = "1.0")
    public Attribute.Double correctionFactor;

    /*
     *  Component run stages
     */
    public void init() throws Attribute.Entity.NoSuchAttributeException {
    }

    public void run() throws Attribute.Entity.NoSuchAttributeException {


        double[] inputValues = this.inputValues.getValue();
        double[] outputValues = new double[inputValues.length];
        double correctionFactor = this.correctionFactor.getValue();

        for (int i = 0; i < inputValues.length; i++) {
            outputValues[i] = inputValues[i] * correctionFactor;
        }

        this.outputValues.setValue(outputValues);

        
    }

    public void cleanup() {
    }
}
