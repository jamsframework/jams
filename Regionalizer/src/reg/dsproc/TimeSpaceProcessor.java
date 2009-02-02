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

    private static final String TABLE_NAME_MONTHAVG = "MONTHAVG",
            TABLE_NAME_SPATAVG = "SPATAVG";

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

    public ResultSet customQuery(String query) throws SQLException {
        Statement stmt = conn.createStatement();
        ResultSet rs = stmt.executeQuery(query);
        return rs;
    }

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

        Statement stmt = conn.createStatement();
        ResultSet rs = stmt.executeQuery(query);
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

    public DataMatrix getSpatialAvg() throws SQLException, IOException {

        DataMatrix result = null;

        if (!isTableExisting(TABLE_NAME_SPATAVG)) {
            return result;
        }

        int attribCount = getSelectedAttribCount();

        Statement stmt = conn.createStatement();
        String q = "SELECT * FROM " + TABLE_NAME_SPATAVG;
        ResultSet rs = stmt.executeQuery(q);

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

    public DataMatrix getYearlyAvg() throws SQLException, IOException {

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

    public DataMatrix getMonthlyAvg(int month) throws SQLException, IOException {

        DataMatrix result = null;

        if (!isTableExisting(TABLE_NAME_MONTHAVG)) {
            return result;
        }

        int attribCount = getSelectedAttribCount();

        Statement stmt = conn.createStatement();
        String q = "SELECT * FROM " + TABLE_NAME_MONTHAVG + " WHERE MONTH = " + month;
        ResultSet rs = stmt.executeQuery(q);

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

        double[][] dataArray = data.toArray(new double[attribCount][data.size()]);
        Long[] idArray = ids.toArray(new Long[ids.size()]);
        result = new DataMatrix(dataArray, idArray, this.getDataStoreProcessor());

        return result;
    }

    private boolean isTableExisting(String tableName) throws SQLException {
        Statement stmt = conn.createStatement();
        String q = "SELECT * FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_NAME='" + tableName + "'";
        ResultSet rs = stmt.executeQuery(q);
        if (rs.next()) {
            return true;
        } else {
            return false;
        }
    }

    public void calcMonthlyAvg() throws SQLException, IOException {

        // get number of selected attributes
        int numSelected = getSelectedAttribCount();

        // create the db tables to store the calculated monthly means
        Statement stmt = conn.createStatement();
        stmt.execute("DROP TABLE IF EXISTS " + TABLE_NAME_MONTHAVG);
        String q = "CREATE TABLE " + TABLE_NAME_MONTHAVG + " (";
        q += "MONTH " + DataStoreProcessor.TYPE_MAP.get("JAMSInteger") + ",";
        q += spaceID + " " + DataStoreProcessor.TYPE_MAP.get("JAMSLong") + ",";

        for (int i = 1; i <= numSelected; i++) {
            q += "a_" + i + " " + DataStoreProcessor.TYPE_MAP.get("JAMSDouble") + ",";
        }
        q = q.substring(0, q.length() - 1);
        q += ")";
        stmt.execute(q);

        // loop over months
        for (int i = 1; i <= 12; i++) {
            calcMonthlyAvg(i);
        }
    }

    public DataMatrix calcMonthlyAvg(int month) throws SQLException, IOException {

        Statement stmt = conn.createStatement();

        String monthString = String.format("%02d", month);
        setTimeFilter("%-" + monthString + "-%");

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
            String q = "INSERT INTO " + TABLE_NAME_MONTHAVG + " VALUES (" + month + ", " + ids[i];
            for (int j = 0; j < data[i].length; j++) {
                q += ", " + data[i][j];
            }
            q += ")";
            stmt.execute(q);
        }

        return aggregate;
    }

    public DataMatrix calcSpatialAvg() throws SQLException, IOException {

        int attribCount = getSelectedAttribCount();

        // create the db tables to store the calculated spatial mean
        Statement stmt = conn.createStatement();
        stmt.execute("DROP TABLE IF EXISTS " + TABLE_NAME_SPATAVG);
        String q = "CREATE TABLE " + TABLE_NAME_SPATAVG + " (";
        q += timeID + " " + DataStoreProcessor.TYPE_MAP.get("JAMSCalendar") + ",";

        for (int i = 1; i <= attribCount; i++) {
            q += "a_" + i + " " + DataStoreProcessor.TYPE_MAP.get("JAMSDouble") + ",";
        }
        q = q.substring(0, q.length() - 1);
        q += ")";
        stmt.execute(q);

        // reset filter and get the data
        resetTimeFilter();        
        ResultSet rs = getData();
        
        // we have a set of positions now, so get the matrixes and rock'n roll
        // get the first dataset
        long position;
        DataMatrix aggregate;
        
        ArrayList<double[]> data = new ArrayList<double[]>();
        ArrayList<String> timeStamps = new ArrayList<String>();

        // loop over datasets for current month
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
            stmt.execute(q);
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
                System.out.println(attrib.getName());
            } else {
                attrib.setSelected(false);
            }
        }

        //tsproc.calcMonthlyAvg();
        //DataMatrix spatAvg = tsproc.calcSpatialAvg();

        DataMatrix spatAvg = tsproc.getSpatialAvg();
        for (Object o : spatAvg.getIds()) {
            System.out.println(o);
        }
        spatAvg.print(5, 3);

        DataMatrix dm = tsproc.getMonthlyAvg(1);
        dm = tsproc.getYearlyAvg();
//        dm.print(5, 3);

//        for (Object o : dm.getIds()) {
//            System.out.print(o + " ");
//        }



        //output(tsproc.customQuery("SELECT count(*) from data "));

        tsproc.dsdb.close();
    }
}
