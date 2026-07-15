/*
 * DataWriter.java
 * Created on 21. März 2008, 13:32
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

import java.io.IOException;

/**
 *
 * @author Sven Kralisch
 */
public interface DataWriter {
    
    public static interface DataWriterState{
        
    }
    
    public int init();

    public int cleanup();

    public int writeData(DataSet[] data);
    
    public DataWriterState getState();
    public void setState(DataWriterState state) throws IOException; 
}
