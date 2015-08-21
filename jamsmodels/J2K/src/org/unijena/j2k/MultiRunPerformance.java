/*
 * MultipleModelRunContext.java
 * Created on 10. July 2007, 17:03
 *
 * This file is part of JAMS
 * Copyright (C) 2005 S. Kralisch and P. Krause
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

package org.unijena.j2k;

import jams.data.*;
import jams.model.*;

/**
 *
 * @author nsk
 */
@JAMSComponentDescription(
title="InErrorContext",
        author="Peter Krause",
        description="Context component which helps in estimating the performance" +
        "The component lets the model run for a specified number of times."
        )
        public class MultiRunPerformance extends JAMSContext {
    
    /*
     *  Component variables
     */
    
    @JAMSVarDescription(
    access = JAMSVarDescription.AccessType.READ,
            description = "Number of model evaluations"
            )
            public Attribute.Integer modelRuns;
    
    @JAMSVarDescription(
    access = JAMSVarDescription.AccessType.READ,
            description = "Flag for dis/enabling this sampler"
            )
            public Attribute.Boolean enable;
    
    
    int currentCount; 
    int runCounter = 0;
    
    public void init() {
        
    }
    
    public void run() {
        if (runEnumerator == null) {
            runEnumerator = getChildrenEnumerator();
        }
        
        //component disabled, just a normal model run
        if (!enable.getValue()) {
            singleRun();  
        } 
        //component enabled
        else{
            while (hasNext()) {
                singleRun();     
                this.runCounter++;
                System.out.println("run: " + this.runCounter);
            }
            
            runEnumerator.reset();
            while(runEnumerator.hasNext() && doRun) {
                Component comp = runEnumerator.next();
            }
        }
    }
    
    public void cleanup() {
        
       
            
            
    }
    
    private void singleRun() {     
        runEnumerator.reset();
        while(runEnumerator.hasNext() && doRun) {
            Component comp = runEnumerator.next();
            this.currentCount++;
            //comp.updateInit();
            try {
                comp.init();
            } catch (Exception e) {
                
            }
        }
        
        runEnumerator.reset();
        while(runEnumerator.hasNext() && doRun) {
            Component comp = runEnumerator.next();
            //comp.updateRun();
            try {
                comp.run();
            } catch (Exception e) {
                
            }
        }
        
        runEnumerator.reset();
        while(runEnumerator.hasNext() && doRun) {
            Component comp = runEnumerator.next();
            try {
                comp.cleanup();
            } catch (Exception e) {
                
            }
        }
    }
    
    private boolean hasNext() {
        return currentCount < modelRuns.getValue();
    }
}
