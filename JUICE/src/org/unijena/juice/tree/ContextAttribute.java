/*
 * ModelAttribute.java
 * Created on 4. März 2007, 18:58
 *
 * This file is part of JAMS
 * Copyright (C) 2007 FSU Jena
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

package org.unijena.juice.tree;

/**
 *
 * @author Sven Kralisch
 */
public class ContextAttribute {
    
    public String name = "", value = "";
    public Class type = null;
    
    public ContextAttribute(String name, Class type) {
        this.name = name;
        this.type = type;
    }

}
