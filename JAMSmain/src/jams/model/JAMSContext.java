/*
 * JAMSContext.java
 *
 * Created on 22. Juni 2005, 21:03
 *
 * This file is part of JAMS
 * Copyright (C) 2005 FSU Jena
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
package jams.model;

/**
 *
 * @author S. Kralisch
 */
import jams.io.datatracer.DataTracer;
import jams.workspace.stores.OutputDataStore;
import java.io.Serializable;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.util.*;
import jams.JAMS;
import jams.tools.JAMSTools;
import jams.data.*;
import jams.dataaccess.*;
import jams.dataaccess.CalendarAccessor;
import jams.io.datatracer.AbstractTracer;
import jams.runtime.JAMSRuntime;
import jams.workspace.stores.Filter;
import java.io.IOException;
import java.io.ObjectOutputStream;

@JAMSComponentDescription(title = "JAMS Context",
        author = "Sven Kralisch",
        date = "2005-06-22",
        version = "1.0_0",
        description = "This component represents a JAMS "
        + "context which is the top level component of every component"
        + " hierarchie in JAMS models")
public class JAMSContext extends JAMSComponent implements Context {

    private Attribute.EntityCollection entities;
    protected ArrayList<Component> components = new ArrayList();
    protected ComponentEnumerator initEnumerator = null, initAllEnumerator = null, runEnumerator = null, cleanupEnumerator = null, cleanupAllEnumerator = null;
    private ArrayList<AttributeAccess> attributeAccessList = new ArrayList();
    private ArrayList<AttributeSpec> attributeSpecs = new ArrayList();
    protected DataAccessor[] dataAccessors = new DataAccessor[0];
    private HashMap<String, DataAccessor> dataAccessorMap;
    private HashMap<String, JAMSData> attributeMap;
    protected DataTracer[] dataTracers;
    protected boolean doRun = true;
    protected boolean isPaused = false;
    transient protected Runnable runRunnable, resumeRunnable, initAllRunnable, cleanupAllRunnable;
    transient protected Component currentComponent;

    /**
     * Creates a new context
     */
    public JAMSContext() {

        // create a list with one entitiy, this is the minimum number for any context
        ArrayList<Attribute.Entity> list = new ArrayList<Attribute.Entity>();
        list.add(DefaultDataFactory.getDataFactory().createEntity());
        setEntities(DefaultDataFactory.getDataFactory().createEntityCollection());
        getEntities().setEntities(list);

        attributeMap = new HashMap<String, JAMSData>();
    }

    /**
     * Change the positions of two components
     *
     * @param i The position of the first component
     * @param j The position of the second component
     */
    public void exchange(int i, int j) {
        Component oi = components.get(i);
        Component oj = components.get(j);
        components.set(i, oj);
        components.set(j, oi);
    }

    /**
     * Add a single component to this context
     *
     * @param c The component to be added
     */
    public void addComponent(Component c) {
        components.add(c);
        c.setContext(this);
    }

    /**
     * Remove a single component from this context
     *
     * @param index The index of the component to be removed
     */
    public void removeComponent(int index) {
        components.remove(index);
    }

    /**
     *
     * @return All child components as ArrayList
     */
    public ArrayList<Component> getComponents() {
        return components;
    }

    /**
     *
     * @param components Set the child components of this context and set this
     * object as their context
     */
    public void setComponents(ArrayList<Component> components) {
        this.components = components;
        Iterator<Component> i = components.iterator();
        while (i.hasNext()) {
            i.next().setContext(this);
        }
    }

    /**
     *
     * @return An enumerator iterating once over all child components
     */
    protected ComponentEnumerator getChildrenEnumerator() {
        return new ComponentEnumerator() {
            Component[] compArray = getCompArray();
            int index = 0;

            @Override
            public boolean hasNext() {
                return (index < compArray.length);
            }

            public boolean hasPrevious() {
                return (index > 0);
            }

            @Override
            public Component next() {
                return compArray[index++];
            }

            @Override
            public Component previous() {
                return compArray[--index];
            }

            @Override
            public void reset() {
                index = 0;
            }
        };
    }

    /**
     *
     * @return An enumerator iterating over all child components depending on
     * this context's init stage functionality
     */
    protected ComponentEnumerator getInitEnumerator() {
        return getChildrenEnumerator();
    }

    /**
     *
     * @return An enumerator iterating over all child components depending on
     * this context's cleanup stage functionality
     */
    protected ComponentEnumerator getCleanupEnumerator() {
        return getChildrenEnumerator();
    }

