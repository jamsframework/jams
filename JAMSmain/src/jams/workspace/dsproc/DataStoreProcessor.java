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
package jams.workspace.dsproc;

import jams.data.JAMSCalendar;
import jams.io.BufferedFileReader;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.NoSuchElementException;
import java.util.Observable;
import java.util.Observer;
import java.util.StringTokenizer;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Sven Kralisch <sven.kralisch at uni-jena.de>
 */
public class DataStoreProcessor {

    public static final HashMap<String, String> TYPE_MAP = getTypeMap();
    public static final String DB_USER = "jamsuser", DB_PASSWORD = "";
    private File dsFile;
    private ArrayList<ContextData> contexts = new ArrayList<ContextData>();
    private ArrayList<FilterData> filters = new ArrayList<FilterData>();
    private ArrayList<AttributeData> attributes = new ArrayList<AttributeData>();
    private BufferedFileReader reader;
    private int overallSize;
    private String jdbcURL;
    private Connection conn;
    private Statement stmt;
    private ImportProgressObservable importProgressObservable = new ImportProgressObservable();
    private boolean cancelCreateIndex = false;
    public static final int UnsupportedDataStore = 0;
    public static final int TimeSpaceDataStore = 1;
    public static final int EnsembleTimeSeriesDataStore = 2;
    public static final int SimpleDataSerieDataStore = 3;
    public static final int SimpleTimeSerieDataStore = 4;
    public static final int SimpleEnsembleDataStore = 5;
    private PreparedStatement pIndexInsertStmt;

    public DataStoreProcessor(File dsFile) {
        this.dsFile = dsFile;

        try {
            initDS();
        } catch (IOException ex) {
            Logger.getLogger(DataStoreProcessor.class.getName()).log(Level.SEVERE, null, ex);
        }

        // use a lot (100MB) of cache to avoid output of data to file which  
        // causes errorneous handling of timestamps
        jdbcURL = "jdbc:h2:mem:" + dsFile.toString().substring(0, dsFile.toString().lastIndexOf(".")) + ";LOG=0";

    }

    public static int getDataStoreType(File file) {
        DataStoreProcessor dsdb = new DataStoreProcessor(file);

        try {
            //if (!dsdb.existsH2DB()) {
            if (dsdb.isTimeSpaceDatastore()) {
                return TimeSpaceDataStore;
            }
            if (dsdb.isSimpleDataSerieDatastore()) {
                return SimpleDataSerieDataStore;
            }
            if (dsdb.isSimpleTimeSerieDatastore()) {
                return SimpleTimeSerieDataStore;
            }
            if (dsdb.isEnsembleTimeSeriesDatastore()) {
                return EnsembleTimeSeriesDataStore;
            }
            if (dsdb.isSimpleEnsembleDatastore()) {
                return SimpleEnsembleDataStore;
            }
            return UnsupportedDataStore;
            //}
        } catch (Exception e) {
            Logger.getLogger(DataStoreProcessor.class.getName()).log(Level.SEVERE, null, e);
        }
        return UnsupportedDataStore;
    }

    public synchronized void createDB() throws IOException, SQLException, ClassNotFoundException {
        Class.forName("org.h2.Driver");
        clearDB();
        initTables();
        createIndex();
    }

    private static HashMap<String, String> getTypeMap() {
        HashMap<String, String> result = new HashMap<String, String>();
        result.put("JAMSInteger", "INT4");
        result.put("JAMSLong", "INT8");
        result.put("JAMSFloat", "FLOAT4");
        result.put("JAMSDouble", "FLOAT8");
        result.put("JAMSString", "VARCHAR(255)");
        result.put("JAMSCalendar", "TIMESTAMP");
        return result;
    }

    public void clearDB() throws SQLException {

        if ((conn == null) || (conn.isClosed())) {
            // open/create the db
            conn = getH2Connection(false);
        }

        // get a statement object
        stmt = conn.createStatement();

        // remove index table if exists
        ResultSet rs = stmt.executeQuery("SELECT * FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_SCHEMA='PUBLIC'");
        ArrayList<String> tables = new ArrayList<String>();
        while (rs.next()) {
            tables.add(rs.getString("TABLE_NAME"));

        }
        rs.close();

        for (String table : tables) {
            stmt.execute("DROP TABLE IF EXISTS " + table);
        }
    }

