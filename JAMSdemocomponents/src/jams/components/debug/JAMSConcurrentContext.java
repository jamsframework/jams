/*
 * JAMSConcurrentContext.java
 * Created on 18. April 2008, 21:12
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
package jams.components.debug;

import jams.data.Attribute;
import jams.data.JAMSEntity;
import jams.data.JAMSEntityCollection;
import jams.data.EntityEnumerator;
import jams.data.JAMSInteger;
import jams.model.Component;
import jams.model.JAMSComponent;
import jams.model.ComponentEnumerator;
import jams.model.JAMSContext;
import jams.model.JAMSVarDescription;
import jams.runtime.concurrent.RunnableComponent;
import jams.runtime.concurrent.TaskExecutor;

/**
 *
 * @author Sven Kralisch
 */
public class JAMSConcurrentContext extends JAMSContext {

    @JAMSVarDescription (access = JAMSVarDescription.AccessType.READ,
                         update = JAMSVarDescription.UpdateType.RUN,
                         description = "List of spatial entities")
    public Attribute.EntityCollection entities;

    @JAMSVarDescription (access = JAMSVarDescription.AccessType.WRITE,
                         update = JAMSVarDescription.UpdateType.RUN,
                         description = "Current spatial entity")
    public JAMSEntity currentEntity;

    @JAMSVarDescription (access = JAMSVarDescription.AccessType.READ,
                         update = JAMSVarDescription.UpdateType.INIT,
                         description = "Max. number of concurrent threads")
    public JAMSInteger maxThreads;

    @Override
    public Attribute.EntityCollection getEntities() {
        return entities;
    }

    @Override
    public void setEntities(Attribute.EntityCollection entities) {
        this.entities = entities;
    }

    public JAMSEntity getCurrentEntity() {
        return currentEntity;
    }

    public void setCurrentEntity(JAMSEntity currentEntity) {
        this.currentEntity = currentEntity;
    }
    private Runnable[] tasks;

    private TaskExecutor executor;

    @Override
    public void init() {
        super.init();
        Component[] compArray = getCompArray();
        tasks = new RunnableComponent[compArray.length];
        for (int i = 0; i < compArray.length; i++) {
            tasks[i] = new RunnableComponent(compArray[i]);
        }
        executor = new TaskExecutor(maxThreads.getValue());
    }

    @Override
    public ComponentEnumerator getRunEnumerator() {
        return new RunEnumerator();
    }

    @Override
    public void run() {

        if (runEnumerator == null) {
            runEnumerator = getRunEnumerator();
        }

        executor.start(tasks);

        runEnumerator.reset();
        while (runEnumerator.hasNext() && doRun) {
            runEnumerator.next();
        }

        updateEntityData();
    }

    @Override
    public void cleanup() {
        executor.shutdown();
        super.cleanup();
    }

    class RunEnumerator implements ComponentEnumerator {

        EntityEnumerator ee = getEntities().getEntityEnumerator();

        int index = 0;

        @Override
        public boolean hasNext() {
            return ee.hasNext();
        }

        @Override
        public JAMSComponent next() {
            // check end of component elements list, if required switch to the next
            // entity and start with the new Component list again
            if (ee.hasNext()) {
                updateEntityData();
//                setCurrentEntity(ee.next());
                index++;
                updateComponentData(index);
            }
            return null;
        }

        @Override
        public void reset() {
            ee.reset();
//            setCurrentEntity(getEntities().getCurrent());
            index = 0;
            updateComponentData(index);
        }

        // TODO
        @Override
        public void setState(byte[]state) {

        }

        // TODO
        @Override
        public byte[] getState() {
            return null;
        }               
    }
}
