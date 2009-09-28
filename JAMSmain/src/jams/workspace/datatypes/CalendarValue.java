/*
 * CalendarValue.java
 * Created on 13. Februar 2008, 14:38
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

import com.vividsolutions.jts.geom.Geometry;
import jams.data.Attribute;
import jams.data.JAMSCalendar;
import jams.workspace.DataValue;

/**
 *
 * @author Sven Kralisch
 */
public class CalendarValue implements DataValue {

    private Attribute.Calendar value;

    public CalendarValue(Attribute.Calendar value) {
        this.value = value;
    }

    public double getDouble() {
        return (double) value.getTimeInMillis();
    }

    public long getLong() {
        return  value.getTimeInMillis();
    }

    public String getString() {
        return value.toString();
    }

    public Object getObject() {
        return value;
    }

    public void setDouble(double value) {
        this.value.setTimeInMillis(Math.round(value));
    }

    public void setLong(long value) {
        this.value.setTimeInMillis(value);
    }

    public void setString(String value) {
        this.value.setValue(value);
    }

    public void setObject(Object value) {
        if (value instanceof JAMSCalendar) {
            this.value = (JAMSCalendar) value;
        }
    }

    public Attribute.Calendar getCalendar() {
        return value;
    }

    public void setCalendar(Attribute.Calendar value) {
        this.value = value;
    }

    @Override
    public Geometry getGeometry() {
        throw new UnsupportedOperationException("Not supported.");
    }

    @Override
    public void setGeometry(Geometry geometry) {
        throw new UnsupportedOperationException("Not supported.");
    }
}

