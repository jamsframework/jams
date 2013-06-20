/*
 * RBISPgSQL.java
 * Created on 31. Januar 2008, 16:18
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
package jams.workspace.plugins;

import jams.JAMS;
import jams.data.Attribute;
import jams.data.DefaultDataFactory;
import jams.workspace.DataReader;
import jams.workspace.DataValue;
import java.sql.SQLException;
import java.util.ArrayList;
import jams.workspace.DefaultDataSet;
import jams.workspace.Workspace;
import jams.workspace.datatypes.CalendarValue;
import jams.workspace.datatypes.DoubleValue;
import jams.workspace.datatypes.LongValue;
import jams.workspace.datatypes.ObjectValue;
import jams.workspace.datatypes.StringValue;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.TimeZone;

/**
 *
 * @author Christian Fischer
 * 
 * input data reader
 * perfoms sql query on database table
 * difference to jdbcSQL:
 *      everytime function query is executed a new db connection is established and a new 
 *      sql query execution is perfomed. if data was added to the database in the meantime
 *      you will get these data. interval of available data can be request by getLastDate
 * 
 *      some precondition: * date is the first column in the result table, formated as timestamp based
 *                           on seconds. this fits to jdbcSQL 
 *                         * the requested sql stmt is extended by where date > lastDate  orderby date
 */
public class OpenSQL implements DataReader {

    private static final int DOUBLE = 0;
    private static final int LONG = 1;
    private static final int STRING = 2;
    private static final int TIMESTAMP = 3;
    private static final int OBJECT = 4;

    static class QueryResult {
        ResultSet rs;
        int numberOfColumns = -1;
        int[] type;
    }
    private String user,  password,  host,  db,  query, lastDateQuery, driver, dateColumnName, metadataQuery;
    transient private JdbcSQLConnector pgsql;
    transient private QueryResult metadataResult, dataResult;
    private final boolean alwaysReconnect = false;
    private DefaultDataSet[] currentData = null, currentMetadata = null;
    private boolean isClosed = true;
    private Date currentDate = null;
    private String lastDate;

    int offset = 0;

    ArrayList<Integer> indexMap = new ArrayList<Integer>();

    public void setUser(String user) {
        this.user = user;
    }

    public void setPassword(String password) {
        this.password = password;
    }
        
    public void setHost(String host) {
        this.host = host;
    }

    public void setQuery(String query) {
        this.query = query;
    }

    public void setDb(String db) {
        this.db = db;
    }
    
    public void setDriver(String driver){
        this.driver = driver;
    }

    @Override
    public DefaultDataSet[] getData() {
        return currentData;
    }

    public ReaderType getReaderType(){
        if (this.metadataQuery!=null && this.query!=null)
            return ReaderType.ContentAndMetadataReader;
        else if (this.metadataQuery!=null)
            return ReaderType.MetadataReader;
        else if (this.query != null)
            return ReaderType.ContentReader;
        else
            return ReaderType.Empty;
    }

    @Override
    public int numberOfColumns() {
        return dataResult.numberOfColumns;
    }

    /**
     * @return the metadataQuery
     */
    public String getMetadataQuery() {
        return metadataQuery;
    }

    /**
     * @param metadataQuery the metadataQuery to set
     */
    public void setMetadataQuery(String metadataQuery) {
        this.metadataQuery = metadataQuery;
    }
    public void setDateColumnName(String name){
        this.dateColumnName = name;
    }
    
    public void setLastDateQuery(String query) {
        this.lastDateQuery = query;
    }
          
    @Override
    public int fetchValues() {
        return fetchValues(Integer.MAX_VALUE);
    }

    @Override
    public int fetchValues(int count) {
        if (getReaderType() != ReaderType.ContentReader && getReaderType() != ReaderType.ContentAndMetadataReader)
            return 0;

        if (currentData==null){
            query();
        }

        currentData = getDBRows(count);
        return 0;
    }

