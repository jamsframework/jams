/*
 * JAMS_Scenario.java
 * Created on 14. August 2007, 15:28
 *
 * This file is part of JAMS
 * Copyright (C) 2005 FSU Jena, c8fima
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA
 *
 */

package org.jams.j2k.s_n.management;

import jams.data.*;
import jams.model.*;

/**
 *
 * @author c8fima
 */
@JAMSComponentDescription(
title="JAMS_Scenario",
        author="Manfred Fink",
        description="Sets Parameter for Scenario calulation in dependence of the calendar"
        )
        public class JAMS_Scenario extends JAMSComponent {
    
    /*
     *  Component variables
     */
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "Date to start the scenario"
            )
            public Attribute.Calendar start;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "Date to end the scenario"
            )
            public Attribute.Calendar end;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "Normal Value"
            )
            public Attribute.Double StandardValue;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "Scenario Value"
            )
            public Attribute.Double ScenarioValue;
    
     @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            description = "Output Value"
            )
            public Attribute.Double OutputValue;
     
      @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "Encapsulating time interval"
            )
            public Attribute.TimeInterval timeInterval;
      
      @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "Current time"
            )
            public Attribute.Calendar time;
    
    
    /*
     *  Component run stages
     */
    
     private Attribute.TimeInterval ti;
     
    @Override
    public void init() {
     ti = this.getModel().getRuntime().getDataFactory().createTimeInterval();
     ti.setStart(start);
     ti.setEnd(end);
     ti.setTimeUnit(timeInterval.getTimeUnit());
     ti.setTimeUnitCount(timeInterval.getTimeUnitCount());
    }
    
    @Override
    public void run() {
     double value = 0;
        if (time.after(ti.getStart()) && time.before(ti.getEnd())) {
        
         value = ScenarioValue.getValue();           
        } else {
         value = StandardValue.getValue();  
        }
     
     OutputValue.setValue(value);
     
        
    }
    
    @Override
    public void cleanup() {
        
    }
}