    /**
     *
     * @return An enumerator iterating over all child components depending on
     * this context's run stage functionality
     */
    protected ComponentEnumerator getRunEnumerator() {
        return new ComponentEnumerator() {
            ComponentEnumerator ce = getChildrenEnumerator();
            EntityEnumerator ee = getEntities().getEntityEnumerator();
            int index = 0;

            @Override
            public boolean hasNext() {
                boolean nextComp = ce.hasNext();
                boolean nextEntity = ee.hasNext();
                return (nextEntity || nextComp);
            }

            @Override
            public boolean hasPrevious() {
                boolean prevComp = ce.hasPrevious();
                boolean prevEntity = ee.hasPrevious();
                return (prevComp || prevEntity);
            }

            @Override
            public Component next() {
                // check end of component elements list, if required switch to the next
                // entity and start with the new Component list again
                if (!ce.hasNext() && ee.hasNext()) {
                    updateEntityData();
                    ee.next();

                    index++;
                    updateComponentData(index);
                    ce.reset();
                }
                return ce.next();
            }

            @Override
            public void reset() {
                ee.reset();
//            setCurrentEntity(getEntities().getCurrent());
                ce.reset();
                index = 0;
                updateComponentData(index);
            }

            @Override
            public Component previous() {
                if (ce.hasPrevious()) {
                    return ce.previous();
                } else if (ee.hasPrevious()) {
                    ee.previous();
                    index--;
                    updateComponentData(index);
                    while (ce.hasNext()) {
                        ce.next();
                    }
                    return ce.previous();
                }
                return null;
            }
        };
    }

    /**
     *
     * @return An enumerator iterating over all child components depending on
     * this context's initAll stage functionality
     */
    protected ComponentEnumerator getInitAllEnumerator() {
        return getRunEnumerator();
    }
    
    /**
     *
     * @return An enumerator iterating over all child components depending on
     * this context's initAll stage functionality
     */
    protected ComponentEnumerator getCleanupAllEnumerator() {
        return getRunEnumerator();
    }    

    /**
     *
     * @return All child components as array
     */
    protected Component[] getCompArray() {
        if (components.size() == 0) {
            //add a dummy component
            components.add(new JAMSComponent());
        }
        Component[] comps = new Component[components.size()];
        components.toArray(comps);
        return comps;
    }

    /**
     * Registers a new accessor managed by this context
     *
     * @param user The components that wants to have access
     * @param varName The name of the components member which is connected
     * @param attributeName The name of the attribute within this context
     * @param accessType The permission type (DataAccessor.READ_ACCESS,
     * DataAccessor.WRITE_ACCESS or DataAccessor.READWRITE_ACCESS)
     */
    @Override
    public void addAccess(Component user, String varName, String attributeName, int accessType) {
        attributeAccessList.add(new AttributeAccess(user, varName, attributeName, accessType));
    }

    /**
     * Registers a new attribute object for this context
     *
     * @param attributeName The name of the attribute
     * @param clazz The type of the attribute
     * @param value The value of the attribute
     */
    @Override
    public void addAttribute(String attributeName, String clazz, String value) {
        attributeSpecs.add(new AttributeSpec(attributeName, clazz, value));
    }

    /**
     * get registered attributes
     *
     * @return registered attributes
     */
//    public ArrayList<AttributeSpec> getAttributes() {
//        ArrayList<AttributeSpec> attributes = new ArrayList<AttributeSpec>();
//        for (int i = 0; i < this.attributeSpecs.size(); i++) {
//            AttributeSpec orginial = attributeSpecs.get(i);
//            attributes.add(new AttributeSpec(orginial.attributeName, orginial.className, orginial.value));
//        }
//        return attributes;
//    }
    /**
     * Sets the model for this context. Additionally an observer of the runtimes
     * runstate is created in order to stop iteration on runstate changes
     *
     * @param model The model object
     */
    @Override
    public void setModel(Model model) {
        super.setModel(model);
        JAMSRuntime rt = getModel().getRuntime();

        rt.addStateObserver(new Observer() {
            @Override
            public void update(Observable obs, Object obj) {
                if (getModel().getRuntime().getState() != JAMSRuntime.STATE_RUN) {
                    JAMSContext.this.doRun = false;
                } else {
                    JAMSContext.this.doRun = true;
                }
            }
        });
    }