     public DefaultDataSet getMetadata(int i) {
        if (getReaderType() != ReaderType.MetadataReader && getReaderType() != ReaderType.ContentAndMetadataReader)
            return null;

        if (isClosed) {
            establishConnection();
        }
       if (currentMetadata==null){
            metadataResult = executeQuery(getMetadataQuery());
            currentMetadata = queryResultToDataSet(metadataResult, 10000);
        }
        if (currentMetadata==null)
            return null;

        return currentMetadata[i];
    }

    DefaultDataSet[] queryResultToDataSet(QueryResult r, long count) {
        ArrayList<DefaultDataSet> data = new ArrayList<DefaultDataSet>();
        DefaultDataSet dataSet;
        DataValue value;

        if (r == null || r.rs == null){
            return null;
        }

        try {
            int i = 0;
            while ((i < count) && r.rs.next()) {
                i++;
                dataSet = new DefaultDataSet(r.numberOfColumns);

                for (int j = 0; j < r.numberOfColumns; j++) {

                    switch (r.type[j]) {
                        case DOUBLE:
                            double v = r.rs.getDouble(j + 1);
                            if (!r.rs.wasNull()) {
                                value = new DoubleValue(v);
                            } else {
                                value = new StringValue("");
                            }
                            dataSet.setData(j, value);
                            break;
                        case LONG:
                            value = new LongValue(r.rs.getLong(j + 1));
                            dataSet.setData(j, value);
                            break;
                        case STRING:
                            value = new StringValue(r.rs.getString(j + 1));
                            dataSet.setData(j, value);
                            break;
                        case TIMESTAMP:
                            Attribute.Calendar cal = DefaultDataFactory.getDataFactory().createCalendar();
                            //does not work .. hours are not represented well
                            GregorianCalendar greg = new GregorianCalendar();
                            greg.setTimeZone(TimeZone.getTimeZone("GMT"));
                            cal.setTimeInMillis(r.rs.getDate(j + 1, greg).getTime());

                            String date = r.rs.getString(j + 1);
                            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss Z");
                            try {

                                long millis = format.parse(date + " +0000").getTime();
                                cal.setTimeInMillis(millis);
                            } catch (Exception e) {
                                throw new SQLException(e.toString());
                            }

                            value = new CalendarValue(cal);
                            dataSet.setData(j, value);
                            break;
                        default:
                            value = new ObjectValue(r.rs.getObject(j + 1));
                            dataSet.setData(j, value);
                    }
                }
                data.add(dataSet);
            }

        } catch (SQLException sqlex) {
            System.out.println("jdbcSQL: " + sqlex);
            sqlex.printStackTrace();
        }

        return data.toArray(new DefaultDataSet[data.size()]);
    }

    QueryResult executeQuery(String query) {
        QueryResult result = new QueryResult();
        try {
            ResultSet rs = null;
            if (query.contains("WHERE")){
                rs = pgsql.execQuery(query + " AND " + dateColumnName + ">\"" + lastDate + "\" ORDER BY " + dateColumnName + " ASC");
            }else{
                rs = pgsql.execQuery(query + " WHERE " + dateColumnName + ">\"" + lastDate + "\" ORDER BY " + dateColumnName + " ASC");
            }

            rs = pgsql.execQuery(query);
            //rs.setFetchSize(0);
            ResultSetMetaData rsmd = rs.getMetaData();
            int numberOfColumns = rsmd.getColumnCount();
            int type[] = new int[numberOfColumns];
            for (int i = 0; i < numberOfColumns; i++) {
                if (rsmd.getColumnTypeName(i + 1).startsWith("int") || rsmd.getColumnTypeName(i + 1).startsWith("INT")
                        || rsmd.getColumnTypeName(i + 1).startsWith("integer") || rsmd.getColumnTypeName(i + 1).startsWith("INTEGER")) {
                    type[i] = LONG;
                } else if (rsmd.getColumnTypeName(i + 1).startsWith("float") || rsmd.getColumnTypeName(i + 1).startsWith("FLOAT")) {
                    type[i] = DOUBLE;
                } else if (rsmd.getColumnTypeName(i + 1).startsWith("double") || rsmd.getColumnTypeName(i + 1).startsWith("DOUBLE")) {
                    type[i] = DOUBLE;
                } else if (rsmd.getColumnTypeName(i + 1).startsWith("numeric") || rsmd.getColumnTypeName(i + 1).startsWith("NUMERIC")) {
                    type[i] = DOUBLE;
                } else if (rsmd.getColumnTypeName(i + 1).startsWith("varchar") || rsmd.getColumnTypeName(i + 1).startsWith("VARCHAR")) {
                    type[i] = STRING;
                } else if (rsmd.getColumnTypeName(i + 1).startsWith("datetime") || rsmd.getColumnTypeName(i + 1).startsWith("DATETIME")) {
                    type[i] = TIMESTAMP;
                } else {
                    type[i] = OBJECT;
                }
            }
            result.numberOfColumns = numberOfColumns;
            result.type = type;
            result.rs = rs;
        } catch (SQLException sqlex) {
            System.err.println("jdbcSQL: " + sqlex);
            sqlex.printStackTrace();
            return null;
        }
        return result;
    }

