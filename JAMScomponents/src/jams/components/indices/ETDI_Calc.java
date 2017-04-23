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
import jams.model.*;
import java.util.List;

/**
 *
 * @author Sven Kralisch <sven.kralisch at uni-jena.de>
 */
@JAMSComponentDescription(
        title = "Evapotranspiration Deficit Index (ETDI) Calculator",
        author = "Sven Kralisch",
        description = "This component calculates the Evapotranspiration Deficit Index (ETDI)",
        date = "2017-04-17",
        version = "1.0_0"
)
@VersionComments(entries = {
    @VersionComments.Entry(version = "1.0_0", comment = "Initial version")
})
public class ETDI_Calc extends AbstractDICalc {

    /*
     *  Component attributes
     */
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "List of collected soil water content values"
    )
    public Attribute.Object wsValues;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = "Statistics for SMDI calculation"
    )
    public Attribute.Object etdiStats;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            description = "Water stress anomaly"
    )
    public Attribute.Double wsa;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = "Evapotranspiration Deficit Index (ETDI)"
    )
    public Attribute.Double etdi;

    
    @Override
    public void run() {

        Stats stats;
        
        if (etdiStats.getValue() == null) {
            stats = calcStats((List) wsValues.getValue());
            etdiStats.setValue(stats);
        }

        stats = (Stats) etdiStats.getValue();
        int timeIndex = getTimeIndex(date);

        List<Double> list = (List) wsValues.getValue();
        double ws = list.remove(0);

        double mws = stats.median[timeIndex];
        double min = stats.min[timeIndex];
        double max = stats.max[timeIndex];

        double ws_, etdi_;

        if (ws <= mws) {
            if (mws == min) {
                ws_ = 0;
            } else {
                ws_ = 100 * (mws - ws) / (mws - min);
            }
        } else if (mws == max) {
            ws_ = 0;
        } else {
            ws_ = 100 * (mws - ws) / (max - mws);
        }

        etdi_ = 0.5 * etdi.getValue() + ws_ / 50;

        etdi.setValue(etdi_);
        wsa.setValue(ws_);
    }

}
