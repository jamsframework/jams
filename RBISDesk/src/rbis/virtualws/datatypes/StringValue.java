/*
 * StringValue.java
 * Created on 7. Februar 2008, 21:26
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
package rbis.virtualws.datatypes;

import org.unijena.jams.data.JAMSCalendar;

/**
 *
 * @author Sven Kralisch
 */
public class StringValue implements DataValue {

    private String value;

    public StringValue(String value) {
        this.value = value;
    }

    public double getDouble() throws NumberFormatException {
        return new Double(value);
    }

    public long getLong() throws NumberFormatException {
        return new Long(value);
    }

    public String getString() {
        return value;
    }

    public Object getObject() {
        return value;
    }

    public void setDouble(double value) {
        this.value = "" + value;
    }

    public void setLong(long value) {
        this.value = "" + value;
    }

    public void setString(String value) {
        this.value = value;
    }

    public void setObject(Object value) {
        if (value instanceof String) {
            this.value = (String) value;
        }
    }

    public JAMSCalendar getCalendar() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void setCalendar(JAMSCalendar value) {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}

