/*
 * JAMSEntity.java
 * Created on 2. August 2005, 21:04
 *
 * This file is part of JAMS
 * Copyright (C) 2005 FSU Jena
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
package jams.data;

import java.util.*;
import com.vividsolutions.jts.geom.Geometry;
import gnu.trove.map.hash.THashMap;
import jams.JAMS;

/**
 *
 * @author S. Kralisch
 */
public class JAMSEntity implements Attribute.Entity {

    private THashMap<String, Object> values = new THashMap<String, Object>();
    private long id = -1;

    @Override
    public void setFloat(String name, float attribute) {
        JAMSFloat v = (JAMSFloat) this.values.get(name);
        if (v!=null){
            v.setValue(attribute);
        }else{
            this.values.put(name, new JAMSFloat(attribute));
        }
    }

    @Override
    public void setDouble(String name, double attribute) {
        JAMSDouble v = (JAMSDouble) this.values.get(name);
        if (v!=null){
            v.setValue(attribute);
        }else{
            this.values.put(name, new JAMSDouble(attribute));
        }
    }

    @Override
    public void setInt(String name, int attribute) {
        JAMSInteger v = (JAMSInteger) this.values.get(name);
        if (v!=null){
            v.setValue(attribute);
        }else{
            this.values.put(name, new JAMSInteger(attribute));
        }                
    }

    @Override
    public void setLong(String name, long attribute) {
        JAMSLong v = (JAMSLong) this.values.get(name);
        if (v!=null){
            v.setValue(attribute);
        }else{
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
        if (v!=null){
            v.setValue(attribute);
        }else{
            this.values.put(name, new JAMSGeometry(attribute));
        }                
    }

    @Override
    public float getFloat(String name) throws JAMSEntity.NoSuchAttributeException {
        if (values.containsKey(name)) {
            return ((JAMSFloat) values.get(name)).getValue();
        } else {
            throw new JAMSEntity.NoSuchAttributeException(JAMS.i18n("Attribute_") + name + JAMS.i18n("_(float)_not_found!"));
        }
    }

    @Override
    public double getDouble(String name) throws JAMSEntity.NoSuchAttributeException {
        Object o = values.get(name);
        if (o == null){
            throw new JAMSEntity.NoSuchAttributeException(JAMS.i18n("Attribute_") + name + JAMS.i18n("_(double)_not_found!"));
        }
        return ((JAMSDouble)o).getValue(); 
    }

    @Override
    public int getInt(String name) throws JAMSEntity.NoSuchAttributeException {
        if (values.containsKey(name)) {
            return ((JAMSInteger) values.get(name)).getValue();
        } else {
            throw new JAMSEntity.NoSuchAttributeException(JAMS.i18n("Attribute_") + name + JAMS.i18n("_(int)_not_found!"));
        }
    }

    @Override
    public long getLong(String name) throws JAMSEntity.NoSuchAttributeException {
        if (values.containsKey(name)) {
            return ((JAMSLong) values.get(name)).getValue();
        } else {
            throw new JAMSEntity.NoSuchAttributeException(JAMS.i18n("Attribute_") + name + JAMS.i18n("_(long)_not_found!"));
        }
    }

    @Override
    public Object getObject(String name) throws JAMSEntity.NoSuchAttributeException {
        if (values.containsKey(name)) {
            return values.get(name);
        } else {
            throw new JAMSEntity.NoSuchAttributeException(JAMS.i18n("Attribute_") + name + JAMS.i18n("_(Object)_not_found!"));
        }
    }

    @Override
    public Geometry getGeometry(String name) throws JAMSEntity.NoSuchAttributeException {
        if (values.containsKey(name)) {
            return ((JAMSGeometry) values.get(name)).getValue();
        } else {
            throw new JAMSEntity.NoSuchAttributeException(JAMS.i18n("Attribute_") + name + JAMS.i18n("_(Geometry)_not_found!"));
        }
    }

    @Override
    public boolean existsAttribute(String name) {
        return values.containsKey(name);
    }

    @Override
    public Object[] getKeys() {
        return this.values.keySet().toArray(new Object[values.size()]);
    }

    @Override
    public boolean isEmpty() {
        return this.values.isEmpty();
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
    public void setValue(THashMap<String, Object> values) {
        this.values = values;
    }

    @Override
    public THashMap<String, Object> getValue() {
        return values;
    }

    public boolean removeValue(String name) {
        if (this.values.remove(name) != null) {
            return true;
        } else {
            return false;
        }
    }
    
    public void removeValues() {
        values.clear();
    }

    public String getStringValue() {
        String result = "";
        String[] names = values.keySet().toArray(new String[values.size()]);
        if (names.length > 0) {
            result += names[0] + "=" + values.get(names[0]).toString() + "f";
        }
        for (int i = 1; i < names.length; i++) {
            result += "\t" + names[i] + "=" + values.get(names[i]).toString() + "f";
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

    @Override
    public String toString() {
        String result = "$ID = " + getId();
        if (values == null) {
            return result;
        }
        TreeSet<String> orderedSet = new TreeSet<String>(values.keySet());
        Iterator<String> iter = orderedSet.iterator();
        while (iter.hasNext()) {
            String key = iter.next();
            result += "\t" + key + "=" + values.get(key);
        }
        return result;
    }
}