    /**
     * Iniatialization of all objects that are needed to manage the data
     * exchange between descendent components. Needs to be called once at the
     * beginning of the init stage before calling the init() methods of child
     * components.
     */
    @Override
    public void initAccessors() {

        attributeMap = new HashMap<String, JAMSData>();
        dataAccessorMap = new HashMap<String, DataAccessor>();

        AttributeAccess accessSpec;
        AttributeSpec attributeSpec;
        JAMSData dataObject;
        Class clazz;

        Attribute.Entity[] entityArray = getEntities().getEntityArray();

        //handle all attribute declarations in this context
        Iterator<AttributeSpec> attributeIterator = attributeSpecs.iterator();
        while (attributeIterator.hasNext()) {
            attributeSpec = attributeIterator.next();

            try {

                clazz = Class.forName(attributeSpec.className);
                JAMSData data = getDataObject(entityArray, clazz, attributeSpec.attributeName, DataAccessor.WRITE_ACCESS, null);
                data.setValue(attributeSpec.value);

                //add attributes to "handle map"
                /*
                 String id = this.getInstanceName() + "." + attributeSpec.attributeName;
                 getModel().getRuntime().getDataHandles().put(id, data);
                 */
                for (Attribute.Entity entity : entityArray) {
                    entity.setObject(attributeSpec.attributeName, data);
                }

            } catch (ClassNotFoundException cnfe) {
                getModel().getRuntime().handle(cnfe, false);
            } catch (InstantiationException ie) {
                getModel().getRuntime().handle(ie, false);
            } catch (IllegalAccessException iae) {
                getModel().getRuntime().handle(iae, false);
            } catch (Exception e) {
                getModel().getRuntime().handle(e, false);
            }
        }

        Iterator<AttributeAccess> accessIterator = attributeAccessList.iterator();
        while (accessIterator.hasNext()) {
            accessSpec = accessIterator.next();

            try {

                clazz = JAMSTools.getField(accessSpec.getComponent().getClass(), accessSpec.getVarName()).getType();

                if (clazz.isArray()) {

                    StringTokenizer tok = new StringTokenizer(accessSpec.getAttributeName(), ";");
                    int count = tok.countTokens();

                    Class componentClass = clazz.getComponentType();
                    JAMSData[] array = (JAMSData[]) Array.newInstance(componentClass, count);

                    for (int i = 0; i < count; i++) {
                        array[i] = getDataObject(entityArray, componentClass, tok.nextToken(), accessSpec.getAccessType(), null);
                    }

                    Field field = JAMSTools.getField(accessSpec.getComponent().getClass(), accessSpec.getVarName());

                    // set the component's field value to dataObject
                    JAMSTools.setField(accessSpec.getComponent(), field, array);
                    //field.set(accessSpec.component, array);

                    // field has been set with some value, so
                    // remove it from list of nullFields
                    if (getModel().getNullFields() != null) {
                        ArrayList<Field> nullFields = getModel().getNullFields().get(accessSpec.getComponent());
                        nullFields.remove(field);
                    }
                } else {

                    /* 
                     * maybe the component's data object already has a value 
                     * assigned, so get it, but
                     * problem in case of a repeated execution of the whole
                     * model w/o newly creating the components with their initial
                     * attribute values -- they would keep their old attribute
                     * value introducing a model memory :/ -- better use null instead..
                     */
                    JAMSData componentObject = null;//(JAMSData) accessSpec.component.getImplementingClass().getDeclaredField(accessSpec.varName).get(accessSpec.component);

                    //get the data object belonging to the attribute
                    dataObject = getDataObject(entityArray, clazz, accessSpec.getAttributeName(), accessSpec.getAccessType(), componentObject);

                    //assign the dataObject to the component
                    Field field = JAMSTools.getField(accessSpec.getComponent().getClass(), accessSpec.getVarName());

                    // set the component's field value to dataObject
                    JAMSTools.setField(accessSpec.getComponent(), field, dataObject);

                    // field has been set with some value, so
                    // remove it from list of nullFields
//                    if (getModel().getNullFields() != null) { // can be null after deserialization
//                        ArrayList<Field> nullFields = getModel().getNullFields().get(accessSpec.component);
//                        nullFields.remove(field);
//                    }
                }
            } catch (Exception e) {
                getModel().getRuntime().sendErrorMsg(JAMS.i18n("Error_occured_in_") + accessSpec.getComponent().getInstanceName() + ": " + accessSpec.getVarName());
                getModel().getRuntime().handle(e, false);
            }

            // create DataAccessor array from DataAccessor list
            if (dataAccessorMap.size() > 0) {
                this.dataAccessors = dataAccessorMap.values().toArray(new DataAccessor[dataAccessorMap.size()]);
            }
        }
    }

