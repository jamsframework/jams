/*
 * DoubleValue.java
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

/**
 *
 * @author Sven Kralisch
 */
public class DoubleValue implements DataValue {

    private Double value;

    public DoubleValue(double value) {
        this.value = new Double(value);
    }

    public double getDouble() {
        return value.doubleValue();
    }

    public long getLong() {
        return value.longValue();
    }

    public String getString() {
        return value.toString();
    }

    public Object getObject() {
        return value;
    }

    public void setDouble(double value) {
        this.value = new Double(value);
    }

    public void setLong(long value) {
        this.value = new Double(value);
    }

    public void setString(String value) throws NumberFormatException {
        this.value = new Double(value);
    }

    public void setObject(Object value) {
        if (value instanceof Double) {
            this.value = (Double) value;
        }
    }
}

