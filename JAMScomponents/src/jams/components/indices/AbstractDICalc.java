/*
 * SMDI_Calc.java
 * Created on 18.04.2017, 22:56:45
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

import jams.data.*;
import jams.data.Attribute.Calendar;
import jams.model.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 *
 * @author Sven Kralisch <sven.kralisch at uni-jena.de>
 */
@JAMSComponentDescription(
        title = "Soil Moisture Deficit Index (SMDI) Calculator",
        author = "Sven Kralisch",
        description = "This component calculates the Soil Moisture Deficit Index (SMDI)",
        date = "2017-04-17",
        version = "1.0_0"
)
@VersionComments(entries = {
    @VersionComments.Entry(version = "1.0_0", comment = "Initial version")
})
public abstract class AbstractDICalc extends JAMSComponent {

    /*
     *  Component attributes
     */
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "Current date"
    )
    public Attribute.Calendar date;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "Size of aggregation window for baseline "
                    + "statistics (e.g. \"7\" for weekly stats)",
            defaultValue = "7"
    )
    public Attribute.Integer tempRes;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = "Counter of overall values stored"
    )
    public Attribute.Integer counter;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "Output of long-term statistics for each(!) entity",
            defaultValue = "false"
    )
    public Attribute.Boolean debug;

    protected int statRes = 0, tres = 1;

    /*
     *  Component run stages
     */
    @Override
    public void init() {
        // leapyears not considered for stats calculation
        tres = tempRes.getValue();
        statRes = 365 / tres;
    }
    
    @Override
    public void initAll() {
        counter.setValue(0);
    }    
    
    @Override
    public abstract void run();

    protected Stats calcStats(double[] valueList) {

        List<Double>[] groupedValues = new List[statRes];
        for (int i = 0; i < groupedValues.length; i++) {
            groupedValues[i] = new ArrayList();
        }

        //sort values into corresponding slots of long-term stats
        int index = 0;
        for (double value : valueList) {
            groupedValues[index].add(value);
            index++;
            if (index == statRes) {
                index = 0;
            }
        }

        // calculate the mean, min and max for each slot of long-term stats
        Stats stats = new Stats(groupedValues.length);

        for (int i = 0; i < groupedValues.length; i++) {

            List<Double> list = (List) groupedValues[i];
            List<Double> sortedList = new ArrayList(list);

            if (list.isEmpty()) {
                continue;
            }

            Collections.sort(sortedList);

            if (sortedList.size() % 2 == 0) {
                stats.median[i] = (sortedList.get(sortedList.size() / 2) + sortedList.get(sortedList.size() / 2 - 1)) / 2;
            } else {
                stats.median[i] = sortedList.get(sortedList.size() / 2);
            }

            stats.min[i] = sortedList.get(0);
            stats.max[i] = sortedList.get(sortedList.size() - 1);

        }
        if (debug.getValue()) {
            getModel().getRuntime().println("Long-term stats:");
            for (int i = 0; i < groupedValues.length; i++) {
                getModel().getRuntime().println(i + ":\t" + stats.min[i] + "\t" + stats.median[i] + "\t" + stats.max[i]);
            }
        }

        return stats;
    }

    public class Stats {

        double min[];
        double max[];
        double median[];

        public Stats(int size) {
            min = new double[size];
            max = new double[size];
            median = new double[size];
        }
    }

}