    private boolean skip(long count) {
        try{
            if (count == 0)
                return true;
            else if(count > 1){
                dataResult.rs.relative((int)count-1);
                //rs.skip(count-1);
                dataResult.rs.next();
            }else{
                dataResult.rs.next();
            }
        }catch(SQLException sqlex){
            System.err.println("OpenSQL: " + sqlex);sqlex.printStackTrace();
            return false;
        }
        return true;
    }

    void establishConnection() {
        try {
            if (pgsql == null) {
                pgsql = new JdbcSQLConnector(host, db, user, password, driver);
                pgsql.connect();
                isClosed = false;
            } else if (this.alwaysReconnect) {
                pgsql.close();
                pgsql = null;
                isClosed = true;
                establishConnection();

            }
        } catch (SQLException sqlex) {
            System.err.println("PollingSQL: " + sqlex);
            sqlex.printStackTrace();
            isClosed = true;
        }
    }

    public void query() {
        establishConnection();
        this.dataResult = executeQuery(query);
        return;
    }

    @Override
    public int init() {
        offset = 0;

        if (db == null) {
            return -1;
        }

        if (user == null) {
            return -1;
        }

        if (password == null) {
            return -1;
        }

        if (host == null) {
            return -1;
        }

        if (query == null && metadataQuery == null) {
            return -1;
        }

        if (driver == null) {
            driver = "jdbc:postgresql";
        }
        lastDate = "1970-01-01 01:00";
        return 0;
    }

    int closeResult(QueryResult r) {
        try {
            if (r.rs != null) {
                r.rs.close();
                r.rs = null;
            }
        } catch (SQLException sqlex) {
            System.out.println("jdbcSQL: " + sqlex);
            sqlex.printStackTrace();
            return -1;
        }
        return 0;
    }

    @Override
    public int cleanup() {
        try {
            if (closeResult(metadataResult) != 0) {
                return -1;
            }
            if (closeResult(dataResult) != 0) {
                return -1;
            }

            if (pgsql != null) {
                pgsql.close();
                pgsql = null;
                isClosed = true;
            }
        } catch (SQLException sqlex) {
            System.out.println("jdbcSQL: " + sqlex);
            return -1;
        }

        return 0;
    }

    public Attribute.Calendar getLastDate(){
        establishConnection();
        
        try{
            ResultSet rs2 = null;
            rs2 = pgsql.execQuery(lastDateQuery);
            
            Attribute.Calendar cal = DefaultDataFactory.getDataFactory().createCalendar();                                                                             
            String date = lastDate;
            
            if (rs2.next())
                date = rs2.getString(1);
            
            cal.setTimeInMillis(Long.parseLong(date)*1000); 
            rs2.close();
            return cal;                
        } catch (Exception sqlex) {
            System.err.println("PollingSQL: " + sqlex);sqlex.printStackTrace();
            sqlex.printStackTrace();
        }
        return null;
    }

