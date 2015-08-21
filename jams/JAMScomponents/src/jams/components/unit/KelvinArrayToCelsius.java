 /*
 * KgPerSecondToMillimeter.java
 * Created on 13. November 2014, 11:57
 *
 * This file is part of JAMS
 * Copyright (C) 2005 FSU Jena
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
package jams.components.unit;

import jams.data.Attribute;
import jams.data.Attribute.Calendar;
import jams.model.JAMSComponent;
import jams.model.JAMSVarDescription;

/**
 *
 * @author christian
 */
public class KelvinArrayToCelsius extends JAMSComponent {

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "input temperature",
            unit = "K")
    public Attribute.DoubleArray inputTemperature;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            description = "output temperature",
            unit = "°C")
    public Attribute.DoubleArray outputTemperature;
        
    @Override
    public void run() {
        double input[] = inputTemperature.getValue();
        double output[] = outputTemperature.getValue();
        int n = input.length;
        
        if (output == null){
            output = new double[n];
        }
        for (int i=0;i<n;i++){
            output[i] = input[i] - 273.15;
        }
        outputTemperature.setValue(output);
    }
}
