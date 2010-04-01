/*
 * DoubleConditionalContext.java
 * Created on 9. April 2008, 11:31
 *
 * This file is part of JAMS
 * Copyright (C) FSU Jena
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
package jams.components.conditional;

import jams.data.JAMSDouble;
import jams.model.Component;
import jams.model.ComponentEnumerator;
import jams.model.JAMSComponentDescription;
import jams.model.JAMSContext;
import jams.model.JAMSVarDescription;

/**
 *
 * @author S. Kralisch
 */
@JAMSComponentDescription(title = "DoubleConditionalContext", author = "Sven Kralisch", date = "9. April 2008", description = "This component represents a JAMS context which can be used to " +
"conditionally execute components. This context must contain two components. If \"value1\" equals \"value2\", the first one will be executed, otherwise the second one.")
public class DoubleConditionalContext extends JAMSContext {

    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ, update = JAMSVarDescription.UpdateType.INIT, description = "Boolean attribute defining which component to execute")
    public JAMSDouble value1;
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ, update = JAMSVarDescription.UpdateType.INIT, description = "Boolean attribute defining which component to execute")
    public JAMSDouble value2;

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
        public Component next() {
            // if condition is true return first component, else second component
            if (value1.getValue() == value2.getValue()) {
                return compArray[0];
            } else {
                return compArray[1];
            }
        }

        @Override
        public void reset() {
            next = true;
        }     

        // TODO
        @Override
        public void setState(byte[]state) {

        }

        // TODO
        @Override
        public byte[] getState() {
            return null;
        }
    }
}
