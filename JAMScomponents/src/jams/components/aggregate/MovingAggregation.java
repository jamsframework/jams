/*
 * MovingAggregation.java
 * Created on 25.10.2018, 21:45:51
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
 * @author Sven Kralisch <sven.kralisch at uni-jena.de>
 */
@JAMSComponentDescription(
        title = "MovingAggregation",
        author = "Sven Kralisch",
        description = "Calculate a moving averages of time series data",
        date = "2018-10-25",
        version = "1.0_0"
)
@VersionComments(entries = {
    @VersionComments.Entry(version = "1.0_0", comment = "Initial version")
})
public class MovingAggregation extends JAMSComponent {

    /*
     *  Component attributes
     */
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "Values to aggregate"
    )
    public Attribute.DoubleArray inputValues;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "Size of aggregation window"
    )
    public Attribute.Integer windowSize;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "Current time. If set, the output will be done"
            + "on a yearly basis, resulting in one value per year."
    )
    public Attribute.Calendar time;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "Calc mean? If not, calc sum.",
            defaultValue = "true"
    )
    public Attribute.Boolean calcMean;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "Continue aggregation? If yes, aggregation continues "
            + "based on current aggregated values which are used for "
            + "initialization before the first aggregation step.",
            defaultValue = "false"
    )
    public Attribute.Boolean continueAggregation;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = "Aggregation results"
    )
    public Attribute.DoubleArray aggregatedValues;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            description = "Yearly aggregation results"
    )
    public Attribute.DoubleArray aggregatedValuesYearly;

    private int counter, valueCounter, startDay = -1;
    private double[] a, ya;

    /*
     *  Component run stages
     */
    @Override
    public void init() {
        if (continueAggregation.getValue()) {
            counter = windowSize.getValue();
        } else {
            counter = 0;
        }
    }

    @Override
    public void run() {

        if (aggregatedValues.getValue() == null) {
            aggregatedValues.setValue(new double[inputValues.getValue().length]);
        }

        a = aggregatedValues.getValue();
        if (counter < windowSize.getValue()) {
            counter++;
        }
        add(counter);

        if (time != null) {
            if (startDay < 0) {
                startDay = time.get(Attribute.Calendar.DAY_OF_YEAR);
                aggregatedValuesYearly.setValue(new double[inputValues.getValue().length]);
                ya = new double[inputValues.getValue().length];
                for (int i = 0; i < a.length; i++) {
                    ya[i] += a[i];
                }
                valueCounter = 1;
            }

            if (time.get(Attribute.Calendar.DAY_OF_YEAR) == startDay) {
                for (int i = 0; i < a.length; i++) {
                    aggregatedValuesYearly.getValue()[i] = ya[i] / valueCounter;
                    ya[i] = 0;
                }
                valueCounter = 0;
            }
            for (int i = 0; i < a.length; i++) {
                ya[i] += a[i];
            }
            valueCounter++;
        }
    }

    private void add(int n) {
        double[] in = inputValues.getValue();
//        in[0] = Math.random();
        if (calcMean.getValue()) {
            for (int i = 0; i < a.length; i++) {
                a[i] = (a[i] * (n - 1) + in[i]) / n;
            }
        } else {
            for (int i = 0; i < a.length; i++) {
                a[i] = a[i] * (n - 1) / n + in[i];
            }
        }
    }

    @Override
    public void cleanup() {
    }
}
