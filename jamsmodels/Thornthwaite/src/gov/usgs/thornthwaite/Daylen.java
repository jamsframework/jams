/*
 * Daylen.java
 * Created on 30. September 2005, 11:37
 *
 * This file is part of JAMS
 * Copyright (C) 2005 S. Kralisch and P. Krause
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
package gov.usgs.thornthwaite;

import jams.model.*;
import jams.data.*;
import java.util.Calendar;

/**
 *
 * @author S. Kralisch
 */
@JAMSComponentDescription (title = "Thorntwaite daylength",
                           author = "Sven Kralisch",
                           date = "30. September 2005",
                           description = "This component calculates the length of a day in the current month based on the latitude")
public class Daylen extends JAMSComponent {

    @JAMSVarDescription (access = JAMSVarDescription.AccessType.READ,
                         description = "Current time")
    public Attribute.Calendar time;

    @JAMSVarDescription (access = JAMSVarDescription.AccessType.READ,
                         description = "The models latitude")
    public Attribute.Double latitude;

    @JAMSVarDescription (access = JAMSVarDescription.AccessType.WRITE,
                         description = "Length of a day at the current time")
    public Attribute.Double daylength;

    static final int[] DAYS = {
        15, 45, 74, 105, 135, 166, 196, 227, 258, 288, 319, 349
    };

    public void run() {

        int month = this.time.get(Calendar.MONTH);
        double latitude = this.latitude.getValue();

        double dayl = (double) DAYS[month] - 80.;
        if (dayl < 0.0) {
            dayl = 285. + (double) DAYS[month];
        }

        double decr = 23.45 * Math.sin(dayl / 365. * 6.2832) * 0.017453;
        double alat = latitude * 0.017453;
        double csh = (-0.02908 - Math.sin(decr) * Math.sin(alat)) / (Math.cos(decr) * Math.cos(alat));
        double dl = 24.0 * (1.570796 - Math.atan(csh / Math.sqrt(1. - csh * csh))) / Math.PI;

        this.daylength.setValue(dl);
    }
}
