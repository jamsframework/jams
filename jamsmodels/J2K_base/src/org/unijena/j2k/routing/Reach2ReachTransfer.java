/*
 * Reach2ReachTransfer.java
 * Created on 23.10.2014, 12:01:26
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
package org.unijena.j2k.routing;

import jams.data.*;
import jams.model.*;

/**
 *
 * @author Sven Kralisch
 */
@JAMSComponentDescription(
        title = "Reach2ReachTransfer",
        author = "Sven Kralisch",
        description = "Simulation of artificial transfer of water and substances "
        + "between reaches.",
        date = "2014-10-23",
        version = "1.0_0"
)
@VersionComments(entries = {
    @VersionComments.Entry(version = "1.0_0", comment = "Initial version")
})
public class Reach2ReachTransfer extends JAMSComponent {

    /*
     *  Component attributes
     */
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "Reach collection"
    )
    public Attribute.EntityCollection reaches;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "Source reach ID"
    )
    public Attribute.Long sourceReachID;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "Target reach ID"
    )
    public Attribute.Long targetReachID;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "Provisioning attributes of the source reach"
    )
    public Attribute.String[] sourceNames;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "Receiving attributes of the target reach"
    )
    public Attribute.String[] targetNames;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "Fraction of overall volume to be transferred",
            defaultValue = "1"
    )
    public Attribute.Double fraction;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "Lower boundary for transferred volume",
            defaultValue = "-1"
    )
    public Attribute.Double lowerBound;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "Upper boundary for transferred volume",
            defaultValue = "-1"
    )
    public Attribute.Double upperBound;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            description = "Component volumes that were transferred"
    )
    public Attribute.Double[] volumes;

    @Override
    public void run() {

        // check if there is a source and receiver
        Attribute.Entity source = reaches.getEntity(sourceReachID.getValue());
        Attribute.Entity target = reaches.getEntity(targetReachID.getValue());
        if (source == null || target == null || source.isEmpty() || target.isEmpty()) {
            return;
        }

        // obtain the component values and calculate overall volume available 
        // for transfer
        double sum = 0;
        double[] values = new double[sourceNames.length];
        for (int i = 0; i < values.length; i++) {
            values[i] = source.getDouble(sourceNames[i].getValue());
            sum += values[i];
        }
        
        if (sum == 0) {
            return;
        }

        // consider given fraction first
        double targetVolume = sum * fraction.getValue();

        // increase if targetVolume is lower than given lower bound
        if (lowerBound.getValue() >= 0) {
            targetVolume = Math.max(targetVolume, Math.min(sum, lowerBound.getValue()));
        }

        // decrease if targetVolume is lower than given lower bound
        if (upperBound.getValue() >= 0) {
            targetVolume = Math.min(targetVolume, upperBound.getValue());
        }

        // calculate fraction of overall volume that will be transferred
        double fractionSum = targetVolume / sum;

        // do the transfer
        for (int i = 0; i < values.length; i++) {

            // calculate volume to be removed from each component
            double x = values[i] * fractionSum;

            // add this volume to the corresponding target component
            target.setDouble(targetNames[i].getValue(), target.getDouble(targetNames[i].getValue()) + x);

            // remove this volume from the source component
            source.setDouble(sourceNames[i].getValue(), values[i] - x);

            // store transferred volume in output attribute
            if (volumes != null && volumes[i] != null) {
                volumes[i].setValue(x);
            }

        }
    }

}