    /**
     *
     * @return A string representing the current state of the context
     */
    @Override
    public String getTraceMark() {
        return Long.toString(getEntities().getCurrent().getId());
    }

    /**
     *
     * @param store A data store defining what data should be stored and
     * providing a writer object
     * @return A data tracer object
     */
    protected DataTracer createDataTracer(OutputDataStore store) {

        // create a DataTracer which is suited for this context
        if (store.getFilters().length == 0) {
            return new AbstractTracer(this, store, JAMSLong.class) {
                @Override
                public void trace() {

                    DataAccessor[] dataAccessors = this.getAccessorObjects();//accessorObjects;
                    EntityEnumerator ee = getEntities().getEntityEnumerator();
                    ee.reset();
                    int j = 0;

                    whileLoop:
                    while (true) {

                        // process the entity with index j
                        process(dataAccessors, j);

                        if (ee.hasNext()) {
                            ee.next();
                            j++;
                        } else {
                            break;
                        }
                    }

                    if (hasOutput()) {
                        endMark();
                        setOutput(false);
                    }
                }

                private void process(DataAccessor[] dataAccessors, int j) {
                    String traceMark = getTraceMark();

                    // if we haven't output a mark so far, do it now
                    if (!hasOutput()) {
                        setOutput(true);
                        startMark();
                    }

                    output(traceMark);

                    for (int i = 0; i < dataAccessors.length; i++) {
                        dataAccessors[i].setIndex(j);
                        dataAccessors[i].read();
                        output(dataAccessors[i].getComponentObject());
                    }
                    nextRow();

                }
            };
        } else {
            return new AbstractTracer(this, store, JAMSLong.class) {
                @Override
                public void trace() {

                    // check for filters on other contexts first
                    for (Filter filter : store.getFilters()) {
                        if (filter.getContext() != JAMSContext.this) {
                            String s = filter.getContext().getTraceMark();
                            if (!filter.isFiltered(s)) {
                                return;
                            }
                        }
                    }

                    DataAccessor[] dataAccessors = this.getAccessorObjects();//accessorObjects;
                    EntityEnumerator ee = getEntities().getEntityEnumerator();
                    ee.reset();
                    int j = 0;

                    whileLoop:
                    while (true) {

                        // process the entity with index j
                        process(dataAccessors, j);

                        if (ee.hasNext()) {
                            ee.next();
                            j++;
                        } else {
                            break;
                        }
                    }

                    if (hasOutput()) {
                        endMark();
                        setOutput(false);
                    }
                }

                private void process(DataAccessor[] dataAccessors, int j) {
                    String traceMark = getTraceMark();

                    // take care of filters in this context
                    for (Filter filter : store.getFilters()) {
                        if (filter.getContext() == JAMSContext.this) {
                            if (!filter.isFiltered(traceMark)) {
                                return;
                            }
                        }
                    }

                    // if we haven't output a mark so far, do it now
                    if (!hasOutput()) {
                        setOutput(true);
                        startMark();
                    }

                    output(traceMark);

                    for (int i = 0; i < dataAccessors.length; i++) {
                        dataAccessors[i].setIndex(j);
                        dataAccessors[i].read();
                        output(dataAccessors[i].getComponentObject());
                    }
                    nextRow();

                }
            };
        }
    }

    /**
     * Set up the output data tracers defined for this context
     */
    @Override
    public void setupDataTracer() {

        // check if there are output stores for this context
        if (getModel().getWorkspace() == null) {

            // if there is no workspace, we can't use datastores
            this.dataTracers = new DataTracer[0];

        } else {

            // get the output stores if existing
            OutputDataStore[] stores = getModel().getWorkspace().getOutputDataStores(this.getInstanceName());

            this.dataTracers = new DataTracer[stores.length];
            int i = 0;
            for (OutputDataStore store : stores) {
                this.dataTracers[i] = createDataTracer(store);
                i++;
            }

        }

        //initTracerDataAccess();
        for (int j = 0; j < this.components.size(); j++) {
            Component comp = components.get(j);
            if (comp instanceof Context) {
                ((Context) comp).setupDataTracer();
            }
        }

    }

