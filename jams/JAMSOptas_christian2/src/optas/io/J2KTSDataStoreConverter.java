/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package optas.io;

import jams.data.Attribute;
import jams.data.DefaultDataFactory;
import jams.workspace.stores.J2KTSDataStore;
import java.util.ArrayList;
import optas.core.OPTASException;
import optas.data.time.api.TimeSerie;
import optas.data.view.ViewFactory;
import optas.data.time.DefaultTimeSerie;

/**
 *
 * @author Christian Fischer <christian.fischer.2@uni-jena.de>
 */
public class J2KTSDataStoreConverter {
    
    public static TimeSerie createFromJ2KTSDataStore(J2KTSDataStore store, int index, String name) {
        Attribute.TimeInterval range = DefaultDataFactory.getDataFactory().createTimeInterval();
        range.setStart(store.getStartDate());
        range.setEnd(store.getEndDate());
        range.setTimeUnit(store.getTimeUnit());
        range.setTimeUnitCount(store.getTimeUnitCount());

        long count = range.getNumberOfTimesteps();
        double value[] = new double[(int) count];
        int i = 0;
        while (store.hasNext()) {
            value[i++] = store.getNext().getData()[index].getDouble();
        }

        return new DefaultTimeSerie(name, range, ViewFactory.createView(value));
    }

    public static TimeSerie createFromJ2KTSDataStore(J2KTSDataStore store, String name) {
        ArrayList<String> list = store.getDataSetDefinition().getAttributeNames();
        int index = list.indexOf(name);
        if (index == -1) {
            throw new OPTASException("Attribute " + name + " was not found in datastore " + store.getDisplayName());
        }

        return createFromJ2KTSDataStore(store, index, name);
    } 
}
