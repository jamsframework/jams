/*
 * TimeIntervalFromDataStore.java
 * Created on 22.12.2021, 23:40:48
 *
 * This file is part of JAMS
 * Copyright (C) FSU Jena
 *
 * JAMS is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public License
 * as published by the Free Software Foundation; either version 3
 * of the License, or (at your option) any later version.
 *
 * JAMS is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with JAMS. If not, see <http://www.gnu.org/licenses/>.
 *
 */

package jams.components.io;

import jams.data.*;
import jams.model.*;
import jams.workspace.stores.InputDataStore;
import jams.workspace.stores.TSDataStore;

/**
 *
 * @author Sven Kralisch <sven at kralisch.com>
 */
@JAMSComponentDescription(
    title="TimeIntervalFromDataStore",
    author="Sven Kralisch",
    description="Extract a time interval object from a data store",
    date = "2021-12-22",
    version = "1.0_0"
)
@VersionComments(entries = {
    @VersionComments.Entry(version = "1.0_0", comment = "Initial version")
})
public class TimeIntervalFromDataStore extends JAMSComponent {

    /*
     *  Component attributes
     */
    
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
            description = "Datastore ID")
    public Attribute.String id;

    @JAMSVarDescription(access = JAMSVarDescription.AccessType.WRITE,
            description = "The time interval covered by the datastore")
    public Attribute.TimeInterval timeInterval;    
    /*
     *  Component run stages
     */
    
    @Override
    public void init() {
        
        InputDataStore is = null;
        
        if (id != null) {
            is = getModel().getWorkspace().getInputDataStore(id.getValue());
        }

        // check if store exists
        if (is == null) {
            getModel().getRuntime().sendHalt("Error accessing datastore \""
                    + id + "\" from " + getInstanceName() + ": Datastore could not be found!");
            return;
        }

        // check if this is a TSDataStore
        if (!(is instanceof TSDataStore)) {
            getModel().getRuntime().sendHalt("Error accessing datastore \""
                    + id + "\" from " + getInstanceName() + ": Datastore is not a time series datastore!");
            return;
        }

        TSDataStore store = (TSDataStore) is;

        
        timeInterval.setStart(store.getStartDate());
        timeInterval.setEnd(store.getEndDate());
        timeInterval.setTimeUnit(store.getTimeUnit());
        timeInterval.setTimeUnitCount(store.getTimeUnitCount());
        
    }

}