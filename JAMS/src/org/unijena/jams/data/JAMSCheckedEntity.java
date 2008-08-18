/*
 * JAMSCheckedEntity.java
 * Created on 24. November 2005, 07:35
 *
 * This file is part of JAMS
 * Copyright (C) 2005 FSU Jena
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
package org.unijena.jams.data;

import java.util.*;
import com.vividsolutions.jts.geom.Geometry;
import java.io.Serializable;

/**
 *
 * @author S. Kralisch
 */
class JAMSCheckedEntity implements JAMSEntity, Serializable {

    private HashMap<String, Object> values = new HashMap<String, Object>();

    public void setFloat(String name, float attribute) {
        JAMSFloat v = (JAMSFloat) this.values.get(name);
        try {
            v.setValue(attribute);
        } catch (NullPointerException npe) {
            this.values.put(name, new JAMSFloat(attribute));
        }
    }

    public void setDouble(String name, double attribute) {
        JAMSDouble v = (JAMSDouble) this.values.get(name);
        try {
            v.setValue(attribute);
        } catch (NullPointerException npe) {
            this.values.put(name, new JAMSDouble(attribute));
        }
    }

    public void setInt(String name, int attribute) {
        JAMSInteger v = (JAMSInteger) this.values.get(name);
        try {
            v.setValue(attribute);
        } catch (NullPointerException npe) {
            this.values.put(name, new JAMSInteger(attribute));
        }
    }

    public void setLong(String name, long attribute) {
        JAMSLong v = (JAMSLong) this.values.get(name);
        try {
            v.setValue(attribute);
        } catch (NullPointerException npe) {
            this.values.put(name, new JAMSLong(attribute));
        }
    }

    public void setObject(String name, Object attribute) {
        this.values.put(name, attribute);
    }

    public void setGeometry(String name, Geometry attribute) {
        JAMSGeometry v = (JAMSGeometry) this.values.get(name);
        try {
            v.setValue(attribute);
        } catch (NullPointerException npe) {
            this.values.put(name, new JAMSGeometry(attribute));
        }
    }

    public float getFloat(String name) throws JAMSEntity.NoSuchAttributeException {
        if (values.containsKey(name)) {
            return ((JAMSFloat) values.get(name)).getValue();
        } else {
            throw new JAMSEntity.NoSuchAttributeException("Attribute " + name + " (float) not found!");
        }
    }

    public double getDouble(String name) throws JAMSEntity.NoSuchAttributeException {
        if (values.containsKey(name)) {
            return ((JAMSDouble) values.get(name)).getValue();
        } else {
            throw new JAMSEntity.NoSuchAttributeException("Attribute " + name + " (double) not found!");
        }
    }

    public int getInt(String name) throws JAMSEntity.NoSuchAttributeException {
        if (values.containsKey(name)) {
            return ((JAMSInteger) values.get(name)).getValue();
        } else {
            throw new JAMSEntity.NoSuchAttributeException("Attribute " + name + " (int) not found!");
        }
    }

    public long getLong(String name) throws JAMSEntity.NoSuchAttributeException {
        if (values.containsKey(name)) {
            return ((JAMSLong) values.get(name)).getValue();
        } else {
            throw new JAMSEntity.NoSuchAttributeException("Attribute " + name + " (long) not found!");
        }
    }

    public Object getObject(String name) throws JAMSEntity.NoSuchAttributeException {
        if (values.containsKey(name)) {
            return values.get(name);
        } else {
            throw new JAMSEntity.NoSuchAttributeException("Attribute " + name + " (Object) not found!");
        }
    }

    public Geometry getGeometry(String name) throws JAMSEntity.NoSuchAttributeException {
        if (values.containsKey(name)) {
            return ((JAMSGeometry) values.get(name)).getValue();
        } else {
            throw new JAMSEntity.NoSuchAttributeException("Attribute " + name + " (Geometry) not found!");
        }
    }

    public boolean existsAttribute(String name) {
        try{
        if (values.containsKey(name)) {
            return true;
        } else {
            return false;
        }}catch(NullPointerException e){
            System.out.print("test");
            return false;
        }
    }

    public Object[] getKeys() {
        return this.values.keySet().toArray(new Object[values.size()]);
    }

    //that's crap
    public void setValue(String value) {
        StringTokenizer st1 = new StringTokenizer(value, "\t");
        while (st1.hasMoreTokens()) {
            StringTokenizer st2 = new StringTokenizer(st1.nextToken(), "=");
            String name = st2.nextToken().trim();
            String val = st2.nextToken().trim();
            try {
                JAMSDouble d = new JAMSDouble(Double.parseDouble(val));
                values.put(name, d);
            } catch (NumberFormatException nfe) {
                System.out.println("\"" + val + "\" is not a valid double expression!");
                nfe.printStackTrace();
            }
        }
    }

    public void setValue(HashMap<String, Object> values) {
        this.values = values;
    }

    public HashMap<String, Object> getValue() {
        return values;
    }

    public String getStringValue() {
        String result = "";
        Object[] names = values.keySet().toArray();
        if (names.length > 0) {
            result += names[0] + "=" + values.get(names[0]) + "f";
        }
        for (int i = 1; i < names.length; i++) {
            result += "\t" + names[i] + "=" + values.get(names[i]) + "f";
        }
        return result;
    }
}
