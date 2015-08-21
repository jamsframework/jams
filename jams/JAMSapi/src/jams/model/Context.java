/*
 * Context.java
 * Created on 5. November 2009, 16:25
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

package jams.model;

import jams.data.Attribute;
import jams.dataaccess.DataAccessor;
import jams.data.JAMSData;
import java.util.ArrayList;
import java.util.HashMap;

/**
 *
 * @author Sven Kralisch <sven.kralisch at uni-jena.de>
 */
public interface Context extends Component {

    /**
     * Registers a new accessor managed by this context
     * @param user The components that wants to have access
     * @param varName The name of the components member which is connected
     * @param attributeName The name of the attribute within this context
     * @param accessType The permission type (DataAccessor.READ_ACCESS,
     * DataAccessor.WRITE_ACCESS or DataAccessor.READWRITE_ACCESS)
     */
    void addAccess(Component user, String varName, String attributeName, int accessType);

    /**
     * Registers a new attribute object for this context
     * @param attributeName The name of the attribute
     * @param clazz The type of the attribute
     * @param value The value of the attribute
     */
    void addAttribute(String attributeName, String clazz, String value);

    /**
     * Add a single component to this context
     * @param c The component to be added
     */
    void addComponent(Component c);

    /**
     * Get a map containing
     * @return
     */
    HashMap<String, JAMSData> getAttributeMap();

    /**
     *
     * @return
     */
    ArrayList<AttributeAccess> getAttributeAccessList();

    /**
     * Get a child component of this context by its name
     * @param name The components name
     * @return The component
     */
    Component getComponent(String name);

    /**
     *
     * @return All child components as ArrayList
     */
    ArrayList<Component> getComponents();

    HashMap<String, DataAccessor> getDataAccessorMap();
    
    Attribute.EntityCollection getEntities();

    long getNumberOfIterations();

    long getRunCount();

    /**
     *
     * @return A string representing the current state of the context
     */
    String getTraceMark();

    /**
     * Iniatialization of all objects that are needed to manage the data
     * exchange between descendent components. Needs to be called once at the
     * beginning of the init stage before calling the init() methods of child
     * components.
     */
    void initAccessors();

    /**
     * Remove a single component from this context
     * @param index The index of the component to be removed
     */
    void removeComponent(int index);

    /**
     *
     * @param components Set the child components of this context and set this
     * object as their context
     */
    void setComponents(ArrayList<Component> components);

    void setEntities(Attribute.EntityCollection entities);

    void setupDataTracer();
    
    void updateEntityData();

    void updateComponentData(int index);
    
    void resume();            
    
    void setExecutionState(int state);
}
