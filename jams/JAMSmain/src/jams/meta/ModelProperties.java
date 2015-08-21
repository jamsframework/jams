/*
 * ModelProperties.java
 * Created on 10. March 2007, 12:50
 *
 * This file is part of JAMS
 * Copyright (C) 2006 FSU Jena
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
package jams.meta;

import java.util.ArrayList;
import java.util.HashMap;
import jams.tools.StringTools;

/**
 *
 * @author Sven Kralisch
 */
public class ModelProperties {

    //private HashMap<String, ArrayList<ModelProperty>> properties = new HashMap<String, ArrayList<ModelProperty>>();
    private HashMap<String, Group> groups = new HashMap<String, Group>();
    private ArrayList<Group> groupList = new ArrayList<Group>();
    
    // this is pretty ugly, change it
    private static String groupSeparator = Character.toString((char)187); 

    public boolean addProperty(Group group, ModelProperty p) {

        ArrayList<Object> properties = group.propertyList;

        if (properties.contains(p)) {
            return false;
        } else {
            properties.add(p);
            p.setGroup(group);
            return true;
        }
    }
    
    public void removeAll() {
        groups = new HashMap<String, Group>();
        groupList = new ArrayList<Group>();
    }

    public void removePropertyFromGroup(Group group, ModelElement p) {
        group.propertyList.remove(p);
        p.group = null;
    }

    public void addPropertyToGroup(Group group, ModelElement p) {
        group.propertyList.add(p);
        p.group = group;
    }

    public void addPropertyToGroup(Group group, ModelElement p, int index) {
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
        if (index >= 0 && index <= groupList.size()) {
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

    public Group createSubgroup(Group theGroup, String theSubgroupName) {
        Group subgroup = new Group();
        subgroup.setSubGroup(true);
        subgroup.setName(theSubgroupName);
        this.addPropertyToGroup(theGroup, subgroup);

        return subgroup;
    }

    public ArrayList<Group> getGroupList() {
        return groupList;
    }

    /**
     * get group by name
     * name could be groupName-subgroupName
     * in this case get subgroup
     * @param groupName
     * @return group or subgroup
     */
    public Group getGroup(String groupName) {
        if (groupName.indexOf(ModelProperties.groupSeparator) > 0) {
            String[] groupNames = StringTools.toArray(groupName, ModelProperties.groupSeparator);
            return getGroup(groupNames[0], groupNames[1]);
        } else {
            return groups.get(groupName);
        }
    }

    /**
     * 
     * @param groupName
     * @param subgroupName
     * @return
     */
    public Group getGroup(String groupName, String subgroupName) {
        Group group = getGroup(groupName);
        if (StringTools.isEmptyString(subgroupName)) {
            return group;
        }
        for (Object modelElement : group.propertyList) {
            if (modelElement instanceof Group) {
                Group subgroup = (Group) modelElement;
                if (subgroup.name.equals(subgroupName)) {
                    return (subgroup);
                }
            }
        }
        return null;
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

    /**
     * get all group names inclusive subgroups
     * @return array of all groupName[.subGroupName]
     */
    public String[] getAllGroupNames() {

        ArrayList<String> rv = new ArrayList<String>();

        for (Group group : groupList) {
            rv.add(group.getName());
            for (Object modelElement : group.propertyList) {
                if (modelElement instanceof Group) {
                    rv.add(((Group) modelElement).getName());
                }
            }
        }

        String result[] = new String[rv.size()];
        int i = 0;
        for (Object item : rv) {
            result[i++] = (String) item;
        }

        return result;
    }

    public class ModelElement {

        public String name;
        private Group group;
        private HelpComponent helpComponent;

        public ModelElement() {
            this.helpComponent = new HelpComponent();
        }
        

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public Group getGroup() {
            return group;
        }

        public Group getMainGroup() {
            Group theGroup = getGroup();
            if (theGroup.isSubGroup()) {
                return theGroup.getGroup();
            } else {
                return theGroup;
            }
        }

        public void setGroup(Group group) {
            this.group = group;
        }

        public HelpComponent getHelpComponent() {
            return helpComponent;
        }

        public void setHelpComponent(HelpComponent helpComponent) {
            this.helpComponent = helpComponent;
        }
    
    }

    public class ModelProperty extends ModelElement {

        public String description;
        public ComponentDescriptor component;
        public ComponentField var;
        public ContextAttribute attribute;
        public double lowerBound,  upperBound;
        public int length;

        public ModelProperty() {
            super();
        }
        
    }

    public class Group extends ModelElement {

        private ArrayList<Object> propertyList = new ArrayList<Object>();
        private boolean subGroup = false;

        public Group() {
            super();
        }

        public ArrayList<Object> getProperties() {
            return propertyList;
        }

        public boolean isSubGroup() {
            return subGroup;
        }

        public void setSubGroup(boolean isSubGroup) {
            this.subGroup = isSubGroup;
        }

        @Override
        public String getName() {
            if (isSubGroup()) {
                return getGroup().getName() + groupSeparator + name;
            } else {
                return name;
            }
        }

        public String getCanonicalName() {
            return name;
        }
    }
}
