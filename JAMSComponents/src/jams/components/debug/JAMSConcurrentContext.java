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

import org.unijena.jams.data.JAMSEntity;
import org.unijena.jams.data.JAMSEntityCollection;
import org.unijena.jams.data.JAMSEntityEnumerator;
import org.unijena.jams.data.JAMSInteger;
import org.unijena.jams.model.JAMSComponent;
import org.unijena.jams.model.JAMSComponentEnumerator;
import org.unijena.jams.model.JAMSContext;
import org.unijena.jams.model.JAMSVarDescription;
import org.unijena.jams.runtime.concurrent.RunnableComponent;
import org.unijena.jams.runtime.concurrent.TaskExecutor;

/**
 *
 * @author Sven Kralisch
 */
public class JAMSConcurrentContext extends JAMSContext {
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            update = JAMSVarDescription.UpdateType.RUN,
            description = "List of spatial entities"
            )
            public JAMSEntityCollection entities;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            update = JAMSVarDescription.UpdateType.RUN,
            description = "Current spatial entity"
            )
            public JAMSEntity currentEntity;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            update = JAMSVarDescription.UpdateType.INIT,
            description = "Max. number of concurrent threads"
            )
            public JAMSInteger maxThreads;    
    
    
    public JAMSEntityCollection getEntities() {
        return entities;
    }
    
    public void setEntities(JAMSEntityCollection entities) {
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
        JAMSComponent[] compArray = getCompArray();
        tasks = new RunnableComponent[compArray.length];
        for (int i = 0; i < compArray.length; i++) {
            tasks[i] = new RunnableComponent(compArray[i]);
        }
        executor = new TaskExecutor(maxThreads.getValue());
    }

    public JAMSComponentEnumerator getRunEnumerator() {
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

    class RunEnumerator implements JAMSComponentEnumerator {

        JAMSEntityEnumerator ee = getEntities().getEntityEnumerator();
        int index = 0;

        public boolean hasNext() {
            return ee.hasNext();
        }

        public JAMSComponent next() {
            // check end of component elements list, if required switch to the next
            // entity and start with the new Component list again
            if (ee.hasNext()) {
                updateEntityData();
                setCurrentEntity(ee.next());
                index++;
                updateDataAccessors(index);
            }
            return null;
        }

        public void reset() {
            ee.reset();
            setCurrentEntity(getEntities().getCurrent());
            index = 0;
            updateDataAccessors(index);
        }
    }
}
