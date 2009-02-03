/*
 * TimeSpaceProcessor.java
 * Created on 1. Januar 2009, 18:32
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
package reg.dsproc;

import jams.data.JAMSCalendar;
import jams.data.JAMSDataFactory;
import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import reg.dsproc.DataStoreProcessor.AttributeData;

/**
 *
 * @author Sven Kralisch <sven.kralisch at uni-jena.de>
 */
public class TimeSpaceProcessor {

    private static final String TABLE_NAME_MONTHAVG = "MONTHAVG",  TABLE_NAME_YEARAVG = "YEARAVG",  TABLE_NAME_SPATAVG = "SPATAVG";

    private DataStoreProcessor dsdb;

    private Connection conn;

    private ArrayList<DataStoreProcessor.ContextData> contexts;

    private String spaceID,  timeID;

    private String timeFilter = null;

    public TimeSpaceProcessor(String fileName) {
        this(new DataStoreProcessor(fileName));
    }

    public TimeSpaceProcessor(DataStoreProcessor dsdb) {
        this.dsdb = dsdb;

        if (isTimeSpaceDatastore()) {

            spaceID = contexts.get(0).getName() + "ID";
            timeID = contexts.get(1).getName() + "ID";

            try {
                this.conn = dsdb.getH2Connection(true);
            } catch (SQLException ex) {
                System.out.println("Error while creating connection to H2 database of " + dsdb.getFileName());
            }
        }
    }

