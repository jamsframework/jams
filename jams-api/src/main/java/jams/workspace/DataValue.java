/*
 * DataValue.java
 * Created on 7. Februar 2008, 21:26
 *
 * This file is part of JAMS
 * Copyright (C) FSU Jena
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
package jams.workspace;

import com.vividsolutions.jts.geom.Geometry;
import jams.data.Attribute;
import java.io.Serializable;

/**
 *
 * @author Sven Kralisch
 */
public interface DataValue extends Serializable{

    public double getDouble();

    public long getLong();
    
    public String getString();

    public Attribute.Calendar getCalendar();

    public Geometry getGeometry();

    public Object getObject();

    public void setDouble(double value);

    public void setLong(long value);
    
    public void setString(String value);

    public void setCalendar(Attribute.Calendar value);

    public void setGeometry(Geometry geometry);

    public void setObject(Object value);
}

