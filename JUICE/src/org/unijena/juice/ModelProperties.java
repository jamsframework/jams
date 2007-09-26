/*
 * ModelProperties.java
 * Created on 10. M�rz 2007, 12:50
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
import org.unijena.juice.tree.ContextAttribute;

/**
 *
 * @author Sven Kralisch
 */
public class ModelProperties {
    
    //private HashMap<String, ArrayList<ModelProperty>> properties = new HashMap<String, ArrayList<ModelProperty>>();
    private HashMap<String, Group> groups = new HashMap<String, Group>();
    private ArrayList<Group> groupList = new ArrayList<Group>();
    
    public boolean addProperty(Group group, ModelProperty p) {
        
        ArrayList<ModelProperty> properties = group.propertyList;
        
        if (properties.contains(p)) {
            return false;
        } else {
            properties.add(p);
            p.group = group;
            return true;
        }
    }
    
    public void removePropertyFromGroup(Group group, ModelProperty p) {
        group.propertyList.remove(p);
        p.group = null;
    }
    
    public void addPropertyToGroup(Group group, ModelProperty p) {
        group.propertyList.add(p);
        p.group = group;
    }

    public void addPropertyToGroup(Group group, ModelProperty p, int index) {
        group.propertyList.add(index, p);
    }
    
    public void removeGroup(Group group) {
        groups.remove(group.name);
        groupList.remove(group);
    }
    
    public boolean addGroup(String groupName) {
        if (groups.keySet().contains(groupName)) {
            return false;
        } else {
            Group group = new Group();
            group.name = groupName;
            groups.put(groupName, group);
            groupList.add(group);
            return true;
        }
    }
    
    public boolean insertGroup(Group group, int index) {
        if (index>=0 && index<groupList.size()) {
            groups.put(group.name, group);
            groupList.add(index, group);
            return true;
        } else {
            return false;
        }
    }
    
    public ModelProperty createProperty() {
        return new ModelProperty();
    }
    
    public ArrayList<Group> getGroupList() {
        return groupList;
    }
    
    public Group getGroup(String groupName) {
        return groups.get(groupName);
    }
    
    public Group getGroup(int index) {
        return groupList.get(index);
    }
    
    public HashMap<String, Group> getGroups() {
        return groups;
    }
    
    public boolean setGroupName(Group group, String name) {
        if (groups.keySet().contains(name)) {
            return false;
        } else {
            groups.remove(group.name);
            group.name = name;
            groups.put(group.name, group);
            return true;
        }
    }
    
    public String[] getGroupNames() {
        String[] result = new String[groupList.size()];
        int i = 0;
        for (Group group : groupList) {
            result[i++] = group.getName();
        }
        return result;
    }
    
    public class ModelProperty {
        public String defaultValue, value, description, name;
        public ComponentDescriptor component;
        public ComponentDescriptor.ComponentVar var;
        public ContextAttribute attribute;
        public double lowerBound, upperBound;
        private Group group;
        public Group getGroup() {
            return group;
        }
    }
    
    public class Group {
        private ArrayList<ModelProperty> propertyList = new ArrayList<ModelProperty>();
        private String name;
        public String getName() {
            return name;
        }
        public ArrayList<ModelProperty> getProperties() {
            return propertyList;
        }
    }
    
}
