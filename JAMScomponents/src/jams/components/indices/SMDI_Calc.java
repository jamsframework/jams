/*
 * SMDI_Calc.java
 * Created on 18.04.2017, 22:56:45
 *
 * This file is part of JAMS
 * Copyright (C) FSU Jena
 *
 * JAMS is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public License
 * as published by the Free Software Foundation; either version 3
 * of the License, or (at your option) any later version.
 *
 * JAMS is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with JAMS. If not, see <http://www.gnu.org/licenses/>.
 *
 */
package jams.components.indices;

import jams.data.*;
import jams.data.Attribute.Calendar;
import jams.model.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 *
 * @author Sven Kralisch <sven.kralisch at uni-jena.de>
 */
@JAMSComponentDescription(
        title = "Soil Moisture Deficit Index (SMDI) Calculator",
        author = "Sven Kralisch",
        description = "This component calculates the Soil Moisture Deficit Index (SMDI)",
        date = "2017-04-17",
        version = "1.0_0"
)
@VersionComments(entries = {
    @VersionComments.Entry(version = "1.0_0", comment = "Initial version")
})
public class SMDI_Calc extends AbstractDICalc {

    /*
     *  Component attributes
     */
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "List of collected soil water content values"
    )
    public Attribute.Object swValues;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = "Statistics for SMDI calculation"
    )
    public Attribute.Object smdiStats;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            description = "Soil water deficit"
    )
    public Attribute.Double sd;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = "Soil moisture deficit index (SMDI)"
    )
    public Attribute.Double smdi;

    private int statRes = 0;

    @Override
    public void run() {
        
        Stats stats;
        
        if (smdiStats.getValue() == null) {
            stats = calcStats((List) swValues.getValue());
            smdiStats.setValue(stats);
        }

        stats = (Stats) smdiStats.getValue();        
        int timeIndex = getTimeIndex(date);

        List<Double> list = (List) swValues.getValue();
        double sw = list.remove(0);

        double msw = stats.median[timeIndex];
        double min = stats.min[timeIndex];
        double max = stats.max[timeIndex];

        double sd_, smdi_;

        if (sw <= msw) {
            if (msw == min) {
                sd_ = 0;
            } else {
                sd_ = 100 * (sw - msw) / (msw - min);
            }
        } else if (msw == max) {
            sd_ = 0;
        } else {
            sd_ = 100 * (sw - msw) / (max - msw);
        }

        smdi_ = 0.5 * smdi.getValue() + sd_ / 50;

        smdi.setValue(smdi_);
        sd.setValue(sd_);
    }

}
