/*
 * HamonET.java
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
@JAMSComponentDescription (title = "Thorntwaite potET",
                           author = "Sven Kralisch",
                           date = "30. September 2005",
                           description = "This component calculates the potential ET after Hamon based on time, temperature and day length")
public class HamonET extends JAMSComponent {

    @JAMSVarDescription (access = JAMSVarDescription.AccessType.READ,
                         description = "Current time")
    public Attribute.Calendar time;

    @JAMSVarDescription (access = JAMSVarDescription.AccessType.READ,
                         description = "Current temperature")
    public Attribute.Double temp;

    @JAMSVarDescription (access = JAMSVarDescription.AccessType.READ,
                         description = "Length of a day in this month")
    public Attribute.Double daylength;

    @JAMSVarDescription (access = JAMSVarDescription.AccessType.WRITE,
                         description = "Resulting potential ET")
    public Attribute.Double potET;

    /*
    public void init() {
    System.out.println(potET.getUnit());
    System.out.println(daylength.getUnit());
    System.out.println(potET.getUnit().isCompatible(daylength.getUnit()));
    Converter conv = daylength.getUnit().getConverterTo(potET.getUnit());
    System.out.println(conv.convert(1));
    }
     */
    public void run() {

        double temp = this.temp.getValue();
        double daylength = this.daylength.getValue();

        double Wt = 4.95 * Math.exp(0.062 * temp) / 100.;
        double D2 = (daylength / 12.0) * (daylength / 12.0);
        double potET = 0.55 * time.getActualMaximum(Calendar.DAY_OF_MONTH) * D2 * Wt;

        if (potET <= 0.0) {
            potET = 0.0;
        }
        if (temp <= -1.0) {
            potET = 0.0;
        }

        potET *= 25.4;

        this.potET.setValue(potET);
    }
}
