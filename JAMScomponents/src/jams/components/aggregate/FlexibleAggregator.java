/*
 * FlexibleAggregator.java
 * Created on 15.06.2025, 21:54:40
 *
 * file is part of JAMS
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

package jams.components.aggregate;

import jams.JAMS;
import jams.data.*;
import jams.model.*;

/**
 *
 * @author Sven Kralisch <sven at kralisch.com>
 */
@JAMSComponentDescription(
    title="FlexibleAggregator",
    author="Sven Kralisch",
    description="Aggreagate double data A as long as data B is NaN, then output "
            + "aggregate A. Output NaN instead if B is NaN",
    date = "2025-06-15",
    version = "1.0_0"
)
@VersionComments(entries = {
    @VersionComments.Entry(version = "1.0_0", comment = "Initial version")
})
public class FlexibleAggregator extends JAMSComponent {

    /*
     *  Component attributes
     */
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "data to be aggregated"
            )
            public Attribute.Double valueA;
                
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "data controlling the aggregation"
            )
            public Attribute.Double valueB;
                
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "calculater the average? if not, the sum is calculated",
            defaultValue = "true"
            )
            public Attribute.Boolean avg;
                
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            description = "aggregate of valueA"
            )
            public Attribute.Double aggregate;

    
    double sum;
    int counter;
    
    /*
     *  Component run stages
     */
    
    @Override
    public void init() {
        sum = 0;
        counter = 0;
    }

    @Override
    public void run() {
        sum += this.valueA.getValue();
        counter++;
        
        if (valueB.getValue() != JAMS.getMissingDataValue()) {
            
            if (avg.getValue()) {
                sum /= counter;
            }
            aggregate.setValue(sum);
            sum = 0;
            counter = 0;
        } else {
            aggregate.setValue(JAMS.getMissingDataValue());
        }
        
    }

}