/*
 * JAMSModel.java
 * Created on 31. Mai 2006, 17:03
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
package jams.model;

import jams.JAMS;
import jams.data.Attribute.FloatArray;
import jams.data.JAMSBoolean;
import jams.data.JAMSBooleanArray;
import jams.data.JAMSCalendar;
import jams.data.JAMSData;
import jams.tools.JAMSTools;
import jams.data.JAMSDirName;
import jams.data.JAMSDouble;
import jams.data.JAMSDoubleArray;
import jams.workspace.JAMSWorkspace;
import jams.workspace.stores.OutputDataStore;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.HashMap;
import jams.data.JAMSFloat;
import jams.data.JAMSGeometry;
import jams.data.JAMSInteger;
import jams.data.JAMSIntegerArray;
import jams.data.JAMSLong;
import jams.data.JAMSLongArray;
import jams.data.JAMSObject;
import jams.data.JAMSString;
import jams.data.JAMSStringArray;
import jams.data.JAMSTimeInterval;
import jams.dataaccess.DataAccessor;
import jams.runtime.JAMSRuntime;
import jams.tools.SnapshotTools.ContextSnapshotData;
import jams.workspace.InvalidWorkspaceException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Iterator;

/**
 *
 * @author S. Kralisch
 */
@JAMSComponentDescription (title = "JAMS model",
                           author = "Sven Kralisch",
                           date = "26. September 2005",
                           description = "This component represents a JAMS model which is a special type of context component")
public class JAMSModel extends JAMSContext implements Model {

    @JAMSVarDescription (access = JAMSVarDescription.AccessType.READ)
    public JAMSDirName workspaceDirectory = new JAMSDirName();

    private JAMSRuntime runtime;

    private String name, author, date;

    public JAMSWorkspace workspace;

    transient private HashMap<Component, ArrayList<Field>> nullFields;

    public JAMSModel(JAMSRuntime runtime) {
        this.runtime = runtime;
    }

