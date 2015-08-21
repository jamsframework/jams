/*
 * EntityEnumerator.java
 * Created on 1. August 2005, 16:51
 *
 * This file is part of JAMS
 * Copyright (C) 2005 FSU Jena
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
package jams.data;

import java.io.Serializable;

/**
 *
 * @author S. Kralisch
 */
public interface EntityEnumerator extends Serializable{            

    boolean hasNext();

    boolean hasPrevious();
    
    void reset();

    Attribute.Entity next();

    Attribute.Entity previous();

}
