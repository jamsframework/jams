/*
 * ShapeFileDataStore.java
 * Created on 13. April 2009, 19:00
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
package jams.workspace.stores;

import jams.tools.StringTools;
import jams.tools.XMLTools;
import jams.workspace.DefaultDataSet;
import jams.workspace.JAMSWorkspace;
import jams.workspace.Workspace;
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

    /**
     * constructor with xml-document
     * 
     * @param ws
     * @param id
     * @param doc
     * @throws URISyntaxException
     */
    public ShapeFileDataStore(JAMSWorkspace ws, String id, Document doc) throws URISyntaxException {
        super(ws);

        // source can have uri or filename
        String wkUri = null;
        String wkFileName = null;
        String wkKeyColumn = null;
        Element sourceElement = (Element) doc.getElementsByTagName("source").item(0);
        if (sourceElement != null) {
            wkUri = getNodeValue(sourceElement, "uri");
            wkFileName = getNodeValue(sourceElement, "filename");
        }
        Element keyElement = (Element) doc.getElementsByTagName("key").item(0);
        if (keyElement != null) {
            wkKeyColumn = keyElement.getTextContent();
        }

        init(id, wkUri, wkFileName, wkKeyColumn);
    }

    /**
     * alternative Constructor with uri or filename
     * @param ws
     * @param id
     * @param uriString
     * @param fileName (alternative to uri)
     * @param keyColumn
     * @throws URISyntaxException
     */
    public ShapeFileDataStore(JAMSWorkspace ws, String id, String uriString, String fileName, String keyColumn) throws URISyntaxException {
        super(ws);
        init(id, uriString, fileName, keyColumn);
    }

    /**
     * init the shapeFileDataStore
     *
     * @param id
     * @param uriString
     * @param fileName
     * @param keyColumn
     * @throws URISyntaxException
     */
    private void init(String id, String uriString, String fileName, String keyColumn) throws URISyntaxException {
        this.id = id;
        if (!StringTools.isEmptyString(uriString)) {
                this.uri = new URI(uriString);
                this.shapeFile = new File(this.uri);
        }
        if (this.shapeFile == null || !this.shapeFile.exists()) {
            if (!StringTools.isEmptyString(fileName)) {
                this.shapeFile = new File(ws.getLocalInputDirectory(), fileName);
            }
        }
        if (shapeFile == null) {
            this.shapeFile = new File(ws.getLocalInputDirectory(), id + ".shp");
        }
        if (this.shapeFile.exists()) {
            ws.getRuntime().println("Trying to use shape file from " + shapeFile.toString() + " ..");
            this.uri = this.shapeFile.toURI();
            this.fileName = this.shapeFile.getName();
            if (keyColumn != null) {
                this.keyColumn = keyColumn;
            }
        } else {
            ws.getRuntime().println("No shape file found for shape datastore \"" + id + "\" ..");
        }
    }

    public Document getDocument() throws Exception {

        String xmlString = "<shapefiledatastore>";
        xmlString += "<source>";
        xmlString += "<uri>";
        String uriString = this.uri.toASCIIString();
        if (!StringTools.isEmptyString(uriString)) {
            xmlString += uriString;
        }
        xmlString += "</uri>";
        xmlString += "<filename>";
        if (!StringTools.isEmptyString(this.fileName)) {
            xmlString += this.fileName;
        }
        xmlString += "</filename>";
        xmlString += "</source>";

        xmlString += "<key>";
        if (!StringTools.isEmptyString(this.keyColumn)) {
            xmlString += this.keyColumn;
        }
        xmlString += "</key>";

        xmlString += "</shapefiledatastore>";
        return XMLTools.getDocumentFromString(xmlString);
    }


    @Override
    public boolean hasNext() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public DefaultDataSet getNext() {
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

    public void setWorkspace(Workspace ws)throws IOException {
        close();
        //todo
    }
}
