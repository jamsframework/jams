/*
 * DefaultDataSetDefinition.java
 * Created on 24. Januar 2008, 08:53
 *
 * This file is part of JAMS
 * Copyright (C) FSU Jena
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
package jams.workspace;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;
import java.util.Set;
import jams.JAMS;
import java.io.Serializable;

/**
 *
 * @author Sven Kralisch
 */
public class DefaultDataSetDefinition implements DataSetDefinition, Serializable {

    private int columnCount;

    private ArrayList<Class> dataTypes;

    private ArrayList<String> attributeNames = new ArrayList<String>();

    private HashMap<String, Class> attributes = new HashMap<String, Class>();

    private HashMap<String, ArrayList<Object>> attributeValues = new HashMap<String, ArrayList<Object>>();

    public DefaultDataSetDefinition(ArrayList<Class> dataTypes) {
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

    public boolean setAttributeValues(String attributeName, ArrayList<Object> values) {

        //check if size matches
        if (values.size() != columnCount) {
            return false;
        }

        //check if attribute exists
        if (!attributes.containsKey(attributeName)) {
            return false;
        }

        //check if provided values are compatible to attribute types
        Class<?> type = attributes.get(attributeName);
        for (int i = 0; i < values.size(); i++) {
            if (!type.isAssignableFrom(values.get(i).getClass())) {
                System.out.println(JAMS.i18n("Invalid_type_in_dataset_definition:_") + values.get(i).getClass() + "<->" + type +
                        " (" + attributeName + ")");
                return false;
            }
        }

        attributeValues.put(attributeName, values);
        return true;
    }

    public boolean setAttributeValues(String attributeName, Object value) {

        ArrayList<Object> values = new ArrayList<Object>();
        for (int i = 0; i < columnCount; i++) {
            values.add(value);
        }
        return setAttributeValues(attributeName, values);
    }

    public boolean setAttributeValues(int column, ArrayList<Object> values) {

        //check if column exists
        if ((column < 0) || (column >= columnCount)) {
            return false;
        }

//        for (int i = 0; i < attributeNames.size(); i++) {
        int i = 0;
        for (String attributeName : attributeNames) {

            //check if size matches
            if (i >= values.size()) {
                return false;
            }

            //check if provided values are compatible to attribute types
            Class<?> type = attributes.get(attributeName);

            if (!type.isAssignableFrom(values.get(i).getClass())) {
                System.out.println(JAMS.i18n("Invalid_type_in_dataset_definition:_") + values.get(i).getClass() + "<->" + type +
                        " (" + attributeName + ")");
                return false;
            }

            ArrayList<Object> valueRow = attributeValues.get(attributeName);
            valueRow.set(column, values.get(i));

            i++;
        }

        return true;
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

        result += TYPE_ID;
        for (Class type : dataTypes) {
            result += "\t" + type.getName();
        }
        result += "\n";

        for (String attributeName : attributeNames) {

            result += "#" + attributeName;

            result += "\t" + attributes.get(attributeName).getName();

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

        DefaultDataSetDefinition def = new DefaultDataSetDefinition(dataTypes);

        def.addAttribute("ID", Long.class);
        def.addAttribute("NAME", String.class);
        def.addAttribute("LAT", Double.class);
        def.addAttribute("LONG", Double.class);
        def.addAttribute("HEIGHT", Double.class);

        ArrayList<Object> values = new ArrayList<Object>();
        values.add(new Long(0));
        values.add(new Long(1));
        values.add(new Long(2));
        values.add(new Long(12));
        def.setAttributeValues("ID", values);

        values = new ArrayList<Object>();
        values.add("Tmean");
        values.add("Tmin");
        values.add("Tmax");
        values.add("Precip");
        def.setAttributeValues("NAME", values);

        Random r = new Random(42);
        values = new ArrayList<Object>();
        for (int i = 0; i < 4; i++) {
            values.add(100000 * new Double(r.nextDouble()));
        }
        def.setAttributeValues("LAT", values);
        values = new ArrayList<Object>();
        for (int i = 0; i < 4; i++) {
            values.add(100000 * new Double(r.nextDouble()));
        }
        def.setAttributeValues("LONG", values);
        values = new ArrayList<Object>();
        for (int i = 0; i < 4; i++) {
            values.add(100 * new Double(r.nextDouble()));
        }
        def.setAttributeValues("HEIGHT", values);

        values = new ArrayList<Object>();
        values.add(new Long(42));
        values.add("Vmax");
        values.add(new Double(42000));
        values.add(new Double(84000));
        values.add(new Double(-1));
        def.setAttributeValues(0, values);

        System.out.println(def.toASCIIString());
    }

    public ArrayList<String> getAttributeNames() {
        return attributeNames;
    }
}

