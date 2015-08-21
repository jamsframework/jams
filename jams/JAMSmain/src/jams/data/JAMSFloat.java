/*
 * JAMSFloat.java
 * Created on 28. September 2005, 16:06
 *
 * This file is part of JAMS
 * Copyright (C) 2005 FSU Jena
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

import jams.JAMS;
import java.util.Locale;

/**
 *
 * @author S. Kralisch
 */
public class JAMSFloat implements Attribute.Float {

    private float value;
    
    /**
     * Creates a new instance of JAMSFloat
     */
    JAMSFloat() {
    }

    JAMSFloat(float value) {
        this.value = value;
    }

    public String toString() {
//        return Float.toString(value);
        return String.format(Locale.ENGLISH, JAMS.getFloatFormat(), value);
    }

    public float getValue() {
        return value;
    }

    public void setValue(float value) {
        this.value = value;
    }

    public void setValue(String value) {
        this.value = Float.parseFloat(value);
    }

    public boolean equals(Object other) {
        if ((other == null) || !(other instanceof JAMSFloat)) {
            return false;
        }
        return value == ((JAMSFloat) other).getValue();
    }  // end equals()
}
