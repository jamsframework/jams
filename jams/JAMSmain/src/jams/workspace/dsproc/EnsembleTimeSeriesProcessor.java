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
package jams.workspace.dsproc;

import jams.data.Attribute;
import jams.data.DefaultDataFactory;
import jams.data.JAMSCalendar;
import java.io.File;
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Observable;
import java.util.Observer;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Sven Kralisch <sven.kralisch at uni-jena.de>
 */
public class EnsembleTimeSeriesProcessor extends Processor {

    private static final String TABLE_NAME_MONTHAVG = "MONTHAVG", TABLE_NAME_YEARAVG = "YEARAVG", TABLE_NAME_SPATSUM = "SPATSUM";
    private String ensembleID;
    private String ensembleFilter = null;

    public EnsembleTimeSeriesProcessor(File file) {
        this(new DataStoreProcessor(file));
    }

    public EnsembleTimeSeriesProcessor(AbstractDataStoreProcessor dsdb) {
        this.dsdb = dsdb;
        this.contexts = dsdb.getContexts();
        if (dsdb.isEnsembleTimeSeriesDatastore()) {

            ensembleID = contexts.get(1).getName() + "ID";

            try {
                this.conn = dsdb.getH2Connection(true);
            } catch (SQLException ex) {
                Logger.getLogger(EnsembleTimeSeriesProcessor.class.getName()).log(Level.SEVERE, "Error while creating connection to H2 database of {0}", dsdb.getFile());
            }
        }
    }

    /**
     * Get data from the database based on defined filters on time and space
     * @return The data as JDBC result set
     * @throws java.sql.SQLException
     */
    public synchronized ResultSet getData() throws SQLException {

        String query = "SELECT " + ensembleID + ", position FROM index";

        if (ensembleFilter != null) {

            query += " WHERE ";
            String s = null;
            if (ensembleFilter != null) {
                if (ensembleFilter.contains("%")) {
                    s = " LIKE '" + ensembleFilter + "'";
                } else {
                    s = " = '" + ensembleFilter + "'";
                }
                query += ensembleID + s;
            }
        }
        query += " ORDER BY position";

//        System.out.println(query);

        ResultSet rs = customSelectQuery(query);
        return rs;
    }

    public int getTimeUnit() throws SQLException, IOException{
        Attribute.Calendar timeSteps[] = this.getTimeSteps();
        if (timeSteps == null)
            return -1;
        Attribute.Calendar t0 = timeSteps[0];
        Attribute.Calendar tn = timeSteps[timeSteps.length-1];

        int n = timeSteps.length;

        long diff = ((tn.getTimeInMillis() - t0.getTimeInMillis())/n)/1000;

        //todo: add time unit count
        if (0<=diff && diff<30)
            return Calendar.SECOND;
        if (30<=diff && diff<1800)
            return Calendar.MINUTE;
        if (1800<=diff && diff<43200)
            return Calendar.HOUR;
        if (43200<=diff && diff<1296000)
            return Calendar.DAY_OF_YEAR;
        if (1296000<=diff && diff<3456000)
            return Calendar.MONTH;
        else
            return Calendar.YEAR;
    }
    
    public synchronized DataMatrix getCrossProduct(long[] modelRunIds, String[] dateIds) throws SQLException, IOException {
        double[][] matrix = new double[dateIds.length][modelRunIds.length];
        int idMap[] = null;

        for (int i = 0; i < modelRunIds.length; i++) {
            DataMatrix col = getTimeSeriesData(this.getModelRuns()[i]);
            if (idMap == null) {
                idMap = new int[dateIds.length];
                //bad quadratic time!!
                for (int j = 0; j < dateIds.length; j++) {
                    idMap[j] = col.getIDPosition(dateIds[j]);
                }
            }
            double timeSerie[] = col.getCol(0);
            for (int j = 0; j < dateIds.length; j++) {
                matrix[j][i] = timeSerie[idMap[j]];
            }
        }
        String modelRunIdStrings[] = new String[modelRunIds.length];
        for (int i = 0; i < modelRunIds.length; i++) {
            modelRunIdStrings[i] = Long.toString(modelRunIds[i]);
        }
        return new DataMatrix(matrix, dateIds, modelRunIdStrings);
    }

