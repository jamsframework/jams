/*
 * LongValue.java
 * Created on 7. Februar 2008, 21:26
 *
 * This file is part of JAMS
 * Copyright (C) FSU Jena
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
package jams.workspace.datatypes;

import com.vividsolutions.jts.geom.Geometry;
import jams.data.Attribute;
import jams.workspace.DataValue;

/**
 *
 * @author Sven Kralisch
 */
public class LongValue implements DataValue {

    private Long value;

    public LongValue(long value) {
        this.value = new Long(value);
    }

    public LongValue(String value) {
        this.value = new Long(value);
    }

    public double getDouble() {
        return new Double(value);
    }

    public long getLong() {
        return value;
    }

    public String getString() {
        return value.toString();
    }

    public Object getObject() {
        return value;
    }

    public void setDouble(double value) {
        this.value = new Long((long) value);
    }

    public void setLong(long value) {
        this.value = new Long(value);
    }

    public void setString(String value) throws NumberFormatException {
        this.value = new Long(value);
    }

    public void setObject(Object value) {
        if (value instanceof Long) {
            this.value = (Long) value;
        }
    }

    public Attribute.Calendar getCalendar() {
        throw new UnsupportedOperationException("Not supported.");
    }

    public void setCalendar(Attribute.Calendar value) {
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

