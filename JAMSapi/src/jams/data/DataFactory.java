/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package jams.data;

import com.vividsolutions.jts.geom.Geometry;
import java.util.HashMap;

/**
 *
 * @author christian
 */
public interface DataFactory {

    public JAMSData createInstance(Class clazz) throws InstantiationException, IllegalAccessException;
    /**
     * Creates a new JAMSData instance that is a representation of a given data object
     * @param value The object to be represented by a JAMSData instance
     * @return A JAMSData instance
     */
    public JAMSData createInstance(Object value);
    public Attribute.Double createDouble();

    public  Attribute.DoubleArray createDoubleArray();

    public  Attribute.Float createFloat();

    public  Attribute.FloatArray createFloatArray();

    public  Attribute.Long createLong();

    public  Attribute.LongArray createLongArray();

    public  Attribute.Integer createInteger();

    public  Attribute.IntegerArray createIntegerArray();

    public  Attribute.String createString();

    public  Attribute.StringArray createStringArray();

    public  Attribute.Boolean createBoolean();

    public  Attribute.BooleanArray createBooleanArray();

    public  Attribute.Calendar createCalendar();

    public  Attribute.DirName createDirName();

    public  Attribute.Document createJAMSDocument();

    public  Attribute.FileName createFileName();

    public  Attribute.Geometry createGeometry();

    public  Attribute.TimeInterval createTimeInterval();

    public  Attribute.Entity createEntity();

    public  Attribute.Document createDocument();

    public  Attribute.EntityCollection createEntityCollection();

    public  Attribute.Object createObject();
    
    public  Attribute.ObjectArray createObjectArray();

    /**
     * Returns the standard implementation of a JAMSData interface
     * @param interfaceType A JAMSData interface 
     * @return The class that represents the standard implementation of the 
     * provided interface
     */
    public Class getImplementingClass(Class interfaceType);

    /**
     * Returns the JAMSData interface that belongs to a JAMSData class. This
     * method exists for compatibility reasons only.
     * @param clazz A class that implements a JAMSData interface
     * @return The belonging JAMSData interface
     */
    public Class getBelongingInterface(Class clazz);

}
