/*
 * BooleanConditionalContext.java
 * Created on 7. Januar 2008, 09:34
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

/**
 *
 * @author S. Kralisch
 */
@JAMSComponentDescription(title = "BooleanConditionalContext", author = "Sven Kralisch", date = "7. January 2008", description = "This component represents a JAMS context which can be used to "
+ "conditionally execute components. This context must contain two components. If \"condition\" is true, the first one will be executed, otherwise the second one.")
public class BooleanConditionalExecutionContext extends JAMSContext {

    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ, update = JAMSVarDescription.UpdateType.INIT, description = "Boolean attribute defining if the component should be executed")
    public Attribute.Boolean condition;
    
    public BooleanConditionalExecutionContext() {
    }

    @Override
    public ComponentEnumerator getInitEnumerator() {        
        if (condition.getValue()){
            return super.getInitEnumerator();
        }
        return new EmptyEnumerator();
    }
    
    @Override
    public ComponentEnumerator getInitAllEnumerator() {        
        if (condition.getValue()){
            return super.getInitAllEnumerator();
        }
        return new EmptyEnumerator();
    }
    
    @Override
    public ComponentEnumerator getRunEnumerator() {
        if (condition.getValue()){
            return super.getRunEnumerator();
        }
        return new EmptyEnumerator();
    }
    
    @Override
    public ComponentEnumerator getCleanupEnumerator() {
        if (condition.getValue()){
            return super.getCleanupEnumerator();
        }
        return new EmptyEnumerator();
    }

    @Override
    public long getNumberOfIterations() {
        return 1;
    }

    class EmptyEnumerator implements ComponentEnumerator {

        final DummyComponent dummy = new DummyComponent();        
        boolean next = true;

        public class DummyComponent extends JAMSComponent {
        }

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
            return dummy;
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