    private void createRunRunnable(boolean isProfiling) {
        // create the runnable object that is executed at each run stage of that context
        // this will differ depending on wether the child components' execution time should be
        // measured or not
        if (isProfiling) {

            runRunnable = new Runnable() {
                @Override
                public void run() {
                    long cStart = System.currentTimeMillis();
                    try {
                        currentComponent.run();
                    } catch (Exception e) {
                        getModel().getRuntime().handle(e, currentComponent.getInstanceName());
                    }
                    getModel().measureTime(cStart, currentComponent);
                }
            };

        } else {

            runRunnable = new Runnable() {
                @Override
                public void run() {
                    try {
                        currentComponent.run();
                    } catch (Exception e) {
                        getModel().getRuntime().handle(e, currentComponent.getInstanceName());
                    }
                }
            };

        }
    }

    private void createInitAllRunnable(boolean isProfiling) {
        // create the runnable object that is executed at each run stage of that context
        // this will differ depending on wether the child components' execution time should be
        // measured or not
        if (isProfiling) {

            initAllRunnable = new Runnable() {
                @Override
                public void run() {
                    long cStart = System.currentTimeMillis();
                    try {
                        currentComponent.initAll();
                    } catch (Exception e) {
                        getModel().getRuntime().handle(e, currentComponent.getInstanceName());
                    }
                    getModel().measureTime(cStart, currentComponent);
                }
            };

        } else {

            initAllRunnable = new Runnable() {
                @Override
                public void run() {
                    try {
                        currentComponent.initAll();
                    } catch (Exception e) {
                        getModel().getRuntime().handle(e, currentComponent.getInstanceName());
                    }
                }
            };

        }
    }

    private void createCleanupAllRunnable(boolean isProfiling) {
        // create the runnable object that is executed at each run stage of that context
        // this will differ depending on wether the child components' execution time should be
        // measured or not
        if (isProfiling) {

            cleanupAllRunnable = new Runnable() {
                @Override
                public void run() {
                    long cStart = System.currentTimeMillis();
                    try {
                        currentComponent.cleanupAll();
                    } catch (Exception e) {
                        getModel().getRuntime().handle(e, currentComponent.getInstanceName());
                    }
                    getModel().measureTime(cStart, currentComponent);
                }
            };

        } else {

            cleanupAllRunnable = new Runnable() {
                @Override
                public void run() {
                    try {
                        currentComponent.cleanupAll();
                    } catch (Exception e) {
                        getModel().getRuntime().handle(e, currentComponent.getInstanceName());
                    }
                }
            };

        }
    }    
    
    private void createResumeRunnable(boolean isProfiling) {
        // create the runnable object that is executed at each run stage of that context
        // this will differ depending on wether the child components' execution time should be
        // measured or not
        if (isProfiling) {
            resumeRunnable = new Runnable() {
                @Override
                public void run() {
                    long cStart = System.currentTimeMillis();
                    ((Context) currentComponent).resume();
                    getModel().measureTime(cStart, currentComponent);
                }
            };

        } else {

            resumeRunnable = new Runnable() {
                @Override
                public void run() {
                    ((Context) currentComponent).resume();
                }
            };

        }
    }

    protected void initTracerDataAccess() {
        // get the output stores if existing
        OutputDataStore[] stores = getModel().getWorkspace().getOutputDataStores(this.getInstanceName());

        // make sure there are accessors for all attributes        
        Attribute.Entity[] entityArray = getEntities().getEntityArray();
        for (OutputDataStore store : stores) {
            for (String attributeName : store.getAttributes()) {

                try {
                    JAMSData attribute = (JAMSData) entityArray[0].getObject(attributeName);

                    // don't care as this attribute is not even existing
                    if (attribute == null) {
                        continue;
                    }
                    Class clazz = attribute.getClass();
                    getDataObject(entityArray, clazz, attributeName, DataAccessor.READ_ACCESS, null);
                } catch (JAMSEntity.NoSuchAttributeException nsae) {
                    getModel().getRuntime().sendErrorMsg(JAMS.i18n("Can't_trace_attribute_") + attributeName
                            + JAMS.i18n("_in_context_") + this.getInstanceName() + JAMS.i18n("_(not_found)!"));
                    // will do nothing here since this will be handled at
                    // the DataTracer's init method below..
                } catch (Exception e) {
                    getModel().getRuntime().sendErrorMsg(JAMS.i18n("Error_while_trying_to_trace_") + attributeName + ": " + this.getInstanceName());
                    getModel().getRuntime().handle(e, false);
                }
            }
        }

        // check if new dataAccessor objects were added
        // if so, create new array from list
        if (this.dataAccessorMap.size() > this.dataAccessors.length) {
            this.dataAccessors = dataAccessorMap.values().toArray(new DataAccessor[dataAccessorMap.size()]);
        }

        if (this.dataTracers != null) {
            for (DataTracer dataTracer : this.dataTracers) {
                if (dataTracer != null) {
                    dataTracer.updateDataAccessors();
                }
            }
        }
    }

