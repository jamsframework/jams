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
import jams.model.*;

/**
 *
 * @author Sven Kralisch <sven.kralisch at uni-jena.de>
 */
@JAMSComponentDescription(
        title = "Evapotranspiration Deficit Index (ETDI) Data Collector",
        author = "Sven Kralisch",
        description = "This component collects required data to calculate the Evapotranspiration Deficit Index (ETDI)",
        date = "2017-04-17",
        version = "1.0_0"
)
@VersionComments(entries = {
    @VersionComments.Entry(version = "1.0_0", comment = "Initial version")
})
public class ETDI_DataCollect extends JAMSComponent {

    /*
     *  Component attributes
     */
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "Potential evapotranspiration",
            defaultValue = "0"
    )
    public Attribute.Double potET;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "Actual evapotranspiration",
            defaultValue = "0"
    )
    public Attribute.Double actET;
    
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
            description = "Sum of recent potET values collected"
    )
    public Attribute.Double potET_sum;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = "Sum of recent actET values collected"
    )
    public Attribute.Double actET_sum;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = "Counter of overall values stored"
    )
    public Attribute.Integer counter;    

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = "Array of collected water stress values"
    )
    public Attribute.DoubleArray wsValues;

    

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
        wsValues.setValue(new double[arraySize]);
        potET_sum.setValue(0);
        counter.setValue(0);
    }

    @Override
    public void run() {

        int day = date.get(Attribute.Calendar.DAY_OF_YEAR);
        
        //aggregate the last day in leapyears with the last collected value
        if (day == 366) {
            int oldSlot = counter.getValue() - 1;
            double oldValue = wsValues.getValue()[oldSlot];
            //calculate the new WS value
            double ws = (potET.getValue() - actET.getValue()) / potET.getValue();
            double newValue = (oldValue * tres + ws) / (tres + 1);
            wsValues.getValue()[oldSlot] = newValue;
            return;
        }
        
        double sPET = potET_sum.getValue();
        double sAET = actET_sum.getValue();
        int c = counter.getValue();

        //aggregate the remaining values not yet collected eith the last
        //collected values
        if (day == 1 && tres > 1 && c > 0) {
            //get number of remaining values not yet counted
            int n = 365 % tres;
            //get value from recent slot and add remaining soilWater_sum
            int oldSlot = counter.getValue() - 1;
            double oldValue = wsValues.getValue()[oldSlot];
            //calculate the new average WS value for the remaining days
            double ws = (sPET - sAET) / sPET;
            //calculate weighted mean of old and new WS values
            double newValue = (oldValue * tres + ws * n) / (tres + n);
            //write back new value
            wsValues.getValue()[oldSlot] = newValue;
            sPET = 0;
            sAET = 0;
        }        
        
        //sum up actET and potET values
        sPET = sPET + potET.getValue();
        sAET = sAET + actET.getValue();

        if (day % tres == 0) {
            //calculate the WS based on actET/potET sums
            double ws = (sPET - sAET) / sPET;
            wsValues.getValue()[c] = ws;
            counter.setValue(c+1);
            sPET = 0;
            sAET = 0;
        }

        potET_sum.setValue(sPET);
        actET_sum.setValue(sAET);
    }    
    
}
