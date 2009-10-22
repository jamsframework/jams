/*
 * BooleanConditionalContext.java
 * Created on 7. Januar 2008, 09:34
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
package jams.model;

import jams.model.*;
import jams.data.JAMSBoolean;

/**
 *
 * @author S. Kralisch
 */
@JAMSComponentDescription(title = "BooleanConditionalContext", author = "Sven Kralisch", date = "7. January 2008", description = "This component represents a JAMS context which can be used to " +
"conditionally execute components. This context must contain two components. If \"condition\" is true, the first one will be executed, otherwise the second one.")
public class BooleanConditionalContext extends JAMSContext {

    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ, update = JAMSVarDescription.UpdateType.INIT, description = "Boolean attribute defining which component to execute")
    public JAMSBoolean condition;

    public BooleanConditionalContext() {
    }
    
    @Override
    public ComponentEnumerator getRunEnumerator() {
        return new RunEnumerator();
    }
    
    @Override
    public ComponentEnumerator getChildrenEnumerator() {
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
            if (condition.getValue()) {
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