    public boolean isTimeSpaceDatastore() {
        ArrayList<DataStoreProcessor.ContextData> cntxt = dsdb.getContexts();
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

    public ResultSet getData(JAMSCalendar date) throws SQLException {

        date.setDateFormat("yyyy-MM-dd HH:mm:ss");
        String query = "SELECT " + spaceID;

        for (DataStoreProcessor.AttributeData attribute : dsdb.getAttributes()) {
            if (attribute.isSelected()) {
                query += ", " + attribute.getName();
            }
        }

        query += " FROM data WHERE " + timeID + "='" + date.toString() + "'";
        System.out.println(query);

        Statement stmt = conn.createStatement();
        ResultSet rs = stmt.executeQuery(query);
        System.out.println("finished");
        return rs;
    }

    /**
     * Send a custom select-query to the database
     * @param query The query string
     * @return A JDBC result set
     * @throws java.sql.SQLException
     */
    public ResultSet customSelectQuery(String query) throws SQLException {
        Statement stmt = conn.createStatement();
        ResultSet rs = stmt.executeQuery(query);
        return rs;
    }

    /**
     * Send a custom query to the database
     * @param query The query string
     * @return true, if the query was sent successfully, false otherwise
     * @throws java.sql.SQLException
     */
    public boolean customQuery(String query) throws SQLException {
        Statement stmt = conn.createStatement();
        boolean result = stmt.execute(query);
        return result;
    }

    /**
     * Get data from the database based on defined filters on time and space
     * @return The data as JDBC result set
     * @throws java.sql.SQLException
     */
    public ResultSet getData() throws SQLException {

        String query = "SELECT " + timeID + ", position FROM index";

        if (timeFilter != null) {

            query += " WHERE ";
            String s = null;
            if (timeFilter != null) {
                if (timeFilter.contains("%")) {
                    s = " LIKE '" + timeFilter + "'";
                } else {
                    s = " = '" + timeFilter + "'";
                }
                query += timeID + s;
            }
        }
        query += " ORDER BY position";

        System.out.println(query);

        ResultSet rs = customSelectQuery(query);
        return rs;
    }

    private int getSelectedAttribCount() {
        // get number of selected attributes
        int numSelected = 0;
        for (AttributeData a : getDataStoreProcessor().getAttributes()) {
            if (a.isSelected() && a.getType().equals("JAMSDouble")) {
                numSelected++;
            }
        }
        return numSelected;
    }

    /**
     * Gets the values of the selected attributes of a single spatial entity
     * at all time steps
     * @param id The id of the spatial entiy
     * @return A DataMatrix object containing one row per timestep with the
     * values of selected attributes in columns
     * @throws java.sql.SQLException
     * @throws java.io.IOException
     */
    public DataMatrix getEntityData(long id) throws SQLException, IOException {

        int attribCount = getSelectedAttribCount();
        int idPosition = 0;
        ArrayList<double[]> data = new ArrayList<double[]>();
        ArrayList<String> timeStamps = new ArrayList<String>();

        // reset filter and get the data
        resetTimeFilter();
        ResultSet rs = getData();

        // get first dataset to obtain id position
        if (rs.next()) {
            DataMatrix m = dsdb.getData(rs.getLong("POSITION"));
            idPosition = m.getIDPosition(String.valueOf(id));
            data.add(m.getRow(idPosition));
            timeStamps.add(rs.getTimestamp(timeID).toString());
        }

        // loop over datasets
        while (rs.next()) {
            DataMatrix m = dsdb.getData(rs.getLong("POSITION"));
            data.add(m.getRow(idPosition));
            timeStamps.add(rs.getTimestamp(timeID).toString());
        }

        double[][] dataArray = data.toArray(new double[attribCount][data.size()]);
        String[] timeStampArray = timeStamps.toArray(new String[timeStamps.size()]);

        DataMatrix result = new DataMatrix(dataArray, timeStampArray, this.getDataStoreProcessor());

        return result;

    }

    /**
     * Gets the overall spatial average values of the selected
     * attributes for all time steps
     * @return A DataMatrix object containing one row per timestep with the
     * spatial average values of selected attributes in columns
     * @throws java.sql.SQLException
     * @throws java.io.IOException
     */
    public DataMatrix getSpatialAvg() throws SQLException, IOException {

        DataMatrix result = null;

        if (!isTableExisting(TABLE_NAME_SPATAVG)) {
            return result;
        }

        int attribCount = getSelectedAttribCount();

        String q = "SELECT * FROM " + TABLE_NAME_SPATAVG;
        ResultSet rs = customSelectQuery(q);

        ArrayList<double[]> data = new ArrayList<double[]>();
        ArrayList<String> ids = new ArrayList<String>();
        while (rs.next()) {
            double[] rowdata = new double[attribCount];
            for (int i = 0; i < attribCount; i++) {
                // get data starting from the 3rd column
                rowdata[i] = rs.getDouble(i + 2);
            }
            data.add(rowdata);
            ids.add(rs.getString(1));
        }

        double[][] dataArray = data.toArray(new double[attribCount][data.size()]);
        String[] idArray = ids.toArray(new String[ids.size()]);
        result = new DataMatrix(dataArray, idArray, this.getDataStoreProcessor());

        return result;
    }

    /**
     * Gets the overall temporal average values of the selected
     * attributes for all entities
     * @return A DataMatrix object containing one row per entity with the
     * temporal average values of selected attributes in columns
     * @throws java.sql.SQLException
     * @throws java.io.IOException
     */
    public DataMatrix getTemporalAvg() throws SQLException, IOException {

        DataMatrix aggregate = getMonthlyAvg(1);
        if (aggregate == null) {
            return null;
        }
        for (int i = 2; i <= 12; i++) {
            DataMatrix monthlyData = getMonthlyAvg(i);
            if (monthlyData == null) {
                return null;
            }
            aggregate = aggregate.plus(monthlyData);
        }
        aggregate = aggregate.times(1f / 12);
        return aggregate;
    }

    /**
     * Gets the longtime monthly average values of the selected
     * attributes for all entities
     * @param month The month for which the average values shall be returned
     * @return A DataMatrix object containing one row per entity with the
     * longtime monthly average values of selected attributes in columns
     * @throws java.sql.SQLException
     * @throws java.io.IOException
     */
    public DataMatrix getMonthlyAvg(int month) throws SQLException, IOException {

        DataMatrix result = null;

        // check if the values have already been calculated, return null if not
        if (!isTableExisting(TABLE_NAME_MONTHAVG)) {
            return result;
        }

        int attribCount = getSelectedAttribCount();

        // create and send a select statement to get the data from the database
        String q = "SELECT * FROM " + TABLE_NAME_MONTHAVG + " WHERE MONTH = " + month;
        ResultSet rs = customSelectQuery(q);

        // iterate through the result set
        ArrayList<double[]> data = new ArrayList<double[]>();
        ArrayList<Long> ids = new ArrayList<Long>();
        while (rs.next()) {
            double[] rowdata = new double[attribCount];
            for (int i = 0; i < attribCount; i++) {
                // get data starting from the 3rd column
                rowdata[i] = rs.getDouble(i + 3);
            }
            data.add(rowdata);
            ids.add(rs.getLong(2));
        }

        // create a DataMatrix object from the results
        double[][] dataArray = data.toArray(new double[attribCount][data.size()]);
        Long[] idArray = ids.toArray(new Long[ids.size()]);
        result = new DataMatrix(dataArray, idArray, this.getDataStoreProcessor());

        return result;
    }

    private boolean isTableExisting(String tableName) throws SQLException {
        String q = "SELECT * FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_NAME='" + tableName + "'";
        ResultSet rs = customSelectQuery(q);
        if (rs.next()) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * Initialises the calculation of yearly average values of the selected
     * attributes for all entities
     * @throws java.sql.SQLException
     * @throws java.io.IOException
     */
    public void calcYearlyAvg() throws SQLException, IOException {

        // get number of selected attributes
        int numSelected = getSelectedAttribCount();

        // create the db tables to store the calculated monthly means
        customQuery("DROP TABLE IF EXISTS " + TABLE_NAME_YEARAVG);
        String q = "CREATE TABLE " + TABLE_NAME_YEARAVG + " (";
        q += "YEAR " + DataStoreProcessor.TYPE_MAP.get("JAMSInteger") + ",";
        q += spaceID + " " + DataStoreProcessor.TYPE_MAP.get("JAMSLong") + ",";

        for (int i = 1; i <= numSelected; i++) {
            q += "a_" + i + " " + DataStoreProcessor.TYPE_MAP.get("JAMSDouble") + ",";
        }
        q = q.substring(0, q.length() - 1);
        q += ")";
        customQuery(q);

        // get min and max dates
        q = "SELECT min(" + timeID + ") AS MINDATE, max(" + timeID + ") AS MAXDATE FROM index";
        System.out.println(q);
        ResultSet rs = customSelectQuery(q);
        rs.next();
        JAMSCalendar minDate = JAMSDataFactory.createCalendar();
        JAMSCalendar maxDate = JAMSDataFactory.createCalendar();
        minDate.setValue(rs.getTimestamp("MINDATE").toString());
        maxDate.setValue(rs.getTimestamp("MAXDATE").toString());

        // loop over years
        for (int i = minDate.get(JAMSCalendar.YEAR); i <= maxDate.get(JAMSCalendar.YEAR); i++) {
            String filterString = String.format("%04d", i) + "-%-%";
            calcTempAvg(filterString, TABLE_NAME_YEARAVG, String.valueOf(i));
        }
    }

    /**
     * Initialises the calculation of longterm monthly average values of the
     * selected attributes for all entities
     * @throws java.sql.SQLException
     * @throws java.io.IOException
     */
    public void calcMonthlyAvg() throws SQLException, IOException {

        // get number of selected attributes
        int numSelected = getSelectedAttribCount();

        // create the db tables to store the calculated monthly means
        customQuery("DROP TABLE IF EXISTS " + TABLE_NAME_MONTHAVG);
        String q = "CREATE TABLE " + TABLE_NAME_MONTHAVG + " (";
        q += "MONTH " + DataStoreProcessor.TYPE_MAP.get("JAMSInteger") + ",";
        q += spaceID + " " + DataStoreProcessor.TYPE_MAP.get("JAMSLong") + ",";

        for (int i = 1; i <= numSelected; i++) {
            q += "a_" + i + " " + DataStoreProcessor.TYPE_MAP.get("JAMSDouble") + ",";
        }
        q = q.substring(0, q.length() - 1);
        q += ")";
        customQuery(q);

        // loop over months
        for (int i = 1; i <= 12; i++) {
            String filterString = "%-" + String.format("%02d", i) + "-%";
            calcTempAvg(filterString, TABLE_NAME_MONTHAVG, String.valueOf(i));
        }
    }

    private DataMatrix calcTempAvg(String filter, String tableName, String id) throws SQLException, IOException {

        // set the temporal filter and get the result set
        setTimeFilter(filter);
        ResultSet rs = getData();

        // we have a set of positions now, so get the matrixes and rock'n roll
        // get the first dataset
        long position;
        DataMatrix aggregate;
        int count = 1;

        if (rs.next()) {
            position = rs.getLong("POSITION");
            aggregate = dsdb.getData(position);
        } else {
            return null;
        }

        // loop over datasets for current month
        while (rs.next()) {
            position = rs.getLong("POSITION");
            DataMatrix m = dsdb.getData(position);
            aggregate = aggregate.plus(m);
            count++;
        }

        aggregate = aggregate.times(1f / count);

        Object ids[] = aggregate.getIds();
        double data[][] = aggregate.getArray();
        for (int i = 0; i < data.length; i++) {
            String q = "INSERT INTO " + tableName + " VALUES (" + id + ", " + ids[i];
            for (int j = 0; j < data[i].length; j++) {
                q += ", " + data[i][j];
            }
            q += ")";
            customQuery(q);
        }

        return aggregate;
    }
    
    /**
     * Initialises the calculation of overall spatial average values of the
     * selected attributes for all time steps
     * @return A DataMatrix object containing one row per timestep with the
     * spatial average values of selected attributes in columns
     * @throws java.sql.SQLException
     * @throws java.io.IOException
     */
    public DataMatrix calcSpatialAvg() throws SQLException, IOException {

        int attribCount = getSelectedAttribCount();
        long position;
        ArrayList<double[]> data = new ArrayList<double[]>();
        ArrayList<String> timeStamps = new ArrayList<String>();

        // create the db table to store the calculated spatial mean
        customQuery("DROP TABLE IF EXISTS " + TABLE_NAME_SPATAVG);
        String q = "CREATE TABLE " + TABLE_NAME_SPATAVG + " (";
        q += timeID + " " + DataStoreProcessor.TYPE_MAP.get("JAMSCalendar") + ",";

        for (int i = 1; i <= attribCount; i++) {
            q += "a_" + i + " " + DataStoreProcessor.TYPE_MAP.get("JAMSDouble") + ",";
        }
        q = q.substring(0, q.length() - 1);
        q += ")";
        customQuery(q);

        // reset filter and get the data
        resetTimeFilter();
        ResultSet rs = getData();

        // loop over datasets
        while (rs.next()) {
            position = rs.getLong("POSITION");
            DataMatrix m = dsdb.getData(position);
            data.add(m.getAvgRow());
            timeStamps.add(rs.getTimestamp(timeID).toString());
        }

        double[][] dataArray = data.toArray(new double[attribCount][data.size()]);
        String[] timeStampArray = timeStamps.toArray(new String[timeStamps.size()]);

        // write the calculated array to the database
        for (int i = 0; i < dataArray.length; i++) {
            q = "INSERT INTO " + TABLE_NAME_SPATAVG + " VALUES ('" + timeStampArray[i] + "'";
            for (int j = 0; j < dataArray[i].length; j++) {
                q += ", " + dataArray[i][j];
            }
            q += ")";
            customQuery(q);
        }

        DataMatrix result = new DataMatrix(dataArray, timeStampArray, this.getDataStoreProcessor());

        return result;
    }

    public static void output(ResultSet rs) throws SQLException {
        ResultSetMetaData rsmd = rs.getMetaData();
        int numberOfColumns = rsmd.getColumnCount();

        for (int i = 1; i <= numberOfColumns; i++) {
            System.out.print(rsmd.getColumnName(i) + "\t");
        }
        System.out.println();

        while (rs.next()) {
            for (int i = 1; i <= numberOfColumns; i++) {
                System.out.print(rs.getString(i) + "\t");
            }
            System.out.println();
        }
    }

    /**
     * @param timeFilter the timeIDFilter to set
     */
    public void setTimeFilter(String timeFilter) {
        this.timeFilter = timeFilter;
    }

    public void resetTimeFilter() {
        timeFilter = null;
    }

    /**
     * @return the h2ds
     */
    public DataStoreProcessor getDataStoreProcessor() {
        return dsdb;
    }

    public static void main(String[] args) throws Exception {
        TimeSpaceProcessor tsproc = new TimeSpaceProcessor("D:/jamsapplication/JAMS-Gehlberg/output/current/HRULoop_0.dat");
        tsproc.isTimeSpaceDatastore();

//        JAMSCalendar date = new JAMSCalendar();
//        date.setValue("1995-11-01 07:30");
//
//        //output(tsproc.getData(date));
//        tsproc.setSpaceFilter("42");
//        tsproc.setTimeFilter("%-02-%");
//        tsproc.setAggregator("sum");
//
//        output(tsproc.getData());

        ArrayList<DataStoreProcessor.AttributeData> attribs = tsproc.getDataStoreProcessor().getAttributes();
        for (DataStoreProcessor.AttributeData attrib : attribs) {
            if (attrib.getName().startsWith("act")) {
                attrib.setSelected(true);
                System.out.print(attrib.getName() + " ");
            } else {
                attrib.setSelected(false);
            }
        }
        System.out.println();

        int c = 4;

        DataMatrix m = null;
        switch (c) {
            case 0:
                tsproc.calcMonthlyAvg();
                m = tsproc.getMonthlyAvg(1);
                break;
            case 1:
                tsproc.calcSpatialAvg();
                m = tsproc.getSpatialAvg();
                break;
            case 2:
                m = tsproc.getTemporalAvg();
                break;
            case 3:
                tsproc.calcYearlyAvg();
                m = tsproc.getMonthlyAvg(1);
                break;
            case 4:
                m = tsproc.getEntityData(1);
                break;
        }

        if (m == null) {
            return;
        }
        for (Object o : m.getIds()) {
            System.out.print(o + " ");
        }
        System.out.println();
        m.print(5, 3);

        //output(tsproc.customQuery("SELECT count(*) from data "));
        tsproc.dsdb.close();
    }
}
