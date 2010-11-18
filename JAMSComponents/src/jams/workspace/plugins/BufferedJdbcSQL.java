/*
 * JdbcSQL.java
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
import java.text.SimpleDateFormat;
import java.util.GregorianCalendar;
import java.util.TimeZone;

/**
 *
 * @author Sven Kralisch
 */
public class BufferedJdbcSQL implements DataReader {

    private static final int DOUBLE = 0;
    private static final int LONG = 1;
    private static final int STRING = 2;
    private static final int TIMESTAMP = 3;
    private static final int OBJECT = 4;
    private String user, password, host, db, query, driver;
    transient private BufferedResultSet rs;
    transient private ResultSetMetaData rsmd;
    transient private BufferedJdbcSQLConnector pgsql;
    private int numberOfColumns = -1;
    private int[] type;
    private final boolean alwaysReconnect = false;
    private DefaultDataSet[] currentData = null;
    private boolean isClosed;
    private int offset = 0;

    public void JdbcSQL() {
        isClosed = true;
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

    public void setDriver(String driver) {
        this.driver = driver;
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
            rs.skip(count);
            rs.next();
            System.out.println("after skip position is: " + rs.getString(0));            
        } catch (Exception e) {
            e.printStackTrace();
        }
        return true;
    }

    private DefaultDataSet[] getDBRows(long count) {

        ArrayList<DefaultDataSet> data = new ArrayList<DefaultDataSet>();
        DefaultDataSet dataSet;
        DataValue value;

        try {

            int i = 0;
            while ((i < count) && rs.next()) {
                i++;
                offset++;
                dataSet = new DefaultDataSet(numberOfColumns);

                for (int j = 0; j < numberOfColumns; j++) {

                    switch (type[j]) {
                        case DOUBLE:
                            double v = rs.getDouble(j + 1);
                            if (!rs.wasNull()) {
                                value = new DoubleValue(v);
                            } else {
                                value = new StringValue("");
                            }
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
                data.add(dataSet);
            }

        } catch (SQLException sqlex) {
            System.out.println("jdbcSQL: " + sqlex);
        }

        return data.toArray(new DefaultDataSet[data.size()]);
    }

    void establishConnection() {
        try {
            if (pgsql == null) {
                pgsql = new BufferedJdbcSQLConnector(host, db, user, password, driver);
                pgsql.connect();
                isClosed = false;
            } else if (this.alwaysReconnect) {
                pgsql.close();
                pgsql = null;
                isClosed = true;
                establishConnection();
            }
        } catch (SQLException sqlex) {
            System.err.println("jdbcSQL: " + sqlex);
            sqlex.printStackTrace();
            isClosed = true;
        }
    }

    public void query() {
        establishConnection();
        try {
            if (rs != null) {
                rs.close();
            }

            rs = pgsql.execQuery(query);
            //rs.setFetchSize(0);
            rsmd = rs.getMetaData();
            numberOfColumns = rsmd.getColumnCount();
            type = new int[numberOfColumns];
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
        } catch (SQLException sqlex) {
            System.err.println("jdbcSQL: " + sqlex);
        }
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

        if (query == null) {
            return -1;
        }

        if (driver == null) {
            driver = "jdbc:postgresql";
        }

        query();
        return 1;
    }

    @Override
    public int cleanup() {
        try {
            if (rs != null) {
                rs.close();
                rs = null;
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

    @Override
    public int numberOfColumns() {
        return numberOfColumns;
    }

    public void setState(DataReaderState state) {
    }

    public DataReaderState getState() {
        return null;
    }

    public void getState(java.io.ObjectOutputStream stream) throws IOException {
        stream.writeBoolean(isClosed);
        stream.writeInt(this.offset);
    }

    public void setState(java.io.ObjectInputStream stream) throws IOException, ClassNotFoundException {
        isClosed = stream.readBoolean();
        int oldOffset = stream.readInt();
        if (isClosed) {
            this.cleanup();
            return;
        }
        query();
        try {
            Thread.sleep(1000);
        } catch (Exception e) {
        }
        this.skip(oldOffset);
    }
}
