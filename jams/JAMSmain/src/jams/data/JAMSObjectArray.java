/*
 * JAMSObject.java
 * Created on 28. Mai 2009, 22:35
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
package jams.data;

/**
 *
 * @author Sven Kralisch <sven.kralisch at uni-jena.de>
 */
public class JAMSObjectArray implements Attribute.ObjectArray {

    private java.lang.Object[] value;

    JAMSObjectArray() {
    }

    JAMSObjectArray(Object[] value) {
        this.setValue(value);
    }

    @Override
    public java.lang.Object[] getValue() {
        return value;
    }

    @Override
    public void setValue(java.lang.Object[] value) {
        this.value = value;
    }

    @Override
    public void setValue(String value) {
    }
}
