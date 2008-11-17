/*
 * ObjectValue.java
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
package jams.workspace.datatypes;

import jams.data.JAMSCalendar;

/**
 *
 * @author Sven Kralisch
 */
public class ObjectValue implements DataValue {

    private Object value;

    public ObjectValue(Object value) {
        this.value = value;
    }

    public double getDouble() {
        return 0;
    }

    public long getLong() {
        return 0;
    }

    public String getString() {
        return value.toString();
    }

    public Object getObject() {
        return value;
    }

    public void setDouble(double value) {
    }

    public void setLong(long value) {
    }

    public void setString(String value) {
    }

    public void setObject(Object value) {
    }

    public JAMSCalendar getCalendar() {
        return null;
    }

    public void setCalendar(JAMSCalendar value) {
    }
}

