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

import jams.JAMS;
import jams.data.JAMSCalendar;
import jams.io.BufferedFileReader;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.NoSuchElementException;
import java.util.Observer;
import java.util.StringTokenizer;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Sven Kralisch <sven.kralisch at uni-jena.de>
 */
public class DataStoreProcessor extends AbstractDataStoreProcessor{

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
    private PreparedStatement pIndexInsertStmt;

    public DataStoreProcessor(File dsFile) {
        super(dsFile);
        this.dsFile = dsFile;

        try {
            initDS();
        } catch (IOException ex) {
            Logger.getLogger(DataStoreProcessor.class.getName()).log(Level.SEVERE, null, ex);
        }

        // use a lot (100MB) of cache to avoid output of data to file which  
        // causes errorneous handling of timestamps
        if (dsFile.toString().lastIndexOf(".") == -1){
            jdbcURL = "jdbc:h2:mem:" + dsFile.toString() + ";LOG=0";
        }else
            jdbcURL = "jdbc:h2:mem:" + dsFile.toString().substring(0, dsFile.toString().lastIndexOf(".")) + ";LOG=0";

    }

    @Override
    public boolean isEmpty() {
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new FileReader(dsFile));
            String line = null;
            boolean isDataStart = false;
            int dataCounter = 0;

            while ((line = reader.readLine()) != null) {
                if (line.contains("@data")) {
                    isDataStart = true;
                } else if (isDataStart) {
                    dataCounter++;
                }
            }
            reader.close();
            return dataCounter <= 1;

        } catch (IOException fnfe) {
            try {
                if (reader != null) {
                    reader.close();
                }
            } catch (IOException ioe) {
                ioe.printStackTrace();
                return true;
                //throw new IOException(JAMS.i18n("Could_not_read_file_") + file.toString(), ioe);
            }
            return true;
        }
    }
    public static DataStoreType getDataStoreType(File file) {
        DataStoreProcessor dsdb = new DataStoreProcessor(file);

        try {
            //if (!dsdb.existsH2DB()) {
            if (dsdb.isTimeSpaceDatastore()) {
                return DataStoreType.SpatioTemporal;
            }
            if (dsdb.isSimpleDataSerieDatastore()) {
                return DataStoreType.DataSerie1D;
            }
            if (dsdb.isSimpleTimeSerieDatastore()) {
                return DataStoreType.Timeserie;
            }
            if (dsdb.isEnsembleTimeSeriesDatastore()) {
                return DataStoreType.TimeDataSerie;
            }
            /*if (dsdb.isSimpleEnsembleDatastore()) {
                return SimpleEnsembleDataStore;
            }*/
            return DataStoreType.Unsupported;
            //}
        } catch (Exception e) {
            Logger.getLogger(DataStoreProcessor.class.getName()).log(Level.SEVERE, null, e);
        }
        return DataStoreType.Unsupported;
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
                throw new SQLException("unsupported attribute type: " + attribute.getType());
            }
            String attributeName = attribute.getName();
            while (q.contains(attributeName)) {
                attributeName += "_";
            }
            q += "\"" + attributeName + "\" " + dataType + ",";
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
            reader.close();
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
//        Calendar localCal = Calendar.getInstance();
        Timestamp ts;
        
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
            }else if (cd.getType().contains("optas") || cd.getType().contains("optimizer")) {                
                pIndexInsertStmt.setLong(contexts.size()-i, Long.parseLong(value));
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
        if (!(cntxt.get(0).getType().equals("jams.model.JAMSSpatialContext") || cntxt.get(0).getType().equals("jams.components.core.SpatialContext") || cntxt.get(0).getType().equals("jams.components.conditional.FilteredSpatialContext"))) {
            return false;
        }
        if (!(cntxt.get(1).getType().equals("jams.model.JAMSTemporalContext") || cntxt.get(1).getType().equals("jams.components.core.TemporalContext"))) {
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
        if (!cntxt.get(0).getType().equals("jams.components.core.TemporalContext")) {
            return false;
        }
        if (!cntxt.get(1).getType().contains("jams.components.optimizer") &&
                !cntxt.get(1).getType().contains("optas.optimizer")) {
            return false;
        }

        this.contexts = cntxt;
        return true;
    }

    /*public synchronized boolean isSimpleEnsembleDatastore(){
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
    }*/

    /**
     * Check if this is a datastore that contains no further inner contexts
     * @return True or false
     */
    public synchronized boolean isSimpleTimeSerieDatastore() {
        ArrayList<DataStoreProcessor.ContextData> cntxt = getContexts();
        if (cntxt.size() != 1) {
            return false;
        }
        if (cntxt.get(0).getType().contains("Temporal")) {
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
        if (cntxt.get(0).getType().equals("jams.components.core.TemporalContext")) {
            return false;
        }

        /*if (cntxt.get(0).getType().contains("optas.optimizer") ||
            cntxt.get(0).getType().contains("jams.components.optimizer")) {
            return false;
        }*/

        this.contexts = cntxt;
        return true;
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
                    //TODO: review this decision .. 
                    if (cols[j] == JAMS.getMissingDataValue()){
                        cols[j] = Double.NaN;
                    }
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

    @Override
    public AbstractDataStoreProcessor[] getSubDataStores() {
        return new AbstractDataStoreProcessor[0];
    }

    public String toString(){
        return this.dsFile.getName();
    }
}
