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

import jams.tools.JAMSTools;
import jams.workspace.DataSet;
import jams.workspace.JAMSWorkspace;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 *
 * @author Sven Kralisch <sven.kralisch at uni-jena.de>
 */
public class ShapeFileDataStore extends GeoDataStore {

    /**
     * the name of the shapefile
     */
    private String fileName = null;

    /**
     * the uri
     */
    private URI uri = null;

    /**
     * the key column, necessary for identifying values
     */
    private String keyColumn = null;

    /**
     * the shapeFile itself
     */
    private File shapeFile;

    public ShapeFileDataStore(JAMSWorkspace ws, String id, Document doc) throws URISyntaxException {
        super(ws);
        this.id = id;
        // source can have uri or filename
        Element sourceElement = (Element) doc.getElementsByTagName("source").item(0);
        if (sourceElement != null) {
            String uriString = getNodeValue(sourceElement, "uri");
            if (!JAMSTools.isEmptyString(uriString)) {
                this.uri = new URI(uriString);
                this.shapeFile = new File(this.uri);
            }
            if (this.shapeFile == null || !this.shapeFile.exists()) {
                String i_filename = getNodeValue(sourceElement, "filename");
                if (!JAMSTools.isEmptyString(i_filename)) {
                    this.shapeFile = new File(ws.getLocalInputDirectory(), i_filename);
                }
            }
        }

        if (shapeFile == null) {
            this.shapeFile = new File(ws.getLocalInputDirectory(), id + ".shp");
        }

        if (this.shapeFile.exists()) {
            ws.getRuntime().println("Trying to use shape file from " + shapeFile.toString() + " ..");
            this.uri = this.shapeFile.toURI();
            this.fileName = this.shapeFile.getName();
            Element keyElement = (Element) doc.getElementsByTagName("key").item(0);
            if (keyElement != null) {
                this.keyColumn = keyElement.getTextContent();
            }
        } else {
            ws.getRuntime().println("No shape file found for shape datastore \"" + id + "\" ..");
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
    }

    public String getFileName() {
        return fileName;
    }

    public File getShapeFile() {
        return shapeFile;
    }

    public URI getUri() {
        return uri;
    }

    public String getKeyColumn() {
        return keyColumn;
    }
}
