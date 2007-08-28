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

package org.unijena.juice;

import java.util.ArrayList;
import java.util.HashMap;
import org.unijena.juice.tree.ComponentDescriptor;

/**
 *
 * @author S. Kralisch
 */
public class DataRepository {
    
    private HashMap<String, Attribute> attributesByName = new HashMap<String, Attribute>();
    private HashMap<Class, ArrayList<Attribute>> attributesByType = new HashMap<Class, ArrayList<Attribute>>();
    
    /** Creates a new instance of DataRepository */
    public DataRepository() {
    }
    
    public void addAttribute(String name, Class type) {
        Attribute a = new Attribute(name, type);
        attributesByName.put(name, a);
        
        ArrayList<Attribute> attributes = attributesByType.get(type);
        if (attributes == null) {
            attributes = new ArrayList<Attribute>();
            attributesByType.put(type, attributes);
        }
        
        attributes.add(a);
    }
    
    public void removeAttribute(Attribute attribute) {
        attributesByName.remove(attribute.name);
    }
    
    public HashMap<String, Attribute> getAttributesByName() {
        return attributesByName;
    }
    
    public ArrayList<Attribute> getAttributesByType(Class type) {
        return attributesByType.get(type);
    }
    
    
    public class Attribute {
        
        public String name;
        public Class type;
        
        public Attribute(String name, Class type) {
            this.name = name;
            this.type = type;
        }
        
        public String toString() {
            return name;
        }
    }
    
}
