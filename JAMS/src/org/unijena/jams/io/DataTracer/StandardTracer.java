/*
 * StandardTracer.java
 * Created on 28. August 2008, 13:40
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
package org.unijena.jams.io.DataTracer;

import jams.virtualws.stores.OutputDataStore;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import org.unijena.jams.data.JAMSEntity;
import org.unijena.jams.dataaccess.DataAccessor;
import org.unijena.jams.model.JAMSContext;

/**
 *
 * @author Sven Kralisch <sven.kralisch at uni-jena.de>
 */
public class StandardTracer implements DataTracer {

    private DataAccessor[] accessorObjects;
    private ArrayList<String> attributeNames = new ArrayList<String>();
    protected JAMSContext context;
    private JAMSContext[] parents;
    protected OutputDataStore store;
    private Class idClazz;

    /**
     * DataTracer constructor
     * @param context The context that the attributes belong to
     * @param store The belonging datastore object which provides the attribute 
     *              names and output functionality
     * @param idClazz The type of the ID field, needed for type output
     */
    public StandardTracer(JAMSContext context, OutputDataStore store, Class idClazz) {
        this.context = context;
        this.store = store;
        this.idClazz = idClazz;
    }

    /**
     * Register the name of an attribute that should be traced later on.
     * @param attributeName The attribute's name
     */
    public void registerAttribute(String attributeName) {
        attributeNames.add(attributeName);
    }

    /**
     * Initialize the DataTracer, i.e. get the data objects to be traced from
     * the provided dataObjectHash, open the store and output some metadata 
     * to the store. Nothing will be written to the store as no attribute
     * names are provided or none of them are found in the dataObjectHash.
     * @param dataObjectHash A HashMap containing the data objects that can be
     * accessed by their name
     * @return A string array containing the attribute names that could not be 
     * found.
     */
    @Override
    public String[] init(HashMap<String, DataAccessor> dataObjectHash) {

        ArrayList<String> missingAttributes = new ArrayList<String>();
        ArrayList<DataAccessor> accessorList = new ArrayList<DataAccessor>();

        for (String attributeName : attributeNames) {
            DataAccessor dataAccessor = dataObjectHash.get(attributeName);
            if (dataAccessor != null) {
                accessorList.add(dataAccessor);
            } else {
                missingAttributes.add(attributeName);
            }
        }
        attributeNames.removeAll(missingAttributes);
        this.accessorObjects = accessorList.toArray(new DataAccessor[accessorList.size()]);

        if (this.accessorObjects.length > 0) {
            createHeader();
        }

        return missingAttributes.toArray(new String[missingAttributes.size()]);
    }

    /**
     * 
     * @return The data objects that are traced by this DataTracer.
     */
    public DataAccessor[] getAccessorObjects() {
        return accessorObjects;
    }

    private void createHeader() {

        try {
            store.open();
        } catch (IOException ioe) {
            context.getModel().getRuntime().sendErrorMsg("Error creating data output directory!");
            return;
        }

        JAMSContext parent = context;
        ArrayList<JAMSContext> parentList = new ArrayList<JAMSContext>();
        while (parent != context.getModel()) {
            parent = parent.getContext();

            // only ancestors with more than one iteration are considered
            // in order to consider also contexts whose number of executions are
            // still unknown (e.g. parameter sampler), we check for !=1
            if (parent.getNumberOfIterations() != 1) {
                parentList.add(parent);
            }
        }
        this.parents = parentList.toArray(new JAMSContext[parentList.size()]);


        output("@context\n");
        output(this.context.getClass().getName() + "\t" + this.context.getInstanceName() + "\t" + context.getNumberOfIterations() + "\n");

        output("@ancestors\n");
        for (JAMSContext p : this.parents) {
            output(p.getClass().getName() + "\t" + p.getInstanceName() + "\t" + p.getNumberOfIterations() + "\n");
        }

        output("@attributes\n");
        output("ID\t");
        for (String attributeName : this.attributeNames) {
            output(attributeName + "\t");
        }

        output("\n@types\n");
        output(idClazz.getSimpleName() + "\t");
        for (DataAccessor accessorObject : this.accessorObjects) {
            output(accessorObject.getComponentObject().getClass().getSimpleName() + "\t");
        }

        output("\n@data\n");
    }

    /**
     * This method contains code to be executed as traced JAMSData objects change
     */
    @Override
    public void trace() {
       
        DataAccessor[] dataAccessors = getAccessorObjects();
        JAMSEntity[] entities = context.getEntities().getEntityArray();
        for (int j = 0; j < entities.length; j++) {

            output(entities[j].getId());
            output("\t");

            for (int i = 0; i < dataAccessors.length; i++) {
                dataAccessors[i].setIndex(j);
                dataAccessors[i].read();
                output(dataAccessors[i].getComponentObject());
                output("\t");
            }
            output("\n");
        }
    }
    
    /**
     * Output some mark at the beginning of the contexts output within it's
     * run() method. If this context has parent contexts with more than
     * one iteration, some status information of those parent contexts are
     * provided here as well (JAMSContext::getTraceMark()).
     */
    @Override
    public void setStartMark() {

        for (JAMSContext parent : parents) {
            output(parent.getInstanceName() + "\t" + parent.getTraceMark() + "\n");
        }
        output("@start\n");
    }

    /**
     * Output some mark at the end of the contexts output within it's run()
     * method.
     */
    @Override
    public void setEndMark() {
        output("@end\n");
    }

    protected void output(Object o) {
        try {
            store.write(o);
        } catch (IOException ioe) {
        }
    }

    /**
     * Closes the store belonging to this DataTracer, i.e. calls the store's
     * close() method.
     */
    @Override
    public void close() {
        try {
            store.close();
        } catch (IOException ioe) {
        }
    }
}