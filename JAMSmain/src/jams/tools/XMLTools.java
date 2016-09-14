/*
 * XMLTools.java
 *
 * Created on 10. Juni 2005, 07:47
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
package jams.tools;

import jams.JAMS;
import jams.JAMSException;
import javax.xml.parsers.*;
import org.xml.sax.*;
import java.io.*;
import java.util.Properties;
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
     *
     * @param fileName The name of the file containing the document
     * @return The XML document
     * @throws java.io.FileNotFoundException
     */
    public static Document getDocument(String fileName) {

        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true);
        Document document = null;

        File file = new File(fileName);
        if (!file.exists()) {
            throw new JAMSException(JAMS.i18n("File_") + fileName + JAMS.i18n("_does_not_exist!"), JAMS.i18n("File_open_error"));
        }

        try {
            /*byte[] buffer = new byte[(int)file.length()];
            BufferedInputStream f = new BufferedInputStream(new FileInputStream(file));
            f.read(buffer);*/
            DocumentBuilder builder = factory.newDocumentBuilder();
            document = builder.parse(file);

        } catch (SAXException sxe) {
            // Error generated during parsing
            throw new JAMSException(sxe.getMessage(), sxe);

        } catch (ParserConfigurationException pce) {
            // Parser with specified options can't be built
            throw new JAMSException(pce.getMessage(), pce);

        } catch (IOException ioe) {
            // I/O error
            throw new JAMSException(ioe.getMessage(), ioe);
        }

        return document;
    }

    /**
     * Creates an XML document from a string object
     *
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
            InputStream source = new ByteArrayInputStream(docString.getBytes(JAMS.getCharset()));
            document = builder.parse(source);

        } catch (ParserConfigurationException pce) {
            // Parser with specified options can't be built
            pce.printStackTrace();

        }

        return document;
    }

    /**
     * Creates an empty XML document
     *
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
     *
     * @param doc The XML document
     * @return A string reoresenting the XML document
     */
    public static String getStringFromDocument(Document doc) {
        if (doc == null) {
            return "";
        }
        return xmlSerializerSun(doc);
    }

    public static String getStringFromNode(Node doc) {
        if (doc == null) {
            return "";
        }
        return xmlSerializerSun(doc);
    }

    private static String xmlSerializerSun(Node doc) {

        String returnValue = "";

        try {
            Transformer transformer = getTransformer(true);

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
     *
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
                Transformer transformer = getTransformer(false);

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

    public static InputStream writeXmlToStream(Document doc) throws IOException {
        try {
            Transformer transformer = getTransformer(false);

            DOMSource source = new DOMSource(doc);
            ByteArrayOutputStream os = new ByteArrayOutputStream();
            StreamResult result = new StreamResult(os);
            transformer.transform(source, result);
            os.close();
            byte[] buffer = os.toByteArray();
            ByteArrayInputStream in = new ByteArrayInputStream(buffer);
            return in;
        } catch (TransformerConfigurationException tce) {
            return null;
        } catch (TransformerException te) {
            return null;
        }
    }

    public static InputStream propertiesToStream(Properties p) throws IOException {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        p.store(output, null);
        return new ByteArrayInputStream(output.toByteArray());
    }

    private static Transformer getTransformer(boolean omitXML) throws TransformerConfigurationException {

        TransformerFactory factory = TransformerFactory.newInstance();

        Transformer transformer = factory.newTransformer();
        transformer.setOutputProperty(OutputKeys.METHOD, "xml");
        transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");
        transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, omitXML ? "yes" : "no");
        transformer.setOutputProperty(OutputKeys.INDENT, "yes");
        transformer.setOutputProperty(OutputKeys.STANDALONE, "yes");
        transformer.setOutputProperty(OutputKeys.ENCODING, JAMS.getCharset());

        transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "no");
        if (omitXML) {
            transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
            transformer.setOutputProperty(OutputKeys.STANDALONE, "yes");
        }
        transformer.setOutputProperty(OutputKeys.ENCODING, JAMS.getCharset());
        return transformer;
    }

}
