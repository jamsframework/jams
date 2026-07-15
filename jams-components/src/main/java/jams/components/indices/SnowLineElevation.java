/*
 * SnowLineElevation.java
 * Created on 07.10.2021, 22:09:40
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

import jams.JAMS;
import jams.data.*;
import jams.model.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 *
 * @author Sven Kralisch <kralisch@gmail.com>
 */
@JAMSComponentDescription(
        title = "SnowLine",
        author = "Sven Kralisch",
        description = "Calculate whether or not an entity is part of the snow line",
        date = "2021-09-30",
        version = "1.0_0")
@VersionComments(entries = {
    @VersionComments.Entry(version = "1.0_0", comment = "Initial version")
})
public class SnowLineElevation extends JAMSComponent {

    /*
     *  Component attributes
     */
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "list of model entities"
    )
    public Attribute.EntityCollection entities;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "snow water equivalent attribute name",
            defaultValue = "snowTotSWE"
    )
    public Attribute.String sweName;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "elevation attribute name",
            defaultValue = "elevation"
    )
    public Attribute.String elevationName;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "area attribute name",
            defaultValue = "area"
    )
    public Attribute.String areaName;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "elevation sampling step size",
            defaultValue = "10",
            unit = "m"
    )
    public Attribute.Double stepSize;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            description = "snow line elevation"
    )
    public Attribute.Double snowLineElevation;

    private List<Attribute.Entity> orderedList;
    private List<double[]> values = new ArrayList();
    private List<Double> elevations = new ArrayList();

    /*
     *  Component run stages
     */
    @Override
    public void init() {
    }

    @Override
    public void run() {

        double elev, swe, error, area, bestSle = 0, 
                minError = Double.POSITIVE_INFINITY, sweSum;

        if (orderedList == null) {

            orderedList = new ArrayList(entities.getEntities());
            Comparator comp = new Comparator<Attribute.Entity>() {
                @Override
                public int compare(Attribute.Entity e1, Attribute.Entity e2) {
                    double elev1 = e1.getDouble(elevationName.getValue());
                    double elev2 = e2.getDouble(elevationName.getValue());
                    if (elev1 < elev2) {
                        return 1;
                    } else if (elev1 > elev2) {
                        return -1;
                    } else {
                        return 0;
                    }
                }
            };
            Collections.sort(orderedList, comp);

            for (Attribute.Entity e : orderedList) {

                elev = e.getDouble(elevationName.getValue());
                area = e.getDouble(areaName.getValue());

                double[] v = {elev, area, 0};
                values.add(v);
            }
            
            double e = values.get(0)[0];
            while (e > values.get(values.size()-1)[0]) {
                elevations.add(e);
                e -= stepSize.getValue();
            }
        }

        // update the swe values
        int i = 0;
        for (double[] v : values) {
            v[2] = orderedList.get(i).getDouble(sweName.getValue());
            i++;
        }
        
        for (double sle : elevations) {

            error = 0;
            sweSum = 0;

            // compare all entities against the sle
            for (double[] v : values) {

                elev = v[0];
                area = v[1];
                swe = v[2];
                sweSum += swe;

                //above but no snow or below but snow
                if (((elev >= sle) && (swe == 0)) || ((elev < sle) && (swe != 0))) {
                    error += area;
                }

            }
            
//            if (sweSum == 0) {
//                snowLineElevation.setValue(JAMS.getMissingDataValue());
//                return;
//            }
            
            if (error <= minError) {
                minError = error;
                bestSle = sle;
            }
        }
        
        if (bestSle == values.get(0)[0]) {
            bestSle = JAMS.getMissingDataValue();
        }
        
        snowLineElevation.setValue(bestSle);
    }

    @Override
    public void cleanup() {
    }
}
