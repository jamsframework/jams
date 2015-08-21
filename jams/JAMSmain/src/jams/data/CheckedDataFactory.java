/*
 * DefaultDataFactory.getDataFactory().java
 * Created on 24. November 2005, 07:33
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

import com.vividsolutions.jts.geom.Geometry;
import java.util.HashMap;

/**
 *
 * @author S. Kralisch
 */
public class CheckedDataFactory extends DefaultDataFactory {

    private static HashMap<Class, Class> interfaceLookup, classLookup;
    static private CheckedDataFactory instance = new CheckedDataFactory();
    
    private CheckedDataFactory(){
        
    }
    /**
     * Creates a new JAMSData instance that is a representation of a given data object
     * @param value The object to be represented by a JAMSData instance
     * @return A JAMSData instance
     */
    public JAMSData createInstance(Object value) {
        Class type = value.getClass();
        JAMSData result;

        if (Integer.class.isAssignableFrom(type)) {
            JAMSInteger v = new JAMSInteger();
            v.setValue(((Integer) value).intValue());
            result = v;
        } else if (Long.class.isAssignableFrom(type)) {
            JAMSLong v = new JAMSLong();
            v.setValue(((Long) value).longValue());
            result = v;
        } else if (Float.class.isAssignableFrom(type)) {
            JAMSFloatChecked v = new JAMSFloatChecked();
            v.setValue(((Float) value).floatValue());
            result = v;
        } else if (Double.class.isAssignableFrom(type)) {
            JAMSDoubleChecked v = new JAMSDoubleChecked();
            v.setValue(((Double) value).doubleValue());
            result = v;
        } else if (String.class.isAssignableFrom(type)) {
            JAMSString v = new JAMSString();
            v.setValue(value.toString());
            result = v;
        } else if (Geometry.class.isAssignableFrom(type)) {
            JAMSGeometry v = new JAMSGeometry((Geometry) value);
            result = v;
        } else {
            result = new jams.data.JAMSString();
            result.setValue(value.toString());
        }

        return result;
    }

    public Attribute.Double createDouble() {
        return new JAMSDoubleChecked();
    }

    public Attribute.DoubleArray createDoubleArray() {
        return new JAMSDoubleArrayChecked();
    }

    public Attribute.Float createFloat() {
        return new JAMSFloatChecked();
    }

    public Attribute.FloatArray createFloatArray() {
        return new JAMSFloatArrayChecked();
    }
    
    /**
     * Returns the standard implementation of a JAMSData interface
     * @param interfaceType A JAMSData interface 
     * @return The class that represents the standard implementation of the 
     * provided interface
     */
    public Class getImplementingClass(Class interfaceType) {

        while (interfaceType.isArray()) {
            interfaceType = interfaceType.getComponentType();
        }

        if (interfaceLookup == null) {

            interfaceLookup = new HashMap<Class, Class>();

            interfaceLookup.put(Attribute.Boolean.class, JAMSBoolean.class);
            interfaceLookup.put(Attribute.BooleanArray.class, JAMSBooleanArray.class);
            interfaceLookup.put(Attribute.Calendar.class, JAMSCalendar.class);
            interfaceLookup.put(Attribute.DirName.class, JAMSDirName.class);
            interfaceLookup.put(Attribute.Document.class, JAMSDocument.class);
            interfaceLookup.put(Attribute.Double.class, JAMSDoubleChecked.class);
            interfaceLookup.put(Attribute.DoubleArray.class, JAMSDoubleArrayChecked.class);
            interfaceLookup.put(Attribute.Entity.class, JAMSEntityChecked.class);
            interfaceLookup.put(Attribute.EntityCollection.class, JAMSEntityCollectionChecked.class);
            interfaceLookup.put(Attribute.FileName.class, JAMSFileName.class);
            interfaceLookup.put(Attribute.Float.class, JAMSFloatChecked.class);
            interfaceLookup.put(Attribute.FloatArray.class, JAMSFloatArrayChecked.class);
            interfaceLookup.put(Attribute.Geometry.class, JAMSGeometry.class);
            interfaceLookup.put(Attribute.Integer.class, JAMSInteger.class);
            interfaceLookup.put(Attribute.IntegerArray.class, JAMSIntegerArray.class);
            interfaceLookup.put(Attribute.Long.class, JAMSLong.class);
            interfaceLookup.put(Attribute.LongArray.class, JAMSLongArray.class);
            interfaceLookup.put(Attribute.Object.class, JAMSObject.class);
            interfaceLookup.put(Attribute.String.class, JAMSString.class);
            interfaceLookup.put(Attribute.StringArray.class, JAMSStringArray.class);
            interfaceLookup.put(Attribute.TimeInterval.class, JAMSTimeInterval.class);
        }

        return interfaceLookup.get(interfaceType);
    }
    
    public static DataFactory getDataFactory(){        
        return instance;
    }
}
