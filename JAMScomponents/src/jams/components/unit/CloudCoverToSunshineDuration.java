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

import jams.data.*;
import jams.model.*;
import jams.data.Attribute;
import jams.data.Attribute.Calendar;
import jams.model.JAMSComponent;
import jams.model.JAMSComponentDescription;
import jams.model.JAMSVarDescription;

/**
 *
 * @author christian
 * 
 * 
 */

@JAMSComponentDescription(
        title="Converts clt into sunh",
        author="Christian Fischer",
        date="1. December 2010",
        description=" Bug for cloud cover fixed (old: sunh = maxSunh * cloudCoverFraction * 0.01  ;new: sunh = maxSunh * (1 - (cloudCoverFraction * 0.01))")

public class CloudCoverToSunshineDuration extends JAMSComponent {

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "fraction of cloud cover in parts",
            unit = "%")
    public Attribute.Double cloudCoverFraction;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "Maximum sunshine duration in h",
            unit = "h/d")
    public Attribute.Double maxSunh;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            description = "sunshine duration in h",
            unit = "h/d")
    public Attribute.Double sunh;
    
    @Override
    public void run() {
        sunh.setValue(maxSunh.getValue() * (1 - (cloudCoverFraction.getValue() * 0.01)));
    }
}
