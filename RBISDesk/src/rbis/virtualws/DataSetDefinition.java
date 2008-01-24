/*
 * DataSetDefinition.java
 * Created on 24. Januar 2008, 08:53
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
package rbis.virtualws;

import java.util.ArrayList;

public class DataSetDefinition {

    private ArrayList<Class> dataTypes;

    public DataSetDefinition(ArrayList<Class> dataTypes) {
        this.dataTypes = dataTypes;
    }

    public AttributeDefinition getAttribute(String title) {
        return null;
    }

    public Object getAttributeValue(AttributeDefinition attribute, int column) {
        return null;
    }

    public ArrayList<Object> getAttributeValues(AttributeDefinition attribute) {
        return null;
    }

    public ArrayList<Object> getAttributeValues(int column) {
        return null;
    }

    public void removeAttribute(AttributeDefinition attribute) {
    }

    public void addAttribute(Class type) {
    }

    ArrayList<AttributeDefinition> getAttributes() {
        return null;
    }
}

