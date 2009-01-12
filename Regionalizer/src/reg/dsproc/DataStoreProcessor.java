/*
 * DataStoreProcessor.java
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
package reg.dsproc;

import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Observable;
import java.util.Observer;
import java.util.StringTokenizer;
import java.util.logging.Level;
import java.util.logging.Logger;
import reg.dsdb.H2DataStore;

/**
 *
 * @author Sven Kralisch <sven.kralisch at uni-jena.de>
 */
public class DataStoreProcessor {

    public static final String DB_USER = "jamsuser",  DB_PASSWORD = "";

    private String fileName;

    private ArrayList<ContextData> contexts = new ArrayList<ContextData>();

    private ArrayList<FilterData> filters = new ArrayList<FilterData>();

    private ArrayList<AttributeData> attributes = new ArrayList<AttributeData>();

    private HashMap<String, String> typeMap = new HashMap<String, String>();

    private BufferedFileReader reader;

    private int overallSize;

    private String jdbcURL;

    private Connection conn;

    private Statement stmt;

    private ImportProgressObservable importProgressObservable = new ImportProgressObservable();

    public DataStoreProcessor(String fileName) {
        this.fileName = fileName;

        try {
            initDS();
        } catch (IOException ex) {
            Logger.getLogger(DataStoreProcessor.class.getName()).log(Level.SEVERE, null, ex);
        }

        jdbcURL = "jdbc:h2:" + fileName.substring(0, fileName.lastIndexOf(".")) + ";LOG=0";

    }

    public void createDB() throws IOException, SQLException, ClassNotFoundException {
        typeMap.put("JAMSLong", "INT8");
        typeMap.put("JAMSDouble", "FLOAT8");
        typeMap.put("JAMSString", "VARCHAR(255)");
        typeMap.put("JAMSCalendar", "TIMESTAMP");

        removeDB();
        Class.forName("org.h2.Driver");
        initDB();
        createIndex();
    }

