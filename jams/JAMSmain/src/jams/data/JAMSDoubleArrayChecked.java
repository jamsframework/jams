/*
 * JAMSDoubleArray.java
 * Created on 02. October 2005, 22:08
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

import java.util.*;

/**
 *
 * @author S. Kralisch
 */
public class JAMSDoubleArrayChecked extends JAMSDoubleArray {

    /**
     * Creates a new instance of JAMSDoubleArray
     */
    JAMSDoubleArrayChecked() {
    }

    public void setValue(double[] value) {
        if (value != null) {
            for (double d : value) {
                if (Double.isNaN(d)) {
                    throw new ArithmeticException();
                }
            }
        }
        super.setValue(value);
    }

    public void setValue(String value) {
        StringTokenizer st = new StringTokenizer(value, ";");
        double[] values = new double[st.countTokens()];
        for (int i = 0; i < values.length; i++) {
            values[i] = Double.parseDouble(st.nextToken().trim());
        }
        setValue(values);
    }
}
