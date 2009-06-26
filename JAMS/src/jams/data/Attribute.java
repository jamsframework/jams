/*
 * Attribute.java
 * Created on 15. Dezember 2007, 19:16
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
package jams.data;

import java.text.DateFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;

/**
 *
 * @author S. Kralisch
 */
public interface Attribute {

    public interface Boolean extends JAMSData {

        public boolean getValue();

        public void setValue(boolean value);
    }

    public interface BooleanArray extends JAMSData {

        public boolean[] getValue();

        public void setValue(boolean[] value);
    }

    public interface Double extends JAMSData {

        public double getValue();

        public void setValue(double value);
    }

    public interface DoubleArray extends JAMSData {

        public double[] getValue();

        public void setValue(double[] value);
    }

    public interface Float extends JAMSData {

        public float getValue();

        public void setValue(float value);
    }

    public interface FloatArray extends JAMSData {

        public float[] getValue();

        public void setValue(float[] value);
    }

    public interface Integer extends JAMSData {

        public int getValue();

        public void setValue(int value);
    }

    public interface IntegerArray extends JAMSData {

        public int[] getValue();

        public void setValue(int[] value);
    }

    public interface Long extends JAMSData {

        public long getValue();

        public void setValue(long value);
    }

    public interface LongArray extends JAMSData {

        public long[] getValue();

        public void setValue(long[] value);
    }

    public interface String extends JAMSData {

        public java.lang.String getValue();

        public void setValue(java.lang.String value);

        public int getLength();

        public void setLength(int length);
    }

    public interface StringArray extends JAMSData {

        public java.lang.String[] getValue();

        public void setValue(java.lang.String[] value);
    }

    public interface Calendar extends JAMSData {

        public Attribute.Calendar getValue();

        public void setValue(Attribute.Calendar value);

        public int compareTo(Attribute.Calendar cal, int accuracy);

        public int compareTo(Attribute.Calendar cal);

        public void removeUnsignificantComponents(int accuracy);

        public java.lang.String toString(DateFormat dateFormat);

        public void setValue(java.lang.String value, java.lang.String format) throws ParseException;

        public void setDateFormat(java.lang.String formatString);

        public DateFormat getDateFormat();

        public boolean after(Attribute.Calendar calendar);

        public boolean before(Attribute.Calendar calendar);

        public Attribute.Calendar clone();

        public long getTimeInMillis();

        public int get(int field);

        public void add(int field, int amount);

        public void setTimeInMillis(long millis);
    }

    public interface FileName extends Attribute.String {
    }

    public interface DirName extends Attribute.String {
    }

    public interface Document extends JAMSData {

        public org.w3c.dom.Document getValue();

        public void setValue(org.w3c.dom.Document value);
    }

    public interface Geometry extends JAMSData {

        public void setValue(com.vividsolutions.jts.geom.Geometry geo);

        public com.vividsolutions.jts.geom.Geometry getValue();
    }

    public interface Entity extends JAMSData {

        public void setFloat(java.lang.String name, float attribute);

        public void setDouble(java.lang.String name, double attribute);

        public void setInt(java.lang.String name, int attribute);

        public void setLong(java.lang.String name, long attribute);

        public void setObject(java.lang.String name, java.lang.Object attribute);

        public void setGeometry(java.lang.String name, com.vividsolutions.jts.geom.Geometry attribute);

        public float getFloat(java.lang.String name) throws NoSuchAttributeException;

        public double getDouble(java.lang.String name) throws NoSuchAttributeException;

        public int getInt(java.lang.String name) throws NoSuchAttributeException;

        public long getLong(java.lang.String name) throws NoSuchAttributeException;

        public java.lang.Object getObject(java.lang.String name) throws NoSuchAttributeException;

        public com.vividsolutions.jts.geom.Geometry getGeometry(java.lang.String name) throws NoSuchAttributeException;

        public boolean existsAttribute(java.lang.String name);

        public java.lang.Object[] getKeys();

        public void setValue(HashMap<java.lang.String, java.lang.Object> value);

        public HashMap<java.lang.String, java.lang.Object> getValue();

        public long getId();

        public void setId(long id);

        public class NoSuchAttributeException extends Exception {

            public NoSuchAttributeException(java.lang.String errorMsg) {
                super(errorMsg);
            }
        }
    }

    public interface EntityCollection extends JAMSData {

        public Attribute.Entity[] getEntityArray();

        public JAMSEntityEnumerator getEntityEnumerator();

        public ArrayList<Attribute.Entity> getEntities();

        public void setEntities(ArrayList<Attribute.Entity> entities);

        public Attribute.Entity getCurrent();

        public void setValue(ArrayList<Attribute.Entity> entities);

        public ArrayList<Attribute.Entity> getValue();
    }

    public interface TimeInterval extends JAMSData {

        public boolean encloses(JAMSTimeInterval ti);

        public long getStartOffset(JAMSTimeInterval ti);

        public java.lang.String getValue();

        public Attribute.Calendar getStart();

        public void setStart(Attribute.Calendar start);

        public Attribute.Calendar getEnd();

        public void setEnd(Attribute.Calendar end);

        public int getTimeUnit();

        public void setTimeUnit(int timeUnit);

        public int getTimeUnitCount();

        public void setTimeUnitCount(int timeUnitCount);

        public long getNumberOfTimesteps();
    }

    public interface Object extends JAMSData {

        public java.lang.Object getValue();

        public void setValue(java.lang.Object value);
    }
}

