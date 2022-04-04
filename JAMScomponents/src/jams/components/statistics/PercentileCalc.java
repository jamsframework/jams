/*
 * PercentileCalc.java
 * Created on 16.01.2022, 23:30:03
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
package jams.components.statistics;

import jams.data.*;
import jams.model.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 *
 * @author Sven Kralisch <sven at kralisch.com>
 */
@JAMSComponentDescription(
        title = "PercentileCalc",
        author = "Sven Kralisch",
        description = "Calculates defined percentiles of a data array",
        date = "2022-01-15",
        version = "1.0_0"
)
@VersionComments(entries = {
    @VersionComments.Entry(version = "1.0_0", comment = "Initial version")
})
public class PercentileCalc extends JAMSComponent {

    /*
     *  Component attributes
     */
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "The list to store the values"
    )
    public Attribute.Object list;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "Percentile tresholds"
    )
    public Attribute.Integer[] percentileTreshold;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            description = "Percentile values"
    )
    public Attribute.Double[] percentileValue;

    /*
     *  Component run stages
     */
    @Override
    public void run() {
        
        List<Double> l = (ArrayList<Double>) list.getValue();       
        Collections.sort(l);
        
        double n = l.size();
        int i = 0;
        int c = 0;
        
        for (double d : l) {
            if (c/n >= percentileTreshold[i].getValue()/100.0) {
                percentileValue[i].setValue(d);
                i++;
                if (i == percentileTreshold.length) {
                    break;
                }
            }
            c++;
        }

    }
}
