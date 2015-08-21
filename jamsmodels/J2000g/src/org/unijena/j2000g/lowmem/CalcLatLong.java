/*
 * CalcLatLong.java
 * Created on 24. November 2005, 11:46
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

package org.unijena.j2000g.lowmem;

import jams.data.*;
import jams.model.*;

/**
 *
 * @author Peter Krause
 */
@JAMSComponentDescription(
        title="CalcLatLonge",
        author="Peter Krause",
        description="Handling of geographical coordination system and conversion"
        + "into lat-long values. The module can handle UTM or Gauss-Krueger "
        + "coordinates. Additionally, a slope-aspect correction factor for a "
        + "standard year is calculated which considers the specific slope-aspect"
        + "combination of a spatial modelling unit and the implication for e.g."
        + "incoming radiation.",
        version="1.0_0",
        date="2011-05-30"
        )
        public class CalcLatLong extends JAMSComponent {
    
    /*
     *  Component variables
     */
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "entity x-coordinate"
            )
            public Attribute.Double x;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "entity y-coordinate"
            )
            public Attribute.Double y;
        
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            description = "entity latitute",
            unit="degree",
            lowerBound = 0,
            upperBound = 90.0
            )
            public Attribute.Double latitude;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            description = "entity longitude",
            unit="degree",
            lowerBound = 0,
            upperBound = 180.0
            )
            public Attribute.Double longitude;
        
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "Projection [GK, UTMZZL, LL]"
            )
            public Attribute.String projection;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "temporal resolution [d | h | m]"
            )
            public Attribute.String tempRes;
    
    int[] monthMean = {15,45,74,105,135,166,196,227,258,288,319,349};
       
    /*
     *  Component run stages
     */
    
    public void init() {
        
    }
    
    public void run() throws Attribute.Entity.NoSuchAttributeException{
        double[] latLong = new double[2];
        String proj;
        if(this.projection.toString() == null)
            proj = "GK";
        else
            proj = this.projection.toString();
        if(proj.equals("GK")){
            latLong = org.unijena.j2k.geographicalCalculations.GKConversion.GK2LatLon(x.getValue(), y.getValue());
            latitude.setValue(latLong[0]);
            longitude.setValue(latLong[1]);
        }else if(proj.equals("LL")){
            //nothing to do here as far as the module is structured as it is right now
        }
        else if(proj.substring(0,3).equals("UTM")){
            int len = proj.length();
            String zoneStr = proj.substring(3, len);
            latLong = org.unijena.j2k.geographicalCalculations.UTMConversion.utm2LatLong(x.getValue(), y.getValue(), zoneStr);
            latitude.setValue(latLong[0]);
            longitude.setValue(latLong[1]);
        }        
    }
    
    public void cleanup() {
        
    }
}
