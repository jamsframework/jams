/*
 * CalendarOutput.java
 * Created on 21. March 2007, 17:26
 *
 * This file is part of JAMSConstants
 * Copyright (C) 2007 FSU Jena
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

package jams.components.io;

import jams.JAMSConstants;
import jams.data.*;
import jams.model.*;

/**
 *
 * @author Sven Kralisch
 */
@JAMSComponentDescription(
title="TimeOutput",
        author="Sven Kralisch",
        description="Output value of calendar data at run stage"
        )
        public class CalendarOutput extends JAMSComponent {
    
    /*
     *  Component variables
     */
    
    @JAMSVarDescription(
    access = JAMSVarDescription.AccessType.READ,
            update = JAMSVarDescription.UpdateType.RUN,
            description = "Date object to be output"
            )
            public JAMSCalendar value;
    
    
    /*
     *  Component run stages
     */    
    public void run() {
        getModel().getRuntime().println(this.getInstanceName()+ ": " + value.toString(), JAMSConstants.STANDARD);
    }
    
}
