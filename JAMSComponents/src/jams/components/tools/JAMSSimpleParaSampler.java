/*
 * JAMSSimpleParaSampler.java
 * Created on 13. Februar 2006, 13:43
 *
 * This file is part of JAMS
 * Copyright (C) 2005 FSU Jena
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

package jams.components.tools;

import org.unijena.jams.data.*;
import org.unijena.jams.model.*;

/**
 *
 * @author S. Kralisch
 */
@JAMSComponentDescription(
        title="JAMS simple parameter sampler",
        author="Sven Kralisch",
        description="" +
        "This component creates a context for a repeated component execution, e.g. for model calibration." +
        "For this purpose <code>lowerBound</code> and <code>upperBound</code> define borders of an interval where " +
        "<code>count</code> double samples will be taken from. The samples will be distributed homogeneously over the " +
        "specified interval without regard of additional information. For each sample taken the init, run " +
        "and cleanup stages of all child components will be executed. At init stage the child components can " +
        "access the JAMSSimpleParaSampler's <code>value</code> attribute which provides the current sample value. " +
        "Multiple JAMSSimpleParaSamplers can be nested in order to sample a multi-dimensional parameter " +
        "space.</br>" +
        "An example application of the JAMSSimpleParaSampler could look as follows:</br>" +
        "<code>" +
        "<compoundcomponent class=\"org.unijena.jams.tools.JAMSSimpleParaSampler\" name=\"ParaSampler1\"></br>" +
        "    <jamsvar name=\"lowerBound\" value=\"3\"/></br>" +
        "    <jamsvar name=\"upperBound\" value=\"4\"/></br>" +
        "    <jamsvar name=\"count\" value=\"3\"/></br>" +
        "    <compoundcomponent class=\"org.unijena.jams.tools.JAMSSimpleParaSampler\" name=\"ParaSampler1\"></br>" +
        "        <jamsvar name=\"lowerBound\" value=\"3\"/></br>" +
        "        <jamsvar name=\"upperBound\" value=\"4\"/></br>" +
        "        <jamsvar name=\"count\" value=\"3\"/></br>" +
        "        <component class=\"...\" name=\"Process\"></br>" +
        "           ...</br>" +
        "           <jamsvar name=\"p1\" provider=\"ParaSampler1\" value=\"value\"/></br>" +
        "           <jamsvar name=\"p2\" provider=\"ParaSampler2\" value=\"value\"/></br>" +
        "           ...</br>" +
        "        </component>" +
        "    </compoundcomponent>" +
        "</compoundcomponent>" +
        "</code>")
public class JAMSSimpleParaSampler extends JAMSContext {
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            update = JAMSVarDescription.UpdateType.INIT,
            description = "Sampling interval lower boundary"
            )
            public JAMSDouble lowerBound;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            update = JAMSVarDescription.UpdateType.INIT,
            description = "Sampling interval upper boundary"
            )
            public JAMSDouble upperBound;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            update = JAMSVarDescription.UpdateType.INIT,
            description = "Number of samples"
            )
            public JAMSInteger count;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            update = JAMSVarDescription.UpdateType.RUN,
            description = "current parameter value"
            )
            public JAMSDouble value = new JAMSDouble(0);
    
    
    double stepSize;
    
    public void init() {
        if (count.getValue() < 2)
            count.setValue(2);
        stepSize = (upperBound.getValue()-lowerBound.getValue()) / (count.getValue()-1);
        value.setValue(lowerBound.getValue());
    }
    
    public void run() {
        
        if (runEnumerator == null) {
            runEnumerator = super.getChildrenEnumerator();
        }
        
        while (value.getValue() <= upperBound.getValue()) {
                        
            runEnumerator.reset();
            while(runEnumerator.hasNext()) {
                JAMSComponent comp = runEnumerator.next();
                //comp.updateInit();
                try {
                    comp.init();
                } catch (Exception e) {
                    getModel().getRuntime().handle(e, comp.getInstanceName());
                }
            }
            
            runEnumerator.reset();
            while(runEnumerator.hasNext()) {
                JAMSComponent comp = runEnumerator.next();
                //comp.updateRun();
//                comp.updateReadEntity();
                try {
                    comp.run();
                } catch (Exception e) {
                    getModel().getRuntime().handle(e, comp.getInstanceName());
                }
//                comp.updateWriteEntity();
            }
            
            runEnumerator.reset();
            while(runEnumerator.hasNext()) {
                JAMSComponent comp = runEnumerator.next();
                try {
                    comp.cleanup();
                } catch (Exception e) {
                    getModel().getRuntime().handle(e, comp.getInstanceName());
                }
            }
            value.setValue(value.getValue()+stepSize);
        }
    }
    
    public void cleanup() {
        //do nothing here
    }
    
    public JAMSComponentEnumerator getRunEnumerator() {
        return new RunEnumerator();
    }
    
    public JAMSComponentEnumerator getChildrenEnumerator() {
        return super.getRunEnumerator();
    }
    
    public long getNumberOfIterations() {
        return count.getValue();
    }
    
    class RunEnumerator implements JAMSComponentEnumerator {
        
        JAMSComponentEnumerator ce = getChildrenEnumerator();
        
        public boolean hasNext() {
            boolean nextValue = value.getValue() <= (upperBound.getValue()-stepSize);
            boolean nextComp = ce.hasNext();
            return (nextValue || nextComp) ;
        }
        
        public JAMSComponent next() {
            // check end of component elements list, if required switch to the next
            // value and start with the new Component list again
            if (!ce.hasNext() && (value.getValue() <= (upperBound.getValue()-stepSize))) {
                value.setValue(value.getValue()+stepSize);
                ce.reset();
            }
            return ce.next();
        }
        
        public void reset() {
            value.setValue(lowerBound.getValue());
            ce.reset();
        }
    }
}
