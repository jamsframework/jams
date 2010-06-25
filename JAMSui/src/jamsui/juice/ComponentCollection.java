/*
 * Context.java
 * Created on 25.06.2010, 09:19:58
 *
 * This file is part of JAMS
 * Copyright (C) FSU Jena
 *
 * JAMS is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * JAMS is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with JAMS. If not, see <http://www.gnu.org/licenses/>.
 *
 */
package jamsui.juice;

import jams.tools.StringTools;
import java.util.HashMap;
import java.util.Set;

/**
 * This class represents a collection of components which can either be
 * managed by component repository or a model
 * @author Sven Kralisch <sven.kralisch at uni-jena.de>
 */
public class ComponentCollection {

    private HashMap<String, ComponentDescriptor> componentDescriptors;

    public ComponentCollection() {
        componentDescriptors = new HashMap<String, ComponentDescriptor>();
    }

    public String registerComponentDescriptor(String oldName, String newName, ComponentDescriptor cd) {

        String newNewName = createComponentInstanceName(newName);
        getComponentDescriptors().remove(oldName);
        getComponentDescriptors().put(newNewName, cd);

        return newNewName;
    }

    public void unRegisterComponentDescriptor(ComponentDescriptor cd) {
        getComponentDescriptors().remove(cd.getName());
    }

    /*
     * Create a new name for a component instance.
     * Use the given name.
     */
    public String createComponentInstanceName(String name) {
        return name;
    }

    public HashMap<String, ComponentDescriptor> getComponentDescriptors() {
        return componentDescriptors;
    }

    public ComponentDescriptor getComponentDescriptor(String name) {
        return getComponentDescriptors().get(name);
    }

}
