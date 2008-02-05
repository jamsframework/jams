/*
 * StandardDataStore.java
 * Created on 4. Februar 2008, 23:21
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

/**
 *
 * @author Sven Kralisch
 */
public abstract class StandardDataStore implements DataStore {
    
    public StandardDataStore() {
    }
    
    public String getTitle() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void setTitle(String title) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public String getDescription() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void setDescription(String descriptiom) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public DataSetDefinition getDataSetDefinition() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void setDataSetDefinition(DataSetDefinition dsDef) {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
}
