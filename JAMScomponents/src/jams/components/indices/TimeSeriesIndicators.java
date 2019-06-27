/*
 * TimeSeriesIndicators.java
 * Created on 16.05.2019, 13:36:43
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
package jams.components.indices;

import jams.JAMS;
import jams.data.*;
import jams.model.*;
import jams.workspace.DataValue;
import jams.workspace.DefaultDataSet;
import jams.workspace.stores.InputDataStore;
import jams.workspace.stores.TSDataStore;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Sven Kralisch <sven.kralisch@uni-jena.de>
 */
public class TimeSeriesIndicators extends JAMSComponent {

    /*
     *  Component attributes
     */
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
            description = "Datastore ID")
    public Attribute.String id;

    protected List<String> dateStrings;
    protected List<Attribute.Calendar> dates;
    protected List<Double>[] values;
    protected Attribute.Calendar lastPlusOne;

    protected void readTSData() {

        InputDataStore is = getModel().getWorkspace().getInputDataStore(id.getValue());
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
        Attribute.Calendar storeDate = store.getStartDate().clone();
        int storeUnit = store.getTimeUnit();
        int storeUnitCount = store.getTimeUnitCount();

        values = new List[store.getDataSetDefinition().getColumnCount()];
        for (int i = 0; i < values.length; i++) {
            values[i] = new ArrayList();
        }

        dateStrings = new ArrayList();
        dates = new ArrayList();

        DefaultDataSet ds;
        while ((ds = store.getNext()) != null) {
            dates.add(storeDate.clone());
            dateStrings.add(storeDate.toString());
            DataValue[] data = ds.getData();
            for (int i = 1; i < data.length; i++) {
                String s = data[i].getString();
                double d;
                if (s.equals("NaN") || s.equals("Infinity") || s.isEmpty()) {
                    d = JAMS.getMissingDataValue();
                } else {
                    d = data[i].getDouble();
                }
                values[i - 1].add(d);
            }
            storeDate.add(storeUnit, storeUnitCount);
        }

        lastPlusOne = storeDate;

    }

}
