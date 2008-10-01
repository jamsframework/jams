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
package jams.runtime.concurrent;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 *
 * @author Sven Kralisch
 */
public class TaskExecutor {

    private ThreadPoolExecutor tpe;

    public TaskExecutor() {
        this(1);
    }

    public TaskExecutor(int maxConcurrentThreads) {
        setMaxConcurrentThreads(maxConcurrentThreads);        
    }

    public void setMaxConcurrentThreads(int n) {
        tpe = new ThreadPoolExecutor(n, n, Long.MAX_VALUE,
                TimeUnit.SECONDS, new LinkedBlockingQueue<Runnable>());
    }

    public void start(Runnable[] tasks) {
        TaskGroup taskGroup = new TaskGroup(tasks, tpe);
        taskGroup.run();
    }

    public void shutdown() {
        tpe.shutdown();
    }

    public static void main(String[] args) {

        int max = 10000;
        Runnable[] tasks = new RunnableComponent[max];
        for (int i = 0; i < max; i++) {
            tasks[i] = new RunnableComponent(new Task(i));
        }

        TaskExecutor executor = new TaskExecutor(2);
        executor.start(tasks);
        executor.shutdown();
    }
}
