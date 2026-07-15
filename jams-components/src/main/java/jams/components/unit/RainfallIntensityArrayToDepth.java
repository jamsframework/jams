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
public class RainfallIntensityArrayToDepth extends JAMSComponent {

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "rainfall intensity in [kg/(m²s)]")
    public Attribute.DoubleArray intensity;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            description = "output in [mm] per time unit")
    public Attribute.DoubleArray depth;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "time interval to provide unit")
    public Attribute.TimeInterval timeUnit;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "time interval to provide unit")
    public Attribute.Calendar time;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "density in [kg / m³] (default = 1000.0 for water)",
            defaultValue = "1000.0")
    public Attribute.Double density;

    @Override
    public void run() {
        double secondsInTimeUnit = 0;

        switch (timeUnit.getTimeUnit()) {
            case Calendar.HOUR_OF_DAY:
                secondsInTimeUnit = 60 * 60;
                break;
            case Calendar.DAY_OF_YEAR:
                secondsInTimeUnit = 60 * 60 * 24;
                break;
            case Calendar.MONTH:
                secondsInTimeUnit = 60 * 60 * 24 * time.getValue().getActualMaximum(Calendar.DAY_OF_MONTH);
                break;
            case Calendar.YEAR:
                secondsInTimeUnit = 60 * 60 * 24 * time.getValue().getActualMaximum(Calendar.DAY_OF_YEAR);
                break;
        }
        double intensityArray[] = intensity.getValue();
        int n = intensityArray.length;
        double depthArray[] = depth.getValue();
        if (depthArray == null) {
            depthArray = new double[n];
        }
        for (int i = 0; i < n; i++) {
            depthArray[i] = 
                    (intensityArray[i] / density.getValue()) // m*s^-1
                    * 1000.0 // mm * s^-1
                    * secondsInTimeUnit; // mm per TimeUnit
            
        }
        depth.setValue(depthArray);
    }
}
