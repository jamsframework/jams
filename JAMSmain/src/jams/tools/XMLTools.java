/*
 * XMLTools.java
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

package jams.tools;

import jams.JAMS;
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

public class XMLTools {

    /**
     * Reads a XML document from a file and returns it
     * @param fileName The name of the file containing the document
     * @return The XML document
     * @throws java.io.FileNotFoundException
     */
    public static Document getDocument(String fileName) throws FileNotFoundException {
        
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true);
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
            JAMSTools.handle(sxe);
            
        } catch (ParserConfigurationException pce) {
            // Parser with specified options can't be built
            JAMSTools.handle(pce);
            
        } catch (IOException ioe) {
            // I/O error
            JAMSTools.handle(ioe);
        }
        
        return document;
    }
    
    /**
     * Creates an XML document from a string object
     * @param docString The string representing the document
     * @return The XML document
     * @throws java.io.IOException
     * @throws org.xml.sax.SAXException
     */
    public static Document getDocumentFromString(String docString) throws IOException, SAXException {
        
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true);
        Document document = null;
        
        try {
            
            DocumentBuilder builder = factory.newDocumentBuilder();
            InputStream source = new ByteArrayInputStream(docString.getBytes(JAMS.charset));
            document = builder.parse(source);
            
        } catch (ParserConfigurationException pce) {
            // Parser with specified options can't be built
            pce.printStackTrace();
            
        }
        
        return document;
    }

    /**
     * Creates an empty XML document
     * @return An XML document
     */
    public static Document createDocument() {
        Document document = null;
        try {
            document = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
        } catch (ParserConfigurationException ex) {
            ex.printStackTrace();
        }
        return document;
    }
    
    /**
     * Creates a string representation from a XML document
     * @param doc The XML document
     * @return A string reoresenting the XML document
     */
    public static String getStringFromDocument(Document doc) {
        if (doc == null) {
            return "";
        }
        return xmlSerializerSun(doc);
    }
    
    private static String xmlSerializerSun(Document doc) {
        
        String returnValue = "";
        
        try {
            Transformer transformer = getTransformer();
            
            Source source = new DOMSource(doc);
            StringWriter writer = new StringWriter();
            Result result = new StreamResult(writer);
            transformer.transform(source, result);
            
            returnValue = writer.toString();
            
        } catch (TransformerConfigurationException ex) {
            ex.printStackTrace();
        } catch (TransformerException ex) {
            System.out.println(ex.getMessage());
            ex.printStackTrace();
        }
        
        return returnValue;
    }
    
    /*
    private static String xmlSerializerXerces(Document doc) {
        OutputFormat format = new OutputFormat(doc);
        format.setLineWidth(400);
        format.setIndenting(true);
        format.setIndent(4);
        StringWriter writer = new StringWriter();
        XMLSerializer serializer = new XMLSerializer(writer, format);
        try {
            serializer.serialize(doc);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        return writer.toString();
    }
     */
    
    /**
     * Writes a XML document to a file
     * @param modelDoc The XML document
     * @param filename The name of the file to be written
     * @return true if everything went fine, false otherwise
     * @throws java.io.IOException
     */
    public static boolean writeXmlFile(Document modelDoc, String filename) throws IOException {
        return writeXmlFile(modelDoc, new File(filename));
    }
    
    public static boolean writeXmlFile(Document doc, File savePath) throws IOException {
        
        if (!savePath.exists()) {
            savePath.createNewFile();
        }
        
        if (savePath.canWrite()) {
            
            try {
                Transformer transformer = getTransformer();
                
                DOMSource source = new DOMSource(doc);
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
    
    private static Transformer getTransformer() throws TransformerConfigurationException {
        
        TransformerFactory factory = TransformerFactory.newInstance();
        factory.setAttribute("indent-number", new Integer(4));
        Transformer transformer = factory.newTransformer();
        transformer.setOutputProperty(OutputKeys.METHOD, "xml");
//        transformer.setOutputProperty("{http://xml.apache.org/xalan}indent-amount", "4");
        transformer.setOutputProperty(OutputKeys.INDENT, "yes");
        transformer.setOutputProperty(OutputKeys.STANDALONE, "yes");
        transformer.setOutputProperty(OutputKeys.ENCODING, JAMS.charset);
        
        return transformer;
    }
    
}