    public void removeDBFiles() {
        FileFilter filter = new FileFilter() {

            @Override
            public boolean accept(File pathname) {
                String prefix = new File(dsFile.toString().substring(0, dsFile.toString().lastIndexOf("."))).getPath();
                if (pathname.getPath().endsWith(".db") && pathname.getPath().startsWith(prefix)) {
                    return true;
                } else {
                    return false;
                }
            }
        };

        File parent = dsFile.getParentFile();
        File[] h2Files = parent.listFiles(filter);

        for (File h2File : h2Files) {
            boolean result = h2File.delete();
            Logger.getLogger(DataStoreProcessor.class.getName()).log(Level.INFO, "Trying to delete {0}: {1}", new Object[]{h2File.getPath(), result});
        }
    }

    public void close() throws SQLException {
        if ((conn != null) && !conn.isClosed()) {
            Logger.getLogger(DataStoreProcessor.class.getName()).log(Level.INFO, "Closing database connection");
            conn.close();
            //conn = null;
        }
    }
    
    public boolean existsH2DB() throws SQLException {

        if ((conn == null) || (conn.isClosed())) {
            // open/create the db
            conn = getH2Connection(false);
        }

        // get a statement object
        stmt = conn.createStatement();

        // remove index table if exists
        ResultSet rs = stmt.executeQuery("SELECT count(*) FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_NAME='INDEX' OR TABLE_NAME='DATA'");
        rs.next();
        int count = rs.getInt(1);
        rs.close();

        if (count != 2) {
            return false;
        } else {
            return true;
        }
    }

    public boolean isDBObsolete() {

        //@TODO
        //needs to be reimplemented to recognise changes in dat files
        return true;
    }

    public boolean existsH2DBFiles() {
        String prefix = dsFile.toString().substring(0, dsFile.toString().lastIndexOf("."));
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

        if (!checkForDB || existsH2DBFiles()) {
            // check of the DB has correct version
            try {
                conn = DriverManager.getConnection(jdbcURL, DB_USER, DB_PASSWORD);
            } catch (org.h2.jdbc.JdbcSQLException ex) {
                File[] children = dsFile.getParentFile().listFiles(new FileFilter() {

                    @Override
                    public boolean accept(File pathname) {
                        if (pathname.getPath().endsWith("db")) {
                            return true;
                        } else {
                            return false;
                        }
                    }
                });
                for (File child : children) {
                    child.delete();
                }
                try {
                    conn = DriverManager.getConnection(jdbcURL, DB_USER, DB_PASSWORD);
                } catch (org.h2.jdbc.JdbcSQLException ex2) {
                    return null;
                }                
            }
            return conn;
        } else {
            return null;
        }
    }

    private void initTables() throws SQLException {

        // open/create the db
        conn = getH2Connection(false);

        // get a statement object
        stmt = conn.createStatement();
        
//        System.out.println("SET CACHE_SIZE 999000");
//        stmt.execute("SET CACHE_SIZE 999000");

        // set some options
        //stmt.execute("SET LOG 0");
        //stmt.execute("SET WRITE_DELAY 10000");
        //stmt.execute("SET MAX_OPERATION_MEMORY 0");
        //stmt.execute("SET EXCLUSIVE FALSE");
        //stmt.execute("SET DEFAULT_TABLE_TYPE MEMORY");

        /*
         * Build index table
         */
        // remove index table if exists
        stmt.execute("DROP TABLE IF EXISTS index");

        // build create query
        String q = "CREATE TABLE index (";

        for (int i = contexts.size() - 1; i > 0; i--) {
            ContextData cd = contexts.get(i);
            q += cd.getName() + "ID " + TYPE_MAP.get(cd.getIdType()) + ",";
        }

        q += "position " + TYPE_MAP.get("JAMSLong") + ")";

        // create table
        stmt.execute(q);

        // create indexes
        for (int i = contexts.size() - 1; i > 0; i--) {
            ContextData cd = contexts.get(i);
            q = "CREATE INDEX " + cd.getName() + "_index ON index (" + cd.getName() + "ID)";
            stmt.execute(q);
        }


        /*
         * Build data table
         */
        // remove data table if exists
        stmt.execute("DROP TABLE IF EXISTS data");

        // build create query
        q = "CREATE TABLE data (";

        for (int i = contexts.size() - 1; i >= 0; i--) {
            ContextData cd = contexts.get(i);
            q += cd.getName() + "ID " + TYPE_MAP.get(cd.getIdType()) + ",";
        }

        for (int i = 0; i < attributes.size(); i++) {
            AttributeData attribute = attributes.get(i);
            String dataType = TYPE_MAP.get(attribute.getType());
            if (dataType == null){
                throw new SQLException("unsupported attribute type:" + attribute.getType());
            }
            q += attribute.getName() + " " + dataType + ",";
        }
        q = q.substring(0, q.length() - 1);

        q += ")";

        // create table
        stmt.execute(q);

        // create indexes
        for (int i = contexts.size() - 1; i >= 0; i--) {
            ContextData cd = contexts.get(i);
            q = "CREATE INDEX " + cd.getName() + "_data ON data (" + cd.getName() + "ID)";
            stmt.execute(q);
        }
    }

