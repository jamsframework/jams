/*
 * LongTermAggregation.java
 * Created on 29.06.2017, 22:39:50
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

import jams.JAMS;
import jams.data.*;
import jams.model.*;

/**
 *
 * @author Sven Kralisch <sven.kralisch at uni-jena.de>
 */
@JAMSComponentDescription(
        title = "LongTermAggregation",
        author = "Sven Kralisch",
        description = "Calculates longterm aggregates, e.g. average daily temperature",
        date = "2017-06-29",
        version = "1.0_2"
)
@VersionComments(entries = {
    @VersionComments.Entry(version = "1.0_0", date = "2017-06-29", comment = "Initial version"),
    @VersionComments.Entry(version = "1.0_1", comment = "Some improvements"),
    @VersionComments.Entry(version = "1.0_2", date = "2018-09-10", comment = "Some improvements")
})
public class LongTermAggregation extends JAMSComponent {

    /*
     *  Component attributes
     */
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "Description"
    )
    public Attribute.Double[] inputAttribute;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "Description"
    )
    public Attribute.Boolean[] calcAverage;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "Description"
    )
    public Attribute.Calendar time;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "day (of year), week (of year), month (of year), year (overall aggregate), irregular (not implemented)"
    )
    public Attribute.String targetUnit;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "if targetUnit is \"irregular\""
    )
    public Attribute.TimeInterval targetInterval;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            description = "Aggregation result"
    )
    public Attribute.DoubleArray[] aggregate;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            description = "number of values"
    )
    public Attribute.IntegerArray[] nValues;

    int count[][], field;

    /*
     *  Component run stages
     */
    @Override
    public void init() {
        int maxn;
        if (targetUnit.getValue().equals("day")) {
            field = Attribute.Calendar.DAY_OF_YEAR;
            maxn = 366;
            for (Attribute.DoubleArray da : aggregate) {
                da.setValue(new double[maxn]);
            }
        } else if (targetUnit.getValue().equals("week")) {
            field = Attribute.Calendar.WEEK_OF_YEAR;
            maxn = 53;
            for (Attribute.DoubleArray da : aggregate) {
                da.setValue(new double[maxn]);
            }
        } else if (targetUnit.getValue().equals("month")) {
            field = Attribute.Calendar.MONTH;
            maxn = 12;
            for (Attribute.DoubleArray da : aggregate) {
                da.setValue(new double[maxn]);
            }
        } else if (targetUnit.getValue().equals("year")) {
            field = Attribute.Calendar.YEAR;
            maxn = 1;
            for (Attribute.DoubleArray da : aggregate) {
                da.setValue(new double[maxn]);
            }
        } else if (targetUnit.getValue().equals("irregular")) {
            field = -1;
            maxn = 1;
            for (Attribute.DoubleArray da : aggregate) {
                da.setValue(new double[maxn]);
            }
        } else {
            field = Attribute.Calendar.MONTH;
            maxn = 12;
            for (Attribute.DoubleArray da : aggregate) {
                da.setValue(new double[maxn]);
            }
        }
        count = new int[inputAttribute.length][maxn];
    }

    @Override
    public void run() {
        if (field < 0) {
            for (int i = 0; i < inputAttribute.length; i++) {
                count[i][0]++;
                //TODO: implementation for irregular time intervals
            }
        } else {
            int index = time.get(field);
            if (field != Attribute.Calendar.MONTH) {
                index--;
            }
            if (field == Attribute.Calendar.YEAR) {
                index = 0;
            }
            for (int i = 0; i < inputAttribute.length; i++) {

                if (inputAttribute[i].getValue() == JAMS.getMissingDataValue()) {
                    continue;
                }

                if (calcAverage[i].getValue()) {
                    double result = (aggregate[i].getValue()[index] * count[i][index] + inputAttribute[i].getValue()) / (count[i][index] + 1);
                    aggregate[i].getValue()[index] = result;
                } else {
                    double result = aggregate[i].getValue()[index] + inputAttribute[i].getValue();
                    aggregate[i].getValue()[index] = result;
                }
                count[i][index]++;
            }
            

        }
        for (int i = 0; i < nValues.length; i++) {
            nValues[i].setValue(count[i]);
        }
    }

    @Override
    public void cleanup() {
    }
}
