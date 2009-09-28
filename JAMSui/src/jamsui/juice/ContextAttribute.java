/*
 * ModelAttribute.java
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

package jamsui.juice;

import java.util.HashMap;
import jams.gui.tools.GUIHelper;

/**
 *
 * @author Sven Kralisch
 */
public class ContextAttribute {
    private String name = "";
    private String value = "";
    private Class type = null;
    private ComponentDescriptor context;
//    public ComponentDescriptor component;
    
    
    public ContextAttribute(String name, Class type, ComponentDescriptor context) {
        this.name = name;
        this.type = type;
        this.context = context;
//        this.component = component;
    }
    
    private void renameContextAttribute(String newName) {
        
        HashMap<String, ContextAttribute> attributes = getContext().getContextAttributes();
        
        if (attributes.get(newName) != null) {
            GUIHelper.showErrorDlg(JUICE.getJuiceFrame(), JUICE.resources.getString("Context_attribute_") + newName + JUICE.resources.getString("_does_already_exist._") +
                    JUICE.resources.getString("Please_remove_or_chose_a_different_name!"), JUICE.resources.getString("Error_renaming_context_attribute"));
        } else {
            attributes.remove(this.name);
            this.name = newName;
            attributes.put(newName, this);
        }
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
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
    
    public ComponentDescriptor getContext() {
        return context;
    }

    @Override
    public String toString() {
        return name;
    }
        
}
