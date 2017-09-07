/*
 * SMDI_DataCollect.java
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

/**
 *
 * @author Sven Kralisch <sven.kralisch at uni-jena.de>
 */
@JAMSComponentDescription(
        title = "Soil Moisture Deficit Index (SMDI) Data Collector",
        author = "Sven Kralisch",
        description = "This component collects required data to calculate the Soil Moisture Deficit Index (SMDI)",
        date = "2017-04-17",
        version = "1.0_0"
)
@VersionComments(entries = {
    @VersionComments.Entry(version = "1.0_0", comment = "Initial version")
})
public class SMDI_DataCollect extends JAMSComponent {

    /*
     *  Component attributes
     */
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "Soil water content",
            defaultValue = "0"
    )
    public Attribute.Double soilWater;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = "The simulation time interval"
    )
    public Attribute.TimeInterval simulationTimeInterval;

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
            description = "Sum of recent values collected"
    )
    public Attribute.Double soilWater_sum;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = "Counter of overall values stored"
    )
    public Attribute.Integer counter;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = "Array of collected soil water content values"
    )
    public Attribute.DoubleArray swValues;


    int arraySize, tres;

    /*
     *  Component run stages
     */
    @Override
    public void init() {
        tres = tempRes.getValue();
        arraySize = (int) simulationTimeInterval.getNumberOfTimesteps() / tres;
    }

    @Override
    public void initAll() {
        swValues.setValue(new double[arraySize]);
        soilWater_sum.setValue(0);
        counter.setValue(0);
    }

    @Override
    public void run() {

        int day = date.get(Attribute.Calendar.DAY_OF_YEAR);
        
        //aggregate the last day in leapyears with the last collected value
        if (day == 366) {
            int oldSlot = counter.getValue() - 1;
            double oldValue = swValues.getValue()[oldSlot];
            double newValue = (oldValue * tres + soilWater.getValue()) / (tres + 1);
            swValues.getValue()[oldSlot] = newValue;
            return;
        }
        
        double s = soilWater_sum.getValue();
        int c = counter.getValue();

        //aggregate the remaining values not yet collected eith the last
        //collected values
        if (day == 1 && tres > 1 && c > 0) {
            //get number of remaining values not yet counted
            int n = 365 % tres;
            //get value from recent slot and add remaining soilWater_sum
            int oldSlot = counter.getValue() - 1;
            double oldValue = swValues.getValue()[oldSlot];
            double newValue = (oldValue * tres + s) / (tres + n);
            //write back new value
            swValues.getValue()[oldSlot] = newValue;
            s = 0;
        }
        
        s = s + soilWater.getValue();

        if (day % tres == 0) {
            swValues.getValue()[c] = s / tres;
            counter.setValue(c+1);
            s = 0;
        }

        soilWater_sum.setValue(s);
    }
//
//    public static void main(String[] args) {
//        Attribute.Calendar c = JAMSDataFactory.createCalendar();
//        c.set(2016, 11, 31, 0, 0, 0);
//        System.out.println(c.get(Calendar.WEEK_OF_YEAR));
////        c.add(Calendar.DAY_OF_YEAR, -1);
//        int day = c.get(Calendar.DAY_OF_YEAR);
//        System.out.println(day);
//        System.out.println(c);
//        int slot = day / 7 - 1;
//        System.out.println(slot);
//        System.out.println(day % 7);
//
//        Attribute.Calendar c2 = JAMSDataFactory.createCalendar();
//        c2.set(2015, 11, 31, 0, 0, 0);
//        Attribute.TimeInterval ti = JAMSDataFactory.createTimeInterval();
//        ti.setStart(c);
//        ti.setEnd(c2);
//        ti.setTimeUnit(6);
//        ti.setTimeUnitCount(1);
//        
//        System.out.println(ti.getNumberOfTimesteps());
//    }
}
