/*
 * DataReader.java
 * Created on 31. Januar 2008, 16:21
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

/**
 *
 * @author Sven Kralisch
 */
public interface DataReader extends Serializable{

    enum ReaderType{ContentReader, MetadataReader, ContentAndMetadataReader, Empty};

    public int init();

    public int cleanup();

    public int fetchValues();

    public int fetchValues(int count);
    
    public DataSet[] getData();
    public DataSet   getMetadata(int row);

    public ReaderType getReaderType();

    public int numberOfColumns();            
}
