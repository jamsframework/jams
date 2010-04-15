/*
 * DataSetDefinition.java
 * Created on 5. November 2009, 16:25
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

package jams.workspace;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Set;

/**
 *
 * @author Sven Kralisch <sven.kralisch at uni-jena.de>
 */
public interface DataSetDefinition extends Serializable {

    public static final String TYPE_ID = "#_TYPE_";

    void addAttribute(String attributeName, Class type);

    Object getAttributeValue(String attributeName, int column);

    ArrayList<Object> getAttributeValues(String attributeName);

    ArrayList<Object> getAttributeValues(int column);

    Set<String> getAttributes();

    int getColumnCount();

    Class getType(String attributeName);

    void removeAttribute(String attributeName);

    boolean setAttributeValues(String attributeName, ArrayList<Object> values);

    boolean setAttributeValues(String attributeName, Object value);

    boolean setAttributeValues(int column, ArrayList<Object> values);

    String toASCIIString();

}
