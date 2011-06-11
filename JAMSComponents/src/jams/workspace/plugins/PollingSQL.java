/*
 * RBISPgSQL.java
 * Created on 31. Januar 2008, 16:18
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

import jams.data.Attribute;
import jams.data.JAMSDataFactory;
import jams.workspace.DataReader;
import jams.workspace.DataValue;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import jams.workspace.DefaultDataSet;
import jams.workspace.datatypes.CalendarValue;
import jams.workspace.datatypes.DoubleValue;
import jams.workspace.datatypes.LongValue;
import jams.workspace.datatypes.ObjectValue;
import jams.workspace.datatypes.StringValue;
import jams.workspace.plugins.BufferedJdbcSQLConnector.BufferedResultSet;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
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
public class PollingSQL implements DataReader {

    private static final int DOUBLE = 0;
    private static final int LONG = 1;
    private static final int STRING = 2;
    private static final int TIMESTAMP = 3;
    private static final int OBJECT = 4;
    
    private String user,  password,  host,  db,  query, lastDateQuery, driver, dateColumnName;
    private boolean openEnd=false;
    transient private BufferedResultSet rs;
    transient private ResultSetMetaData rsmd;
    transient private BufferedJdbcSQLConnector pgsql;
    private int numberOfColumns = -1;
    private int[] type;    
    private DefaultDataSet[] currentData = null;
    private final boolean alwaysReconnect = false;
    
    private String lastDate;

    private Long currentDateDB;
    private Long currentDateDS;
    private Long timestep = 3600L;

    int offset = 0;
    
    public PollingSQL(){        
    }
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

    public void setDateColumnName(String name){
        this.dateColumnName = name;
    }

    public void setLastDateQuery(String query) {
        this.lastDateQuery = query;
    }

    public void setOpenEnd(String openEnd) {
        if (openEnd.equals("true"))
            this.openEnd = true;
        else
            this.openEnd = false;
    }

    
    @Override
    public DefaultDataSet[] getData() {
        return currentData;
    }

    @Override
    public int fetchValues() {
        currentData = getDBRows(Long.MAX_VALUE);        
        return 0;
    }

    @Override
    public int fetchValues(int count) {
        currentData = getDBRows(count);
        return 0;
    }

    private boolean skip(long count) {
        try{
            if (count == 0)
                return true;
            else if(count > 1){
                rs.skip(count-1);
                rs.next();
            }else{
                rs.next();
            }
        }catch(SQLException sqlex){
            System.err.println("PollingSQL: " + sqlex);sqlex.printStackTrace();
            return false;
        }
        return true;            
    }

    public Attribute.Calendar getLastDate(){
        establishConnection();
        
        try{
            BufferedResultSet rs2 = null;
            rs2 = pgsql.execQuery(lastDateQuery);
            
            Attribute.Calendar cal = JAMSDataFactory.createCalendar();                                                                             
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

    private DefaultDataSet constructDefaultDataSet(Date date) {
        DefaultDataSet dataSet = new DefaultDataSet(numberOfColumns);
        for (int j = 0; j < numberOfColumns; j++) {
            switch (type[j]) {
                case DOUBLE:
                    dataSet.setData(j, new DoubleValue(-9999.0));
                    break;
                case LONG:
                    dataSet.setData(j, new LongValue(-9999L));
                    break;
                case STRING:
                    dataSet.setData(j, new StringValue("-9999"));
                    break;
                case TIMESTAMP:
                    Attribute.Calendar cal = JAMSDataFactory.createCalendar();
                    cal.setTime(date);
                    dataSet.setData(j, new CalendarValue(cal));
                    break;
                default:
                    dataSet.setData(j, new StringValue("-9999"));
            }
        }
        return dataSet;
    }

    private DefaultDataSet getNextDBRow() {
        DefaultDataSet dataSet;
        DataValue value;

        try {
            if (!rs.next()) {
                return null;
            }            
            offset++;
            dataSet = new DefaultDataSet(numberOfColumns);

            this.lastDate = rs.getString(1);
            currentDateDB = Long.parseLong(lastDate);

            for (int j = 0; j < numberOfColumns; j++) {

                switch (type[j]) {
                    case DOUBLE:
                        value = new DoubleValue(rs.getDouble(j + 1));
                        dataSet.setData(j, value);
                        break;
                    case LONG:
                        value = new LongValue(rs.getLong(j + 1));
                        dataSet.setData(j, value);
                        break;
                    case STRING:
                        value = new StringValue(rs.getString(j + 1));
                        dataSet.setData(j, value);
                        break;
                    case TIMESTAMP:
                        Attribute.Calendar cal = JAMSDataFactory.createCalendar();
                        //does not work .. hours are not represented well
                        GregorianCalendar greg = new GregorianCalendar();
                        greg.setTimeZone(TimeZone.getTimeZone("GMT"));
                        cal.setTimeInMillis(rs.getDate(j + 1, greg).getTime());

                        String date = rs.getString(j + 1);
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
                        value = new ObjectValue(rs.getObject(j + 1));
                        dataSet.setData(j, value);
                }
            }
            return dataSet;

        } catch (SQLException sqlex) {
            System.err.println("PollingSQL: " + sqlex);
            sqlex.printStackTrace();
            sqlex.printStackTrace();
        }
        return null;
    }
    DefaultDataSet nextDataSet = null;

    private DefaultDataSet[] getDBRows(long count) {        
        ArrayList<DefaultDataSet> data = new ArrayList<DefaultDataSet>();
        DefaultDataSet dataSet;
        
        while ((data.size() < count)) {
            if (currentDateDS == null) {
                dataSet = getNextDBRow();
                data.add(dataSet);
                currentDateDS = new Long(currentDateDB);
                currentDateDS += timestep;
            } else {
                if (nextDataSet != null) {
                    dataSet = nextDataSet;
                } else {
                    dataSet = getNextDBRow();
                    if (dataSet == null) {
                        if (openEnd){
                            dataSet = constructDefaultDataSet(new Date(currentDateDS));
                            currentDateDB = currentDateDS;                            
                        }else
                            break;
                    }
                }

                if (Math.abs(currentDateDB - currentDateDS) <= 1800) {
                    nextDataSet = null;
                    data.add(dataSet);
                    currentDateDS += timestep;
                } else if (currentDateDB > currentDateDS) {
                    nextDataSet = dataSet;
                    data.add(constructDefaultDataSet(new Date(currentDateDS)));
                    currentDateDS += timestep;
                } else if (currentDateDB < currentDateDS) {
                    nextDataSet = null;
                }
            }
        }
        return data.toArray(new DefaultDataSet[data.size()]);
    }

    public void query(){
        establishConnection();
        try {
            if (rs != null){
                rs.close();                
            }
            if (!query.contains("LIMIT")){
                if (query.contains("WHERE")){
                    rs = pgsql.execQuery(query + " AND " + dateColumnName + ">\"" + lastDate + "\" ORDER BY " + dateColumnName + " ASC");
                }else{
                    rs = pgsql.execQuery(query + " WHERE " + dateColumnName + ">\"" + lastDate + "\" ORDER BY " + dateColumnName + " ASC");
                }
            }else
                rs = pgsql.execQuery(query);
            //rs.setFetchSize(0);
            rsmd = rs.getMetaData();
            numberOfColumns = rsmd.getColumnCount();
            type = new int[numberOfColumns];
            for (int i = 0; i < numberOfColumns; i++) {
                
                if (rsmd.getColumnTypeName(i + 1).startsWith("int") || rsmd.getColumnTypeName(i + 1).startsWith("INT") || 
                    rsmd.getColumnTypeName(i + 1).startsWith("integer") || rsmd.getColumnTypeName(i + 1).startsWith("INTEGER")  ) {
                    type[i] = LONG;
                } else if (rsmd.getColumnTypeName(i + 1).startsWith("float") || rsmd.getColumnTypeName(i + 1).startsWith("FLOAT") ) {
                    type[i] = DOUBLE;
                } else if (rsmd.getColumnTypeName(i + 1).startsWith("double") || rsmd.getColumnTypeName(i + 1).startsWith("DOUBLE") ) {
                    type[i] = DOUBLE;
                } else if (rsmd.getColumnTypeName(i + 1).startsWith("numeric") || rsmd.getColumnTypeName(i + 1).startsWith("NUMERIC") ) {
                    type[i] = DOUBLE;
                } else if (rsmd.getColumnTypeName(i + 1).startsWith("varchar") || rsmd.getColumnTypeName(i + 1).startsWith("VARCHAR")) {
                    type[i] = STRING;
                } else if (rsmd.getColumnTypeName(i + 1).startsWith("datetime") || rsmd.getColumnTypeName(i + 1).startsWith("DATETIME")) {
                    type[i] = TIMESTAMP;
                } else if (rsmd.getColumnTypeName(i + 1).startsWith("bigint") || rsmd.getColumnTypeName(i + 1).startsWith("BIGINT")) {
                    type[i] = LONG;
                } else {
                    type[i] = OBJECT;
                }
            }            
        } catch (SQLException sqlex) {
            System.err.println("PollingSQL: " + sqlex);  
            sqlex.printStackTrace();
        }
    }
    
    void establishConnection() {
        try {
            if (pgsql == null) {
                pgsql = new BufferedJdbcSQLConnector(host, db, user, password, driver);
                pgsql.connect();
            } else if (this.alwaysReconnect) {
                pgsql.close();
                pgsql = null;
                establishConnection();
            }
        } catch (SQLException sqlex) {
            System.err.println("PollingSQL: " + sqlex);
            sqlex.printStackTrace();
        }
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

        if (query == null) {
            return -1;
        }
        
        if (driver == null) {
            driver = "jdbc:postgresql";
        }
        lastDate = "1970-01-01 01:00";
        query();
        return 0;
    }

    @Override
    public int cleanup() {
        try {
            if (rs != null) {
                rs.close();                
            }
            if (pgsql != null) {
                pgsql.close();                
            }
        } catch (SQLException sqlex) {
            System.out.println("PollingSQL: " + sqlex);
            sqlex.printStackTrace();
            return -1;
        }

        return 0;
    }

    @Override
    public int numberOfColumns() {
        return numberOfColumns;
    }

    private void writeObject(ObjectOutputStream out) throws IOException{
        out.defaultWriteObject();
    }

    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException{
        in.defaultReadObject();
        long ot = System.currentTimeMillis();

        int oldOffset = offset;
        query();

        long dt = System.currentTimeMillis() - ot;
        System.out.println("recover-time_query:" + dt);
        this.skip(oldOffset);
    }
}
