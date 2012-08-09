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
public class VariableMultiply extends JAMSComponent {

    @JAMSVarDescription(
    access = JAMSVarDescription.AccessType.READ,
    update = JAMSVarDescription.UpdateType.RUN,
    description = "Current time")
    public JAMSCalendar time;
    @JAMSVarDescription(
    access = JAMSVarDescription.AccessType.READ,
    update = JAMSVarDescription.UpdateType.RUN,
    description = "value attribute")
    public JAMSDouble input;
    @JAMSVarDescription(
    access = JAMSVarDescription.AccessType.READWRITE,
    update = JAMSVarDescription.UpdateType.RUN,
    description = "sum attribute")
    public JAMSDouble output;
    private long count;

    public void init() {
    }

    public void run() {
        int max_d = time.getActualMaximum(time.DAY_OF_MONTH);

        output.setValue(input.getValue() / (double) max_d);
    }

    public void cleanup() {
    }
}
