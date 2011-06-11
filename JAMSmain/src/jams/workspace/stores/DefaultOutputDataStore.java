/*
 * DefaultOutputDataStore.java
 * Created on 9. September 2008, 22:23
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

import jams.io.BufferedFileWriter;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import jams.workspace.JAMSWorkspace;
import java.io.File;
import java.io.IOException;
import java.util.regex.Pattern;
import jams.model.Context;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

/**
 *
 * @author Sven Kralisch <sven.kralisch at uni-jena.de>
 */
public class DefaultOutputDataStore implements OutputDataStore {

    private static final String TRACE_STRING = "attribute";
    private static final String FILTER_STRING = "filter";
    private static final String CONTEXT_STRING = "context";
    private static final String EXPRESSION_STRING = "expression";
    private static final String ATTRIBUTE_STRING = "id";
    private String id;
    private String[] attributes;
    private DefaultFilter[] filters;
    transient private BufferedFileWriter writer;
    transient private JAMSWorkspace ws;
    private int columnsPerLine;
    private int columnCounter;
    private boolean firstRow;
    private File outputFile;
    private String fileName;
    
    public DefaultOutputDataStore(JAMSWorkspace ws, Document doc, String id) {

        this.id = id;
        this.ws = ws;

        Element root = doc.getDocumentElement();

        NodeList traceNodes = root.getElementsByTagName(TRACE_STRING);
        int length = traceNodes.getLength();
        attributes = new String[length];
        for (int i = 0; i < length; i++) {
            Element traceElement = (Element) traceNodes.item(i);
            attributes[i] = traceElement.getAttribute(ATTRIBUTE_STRING);
        }

        NodeList filterNodes = root.getElementsByTagName(FILTER_STRING);
        length = filterNodes.getLength();
        filters = new DefaultFilter[length];
        for (int i = 0; i < length; i++) {
            Element filterElement = (Element) filterNodes.item(i);
            filters[i] = new DefaultFilter(filterElement.getAttribute(CONTEXT_STRING),
                    filterElement.getAttribute(EXPRESSION_STRING));
        }
        firstRow = true;
        columnsPerLine = 0;
        columnCounter = 0;

        File outputDirectory = ws.getOutputDataDirectory();
        outputDirectory.mkdirs();

        outputFile = new File(outputDirectory.getPath() + File.separator + id + JAMSWorkspace.OUTPUT_FILE_ENDING);

    }

    public String getID() {
        return id;
    }

    public String[] getAttributes() {
        return attributes;
    }

    public void open(boolean append) throws IOException {
        File outputDirectory = ws.getOutputDataDirectory();
        outputDirectory.mkdirs();

        outputFile = new File(outputDirectory.getPath() + File.separator + id + JAMSWorkspace.OUTPUT_FILE_ENDING);
        writer = new BufferedFileWriter(new FileOutputStream(outputFile,append));
    }

    public void write(Object o) throws IOException {
        writer.write(o.toString());
    }

    public void writeCell(Object o) throws IOException {
        columnCounter++;
        writer.write(o.toString() + "\t");
    }

    public void nextRow() throws IOException {
        if (firstRow) {
            columnsPerLine = columnCounter;
            firstRow = false;
        } else {
            if (columnsPerLine > columnCounter) {
                System.err.println("DefaultOutputDataStore:row not complete, one or more attributes are missing");
            }
            if (columnsPerLine < columnCounter) {
                System.err.println("DefaultOutputDataStore:too many attributes in row");
            }
        }
        columnCounter = 0;
        writer.write("\n");
    }

    public void flush() throws IOException {
        writer.flush();
    }

    public void close() throws IOException {
        if (writer != null) {
            writer.close();
        }
    }

    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException{
        in.defaultReadObject();
        long position = in.readLong();
        if (this.writer!=null){
            writer.close();
        }
        //this is not ok, because in this case we cannot move the workspace
        //directory .. 
        writer = new BufferedFileWriter(new FileOutputStream(outputFile,true));
        writer.setPosition(position);
    }

    private void writeObject(ObjectOutputStream out) throws IOException{
        out.defaultWriteObject();
        out.writeLong(writer.getPosition());
    }
                    
    public DefaultFilter[] getFilters() {
        return filters;
    }

    public boolean isValid() {
        if (outputFile.canWrite()) {
            return true;
        } else {
            return true;
        }
    }

    public class DefaultFilter implements Filter {

        private String contextName, expression;
        private Pattern pattern = null;
        private Context context = null;

        public DefaultFilter(String contextName, String expression) {
            this.contextName = contextName;
            this.expression = expression;
        }

        public String getContextName() {
            return contextName;
        }

        public String getExpression() {
            return expression;
        }

        public Pattern getPattern() {
            return pattern;
        }

        public void setPattern(Pattern pattern) {
            this.pattern = pattern;
        }

        public Context getContext() {
            return context;
        }

        public void setContext(Context context) {
            this.context = context;
        }
    }
}
