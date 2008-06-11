/*
 * JAMSNumeric.java
 * Created on 6. Januar 2006, 11:36
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

import org.jscience.physics.units.Unit;

/**
 *
 * @author S. Kralisch
 */
public abstract class JAMSNumeric extends JAMSSerializableData {

    /*
    private NumericRange range = null;
    private Unit unit = null;
    
    public NumericRange getRange() {
        return range;
    }

    public void setRange(double minValue, double maxValue) {
        this.range = new NumericRange(minValue, maxValue);
    }

    public Unit getUnit() {
        return unit;
    }

    public void setUnit(String unitString) {
        this.unit = Unit.valueOf(unitString);
    }
    
    public class NumericRange {

        private double maxValue,  minValue;

        public NumericRange(double minValue, double maxValue) {
            this.maxValue = maxValue;
            this.minValue = minValue;
        }

        public double getMaxValue() {
            return maxValue;
        }

        public double getMinValue() {
            return minValue;
        }
    }
    */

}
