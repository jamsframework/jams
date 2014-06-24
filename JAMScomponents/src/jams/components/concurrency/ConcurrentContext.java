/*
 * ConcurrentContext.java
 * Created on 12. Januar 2010, 22:55
 *
 * This file is a JAMS component
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
package jams.components.concurrency;

import jams.model.Component;
import jams.model.ComponentEnumerator;
import jams.model.Context;
import jams.model.JAMSComponentDescription;
import jams.model.JAMSContext;
import jams.model.Model;
import jams.runtime.JAMSRuntime;
import java.util.ArrayList;
import java.util.List;
import java.util.Observable;
import java.util.Observer;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 *
 * @author Sven Kralisch <sven.kralisch at uni-jena.de>
 */
@JAMSComponentDescription(title = "ConcurrentContext",
author = "Sven Kralisch",
description = "A context that executes its child components concurrently")
public class ConcurrentContext extends JAMSContext {

    transient private ExecutorService executor;
    transient List<Callable<Component>> callables;

    @Override
    public void init() {
        super.init();
        executor = null;
    }
    /*
     * Component run stages
     */
    
    @Override
    public void run() {

        if (executor == null) {
            if (runEnumerator == null) {
                runEnumerator = getRunEnumerator();
            }

            runEnumerator.reset();
            callables = new ArrayList();
            while (runEnumerator.hasNext()) {
                Component comp = runEnumerator.next();
                callables.add(new CallableComponent(comp));
            }
            executor = Executors.newFixedThreadPool(callables.size());
        }

        try {

            List<Future<Component>> futures = executor.invokeAll(callables);
            
            for (Future f : futures) {
                f.get();
            }
            
        } catch (ExecutionException ee) {
            this.getModel().getRuntime().handle(ee, this.getInstanceName());
        } catch (InterruptedException ie) {
            this.getModel().getRuntime().handle(ie, this.getInstanceName());
        }
                
        updateEntityData();
        
        System.out.println("Finish ConcContext ... ");

    }
    
    @Override
    public void cleanup() {
        if (executor != null) {
            executor.shutdown();
        }
    }
        
    @Override
    public void setExecutionState(int state){
        
        switch (state){
            case JAMSRuntime.STATE_RUN: doRun = true; break;
            case JAMSRuntime.STATE_STOP: doRun = false; break;
            case JAMSRuntime.STATE_PAUSE: doRun = false; break;
        }
     
        //do not propagate this information!!
        /*ComponentEnumerator ce = this.getChildrenEnumerator();
        while (ce.hasNext()){
            Component comp = ce.next();
            if (comp instanceof Context){
                ((Context)comp).setExecutionState(state);
            }
        }*/
    }
}
