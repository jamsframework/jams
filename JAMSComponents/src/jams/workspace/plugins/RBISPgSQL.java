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

import jams.workspace.DataReader;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import jams.workspace.db.PGSQLConnector;
import jams.workspace.DataSet;
import jams.workspace.datatypes.DataValue;
import jams.workspace.datatypes.DoubleValue;
import jams.workspace.datatypes.LongValue;
import jams.workspace.datatypes.ObjectValue;
import jams.workspace.datatypes.StringValue;

/**
 *
 * @author Sven Kralisch
 */
public class RBISPgSQL implements DataReader {

    private static final int DOUBLE = 0;
    private static final int LONG = 1;
    private static final int STRING = 2;
    private static final int OBJECT = 3;
    private String user,  password,  host,  db,  query;
    private ResultSet rs;
    private ResultSetMetaData rsmd;
    private PGSQLConnector pgsql;
    private int numberOfColumns = -1;
    private int[] type;
    private boolean inited = false,  cleanedup = false;
    private DataSet[] currentData = null;            
            
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

    @Override
    public DataSet[] getData() {
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


    private DataSet[] getDBRows(long count) {
        
        ArrayList<DataSet> data = new ArrayList<DataSet>();
        DataSet dataSet;
        DataValue value;

        try {

            int i = 0;
            while ((i < count) && rs.next()) {
                i++;
                dataSet = new DataSet(numberOfColumns);

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
                        default:
                            value = new ObjectValue(rs.getObject(j + 1));
                            dataSet.setData(j, value);
                    }
                }
                data.add(dataSet);
            }

        } catch (SQLException sqlex) {
            System.out.println("RBISPgSQL: " + sqlex);
        }

        return data.toArray(new DataSet[data.size()]);
    }

    @Override
    public int init() {

        if (inited) {
            return 0;
        } else {
            inited = true;
        }

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

        pgsql = new PGSQLConnector(host, db, user, password);

        try {

            pgsql.connect();
            rs = pgsql.execQuery(query);
            rs.setFetchSize(0);
            rsmd = rs.getMetaData();
            numberOfColumns = rsmd.getColumnCount();
            type = new int[numberOfColumns];
            for (int i = 0; i < numberOfColumns; i++) {
                if (rsmd.getColumnTypeName(i + 1).startsWith("int")) {
                    type[i] = LONG;
                } else if (rsmd.getColumnTypeName(i + 1).startsWith("float")) {
                    type[i] = DOUBLE;
                } else if (rsmd.getColumnTypeName(i + 1).startsWith("numeric")) {
                    type[i] = DOUBLE;
                } else if (rsmd.getColumnTypeName(i + 1).startsWith("varchar")) {
                    type[i] = STRING;
                } else {
                    type[i] = OBJECT;
                }
            }
            cleanedup = false;

        } catch (SQLException sqlex) {
            System.err.println("RBISPgSQL: " + sqlex);
            return -1;
        }
        return 0;
    }

    @Override
    public int cleanup() {

        if (cleanedup) {
            return 0;
        } else {
            cleanedup = true;
        }

        try {
            rs.close();
            pgsql.close();
            inited = false;
        } catch (SQLException sqlex) {
            System.out.println("RBISPgSQL: " + sqlex);
            return -1;
        }
        
        return 0;
    }

    @Override
    public int numberOfColumns() {
        return numberOfColumns;
    }
}
