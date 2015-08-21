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

/**
 *
 * @author S. Kralisch
 */
public class IsNewTimeStep extends JAMSComponent {

    @JAMSVarDescription(
    access = JAMSVarDescription.AccessType.READ,
    description = "Current time")
    public Attribute.Calendar time;
    @JAMSVarDescription(
    access = JAMSVarDescription.AccessType.READWRITE,
    description = "Current time")
    public Attribute.Calendar oldTime;
    @JAMSVarDescription(
    access = JAMSVarDescription.AccessType.WRITE,
    description = "sum attribute")
    public Attribute.Boolean isNewTimeStep;

    public void run() {
        if (time.getTimeInMillis() == oldTime.getTimeInMillis()){
            isNewTimeStep.setValue(false);
        }else{
            isNewTimeStep.setValue(true);
        }
        oldTime.setValue(time.getValue());
    }
}
