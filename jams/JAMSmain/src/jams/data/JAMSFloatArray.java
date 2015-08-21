/*
 * JAMSFloatArray.java
 * Created on 02. October 2005, 23:01
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
import java.util.*;

/**
 *
 * @author S. Kralisch
 */
public class JAMSFloatArray implements Attribute.FloatArray {

    private float[] value;

    /**
     * Creates a new instance of JAMSFloatArray
     */
    JAMSFloatArray() {
    }

    public String toString() {
        String s = "", formatString = JAMS.getFloatFormat();
        if (value == null || value.length == 0) {
            s = "null";
        } else {
            s += String.format(Locale.ENGLISH, formatString, value[0]);
            for (int i = 1; i < value.length; i++) {
                s += ";" + String.format(Locale.ENGLISH, formatString, value[i]);
            }
        }
        return s;
    }

    public float[] getValue() {
        return value;
    }

    public void setValue(float[] value) {
        this.value = value;
    }

    public void setValue(String value) {
        StringTokenizer st = new StringTokenizer(value, ";");
        float[] values = new float[st.countTokens()];
        for (int i = 0; i < values.length; i++) {
            values[i] = Float.parseFloat(st.nextToken().trim());
        }
        this.value = values;
    }    
}
