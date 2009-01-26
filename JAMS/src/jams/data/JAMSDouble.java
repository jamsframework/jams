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
package jams.data;

/**
 *
 * @author S. Kralisch
 */
public class JAMSDouble implements Attribute.Double, JAMSNumeric {

    private double value;

    /**
     * Creates a new instance of JAMSDouble
     */
    JAMSDouble() {
    }

    JAMSDouble(double value) {
        this.value = value;
    }

    /*
    public JAMSDouble(double value, double minValue, double maxValue, String unitString) {
        this.setValue(value);
        this.setUnit(unitString);
        this.setRange(minValue, maxValue);
    }
    */

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
        if ((other == null) || !(other instanceof JAMSDouble)) {
            return false;
        }
        return value == ((JAMSDouble) other).getValue();
    }  // end equals()
}
