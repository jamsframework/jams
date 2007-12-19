/*
 * Attribute.java
 * Created on 15. Dezember 2007, 19:16
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
package org.unijena.jams.data;

/**
 *
 * @author S. Kralisch
 */
public interface Attribute {

    public interface Boolean extends JAMSData {

        public boolean getValue();

        public void setValue(boolean value);
    }

    public interface BooleanArray extends JAMSData {

        public boolean[] getValue();

        public void setValue(boolean[] value);
    }

    public interface Double extends JAMSData {

        public double getValue();

        public void setValue(double value);
    }

    public interface DoubleArray extends JAMSData {

        public double[] getValue();

        public void setValue(double[] value);
    }

    public interface Float extends JAMSData {

        public float getValue();

        public void setValue(float value);
    }

    public interface FloatArray extends JAMSData {

        public float[] getValue();

        public void setValue(float[] value);
    }

    public interface Integer extends JAMSData {

        public int getValue();

        public void setValue(int value);
    }

    public interface IntegerArray extends JAMSData {

        public int[] getValue();

        public void setValue(int[] value);
    }

    public interface Long extends JAMSData {

        public long getValue();

        public void setValue(long value);
    }

    public interface LongArray extends JAMSData {

        public long[] getValue();

        public void setValue(long[] value);
    }

    public interface String extends JAMSData {

        public String getValue();

        public void setValue(String value);
    }

    public interface StringArray extends JAMSData {

        public String[] getValue();

        public void setValue(String[] value);
    }
}

