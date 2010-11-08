/*
 * ContextAttribute.java
 * Created on 4. März 2007, 18:58
 *
 * This file is part of JAMS
 * Copyright (C) 2007 FSU Jena
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

package jams.meta;

import java.util.HashMap;
import jams.JAMS;
import jams.JAMSException;
import java.util.HashSet;

/**
 * This class represents attributes of a context, which can be static attributes
 * (user defined) or dynamic attributes (defined by other components)
 * @author Sven Kralisch
 */
public class ContextAttribute {
    private String name = "";
    private String value = "";
    private Class type = null;
    private ContextDescriptor context;
    private HashSet<ComponentField> fields = new HashSet<ComponentField>();
    
    public ContextAttribute(String name, Class type, ContextDescriptor context) {
        this.name = name;
        this.type = type;
        this.context = context;
    }
    
    private void renameContextAttribute(String newName) throws JAMSException {
        
        HashMap<String, ContextAttribute> attributes = getContext().getDynamicAttributes();
        
        if (attributes.get(newName) != null) {
            throw new JAMSException(JAMS.resources.getString("Context_attribute_") + newName + JAMS.resources.getString("_does_already_exist._") +
                    JAMS.resources.getString("Please_remove_or_chose_a_different_name!"), JAMS.resources.getString("Error_renaming_context_attribute"));
        } else {
            attributes.remove(this.name);
            this.name = newName;
            attributes.put(newName, this);
        }
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) throws JAMSException {
        if (!this.name.equals(name)) {
            renameContextAttribute(name);
        }
    }
    
    public String getValue() {
        return value;
    }
    
    public void setValue(String value) {
        this.value = value;
    }
    
    public Class getType() {
        return type;
    }
    
    public void setType(Class type) {
        this.type = type;
    }
    
    public ContextDescriptor getContext() {
        return context;
    }

    @Override
    public String toString() {
        return name;
    }

    /**
     * @return the fields
     */
    public HashSet<ComponentField> getFields() {
        return fields;
    }
}
