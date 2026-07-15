/*
 * DataLoop.java
 * Created on 24. April 2013, 14:24
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
package jams.components.debug;

import jams.data.*;
import jams.model.Component;
import jams.model.ComponentEnumerator;
import jams.model.JAMSComponentDescription;
import jams.model.JAMSContext;
import jams.model.JAMSVarDescription;

/**
 *
 * @author S. Kralisch
 */
@JAMSComponentDescription(title = "DataLoop",
author = "Sven Kralisch",
date = "2013-04-24",
version = "1.0_0",
description = "This context loops over a double interval defined by start "
        + "value, end value and a step size")
public class DataLoop extends JAMSContext {

    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
    description = "Start value")
    public Attribute.Double start;

    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
    description = "End value")
    public Attribute.Double end;
    
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
    description = "Step size")
    public Attribute.Double step;
    
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.WRITE,
    description = "Current value")
    public Attribute.Double current;
    
    public DataLoop() {
        super();
    }

    @Override
    public void init() {
        super.init();
        current.setValue(start.getValue());
    }

    @Override
    public void run() {
        super.run();
    }

    @Override
    public ComponentEnumerator getRunEnumerator() {
        // check if there are components to iterate on
        if (!components.isEmpty()) {
            // if yes, return standard enumerator
            return new RunEnumerator();
        } else {
            // if not, return empty enumerator
            return new RunEnumerator() {

                @Override
                public boolean hasNext() {
                    return false;
                }

                @Override
                public Component next() {
                    return null;
                }

                @Override
                public void reset() {
                }
            };
        }
    }

    @Override
    public long getNumberOfIterations() {
        return Math.round(start.getValue() - end.getValue() / step.getValue());
    }

    @Override
    public String getTraceMark() {
        return current.toString();
    }

    class RunEnumerator implements ComponentEnumerator {

        ComponentEnumerator ce = getChildrenEnumerator();
        //DataTracer dataTracers = getDataTracer();

        @Override
        public boolean hasNext() {
            boolean nextValue = current.getValue() < end.getValue() - step.getValue();
            boolean nextComp = ce.hasNext();
            return (nextValue || nextComp);
        }

        @Override
        public boolean hasPrevious() {
            boolean prevTime = current.getValue() >= start.getValue() + step.getValue();
            boolean prevComp = ce.hasPrevious();
            return (prevTime || prevComp);
        }

        @Override
        public Component next() {
            // check end of component elements list, if required switch to the next
            // value start with the new Component list again
            if (!ce.hasNext() && current.getValue() < end.getValue() - step.getValue()) {            
                current.setValue(current.getValue() + step.getValue());
                ce.reset();
            }
            return ce.next();
        }

        @Override
        public void reset() {
            current.setValue(start.getValue());
            ce.reset();
        }

        public Component previous() {
            if (ce.hasPrevious()) {
                return ce.previous();
            } else {
                current.setValue(current.getValue() - step.getValue());
                while (ce.hasNext()) {
                    ce.next();
                }
                return ce.previous();
            }
        }
    }
}
