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
import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Observable;
import java.util.Observer;
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

    private ProcessingProgressObservable processingProgressObservable = new ProcessingProgressObservable();

    private boolean abortOperation = false;

    public TimeSpaceProcessor(File file) {
        this(new DataStoreProcessor(file));
    }

    public TimeSpaceProcessor(DataStoreProcessor dsdb) {
        this.dsdb = dsdb;

        if (isTimeSpaceDatastore()) {

            spaceID = contexts.get(0).getName() + "ID";
            timeID = contexts.get(1).getName() + "ID";

            try {
                this.conn = dsdb.getH2Connection(true);
            } catch (SQLException ex) {
                System.out.println("Error while creating connection to H2 database of " + dsdb.getFile());
            }
        }
    }

    /**
     * Check if this is a datastore that contains spatial data that vary in time
     * @return True or false
     */
    public synchronized boolean isTimeSpaceDatastore() {
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

    /**
     * Send a custom select-query to the database
     * @param query The query string
     * @return A JDBC result set
     * @throws java.sql.SQLException
     */
    public synchronized ResultSet customSelectQuery(String query) throws SQLException {
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
    public synchronized boolean customQuery(String query) throws SQLException {
        Statement stmt = conn.createStatement();
        boolean result = stmt.execute(query);
        return result;
    }

    /**
     * Get data from the database based on defined filters on time and space
     * @return The data as JDBC result set
     * @throws java.sql.SQLException
     */
    public synchronized ResultSet getData() throws SQLException {

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

//        System.out.println(query);

        ResultSet rs = customSelectQuery(query);
        return rs;
    }

    /**
     * Check how many results are created
     * @return The number of results
     * @throws java.sql.SQLException
     */
    public synchronized int getResultCount() throws SQLException {

        String query = "SELECT count(*) as COUNT FROM index";

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

        ResultSet rs = customSelectQuery(query);
        rs.next();
        return rs.getInt("COUNT");
    }

    private synchronized int getSelectedAttribCount() {
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
     * Gets the values of the selected attributes for all entities at a
     * specific date
     * @param date The date for which the data shall be returned
     * @return A DataMatrix object containing one row per entity with the
     * attribute values in columns
     * @throws java.sql.SQLException
     * @throws java.io.IOException
     */
    public synchronized DataMatrix getTemporalData(JAMSCalendar date) throws SQLException, IOException {

        String oldFormat = ((SimpleDateFormat) date.getDateFormat()).toPattern();
        date.setDateFormat("yyyy-MM-dd HH:mm:ss");
        String filterString = date.toString();
        date.setDateFormat(oldFormat);

        setTimeFilter(filterString);
        ResultSet rs = getData();
        DataMatrix result = null;
        if (rs.next()) {
            long position = rs.getLong("POSITION");
            result = dsdb.getData(position);
        }
        return result;
    }

    /**
     * Gets the mean values of the selected attributes for a set of time steps
     * given by an array of JAMSCalendar objects
     * @param dates An array of JAMSCalendar objects
     * @return A DataMatrix object containing one row per entity with the
     * mean values of selected attributes in columns
     * @throws java.sql.SQLException
     * @throws java.io.IOException
     */
    public synchronized DataMatrix getTemporalMean(JAMSCalendar[] dates) throws SQLException, IOException {

        if ((dates == null) || (dates.length == 0)) {
            return null;
        }

        DataMatrix aggregate = null, matrix;
        int count = 0, percent = 0;

        // loop over dates 
        for (JAMSCalendar date : dates) {

            if (abortOperation) {
                return null;
            }

            matrix = getTemporalData(date);

            if (matrix != null) {
                if (aggregate == null) {
                    aggregate = matrix;
                    count = 1;
                } else {
                    aggregate = aggregate.plus(matrix);
                    count++;
                }
            }

            // update the observer
            int current = Math.round((count / (float) dates.length) * 100);
            if (current > percent) {
                percent = current;
                processingProgressObservable.setProgress(percent);
            }
        }

        if (aggregate != null) {
            aggregate = aggregate.times(1d / count);
        }

        return aggregate;
    }

    /**
     * Gets the mean values of the selected attributes for a set of time steps
     * matching a given pattern
     * @param datePattern A date pattern string
     * @return A DataMatrix object containing one row per entity with the
     * mean values of selected attributes in columns
     * @throws java.sql.SQLException
     * @throws java.io.IOException
     */
    public synchronized DataMatrix getTemporalMean(String datePattern) throws SQLException, IOException {

        // set the temporal filter and get the result set
        setTimeFilter(datePattern);

        ResultSet rs = getData();
        int max = getResultCount();

        // we have a set of positions now, so get the matrixes and rock'n roll
        // get the first dataset
        long position;
        DataMatrix aggregate;
        int count = 1, percent = 0;
        ;

        if (rs.next()) {
            position = rs.getLong("POSITION");
            aggregate = dsdb.getData(position);
        } else {
            return null;
        }

        // loop over datasets for current month
        while (rs.next()) {

            if (abortOperation) {
                return null;
            }

            position = rs.getLong("POSITION");
            DataMatrix m = dsdb.getData(position);
            aggregate = aggregate.plus(m);
            count++;

            // update the observer
            int current = Math.round((count * 100) / max);
            if (current > percent) {
                percent = current;
                processingProgressObservable.setProgress(percent);
            }
        }

        aggregate = aggregate.times(1d / count);
        return aggregate;
    }

    /**
     * Gets the mean values of the selected attributes for an array of
     * spatial entities at all time steps
     * @param id The id array of the spatial enties
     * @return A DataMatrix object containing one row per timestep with the
     * mean values of selected attributes in columns
     * @throws java.sql.SQLException
     * @throws java.io.IOException
     */
    public synchronized DataMatrix getSpatialMean(long[] ids) throws SQLException, IOException {

        int attribCount = getSelectedAttribCount();
        int[] idPosition = new int[ids.length];
        ArrayList<double[]> data = new ArrayList<double[]>();
        ArrayList<String> timeStamps = new ArrayList<String>();

        // reset filter and get the data
        resetTimeFilter();
        ResultSet rs = getData();

        // get first dataset to obtain id position
        if (rs.next()) {
            DataMatrix m = dsdb.getData(rs.getLong("POSITION"));
            ArrayList<double[]> a = new ArrayList<double[]>();
            for (int i = 0; i < ids.length; i++) {
                idPosition[i] = m.getIDPosition(String.valueOf(ids[i]));
                if (idPosition[i] == -1) {
                    return null;
                }
                a.add(m.getRow(idPosition[i]));
            }
            data.add(getMean(a));
            timeStamps.add(rs.getTimestamp(timeID).toString());
        } else {
            return null;
        }

        int percent = 0, max = dsdb.getContexts().get(1).getSize();
        float counter = 1;

        // loop over datasets
        while (rs.next()) {

            if (abortOperation) {
                return null;
            }

            DataMatrix m = dsdb.getData(rs.getLong("POSITION"));
            ArrayList<double[]> a = new ArrayList<double[]>();
            for (int i = 0; i < ids.length; i++) {
                a.add(m.getRow(idPosition[i]));
            }
            data.add(getMean(a));
            timeStamps.add(rs.getTimestamp(timeID).toString());

            // update the observer
            counter++;
            int current = Math.round((counter / max) * 100);
            if (current > percent) {
                percent = current;
                processingProgressObservable.setProgress(percent);
            }
        }

        double[][] dataArray = data.toArray(new double[data.size()][attribCount]);
        String[] timeStampArray = timeStamps.toArray(new String[timeStamps.size()]);

        DataMatrix result = new DataMatrix(dataArray, timeStampArray, this.getDataStoreProcessor());

        return result;
    }

    private synchronized double[] getMean(ArrayList<double[]> a) {

        double[] result = a.get(0);
        double[] b;
        for (int i = 1; i < a.size(); i++) {
            b = a.get(i);
            for (int c = 0; c < result.length; c++) {
                result[c] += b[c];
            }
        }
        for (int c = 0; c < result.length; c++) {
            result[c] /= a.size();
        }

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
    public synchronized DataMatrix getSpatialMean() throws SQLException, IOException {

        DataMatrix result = null;

        if (!isSpatMeanExisiting()) {
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

        double[][] dataArray = data.toArray(new double[data.size()][attribCount]);
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
    public synchronized DataMatrix getTemporalAvg() throws SQLException, IOException {

        DataMatrix aggregate = getMonthlyMean(1);
        if (aggregate == null) {
            return null;
        }
        for (int i = 2; i <= 12; i++) {
            DataMatrix monthlyData = getMonthlyMean(i);
            if (monthlyData == null) {
                return null;
            }
            aggregate = aggregate.plus(monthlyData);
        }
        aggregate = aggregate.times(1d / 12);
        return aggregate;
    }

    /**
     * Gets the longtime monthly average values of the selected
     * attributes for all entities
     * @param month The month for which the average values shall be returned
     * as int value between 1 and 12
     * @return A DataMatrix object containing one row per entity with the
     * longtime monthly average values of selected attributes in columns
     * @throws java.sql.SQLException
     * @throws java.io.IOException
     */
    public synchronized DataMatrix getMonthlyMean(int month) throws SQLException, IOException {

        DataMatrix result = null;

        // check if the values have already been calculated, return null if not
        if (!isMonthlyMeanExisiting()) {
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
        double[][] dataArray = data.toArray(new double[data.size()][attribCount]);
        Long[] idArray = ids.toArray(new Long[ids.size()]);
        result = new DataMatrix(dataArray, idArray, this.getDataStoreProcessor());

        return result;
    }

    /**
     * Gets the yearly average values of the selected
     * attributes for all entities
     * @param year The year for which the average values shall be returned
     * @return A DataMatrix object containing one row per entity with the
     * longtime monthly average values of selected attributes in columns
     * @throws java.sql.SQLException
     * @throws java.io.IOException
     */
    public synchronized DataMatrix getYearlyMean(int year) throws SQLException, IOException {

        DataMatrix result = null;

        // check if the values have already been calculated, return null if not
        if (!isYearlyMeanExisiting()) {
            return result;
        }

        int attribCount = getSelectedAttribCount();

        // create and send a select statement to get the data from the database
        String q = "SELECT * FROM " + TABLE_NAME_YEARAVG + " WHERE YEAR = " + year;
        ResultSet rs = customSelectQuery(q);

        // iterate through the result set
        ArrayList<double[]> data = new ArrayList<double[]>();
        ArrayList<Long> ids = new ArrayList<Long>();

        if (rs.next()) {
            double[] rowdata = new double[attribCount];
            for (int i = 0; i < attribCount; i++) {
                // get data starting from the 3rd column
                rowdata[i] = rs.getDouble(i + 3);
            }
            data.add(rowdata);
            ids.add(rs.getLong(2));
        } else {
            return null;
        }

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
        double[][] dataArray = data.toArray(new double[data.size()][attribCount]);
        Long[] idArray = ids.toArray(new Long[ids.size()]);
        result = new DataMatrix(dataArray, idArray, this.getDataStoreProcessor());

        return result;
    }

    private synchronized boolean isTableExisting(String tableName) throws SQLException {
        String q = "SELECT * FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_NAME='" + tableName + "'";
        ResultSet rs = customSelectQuery(q);
        if (rs.next()) {
            return true;
        } else {
            return false;
        }
    }

    public boolean isYearlyMeanExisiting() throws SQLException {
        return isTableExisting(TABLE_NAME_YEARAVG);
    }

    public boolean isMonthlyMeanExisiting() throws SQLException {
        return isTableExisting(TABLE_NAME_MONTHAVG);
    }

    public boolean isSpatMeanExisiting() throws SQLException {
        return isTableExisting(TABLE_NAME_SPATAVG);
    }

    public synchronized void deleteCache() throws SQLException {
        String[] tables = {TABLE_NAME_YEARAVG, TABLE_NAME_SPATAVG, TABLE_NAME_MONTHAVG};
        for (String table : tables) {
            customQuery("DROP TABLE IF EXISTS " + table);
        }
    }

    /**
     * Initialises the calculation of yearly average values of the selected
     * attributes for all entities
     * @throws java.sql.SQLException
     * @throws java.io.IOException
     */
    public synchronized void calcYearlyMean() throws SQLException, IOException {

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
        ResultSet rs = customSelectQuery(q);
        rs.next();
        JAMSCalendar minDate = JAMSDataFactory.createCalendar();
        JAMSCalendar maxDate = JAMSDataFactory.createCalendar();
        minDate.setValue(rs.getTimestamp("MINDATE").toString());
        maxDate.setValue(rs.getTimestamp("MAXDATE").toString());

        int percent = 0, max = maxDate.get(JAMSCalendar.YEAR) - minDate.get(JAMSCalendar.YEAR) + 1;
        float counter = 0;

        // loop over years
        for (int i = minDate.get(JAMSCalendar.YEAR); i <= maxDate.get(JAMSCalendar.YEAR); i++) {
            String filterString = String.format("%04d", i) + "-%-%";
            calcTemporalMean(filterString, TABLE_NAME_YEARAVG, String.valueOf(i));

            if (abortOperation) {
                customQuery("DROP TABLE IF EXISTS " + TABLE_NAME_YEARAVG);
                return;
            }

            // update the observer
            counter++;
            int current = Math.round((counter / max) * 100);
            if (current > percent) {
                percent = current;
                processingProgressObservable.setProgress(percent);
            }
        }
    }

    /**
     * Get the years that data are available for
     * @return An int array containing the years
     * @throws java.sql.SQLException
     */
    public synchronized int[] getYears() throws SQLException {

        // get min and max dates
        String q = "SELECT min(" + timeID + ") AS MINDATE, max(" + timeID + ") AS MAXDATE FROM index";
        ResultSet rs = customSelectQuery(q);
        rs.next();
        JAMSCalendar minDate = JAMSDataFactory.createCalendar();
        JAMSCalendar maxDate = JAMSDataFactory.createCalendar();
        minDate.setValue(rs.getTimestamp("MINDATE").toString());
        maxDate.setValue(rs.getTimestamp("MAXDATE").toString());

        int startYear = minDate.get(JAMSCalendar.YEAR);
        int endYear = maxDate.get(JAMSCalendar.YEAR);
        int[] years = new int[endYear - startYear + 1];
        int c = 0;

        for (int i = startYear; i <= endYear; i++) {
            years[c++] = i;
        }

        return years;
    }

    /**
     * Initialises the calculation of longterm monthly average values of the
     * selected attributes for all entities
     * @throws java.sql.SQLException
     * @throws java.io.IOException
     */
    public synchronized void calcMonthlyMean() throws SQLException, IOException {

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

        int percent = 0;

        // loop over months
        for (int i = 1; i <= 12; i++) {

            // calc the monthly average values
            String filterString = "%-" + String.format("%02d", i) + "-%";
            calcTemporalMean(filterString, TABLE_NAME_MONTHAVG, String.valueOf(i));

            if (abortOperation) {
                customQuery("DROP TABLE IF EXISTS " + TABLE_NAME_MONTHAVG);
                return;
            }

            // update the observer
            int current = Math.round((i / 12f) * 100);
            if (current > percent) {
                percent = current;
                processingProgressObservable.setProgress(percent);
            }
        }
    }

    private synchronized DataMatrix calcTemporalMean(String filter, String tableName, String id) throws SQLException, IOException {

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

            if (abortOperation) {
                return null;
            }

            position = rs.getLong("POSITION");
            DataMatrix m = dsdb.getData(position);
            aggregate = aggregate.plus(m);
            count++;
        }

        aggregate = aggregate.times(1d / count);

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
    public synchronized DataMatrix calcSpatialMean() throws SQLException, IOException {

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

        int percent = 0, max = dsdb.getContexts().get(1).getSize();
        float counter = 0;

        // loop over datasets
        while (rs.next()) {
            position = rs.getLong("POSITION");
            DataMatrix m = dsdb.getData(position);
            data.add(m.getAvgRow());
            timeStamps.add(rs.getTimestamp(timeID).toString());

            if (abortOperation) {
                customQuery("DROP TABLE IF EXISTS " + TABLE_NAME_SPATAVG);
                return null;
            }

            // update the observer
            counter++;
            int current = Math.round((counter / max) * 100);
            if (current > percent) {
                percent = current;
                processingProgressObservable.setProgress(percent);
            }
        }

        double[][] dataArray = data.toArray(new double[data.size()][attribCount]);
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

    /**
     * Gets the IDs of the entities stored in the dataset
     * @return An array of ID values
     * @throws java.sql.SQLException
     * @throws java.io.IOException
     */
    public synchronized Long[] getEntityIDs() throws SQLException, IOException {

        Object[] ids = null;

        ResultSet rs = customSelectQuery("SELECT * FROM index LIMIT 1");
        if (rs.next()) {
            long position = rs.getLong("POSITION");
            DataMatrix m = dsdb.getData(position);
            ids = m.getIds();
        }

        if (ids == null) {
            return null;
        }

        ArrayList<Long> result = new ArrayList<Long>();
        for (Object o : ids) {
            result.add(Long.parseLong(o.toString()));
        }

        Collections.sort(result);

        return result.toArray(new Long[result.size()]);
    }

    /**
     * Get all available time steps
     * @return An array of calendar objects representing the time steps
     * @throws java.sql.SQLException
     */
    public synchronized JAMSCalendar[] getTimeSteps() throws SQLException {

        ArrayList<JAMSCalendar> result = new ArrayList<JAMSCalendar>();

        ResultSet rs = customSelectQuery("SELECT * FROM index");
        while (rs.next()) {
            Timestamp ts = rs.getTimestamp(timeID);
            JAMSCalendar cal = JAMSDataFactory.createCalendar();
            cal.setValue(ts.toString());
            result.add(cal);
        }

        return result.toArray(new JAMSCalendar[result.size()]);
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
    public synchronized void setTimeFilter(String timeFilter) {
        this.timeFilter = timeFilter;
    }

    public synchronized void resetTimeFilter() {
        timeFilter = null;
    }

    /**
     * @return the h2ds
     */
    public DataStoreProcessor getDataStoreProcessor() {
        return dsdb;
    }

    public void close() throws SQLException {
        dsdb.close();
    }

    public void addProcessingProgressObserver(Observer o) {
        processingProgressObservable.addObserver(o);
    }

    public void sendAbortOperation() {
        this.abortOperation = true;
        synchronized (this) {
            this.abortOperation = false;
        }
    }

    private class ProcessingProgressObservable extends Observable {

        private int progress;

        private void setProgress(int progress) {
            this.progress = progress;
            this.setChanged();
            this.notifyObservers(progress);
        }
    }

    public static void main(String[] args) throws Exception {
        TimeSpaceProcessor tsproc = new TimeSpaceProcessor(new File("D:/jamsapplication/JAMS-Gehlberg/output/current/HRULoop_0.dat"));
        tsproc.isTimeSpaceDatastore();

//        JAMSCalendar date = new JAMSCalendar();
//        date.setValue("1995-11-01 07:30");
//
//        //output(tsproc.getTemporalData(date));
//        tsproc.setSpaceFilter("42");
//        tsproc.setTimeFilter("%-02-%");
//        tsproc.setAggregator("sum");
//
//        output(tsproc.getTemporalData());

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

        tsproc.addProcessingProgressObserver(new Observer() {

            public void update(Observable o, Object arg) {
                System.out.println("Progress: " + arg);
            }
        });


        tsproc.deleteCache();

        int c = 4;

        DataMatrix m = null;
        switch (c) {
            case 0:
                // calc/get longterm monthly mean values
                tsproc.calcMonthlyMean();
                m = tsproc.getMonthlyMean(12);
                break;
            case 1:
                // calc/get yearly mean values
                //tsproc.calcYearlyMean();
                m = tsproc.getYearlyMean(1997);
                break;
            case 2:
                // get overall temporal mean values
                // (based on longterm monthly mean values)
                m = tsproc.getTemporalAvg();
                break;
            case 3:
                // calc/get overall spatial mean values
                tsproc.calcSpatialMean();
                m = tsproc.getSpatialMean();
                break;
            case 4:
                // get spatial mean values for selected entities
                //long[] ids = {1, 3, 5, 7, 9};
                long[] ids = {1};
                m = tsproc.getSpatialMean(ids);
                break;
            case 5:
                // get values for a specific date
                JAMSCalendar cal = JAMSDataFactory.createCalendar();
                cal.setValue("2000-10-31 07:30");
                m = tsproc.getTemporalData(cal);
                break;
            case 6:
                // get temporal mean values matching a date pattern
                m = tsproc.getTemporalMean("2%-10-30 07:30%");
                break;
            case 7:
                // get temporal mean values for an array of specific dates
                JAMSCalendar[] dates = new JAMSCalendar[2];
                dates[0] = JAMSDataFactory.createCalendar();
                dates[0].setValue("2000-10-31 07:30");
                dates[1] = JAMSDataFactory.createCalendar();
                dates[1].setValue("2000-10-30 07:30");
                m = tsproc.getTemporalMean(dates);
                break;
            case 8:
                dates = tsproc.getTimeSteps();
                for (JAMSCalendar date : dates) {
                    System.out.println(date);
                }
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
