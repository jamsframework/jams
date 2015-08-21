/*
 * SoilMoisture.java
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
                           description = "This component calculates the soil moisture, actual ET and surface runoff based on " +
"old soil moisture, potET, temperature and precipitation")
public class SoilMoisture extends JAMSComponent {

    @JAMSVarDescription (access = JAMSVarDescription.AccessType.READ,
                         description = "Soil moisture storage capacity")
    public Attribute.Double soilMoistStorCap;

    @JAMSVarDescription (access = JAMSVarDescription.AccessType.READ,
                         description = "Potential ET")
    public Attribute.Double potET;

    @JAMSVarDescription (access = JAMSVarDescription.AccessType.READ,
                         description = "Temperature")
    public Attribute.Double temp;

    @JAMSVarDescription (access = JAMSVarDescription.AccessType.READ,
                         description = "Precipitation")
    public Attribute.Double precip;

    @JAMSVarDescription (access = JAMSVarDescription.AccessType.READWRITE,
                         description = "Old soil moisture")
    public Attribute.Double prestor;

    @JAMSVarDescription (access = JAMSVarDescription.AccessType.WRITE,
                         description = "Simulated soil moisture")
    public Attribute.Double soilMoistStor;

    @JAMSVarDescription (access = JAMSVarDescription.AccessType.WRITE,
                         description = "Surface runoff")
    public Attribute.Double surfaceRunoff;

    @JAMSVarDescription (access = JAMSVarDescription.AccessType.WRITE,
                         description = "Difference between precip and potential ET")
    public Attribute.Double pmpe;

    @JAMSVarDescription (access = JAMSVarDescription.AccessType.WRITE,
                         description = "Actual ET")
    public Attribute.Double actET;

    @JAMSVarDescription (access = JAMSVarDescription.AccessType.WRITE,
                         description = "Difference betwenn potential ET and actual ET")
    public Attribute.Double dff;

    public void run() {
        // get the parameter values
        double soilMoistStorCap = this.soilMoistStorCap.getValue();
        double prestor = this.prestor.getValue();

        // get the input values
        double temp = this.temp.getValue();
        double precip = this.precip.getValue();
        double potET = this.potET.getValue();

        double pmpe = precip - potET;

        double surfaceRunoff = 0.0;
        double soilMoistStor = 0.0;
        double actET = 0.0;

        if (temp < 0.0 && pmpe > 0.0) {
            surfaceRunoff = 0.0;
            soilMoistStor = prestor;
            actET = 0.0;
        } else if (pmpe > 0.0 || pmpe == 0.0) {

            actET = potET;
            //  SOIL MOISTURE RECHARGE
            if (prestor < soilMoistStorCap) {
                soilMoistStor = prestor + pmpe;
            }

            // SOIL MOISTURE STORAGE AT CAPACITY
            if (prestor == soilMoistStorCap) {
                soilMoistStor = soilMoistStorCap;
            }
            if (soilMoistStor > soilMoistStorCap) {
                soilMoistStor = soilMoistStorCap;
            }
            // CALCULATE SURPLUS
            surfaceRunoff = (prestor + pmpe) - soilMoistStorCap;
            if (surfaceRunoff < 0.0) {
                surfaceRunoff = 0.0;
            }
            //  CALCULATE MONTHLY CHANGE IN SOIL MOISTURE
            prestor = soilMoistStor;
        } else {
            soilMoistStor = prestor - Math.abs(pmpe * (prestor / soilMoistStorCap));
            if (soilMoistStor < 0.0) {
                soilMoistStor = 0.0;
            }
            double delstor = soilMoistStor - prestor;
            prestor = soilMoistStor;
            actET = precip + (delstor * (-1.0));
            surfaceRunoff = 0.0;
        }

        //SETTING THE OUTPUT VARIABLES
        this.dff.setValue(potET - actET);
        this.pmpe.setValue(pmpe);
        this.surfaceRunoff.setValue(surfaceRunoff);
        this.soilMoistStor.setValue(soilMoistStor);
        this.actET.setValue(actET);
        this.prestor.setValue(prestor);
    }
}
