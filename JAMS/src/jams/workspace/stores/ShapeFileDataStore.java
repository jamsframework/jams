/*
 * ShapeFileDataStore.java
 * Created on 13. April 2009, 19:00
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
package jams.workspace.stores;

import jams.workspace.DataSet;
import jams.workspace.JAMSWorkspace;
import java.io.File;
import java.io.IOException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 *
 * @author Sven Kralisch <sven.kralisch at uni-jena.de>
 */
public class ShapeFileDataStore extends GeoDataStore {

    public ShapeFileDataStore(JAMSWorkspace ws, String id, Document doc) throws ClassNotFoundException {
        super(ws);
        this.id = id;

        Element sourceElement = (Element) doc.getElementsByTagName("source").item(0);

        File sourceFile = null;
        if (sourceElement != null) {
            String sourceFileName = sourceElement.getAttribute("value");
            if (sourceFileName != null) {
                sourceFile = new File(sourceFileName);
            }
        } else {
            sourceFile = new File(ws.getLocalInputDirectory(), id + ".shp");
        }

        // to be cont'd, reader implemented as jams.workspace.DataReader 
        // in components project (Geotools dependencies outside JAMS!!)
    }

    @Override
    public boolean hasNext() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public DataSet getNext() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void close() throws IOException {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}
