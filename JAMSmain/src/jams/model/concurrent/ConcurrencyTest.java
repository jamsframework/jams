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
package jams.model.concurrent;

import jams.model.JAMSComponent;

/**
 *
 * @author Sven Kralisch
 */
public class ConcurrencyTest {

    public static void main(String[] args) {

        int max = 1000;
        Runnable[] tasks = new RunnableComponent[max];
        for (int i = 0; i < max; i++) {
            tasks[i] = new RunnableComponent(new TestComponent(i));
        }

        TaskExecutor executor = new TaskExecutor(2, tasks);
        long t = System.currentTimeMillis();
        executor.start();
        System.out.println("\nRuntime: " + (System.currentTimeMillis() - t));
        executor.shutdown();
    }

    static class TestComponent extends JAMSComponent {

        int id;

        public TestComponent(int id) {
            this.id = id;
        }

        public void run() {
            double d = Math.random() * 1000000;
            while (d > 0) {
                d--;
            }
            System.out.print(this.id + " ");
        }
    }
}
