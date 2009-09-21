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
package jams.model;

import jams.data.JAMSEntityCollection;
import jams.dataaccess.DataAccessor;
import java.util.ArrayList;
import java.util.HashMap;


/**
 *
 * @author S. Kralisch
 */

@JAMSComponentDescription (title = "JAMS Component",
                           author = "Sven Kralisch",
                           date = "27. Juni 2005",
                           description = "This component represents a JAMS context which is the top level " +
"component of every component hierarchie in JAMS")
public interface JAMSContext extends JAMSComponent {

    /**
     * Change the positions of two components
     * @param i The position of the first component
     * @param j The position of the second component
     */
    public void exchange(int i, int j);

    /**
     * Add a single component to this context
     * @param c The component to be added
     */
    public void addComponent(JAMSComponent c);

    /**
     * Remove a single component from this context
     * @param index The index of the component to be removed
     */
    public void removeComponent(int index);

    /**
     * 
     * @return All child components as ArrayList
     */
    public ArrayList getComponents();

    /**
     * 
     * @param components Set the child components of this context and set this
     * object as their context
     */
    public void setComponents(ArrayList<JAMSComponent> components);

    /**
     * 
     * @return An enumerator iterating over all child components depending on
     * this contexts functionality
     */
    public ComponentEnumerator getRunEnumerator();

    /**
     * 
     * @return An enumerator iterating once over all child components
     */
    public ComponentEnumerator getChildrenEnumerator();

    /**
     * 
     * @return All child components as array
     */
    public JAMSComponent[] getCompArray();

    /**
     * Registers a new accessor managed by this context
     * @param user The components that wants to have access
     * @param varName The name of the components member which is connected
     * @param attributeName The name of the attribute within this context
     * @param accessType The permission type (DataAccessor.READ_ACCESS, 
     * DataAccessor.WRITE_ACCESS or DataAccessor.READWRITE_ACCESS)
     */
    public void addAccess(JAMSComponent user, String varName, String attributeName, int accessType);

    /**
     * Registers a new attribute object for this context
     * @param attributeName The name of the attribute
     * @param clazz The type of the attribute
     * @param value The value of the attribute
     */
    public void addAttribute(String attributeName, String clazz, String value);

    /**
     * Iniatialization of all objects that are needed to manage the data 
     * exchange between descendent components. Needs to be called once at the 
     * beginning of the init stage before calling the init() methods of child 
     * components.
     */
    public void initAccessors();

    /**
     * 
     * @return A string representing the current state of the context
     */
    public String getTraceMark();;

    public HashMap<String, DataAccessor> getDaHash();

    public JAMSComponent getComponent(String name);

    public long getNumberOfIterations();

    public JAMSEntityCollection getEntities();

    public void setEntities(JAMSEntityCollection entities);

}
