/*
 * TaskExecutor.java
 * Created on 4. April 2008, 09:02
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

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 *
 * @author Sven Kralisch
 */
public class TaskExecutor {

    private ThreadPoolExecutor tpe;

    private Runnable[] tasks;

    private TaskGroup taskGroup;

    public TaskExecutor(int maxConcurrentThreads, Runnable[] tasks) {
        this.tasks = tasks;
        setMaxConcurrentThreads(maxConcurrentThreads);
    }

    public void setMaxConcurrentThreads(int n) {
        n = Math.max(1, n);
        tpe = new ThreadPoolExecutor(n, n, Long.MAX_VALUE,
                TimeUnit.SECONDS, new LinkedBlockingQueue<Runnable>());
        taskGroup = new TaskGroup(tasks, tpe);
    }

    public void start() {
        taskGroup.run();
    }

    public void shutdown() {
        tpe.shutdown();
    }
}
