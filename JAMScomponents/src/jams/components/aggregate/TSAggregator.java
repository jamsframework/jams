/*
 * TSAggregator.java
 * Created on 13.03.2019, 17:10:10
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

import jams.data.Attribute;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Sven Kralisch <sven.kralisch@uni-jena.de>
 */
public class TSAggregator {

    public static final int MODE_SUM = 0;
    public static final int MODE_AVG = 1;
    private double[] values;
    private List<Attribute.Calendar> dates;
    private int mode;

    public TSAggregator(double[] values, List<Attribute.Calendar> dates, int mode) {
        this.dates = dates;
        this.values = values;
        this.mode = mode;
    }

    public double[] toMonthly() {
        return toTimeInterval(Attribute.Calendar.MONTH);
    }

    public double[] toWeekly() {
        return toTimeInterval(Attribute.Calendar.WEEK_OF_YEAR);
    }
    
    public double[] toTimeInterval(int timeField) {

        List<Double> result = new ArrayList();
        List<Double> counts = new ArrayList();
        Attribute.Calendar date = dates.get(0);
        double sum = 0, count = 0;

        for (int i = 0; i < values.length; i++) {

            if (dates.get(i).get(timeField) == date.get(timeField)) {

                sum += values[i];
                count++;

            } else {

                result.add(sum);
                counts.add(count);

                sum = values[i];
                count = 1;

                date = dates.get(i);

            }
        }

        result.add(sum);
        counts.add(count);

        double[] a = new double[result.size()];
        for (int i = 0; i < result.size(); i++) {
            a[i] = result.get(i);
        }

        if (mode == MODE_AVG) {
            for (int i = 0; i < counts.size(); i++) {
                a[i] /= counts.get(i);
            }
        }

        return a;
    }

}
