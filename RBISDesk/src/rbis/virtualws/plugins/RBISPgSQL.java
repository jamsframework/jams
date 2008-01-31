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
package rbis.virtualws.plugins;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import rbis.db.PGSQLConnector;
import rbis.virtualws.DataSet;

/**
 *
 * @author Sven Kralisch
 */
public class RBISPgSQL implements DataReader {

    private String user,  password,  host,  db,  query;
    private ResultSet rs;
    private ResultSetMetaData rsmd;
    private PGSQLConnector pgsql;

    public RBISPgSQL() {
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

    public DataSet[] getValues() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public DataSet[] getValues(int count) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public DataSet getValue() {
        return getDBRows(1)[0];
    }

    private DataSet[] getDBRows(int count) {

        ArrayList<DataSet> data = new ArrayList<DataSet>();
        DataSet dataSet;

        try {

            int i = 0;
            while (rs.next() && i < count) {
                i++;
                dataSet = new DataSet();
                
                
            }

        /*            
        int numberOfColumns = rsmd.getColumnCount();
        int rowCount = 1;
        while (rs.next()) {
        System.out.println("Line " + rowCount + ": ");
        for (int i = 1; i <= numberOfColumns; i++) {
        System.out.print("\t" + rsmd.getColumnName(i) + ": ");
        System.out.println(rs.getString(i));
        }
        System.out.println("");
        rowCount++;
        }
         */
        } catch (SQLException sqlex) {
            sqlex.printStackTrace();
        }

        return data.toArray(new DataSet[data.size()]);
    }

    public void init() {

        if (db == null) {
            return;
        }

        if (user == null) {
            return;
        }

        if (password == null) {
            return;
        }

        if (host == null) {
            return;
        }

        if (query == null) {
            return;
        }

        pgsql = new PGSQLConnector(host, db, user, password);

        try {

            pgsql.connect();
            rs = pgsql.execQuery(query);
            rs.setFetchSize(0);
            rsmd = rs.getMetaData();

        } catch (SQLException sqlex) {
            sqlex.printStackTrace();
        }
    }

    public void cleanup() {
        try {
            rs.close();
            pgsql.close();
        } catch (SQLException sqlex) {
            sqlex.printStackTrace();
        }
    }
}
