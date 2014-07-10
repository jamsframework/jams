/*
 * SwitchContext.java
 * Created on 12.11.2012, 11:53:59
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
package jams.components.conditional;

import jams.data.*;
import jams.model.*;

/**
 *
 * @author Sven Kralisch <sven.kralisch at uni-jena.de>
 */
@JAMSComponentDescription(
        title = "SwitchContext",
        author = "Sven Kralisch",
        date = "12. November 2012",
        version = "1.0_1",
        description = "This component represents a JAMS context which can be used to "
        + "switch between alternative components. It executes child component number i "
        + "if the the given attribute matches the i-th value. If no value matches, the "
        + "i+1-th child component is executed (if existing).")
@VersionComments(entries = @VersionComments.Entry(version = "1.0_1",
        comment = "Fixed minor issue with wrong counting of run() invocations"))
public class SwitchContext extends JAMSContext {

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "Double attribute to be compared with values")
    public Attribute.Double attribute;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "Double values to be compared which attribute")
    public Attribute.Double[] values;

    @Override
    public ComponentEnumerator getRunEnumerator() {
        return new SwitchContext.RunEnumerator();
    }

    @Override
    public long getNumberOfIterations() {
        return 0;
    }

    @Override
    public long getRunCount() {
        return 0;
    }

    public class RunEnumerator implements ComponentEnumerator {

        final JAMSComponent dummy = new JAMSComponent() {

            public void run() {
                return;
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
            // if condition is true return first component, else second component
            for (int i = 0; i < values.length; i++) {
                if (attribute.getValue() == values[i].getValue()) {
                    return compArray[i];
                }
            }
            if (compArray.length <= values.length) {
                return dummy;
            } else {
                return compArray[values.length];
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
}
