/*
 * TemporalSumAggregator.java
 * Created on 19. Juli 2006, 11:57
 *
 * This file is part of JAMS
 * Copyright (C) 2005 FSU Jena
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
package jams.components.aggregate;

import jams.model.*;
import jams.data.*;

/**
 *
 * @author S. Kralisch
 */
@JAMSComponentDescription (title = "TemporalSumAggregator",
                           author = "Sven Kralisch",
                           description = "Calculates the weighted average of given values in a given time interval")
public class TemporalSumAggregator extends JAMSComponent {

    @JAMSVarDescription (access = JAMSVarDescription.AccessType.READ,
                         description = "Current time")
    public Attribute.Calendar time;

    @JAMSVarDescription (access = JAMSVarDescription.AccessType.READ,
                         description = "The value(s) to be summed up")
    public Attribute.Double[] value;

    @JAMSVarDescription (access = JAMSVarDescription.AccessType.READ,
                         description = "A weight to be used to calculate the weighted average")
    public Attribute.Double weight;

    @JAMSVarDescription (access = JAMSVarDescription.AccessType.READWRITE,
                         description = "The resulting weighted average(s) of the given values")
    public Attribute.Double[] sum;

    @JAMSVarDescription (access = JAMSVarDescription.AccessType.READ,
                         description = "A time interval defining start and end of the weighted temporal aggregation")
    public Attribute.TimeInterval aggregationTimeInterval;

    @JAMSVarDescription (access = JAMSVarDescription.AccessType.READ,
                         description = "Calculate the average value? If average is false, the (weighted) sum will be calculated.",
                         defaultValue = "true")
    public Attribute.Boolean average;

    private long count;

    public void init() {
        for (int i = 0; i < value.length; i++) {
            sum[i].setValue(0);
        }
        if (average.getValue()) {
            count = aggregationTimeInterval.getNumberOfTimesteps();
        } else {
            count = 1;
        }
    }

    public void run() {
        if (!time.after(aggregationTimeInterval.getEnd()) && !time.before(aggregationTimeInterval.getStart())) {
            for (int i = 0; i < value.length; i++) {
                sum[i].setValue(sum[i].getValue() + (value[i].getValue() / (weight.getValue() * count)));
            }
        }
    }
}
