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
import jams.model.JAMSComponentDescription;
import jams.model.JAMSContext;
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

    private ExecutorService executor;
    private Runnable[] tasks;
    private Future[] futures;

    /*
     * Component run stages
     */
    @Override
    public void run() {

        if (executor == null) {
            if (runEnumerator == null) {
                runEnumerator = getRunEnumerator();
            }

            ArrayList<Runnable> runnableList = new ArrayList<>();

            runEnumerator.reset();
            while (runEnumerator.hasNext() && doRun) {
                Component comp = runEnumerator.next();
                runnableList.add(new RunnableComponent(comp));
            }
            tasks = runnableList.toArray(new Runnable[runnableList.size()]);
            futures = new Future[tasks.length];
            executor = Executors.newFixedThreadPool(tasks.length);
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

        } catch (ExecutionException | InterruptedException ee) {
            this.getModel().getRuntime().handle(ee, this.getInstanceName());
        }

        updateEntityData();

    }
}
