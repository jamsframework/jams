/*
 * DataRepository.java
 * Created on 4. Januar 2007, 14:22
 *
 * This file is part of JAMS
 * Copyright (C) 2006 FSU Jena
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

package jams.juice;

import jamsui.juice.ComponentDescriptor;
import jamsui.juice.ContextAttribute;
import java.util.ArrayList;
import java.util.HashMap;

/**
 *
 * @author S. Kralisch
 */
public class AttributeRepository {
    
    //private HashMap<String, ArrayList<Attribute>> attributesByName = new HashMap<String, ArrayList<Attribute>>();
    private HashMap<Class, ArrayList<ContextAttribute>> attributesByType = new HashMap<Class, ArrayList<ContextAttribute>>();
    private ComponentDescriptor context;
    
    public AttributeRepository(ComponentDescriptor context) {
        this.context = context;
    }
    
    public void addAttribute(ContextAttribute attribute) {        
        ArrayList<ContextAttribute> attributes = attributesByType.get(attribute.getType());
        if (attributes == null) {
            attributes = new ArrayList<ContextAttribute>();
            attributesByType.put(attribute.getType(), attributes);
        }
        attributes.add(attribute);
        
    }
    
    public void removeAttribute(ContextAttribute attribute) {
        ArrayList<ContextAttribute> aList = attributesByType.get(attribute.getType());
        aList.remove(attribute);
//        aList = attributesByName.get(attribute.name);
//        aList.remove(attribute);
    }
    
    public ArrayList<ContextAttribute> getUniqueAttributesByType(Class type) {
        ArrayList<ContextAttribute> aList =  getAttributesByType(type);
        
        if (aList == null) {
            return null;
        }
        
        HashMap<String, ContextAttribute> map = new HashMap<String, ContextAttribute>();
        for (ContextAttribute a : aList) {
            map.put(a.getName(), a);
        }
        ArrayList<ContextAttribute> result = new ArrayList<ContextAttribute>();
        for (ContextAttribute a : map.values()) {
            result.add(a);
        }
        return result;
    }
    
    public ArrayList<ContextAttribute> getAttributesByType(Class<?> type) {
        ArrayList<ContextAttribute> result = new ArrayList<ContextAttribute>();
        for (Class<?> subType : attributesByType.keySet()) {
            if (type.isAssignableFrom(subType)) {
                result.addAll(attributesByType.get(subType));
            }
        }
//        return attributesByType.get(type);
        return result;
    }

    public ContextAttribute getAttributeByTypeName(Class type, String name) {
        ArrayList<ContextAttribute> attrs = getAttributesByType(type);
        for (ContextAttribute attr : attrs) {
            if (attr.getName().equals(name)) {
                return attr;
            }
        }
        return null;
    }
}
