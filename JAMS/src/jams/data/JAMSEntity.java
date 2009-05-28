/*
 * JAMSEntity.java
 * Created on 2. August 2005, 21:04
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
package jams.data;

import java.util.*;
import com.vividsolutions.jts.geom.Geometry;
import jams.JAMS;

/**
 *
 * @author S. Kralisch
 */
public class JAMSEntity implements Attribute.Entity {

    private HashMap<String, Object> values = new HashMap<String, Object>();

    private long id = -1;

    @Override
    public void setFloat(String name, float attribute) {
        JAMSFloat v = (JAMSFloat) this.values.get(name);
        try {
            v.setValue(attribute);
        } catch (NullPointerException npe) {
            this.values.put(name, new JAMSFloat(attribute));
        }
    }

    @Override
    public void setDouble(String name, double attribute) {
        JAMSDouble v = (JAMSDouble) this.values.get(name);
        try {
            v.setValue(attribute);
        } catch (NullPointerException npe) {
            this.values.put(name, new JAMSDouble(attribute));
        }
    }

    @Override
    public void setInt(String name, int attribute) {
        JAMSInteger v = (JAMSInteger) this.values.get(name);
        try {
            v.setValue(attribute);
        } catch (NullPointerException npe) {
            this.values.put(name, new JAMSInteger(attribute));
        }
    }

    @Override
    public void setLong(String name, long attribute) {
        JAMSLong v = (JAMSLong) this.values.get(name);
        try {
            v.setValue(attribute);
        } catch (NullPointerException npe) {
            this.values.put(name, new JAMSLong(attribute));
        }
    }

    @Override
    public void setObject(String name, Object attribute) {
        this.values.put(name, attribute);
    }

    @Override
    public void setGeometry(String name, Geometry attribute) {
        JAMSGeometry v = (JAMSGeometry) this.values.get(name);
        try {
            v.setValue(attribute);
        } catch (NullPointerException npe) {
            this.values.put(name, new JAMSGeometry(attribute));
        }
    }

    @Override
    public float getFloat(String name) throws JAMSEntity.NoSuchAttributeException {
        if (values.containsKey(name)) {
            return ((JAMSFloat) values.get(name)).getValue();
        } else {
            throw new JAMSEntity.NoSuchAttributeException(JAMS.resources.getString("Attribute_") + name + JAMS.resources.getString("_(float)_not_found!"));
        }
    }

    @Override
    public double getDouble(String name) throws JAMSEntity.NoSuchAttributeException {
        if (values.containsKey(name)) {
            return ((JAMSDouble) values.get(name)).getValue();
        } else {
            throw new JAMSEntity.NoSuchAttributeException(JAMS.resources.getString("Attribute_") + name + JAMS.resources.getString("_(double)_not_found!"));
        }
    }

    @Override
    public int getInt(String name) throws JAMSEntity.NoSuchAttributeException {
        if (values.containsKey(name)) {
            return ((JAMSInteger) values.get(name)).getValue();
        } else {
            throw new JAMSEntity.NoSuchAttributeException(JAMS.resources.getString("Attribute_") + name + JAMS.resources.getString("_(int)_not_found!"));
        }
    }

    @Override
    public long getLong(String name) throws JAMSEntity.NoSuchAttributeException {
        if (values.containsKey(name)) {
            return ((JAMSLong) values.get(name)).getValue();
        } else {
            throw new JAMSEntity.NoSuchAttributeException(JAMS.resources.getString("Attribute_") + name + JAMS.resources.getString("_(long)_not_found!"));
        }
    }

    @Override
    public Object getObject(String name) throws JAMSEntity.NoSuchAttributeException {
        if (values.containsKey(name)) {
            return values.get(name);
        } else {
            throw new JAMSEntity.NoSuchAttributeException(JAMS.resources.getString("Attribute_") + name + JAMS.resources.getString("_(Object)_not_found!"));
        }
    }

    @Override
    public Geometry getGeometry(String name) throws JAMSEntity.NoSuchAttributeException {
        if (values.containsKey(name)) {
            return ((JAMSGeometry) values.get(name)).getValue();
        } else {
            throw new JAMSEntity.NoSuchAttributeException(JAMS.resources.getString("Attribute_") + name + JAMS.resources.getString("_(Geometry)_not_found!"));
        }
    }

    @Override
    public boolean existsAttribute(String name) {
        try {
            if (values.containsKey(name)) {
                return true;
            } else {
                return false;
            }
        } catch (NullPointerException e) {
            return false;
        }
    }

    @Override
    public Object[] getKeys() {
        return this.values.keySet().toArray(new Object[values.size()]);
    }

    //that's crap
    @Override
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

    @Override
    public void setValue(HashMap<String, Object> values) {
        this.values = values;
    }

    @Override
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

    @Override
    public long getId() {
        return id;
    }

    @Override
    public void setId(long id) {
        this.id = id;
    }
}