    protected JAMSData getDataObject(final Attribute.Entity[] ea, Class clazz, final String attributeName, final int accessType, JAMSData componentObject) throws InstantiationException, IllegalAccessException, ClassNotFoundException, JAMSEntity.NoSuchAttributeException {
        JAMSData dataObject;
        DataAccessor da = null;

        dataObject = attributeMap.get(attributeName);
        if (dataObject == null) {

            if (componentObject != null) {
                dataObject = componentObject;
            } else {
                try {
                    dataObject = DefaultDataFactory.getDataFactory().createInstance(clazz);
                } catch (InstantiationException ex) {
                    getModel().getRuntime().handle(ex, false);
                } catch (IllegalAccessException ex) {
                    getModel().getRuntime().handle(ex, false);
                }
            }

            attributeMap.put(attributeName, dataObject);
            DataFactory dataFactory = getModel().getRuntime().getDataFactory();
            if (clazz.isInterface()) {
                clazz = dataFactory.getImplementingClass(clazz);
            }

            if (JAMSDouble.class.isAssignableFrom(clazz)) {
                da = new DoubleAccessor(dataFactory, ea, dataObject, attributeName, accessType);
            } else if (JAMSDoubleArray.class.isAssignableFrom(clazz)) {
                da = new DoubleArrayAccessor(dataFactory, ea, dataObject, attributeName, accessType);
            } else if (JAMSLong.class.isAssignableFrom(clazz)) {
                da = new CalendarAccessor(dataFactory, ea, dataObject, attributeName, accessType);
            } else if (JAMSLongArray.class.isAssignableFrom(clazz)) {
                da = new LongArrayAccessor(dataFactory, ea, dataObject, attributeName, accessType);
            } else if (JAMSString.class.isAssignableFrom(clazz)) {
                da = new StringAccessor(dataFactory, ea, dataObject, attributeName, accessType);
            } else if (JAMSStringArray.class.isAssignableFrom(clazz)) {
                da = new StringArrayAccessor(dataFactory, ea, dataObject, attributeName, accessType);
            } else if (JAMSBoolean.class.isAssignableFrom(clazz)) {
                da = new BooleanAccessor(dataFactory, ea, dataObject, attributeName, accessType);
            } else if (JAMSBooleanArray.class.isAssignableFrom(clazz)) {
                da = new BooleanArrayAccessor(dataFactory, ea, dataObject, attributeName, accessType);
            } else if (JAMSFloat.class.isAssignableFrom(clazz)) {
                da = new FloatAccessor(dataFactory, ea, dataObject, attributeName, accessType);
            } else if (JAMSFloatArray.class.isAssignableFrom(clazz)) {
                da = new FloatArrayAccessor(dataFactory, ea, dataObject, attributeName, accessType);
            } else if (JAMSInteger.class.isAssignableFrom(clazz)) {
                da = new IntegerAccessor(dataFactory, ea, dataObject, attributeName, accessType);
            } else if (JAMSIntegerArray.class.isAssignableFrom(clazz)) {
                da = new IntegerArrayAccessor(dataFactory, ea, dataObject, attributeName, accessType);
            } else if (JAMSString.class.isAssignableFrom(clazz)) {
                da = new StringAccessor(dataFactory, ea, dataObject, attributeName, accessType);
            } else if (JAMSDirName.class.isAssignableFrom(clazz)) {
                da = new StringAccessor(dataFactory, ea, dataObject, attributeName, accessType);
            } else if (JAMSEntity.class.isAssignableFrom(clazz)) {
                da = new EntityAccessor(dataFactory, ea, dataObject, attributeName, accessType);
            } else if (JAMSEntityCollection.class.isAssignableFrom(clazz)) {
                da = new EntityCollectionAccessor(dataFactory, ea, dataObject, attributeName, accessType);
            } else if (JAMSCalendar.class.isAssignableFrom(clazz)) {
                da = new CalendarAccessor(dataFactory, ea, dataObject, attributeName, accessType);
            } else if (JAMSTimeInterval.class.isAssignableFrom(clazz)) {
                da = new TimeIntervalAccessor(dataFactory, ea, dataObject, attributeName, accessType);
            } else if (JAMSDocument.class.isAssignableFrom(clazz)) {
                da = new DocumentAccessor(dataFactory, ea, dataObject, attributeName, accessType);
            } else if (JAMSGeometry.class.isAssignableFrom(clazz)) {
                da = new GeometryAccessor(dataFactory, ea, dataObject, attributeName, accessType);
            } else if (JAMSObject.class.isAssignableFrom(clazz)) {
                da = new ObjectAccessor(dataFactory, ea, dataObject, attributeName, accessType);
            } else if (JAMSObjectArray.class.isAssignableFrom(clazz)) {
                da = new ObjectArrayAccessor(dataFactory, ea, dataObject, attributeName, accessType);
            } else {
                getModel().getRuntime().sendHalt(JAMS.i18n("Class_") + clazz.getCanonicalName() + JAMS.i18n("_not_supported!") + " (attribute:" + attributeName + ")");
            }

            if (da != null) {
                dataAccessorMap.put(attributeName, da);
            }
        }
        return dataObject;
    }