    private void initDS() throws IOException {
        String row;
        StringTokenizer tok;
        reader = new BufferedFileReader(new FileInputStream(dsFile));

        // @context row
        row = reader.readLine();
        if ((row == null) || !row.equals("@context")) {
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

    private synchronized void createIndex() throws IOException, SQLException {

        float counter = 0;
        int percent = 0;

        reader = new BufferedFileReader(new FileInputStream(dsFile));

        while (!reader.readLine().equals("@data")) {
        }

        String indexInsert = "INSERT INTO index VALUES (";
        for (int i = contexts.size() - 1; i > 0; i--) {
            indexInsert += "?,";
        }        
        indexInsert += "?)";
        pIndexInsertStmt = conn.prepareStatement(indexInsert);
        
        boolean result = parseBlock();
        while (result) {

            if (cancelCreateIndex) {
                clearDB();
                return;
            }

            result = parseBlock();

            counter++;
            int current = Math.round((counter / overallSize) * 100);
            if (current > percent) {
                percent = current;
                importProgressObservable.setProgress(percent);
            }
        }
    }

    public void cancelCreateIndex() {
        cancelCreateIndex = true;
    }

    private boolean parseBlock() throws IOException, SQLException {
        String row;
        JAMSCalendar cal = new JAMSCalendar();
        Calendar localCal = Calendar.getInstance();
        Timestamp ts = null;
        
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
                cal.setValue(value);
                ts = new Timestamp(cal.getTimeInMillis());
                pIndexInsertStmt.setTimestamp(contexts.size()-i, ts, cal);                
            }
            row = reader.readLine();
        }

        long position = reader.getPosition();
//        System.out.println(cal + " - " + ts.getTime() + " - " + position);
            
        pIndexInsertStmt.setLong(contexts.size(), position);

        while ((row = reader.readLine()) != null) {
            if (row.startsWith("@end")) {
                break;
            }
        }        
        
//        try{
//            while (!(row = reader.readLine()).startsWith("@end")) {
//            }
//        }catch(NullPointerException npe){
//            //in case end tag not provided
//        }

        pIndexInsertStmt.execute();
//        stmt.execute(query);

        return true;
    }

    public boolean fillBlock(long position) throws IOException, SQLException {
        String row;
        String insertString = "INSERT INTO data VALUES ";
        String queryPrefix = "(";

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
            if (cd.getType().endsWith("TemporalContext")) {
                value += ":00";
            }
            queryPrefix += "'" + value + "',";
        }

        row = reader.readLine();

