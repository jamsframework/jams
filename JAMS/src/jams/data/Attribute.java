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
import java.util.GregorianCalendar;
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

        public GregorianCalendar getValue();

        public void setValue(GregorianCalendar value);

        public int compareTo(JAMSCalendar cal, int accuracy);

        public void removeUnsignificantComponents(int accuracy);

        public java.lang.String toString(DateFormat dateFormat);

        public void setValue(java.lang.String value, java.lang.String format) throws ParseException;

        public void setDateFormat(java.lang.String formatString);

        public DateFormat getDateFormat();
    }

    public interface FileName extends String {
    }

    public interface DirName extends String {
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

        public void setFloat(String name, float attribute);

        public void setDouble(String name, double attribute);

        public void setInt(String name, int attribute);

        public void setLong(String name, long attribute);

        public void setObject(String name, Object attribute);

        public void setGeometry(String name, Geometry attribute);

        public float getFloat(String name) throws NoSuchAttributeException;

        public double getDouble(String name) throws NoSuchAttributeException;

        public int getInt(String name) throws NoSuchAttributeException;

        public long getLong(String name) throws NoSuchAttributeException;

        public Object getObject(String name) throws NoSuchAttributeException;

        public Geometry getGeometry(String name) throws NoSuchAttributeException;

        public boolean existsAttribute(String name);

        public Object[] getKeys();

        public void setValue(HashMap<String, Object> value);

        public HashMap<String, Object> getValue();

        public long getId();

        public void setId(long id);

        public class NoSuchAttributeException extends Exception {

            public NoSuchAttributeException(java.lang.String errorMsg) {
                super(errorMsg);
            }
        }
    }

    public interface EntityList extends JAMSData {

        public JAMSEntity[] getEntityArray();

        public JAMSEntityEnumerator getEntityEnumerator();

        public ArrayList<JAMSEntity> getEntities();

        public void setEntities(ArrayList<JAMSEntity> entities);

        public JAMSEntity getCurrent();

        public void setValue(ArrayList<JAMSEntity> entities);

        public ArrayList<JAMSEntity> getValue();
    }

    public interface TimeInterval extends JAMSData {

        public boolean encloses(JAMSTimeInterval ti);

        public long getStartOffset(JAMSTimeInterval ti);

        public java.lang.String getValue();

        public JAMSCalendar getStart();

        public void setStart(JAMSCalendar start);

        public JAMSCalendar getEnd();

        public void setEnd(JAMSCalendar end);

        public int getTimeUnit();

        public void setTimeUnit(int timeUnit);

        public int getTimeUnitCount();

        public void setTimeUnitCount(int timeUnitCount);

        public long getNumberOfTimesteps();


    }
}

