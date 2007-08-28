/*
 * XMLIO.java
 *
 * Created on 10. Juni 2005, 07:47
 *
 * This file is part of JAMS
 * Copyright (C) 2005 FSU Jena
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

package org.unijena.jams.io;

import javax.xml.parsers.*;
import org.xml.sax.*;
import java.io.*;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.w3c.dom.*;

/**
 *
 * @author S. Kralisch
 */

public class XMLIO {
    
    public static Document getDocument(String fileName) throws FileNotFoundException {
        
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        Document document = null;
        
        File file = new File(fileName);
        if (!file.exists()) {
            throw new FileNotFoundException();
        }
        
        try {
            
            DocumentBuilder builder = factory.newDocumentBuilder();
            document = builder.parse(file);
            
        } catch (SAXException sxe) {
            // Error generated during parsing
            Exception  x = sxe;
            if (sxe.getException() != null)
                x = sxe.getException();
            x.printStackTrace();
            
        } catch (ParserConfigurationException pce) {
            // Parser with specified options can't be built
            pce.printStackTrace();
            
        } catch (IOException ioe) {
            // I/O error
            org.unijena.jams.JAMS.handle(ioe);
        }
        
        return document;
    }
    
    public static Document getDocumentFromString(String docString) throws IOException, SAXException {
        
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        Document document = null;
        
        try {
            
            DocumentBuilder builder = factory.newDocumentBuilder();
            InputStream source = new ByteArrayInputStream(docString.getBytes("ISO-8859-1"));
            document = builder.parse(source);
            
        } catch (ParserConfigurationException pce) {
            // Parser with specified options can't be built
            pce.printStackTrace();
            
        }
        
        return document;
    }
    
    public static String getStringFromDocument(Document doc) {
        
        String returnValue = "";
        
        try {
            Transformer xformer = TransformerFactory.newInstance().newTransformer();
            
            xformer.setOutputProperty(OutputKeys.METHOD, "xml");
            xformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");
            xformer.setOutputProperty(OutputKeys.INDENT, "yes");
            xformer.setOutputProperty(OutputKeys.STANDALONE, "yes");
            xformer.setOutputProperty(OutputKeys.ENCODING, "ISO-8859-1");
            
            Source source = new DOMSource(doc);
            StringWriter writer = new StringWriter();
            Result result = new StreamResult(writer);
            xformer.transform(source, result);
            
            returnValue = writer.toString();
            //returnValue = new String(writer.toString().getBytes(), "ISO-8859-1");
            
        } catch (TransformerConfigurationException ex) {
            ex.printStackTrace();
        } catch (TransformerException ex) {
            ex.printStackTrace();
        }
        
        return returnValue;
    }
    
    public static boolean writeXmlFile(Document modelDoc, String filename) throws IOException {
        return writeXmlFile(modelDoc, new File(filename));
    }
    
    public static boolean writeXmlFile(Document modelDoc, File savePath) throws IOException {
        
        if (!savePath.exists()) {
            savePath.createNewFile();
        }
        
        if (savePath.canWrite()) {
            try {
                Transformer transformer = TransformerFactory.newInstance().newTransformer();
                transformer.setOutputProperty(OutputKeys.METHOD, "xml");
                transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");
                transformer.setOutputProperty(OutputKeys.INDENT, "yes");
                transformer.setOutputProperty(OutputKeys.STANDALONE, "yes");
                transformer.setOutputProperty(OutputKeys.ENCODING, "ISO-8859-1");
                
                DOMSource source = new DOMSource(modelDoc);
                FileOutputStream os = new FileOutputStream(savePath);
                StreamResult result = new StreamResult(os);
                transformer.transform(source, result);
                os.close();
                
            } catch (TransformerConfigurationException tce) {
                return false;
            } catch (TransformerException te) {
                return false;
            }
        } else {
            return false;
        }
        return true;
    }
    
}
