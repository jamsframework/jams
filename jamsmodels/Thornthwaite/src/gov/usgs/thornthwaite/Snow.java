/*
 * Snow.java
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

/**
 *
 * @author S. Kralisch
 */
@JAMSComponentDescription (title = "Thorntwaite snowmelt",
                           author = "Sven Kralisch",
                           date = "30. September 2005",
                           description = "This component calculates the snow melt based on a snow storage, potET, " +
"temperature and precipitation")
public class Snow extends JAMSComponent {

    @JAMSVarDescription (access = JAMSVarDescription.AccessType.READWRITE,
                         description = "Amount of water currently stored as snow")
    public Attribute.Double snowStorage;

    @JAMSVarDescription (access = JAMSVarDescription.AccessType.READ,
                         description = "Current potential ET")
    public Attribute.Double potET;

    @JAMSVarDescription (access = JAMSVarDescription.AccessType.READ,
                         description = "Current temperature")
    public Attribute.Double temp;

    @JAMSVarDescription (access = JAMSVarDescription.AccessType.READ,
                         description = "Current precipitation")
    public Attribute.Double precip;

    @JAMSVarDescription (access = JAMSVarDescription.AccessType.WRITE,
                         description = "Simulated snow melt water")
    public Attribute.Double snowMelt;

    public void run() {

        double snowStorage = this.snowStorage.getValue();

        double temp = this.temp.getValue();

        double potET = this.potET.getValue();
        double precip = this.precip.getValue();
        double pmpe = precip - potET;

        double snowMelt = 0.0;

        if (temp < 0.0 && pmpe > 0.0) {
            snowStorage = precip + snowStorage;
        }

        if (snowStorage > 0.0 && temp >= 0.0) {
            snowMelt = snowStorage * 0.5;
            snowStorage = snowStorage * 0.5;
        } else if (snowStorage == 0.0) {
            snowMelt = 0.0;
        }

        this.snowStorage.setValue(snowStorage);
        this.snowMelt.setValue(snowMelt);
    }
}