    public void removeDB() throws SQLException {
        FileFilter filter = new FileFilter() {

            @Override
            public boolean accept(File pathname) {
                String prefix = new File(getFileName().substring(0, getFileName().lastIndexOf("."))).getPath();
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

    public Connection getH2Connection(boolean checkForDB) throws SQLException {
        if (conn != null) {
            return conn;
        }

        if (!checkForDB) {
            return DriverManager.getConnection(jdbcURL, DB_USER, DB_PASSWORD);
        }

        if (existsH2DB()) {
            return DriverManager.getConnection(jdbcURL, DB_USER, DB_PASSWORD);
        } else {
            return null;
        }
    }

    private void initDB() throws SQLException {

        // open/create the db
        conn = getH2Connection(false);

        // get a statement object
        stmt = conn.createStatement();

        // set some options
        //stmt.execute("SET LOG 0");
        //stmt.execute("SET WRITE_DELAY 10000");
        //stmt.execute("SET MAX_OPERATION_MEMORY 0");
        //stmt.execute("SET EXCLUSIVE FALSE");
        //stmt.execute("SET DEFAULT_TABLE_TYPE MEMORY");

        // remove data table if exists
        stmt.execute("DROP TABLE IF EXISTS data");

        // build create query
        String q = "CREATE TABLE data (";

        for (int i = contexts.size() - 1; i > 0; i--) {
            ContextData cd = contexts.get(i);
            q += cd.getName() + "ID " + typeMap.get(cd.getIdType()) + ",";
        }

        q += "position " + typeMap.get("JAMSLong") + ")";

        // create table
        stmt.execute(q);

        // create indexes
        for (int i = contexts.size() - 1; i > 0; i--) {
            ContextData cd = contexts.get(i);
            q = "CREATE INDEX " + cd.getName() + "_index ON data (" + cd.getName() + "ID)";
            stmt.execute(q);
        }
    }

    private void initDS() throws IOException {
        String row;
        StringTokenizer tok;
        reader = new BufferedFileReader(new FileInputStream(new File(fileName)));

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

        overallSize = 0;
        while (!row.equals("@filters")) {
            tok = new StringTokenizer(row);
            ContextData cd = new ContextData(tok.nextToken(), tok.nextToken(), tok.nextToken(), null);
            contexts.add(cd);
            overallSize += cd.getSize();
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

        // throw away the first, ID token
        typeTokenizer.nextToken();
        attributeTokenizer.nextToken();

        while (attributeTokenizer.hasMoreTokens() && typeTokenizer.hasMoreTokens()) {
            attributes.add(new AttributeData(typeTokenizer.nextToken(), attributeTokenizer.nextToken()));
        }
    }

    private void createIndex() throws IOException, SQLException {

        reader.readLine();

        boolean result = fillBlock();
        while (result) {
            result = fillBlock();
        }

    }

    private boolean fillBlock() throws IOException, SQLException {
        String row;
        String query = "INSERT INTO data VALUES (";

        long position = reader.getPosition();

        // read the ancestor's data
        row = reader.readLine();
        if (row == null) {
            return false;
        }
        for (int i = contexts.size() - 1; i > 0; i--) {
            ContextData cd = contexts.get(i);

            StringTokenizer tok = new StringTokenizer(row, "\t");
            tok.nextToken();
            String value = tok.nextToken();
            if (cd.getType().endsWith("TemporalContext")) {
                value += ":00";
            }
            query += "'" + value + "',";
            row = reader.readLine();
        }
        query += "'" + position + "')";

        while (!(row = reader.readLine()).equals("@end")) {
        }

        stmt.execute(query);

        return true;
    }

    public static void main(String[] args) throws SQLException, ClassNotFoundException, IOException {
//        DataStoreProcessor dsdb = new DataStoreProcessor("D:/jamsapplication/JAMS-Gehlberg/output/current/HRULoop_0.dat");
        DataStoreProcessor dsdb = new DataStoreProcessor("D:/jamsapplication/JAMS-Gehlberg/output/current/TimeLoop.dat");
        dsdb.addImportProgressObserver(new Observer() {

            public void update(Observable o, Object arg) {
                System.out.println("Progress: " + arg);
            }
        });

        dsdb.createDB();
        dsdb.createIndex();

    }

    /**
     * @return the contexts
     */
    public ArrayList<ContextData> getContexts() {
        return contexts;
    }

    /**
     * @return the filters
     */
    public ArrayList<FilterData> getFilters() {
        return filters;
    }

    /**
     * @return the attributes
     */
    public ArrayList<AttributeData> getAttributes() {
        return attributes;
    }

    /**
     * @return the fileName
     */
    public String getFileName() {
        return fileName;
    }

    public void addImportProgressObserver(Observer o) {
        importProgressObservable.addObserver(o);
    }

    public int getSize() {
        int size = 1;
        for (ContextData cd : contexts) {
            size *= cd.getSize();
        }
        return size;
    }

    public class ContextData {

        private String type;

        private String name;

        private String idType;

        private int size;

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

        /**
         * @return the type
         */
        public String getType() {
            return type;
        }

        /**
         * @return the name
         */
        public String getName() {
            return name;
        }

        /**
         * @return the idType
         */
        public String getIdType() {
            return idType;
        }

        /**
         * @return the size
         */
        public int getSize() {
            return size;
        }
    }

    public class FilterData {

        private String regex;

        private String contextName;

        public FilterData(String regex, String contextName) {
            this.regex = regex;
            this.contextName = contextName;
        }

        /**
         * @return the regex
         */
        public String getRegex() {
            return regex;
        }

        /**
         * @return the contextName
         */
        public String getContextName() {
            return contextName;
        }
    }

    public class AttributeData {

        private String type;

        private String name;

        private boolean selected;

        public AttributeData(String type, String name) {
            this.type = type;
            this.name = name;
            this.selected = true;
        }

        /**
         * @return the type
         */
        public String getType() {
            return type;
        }

        /**
         * @return the name
         */
        public String getName() {
            return name;
        }

        /**
         * @return the active
         */
        public boolean isSelected() {
            return selected;
        }

        /**
         * @param active the active to set
         */
        public void setSelected(boolean active) {
            this.selected = active;
        }
    }

    private class ImportProgressObservable extends Observable {

        private int progress;

        private void setProgress(int progress) {
            this.progress = progress;
            this.setChanged();
            this.notifyObservers(progress);
        }
    }
}
