/*
 * ComponentCollection.java
 * Created on 25.06.2010, 09:19:58
 *
 * This file is part of JAMS
 * Copyright (C) FSU Jena
 *
 * JAMS is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public License
 * as published by the Free Software Foundation; either version 3
 * of the License, or (at your option) any later version.
 *
 * JAMS is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with JAMS. If not, see <http://www.gnu.org/licenses/>.
 *
 */
package jams.meta;

import jams.JAMS;
import jams.JAMSLogging;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.logging.Logger;

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
        if (cd.equals(componentDescriptors.get(oldName))) {
            componentDescriptors.remove(oldName);
        }
        componentDescriptors.put(newNewName, cd);
        
        if (!newName.equals(newNewName)) {
            JAMSLogging.registerLogger(Logger.getLogger(this.getClass().getName()));
            Logger.getLogger(this.getClass().getName()).fine(MessageFormat.format(JAMS.i18n("Component_name_is_already_in_use._Renamed_component_to_"), newName, newNewName));
        }        
        
        return newNewName;
    }

    public void unRegisterComponentDescriptor(ComponentDescriptor cd) {
        componentDescriptors.remove(cd.getInstanceName());
    }

    public String createComponentInstanceName(String name) {
        return name;
    }

    public HashMap<String, ComponentDescriptor> getComponentDescriptors() {
        return componentDescriptors;
    }

    public ComponentDescriptor getComponentDescriptor(String name) {
        return componentDescriptors.get(name);
    }
}
