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

import jams.io.DataTracer.DataTracer;
import jams.model.Component;
import jams.model.JAMSComponentDescription;
import jams.model.JAMSContext;
import jams.model.concurrent.TaskExecutor;
import java.util.ArrayList;

/**
 *
 * @author Sven Kralisch <sven.kralisch at uni-jena.de>
 */
@JAMSComponentDescription (title = "ConcurrentContext",
                           author = "Sven Kralisch",
                           description = "A context that executes its child components concurrently")
public class ConcurrentContext extends JAMSContext {

    private TaskExecutor executor;

    /*
     *  Component run stages
     */
    @Override
    public void run() {

        if (executor == null) {
            if (runEnumerator == null) {
                runEnumerator = getRunEnumerator();
            }

            ArrayList<Runnable> runnableList = new ArrayList<Runnable>();
            runEnumerator.reset();
            while (runEnumerator.hasNext() && doRun) {
                Component comp = runEnumerator.next();
                runnableList.add(new RunnableComponent(comp));
            }
            Runnable[] tasks = runnableList.toArray(new Runnable[runnableList.size()]);
            executor = new TaskExecutor(1, tasks);
        }

//        for (DataTracer dataTracer : dataTracers) {
//            dataTracer.startMark();
//        }

        executor.start();
        
        updateEntityData();

        for (DataTracer dataTracer : dataTracers) {
            dataTracer.trace();
            dataTracer.endMark();
        }
    }
}
