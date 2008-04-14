/*
 * TaskExecutor.java
 * Created on 4. April 2008, 09:02
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
package org.unijena.jams.runtime.concurrent;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 *
 * @author Sven Kralisch
 */
public class TaskExecutor {

    public static int MAX_CONCURRENT_THREADS = 1;

    public void start(Runnable[] tasks) {

        ThreadPoolExecutor tpe = new ThreadPoolExecutor(MAX_CONCURRENT_THREADS, MAX_CONCURRENT_THREADS,
                Long.MAX_VALUE, TimeUnit.SECONDS, new LinkedBlockingQueue<Runnable>());

        TaskGroup taskGroup = new TaskGroup(tasks, tpe);
        
        taskGroup.run();

        tpe.shutdown();

    }

    public static void main(String[] args) {
        TaskExecutor.MAX_CONCURRENT_THREADS = 1;

        int max = 10000;
        Runnable[] tasks = new RunnableComponent[max];
        for (int i = 0; i < max; i++) {
            tasks[i] = new RunnableComponent(new Task(i));
        }

        TaskExecutor executor = new TaskExecutor();
        executor.start(tasks);
    }
}
