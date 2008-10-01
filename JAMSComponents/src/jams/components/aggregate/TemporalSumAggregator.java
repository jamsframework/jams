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
public class TemporalSumAggregator extends JAMSComponent {
    
    @JAMSVarDescription(
    access = JAMSVarDescription.AccessType.READ,
            update = JAMSVarDescription.UpdateType.RUN,
            description = "Current time"
            )
            public JAMSCalendar time;
    
    @JAMSVarDescription(
    access = JAMSVarDescription.AccessType.READ,
            update = JAMSVarDescription.UpdateType.RUN,
            description = "value attribute"
            )
            public JAMSDouble[] value;
    
    @JAMSVarDescription(
    access = JAMSVarDescription.AccessType.READ,
            update = JAMSVarDescription.UpdateType.RUN,
            description = "weight attribute"
            )
            public JAMSDouble weight;
    
    @JAMSVarDescription(
    access = JAMSVarDescription.AccessType.READWRITE,
            update = JAMSVarDescription.UpdateType.RUN,
            description = "sum attribute"
            )
            public JAMSDouble[] sum;
    
    @JAMSVarDescription(
    access = JAMSVarDescription.AccessType.READ,
            update = JAMSVarDescription.UpdateType.INIT,
            description = "Aggregation time interval"
            )
            public JAMSTimeInterval aggregationTimeInterval;
        
    private long count;
    
    public void init() {
        count = aggregationTimeInterval.getNumberOfTimesteps();
    }

    public void run() {
        if (!time.after(aggregationTimeInterval.getEnd()) && !time.before(aggregationTimeInterval.getStart())) {
            for (int i = 0; i < value.length; i++) {
                sum[i].setValue(sum[i].getValue()+ (value[i].getValue() / (weight.getValue()*count)));
            }
        }
    }
    
    public void cleanup() {
        for (int i = 0; i < value.length; i++) {
            sum[i].setValue(0);
        }
    }
    
}
