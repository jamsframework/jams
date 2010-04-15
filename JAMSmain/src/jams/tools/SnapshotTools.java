/*
 * SnapshotTools.java
 * Created on 24. September 2009, 09:04
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
package jams.tools;

import jams.data.Attribute.EntityCollection;
import jams.data.Attribute.FloatArray;
import jams.data.JAMSBoolean;
import jams.data.JAMSBooleanArray;
import jams.data.JAMSCalendar;
import jams.data.JAMSData;
import jams.data.JAMSDouble;
import jams.data.JAMSDoubleArray;
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
import jams.data.SnapshotData;
import jams.dataaccess.DataAccessor;
import jams.model.Component;
import jams.model.Context;
import jams.workspace.stores.InputDataStore;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

/**
 *
 * @author Sven Kralisch <sven.kralisch at uni-jena.de>
 */
public class SnapshotTools {

    public static class ContextSnapshotData implements Serializable {

        public EntityCollection entities;
        public HashMap<String, DataAccessor> dataAccessors;
        public byte[] iteratorState;
    }

    public static class JAMSSnapshotData implements SnapshotData {

        private HashMap<String, ContextSnapshotData> contextStates;
        private HashMap<String, byte[]> dataStoreStates;

        public JAMSSnapshotData() {
            contextStates = new HashMap<String, ContextSnapshotData>();
            dataStoreStates = new HashMap<String, byte[]>();
        }

        public void addContextState(Context currentContext) {
            //currentContext.debug("C:\\Arbeit\\TIMELOOP_pre.dat" + counter1++);

            /*ContextSnapshotData data = new ContextSnapshotData();
            data.entities = currentContext.getEntities();
            data.dataAccessors = currentContext.getDataAccessorMap();
            data.iteratorState = currentContext.getIteratorState();
            contextStates.put(currentContext.getInstanceName(), data);

            for (int i = 0; i < currentContext.getComponents().size(); i++) {
                Component c = (Component) currentContext.getComponents().get(i);
                if (c instanceof Context) {
                    addContextState((Context) c);
                }
            }*/
        }

        public void addDataStoreState(InputDataStore store) throws IOException {
            /*ByteArrayOutputStream outStream = new ByteArrayOutputStream();
            ObjectOutputStream objStream = new ObjectOutputStream(outStream);
            store.getState(objStream);
            objStream.flush();
            outStream.flush();
            dataStoreStates.put(store.getID(), outStream.toByteArray());
            objStream.close();
            outStream.close();*/
        }

        public void getDataStoreState(InputDataStore store) throws IOException, ClassNotFoundException, Exception {
            /*if (!dataStoreStates.containsKey(store.getID())) {
                throw new Exception("unknown input data store:" + store.getID());
            }
            ByteArrayInputStream inStream = new ByteArrayInputStream(dataStoreStates.get(store.getID()));
            ObjectInputStream objStream = new ObjectInputStream(inStream);
            store.setState(objStream);
            inStream.close();*/
        }

        public void getContextState(Context currentContext, boolean restoreIterator) throws Exception {            
            //TODO: restore attributes which are not interconnected            
            
            ContextSnapshotData data = contextStates.get(currentContext.getInstanceName());
            if (data == null){
                throw new Exception("unknown context:" + currentContext.getInstanceName());
            }
            
            for (int i = 0; i < currentContext.getComponents().size(); i++) {
                Component c = (Component) currentContext.getComponents().get(i);
                if (c instanceof Context) {
                    getContextState((Context) c, restoreIterator);
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

                    if (obj == null) {
                        System.out.println("object:" + name + " value:" + obj_src + " in context:" + currentContext.getInstanceName() + " does not exist");
                        map.put(name, obj_src);
                        continue;
                    }
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
                    System.out.println("object:" + name + " in context:" + currentContext.getInstanceName() + " does not exist");
                    continue;
                }
                JAMSData obj = da.getComponentObject();
                if (obj instanceof JAMSBoolean) {
                    ((JAMSBoolean) obj).setValue(((JAMSBoolean) data.dataAccessors.get(name).getComponentObject()).getValue());
                } else if (obj instanceof JAMSBooleanArray) {
                    ((JAMSBooleanArray) obj).setValue(((JAMSBooleanArray) data.dataAccessors.get(name).getComponentObject()).getValue());
                } else if (obj instanceof JAMSCalendar) {
                    ((JAMSCalendar) obj).setValue(((JAMSCalendar) data.dataAccessors.get(name).getComponentObject()).getValue());
                } else if (obj instanceof JAMSDouble) {
                    ((JAMSDouble) obj).setValue(((JAMSDouble) data.dataAccessors.get(name).getComponentObject()).getValue());
                } else if (obj instanceof JAMSDoubleArray) {
                    ((JAMSDoubleArray) obj).setValue(((JAMSDoubleArray) data.dataAccessors.get(name).getComponentObject()).getValue());
                /*if (obj instanceof JAMSEntity)
                ((JAMSEntity)obj).setValue(((JAMSEntity)data.dataAccessors.get(name).getComponentObject()).getValue());
                if (obj instanceof JAMSEntityCollection)
                ((JAMSEntityCollection)obj).setValue(((JAMSEntityCollection)data.dataAccessors.get(name).getComponentObject()).getValue());*/
                } else if (obj instanceof JAMSFloat) {
                    ((JAMSFloat) obj).setValue(((JAMSFloat) data.dataAccessors.get(name).getComponentObject()).getValue());
                } else if (obj instanceof FloatArray) {
                    ((FloatArray) obj).setValue(((FloatArray) data.dataAccessors.get(name).getComponentObject()).getValue());
                } else if (obj instanceof JAMSGeometry) {
                    ((JAMSGeometry) obj).setValue(((JAMSGeometry) data.dataAccessors.get(name).getComponentObject()).getValue());
                } else if (obj instanceof JAMSInteger) {
                    ((JAMSInteger) obj).setValue(((JAMSInteger) data.dataAccessors.get(name).getComponentObject()).getValue());
                } else if (obj instanceof JAMSIntegerArray) {
                    ((JAMSIntegerArray) obj).setValue(((JAMSIntegerArray) data.dataAccessors.get(name).getComponentObject()).getValue());
                } else if (obj instanceof JAMSLong) {
                    ((JAMSLong) obj).setValue(((JAMSLong) data.dataAccessors.get(name).getComponentObject()).getValue());
                } else if (obj instanceof JAMSLongArray) {
                    ((JAMSLongArray) obj).setValue(((JAMSLongArray) data.dataAccessors.get(name).getComponentObject()).getValue());
                } else if (obj instanceof JAMSObject) {
                    ((JAMSObject) obj).setValue(((JAMSObject) data.dataAccessors.get(name).getComponentObject()).getValue());
                } else if (obj instanceof JAMSString) {
                    ((JAMSString) obj).setValue(((JAMSString) data.dataAccessors.get(name).getComponentObject()).getValue());
                } else if (obj instanceof JAMSStringArray) {
                    ((JAMSStringArray) obj).setValue(((JAMSStringArray) data.dataAccessors.get(name).getComponentObject()).getValue());
                } else if (obj instanceof JAMSTimeInterval) {
                    ((JAMSTimeInterval) obj).setValue(((JAMSTimeInterval) data.dataAccessors.get(name).getComponentObject()).getValue());
                }
            }
            //restore iterators
/*            if (restoreIterator) {
                currentContext.setIteratorState(data.iteratorState);
            }*/
        }
    }
}
