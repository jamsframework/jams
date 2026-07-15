/*
 * GeometryValue.java
 * Created on 13. April 2009, 19:54
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
 * @author Sven Kralisch <sven.kralisch at uni-jena.de>
 */
public class GeometryValue implements DataValue {

    private Geometry value;

    public GeometryValue(Geometry value) {
        this.value = value;
    }

    @Override
    public double getDouble() {
        throw new UnsupportedOperationException("Not supported.");
    }

    @Override
    public long getLong() {
        throw new UnsupportedOperationException("Not supported.");
    }

    @Override
    public String getString() {
        return value.toString();
    }

    @Override
    public Attribute.Calendar getCalendar() {
        throw new UnsupportedOperationException("Not supported.");
    }

    @Override
    public Geometry getGeometry() {
        return value;
    }

    @Override
    public Object getObject() {
        return value;
    }

    @Override
    public void setDouble(double value) {
        throw new UnsupportedOperationException("Not supported.");
    }

    @Override
    public void setLong(long value) {
        throw new UnsupportedOperationException("Not supported.");
    }

    @Override
    public void setString(String value) {
        throw new UnsupportedOperationException("Not supported.");
    }

    @Override
    public void setCalendar(Attribute.Calendar value) {
        throw new UnsupportedOperationException("Not supported.");
    }

    @Override
    public void setGeometry(Geometry value) {
        this.value = value;
    }

    @Override
    public void setObject(Object value) {
        if (value instanceof Geometry) {
            this.value = (Geometry) value;
        } else {
            throw new UnsupportedOperationException("Not supported.");
        }
    }
}
