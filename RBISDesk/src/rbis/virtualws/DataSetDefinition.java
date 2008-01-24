/*
 * DataSetDefinition.java
 * Created on 24. Januar 2008, 08:53
 *
 * This file is part of JAMS
 * Copyright (C) FSU Jena
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
package rbis.virtualws;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;

public class DataSetDefinition {

    private int columnCount;
    private ArrayList<Class> dataTypes;
    private ArrayList<String> attributeNames = new ArrayList<String>();
    private HashMap<String, Class> attributes = new HashMap<String, Class>();
    private HashMap<String, ArrayList<Object>> attributeValues = new HashMap<String, ArrayList<Object>>();

    public DataSetDefinition(ArrayList<Class> dataTypes) {
        this.dataTypes = dataTypes;
        columnCount = dataTypes.size();
    }

    public Class getType(String attributeName) {
        return attributes.get(attributeName);
    }

    public Object getAttributeValue(String attributeName, int column) {
        ArrayList<Object> values = attributeValues.get(attributeName);
        return values.get(column);
    }

    public ArrayList<Object> getAttributeValues(String attributeName) {
        return attributeValues.get(attributeName);
    }

    public ArrayList<Object> getAttributeValues(int column) {

        ArrayList<Object> list = new ArrayList<Object>();
        for (String attributeName : attributeNames) {
            list.add(getAttributeValue(attributeName, column));
        }

        return list;
    }

    public void setAttributeValues(String attributeName, ArrayList<Object> values) {
        attributeValues.put(attributeName, values);
    }

    public void removeAttribute(String attributeName) {
        attributeNames.remove(attributeName);
        attributes.remove(attributeName);
        attributeValues.remove(attributeName);
    }

    public void addAttribute(String attributeName, Class type) {
        attributeNames.add(attributeName);
        attributes.put(attributeName, type);
        ArrayList<Object> values = new ArrayList<Object>();
        for (int i = 0; i < getColumnCount(); i++) {
            values.add(null);
        }
        attributeValues.put(attributeName, values);
    }

    public Set<String> getAttributes() {
        return attributes.keySet();
    }

    public int getColumnCount() {
        return columnCount;
    }

    public String toASCIIString() {
        String result = "";

        for (String attributeName : attributeNames) {

            result += "#" + attributeName;

            ArrayList<Object> values = getAttributeValues(attributeName);
            for (Object value : values) {
                result += "\t" + value;
            }

            result += "\n";
        }

        return result;
    }

    public static void main(String[] args) {

        ArrayList<Class> dataTypes = new ArrayList<Class>();
        dataTypes.add(Double.class);
        dataTypes.add(Long.class);
        dataTypes.add(String.class);
        dataTypes.add(Object.class);

        DataSetDefinition def = new DataSetDefinition(dataTypes);

        def.addAttribute("ID", Long.class);
        def.addAttribute("NAME", String.class);
        def.addAttribute("LAT", Double.class);
        def.addAttribute("LONG", Double.class);
        def.addAttribute("HEIGHT", Double.class);

        System.out.println(def.toASCIIString());
    }
}