    public JAMSRuntime getRuntime() {
        return runtime;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
        this.setInstanceName(name);
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    @Override
    public void init() {

        runtime.println("", JAMS.STANDARD);
        runtime.println(JAMS.resources.getString("starting_simulation"), JAMS.STANDARD);
        runtime.println(JAMS.resources.getString("*************************************"), JAMS.STANDARD);

        // check if workspace directory was specified
        if (workspaceDirectory.getValue() == null) {
            runtime.sendHalt(JAMS.resources.getString("No_workspace_directory_specified,_stopping_execution!"));
            return;
        }

        // save current model parameter to workspace output directory
        getRuntime().saveModelParameter();

        super.init();

        if (!doRun) {
            return;
        }

        if (!getNullFields().isEmpty()) {
            getRuntime().println(JAMS.resources.getString("##############_UNDEFINED_FIELDS_####################################"), JAMS.VVERBOSE);
            for (Component comp : getNullFields().keySet()) {
                ArrayList<Field> nf = getNullFields().get(comp);
                if (nf.isEmpty()) {
                    continue;
                }
                String str = JAMS.resources.getString("##_") + comp.getInstanceName() + ": ";
                for (Field field : nf) {
                    str += field.getName() + " ";
                }
                getRuntime().println(str, JAMS.VVERBOSE);
            }
            getRuntime().println(JAMS.resources.getString("####################################################################"), JAMS.VVERBOSE);
        }
        setupDataTracer();
    }

    private boolean moveWorkspaceDirectory(String workspaceDirectory) {
        setWorkspaceDirectory(workspaceDirectory);
        // create output dir
        try {
            this.initWorkspace();
            this.workspace.checkValidity(false);
        } catch (InvalidWorkspaceException e) {
            getRuntime().sendHalt("Error during model setup: \"" +
                    this.workspace.getDirectory().getAbsolutePath() + "\" is not a valid datastore, because: " + e.toString());
            return false;
        }
        // reanimate data tracers
        setupDataTracer();
        this.components.size();
        return true;
    }

    public void setWorkspaceDirectory(String workspaceDirectory) {
        this.workspaceDirectory.setValue(workspaceDirectory);
    }

    public void initWorkspace() throws InvalidWorkspaceException {
        String workspaceDir = workspaceDirectory.getValue();
        if (JAMSTools.isEmptyString(workspaceDir)) {
            this.workspace = null;
        } else {
            this.workspace = new JAMSWorkspace(new File(workspaceDir), getRuntime());
        }
    }

    public JAMSWorkspace getWorkspace() {
        return this.workspace;
    }

    public File getWorkspaceDirectory() {
        return workspace.getDirectory();
    }

    private File getOutputDataDirectory() {
        return workspace.getOutputDataDirectory();
    }

    private File getInputDirectory() {
        return workspace.getInputDirectory();
    }

    public OutputDataStore[] getOutputDataStores(String contextName) {
        if (this.workspace == null) {
            return new OutputDataStore[0];
        }
        return this.workspace.getOutputDataStores(contextName);
    }

    public HashMap<Component, ArrayList<Field>> getNullFields() {
        return nullFields;
    }

    public void setNullFields(HashMap<Component, ArrayList<Field>> nullFields) {
        this.nullFields = nullFields;
    }
    
    private void collectEntityCollections(Context currentContext, Component position, HashMap<String, ContextSnapshotData> collection) {        
        //currentContext.debug("C:\\Arbeit\\TIMELOOP_pre.dat" + counter1++);
        
        ContextSnapshotData data = new ContextSnapshotData();
        data.entities = currentContext.getEntities();
        data.dataAccessors = currentContext.getDataAccessorMap();
        data.iteratorState = currentContext.getIteratorState();
        collection.put(currentContext.getInstanceName(), data);

        for (int i = 0; i < currentContext.getComponents().size(); i++) {
            Component c = (Component) currentContext.getComponents().get(i);
            if (c instanceof Context) {
                collectEntityCollections((Context) c, position, collection);
            }
        }
    }

    private void restoreEntityCollections(Context currentContext, HashMap<String, ContextSnapshotData> collection, boolean restoreIterator) {
        ContextSnapshotData data = collection.get(currentContext.getInstanceName());

        for (int i = 0; i < currentContext.getComponents().size(); i++) {
            Component c = (Component) currentContext.getComponents().get(i);
            if (c instanceof Context) {
                restoreEntityCollections((Context) c, collection, restoreIterator);
            }
        }

        //resore entity data
        if (data == null) {
            return;
        }
        ArrayList<jams.data.Attribute.Entity> list = currentContext.getEntities().getEntities();
        for (int i = 0; i < list.size(); i++) {
            HashMap<String, Object> map = list.get(i).getValue();

            jams.data.Attribute.Entity e = data.entities.getEntities().get(i);
            Iterator<String> iter = e.getValue().keySet().iterator();
            while (iter.hasNext()) {
                String name = iter.next();
                Object obj_src = e.getValue().get(name);
                Object obj = map.get(name);

                if (obj_src instanceof JAMSBoolean) {
                    ((JAMSBoolean) obj).setValue(((JAMSBoolean) obj_src).getValue());
                } else if (obj_src instanceof JAMSBooleanArray) {
                    ((JAMSBooleanArray) obj).setValue(((JAMSBooleanArray) obj_src).getValue());
                } else if (obj_src instanceof JAMSCalendar) {
                    ((JAMSCalendar) obj).setValue(((JAMSCalendar) obj_src).getValue());
                } else if (obj_src instanceof JAMSDouble) {
                    ((JAMSDouble) obj).setValue(((JAMSDouble) obj_src).getValue());
                } else if (obj_src instanceof JAMSDoubleArray) {
                    ((JAMSDoubleArray) obj).setValue(((JAMSDoubleArray) obj_src).getValue());
                //these both are skipped, because entities are not replaced
                //but there are cases in which they have to be updated, this is kind of complicated, because
                //a) we have to update only entity attributes
                //b) if the entity contains other entities this is a recursive process
                //c) to prevent infinite loops, we have to determine which entities have been allready updated
                    /*if (obj instanceof JAMSEntity)
                ((JAMSEntity)obj).setValue(((JAMSEntity)data.dataAccessors.get(name).getComponentObject()).getValue());
                if (obj instanceof JAMSEntityCollection)
                ((JAMSEntityCollection)obj).setValue(((JAMSEntityCollection)data.dataAccessors.get(name).getComponentObject()).getValue());*/
                } else if (obj_src instanceof JAMSFloat) {
                    ((JAMSFloat) obj).setValue(((JAMSFloat) obj_src).getValue());
                } else if (obj_src instanceof FloatArray) {
                    ((FloatArray) obj).setValue(((FloatArray) obj_src).getValue());
                } else if (obj_src instanceof JAMSGeometry) {
                    ((JAMSGeometry) obj).setValue(((JAMSGeometry) obj_src).getValue());
                } else if (obj_src instanceof JAMSInteger) {
                    ((JAMSInteger) obj).setValue(((JAMSInteger) obj_src).getValue());
                } else if (obj_src instanceof JAMSIntegerArray) {
                    ((JAMSIntegerArray) obj).setValue(((JAMSIntegerArray) obj_src).getValue());
                } else if (obj_src instanceof JAMSLong) {
                    ((JAMSLong) obj).setValue(((JAMSLong) obj_src).getValue());
                } else if (obj_src instanceof JAMSLongArray) {
                    ((JAMSLongArray) obj).setValue(((JAMSLongArray) obj_src).getValue());
                } else if (obj_src instanceof JAMSString) {
                    ((JAMSString) obj).setValue(((JAMSString) obj_src).getValue());
                } else if (obj_src instanceof JAMSStringArray) {
                    ((JAMSStringArray) obj).setValue(((JAMSStringArray) obj_src).getValue());
                } else if (obj_src instanceof JAMSObject) {
                    ((JAMSObject) obj).setValue(((JAMSObject) obj_src).getValue());
                } else if (obj_src instanceof JAMSTimeInterval) {
                    ((JAMSTimeInterval) obj).setValue(((JAMSTimeInterval) obj_src).getValue());
                }
            }
        }


        //restore component data
        HashMap<String, DataAccessor> map = currentContext.getDataAccessorMap();
        Iterator<String> iter = map.keySet().iterator();
        while (iter.hasNext()) {
            String name = iter.next();
            DataAccessor da = map.get(name);
            if (da == null) {
                continue;
            }
            JAMSData obj = da.getComponentObject();
            if (obj instanceof JAMSBoolean) {
                ((JAMSBoolean) obj).setValue(((JAMSBoolean) data.dataAccessors.get(name).getComponentObject()).getValue());
            }else if (obj instanceof JAMSBooleanArray) {
                ((JAMSBooleanArray) obj).setValue(((JAMSBooleanArray) data.dataAccessors.get(name).getComponentObject()).getValue());
            }else if (obj instanceof JAMSCalendar) {
                ((JAMSCalendar) obj).setValue(((JAMSCalendar) data.dataAccessors.get(name).getComponentObject()).getValue());
            }else if (obj instanceof JAMSDouble) {
                ((JAMSDouble) obj).setValue(((JAMSDouble) data.dataAccessors.get(name).getComponentObject()).getValue());
            }else if (obj instanceof JAMSDoubleArray) {
                ((JAMSDoubleArray) obj).setValue(((JAMSDoubleArray) data.dataAccessors.get(name).getComponentObject()).getValue());
            /*if (obj instanceof JAMSEntity)
            ((JAMSEntity)obj).setValue(((JAMSEntity)data.dataAccessors.get(name).getComponentObject()).getValue());
            if (obj instanceof JAMSEntityCollection)
            ((JAMSEntityCollection)obj).setValue(((JAMSEntityCollection)data.dataAccessors.get(name).getComponentObject()).getValue());*/
            }else if (obj instanceof JAMSFloat) {
                ((JAMSFloat) obj).setValue(((JAMSFloat) data.dataAccessors.get(name).getComponentObject()).getValue());
            }else if (obj instanceof FloatArray) {
                ((FloatArray) obj).setValue(((FloatArray) data.dataAccessors.get(name).getComponentObject()).getValue());
            }else if (obj instanceof JAMSGeometry) {
                ((JAMSGeometry) obj).setValue(((JAMSGeometry) data.dataAccessors.get(name).getComponentObject()).getValue());
            }else if (obj instanceof JAMSInteger) {
                ((JAMSInteger) obj).setValue(((JAMSInteger) data.dataAccessors.get(name).getComponentObject()).getValue());
            }else if (obj instanceof JAMSIntegerArray) {
                ((JAMSIntegerArray) obj).setValue(((JAMSIntegerArray) data.dataAccessors.get(name).getComponentObject()).getValue());
            }else if (obj instanceof JAMSLong) {
                ((JAMSLong) obj).setValue(((JAMSLong) data.dataAccessors.get(name).getComponentObject()).getValue());
            }else if (obj instanceof JAMSLongArray) {
                ((JAMSLongArray) obj).setValue(((JAMSLongArray) data.dataAccessors.get(name).getComponentObject()).getValue());
            }else if (obj instanceof JAMSObject) {
                ((JAMSObject) obj).setValue(((JAMSObject) data.dataAccessors.get(name).getComponentObject()).getValue());
            }else if (obj instanceof JAMSString) {
                ((JAMSString) obj).setValue(((JAMSString) data.dataAccessors.get(name).getComponentObject()).getValue());
            }else if (obj instanceof JAMSStringArray) {
                ((JAMSStringArray) obj).setValue(((JAMSStringArray) data.dataAccessors.get(name).getComponentObject()).getValue());
            }else if (obj instanceof JAMSTimeInterval) {
                ((JAMSTimeInterval) obj).setValue(((JAMSTimeInterval) data.dataAccessors.get(name).getComponentObject()).getValue());
            }
        }
        //restore iterators
        if (restoreIterator)
            currentContext.setIteratorState(data.iteratorState);
    }

    //this is still something todo
    private void restoreEntityEnumerators(Context currentContext) {                
        //TODO: currentContext.reinitEnumerators();        
        for (int i = 0; i < currentContext.getComponents().size(); i++) {
            Component c = (Component) currentContext.getComponents().get(i);
            if (c instanceof Context) {
                restoreEntityEnumerators((Context) c);
            }
        }    
        //currentContext.debug("C:\\Arbeit\\TIMELOOP_post.dat" + counter2++);
    }

    public Snapshot getModelState(boolean holdInMemory, String fileName, Component position) {
        ByteArrayOutputStream outStream = new ByteArrayOutputStream();
        ObjectOutputStream objOut = null;

        HashMap<String, ContextSnapshotData> contextStates = new HashMap<String, ContextSnapshotData>();
        collectEntityCollections(this.getModel(), position, contextStates);

        JAMSSnapshot snapshot = null;
        try {
            objOut = new ObjectOutputStream(outStream);
            objOut.writeObject(contextStates);
            
            snapshot = new JAMSSnapshot(holdInMemory, outStream.toByteArray(), fileName);
            
            objOut.close();
            outStream.close();
        } catch (IOException e) {
            this.getRuntime().sendErrorMsg(JAMS.resources.getString("Unable_to_save_model_state_because,") + e.toString());
        }
                                
        return snapshot;
    }

    public void setModelState(Snapshot inData) {
        setModelState(inData,false);
    }
    @SuppressWarnings ("unchecked")
    public void setModelState(Snapshot inData, boolean restoreIterator) {
        HashMap<String, ContextSnapshotData> contextStates = null;
        try {
            ByteArrayInputStream inStream = new ByteArrayInputStream(inData.getData());
            ObjectInputStream objIn = new ObjectInputStream(inStream);            
            contextStates = (HashMap<String, ContextSnapshotData>) objIn.readObject();
            inStream.close();
            objIn.close();
        } catch (Exception e) {
            this.getRuntime().sendErrorMsg(JAMS.resources.getString("Unable_to_deserialize_jamsentity_collection,_because") + e.toString());
        }
        restoreEntityCollections(this.getModel(), contextStates, restoreIterator);
        restoreEntityEnumerators(this.getModel());
    }
}
