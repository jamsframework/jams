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
public class SpecificHumidityToRelativeHumidity extends JAMSComponent {

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "specific humidity",
            unit = "kg / kg")
    public Attribute.Double specHum;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "mean daily temperature",
            unit = "°C")
    public Attribute.Double tmean;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            description = "relative humidity",
            unit = "%")
    public Attribute.Double relHum;
           
    @Override
    public void run() {
        double s = specHum.getValue();
        double p = 1013.;
        double T = tmean.getValue();
        
        double numerator = ((p*1000.) / ((1.+622.)/s));
        double denumerator = (6.107 * Math.pow(10, 7.48 * T / 237.3+T) );
        double r = 100. * numerator / denumerator;
        
        relHum.setValue(r);
    }
}
