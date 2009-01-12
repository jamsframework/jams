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
package reg.dsdb;

import jams.data.JAMSCalendar;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

/**
 *
 * @author Sven Kralisch <sven.kralisch at uni-jena.de>
 */
public class TimeSpaceProcessor {

    private H2DataStore h2ds;

    private Connection conn;

    private ArrayList<H2DataStore.ContextData> contexts;

    private String spaceID,  timeID;

    private String spaceFilter = null,  timeFilter = null,  aggregator = null;

    public TimeSpaceProcessor(String fileName) {
        this(new H2DataStore(fileName));
    }

    public TimeSpaceProcessor(H2DataStore h2ds) {
        this.h2ds = h2ds;

        if (isTimeSpaceDatastore()) {

            spaceID = contexts.get(0).getName() + "ID";
            timeID = contexts.get(1).getName() + "ID";

            try {
                this.conn = h2ds.getH2Connection(true);
            } catch (SQLException ex) {
                System.out.println("Error while creating connection to H2 database of " + h2ds.getFileName());
            }
        }
    }

    public boolean isTimeSpaceDatastore() {
        ArrayList<H2DataStore.ContextData> contexts = h2ds.getContexts();
        if (contexts.size() != 2) {
            return false;
        }
        if (!contexts.get(0).getType().equals("jams.model.JAMSSpatialContext")) {
            return false;
        }
        if (!contexts.get(1).getType().equals("jams.model.JAMSTemporalContext")) {
            return false;
        }

        this.contexts = contexts;
        return true;
    }

    public ResultSet getData(JAMSCalendar date) throws SQLException {

        date.setDateFormat("yyyy-MM-dd HH:mm:ss");
        String query = "SELECT " + spaceID;

        for (H2DataStore.AttributeData attribute : h2ds.getAttributes()) {
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

//        query = "SELECT count(*) FROM data";
//        query = "SELECT TIMELOOPID, AVG(ELEVATION), AVG(X) FROM data GROUP BY TIMELOOPID";
//        query = "SELECT DISTINCT TIMELOOPID, AVG(ELEVATION), AVG(X), AVG(Y) FROM data WHERE TIMELOOPID LIKE '%-11-% %:%:%' GROUP BY TIMELOOPID";
    }

    public ResultSet customQuery(String query) throws SQLException {
        Statement stmt = conn.createStatement();
        ResultSet rs = stmt.executeQuery(query);
        return rs;
    }

    public ResultSet getData() throws SQLException {

        String query;
        if (aggregator == null) {
            query = "SELECT " + timeID + ", " + spaceID;
        } else {
            query = "SELECT 0 ";
        }

        for (H2DataStore.AttributeData attribute : h2ds.getAttributes()) {
            if (attribute.isSelected()) {
                if (aggregator == null) {
                    query += ", " + attribute.getName();
                } else {
                    query += ", " + aggregator + "(" + attribute.getName() + ")";
                }
            }
        }

        query += " FROM data";

        if ((spaceFilter != null) || (timeFilter != null)) {

            query += " WHERE ";
            String s = null;
            if (spaceFilter != null) {
                if (spaceFilter.contains("%")) {
                    s = " LIKE '" + spaceFilter + "'";
                } else {
                    s = "=" + spaceFilter;
                }
                query += spaceID + s;
            }
            if ((spaceFilter != null) && (timeFilter != null)) {
                query += " AND ";
            }
            if (timeFilter != null) {
                if (timeFilter.contains("%")) {
                    s = " LIKE '" + timeFilter + "'";
                } else {
                    s = " = '" + timeFilter + "'";
                }
                query += timeID + s;
            }
        }

        System.out.println(query);

        Statement stmt = conn.createStatement();
        ResultSet rs = stmt.executeQuery(query);
        return rs;
    }

    public void getMonthlyAvg() throws SQLException {
        Statement stmt = conn.createStatement();
        for (int i = 1; i <= 12; i++) {

            resetSpaceFilter();
            setTimeFilter("%-" + String.format("%02d", i) + "-%");
            setAggregator("AVG");
            output(getData());

        }

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
     * @param spaceFilter the spaceIDFilter to set
     */
    public void setSpaceFilter(String spaceFilter) {
        this.spaceFilter = spaceFilter;
    }

    /**
     * @param timeFilter the timeIDFilter to set
     */
    public void setTimeFilter(String timeFilter) {
        this.timeFilter = timeFilter;
    }

    /**
     * @param aggregator the aggregator to set
     */
    public void setAggregator(String aggregator) {
        this.aggregator = aggregator;
    }

    public void resetSpaceFilter() {
        spaceFilter = null;
    }

    public void resetTimeFilter() {
        timeFilter = null;
    }

    public void resetAggregator() {
        aggregator = null;
    }

    /**
     * @return the h2ds
     */
    public H2DataStore getH2ds() {
        return h2ds;
    }

    public static void main(String[] args) throws Exception {
        TimeSpaceProcessor tsproc = new TimeSpaceProcessor("D:/jamsapplication/JAMS-Gehlberg/output/current/HRULoop_2.dat");
        tsproc.isTimeSpaceDatastore();

        JAMSCalendar date = new JAMSCalendar();
        date.setValue("1995-11-01 07:30");

        //output(tsproc.getData(date));
        tsproc.setSpaceFilter("42");
        tsproc.setTimeFilter("%-02-%");
        tsproc.setAggregator("sum");

        output(tsproc.getData());

        tsproc.getMonthlyAvg();

        //output(tsproc.customQuery("SELECT count(*) from data "));

        tsproc.h2ds.close();
    }
}
