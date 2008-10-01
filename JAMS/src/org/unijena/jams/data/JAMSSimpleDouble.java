/*
 * JAMSDouble.java
 * Created on 28. September 2005, 16:06
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
package org.unijena.jams.data;

import java.io.Serializable;

/**
 *
 * @author S. Kralisch
 */
public class JAMSSimpleDouble implements JAMSData, Serializable/*implements JAMSDouble*/ {

    private double value;

    /**
     * Creates a new instance of JAMSDouble
     */
    public JAMSSimpleDouble() {
    }

    public JAMSSimpleDouble(double value) {
        this.value = value;
    }

    public String toString() {
        return Double.toString(value);
    }

    public double getValue() {
        return value;
    }

    public void setValue(double value) {
        this.value = value;
    }

    public void setValue(String value) {
        this.value = Double.parseDouble(value);
    }

    public boolean equals(Object other) {
        if ((other == null) || !(other instanceof JAMSSimpleDouble)) {
            return false;
        }
        return value == ((JAMSSimpleDouble) other).getValue();
    }  // end equals()
}
