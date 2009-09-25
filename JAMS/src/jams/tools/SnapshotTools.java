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

import jams.data.JAMSData;
import jams.dataaccess.DataAccessor;
import jams.model.AttributeAccess;
import jams.model.Component;
import jams.model.Context;
import java.util.Iterator;
import java.util.Map.Entry;

/**
 *
 * @author Sven Kralisch <sven.kralisch at uni-jena.de>
 */
public class SnapshotTools {

    private static int componentAlreadyProcessed(Context context, Component position, Component component) {
        if (context.getInstanceName().equals(position.getInstanceName())) {
            return 0;
        }
        if (context == component) {
            return 1;
        }
        for (int j = 0; j < context.getComponents().size(); j++) {
            Component c = context.getComponents().get(j);
            if (c == position) {
                return 0;
            }
            if (c == component) {
                return 1;
            }
            if (c instanceof Context) {
                Context subContext = (Context) c;
                int result = componentAlreadyProcessed(subContext, position, component);
                if (result == 0 || result == 1) {
                    return result;
                }
            }
        }
        return 2;
    }

    private static int containsValidData(Context context, Component position, Object componentObj) {
        /* three steps:
         * 1. get name of attribute to componentObj in this context
         *    <context> attribName specifies attribute fully
         * 2. get name of components which use this attribute in write or read-write mode
         * 3. look one of these components has allready been executed
         *
         */

        //search for attribname
        Iterator<Entry<String, JAMSData>> iter = context.getAttributeMap().entrySet().iterator();
        String attribName = null;
        while (iter.hasNext()) {
            Entry<String, JAMSData> e = iter.next();
            if ((Object) e.getValue() == componentObj) {
                attribName = e.getKey();
                break;
            }
        }

        //search in accessSpecs for components which use this attrib
        Component component = null;
        if (attribName != null) {
            for (int i = 0; i < context.getAttributeAccessList().size(); i++) {

                AttributeAccess aAccess = context.getAttributeAccessList().get(i);
                
                if (aAccess.getAttributeName().compareTo(attribName) == 0) {
                    //do they write this attrib?
                    if (aAccess.getAccessType() != DataAccessor.READ_ACCESS) {
                        component = aAccess.getComponent();
                        //has component been executed
                        if (componentAlreadyProcessed(context, position, component) == 1) {
                            return 1;
                        }
                    }
                }
            }
        }
        //look if there is an child context which uses this componentObj
        for (int j = 0; j < context.getComponents().size(); j++) {
            Component c = context.getComponents().get(j);
            if (c.getInstanceName().equals(position.getInstanceName())) {
                return 0;
            }
            if (c instanceof Context) {
                if (containsValidData((Context) context.getComponents().get(j), position, componentObj) == 1) {
                    return 1;
                }
            }
        }
        return 2;
    }

    public static void updateEntityData(Context context, Component currentComponent) {
        //if this context has not been executed at all, exit
        if (componentAlreadyProcessed(context.getModel(), currentComponent, context) != 1) {
            return;
            //if this context is finished ..
        }
        if (!context.componentInContext(currentComponent)) {
            return;
        }

        DataAccessor[] dataAccessors = context.getDataAccessorMap().values().toArray(new DataAccessor[context.getDataAccessorMap().size()]);

        for (int i = 0; i < dataAccessors.length; i++) {
            //get pointer to component data
            Object componentObj = dataAccessors[i].getComponentObject();
            //look if component data is already up to date
            if (containsValidData(context, currentComponent, componentObj) == 1) {
                dataAccessors[i].write();
            }
        }
    }
}
