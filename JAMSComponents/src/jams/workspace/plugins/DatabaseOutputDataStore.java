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
package jams.workspace.plugins;

import jams.workspace.stores.*;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import jams.workspace.JAMSWorkspace;
import java.io.IOException;
import java.util.regex.Pattern;
import jams.model.Context;
import java.sql.SQLException;
import java.util.ArrayList;

/**
 *
 * @author Sven Kralisch <sven.kralisch at uni-jena.de>
 */
public class DatabaseOutputDataStore implements OutputDataStore {

    private static final String TRACE_STRING = "attribute";
    private static final String FILTER_STRING = "filter";
    private static final String CONTEXT_STRING = "context";
    private static final String EXPRESSION_STRING = "expression";
    private static final String ATTRIBUTE_STRING = "id";
    private String id;
    private String[] attributes;
    private DefaultFilter[] filters;
    private JAMSWorkspace ws;
    private String user, password, host, db, driver;
    private String genericSqlStatement;
    private boolean cleanedup = false;
    private boolean dataStarted;
    private JdbcSQLConnector pgsql;
    ArrayList<String> lineArray = new ArrayList<String>(20);

    public void setUser(String user) {
        this.user = user;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public void setPassword(String pw) {
        this.password = pw;
    }

    public void setDb(String db) {
        this.db = db;
    }

    public void setQuery(String query) {
        this.genericSqlStatement = query;
    }

    public void setDriver(String driver) {
        this.driver = driver;
    }

    public void setID(String id) {
        this.id = id;
    }

    public void setWorkspace(JAMSWorkspace ws) {
        this.ws = ws;
    }

    public void setDoc(Document doc) {
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
    }

    public String getID() {
        return id;
    }

    public String[] getAttributes() {
        return attributes;
    }

    public void open(boolean append) throws IOException {
        if (host == null) {
            throw new IOException("unknown host");
        }
        if (db == null) {
            throw new IOException("unknown database");
        }
        if (user == null) {
            throw new IOException("unknown user");
        }
        if (password == null) {
            throw new IOException("unknown password");
        }
        if (driver == null) {
            throw new IOException("unknown driver");
        }
        if (this.genericSqlStatement == null) {
            throw new IOException("unknown sql query");
        }

        pgsql = new JdbcSQLConnector(host, db, user, password, driver);
        try {
            dataStarted = false;
            pgsql.connect();
        } catch (SQLException sqlex) {
            System.err.println("DatabaseOutputDataStore: " + sqlex);
            sqlex.printStackTrace();
            throw new IOException(sqlex.toString());
        }
        dataStarted = false;        
    }

    public void write(Object o) throws IOException {
        if (o.toString().contains("@start")){
            dataStarted = true;
        }
    }

    public void writeCell(Object o) {
        if (o.toString().contains("@start")) {
            dataStarted = true;
        } else if (dataStarted) {
            lineArray.add(o.toString());
        }
    }

    public void nextRow() throws IOException {
        String sqlStatement = genericSqlStatement;
        for (int i = 1; i <= this.lineArray.size(); i++) {
            sqlStatement = sqlStatement.replace("#" + i, "\"" + lineArray.get(i-1) + "\"");
        }
        try {
            if (!sqlStatement.contains("#")) {
                pgsql.execUpdate(sqlStatement);
            } else {
                System.err.println("DatabaseOutputDataStore: skip line, because not all attributes are known");
            }
        } catch (SQLException sqlex) {
            System.err.println("DatabaseOutputDataStore: " + sqlex);
            sqlex.printStackTrace();
        }
        lineArray.clear();
    }

    public void flush() throws IOException {
    }

    public void close() throws IOException {        
        try {
            if (pgsql != null) {
                pgsql.close();
            }
            pgsql = null;
        } catch (SQLException sqlex) {
            System.out.println("DatabaseOutputDataStore: " + sqlex);
            sqlex.printStackTrace();
            return;
        }

        return;
    }

    public DefaultFilter[] getFilters() {
        return filters;
    }
//todo
    public void setState(DataStoreState state){
        
    }
    
    //todo
    public DataStoreState getState(){
        return null;
    }
    
    public boolean isValid() {
        try{
            if (pgsql==null){
                try{
                    open(false);
                }catch(IOException e){
                    return false;
                }
            }
            return pgsql.isValid();
        }catch (SQLException e){
            return false;
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
