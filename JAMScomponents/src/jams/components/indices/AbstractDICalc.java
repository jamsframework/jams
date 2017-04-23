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
            description = "The simulation time interval"
    )
    public Attribute.TimeInterval timeInterval;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "Temporal resolution of baseline statistics [daily,weekly,monthly]",
            defaultValue = "weekly"
    )
    public Attribute.String tres;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "Output of long-term statistics for each(!) entity",
            defaultValue = "false"
    )
    public Attribute.Boolean debug;

    private int statRes = 0;

    /*
     *  Component run stages
     */
    @Override
    public void init() {
        if (tres.getValue().equals("weekly")) {
            statRes = 53;
        } else if (tres.getValue().equals("monthly")) {
            statRes = 12;
        } else if (tres.getValue().equals("daily")) {
            statRes = 366;
        } else {
            getModel().getRuntime().sendHalt("Value of parameter tres must be "
                    + "either \"monhtly\" or \"weekly\" or \"daily\"");
        }
    }

    @Override
    public abstract void run();

    protected int getTimeIndex(Attribute.Calendar time) {
        switch (statRes) {
            case 12:
                return time.get(Calendar.MONTH);
            case 53:
                return time.get(Calendar.WEEK_OF_YEAR) - 1;
            default:
                return time.get(java.util.Calendar.DAY_OF_YEAR) - 1;
        }
    }

    protected Stats calcStats(List<Double> valueList) {

        List<Double>[] groupedValues = new List[statRes];
        for (int i = 0; i < groupedValues.length; i++) {
            groupedValues[i] = new ArrayList();
        }

        Attribute.Calendar time = getModel().getRuntime().getDataFactory().createCalendar();
        time.setValue(timeInterval.getStart());

        for (double value : valueList) {
            int timeIndex = getTimeIndex(time);
            groupedValues[timeIndex].add(value);
            time.add(timeInterval.getTimeUnit(), timeInterval.getTimeUnitCount());
        }

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

    class Stats {

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
