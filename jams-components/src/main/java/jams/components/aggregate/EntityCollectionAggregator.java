/*
 * EntityCollectionAggregator.java
 * Created on 01.07.2025, 22:40:35
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
package jams.components.aggregate;

import jams.data.*;
import jams.model.*;

/**
 *
 * @author Sven Kralisch <sven at kralisch.com>
 */
@JAMSComponentDescription(
        title = "EntityCollectionAggregator",
        author = "Sven Kralisch",
        description = "Aggregate values of entities in a collection",
        date = "2025-07-01",
        version = "1.0_0"
)
@VersionComments(entries = {
    @VersionComments.Entry(version = "1.0_0", comment = "Initial version")
})
public class EntityCollectionAggregator extends JAMSComponent {

    /*
     *  Component attributes
     */
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "List of entities",
            defaultValue = "subbasinhrus"
    )
    public Attribute.EntityCollection entities;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "Name of attribute to be aggregated"
    )
    public Attribute.String[] attributeNames;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "Name of weight to be used during aggregation",
            defaultValue = "area"
    )
    public Attribute.String weightName;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "Should weight be considered?",
            defaultValue = "true"
    )
    public Attribute.Boolean doWeighting;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "Calculate mean?",
            defaultValue = "true"
    )
    public Attribute.Boolean calcMean;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            description = "the result attribute"
    )
    public Attribute.Double[] aggregateValues;

    /*
     *  Component run stages
     */
    @Override
    public void run() {

        double sum, weight;

        if (attributeNames.length != aggregateValues.length) {
            getModel().getRuntime().sendHalt("Error: length mismatch of attributeNames and aggregateValues");
        }

        for (int i = 0; i < attributeNames.length; i++) {
            
            sum = 0;

            for (Attribute.Entity entity : entities.getEntities()) {

                double value = entity.getDouble(attributeNames[i].getValue());
                if (doWeighting.getValue()) {
                    weight = entity.getDouble(weightName.getValue());
                } else {
                    weight = 1;
                }

                sum += value * weight;

            }

            if (calcMean.getValue()) {
                sum /= entities.getEntities().size();
            }

            aggregateValues[i].setValue(sum);
        }

    }

}
