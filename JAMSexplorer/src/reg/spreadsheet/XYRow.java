/*
 * XYRow.java
 * Created on 17. September 2009, 15:16
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

package reg.spreadsheet;

/**
 *
 * @author Robert Riedel
 */
public class XYRow implements Comparable<XYRow> {

    double[] col;

    int compare_index;

    public XYRow(double[] rowdata, int compare_index) {
        this.col = rowdata;
        this.compare_index = compare_index;
    }

    public void setCompareIndex(int compare_index) {
        this.compare_index = compare_index;
    }

    public int compareTo(XYRow arg) {

        if (col[compare_index] < arg.col[compare_index]) {
            return -1;
        }
        if (col[compare_index] > arg.col[compare_index]) {
            return 1;
        }
        return 0;

    }
}
