/*
 * JAMSEntity.java
 * Created on 2. August 2005, 21:04
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

import java.util.*;
import com.vividsolutions.jts.geom.Geometry;

/**
 *
 * @author S. Kralisch
 */
public interface JAMSEntity extends JAMSData {

    public void setFloat(String name, float attribute);

    public void setDouble(String name, double attribute);

    public void setInt(String name, int attribute);

    public void setLong(String name, long attribute);

    public void setObject(String name, Object attribute);

    public void setGeometry(String name, Geometry attribute);

    public float getFloat(String name) throws NoSuchAttributeException;

    public double getDouble(String name) throws NoSuchAttributeException;

    public int getInt(String name) throws NoSuchAttributeException;

    public long getLong(String name) throws NoSuchAttributeException;

    public Object getObject(String name) throws NoSuchAttributeException;

    public Geometry getGeometry(String name) throws NoSuchAttributeException;

    public boolean existsAttribute(String name);

    public Object[] getKeys();

    public void setValue(HashMap<String, Object> value);

    public HashMap<String, Object> getValue();

    public class NoSuchAttributeException extends Exception {

        public NoSuchAttributeException(String errorMsg) {
            super(errorMsg);
        }
    }
}
