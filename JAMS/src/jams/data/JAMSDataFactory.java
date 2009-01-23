/*
 * JAMSDataFactory.java
 * Created on 24. November 2005, 07:33
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

import com.vividsolutions.jts.geom.Geometry;
import jams.runtime.JAMSRuntime;

/**
 *
 * @author S. Kralisch
 */
public class JAMSDataFactory {

    /**
     * Creates a new instance of a given class
     * @param className The name of the class
     * @return An instance of the provided class
     * @throws java.lang.ClassNotFoundException
     * @throws java.lang.InstantiationException
     * @throws java.lang.IllegalAccessException
     */
    public static JAMSData getInstance(String className) throws ClassNotFoundException, InstantiationException, IllegalAccessException {
        return getInstance(Class.forName(className));
    }

    /**
     * Creates a new instance of a given class
     * @param className The name of the class
     * @param rt A runtime object that will be used to handle any exceptions ocurred
     * @return An instance of the provided class
     */
    public static JAMSData getInstance(String className, JAMSRuntime rt) {
        JAMSData value = null;
        try {
            value = getInstance(Class.forName(className));
        } catch (ClassNotFoundException ex) {
            rt.handle(ex, false);
        } catch (InstantiationException ex) {
            rt.handle(ex, false);
        } catch (IllegalAccessException ex) {
            rt.handle(ex, false);
        }

        return value;
    }

    /**
     * Creates a new instance of a given class
     * @param clazz The class
     * @return An instance of the provided class
     * @throws java.lang.InstantiationException
     * @throws java.lang.IllegalAccessException
     */
    public static JAMSData getInstance(Class clazz) throws InstantiationException, IllegalAccessException {
        if (JAMSEntity.class.isAssignableFrom(clazz)) {
            clazz = JAMSCheckedEntity.class;
        }
        return (JAMSData) clazz.newInstance();
    }

    /**
     * Creates a new instance of a given class
     * @param clazz The class
     * @param rt A runtime object that will be used to handle any exceptions ocurred
     * @return An instance of the provided class
     */
    public static JAMSData getInstance(Class clazz, JAMSRuntime rt) {
        if (JAMSEntity.class.isAssignableFrom(clazz)) {
            clazz = JAMSCheckedEntity.class;
        }
        JAMSData value = null;
        try {
            value = (JAMSData) clazz.newInstance();
        } catch (InstantiationException ex) {
            rt.handle(ex, false);
        } catch (IllegalAccessException ex) {
            rt.handle(ex, false);
        }
        return value;
    }

    /**
     * Creates a new JAMSData instance that is a representation of a given object
     * @param value The object to be represented by a JAMSData instance
     * @return A JAMSData instance
     */
    public static JAMSData getInstance(Object value) {
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
            JAMSFloat v = new JAMSFloat();
            v.setValue(((Float) value).floatValue());
            result = v;
        } else if (Double.class.isAssignableFrom(type)) {
            JAMSDouble v = new JAMSDouble();
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
}
