/*
 * AbstractTracer.java
 * Created on 28. August 2008, 13:40
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
package jams.io.datatracer;

import jams.workspace.stores.OutputDataStore;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.regex.Pattern;
import jams.model.Context;
import jams.JAMS;
import jams.dataaccess.DataAccessor;
import jams.workspace.stores.Filter;

/**
 *
 * @author Sven Kralisch <sven.kralisch at uni-jena.de>
 */
public abstract class AbstractTracer implements DataTracer {

    protected DataAccessor[] accessorObjects;
    private ArrayList<String> attributeNames;
    protected Context context;
    private Context[] parents;
    protected OutputDataStore store;
    private Class idClazz;
    private boolean output;

    /**
     * DataTracer constructor
     * @param context The context that the attributes belong to
     * @param store The store that should be used by the DataTracer 
     * @param idClazz The type of the ID attribute, needed for type output
     */
    public AbstractTracer(Context context, OutputDataStore store, Class idClazz) {
        this.context = context;
        this.store = store;
        this.idClazz = idClazz;

        // Initialize the DataTracer, i.e. get the data objects to be traced from
        // the provided dataObjectHash, open the store and output some metadata 
        // to the store. Nothing will be written to the store as no attribute
        // names are provided or none of them are found in the dataObjectHash.
        init();
    }

    @Override
    public void updateDataAccessors(){
        HashMap<String, DataAccessor> dataObjectHash = context.getDataAccessorMap();
        ArrayList<DataAccessor> accessorObjectList = new ArrayList<DataAccessor>();
        this.attributeNames = new ArrayList<String>();

        for (String attributeName : store.getAttributes()) {
            DataAccessor dataAccessor = dataObjectHash.get(attributeName);
            if (dataAccessor != null) {
                accessorObjectList.add(dataAccessor);
                attributeNames.add(attributeName);
            }
        }
        this.accessorObjects = accessorObjectList.toArray(new DataAccessor[accessorObjectList.size()]);

        for (Filter filter : store.getFilters()) {

            Context superContext = context;
            while (superContext != null) {
                if (superContext.getInstanceName().equals(filter.getContextName())) {
                    //filter.setPattern(Pattern.compile(filter.getExpression()));
                    filter.setContext(superContext);
                    break;
                }
                superContext = context.getContext();
            }
        }
    }
    
    private void init() {
        updateDataAccessors();
        //if (this.accessorObjects.length > 0) {
            createHeader();
        //}
    }

    private void createHeader() {

        try {
            store.open(false);
        } catch (IOException ioe) {
            context.getModel().getRuntime().sendErrorMsg("Datastore \"" + store.getID() + "\": " + JAMS.i18n("Error_creating_data_output_directory!"));
            context.getModel().getRuntime().handle(ioe, true);
            return;
        }

        Context parent = context;
        ArrayList<Context> parentList = new ArrayList<Context>();
        while (parent != context.getModel()) {
            parent = parent.getContext();

            // only ancestors with more than one iteration are considered
            // in order to consider also contexts whose number of executions are
            // still unknown (e.g. parameter sampler), we check for !=1
            if (parent.getNumberOfIterations() != 1) {
                parentList.add(parent);
            }
        }
        this.parents = parentList.toArray(new Context[parentList.size()]);

        write("@context\n");
        write(this.context.getClass().getName() + "\t" + this.context.getInstanceName() + "\t" + context.getNumberOfIterations() + "\n");

        write("@ancestors\n");
        for (Context p : this.parents) {
            write(p.getClass().getName() + "\t" + p.getInstanceName() + "\t" + p.getNumberOfIterations() + "\n");
        }

        write("@filters\n");
        for (Filter filter : store.getFilters()) {
            write(filter.getContextName() + "\t" + filter.getExpression() + "\n");
        }

        write("@attributes\n");
        write("ID\t");
        for (String attributeName : this.attributeNames) {
            write(attributeName + "\t");
        }

        write("\n@types\n");
        write(idClazz.getSimpleName() + "\t");
        for (DataAccessor accessorObject : this.accessorObjects) {
            write(accessorObject.getComponentObject().getClass().getSimpleName() + "\t");
        }

        write("\n@data\n");
    }

    private void write(Object o){
        try {
            store.write(o);
        } catch (IOException ioe) {
            context.getModel().getRuntime().handle(ioe, true);
        }
    }
    
    protected void output(Object o) {
        try {
            store.writeCell(o);
        } catch (IOException ioe) {
            context.getModel().getRuntime().handle(ioe, true);
        }
    }
    
    protected void nextRow() {
        try {
            store.nextRow();
        } catch (IOException ioe) {
            context.getModel().getRuntime().handle(ioe, true);
        }
    }
    
    protected void flush() {
        try {
            store.flush();
        } catch (IOException ioe) {
            context.getModel().getRuntime().handle(ioe, true);
        }
    }    

    /**
     * 
     * @return The data objects that are traced by this DataTracer.
     */
    @Override
    public DataAccessor[] getAccessorObjects() {
        return accessorObjects;
    }

    /**
     * This method contains code to be executed as traced JAMSData objects change
     */
    @Override
    public abstract void trace();

    /**
     * Output some mark at the beginning of the contexts output within it's
     * run() method. If this context has parent contexts with more than
     * one iteration, some status information of those parent contexts are
     * provided here as well (Context::getTraceMark()).
     */
    @Override
    public void startMark() {
        for (Context parent : parents) {
            write(parent.getInstanceName() + "\t" + parent.getTraceMark() + "\n");
        }
        write("@start\n");
    }

    /**
     * Output some mark at the end of the contexts output within it's run()
     * method.
     */
    @Override
    public void endMark() {
        write("@end\n");
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

    /**
     * @return the output
     */
    public boolean hasOutput() {
        return output;
    }

    /**
     * @param output the output to set
     */
    public void setOutput(boolean output) {
        this.output = output;
    }
}