/*
 * AttributeAccess.java
 * Created on 24. September 2009, 13:21
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
package jams.model;

import java.io.Serializable;

/**
 *
 * @author Sven Kralisch <sven.kralisch at uni-jena.de>
 */
public class AttributeAccess implements Serializable {

    private Component component;

    private String varName, attributeName;

    private int accessType;

    public AttributeAccess(Component component, String varName, String attributeName, int accessType) {
        this.component = component;
        this.varName = varName;
        this.attributeName = attributeName;
        this.accessType = accessType;
    }

    /**
     * @return the component
     */
    public Component getComponent() {
        return component;
    }

    /**
     * @return the varName
     */
    public String getVarName() {
        return varName;
    }

    /**
     * @return the attributeName
     */
    public String getAttributeName() {
        return attributeName;
    }

    /**
     * @return the accessType
     */
    public int getAccessType() {
        return accessType;
    }
}
