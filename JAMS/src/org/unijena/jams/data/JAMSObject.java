/*
 * JAMSObject.java
 * Created on 8. September 2008, 15:29
 *
 * This file is part of JAMS
 * Copyright (C) 2008 FSU Jena
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
package org.unijena.jams.data;

import java.io.Serializable;

/**
 *
 * @author S. Kralisch
 */
public class JAMSObject implements JAMSData, Serializable {

    private Object value;

    /**
     * Creates a new instance of JAMSObject
     */
    public JAMSObject() {
    }

    public JAMSObject(Object value) {
        this.value = value;
    }

    public String toString() {
        return value.toString();
    }

    public void setValue(Object value) {
        this.value = value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public boolean equals(Object other) {
        if (other == null) {
            return false;
        }
        return value.equals(other);
    }  // end equals()
}
