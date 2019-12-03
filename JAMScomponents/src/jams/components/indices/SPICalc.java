/*
 * SPICalc.java
 * Created on 28.11.2019, 21:52:39
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
import jams.components.aggregate.TSAggregator;
import jams.data.*;
import jams.model.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 *
 * @author Sven Kralisch <sven.kralisch@uni-jena.de>
 */
@JAMSComponentDescription(
        title = "Title",
        author = "Author",
        description = "Description",
        date = "YYYY-MM-DD",
        version = "1.0_0"
)
@VersionComments(entries = {
    @VersionComments.Entry(version = "1.0_0", comment = "Initial version"),
    @VersionComments.Entry(version = "1.0_1", comment = "Some improvements")
})
public class SPICalc extends JAMSComponent {

    /*
     *  Component attributes
     */
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "The list with stored values"
    )
    public Attribute.Object valueList;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "The list to store the values"
    )
    public Attribute.Object dateList;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "Number months for SPI aggregation"
    )
    public Attribute.Integer[] aggrMonths;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            description = "SPI (monthly)"
    )
    public Attribute.DoubleArray[] spiArray;

    /*
     *  Component run stages
     */
    @Override
    public void run() {

        List<Attribute.Calendar> dates = (ArrayList<Attribute.Calendar>) dateList.getValue();
        List<Double> values = (ArrayList<Double>) valueList.getValue();
        double[] array = new double[values.size()];
        for (int i = 0; i < array.length; i++) {
            array[i] = values.get(i);
        }

        TSAggregator aggr = new TSAggregator(array, dates, 0);
        TSAggregator.Aggregate aggrResult = aggr.toMonthly();
        double[] a = aggrResult.values;

        double[] spiArray;
        StandardPrecipitationIndex.MISSING_DATA_VALUE = Double.NaN;

        for (int i = 0; i < aggrMonths.length; i++) {
            spiArray = StandardPrecipitationIndex.calcSPIn(Arrays.copyOf(a, a.length), aggrMonths[i].getValue());
            round(spiArray);
            this.spiArray[i].setValue(spiArray);
        }

    }

    private void round(double[] a) {
        for (int n = 0; n < a.length; n++) {
            a[n] = ((double) Math.round(a[n] * 100)) / 100;
        }
    }

}
