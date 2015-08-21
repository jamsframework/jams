package sidra;
/*
 * WeightedSumAggregator.java
 * Created on 22. Februar 2005, 15:01
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

import jams.model.*;
import jams.data.*;

/**
 *
 * @author S. Kralisch
 */
@JAMSComponentDescription(
        title="WeightedSumAggregator",
        author="Sven Kralisch",
        description="Calculates the weighted sum of given values"
        )
public class WeightedSumAggregator extends JAMSComponent {
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "The value(s) that shall be summed up"
            )
            public JAMSDouble[] value;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "A weight to be used to calculate the weighted sum"
            )
            public JAMSDouble weight;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = "The resulting weighted sum(s) of the given values"
            )
            public JAMSDouble[] sum;
    
    public void init(){
        for (int i = 0; i < value.length; i++) {
            sum[i].setValue(0);
        }
    }

    public void run() {
        for (int i = 0; i < value.length; i++) {
            sum[i].setValue(sum[i].getValue()+ (value[i].getValue() * weight.getValue()));
        }
    }
    
    
}
