/*
 * Context.java
 * Created on 22.03.2010, 15:21:51
 *
 * This file is part of JAMS
 * Copyright (C) FSU Jena
 *
 * JAMS is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * JAMS is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with JAMS. If not, see <http://www.gnu.org/licenses/>.
 *
 */

package jamsui.juice;

import jams.meta.ComponentDescriptor;
import jams.meta.ContextAttribute;
import java.util.ArrayList;

/**
 *
 * @author Sven Kralisch <sven.kralisch at uni-jena.de>
 */
public class OutputDSDescriptor {

    private ComponentDescriptor context;
    private String name;
    private ArrayList<ContextAttribute> contextAttributes = new ArrayList<ContextAttribute>();
    private ArrayList<String> filters = new ArrayList<String>();

    public OutputDSDescriptor(ComponentDescriptor context) {
        this.context = context;
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
        this.name = name;
    }

    /**
     * @return the contextAttributes
     */
    public ArrayList<ContextAttribute> getContextAttributes() {
        return contextAttributes;
    }

    /**
     * @return the filters
     */
    public ArrayList<String> getFilters() {
        return filters;
    }

    /**
     * @return the context
     */
    public ComponentDescriptor getContext() {
        return context;
    }
}