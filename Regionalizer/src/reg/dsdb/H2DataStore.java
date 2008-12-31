/*
 * H2DataStore.java
 * Created on 29. Dezember 2008, 19:05
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
package reg.dsdb;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileFilter;
import java.io.FileReader;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.StringTokenizer;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Sven Kralisch <sven.kralisch at uni-jena.de>
 */
public class H2DataStore {

    public static final String DB_USER = "jamsuser",  DB_PASSWORD = "";

    private String fileName;

    private ArrayList<ContextData> contexts = new ArrayList<ContextData>();

    private ArrayList<FilterData> filters = new ArrayList<FilterData>();

    private ArrayList<AttributeData> attributes = new ArrayList<AttributeData>();

    private HashMap<String, String> typeMap = new HashMap<String, String>();

    private BufferedReader reader;

    private Statement stmt;

    private String jdbcURL;

    private Connection conn;

    public H2DataStore(String fileName) {
        this.fileName = fileName;

        // get the db file name
        jdbcURL = "jdbc:h2:" + fileName.substring(0, fileName.lastIndexOf("."));

        try {
            // load the driver
            Class.forName("org.h2.Driver");
        } catch (ClassNotFoundException ex) {
            Logger.getLogger(H2DataStore.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void createDB() {
        typeMap.put("JAMSLong", "INT8");
        typeMap.put("JAMSDouble", "FLOAT8");
        typeMap.put("JAMSString", "VARCHAR(255)");
        typeMap.put("JAMSCalendar", "TIMESTAMP");

        try {
            initDS();
            initDB();
            fillDB();
        } catch (IOException ex) {
            Logger.getLogger(H2DataStore.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ClassNotFoundException ex) {
            Logger.getLogger(H2DataStore.class.getName()).log(Level.SEVERE, null, ex);
        } catch (SQLException ex) {
            Logger.getLogger(H2DataStore.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void removeDB() throws SQLException {
        FileFilter filter = new FileFilter() {

            @Override
            public boolean accept(File pathname) {
                String prefix = new File(fileName.substring(0, fileName.lastIndexOf("."))).getPath();
                if (pathname.getPath().endsWith(".db") && pathname.getPath().startsWith(prefix)) {
                    return true;
                } else {
                    return false;
                }
            }
        };

        this.close();
        File parent = new File(fileName).getParentFile();
        File[] h2Files = parent.listFiles(filter);

        for (File h2File : h2Files) {
            h2File.delete();
        }
    }

    public void close() throws SQLException {
        if ((conn != null) && !conn.isClosed()) {
            conn.close();
        }
    }

    public Connection getH2Connection() throws SQLException {
        if (conn != null) {
            return conn;
        }

        if (existsH2DB()) {
            return DriverManager.getConnection(jdbcURL, DB_USER, DB_PASSWORD);
        } else {
            return null;
        }
    }

    public boolean existsH2DB() {
        String prefix = fileName.substring(0, fileName.lastIndexOf("."));
        File dataFile = new File(prefix + ".data.db");
        File indexFile = new File(prefix + ".index.db");
        if (dataFile.exists() && indexFile.exists()) {
            return true;
        } else {
            return false;
        }
    }

    private void initDB() throws ClassNotFoundException, SQLException {

        // open / create the db
        conn = DriverManager.getConnection(jdbcURL, DB_USER, DB_PASSWORD);

        // get a statement object
        stmt = conn.createStatement();

        // remove data table if exists
        stmt.execute("DROP TABLE IF EXISTS data");

        // build create query
        String q = "CREATE TABLE data (";

        for (int i = contexts.size() - 1; i >= 0; i--) {
            ContextData cd = contexts.get(i);
            q += cd.name + "ID " + typeMap.get(cd.idType) + ",";
        }

        for (int i = 1; i < attributes.size(); i++) {
            AttributeData attribute = attributes.get(i);
            q += attribute.name + " " + typeMap.get(attribute.type) + ",";
        }
        q = q.substring(0, q.length() - 1);

        q += ")";

        // create table
        stmt.execute(q);

        // build index query
        q = "CREATE INDEX dataindex ON data (";
        for (int i = contexts.size() - 1; i >= 0; i--) {
            ContextData cd = contexts.get(i);
            q += cd.name + "ID,";
        }
        q = q.substring(0, q.length() - 1);
        q += ")";

        // create indexes
        stmt.execute(q);
    }

    private void initDS() throws IOException {
        String row;
        StringTokenizer tok;
        reader = new BufferedReader(new FileReader(fileName));

        // @context row
        row = reader.readLine();
        if (!row.equals("@context")) {
            return;
        }
        row = reader.readLine();
        tok = new StringTokenizer(row);
        contexts.add(new ContextData(tok.nextToken(), tok.nextToken(), tok.nextToken(), null));

        // @ancestors row
        row = reader.readLine();
        row = reader.readLine();

        while (!row.equals("@filters")) {
            tok = new StringTokenizer(row);
            contexts.add(new ContextData(tok.nextToken(), tok.nextToken(), tok.nextToken(), null));
            row = reader.readLine();
        }

        row = reader.readLine();

        while (!row.equals("@attributes")) {
            tok = new StringTokenizer(row);
            filters.add(new FilterData(tok.nextToken(), tok.nextToken()));
            row = reader.readLine();
        }

        row = reader.readLine();
        StringTokenizer attributeTokenizer = new StringTokenizer(row);
        row = reader.readLine();
        row = reader.readLine();
        StringTokenizer typeTokenizer = new StringTokenizer(row);

        while (attributeTokenizer.hasMoreTokens() && typeTokenizer.hasMoreTokens()) {
            attributes.add(new AttributeData(typeTokenizer.nextToken(), attributeTokenizer.nextToken()));
        }
    }

    private void fillDB() throws IOException, SQLException {
        String row;

        row = reader.readLine();

        boolean result = fillBlock();
        while (result == true) {
            result = fillBlock();
        }

    }

    private boolean fillBlock() throws IOException, SQLException {
        String row;
        String queryPrefix = "INSERT INTO data VALUES (";

        // read the ancestor's data
        for (int i = contexts.size() - 1; i > 0; i--) {
            ContextData cd = contexts.get(i);
            row = reader.readLine();
            if (row == null) {
                return false;
            }
            StringTokenizer tok = new StringTokenizer(row, "\t");
            tok.nextToken();
            String value = tok.nextToken();
            if (cd.type.endsWith("TemporalContext")) {
                value += ":00";
            }
            queryPrefix += "'" + value + "',";
        }

        row = reader.readLine();
        while (!(row = reader.readLine()).equals("@end")) {

            String q = queryPrefix;

            StringTokenizer tok = new StringTokenizer(row, "\t");
            while (tok.hasMoreTokens()) {
                q += tok.nextToken() + ",";
            }

            q = q.substring(0, q.length() - 1);
            q += ")";

            // insert data into table
            stmt.execute(q);

        //System.out.println(q);
        }
        return true;
    }

    public static void main(String[] args) {
        H2DataStore dsdb = new H2DataStore("D:/jamsapplication/JAMS-Gehlberg/output/current/HRULoop_1.dat");
        //dsdb.createDB();
        //System.out.println(dsdb.getJdbcURL());
        try {
            System.out.println(dsdb.existsH2DB());
            System.out.println(dsdb.getH2Connection());
            dsdb.removeDB();
        } catch (SQLException ex) {
            Logger.getLogger(H2DataStore.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * @return the jdbcURL
     */
    public String getJdbcURL() {
        return jdbcURL;
    }

    private class ContextData {

        String type, name, idType;

        int size;

        public ContextData(String type, String name, String size, String idType) {
            this.type = type;
            this.name = name;
            this.size = Integer.parseInt(size);

            if (idType == null) {
                if (type.equals("jams.model.JAMSTemporalContext")) {
                    this.idType = "JAMSCalendar";
                } else if (type.equals("jams.model.JAMSSpatialContext")) {
                    this.idType = "JAMSLong";
                }
            }
        }
    }

    private class FilterData {

        String regex, contextName;

        public FilterData(String regex, String contextName) {
            this.regex = regex;
            this.contextName = contextName;
        }
    }

    private class AttributeData {

        String type, name;

        public AttributeData(String type, String name) {
            this.type = type;
            this.name = name;
        }
    }
}
