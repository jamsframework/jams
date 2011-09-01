/*
 * ConcurrentContext.java
 * Created on 12. Januar 2010, 22:55
 *
 * This file is a JAMS component
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
package jams.model.concurrent;

import jams.data.Attribute;
import jams.model.Component;
import jams.model.JAMSComponentDescription;
import jams.model.JAMSContext;
import jams.model.JAMSVarDescription;
import java.util.ArrayList;
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

    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
    description = "List of spatial entities",
    defaultValue = "1")
    public Attribute.Integer numberOfThreads;
    private ExecutorService executor;
    private Runnable[] tasks;
    private ArrayList<Runnable> runnableList;
    private Future[] futures;
//    private ArrayList<Callable<Object>> runnableList;

    public static void main(String[] args) {
        float f = 100/3;
        int number = Float.floatToRawIntBits(f);        
        System.out.println(number + " - " + Float.intBitsToFloat(number));
    }
    
    /*
     *  Component run stages
     */
    @Override
    public void run() {

        if (numberOfThreads.getValue() == 0) {
            super.run();
            return;
        }

        if (executor == null) {
            if (runEnumerator == null) {
                runEnumerator = getRunEnumerator();
            }

            runnableList = new ArrayList<Runnable>();
//            runnableList = new ArrayList<Callable<Object>>();

            runEnumerator.reset();
            while (runEnumerator.hasNext() && doRun) {
                Component comp = runEnumerator.next();
//                runnableList.add(Executors.callable(new RunnableComponent(comp)));
                runnableList.add(new RunnableComponent(comp));
            }
            tasks = runnableList.toArray(new Runnable[runnableList.size()]);
            futures = new Future[tasks.length];
            executor = Executors.newFixedThreadPool(numberOfThreads.getValue());
        }

        try {

            for (int i = 0; i < tasks.length; i++) {
                //submit task and store future object
                futures[i] = executor.submit(tasks[i]);
            }
            for (int i = 0; i < tasks.length; i++) {
                //call future's get method (blocking) in order to wait for task to continue
                futures[i].get();
            }
        } catch (ExecutionException ee) {
            ee.printStackTrace();
        } catch (InterruptedException ie) {
            ie.printStackTrace();
        }

//        for (Runnable r : tasks) {
//            executor.submit(r);
//        }
//        try {
//            executor.invokeAll(runnableList);
//        } catch (InterruptedException ex) {
//            Logger.getLogger(ConcurrentContext.class.getName()).log(Level.SEVERE, null, ex);
//        }

        updateEntityData();

    }
}