    protected void initEntityData() {
        //in case the components want to write access the objects, trace the entity objects attributes
        for (int i = 0; i < dataAccessors.length; i++) {
            if (dataAccessors[i].getAccessType() == DataAccessor.WRITE_ACCESS) {
                dataAccessors[i].initEntityData();
            }
        }
    }

    /**
     * Initialization of this context: 1. Create accessors for all attributes of
     * this context which are to be accessed by descendent components 2. Create
     * the data tracer objects which take care of outputting values of
     * attributes of this context 3. Calling the init() method of all child
     * components
     */
    @Override
    public void init() {

        runEnumerator = null;

        if (!doRun) {
            return;
        }

        // initAll accessors for data exchange between context attributes and
        // component attributes
        initAccessors();

        // create the init/cleanup enumerator (i.e. one invocation for every component)
        if (initEnumerator == null) {
            initEnumerator = getInitEnumerator();
        }

        // initialize init/cleanup enumerator and start iteration
        initEnumerator.reset();

        Component comp = null;
        try {
            while (initEnumerator.hasNext() && doRun) {
                comp = initEnumerator.next();
                //comp.updateInit();
                comp.init();

            }
        } catch (Exception e) {
            getModel().getRuntime().handle(e, comp.getInstanceName());
        }

        initEntityData();

        if (dataTracers != null) {
            for (DataTracer t : this.dataTracers) {
                t.updateDataAccessors();
            }
        }

        createRunRunnable(getModel().isProfiling());
        createInitAllRunnable(getModel().isProfiling());
        createCleanupAllRunnable(getModel().isProfiling());
        createResumeRunnable(getModel().isProfiling());
    }

    @Override
    public void initAll() {

        //initEntityData();
        if (getEntities().getEntities().isEmpty()) {
            return;
        }

        if (initAllEnumerator == null) {
            initAllEnumerator = getInitAllEnumerator();
        }

        initAllEnumerator.reset();

        currentComponent = null;

        while (initAllEnumerator.hasNext() && doRun) {
            currentComponent = initAllEnumerator.next();
            initAllRunnable.run();
        }

        updateEntityData();
    }

    @Override
    public void run() {

        //initEntityData();
        if (getEntities().getEntities().isEmpty()) {
            return;
        }

        if (runEnumerator == null) {
            runEnumerator = getRunEnumerator();
        }

        runEnumerator.reset();

        currentComponent = null;

        while (runEnumerator.hasNext() && doRun) {
            currentComponent = runEnumerator.next();

            try {
                runRunnable.run();
            } catch (Exception e) {
                getModel().getRuntime().handle(e, currentComponent.getInstanceName());
            }
        }

        updateEntityData();

        for (DataTracer dataTracer : dataTracers) {
            dataTracer.trace();
        }
    }

    @Override
    public void cleanup() {

        if (cleanupEnumerator == null) {
            cleanupEnumerator = getCleanupEnumerator();
        }

        cleanupEnumerator.reset();

        Component comp = null;
        while (cleanupEnumerator.hasNext() && doRun) {
            comp = cleanupEnumerator.next();
            try {
                comp.cleanup();
            } catch (Exception e) {
                getModel().getRuntime().handle(e, comp.getInstanceName());
            }
        }

        updateEntityData();
    }
    
    @Override
    public void cleanupAll() {

        //initEntityData();
        if (getEntities().getEntities().isEmpty()) {
            return;
        }

        if (cleanupAllEnumerator == null) {
            cleanupAllEnumerator = getCleanupAllEnumerator();
        }

        cleanupAllEnumerator.reset();

        currentComponent = null;

        while (cleanupAllEnumerator.hasNext() && doRun) {
            currentComponent = cleanupAllEnumerator.next();
            cleanupAllRunnable.run();
        }

        updateEntityData();
    }

    

