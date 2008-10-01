/*
 * JAMSBoolean.java
 * Created on 28. November 2005, 09:13
 *
 * This file is part of JAMS
 * Copyright (C) 2005 FSU Jena
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

import java.io.Serializable;

/**
 *
 * @author S. Kralisch
 */
public class JAMSBoolean implements JAMSData, Serializable {

    private boolean value;

    /** Creates a new instance of JAMSBoolean */
    public JAMSBoolean() {
    }

    public JAMSBoolean(boolean value) {
        this.value = value;
    }

    public boolean getValue() {
        return value;
    }

    public void setValue(boolean value) {
        this.value = value;
    }

    public void setValue(String value) {
        if (value.equals("1")) {
            value = "true";
        }
        this.value = Boolean.parseBoolean(value);
    }

    public String toString() {
        return Boolean.toString(value);
    }

    public boolean equals(Object other) {
        if ((other == null) || !(other instanceof JAMSBoolean)) {
            return false;
        }
        return value & ((JAMSBoolean) other).getValue();
    }  // end equals()
}
