/*
 * JdbcSQLConnector.java
 * Created on 30. November 2007, 16:16
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

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

/**
 *
 * @author Sven Kralisch
 */
public class JdbcSQLConnector {

    private String hostname,  database,  username,  passwd, driver;
    private Connection con;

    transient static HashMap<String,ConnectionInfo> connPool;

    transient private Set<BufferedResultSet> resultSetPool;

    private class ConnectionInfo{
        Connection connection;
        int useCount;
    }


    public String getKey(){
        return hostname + database + username + driver;
    }

    public JdbcSQLConnector(String hostname, String database, String username, String passwd, String driver) {
        this.hostname = hostname;
        this.database = database;
        this.username = username;
        this.passwd = passwd;
        this.driver = driver;
        try {
            if (driver.equalsIgnoreCase("jdbc:postgresql"))
                Class.forName("org.postgresql.Driver");
            if (driver.equalsIgnoreCase("jdbc:mysql"))
                Class.forName("org.gjt.mm.mysql.Driver");
                
        } catch (ClassNotFoundException cnfe) {
            cnfe.printStackTrace();
        }
        if (connPool == null){
            connPool = new HashMap<String,ConnectionInfo>();
        }
        resultSetPool = new HashSet<BufferedResultSet>();        
    }

    public void connect() throws SQLException {
        ConnectionInfo info = this.connPool.get(getKey());
        if (info != null){
            this.con = info.connection;
            info.useCount++;
        }
        //this.con = DriverManager.getConnection("jdbc:postgresql://" + hostname + "/" + database, username, passwd);
        if (this.con==null || this.con.isClosed()){
            this.con = DriverManager.getConnection(driver + "://" + hostname + "/" + database + "?autoReconnect=true", username, passwd);
            info = new ConnectionInfo();
            info.connection = con;
            info.useCount++;
            connPool.put(getKey(), info);
        }
    }

    public int execUpdate(String sqlQuery) throws SQLException {
        if (con == null) {
            connect();
        }
        int trialCount=0;
        while (!con.isValid(10) && trialCount++<4){
            System.err.println("lost connection to database, attempt " + trialCount + " of 4 to reconnect");
            connect();
        }
        Statement stmt = con.createStatement();
        int result = stmt.executeUpdate(sqlQuery);
        stmt.close();
        return result;
    }
static int counter = 0;
    public class BufferedResultSet{
        ResultSet nonBufferedSet;
        Statement stmt;
        String query;
        int limit = 100;
        int marker;

        BufferedResultSet(String query){
            this.query = query;
            marker = 0;
            try{
                fetch();
            }catch(SQLException ex){
            
            }
        }

        public void skip(long count)throws SQLException {
            while (nonBufferedSet.next() && count>0){
                count--;
            }
            marker += count;            
        }

        private void fetch() throws SQLException {
            String lmtQuery = query + " LIMIT " + marker + "," + limit;
            marker += limit;
            if (counter++ % 500 == 0) {
                System.gc();
            }
            if (nonBufferedSet != null) {
                nonBufferedSet.close();                
            }
            nonBufferedSet = nonBufferedExecQuery(lmtQuery);
        }

        private ResultSet nonBufferedExecQuery(String sqlQuery) throws SQLException {
            if (con == null) {
                connect();
            }

            int trialCount = 0;
            while (true) {
                try {
                    //do use a prepare statment to stream the result. this reduces memory
                    //usage
                    if (stmt != null) {
                        stmt.close();
                    }
                    stmt = con.prepareStatement(sqlQuery,
                            ResultSet.TYPE_FORWARD_ONLY,
                            ResultSet.CONCUR_READ_ONLY);

                    //Statement stmt = con.createStatement();
                    return stmt.executeQuery(sqlQuery);
                } catch (SQLException sqlex) {
                    trialCount++;
                    if (trialCount > 4) {
                        throw sqlex;
                    }
                    connect();
                    System.err.println("lost connection to database, attempt " + trialCount + " of 4 to reconnect" + sqlex.toString());
                    try {
                        Thread.sleep(1000);
                    } catch (Exception e) {
                    }
                }
            }
        }

        public boolean next() throws SQLException {
            if (nonBufferedSet == null){
                fetch();
            }
            if (nonBufferedSet.next())
                return true;
            fetch();            
            return nonBufferedSet.next();
        }

        public String getString(int i) throws SQLException {
            return nonBufferedSet.getString(i);
        }

        public double getDouble(int i) throws SQLException {
            return nonBufferedSet.getDouble(i);
        }

        public long getLong(int i) throws SQLException {
            return nonBufferedSet.getLong(i);
        }

        public Object getObject(int i) throws SQLException {
            return nonBufferedSet.getObject(i);
        }

        public ResultSetMetaData getMetaData() throws SQLException {
            return nonBufferedSet.getMetaData();
        }
        public Date getDate(int i, Calendar cal) throws SQLException {
            return nonBufferedSet.getDate(i,cal);
        }

        public void close() throws SQLException {
            if (nonBufferedSet != null)
                nonBufferedSet.close();
            if (stmt!=null)
                stmt.close();
            resultSetPool.remove(this);
        }
    }

    public BufferedResultSet execQuery(String sqlQuery) throws SQLException {
        BufferedResultSet rs = new BufferedResultSet(sqlQuery);
        resultSetPool.add(rs);
        return rs;
    }
    
    public boolean isValid() throws SQLException {
        return this.con.isValid(10);
    }
    
    public void close() throws SQLException {
        ConnectionInfo info = this.connPool.get(getKey());
        //why can this happen?
        if (info==null){
            return;
        }
        info.useCount--;
        if (info.useCount==0){
            this.con.close();
            con = null;
            connPool.remove(getKey());
        }
        for (BufferedResultSet set : this.resultSetPool){
            set.close();
        }
        if (resultSetPool.size()>0){
            System.out.println("warning: resultSet-Pool was not empty after close");
        }
        resultSetPool.clear();
    }

}
