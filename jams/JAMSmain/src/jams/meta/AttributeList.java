/*
 * AttributeList.java
 * Created on 05.11.2014, 07:45:06
 *
 * This file is part of JAMS
 * Copyright (C) FSU Jena
 *
 * JAMS is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public License
 * as published by the Free Software Foundation; either version 3
 * of the License, or (at your option) any later version.
 *
 * JAMS is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with JAMS. If not, see <http://www.gnu.org/licenses/>.
 *
 */
package jams.meta;

import java.util.ArrayList;
import java.util.List;
import java.util.Observable;

/**
 *
 * @author Sven Kralisch <sven.kralisch at uni-jena.de>
 */
public class AttributeList extends Observable {

    private Class type;
    private List<String> elements = new ArrayList();
    private ModelDescriptor md;
    private String name;

    public AttributeList(ModelDescriptor md, String name) {
        this.md = md;
        this.name = name;
    }

    /**
     * @return the type
     */
    public Class getType() {
        return type;
    }

    /**
     * @param type the type to set
     */
    public void setType(Class type) {
        this.type = type;
    }

    public void add(String value) {
        this.elements.add(value);
        this.notifyObservers(value);
    }

    public void remove(String value) {
        this.elements.remove(value);
        this.notifyObservers(value);
    }

    /**
     * @return the elements
     */
    public List<String> getElements() {
        return elements;
    }

    /**
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * @param name the name to set
     */
    public void setName(String name) {
        this.md.getAttributeLists().remove(this.name);
        this.name = name;
        this.md.getAttributeLists().put(this.name, this);
    }

}
