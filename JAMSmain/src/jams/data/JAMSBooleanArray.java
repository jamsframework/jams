/*
 * JAMSBooleanArray.java
 * Created on 7. Februar 2006, 17:29
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

import java.util.StringTokenizer;

/**
 *
 * @author S. Kralisch
 */
public class JAMSBooleanArray implements Attribute.BooleanArray {

    private boolean[] value;

    /** Creates a new instance of JAMSBooleanArray */
    JAMSBooleanArray() {
    }

    JAMSBooleanArray(boolean[] value) {
        this.value = value;
    }

    @Override
    public boolean[] getValue() {
        return value;
    }

    @Override
    public void setValue(boolean[] value) {
        this.value = value;
    }

    @Override
    public void setValue(String value) {
        StringTokenizer st = new StringTokenizer(value, ",");
        boolean[] values = new boolean[st.countTokens()];
        for (int i = 0; i < values.length; i++) {
            values[i] = Boolean.parseBoolean(st.nextToken().trim());
        }
        this.value = values;
    }
    
    @Override
    public String toString() {
        String s = "";
        if (value == null || value.length == 0) {
            s = "null";
        } else {
            s += value[0];
            for (int i = 1; i < value.length; i++) {
                s += "," + value[i];
            }
        }
        return s;
    }
    
}