    /**
     * Gets the values of the selected attributes for all timesteps for a
     * specific modelrun
     * @param date The date for which the data shall be returned
     * @return A DataMatrix object containing one row per entity with the
     * attribute values in columns
     * @throws java.sql.SQLException
     * @throws java.io.IOException
     */
    public synchronized DataMatrix getTimeSeriesData(long modelRun) throws SQLException, IOException {

        setEnsembleFilter(new Long(modelRun).toString());
        ResultSet rs = getData();
        DataMatrix result = null;
        if (rs.next()) {
            long position = rs.getLong("POSITION");
            result = dsdb.getData(position);
        }
        return result;
    }

    /**
     * Initialises the calculation of longterm monthly average values of the
     * selected attributes for all entities
     * @throws java.sql.SQLException
     * @throws java.io.IOException
     */
    public synchronized void calcMonthlyMean() throws SQLException, IOException {

        // get number of selected attributes
        String[] attributeIDs = getDataStoreProcessor().getSelectedDoubleAttribs();
        int numSelected = attributeIDs.length;

        // create the db tables to store the calculated monthly means
        customQuery("DROP TABLE IF EXISTS " + TABLE_NAME_MONTHAVG);
        String q = "CREATE TABLE " + TABLE_NAME_MONTHAVG + " (";
        q += "MONTH " + DataStoreProcessor.TYPE_MAP.get("JAMSInteger") + ",";
        q += ensembleID + " " + DataStoreProcessor.TYPE_MAP.get("JAMSLong") + ",";

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
            String filterString = ".*-" + String.format("%02d", i) + "-.*";
            calcTemporalMean(filterString, TABLE_NAME_MONTHAVG, String.valueOf(i), true);

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

    /**
     * Initialises the calculation of longterm monthly average values of the
     * selected attributes for all entities
     * @throws java.sql.SQLException
     * @throws java.io.IOException
     */
    public synchronized void calcYearlyMean() throws SQLException, IOException {

        // get number of selected attributes
        String[] attributeIDs = getDataStoreProcessor().getSelectedDoubleAttribs();
        int numSelected = attributeIDs.length;

        // create the db tables to store the calculated monthly means
        customQuery("DROP TABLE IF EXISTS " + TABLE_NAME_YEARAVG);
        String q = "CREATE TABLE " + TABLE_NAME_YEARAVG + " (";
        q += "YEAR " + DataStoreProcessor.TYPE_MAP.get("JAMSInteger") + ",";
        q += ensembleID + " " + DataStoreProcessor.TYPE_MAP.get("JAMSLong") + ",";

        for (int i = 1; i <= numSelected; i++) {
            q += "a_" + i + " " + DataStoreProcessor.TYPE_MAP.get("JAMSDouble") + ",";
        }
        q = q.substring(0, q.length() - 1);
        q += ")";
        customQuery(q);

        int percent = 0;

        int[] years = this.getYears();

        // loop over months
        for (int i = 0; i < years.length; i++) {
            int y = years[i];
            // calc the monthly average values
            String filterString = String.format("%04d", y) + "-.*";
            calcTemporalMean(filterString, TABLE_NAME_YEARAVG, Integer.toString(y), true);

            if (abortOperation) {
                customQuery("DROP TABLE IF EXISTS " + TABLE_NAME_MONTHAVG);
                return;
            }

            // update the observer
            int current = Math.round(((i + 1) / (float) years.length) * (float) 100);
            if (current > percent) {
                percent = current;
                processingProgressObservable.setProgress(percent);
            }
        }
    }

    /**
     * Gets the sum of the selected attributes for an array of
     * model runs at all time steps
     * @param ids The id array of the spatial enties
     * @return A DataMatrix object containing one row per timestep with the
     * mean values of selected attributes in columns
     * @throws java.sql.SQLException
     * @throws java.io.IOException
     */
    public synchronized DataMatrix getEnsembleMean(long[] ids) throws SQLException, IOException {
        DataMatrix aggregate = null;

        if (ids.length == 0) {
            return null;
        }

        aggregate = this.getTimeSeriesData(ids[0]);
        for (int i = 1; i < ids.length; i++) {
            aggregate.plus(this.getTimeSeriesData(ids[i]));

            processingProgressObservable.setProgress((int) ((double) (i + 1) * 100.0 / (double) ids.length));
        }

        aggregate.times(1.0 / (double) ids.length);

        return aggregate;
    }

    /**
     * Gets the overall spatial sum of the selected
     * attributes for all time steps
     * @return A DataMatrix object containing one row per timestep with the
     * spatial average values of selected attributes in columns
     * @throws java.sql.SQLException
     * @throws java.io.IOException
     */
    public synchronized DataMatrix getEnsembleMean() throws SQLException, IOException {
        return getEnsembleMean(getModelRuns());
    }

    /**
     * Gets the overall temporal average values of the selected
     * attributes for all entities
     * @return A DataMatrix object containing one row per entity with the
     * temporal average values of selected attributes in columns
     * @throws java.sql.SQLException
     * @throws java.io.IOException
     */
    public synchronized DataMatrix getTemporalMean() throws SQLException, IOException {

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

        this.setEnsembleFilter(null);
        ResultSet rs = getData();
        String attrSet[] = null;
        // we have a set of positions now, so get the matrixes and rock'n roll
        // get the first dataset
        long position;
        HashMap<Long, double[]> aggregate = new HashMap<Long, double[]>();
        HashMap<Long, int[]> counter = new HashMap<Long, int[]>();

        // loop over datasets for current month
        while (rs.next()) {

            if (abortOperation) {
                return null;
            }

            position = rs.getLong("POSITION");
            DataMatrix m = dsdb.getData(position);
            Object[] ids = m.getIds();
            attrSet = m.getAttributeIDs();
            for (int i = 0; i < ids.length; i++) {
                String current_id = (String) ids[i];
                boolean match = false;
                for (int j = 0; j < dates.length; j++) {
                    if (dates[j].toString().equals(current_id)) {
                        match = true;
                        break;
                    }
                }
                if (match) {
                    Long modelrun = rs.getLong(ensembleID);
                    double entry[] = aggregate.get(modelrun);
                    if (entry == null) {
                        aggregate.put(modelrun, m.getRow(i).clone());
                        counter.put(modelrun, new int[]{1});
                    } else {
                        for (int j = 0; j < entry.length; j++) {
                            entry[j] += m.getRow(i)[j];
                        }
                        counter.get(modelrun)[0]++;
                    }
                }
            }
        }
        double table[][] = new double[aggregate.size()][];
        String id_table[] = new String[aggregate.size()];

        Iterator<Long> iter = aggregate.keySet().iterator();
        int tableCounter = 0;
        while (iter.hasNext()) {
            Long modelrun = iter.next();
            double data[] = aggregate.get(modelrun);
            int count = counter.get(modelrun)[0];
            for (int i = 0; i < data.length; i++) {
                data[i] /= (double) count;
            }
            table[tableCounter] = data;
            id_table[tableCounter] = modelrun.toString();
            tableCounter++;
        }

        DataMatrix result = new DataMatrix(table, id_table, attrSet);

        return result;
    }

    /**
     * Gets the yearly average values of the selected
     * attributes for the ensemble
     * @param year The year for which the average values shall be returned
     * @return A DataMatrix object containing one row per entity with the
     * longtime monthly average values of selected attributes in columns
     * @throws java.sql.SQLException
     * @throws java.io.IOException
     */
    public synchronized DataMatrix getYearlyMean(int year) throws SQLException, IOException {

        // check if the values have already been calculated, otherwise calculate them
        if (!isYearlyMeanExisiting()) {
            calcYearlyMean();
        }

        String[] attributeIDs = getDataStoreProcessor().getSelectedDoubleAttribs();
        int attribCount = attributeIDs.length;

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

        return new DataMatrix(dataArray, idArray, attributeIDs);
    }

    /**
     * Gets the longtime monthly average values of the selected
     * attributes for the ensemble
     * @param month The month for which the average values shall be returned
     * as int value between 1 and 12
     * @return A DataMatrix object containing one row per entity with the
     * longtime monthly average values of selected attributes in columns
     * @throws java.sql.SQLException
     * @throws java.io.IOException
     */
    public synchronized DataMatrix getMonthlyMean(int month) throws SQLException, IOException {
        // check if the values have already been calculated, otherwise calculate them
        if (!isMonthlyMeanExisiting()) {
            calcMonthlyMean();
        }

        String[] attributeIDs = getDataStoreProcessor().getSelectedDoubleAttribs();
        int attribCount = attributeIDs.length;

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

        return new DataMatrix(dataArray, idArray, attributeIDs);
    }

    /*calculates temporal mean according to the java regular expression given and stores the result in a new table*/
    private synchronized DataMatrix calcTemporalMean(String regex, String tableName, String id, boolean putInTable) throws SQLException, IOException {
        this.resetEnsembleFilter();
        ResultSet rs = getData();
        String attrSet[] = null;
        HashMap<Long, double[]> aggregate = new HashMap<Long, double[]>();
        HashMap<Long, int[]> counter = new HashMap<Long, int[]>();

        // we have a set of positions now, so get the matrixes and rock'n roll                                     
        // loop over datasets for current month
        while (rs.next()) {
            if (abortOperation) {
                return null;
            }
            long position = rs.getLong("POSITION");
            DataMatrix m = dsdb.getData(position);

            attrSet = m.getAttributeIDs();
            Object[] ids = m.getIds();

            for (int i = 0; i < ids.length; i++) {
                String current_id = (String) ids[i];
                if (!current_id.matches(regex)) {
                    continue;
                }
                Long modelrun = rs.getLong(ensembleID);
                double entry[] = aggregate.get(modelrun);
                if (entry == null) {
                    aggregate.put(modelrun, m.getRow(i).clone());
                    counter.put(modelrun, new int[]{1});
                } else {
                    for (int j = 0; j < entry.length; j++) {
                        entry[j] += m.getRow(i)[j];
                    }
                    counter.get(modelrun)[0]++;
                }
            }
        }
        double table[][] = new double[aggregate.size()][];
        String id_table[] = new String[aggregate.size()];

        Iterator<Long> iter = aggregate.keySet().iterator();
        int tableCounter = 0;
        while (iter.hasNext()) {
            Long modelrun = iter.next();
            double data[] = aggregate.get(modelrun);
            int count = counter.get(modelrun)[0];
            for (int i = 0; i < data.length; i++) {
                data[i] /= (double) count;
            }
            table[tableCounter] = data;
            id_table[tableCounter] = modelrun.toString();
            String q = "INSERT INTO " + tableName + " VALUES (" + id + ", " + modelrun;
            for (int j = 0; j < data.length; j++) {
                q += ", " + data[j];
            }
            q += ")";
            if (putInTable) {
                customQuery(q);
            }
            tableCounter++;
        }

        return new DataMatrix(table, id_table, attrSet);
    }

    /**
     * Gets the ensemble data for one specific date
     * @return DataMatrix with ensemble
     * @throws java.sql.SQLException
     */
    public synchronized DataMatrix getTemporalData(JAMSCalendar date) throws SQLException, IOException {
        return calcTemporalMean(date.toString(), "dummy", "dummy", false);
    }

    /**
     * Get the model runs that data are available for
     * @return An int array containing the modelruns
     * @throws java.sql.SQLException
     */
    public synchronized long[] getModelRuns() throws SQLException {

        // get min and max dates
        String q = "SELECT min(" + ensembleID + ") AS MINDATE, max(" + ensembleID + ") AS MAXDATE FROM index";
        ResultSet rs = customSelectQuery(q);
        rs.next();
        int minID = (int) rs.getLong("MINDATE");
        int maxID = (int) rs.getLong("MAXDATE");

        long[] ids = new long[maxID - minID + 1];

        for (int i = 0; i < ids.length; i++) {
            ids[i] = i + minID;
        }

        return ids;
    }

    /**
     * Get the years that data are available for
     * @return An int array containing the years
     * @throws java.sql.SQLException
     */
    public synchronized int[] getYears() throws SQLException, IOException {
        DataMatrix block0 = getTimeSeriesData(this.getModelRuns()[0]);

        Attribute.Calendar minDate = DefaultDataFactory.getDataFactory().createCalendar();
        minDate.setValue((String) block0.getIds()[0]);
        Attribute.Calendar maxDate = DefaultDataFactory.getDataFactory().createCalendar();
        maxDate.setValue((String) block0.getIds()[block0.getIds().length - 1]);

        int startYear = minDate.get(Attribute.Calendar.YEAR);
        int endYear = maxDate.get(Attribute.Calendar.YEAR);
        int[] years = new int[endYear - startYear + 1];
        int c = 0;

        for (int i = startYear; i <= endYear; i++) {
            years[c++] = i;
        }

        return years;
    }

    /**
     * Get all available time steps
     * @return An array of calendar objects representing the time steps
     * @throws java.sql.SQLException
     */
    public synchronized Attribute.Calendar[] getTimeSteps() throws SQLException, IOException {

        DataMatrix block0 = getTimeSeriesData(this.getModelRuns()[0]);
        if (block0 == null) //could happen if datastore is empty
            return null;
        Object[] ids = block0.getIds();
        Attribute.Calendar steps[] = new Attribute.Calendar[ids.length];

        for (int i = 0; i < ids.length; i++) {
            Attribute.Calendar calendar = DefaultDataFactory.getDataFactory().createCalendar();
            calendar.setValue((String) ids[i]);
            steps[i] = calendar;
        }
        return steps;
    }

    public boolean isMonthlyMeanExisiting() throws SQLException {
        return isTableExisting(TABLE_NAME_MONTHAVG);
    }

    public boolean isYearlyMeanExisiting() throws SQLException {
        return isTableExisting(TABLE_NAME_YEARAVG);
    }

    public boolean isSpatSumExisiting() throws SQLException {
        return isTableExisting(TABLE_NAME_SPATSUM);
    }

    /**
     * @param ensembleFilter the ensembleIDFilter to set
     */
    public synchronized void setEnsembleFilter(String ensembleFilter) {
        this.ensembleFilter = ensembleFilter;
    }

    public synchronized void resetEnsembleFilter() {
        ensembleFilter = null;
    }

    public synchronized void deleteCache() throws SQLException {
        String[] tables = {TABLE_NAME_YEARAVG, TABLE_NAME_SPATSUM, TABLE_NAME_MONTHAVG};
        for (String table : tables) {
            customQuery("DROP TABLE IF EXISTS " + table);
        }
    }

    public static void main(String[] args) throws Exception {
        EnsembleTimeSeriesProcessor tsproc = new EnsembleTimeSeriesProcessor(new File("C:/Arbeit/ModelData/JAMS-Gehlberg/output/output_gehlberg_e2_gutmann/TimeLoop.dat"));
        tsproc.dsdb.isEnsembleTimeSeriesDatastore();

        ArrayList<DataStoreProcessor.AttributeData> attribs = tsproc.getDataStoreProcessor().getAttributes();
        for (DataStoreProcessor.AttributeData attrib : attribs) {
            attrib.setSelected(true);
        }
        System.out.println();

        tsproc.addProcessingProgressObserver(new Observer() {

            public void update(Observable o, Object arg) {
                System.out.println("Progress: " + arg);
            }
        });


        tsproc.deleteCache();

        int c = 0;

        DataMatrix m = null;
        switch (c) {
            case 0:
                // calc/get longterm monthly mean values            
                //m = tsproc.getMonthlyMean(12);
                break;
            case 1:
                // calc/get yearly mean values
                //tsproc.calcYearlyMean();
                //m = tsproc.getYearlyMean(1997);
                break;
            case 2:
                // get overall temporal mean values
                // (based on longterm monthly mean values)
                //m = tsproc.getTemporalAvg();
                break;
            case 3:
                // calc/get overall spatial mean values
                //tsproc.calcSpatialSum();
                //m = tsproc.getSpatialSum();
                break;
            case 4:
                // get spatial mean values for selected entities
                //long[] ids = {1, 3, 5, 7, 9};
                long[] ids = {1};
                //m = tsproc.getSpatialSum(ids);
                break;
            case 5:
                // get values for a specific date
                Attribute.Calendar cal = DefaultDataFactory.getDataFactory().createCalendar();
                cal.setValue("2000-10-31 07:30");
                //m = tsproc.getTemporalData(cal);
                break;
            case 6:
                // get temporal mean values matching a date pattern
                //m = tsproc.getTemporalMean("2%-10-30 07:30%");
                break;
            case 7:
                // get temporal mean values for an array of specific dates
                Attribute.Calendar[] dates = new Attribute.Calendar[2];
                dates[0] = DefaultDataFactory.getDataFactory().createCalendar();
                dates[0].setValue("2000-10-31 07:30");
                dates[1] = DefaultDataFactory.getDataFactory().createCalendar();
                dates[1].setValue("2000-10-30 07:30");
                //m = tsproc.getTemporalMean(dates);
                break;
            case 8:
                dates = tsproc.getTimeSteps();
                for (Attribute.Calendar date : dates) {
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