    boolean dataValid = true;
    boolean noMoreData = false;
    int counter = 0;
    private DefaultDataSet[] getDBRows(long count) {
        
        ArrayList<DefaultDataSet> data = new ArrayList<DefaultDataSet>();
        DefaultDataSet dataSet;
        DataValue value;

        //DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        
        try {
            int i = 0;            
            
            while ((i < count) ) {
                indexMap.add(new Integer(counter));

                if (dataValid){
                    if (!dataResult.rs.next()){
                        noMoreData = true;
                    }else
                        counter++;
                }
                i++;
                offset++;
                dataSet = new DefaultDataSet(dataResult.numberOfColumns);

                Date dateIn = null;

                if (!noMoreData) {
                    Long timestamp = Long.parseLong(dataResult.rs.getString(1));
                    dateIn = new Date();
                    dateIn.setTime(timestamp*1000);
                }

                if (dateIn != null){
                    if (currentDate == null){
                        currentDate = (Date)dateIn.clone();
                    }
                    if (dateIn.compareTo(currentDate) <= 0){                        
                        dataValid = true;
                    }else{
                        dataValid = false;
                    }
                }else{
                    dataValid = false;
                }
                //this.lastDate = this.lastDate;// rs.getString(1);
                currentDate.setTime(currentDate.getTime() + 3600000);
                for (int j = 0; j < dataResult.numberOfColumns; j++) {

                    switch (dataResult.type[j]) {
                        case DOUBLE:
                            if (dataValid)
                                value = new DoubleValue(dataResult.rs.getDouble(j + 1));
                            else
                                value = new DoubleValue(JAMS.getMissingDataValue());
                            dataSet.setData(j, value);
                            break;
                        case LONG:
                            if (dataValid)
                                value = new LongValue(dataResult.rs.getLong(j + 1));
                            else
                                value = new LongValue((Long)JAMS.getMissingDataValue(long.class));

                            dataSet.setData(j, value);
                            break;
                        case STRING:
                            if (dataValid)
                                value = new StringValue(dataResult.rs.getString(j + 1));
                            else
                                value = new LongValue((Long)JAMS.getMissingDataValue(long.class));
                            dataSet.setData(j, value);
                            break;
                        case TIMESTAMP:
                            Attribute.Calendar cal = DefaultDataFactory.getDataFactory().createCalendar();                            
                            //does not work .. hours are not represented well
                            GregorianCalendar greg = new GregorianCalendar();
                            greg.setTimeZone(TimeZone.getTimeZone("GMT"));
                            if (dataValid)
                                cal.setTimeInMillis(dataResult.rs.getDate(j+1,greg).getTime());
                            else
                                cal.setTimeInMillis(currentDate.getTime());

                            String date = dataResult.rs.getString(j+1);
                            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss Z");
                            try{
                                long millis = format.parse(date+" +0000").getTime();
                                cal.setTimeInMillis(millis);
                            }catch(Exception e){
                                throw new SQLException(e.toString());
                            }
                            
                            value = new CalendarValue(cal);
                            dataSet.setData(j, value);
                            break;
                        default:
                            if (dataValid)
                                value = new ObjectValue(dataResult.rs.getObject(j + 1));
                            else
                                value = null;
                            dataSet.setData(j, value);
                    }
                }
                data.add(dataSet);
            }

        } catch (SQLException sqlex) {
            System.err.println("PollingSQL: " + sqlex);sqlex.printStackTrace();
            sqlex.printStackTrace();
        }

        return data.toArray(new DefaultDataSet[data.size()]);
    }
    public void setWorkspace(Workspace ws){

    }
    public void writeObject(ObjectOutputStream out) throws IOException{
        out.defaultWriteObject();
    }

    public void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException{
        in.defaultReadObject();
        if (isClosed) {
            this.cleanup();
            return;
        }
        long ot = System.currentTimeMillis();
        int oldOffset = offset;
        query();

        long dt = System.currentTimeMillis() - ot;
        System.out.println("recover-time_query:" + dt);
        this.skip(oldOffset);
        System.out.println("DATUM after skip:" + this.getData()[0].getData()[0].getLong());
    }
}
