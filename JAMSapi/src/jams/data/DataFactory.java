/*
 * This file is part of JAMS
 * Copyright (C) FSU Jena
 *
 * JAMS is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public License
 * as published by the Free Software Foundation; either version 3
 * of the License, or (at your option) any later version.
 *
 * JAMS is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with JAMS. If not, see <http://www.gnu.org/licenses/>.
 */
package jams.data;

import java.io.Serializable;

/**
 *
 * @author christian
 */
public interface DataFactory extends Serializable {

    public JAMSData createInstance(Class clazz) throws InstantiationException, IllegalAccessException;

    /**
     * Creates a new JAMSData instance that is a representation of a given data
     * object
     *
     * @param value The object to be represented by a JAMSData instance
     * @return A JAMSData instance
     */
    public JAMSData createInstance(Object value);

    public Attribute.Double createDouble();

    public Attribute.DoubleArray createDoubleArray();

    public Attribute.Float createFloat();

    public Attribute.FloatArray createFloatArray();

    public Attribute.Long createLong();

    public Attribute.LongArray createLongArray();

    public Attribute.Integer createInteger();

    public Attribute.IntegerArray createIntegerArray();

    public Attribute.String createString();

    public Attribute.StringArray createStringArray();

    public Attribute.Boolean createBoolean();

    public Attribute.BooleanArray createBooleanArray();

    public Attribute.Calendar createCalendar();

    public Attribute.DirName createDirName();

    public Attribute.Document createJAMSDocument();

    public Attribute.FileName createFileName();

    public Attribute.Geometry createGeometry();

    public Attribute.TimeInterval createTimeInterval();

    public Attribute.Entity createEntity();

    public Attribute.Document createDocument();

    public Attribute.EntityCollection createEntityCollection();

    public Attribute.Object createObject();

    public Attribute.ObjectArray createObjectArray();
    
    /**
     * Returns the standard implementation of a JAMSData interface
     *
     * @param interfaceType A JAMSData interface
     * @return The class that represents the standard implementation of the
     * provided interface
     */
    public Class getImplementingClass(Class interfaceType);

    /**
     * Returns the JAMSData interface that belongs to a JAMSData class. This
     * method exists for compatibility reasons only.
     *
     * @param clazz A class that implements a JAMSData interface
     * @return The belonging JAMSData interface
     */
    public Class getBelongingInterface(Class clazz);
}
