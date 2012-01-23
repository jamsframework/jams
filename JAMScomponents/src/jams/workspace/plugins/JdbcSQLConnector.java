/*
 * JdbcSQLConnector.java
 * Created on 30. November 2007, 16:16
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
    private Statement stmt;

    transient static HashMap<String,ConnectionInfo> connPool;

    transient private Set<ResultSet> resultSetPool;

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
        resultSetPool = new HashSet<ResultSet>();        
    }

    public void connect() throws SQLException {
        ConnectionInfo info = connPool.get(getKey());
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

    public ResultSet execQuery(String sqlQuery) throws SQLException {
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
                /*stmt = con.prepareStatement(sqlQuery,
                ResultSet.TYPE_FORWARD_ONLY,
                ResultSet.CONCUR_READ_ONLY);*/

                stmt = con.createStatement();
                ResultSet rs = stmt.executeQuery(sqlQuery);
                resultSetPool.add(rs);
                return rs;

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
        for (ResultSet set : this.resultSetPool){
            set.close();
        }
        if (resultSetPool.size()>0){
            System.out.println("warning: resultSet-Pool was not empty after close");
        }
        resultSetPool.clear();
    }

}
