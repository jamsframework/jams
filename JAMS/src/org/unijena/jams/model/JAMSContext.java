/*
 * JAMSContext.java
 *
 * Created on 22. Juni 2005, 21:03
 *
 * This file is part of JAMS
 * Copyright (C) 2005 FSU Jena
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

package org.unijena.jams.model;

/**
 *
 * @author S. Kralisch
 */

import java.lang.reflect.Array;
import java.util.*;
import java.util.Map.Entry;
import org.unijena.jams.JAMS;
import org.unijena.jams.data.*;
import org.unijena.jams.dataaccess.*;
import org.unijena.jams.dataaccess.CalendarAccessor;
import org.unijena.jams.runtime.JAMSRuntime;

@JAMSComponentDescription(
title="JAMS Component",
        author="Sven Kralisch",
        date="27. Juni 2005",
        description="This component represents a JAMS context which is the top level " +
        "component of every component hierarchie in JAMS")
        public class JAMSContext extends JAMSComponent {
    
    protected JAMSEntityCollection entities;
    protected JAMSEntity currentEntity;
    
    protected ArrayList <JAMSComponent> components = new ArrayList<JAMSComponent>();
    protected JAMSComponentEnumerator runEnumerator = null;
    protected JAMSComponentEnumerator initCleanupEnumerator = null;
    
    protected ArrayList<AccessSpec> accessSpecs = new ArrayList<AccessSpec>();
    protected ArrayList<AttributeSpec> attributeSpecs = new ArrayList<AttributeSpec>();
    protected JAMSEntityDataAccessor[] dataAccessors = new JAMSEntityDataAccessor[0];
    protected ArrayList<JAMSEntityDataAccessor> daList = new ArrayList<JAMSEntityDataAccessor>();
    protected HashMap<String, JAMSData> attribs = new  HashMap<String, JAMSData>();
    
    protected boolean doRun = true;
    
    public JAMSContext() {
        //create an entity collection with one entity
        setCurrentEntity(JAMSDataFactory.createEntity());
        ArrayList<JAMSEntity> list = new ArrayList<JAMSEntity>();
        list.add(getCurrentEntity());
        setEntities(new JAMSEntityCollection());
        getEntities().setEntities(list);
        
        attribs = new HashMap<String, JAMSData>();
    }
    
    public void exchange(int i,int j) {
        JAMSComponent oi = components.get(i);
        JAMSComponent oj = components.get(j);
        components.set(i,oj);
        components.set(j,oi);
    }
    
    public void addComponent(JAMSComponent c) {
        components.add(c);
    }
    
    public void removeComponent(int index) {
        components.remove(index);
    }
    
    public ArrayList getComponents() {
        return components;
    }
    
    public void setComponents(ArrayList <JAMSComponent> components) {
        this.components = components;
        Iterator<JAMSComponent> i = components.iterator();
        while (i.hasNext()) {
            i.next().setContext(this);
        }
    }
    
    public JAMSComponentEnumerator getRunEnumerator() {
        return new RunEnumerator();
    }
    
    public JAMSComponentEnumerator getChildrenEnumerator() {
        return new ChildrenEnumerator();
    }
    
    public JAMSComponent[] getCompArray() {
        JAMSComponent[] comps = new JAMSComponent[components.size()];
        components.toArray(comps);
        return comps;
    }
    
    public void addAccess(JAMSComponent user, String varName, String attributeName, int accessType) {
        accessSpecs.add(new AccessSpec(user, varName, attributeName, accessType));
    }
    
    public void addAttribute(String attributeName, String clazz, String value) {
        attributeSpecs.add(new AttributeSpec(attributeName, clazz, value));
    }
    
    /**
     * 
     * @param model
     */
    @Override
    public void setModel(JAMSModel model) {
        super.setModel(model);
        JAMSRuntime rt = getModel().getRuntime();
        rt.addRunStateObserver(new Observer() {
            public void update(Observable obs, Object obj) {
                if (getModel().getRuntime().getRunState() != JAMS.RUNSTATE_RUN)
                    JAMSContext.this.doRun = false;
            }
        });        
    }
    /*
    public void registerObserver() {
        JAMSRuntime rt = getModel().getRuntime();
        rt.addRunStateObserver(new Observer() {
            public void update(Observable obs, Object obj) {
                if (getModel().getRuntime().getRunState() != JAMS.RUNSTATE_RUN)
                    JAMSContext.this.doRun = false;
            }
        });
    }
    */
    
    public void initAccessors(){    
        attribs =  new  HashMap<String, JAMSData>();
        
        daList = new ArrayList<JAMSEntityDataAccessor>();
        
        AccessSpec accessSpec;
        AttributeSpec attributeSpec;
        JAMSData dataObject;
        Class clazz;
        
        JAMSEntity[] entityArray = getEntities().getEntityArray();
        
        //handle all attribute declarations in this context
        Iterator<AttributeSpec> attributeIterator = attributeSpecs.iterator();
        while (attributeIterator.hasNext()) {
            attributeSpec = attributeIterator.next();
            
            try {
                
                JAMSData data = JAMSDataFactory.getData(attributeSpec.className);
                data.setValue(attributeSpec.value);
                attribs.put(attributeSpec.attributeName, data);
                
                //add attributes to "handle map"
                String id = this.getInstanceName() + "." + attributeSpec.attributeName;
                getModel().getRuntime().getDataHandles().put(id, data);
                
                for (JAMSEntity entity : entityArray) {
                    entity.setObject(attributeSpec.attributeName, data);
                }
                
            } catch (ClassNotFoundException cnfe) {
                getModel().getRuntime().handle(cnfe, false);
            } catch (InstantiationException ie) {
                getModel().getRuntime().handle(ie, false);
            } catch (IllegalAccessException iae) {
                getModel().getRuntime().handle(iae, false);
            }
        }
        
        Iterator<AccessSpec> accessIterator = accessSpecs.iterator();
        while (accessIterator.hasNext()) {
            accessSpec = accessIterator.next();
            
            try {
                
                clazz = accessSpec.component.getClass().getDeclaredField(accessSpec.varName).getType();
                
                if (clazz.isArray()) {
                    
                    String className = clazz.getCanonicalName();
                    className = className.substring(0, className.length()-2);
                    
                    StringTokenizer tok = new StringTokenizer(accessSpec.attributeName, ";");
                    int count = tok.countTokens();
                    
                    Class componentClass = Class.forName(className);
                    JAMSData[] array = (JAMSData[]) Array.newInstance(componentClass, count);
                    
                    for (int i = 0; i < count; i++)  {
                        //array[i] = (JAMSData) componentClass.newInstance();
                        array[i] = getDataObject(entityArray, componentClass, tok.nextToken(), accessSpec.accessType, null);
                    }
                    accessSpec.component.getClass().getDeclaredField(accessSpec.varName).set(accessSpec.component, array);
                    
                } else {
                    
                    //maybe the component's data object already has a value assigned, so get it
                    JAMSData componentObject = null;//(JAMSData) accessSpec.component.getClass().getDeclaredField(accessSpec.varName).get(accessSpec.component);
                    //JAMSData componentObject = (JAMSData) accessSpec.component.getClass().getDeclaredField(accessSpec.varName).get(accessSpec.component);
                    
                    //get the data object belonging to the attribute
                    dataObject = getDataObject(entityArray, clazz, accessSpec.attributeName, accessSpec.accessType, componentObject);
                    
                    //assign the dataObject to the component
                    accessSpec.component.getClass().getDeclaredField(accessSpec.varName).set(accessSpec.component, dataObject);
                    
                    
                }
            } catch (Exception e) {
                getModel().getRuntime().sendErrorMsg("Error occured in " + accessSpec.component.getInstanceName() + ": " + accessSpec.varName);
                getModel().getRuntime().handle(e, false);
            }
        }
        
        if (daList.size()>0) {
            this.dataAccessors = daList.toArray(new JAMSEntityDataAccessor[daList.size()]);
        }
        
        for (int i=0;i<daList.size();i++){
            int index = 0;
            while(index >= 0){
                try{
                    daList.get(i).setIndex(index);
                    daList.get(i).read();
                    
                }catch(Exception e){
                    break;
                }
                index++;                
            }
            daList.get(i).setIndex(0);
        }
    }
    
    public void init() {
        attribs =  new  HashMap<String, JAMSData>();
                
        runEnumerator = null;
        
        if (!doRun) {
            return;
        }

        initAccessors();
        
        if (initCleanupEnumerator == null) {
            initCleanupEnumerator = getChildrenEnumerator();
        }
        
        initCleanupEnumerator.reset();
        while(initCleanupEnumerator.hasNext() && doRun) {
            JAMSComponent comp = initCleanupEnumerator.next();
            //comp.updateInit();
            try {
                comp.init();
            } catch (Exception e) {
                getModel().getRuntime().handle(e, comp.getInstanceName());
            }
        }
        
        initEntityData();
    }
    
    protected JAMSData getDataObject(final JAMSEntity[] ea, final Class clazz, final String attributeName, final int accessType, JAMSData componentObject) throws InstantiationException, IllegalAccessException, ClassNotFoundException, JAMSEntity.NoSuchAttributeException {
        JAMSData dataObject;
        JAMSEntityDataAccessor da = null;
        
        dataObject = attribs.get(attributeName);
        if (dataObject == null) {
            
            if (componentObject != null) {
                dataObject = componentObject;
            } else {
                if (clazz.getName().equals("org.unijena.jams.data.JAMSEntity")) {
                    dataObject = (JAMSData) JAMSDataFactory.createEntity();
                } else {
                    dataObject = (JAMSData) clazz.newInstance();
                }
            }
            
            attribs.put(attributeName, dataObject);
            
            if (clazz.equals(JAMSDouble.class)) {
                da = new DoubleAccessor(ea, dataObject, attributeName, accessType);
            } else if (clazz.equals(JAMSDoubleArray.class)) {
                da = new DoubleArrayAccessor(ea, dataObject, attributeName, accessType);
            } else if (clazz.equals(JAMSLong.class)) {
                da = new CalendarAccessor(ea, dataObject, attributeName, accessType);
            } else if (clazz.equals(JAMSLongArray.class)) {
                da = new LongArrayAccessor(ea, dataObject, attributeName, accessType);
            } else if (clazz.equals(JAMSString.class)) {
                da = new StringAccessor(ea, dataObject, attributeName, accessType);
            } else if (clazz.equals(JAMSStringArray.class)) {
                da = new StringArrayAccessor(ea, dataObject, attributeName, accessType);
            } else if (clazz.equals(JAMSBoolean.class)) {
                da = new BooleanAccessor(ea, dataObject, attributeName, accessType);
            } else if (clazz.equals(JAMSBooleanArray.class)) {
                da = new BooleanArrayAccessor(ea, dataObject, attributeName, accessType);
            } else if (clazz.equals(JAMSFloat.class)) {
                da = new FloatAccessor(ea, dataObject, attributeName, accessType);
            } else if (clazz.equals(JAMSFloatArray.class)) {
                da = new FloatArrayAccessor(ea, dataObject, attributeName, accessType);
            } else if (clazz.equals(JAMSInteger.class)) {
                da = new IntegerAccessor(ea, dataObject, attributeName, accessType);
            } else if (clazz.equals(JAMSIntegerArray.class)) {
                da = new IntegerArrayAccessor(ea, dataObject, attributeName, accessType);
            } else if (clazz.equals(JAMSEntity.class)) {
                da = new EntityAccessor(ea, dataObject, attributeName, accessType);
            } else if (clazz.equals(JAMSEntityCollection.class)) {
                da = new EntityCollectionAccessor(ea, dataObject, attributeName, accessType);
            } else if (clazz.equals(JAMSCalendar.class)) {
                da = new CalendarAccessor(ea, dataObject, attributeName, accessType);
            } else {
                getModel().getRuntime().sendHalt("Class " + clazz.getCanonicalName() + " not supported!");
            }
            
            daList.add(da);
        }
        return dataObject;
    }
    
    protected void initEntityData() {
        
        //in case the components want to write access the objects, update the entity objects attributes
        for (int i = 0; i < dataAccessors.length; i++) {
            if (dataAccessors[i].getAccessType() != JAMSEntityDataAccessor.READ_ACCESS) {
                for (int j = 0; j < getEntities().getEntities().size(); j++) {
                    dataAccessors[i].setIndex(j);
                    dataAccessors[i].write();
                }
            }
        }
    }
    
    public void run() {
        
        //initEntityData();
        
        if (runEnumerator == null) {
            runEnumerator = getRunEnumerator();
        }
        
        runEnumerator.reset();
        while(runEnumerator.hasNext() && doRun) {
            JAMSComponent comp = runEnumerator.next();
            //comp.updateRun();
            try {
                comp.run();
            } catch (Exception e) {
                getModel().getRuntime().handle(e, comp.getInstanceName());
            }
        }
        
        updateEntityData();
    }
    
    public void cleanup() {
        
        if (initCleanupEnumerator == null) {
            initCleanupEnumerator = getChildrenEnumerator();
        }
        
        initCleanupEnumerator.reset();
        
        while(initCleanupEnumerator.hasNext()) {
            JAMSComponent comp = initCleanupEnumerator.next();
            try {
                comp.cleanup();
            } catch (Exception e) {
                getModel().getRuntime().handle(e, comp.getInstanceName());
            }
        }
        
        ArrayList<JAMSEntity> list = new ArrayList<JAMSEntity>();
        list.add(JAMSDataFactory.createEntity());
        
    }
    
    class ChildrenEnumerator implements JAMSComponentEnumerator {
        
        JAMSComponent[] compArray = getCompArray();
        int index = 0;
        
        public boolean hasNext() {
            return (index < compArray.length);
        }
        
        public JAMSComponent next() {
            return compArray[index++];
        }
        
        public void reset() {
            index = 0;
        }                
    }
    
    class RunEnumerator implements JAMSComponentEnumerator {
        
        JAMSComponentEnumerator ce = getChildrenEnumerator();
        JAMSEntityEnumerator ee = getEntities().getEntityEnumerator();
        int index = 0;
        
        public boolean hasNext() {
            boolean nextComp = ce.hasNext();
            boolean nextEntity = ee.hasNext();
            return (nextEntity || nextComp) ;
        }
                       
        public JAMSComponent next() {
            // check end of component elements list, if required switch to the next
            // entity and start with the new Component list again
            if (!ce.hasNext() && ee.hasNext()) {
                updateEntityData();
                setCurrentEntity(ee.next());
                index++;
                updateDataAccessors(index);
                ce.reset();
            }
            return ce.next();
        }
        
        public void reset() {
            ee.reset();
            setCurrentEntity(getEntities().getCurrent());
            ce.reset();
            index = 0;
            updateDataAccessors(index);
        }
    }
    
    protected int componentAllreadyProcessed(JAMSComponent position, JAMSComponent component){
        if (this.instanceName.equals(position.instanceName))
            return 0;
        if (this == component)                
            return 1;
        for (int j=0;j<this.components.size();j++){
            JAMSComponent c = this.components.get(j);
            if (c == position)
                return 0;
            if (c == component)                
                return 1;
            if (c instanceof JAMSContext){
                JAMSContext context = (JAMSContext)c;
                int result = context.componentAllreadyProcessed(position,component);
                if (result == 0 || result == 1)
                    return result;
            }
        }  
        return 2;
    }
    //looks if component object contains valid data
    protected int isValid(JAMSComponent position, Object componentObj){
        /*three steps:
        1. get name of attribute to componentObj in this context
            <context> attribName specifies attribute fully
        2. get name of components which use this attribute in write or read-write mode
        3. look one of these components has allready been executed
        */
        
        //seatch for attribname
        Iterator<Entry<String,JAMSData>> iter = this.attribs.entrySet().iterator();
        String attribName = null;
        while (iter.hasNext()){
            Entry<String,JAMSData> e = iter.next();
            if ( (Object)e.getValue() == componentObj){
                attribName = e.getKey();
                break;
            }
        }
        //search in accessSpecs for components which use this attrib
        JAMSComponent component = null;
        if (attribName != null){
            for (int i=0;i<this.accessSpecs.size();i++){                
                if ( this.accessSpecs.get(i).attributeName.compareTo(attribName) == 0){
                    //do they write this attrib?
                    if (this.accessSpecs.get(i).accessType != JAMSEntityDataAccessor.READ_ACCESS){
                        component = this.accessSpecs.get(i).component;
                        //has component been executed
                        if (componentAllreadyProcessed(position,component) == 1)
                            return 1;
                    }
                }
            }                        
        }
        //look if there is an child context which uses this componentObj
        for (int j=0;j<components.size();j++){
            JAMSComponent c = components.get(j);
            if (c.instanceName.equals(position.instanceName)){
                return 0;
            }
            if (c instanceof JAMSContext){
                if ( ((JAMSContext)components.get(j)).isValid(position,componentObj) == 1)
                    return 1;
            }
        }    
        return 2;
    }
    
    //update entity data, but update data only, if data source component has allready been executed,
    //which means that data source component is executed before currentComponent
    protected void updateEntityData(JAMSComponent currentComponent) {                
        //if this context has not been executed at all, exit
        if (this.getModel().componentAllreadyProcessed(currentComponent, this) != 1)
                return;
        for (int i = 0; i < dataAccessors.length; i++) {
            //get pointer to component data
            Object componentObj = dataAccessors[i].getComponentObject();
            //look if component data is already up to date
            if (isValid(currentComponent,componentObj) == 1){
                dataAccessors[i].write();
            }
        }
    }
    
    protected void updateEntityData() {
        //write entity data after execution
        for (int i = 0; i < dataAccessors.length; i++) {
            dataAccessors[i].write();
        }
    }
    
    protected void updateDataAccessors(int index) {
        for (int i = 0; i < dataAccessors.length; i++) {
            dataAccessors[i].setIndex(index);
            //read entity data before execution
            dataAccessors[i].read();
        }
    }
    
    public long getNumberOfIterations() {
        return getEntities().getEntities().size();
    }
    
    protected class AttributeSpec {
        
        String attributeName, className, value;
        
        public AttributeSpec(String attributeName, String className, String value) {
            this.attributeName = attributeName;
            this.className = className;
            this.value = value;
        }
        
    }
    
    protected class AccessSpec {
        
        JAMSComponent component;
        String varName;
        String attributeName;
        int accessType;
        
        public AccessSpec(JAMSComponent component, String varName, String attributeName, int accessType) {
            this.component = component;
            this.varName = varName;
            this.attributeName = attributeName;
            this.accessType = accessType;
        }
        
    }
    
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
    
}
