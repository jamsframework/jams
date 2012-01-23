/*
 * DoubleConditionalContext.java
 * Created on 9. April 2008, 11:31
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

import jams.data.JAMSDouble;
import jams.model.Component;
import jams.model.ComponentEnumerator;
import jams.model.JAMSComponent;
import jams.model.JAMSComponentDescription;
import jams.model.JAMSContext;
import jams.model.JAMSVarDescription;

/**
 *
 * @author S. Kralisch
 */
@JAMSComponentDescription(title = "DoubleConditionalContext", author = "Sven Kralisch", date = "9. April 2008", description = "This component represents a JAMS context which can be used to "
+ "conditionally execute components. This context must contain two components. If \"value1\" equals \"value2\", the first one will be executed, otherwise the second one.")
public class DoubleConditionalContext extends JAMSContext {

    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ, update = JAMSVarDescription.UpdateType.INIT, description = "Boolean attribute defining which component to execute")
    public JAMSDouble value1;
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ, update = JAMSVarDescription.UpdateType.INIT, description = "Boolean attribute defining which component to execute")
    public JAMSDouble value2;

    public class DummyComponent extends JAMSComponent {
        public void run() {
            return;
        }
    }

    public DoubleConditionalContext() {
    }

    @Override
    public ComponentEnumerator getRunEnumerator() {
        return new RunEnumerator();
    }

    @Override
    public long getNumberOfIterations() {
        return 1;
    }

    class RunEnumerator implements ComponentEnumerator {

        final DummyComponent dummy = new DummyComponent();
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
            if (value1.getValue() == value2.getValue()) {
                return compArray[0];
            } else {
                if (compArray.length < 1 || compArray[1] == null) {
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
}
