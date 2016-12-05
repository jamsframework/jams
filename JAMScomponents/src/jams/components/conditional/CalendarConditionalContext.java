/*
 * CalendarConditionalContext.java
 * Created on 05.12.2016, 18:04:46
 *
 * This file is part of JAMS
 * Copyright (C) FSU Jena
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
package jams.components.conditional;

import jams.data.*;
import jams.model.Component;
import jams.model.ComponentEnumerator;
import jams.model.JAMSComponent;
import jams.model.JAMSComponentDescription;
import jams.model.JAMSContext;
import jams.model.JAMSVarDescription;
import jams.model.VersionComments;

/**
 *
 * @author Sven Kralisch <sven.kralisch at uni-jena.de>
 */
@JAMSComponentDescription(title = "CalendarConditionalContext",
        author = "Sven Kralisch",
        date = "2016-12-05",
        description = "This component represents a JAMS context which can be used to "
        + "conditionally execute components. This context must contain two components. "
        + "If \"time\" matches \"timePattern\", the first one will be executed, otherwise the second one.",
        version = "1.0_1")
@VersionComments(entries = @VersionComments.Entry(version = "1.0_1", date = "2015-02-18",
        comment = "Fixed minor issue with wrong counting of run() invocations"))
public class CalendarConditionalContext extends JAMSContext {

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "Calendar value")
    public Attribute.Calendar time;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "Calendar pattern to be matched against")
    public Attribute.String timePattern;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "Julian day pattern to be matched against")
    public Attribute.String julDayPattern;

    public CalendarConditionalContext() {
    }

    @Override
    public ComponentEnumerator getRunEnumerator() {
        return new RunEnumerator();
    }

    @Override
    public long getNumberOfIterations() {
        return -1;
    }

    @Override
    public long getRunCount() {
        return -1;
    }

    public class RunEnumerator implements ComponentEnumerator {

        final JAMSComponent dummy = new JAMSComponent() {

            @Override
            public void run() {
            }
        };
        Component[] compArray = getCompArray();
        boolean next = true;

        @Override
        public boolean hasNext() {
            if (next) {
                next = false;
                return true;
            } else {
                return false;
            }
        }

        @Override
        public boolean hasPrevious() {
            return !hasNext();
        }

        @Override
        public Component next() {
            runCount++;
            // if condition is true return first component, else second component
            boolean b;
            if (julDayPattern != null) {
                b = Integer.toString(time.get(Attribute.Calendar.DAY_OF_YEAR)).matches(julDayPattern.getValue());
            } else {
                b = time.toString().matches(timePattern.getValue());
            }
            if (b) {
                return compArray[0];
            } else {
                if (compArray.length < 2 || compArray[1] == null) {
                    return dummy;
                }
                return compArray[1];
            }
        }

        @Override
        public Component previous() {
            return next();
        }

        @Override
        public void reset() {
            next = true;
        }
    }

    public static void main(String[] args) {
        String time = "2000-10-10 07:30";
        String pattern = "^.*-1[0-2]-.*";
        System.out.println(time.matches(pattern));
    }
}
