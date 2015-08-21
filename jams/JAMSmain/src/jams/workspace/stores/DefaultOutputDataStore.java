/*
 * DefaultOutputDataStore.java
 * Created on 9. September 2008, 22:23
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

import de.odysseus.el.util.SimpleContext;
import de.odysseus.el.util.SimpleResolver;
import jams.io.BufferedFileWriter;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import jams.workspace.JAMSWorkspace;
import java.io.File;
import java.io.IOException;
import jams.model.Context;
import jams.workspace.Workspace;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import javax.el.ExpressionFactory;
import javax.el.ValueExpression;

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
    transient private Workspace ws;
    private int columnsPerLine;
    private int columnCounter;
    private boolean firstRow;
    private File outputFile;

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

    public void setWorkspace(Workspace ws) throws IOException {
        boolean wasOpen = this.writer == null ? false : true;
        this.close();

        this.ws = ws;
        File outputDirectory = ws.getOutputDataDirectory();
        outputDirectory.mkdirs();
        outputFile = new File(outputDirectory.getPath() + File.separator + id + JAMSWorkspace.OUTPUT_FILE_ENDING);

        if (wasOpen) {
            open(true);
        }
    }

    @Override
    public String[] getAttributes() {
        return attributes;
    }

    @Override
    public void open(boolean append) throws IOException {
        File outputDirectory = ws.getOutputDataDirectory();
        outputDirectory.mkdirs();

        outputFile = new File(outputDirectory.getPath() + File.separator + id + JAMSWorkspace.OUTPUT_FILE_ENDING);
        writer = new BufferedFileWriter(new FileOutputStream(outputFile, append));
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
            writer = null;
        }
    }

    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        in.defaultReadObject();        
        long position = in.readLong(); 
        if (this.writer != null) {
            writer.close();
        }
        //this is not ok, because in this case we cannot move the workspace
        //directory .. 
        writer = new BufferedFileWriter(new FileOutputStream(outputFile, true));
        writer.setPosition(position);
    }

    private void writeObject(ObjectOutputStream out) throws IOException {
        out.defaultWriteObject();
        if (writer != null)
            out.writeLong(writer.getPosition());
        else
            out.writeLong((long)0L);
    }

    @Override
    public DefaultFilter[] getFilters() {
        return filters;
    }
    
    public void setFilters(DefaultFilter[] filters) {
        this.filters = filters;
    }

    public boolean isValid() {
        if (outputFile.canWrite()) {
            return true;
        } else {
            return true;
        }
    }

    static public class DefaultFilter implements Filter {

        private String contextName, expression;
        private Context context = null;

        transient ExpressionFactory factory = null;
        transient SimpleContext exprContext = new SimpleContext(new SimpleResolver());
        transient ValueExpression valueExpr = null;        
        transient ValueExpression idExpr = null;
        
        public DefaultFilter(String contextName, String expression) {
            this.contextName = contextName;
            this.expression = "${" + expression + "}";

            java.util.Properties properties = new java.util.Properties();
            properties.put("javax.el.cacheSize", "1000");
            properties.put("javax.el.methodInvocations", "false");
            properties.put("javax.el.nullProperties", "false");
            properties.put("javax.el.varArgs", "false");
            properties.put("javax.el.ignoreReturnType", "false");

            factory = new de.odysseus.el.ExpressionFactoryImpl(properties);

            exprContext = FilterFunctions.getContext();

            valueExpr = factory.createValueExpression(exprContext, this.expression, boolean.class);
            idExpr = factory.createValueExpression(exprContext, "${id}", double.class);           
        }

        @Override
        public String getContextName() {
            return contextName;
        }

        @Override
        public String getExpression() {
            return expression;
        }

        @Override
        public boolean isFiltered(String id) {
            idExpr.setValue(exprContext, id);
            boolean result = (Boolean) valueExpr.getValue(exprContext);
            return result;
        }

        @Override
        public Context getContext() {
            return context;
        }

        @Override
        public void setContext(Context context) {
            this.context = context;
            
            Context searchContext = this.getContext();
            while (searchContext != null) {
                ValueExpression contextExpr = factory.createValueExpression(exprContext, "${"+searchContext.getInstanceName()+"}", Context.class);
                contextExpr.setValue(exprContext, searchContext);
                searchContext = searchContext.getContext();                
            } 
        }
        
        private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
            in.defaultReadObject();
        
            java.util.Properties properties = new java.util.Properties();
            properties.put("javax.el.cacheSize", "1000");
            properties.put("javax.el.methodInvocations", "false");
            properties.put("javax.el.nullProperties", "false");
            properties.put("javax.el.varArgs", "false");
            properties.put("javax.el.ignoreReturnType", "false");
                        
            exprContext = FilterFunctions.getContext();
            factory = new de.odysseus.el.ExpressionFactoryImpl(properties);
            valueExpr = factory.createValueExpression(exprContext, expression, boolean.class);
            idExpr = factory.createValueExpression(exprContext, "${id}", double.class);  
        }
        
        private void writeObject(ObjectOutputStream out) throws IOException {
            out.defaultWriteObject();
        }
    }
}