        // grab rowCount rows and append them to one SQL statement
        int rowCount = 1;
        while (true) {
            String q = "";
            int i = 0;
            while ((i < rowCount) && !(row = reader.readLine()).startsWith("@end")) {

                q += queryPrefix;

                StringTokenizer tok = new StringTokenizer(row, "\t");
                while (tok.hasMoreTokens()) {
                    q += tok.nextToken() + ",";
                }

                q = q.substring(0, q.length() - 1);
                q += "),";
                i++;
            }

            // insert data into table
            if (!q.isEmpty()) {
                q = insertString + q.substring(0, q.length() - 1);
                stmt.execute(q);
            }
            if (row.startsWith("@end")) {
                break;
            }
        }
        return true;
    }

    /**
     * Check if this is a datastore that contains spatial data that vary in time
     * @return True or false
     */
    public synchronized boolean isTimeSpaceDatastore() {
        ArrayList<DataStoreProcessor.ContextData> cntxt = getContexts();
        if (cntxt.size() != 2) {
            return false;
        }
        if (!cntxt.get(0).getType().equals("jams.model.JAMSSpatialContext")) {
            return false;
        }
        if (!cntxt.get(1).getType().equals("jams.model.JAMSTemporalContext")) {
            return false;
        }

        this.contexts = cntxt;
        return true;
    }

    /**
     * Check if this is a datastore that contains several model runs each having timeseries
     * @return True or false
     */
    public synchronized boolean isEnsembleTimeSeriesDatastore() {
        ArrayList<DataStoreProcessor.ContextData> cntxt = getContexts();
        if (cntxt.size() != 2) {
            return false;
        }
        if (!cntxt.get(0).getType().equals("jams.model.JAMSTemporalContext")) {
            return false;
        }
        if (!cntxt.get(1).getType().contains("jams.components.optimizer") &&
                !cntxt.get(1).getType().contains("optas.optimizer")) {
            return false;
        }

        this.contexts = cntxt;
        return true;
    }

    public synchronized boolean isSimpleEnsembleDatastore(){
        ArrayList<DataStoreProcessor.ContextData> cntxt = getContexts();
        if (cntxt.size() != 1) {
            return false;
        }
        if (!cntxt.get(0).getType().contains("jams.components.optimizer") &&
                !cntxt.get(0).getType().contains("optas.optimizer")) {
            return false;
        }

        this.contexts = cntxt;
        return true;
    }

    /**
     * Check if this is a datastore that contains no further inner contexts
     * @return True or false
     */
    public synchronized boolean isSimpleTimeSerieDatastore() {
        ArrayList<DataStoreProcessor.ContextData> cntxt = getContexts();
        if (cntxt.size() != 1) {
            return false;
        }
        if (cntxt.get(0).getType().equals("jams.model.JAMSTemporalContext")) {
            return true;
        }

        this.contexts = cntxt;
        return false;
    }

    public synchronized boolean isSimpleDataSerieDatastore() {
        ArrayList<DataStoreProcessor.ContextData> cntxt = getContexts();
        if (cntxt.size() != 1) {
            return false;
        }
        if (cntxt.get(0).getType().equals("jams.model.JAMSTemporalContext")) {
            return false;
        }

        if (cntxt.get(0).getType().contains("optas.optimizer") ||
            cntxt.get(0).getType().contains("jams.components.optimizer")) {
            return false;
        }

        this.contexts = cntxt;
        return true;
    }

    public static void main(String[] args) throws SQLException, ClassNotFoundException, IOException {

//        DataStoreProcessor dsdb = new DataStoreProcessor(new File("E:/jamsapplication/JAMS-Gehlberg/output/current/HRULoop.dat"));
        
//        DataStoreProcessor dsdb = new DataStoreProcessor(new File("C:\\Users\\nsk\\Desktop\\jams\\data\\j2k_gehlberg\\output\\current\\HRULoop.dat"));
        DataStoreProcessor dsdb = new DataStoreProcessor(new File("D:\\jamsmodeldata\\J2K_Yzeron\\j2k_yzeron_h\\output\\20121128_114833\\HRULoop.dat"));
        dsdb.addImportProgressObserver(new Observer() {

            public void update(Observable o, Object arg) {
                System.out.println("Progress: " + arg);
            }
        });

        dsdb.createDB();
//        dsdb.createIndex();

        JAMSCalendar cal = new JAMSCalendar();
        String query = "SELECT TimeLoopID, position FROM index WHERE (position>38001964 AND position<38104316) OR (position>134824233 AND position<134953847)";
//        String query = "SELECT TimeLoopID, position FROM index LIMIT 10000";
        
        Statement stmt = dsdb.conn.createStatement();
        ResultSet rs = stmt.executeQuery(query);
        while (rs.next()) {
            long timeloopid = rs.getTimestamp("TimeLoopID", cal).getTime();
            cal.setMilliSeconds(timeloopid);
            System.out.println("###########" + cal + " - " + timeloopid + " - " + rs.getInt("position"));
        }
        
//###########1997-03-30 01:30 - 859685400000 - 38036072
//###########1997-03-30 03:30 - 859692600000 - 38053131
//###########1997-03-30 03:30 - 859692600000 - 38070199        
        
//        DataMatrix m = dsdb.getData(56139387);
//////        DataMatrix m = dsdb.getData(836);
////        m.print(5, 3);
//        for (Object o : m.getIds()) {
//            System.out.println(o);
//        }
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
    public File getFile() {
        return dsFile;
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

    public long getStartPosition() throws IOException {
        reader.setPosition(0);
        while (!reader.readLine().startsWith("@start")) {
        }
        return reader.getPosition();
    }

    public synchronized DataMatrix getData(long position) throws IOException {

        String line, token;
        int i, j, numSelected = 0;

        boolean selected[] = new boolean[attributes.size()];
        ArrayList<String> attributeNames = new ArrayList<String>();
        i = 0;
        for (AttributeData a : attributes) {
            if (a.isSelected() && a.getType().equals("JAMSDouble")) {
                selected[i] = true;
                attributeNames.add(a.getName());
                numSelected++;
            } else {
                selected[i] = false;
            }
            i++;
        }

        double[] cols;
        ArrayList<double[]> rows = new ArrayList<double[]>();
        ArrayList<String> idList = new ArrayList<String>();

        reader.setPosition(position);

        while ((line = reader.readLine()) != null && !line.startsWith("@end")) {

            cols = new double[numSelected];
            StringTokenizer tok = new StringTokenizer(line, "\t");
            j = 0;
            token = null;
            idList.add(tok.nextToken());
            for (i = 0; i < attributes.size(); i++) {
//            while (tok.hasMoreTokens()) {
                try{
                    token = tok.nextToken();
                }catch(NoSuchElementException nsee){
                    System.out.println(nsee);
                }
                if (selected[i]) {
                    cols[j] = Double.parseDouble(token);
                    j++;
                }
            }
            rows.add(cols);
        }
        double[][] data = rows.toArray(new double[rows.size()][numSelected]);
        String ids[] = idList.toArray(new String[idList.size()]);

        return new DataMatrix(data, ids, attributeNames.toArray(new String[attributeNames.size()]));
    }

    public synchronized String[] getSelectedDoubleAttribs() {
        // get number of selected attributes
        ArrayList<String> attribs = new ArrayList<String>();
        for (AttributeData a : getAttributes()) {
            if (a.isSelected() && a.getType().equals("JAMSDouble")) {
                attribs.add(a.getName());
            }
        }
        return attribs.toArray(new String[attribs.size()]);
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
                } else if (type.contains("jams.components.optimizer") || type.contains("optas.optimizer") || type.contains("dump")) {
                    this.idType = "JAMSLong";
                } else
                    this.idType = "JAMSLong";
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

    public class AttributeData implements Comparable {

        public static final boolean SELECTION_DEFAULT = false;
        public static final int AGGREGATION_SUM = 1;
        public static final int AGGREGATION_MEAN = 2;
        public static final int WEIGHTING_NONE = 1;
        public static final int WEIGHTING_TIMES_AREA = 2;
        public static final int WEIGHTING_DIV_AREA = 3;
        private String type;
        private String name;
        private boolean selected;
        private int aggregationType = AGGREGATION_MEAN;
        private int weightingType = WEIGHTING_NONE;

        public AttributeData(String type, String name) {
            this.type = type;
            this.name = name;
            this.selected = SELECTION_DEFAULT;
        }

        public String getType() {
            return type;
        }

        public String getName() {
            return name;
        }

        public boolean isSelected() {
            return selected;
        }

        public void setSelected(boolean selected) {
            synchronized (DataStoreProcessor.this) {
                this.selected = selected;
            }
        }

        public int getAggregationType() {
            return aggregationType;
        }

        public int getWeightingType() {
            return weightingType;
        }

        public void setAggregationType(int aggregationType) {
            synchronized (DataStoreProcessor.this) {
                this.aggregationType = aggregationType;
            }
        }

        public void setWeightingType(int weightingType) {
            synchronized (DataStoreProcessor.this) {
                this.weightingType = weightingType;
            }
        }

        public int compareTo(Object obj) {
            return (this.getName().compareTo(((AttributeData) obj).getName()));
        }
    }

    private class ImportProgressObservable extends Observable {

        private void setProgress(int progress) {
            this.setChanged();
            this.notifyObservers(progress);
        }
    }
}
