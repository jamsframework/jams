/*
 * Runoff.java
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
@JAMSComponentDescription (title = "Thorntwaite runoff",
                           author = "Sven Kralisch",
                           date = "30. September 2005",
                           description = "This component calculates the runoff based on a runoff factor, tank storage, " +
                           "surface runoff and snowmelt")
public class Runoff extends JAMSComponent {

    @JAMSVarDescription (access = JAMSVarDescription.AccessType.READ,
    description="A factorn defining how much water leaves the model - the remain will be stored in the model"
    )
    public Attribute.Double runoffFactor;

    @JAMSVarDescription (access = JAMSVarDescription.AccessType.READWRITE,
    description="The remain, i.e. the models tank storage")
    public Attribute.Double remain;

    @JAMSVarDescription (access = JAMSVarDescription.AccessType.READ,
    description="Surface runoff water")
    public Attribute.Double surfaceRunoff;

    @JAMSVarDescription (access = JAMSVarDescription.AccessType.READ,
    description="Water coming from snow melt")
    public Attribute.Double snowMelt;

    @JAMSVarDescription (access = JAMSVarDescription.AccessType.WRITE,
    description="Simulated runoff as function of tank storage (remain), surface runoff water and snowmelt water")
    public Attribute.Double runoff;


    public void run() {
        double runoffFactor = this.runoffFactor.getValue();
        double surfaceRunoff = this.surfaceRunoff.getValue();
        double snowMelt = this.snowMelt.getValue();
        double remain = this.remain.getValue();

        double ro1 = (surfaceRunoff + remain) * runoffFactor;
        remain = (surfaceRunoff + remain) * (1.0 - runoffFactor);

        this.runoff.setValue(ro1 + snowMelt);
        this.remain.setValue(remain);
    }
}
