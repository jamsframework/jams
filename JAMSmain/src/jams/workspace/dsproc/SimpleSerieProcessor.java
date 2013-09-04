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
import jams.data.JAMSCalendar;
import jams.data.DefaultDataFactory;
import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Calendar;
import java.util.HashSet;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Sven Kralisch <sven.kralisch at uni-jena.de>
 */
public class SimpleSerieProcessor extends Processor {        
    private String contextID;
    private String ensembleFilter = null;
    private boolean timeSerie = false;
    
    public SimpleSerieProcessor(File file) {
        this(new DataStoreProcessor(file));
        
        timeSerie = this.dsdb.isSimpleTimeSerieDatastore();        
    }

    public boolean isEmpty(){        
        return (this.dsdb.getSize()<=1);
    }
    public boolean isTimeSerie(){
        return timeSerie;
    }
    
    /**
     * Get all available time steps
     * @return An array of calendar objects representing the time steps
     * @throws java.sql.SQLException
     */
    public synchronized Attribute.Calendar[] getTimeSteps() throws SQLException, IOException {
        if (!this.isTimeSerie())
            return null;
        Object[] ids = this.getIDs();
        Attribute.Calendar steps[] = new Attribute.Calendar[ids.length];

        for (int i = 0; i < ids.length; i++) {
            Attribute.Calendar calendar = DefaultDataFactory.getDataFactory().createCalendar();
            calendar.setValue((String) ids[i]);
            steps[i] = calendar;
        }
        return steps;
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
    
    public SimpleSerieProcessor(AbstractDataStoreProcessor dsdb) {
        this.dsdb = dsdb;
        this.contexts = dsdb.getContexts();
        if (dsdb.isSimpleDataSerieDatastore() || dsdb.isSimpleTimeSerieDatastore() ) {                        
            try {
                this.conn = dsdb.getH2Connection(true);
            } catch (SQLException ex) {
                Logger.getLogger(SimpleSerieProcessor.class.getName()).log(Level.INFO, "Error while creating connection to H2 database of {0}", dsdb.getFile());
                Logger.getLogger(SimpleSerieProcessor.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
            }
        }
        timeSerie = this.dsdb.isSimpleTimeSerieDatastore();        
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
    public synchronized DataMatrix getMean(String[] ids) throws SQLException, IOException {
        DataMatrix result = null;
        if (ids.length == 0)
            return null;
        DataMatrix rs = dsdb.getData(dsdb.getStartPosition());
        
        HashSet<String> set = new HashSet<String>();
        for (int i=0;i<ids.length;i++)
            set.add(ids[i]);
        Object obj[] = rs.getIds();
        int n = rs.getColumnDimension();
        double aggregate[][] = new double[1][n];
        for (int i=0;i<obj.length;i++){
            if (set.contains(obj[i].toString())){
                for (int j=0;j<n;j++)
                    aggregate[0][j] += rs.getRow(i)[j];
            }
        }
        Long id[] = new Long[1];
        id[0] = new Long(1);
        result = new DataMatrix(aggregate,id,rs.getAttributeIDs());
                
        return result.times(1.0 / (double)ids.length);
        
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
    public synchronized DataMatrix getData(String[] ids) throws SQLException, IOException {
        DataMatrix result = null;
        if (ids.length == 0)
            return null;
        DataMatrix rs = dsdb.getData(dsdb.getStartPosition());
        
        HashSet<String> set = new HashSet<String>();
        for (int i=0;i<ids.length;i++)
            set.add(ids[i]);
        Object obj[] = rs.getIds();
        int n = rs.getColumnDimension();
        double aggregate[][] = new double[ids.length][n];
        
        int count = 0;
        for (int i=0;i<obj.length;i++){
            if (set.contains(obj[i].toString())){
                for (int j=0;j<n;j++)
                    aggregate[count][j] += rs.getRow(i)[j];
                count++;
            }            
        }
        
        result = new DataMatrix(aggregate,ids,rs.getAttributeIDs());
                
        return result;
        
    }
    
    /**
     * Gets the overall spatial sum of the selected
     * attributes for all time steps
     * @return A DataMatrix object containing one row per timestep with the
     * spatial average values of selected attributes in columns
     * @throws java.sql.SQLException
     * @throws java.io.IOException
     */
    public synchronized DataMatrix getMean() throws SQLException, IOException {
        return getMean(getIDs());
    }                
                  
   
    
    /**
     * Get the model runs that data are available for
     * @return An int array containing the modelruns
     * @throws java.sql.SQLException
     */
    public synchronized String[] getIDs() throws SQLException, IOException {       
        DataMatrix rs = dsdb.getData(dsdb.getStartPosition());
        Object[] objIDs = rs.getIds();
        String[] ids = new String[objIDs.length];
        for (int i=0;i<objIDs.length;i++)
            ids[i] = objIDs[i].toString();
        
        return ids;
    }
    
    /**
     * @param ensembleFilter the ensembleIDFilter to set
     */
    public synchronized void setFilter(String ensembleFilter) {
        this.ensembleFilter = ensembleFilter;
    }

    public synchronized void resetEnsembleFilter() {
        ensembleFilter = null;
    }

    public synchronized DataMatrix calcTemporalMean(String filter) throws SQLException, IOException {       
        if (!isTimeSerie())
            return null;
        DataMatrix rs = dsdb.getData(dsdb.getStartPosition());

        // we have a set of positions now, so get the matrixes and rock'n roll
        // get the first dataset        
        int count = 0;
                
        Object obj[] = rs.getIds();
        int n = rs.getColumnDimension();
        double aggregate[][] = new double[1][n];
        for (int i=0;i<obj.length;i++){
            if (obj[i].toString().matches(filter)){ //TODO: wrong, because in the UI a regex is asked for!
                for (int j=0;j<n;j++){
                    aggregate[0][j] += rs.getRow(i)[j];
                }
                count++;
            }
        }
        
        Long id[] = new Long[1];
        id[0] = new Long(1);
        DataMatrix result = new DataMatrix(aggregate,id,rs.getAttributeIDs());
                
        return result.times(1.0 / (double)count);                
    }
    
    
    /**
     * Get the years that data are available for
     * @return An int array containing the years
     * @throws java.sql.SQLException
     */
    public synchronized int[] getYears() throws SQLException, IOException {
        if (!this.isTimeSerie())
            return new int[0];
        
        DataMatrix rs = dsdb.getData(dsdb.getStartPosition());
        
        Attribute.Calendar minDate = DefaultDataFactory.getDataFactory().createCalendar();
        Attribute.Calendar maxDate = DefaultDataFactory.getDataFactory().createCalendar();
        minDate.setValue(rs.getIds()[0].toString());
        maxDate.setValue(rs.getIds()[rs.getIds().length-1].toString());
                
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
     * Initialises the calculation of yearly average values of the selected
     * attributes for all entities
     * @throws java.sql.SQLException
     * @throws java.io.IOException
     */
    public synchronized DataMatrix getYearlyMean(int year) throws SQLException, IOException {             
        if (!this.isTimeSerie())
            return null;                                
        String filterString = String.format("%04d", year) + "-.*";
        DataMatrix yearMean = calcTemporalMean(filterString);
        return yearMean;
    }
    /**
     * Initialises the calculation of yearly average values of the selected
     * attributes for all entities
     * @throws java.sql.SQLException
     * @throws java.io.IOException
     */
    public synchronized DataMatrix getMonthlyMean(int month) throws SQLException, IOException {             
        if (!this.isTimeSerie())
            return null;                                
        String filterString = ".*-" + String.format("%02d", month) + "-.*";
        DataMatrix yearMean = calcTemporalMean(filterString);
        return yearMean;
    }
}
