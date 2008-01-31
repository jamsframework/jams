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
import rbis.virtualws.DataValue;
import rbis.virtualws.DoubleValue;
import rbis.virtualws.LongValue;
import rbis.virtualws.ObjectValue;
import rbis.virtualws.StringValue;

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
    private int numberOfColumns;
    private int[] type;

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
        DataValue value;

        try {

            int i = 0;
            while (rs.next() && i < count) {
                i++;
                dataSet = new DataSet(numberOfColumns);

                for (int j = 0; j < numberOfColumns; j++) {

                    switch (type[j]) {
                        case DOUBLE:
                            value = new DoubleValue(rs.getDouble(j + 1));
                            dataSet.setData(j, value);
                        case LONG:
                            value = new LongValue(rs.getLong(j + 1));
                            dataSet.setData(j, value);
                        case STRING:
                            value = new StringValue(rs.getString(j + 1));
                            dataSet.setData(j, value);
                        default:
                            value = new ObjectValue(rs.getObject(j + 1));
                            dataSet.setData(j, value);
                    }
                }
                data.add(dataSet);
            }

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
            numberOfColumns = rsmd.getColumnCount();
            type = new int[numberOfColumns];
            for (int i = 0; i < numberOfColumns; i++) {
                if (rsmd.getColumnTypeName(i + 1).startsWith("int")) {
                    type[i] = LONG;
                } else if (rsmd.getColumnTypeName(i + 1).startsWith("float")) {
                    type[i] = DOUBLE;
                } else if (rsmd.getColumnTypeName(i + 1).startsWith("varchar")) {
                    type[i] = DOUBLE;
                } else {
                    type[i] = OBJECT;
                }
            }

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
