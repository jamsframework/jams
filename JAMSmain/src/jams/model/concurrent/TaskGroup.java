/*
 * TaskGroup.java
 * Created on 14. April 2008, 08:35
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
package jams.model.concurrent;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadPoolExecutor;

/**
 *
 * @author Sven Kralisch
 */
public class TaskGroup {

    private Runnable[] tasks;
    private Future[] futures;
    private ThreadPoolExecutor tpe;

    public TaskGroup(Runnable[] tasks, ThreadPoolExecutor tpe) {
        this.tasks = tasks;
        this.futures = new Future[tasks.length];
        this.tpe = tpe;
    }

    
    public void run() {
        try {
            for (int i = 0; i < tasks.length; i++) {
                //submit task and store future object
                futures[i] = tpe.submit(tasks[i]);
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
    }
}
