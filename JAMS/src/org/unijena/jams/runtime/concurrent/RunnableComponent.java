/*
 * RunnableComponent.java
 * Created on 14. April 2008, 08:37
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

import jams.model.JAMSComponent;

/**
 *
 * @author Sven Kralisch
 */
public class RunnableComponent implements Runnable {

    private JAMSComponent component;

    public RunnableComponent(JAMSComponent component) {
        this.component = component;
    }

    public void run() {
        try {
            component.run();
        } catch (Exception e) {
            component.getModel().getRuntime().handle(e, component.getInstanceName());
        }
    }
}