    @Override
    public void resume() {

        for (Component compArray : getCompArray()) {
            try {
                compArray.restore();
            } catch (Exception e) {
                getModel().getRuntime().handle(e, compArray.getInstanceName());
            }
        }

        if (runEnumerator == null) {
            runEnumerator = getRunEnumerator();
        }

        currentComponent = null;

        if (runEnumerator.hasPrevious()) {
            currentComponent = runEnumerator.previous();
            try {
                if (currentComponent instanceof JAMSContext) {
                    resumeRunnable.run();
                }
            } catch (Exception e) {
                getModel().getRuntime().handle(e, currentComponent.getInstanceName());
            }
            runEnumerator.next();
        }
        while (runEnumerator.hasNext() && doRun) {
            currentComponent = runEnumerator.next();
            try {
                runRunnable.run();
            } catch (Exception e) {
                getModel().getRuntime().handle(e, currentComponent.getInstanceName());
            }
        }

        updateEntityData();

        if (!isPaused) {
            for (DataTracer dataTracer : dataTracers) {
                dataTracer.trace();
            }
        }
    }

    @Override
    public void restore() {
        for (Component compArray : getCompArray()) {
            try {
                compArray.restore();
            } catch (Exception e) {
                getModel().getRuntime().handle(e, compArray.getInstanceName());
            }
        }
    }

    @Override
    public HashMap<String, DataAccessor> getDataAccessorMap() {
        return dataAccessorMap;
    }

    @Override
    public HashMap<String, JAMSData> getAttributeMap() {
        return attributeMap;
    }

    /**
     * @return the accessSpecs
     */
    @Override
    public ArrayList<AttributeAccess> getAttributeAccessList() {
        return attributeAccessList;
    }

    /**
     * @param accessSpecs the accessSpecs to set
     */
    public void setAccessSpecs(ArrayList<AttributeAccess> accessSpecs) {
        this.attributeAccessList = accessSpecs;
    }

    @Override
    public Component getComponent(String name) {
        for (int i = 0; i < components.size(); i++) {
            if (components.get(i).getInstanceName().equals(name)) {
                return components.get(i);
            }
            if (components.get(i) instanceof Context) {
                Component comp = ((Context) components.get(i)).getComponent(name);
                if (comp != null) {
                    return comp;
                }
            }
        }
        return null;
    }

    /**
     * Update the attributes of the current entity of this context with the
     * output attributes of the accessing components
     */
    @Override
    public void updateEntityData() {
        for (DataAccessor dataAccessor : dataAccessors) {
            dataAccessor.write();
        }
    }

    /**
     * Update components' input attribute values with the attribute values from
     * the index-th entity of this context
     *
     * @param index The index of the entity to use
     */
    @Override
    public void updateComponentData(int index) {
        for (DataAccessor dataAccessor : dataAccessors) {
            dataAccessor.setIndex(index);
            //read entity data before execution
            dataAccessor.read();
        }
    }

    @Override
    public long getNumberOfIterations() {
        return getEntities().getEntities().size();
    }

    class AttributeSpec implements Serializable {

        public String attributeName, className, value;

        public AttributeSpec(String attributeName, String className, String value) {
            this.attributeName = attributeName;
            this.className = className;
            this.value = value;
        }
    }

    @Override
    public Attribute.EntityCollection getEntities() {
        return entities;
    }

    @Override
    public void setEntities(Attribute.EntityCollection entities) {
        this.entities = entities;
    }

    /*public void createMemoryStatistics(){
     HashMap<String, Long> memoryMap = new HashMap<String, Long>();
     ArrayList<Attribute.Entity> entityList = new ArrayList<Attribute.Entity>();
     for (Attribute.Entity e : entityList){
            
     memoryMap.put("Overhead", )
     entityList.get(i).getKeys()
     }
     }*/
    private void writeObject(ObjectOutputStream objOut) throws IOException {
        objOut.defaultWriteObject();
        objOut.writeBoolean(this.getModel().isProfiling());
    }

    private void readObject(java.io.ObjectInputStream stream) throws IOException, ClassNotFoundException {
        stream.defaultReadObject();

        boolean doProfiling = stream.readBoolean();
        createRunRunnable(doProfiling);
        createResumeRunnable(doProfiling);
    }
}
