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
import java.util.GregorianCalendar;

/**
 *
 * @author S. Kralisch
 */
@JAMSComponentDescription(title = "TemporalPeriodeAggregator",
author = "Christian Fischer",
date = "2013-05-13",
version = "1.0_0",
description = "Component to calculate monthly averages")
public class MonthlyLowFlowDuration extends JAMSComponent {

    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
    description = "is subtimestep finished?")
    public Attribute.Boolean subTimeStepStarted;
    
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
    description = "Current time")
    public Attribute.Calendar time;
                
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
    description = "lowFlowThresholds")
    public Attribute.Double[] lowFlowThresholds;
    
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
    description = "runoff in cbm/s")
    public Attribute.Double totQ;
    
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.WRITE,
    description = "number of days with lowflow in timeperiod",
    defaultValue= "0")
    public Attribute.Integer lowflowDays;
                                    
    @Override
    public void run() { 
        
        if (subTimeStepStarted.getValue()){
            lowflowDays.setValue(0);
        }

        int m = time.get(Attribute.Calendar.MONTH);
        if (totQ.getValue() < lowFlowThresholds[m].getValue()){
            lowflowDays.setValue(lowflowDays.getValue()+1);
        }
    }        
}
