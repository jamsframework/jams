/*
 * TemporalSumAggregator.java
 * Created on 19. Juli 2006, 11:57
 *
 * This file is part of JAMS
 * Copyright (C) 2005 FSU Jena
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public License
 * as published by the Free Software Foundation; either version 3
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, see <http://www.gnu.org/licenses/>.
 *
 */
package jams.components.aggregate;

import jams.model.*;
import jams.data.*;
import jams.data.Attribute.Calendar;
import jams.data.Attribute.TimeInterval;
import java.util.ArrayList;

/**
 *
 * @author S. Kralisch
 */
@JAMSComponentDescription(title = "TemporalPeriodeAggregator",
author = "Christian Fischer",
date = "2013-05-13",
version = "1.0_0",
description = "Component to calculate monthly averages")
public class SubTimeStepGenerator extends JAMSComponent {

    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
    description = "Current time")
    public Attribute.TimeInterval interval;
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
    description = "offset date")
    public Attribute.Calendar time;
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
    description = "timeunit count to be aggregated",
    defaultValue = "6")
    public Attribute.Integer subTimestepUnit;
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
    description = "count of timeunits to be aggregated",
    defaultValue = "1")
    public Attribute.Integer subTimestepUnitCount;
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
    description = "offset date")
    public Attribute.Calendar baseDate;
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READWRITE,
    description = "timestep counter")
    public Attribute.Double currentTimestepCount;
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READWRITE,
    description = "last timestep in subperiod")
    public Attribute.Boolean subTimestepFinished;
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READWRITE,
    description = "first timestep in subperiod")
    public Attribute.Boolean subTimestepStarted;
    ArrayList<TimeInterval> subTimesteps = new ArrayList<TimeInterval>();

    public void init() {
        Calendar nextTimestep = DefaultDataFactory.getDataFactory().createCalendar();

        if (baseDate == null) {
            nextTimestep.setValue(this.interval.getStart().getValue());
        } else {
            nextTimestep.setValue(this.baseDate.getValue());
        }

        nextTimestep.removeUnsignificantComponents(this.interval.getTimeUnit());

        while (nextTimestep.before(interval.getStart())) {
            nextTimestep.add(this.subTimestepUnit.getValue(), this.subTimestepUnitCount.getValue());
        }

        Calendar time_current = interval.getStart().clone();
        Calendar time_end = interval.getEnd().clone();
        time_end.add(interval.getTimeUnit(), interval.getTimeUnitCount());
        Calendar nextTimeStepStart = null;
        Calendar nextTimeStepEnd = null;

        while (time_current.compareTo(time_end) <= 0) {

            if (nextTimestep.compareTo(time_current)<=0) {
                if (nextTimeStepStart != null) {
                    time_current.add(interval.getTimeUnit(), -interval.getTimeUnitCount());
                    nextTimeStepEnd = time_current.clone();
                    time_current.add(interval.getTimeUnit(), interval.getTimeUnitCount());
                    TimeInterval subInterval = DefaultDataFactory.getDataFactory().createTimeInterval();
                    subInterval.setStart(nextTimeStepStart);
                    subInterval.setEnd(nextTimeStepEnd);
                    subInterval.setTimeUnit(interval.getTimeUnit());
                    subInterval.setTimeUnitCount(interval.getTimeUnitCount());
                    subTimesteps.add(subInterval);
                }
                nextTimeStepStart = time_current.clone();
                nextTimestep.add(this.subTimestepUnit.getValue(), this.subTimestepUnitCount.getValue());
            }
            time_current.add(interval.getTimeUnit(), interval.getTimeUnitCount());
        }

        for (TimeInterval timestep : subTimesteps) {
            System.out.println(timestep.getStart() + "->" + timestep.getEnd() + ":" + timestep.getNumberOfTimesteps());
        }
    }

    @Override
    public void run() {
        for (int i = 0; i < subTimesteps.size(); i++) {
            if (time.getTimeInMillis() == this.subTimesteps.get(i).getStart().getTimeInMillis()) {
                currentTimestepCount.setValue(this.subTimesteps.get(i).getNumberOfTimesteps());
                subTimestepStarted.setValue(true);
                subTimestepFinished.setValue(false);
                return;
            }
            if (time.getTimeInMillis() == this.subTimesteps.get(i).getEnd().getTimeInMillis()) {
                subTimestepStarted.setValue(false);
                subTimestepFinished.setValue(true);
                return;
            }
        }
        subTimestepStarted.setValue(false);
        subTimestepFinished.setValue(false);
    }
}
