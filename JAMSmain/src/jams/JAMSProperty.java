/*
 * JAMSProperty.java
 * Created on 2. Mai 2007, 10:44
 *
 * This file is part of JAMS
 * Copyright (C) 2006 FSU Jena
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

package jams;

import java.io.Serializable;
import java.util.Observable;

/**
 *
 * @author Sven Kralisch
 */
public class JAMSProperty extends Observable implements Serializable{
    
    private String name;
    
    /**
     * Creates a new JAMSProperty object
     * @param name The identifier of the property
     */
    public JAMSProperty(String name) {
        this.name = name;
    }

    /**
     * 
     * @return The identifier of the property
     */
    public String getName() {
        return name;
    }
    
    @Override
    public void setChanged() {
        super.setChanged();
    }
}
