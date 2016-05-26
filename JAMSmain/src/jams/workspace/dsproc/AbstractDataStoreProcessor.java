/*
 * DataStoreProcessor.java
 * Created on 29. Dezember 2008, 19:05
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

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Observable;
import java.util.Observer;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Sven Kralisch <sven.kralisch at uni-jena.de>
 */
public abstract class AbstractDataStoreProcessor {

    public enum DataStoreType{Unsupported, Timeserie, SpatioTemporal, DataSerie1D, TimeDataSerie};

    protected AbstractDataStoreProcessor(File dsFile){
    
    }

    abstract public boolean isEmpty();
    abstract public AbstractDataStoreProcessor[] getSubDataStores();
    
    static public AbstractDataStoreProcessor getProcessor(File dsFile){
        if (dsFile.getName().endsWith(".csv")){
            try{
                return new DataStoreProcessorOMS(dsFile);
            }catch(Exception ex){
                Logger.getLogger(AbstractDataStoreProcessor.class.getName()).log(Level.SEVERE, null, ex);
                return null;
            }
        }else{
            return new DataStoreProcessor(dsFile);
        }
    }
    
    static public DataStoreType getDataStoreType(File file){
        if (file.getName().endsWith(".csv")){
            return DataStoreProcessorOMS.getDataStoreType(file);
        }else{
            return DataStoreProcessor.getDataStoreType(file);
        }
    }

    public abstract void createDB() throws IOException, SQLException, ClassNotFoundException;

   
    public abstract void clearDB() throws SQLException;

    public abstract void removeDBFiles();

    public abstract void close() throws SQLException;
    
    public abstract boolean existsH2DB() throws SQLException;

    public abstract boolean isDBObsolete();

    public abstract boolean existsH2DBFiles();

    public abstract Connection getH2Connection(boolean checkForDB) throws SQLException;
    
    public abstract void cancelCreateIndex();
    
    public abstract boolean fillBlock(long position) throws IOException, SQLException;

    public abstract boolean isTimeSpaceDatastore();
    public abstract boolean isEnsembleTimeSeriesDatastore();
    //public abstract boolean isSimpleEnsembleDatastore();
    public abstract boolean isSimpleTimeSerieDatastore();
    public abstract boolean isSimpleDataSerieDatastore();
    public abstract ArrayList<ContextData> getContexts();

    public abstract ArrayList<FilterData> getFilters();
    /**
     * @return the attributes
     */
    public abstract ArrayList<AttributeData> getAttributes();

    /**
     * @return the fileName
     */
    public abstract File getFile();

    public abstract void addImportProgressObserver(Observer o);

    public abstract int getSize();

    public abstract long getStartPosition() throws IOException;

    public abstract DataMatrix getData(long position) throws IOException;

    public abstract String[] getSelectedDoubleAttribs();

    public class ContextData {

        private String type;
        private String name;
        private String idType;
        private int size;

        public ContextData(String type, String name, String size, String idType) {
            this.type = type;
            this.name = name;
            this.size = Integer.parseInt(size);

            if (idType == null) {
                if (type.equals("jams.components.core.TemporalContext") || type.equals("jams.model.JAMSTemporalContext")) {
                    this.idType = "JAMSCalendar";
                } else if (type.equals("jams.components.core.SpatialContext") || type.equals("jams.model.JAMSSpatialContext")) {
                    this.idType = "JAMSLong";
                } else if (type.contains("jams.components.optimizer") || type.contains("optas.optimizer") || type.contains("dump")) {
                    this.idType = "JAMSLong";
                } else {
                    this.idType = "JAMSLong";
                }
            }
        }

        /**
         * @return the type
         */
        public String getType() {
            return type;
        }

        /**
         * @return the name
         */
        public String getName() {
            return name;
        }

        /**
         * @return the idType
         */
        public String getIdType() {
            return idType;
        }

        /**
         * @return the size
         */
        public int getSize() {
            return size;
        }
    }

    public class FilterData {

        private String regex;
        private String contextName;

        public FilterData(String regex, String contextName) {
            this.regex = regex;
            this.contextName = contextName;
        }

        /**
         * @return the regex
         */
        public String getRegex() {
            return regex;
        }

        /**
         * @return the contextName
         */
        public String getContextName() {
            return contextName;
        }
    }

    public class AttributeData implements Comparable {

        public static final boolean SELECTION_DEFAULT = false;
        public static final int AGGREGATION_SUM = 1;
        public static final int AGGREGATION_MEAN = 2;
        public static final int AGGREGATION_WMEAN = 3;
        public static final int WEIGHTING_NONE = 1;
        public static final int WEIGHTING_TIMES_AREA = 2;
        public static final int WEIGHTING_DIV_AREA = 3;
        private String type;
        private String name;
        private boolean selected;
        private int aggregationType = AGGREGATION_MEAN;
        private int weightingType = WEIGHTING_NONE;

        public AttributeData(String type, String name) {
            this.type = type;
            this.name = name;
            this.selected = SELECTION_DEFAULT;
        }

        public String getType() {
            return type;
        }

        public String getName() {
            return name;
        }

        public boolean isSelected() {
            return selected;
        }

        public void setSelected(boolean selected) {
            synchronized (AbstractDataStoreProcessor.this) {
                this.selected = selected;
            }
        }

        public int getAggregationType() {
            return aggregationType;
        }

        public int getWeightingType() {
            return weightingType;
        }

        public void setAggregationType(int aggregationType) {
            synchronized (AbstractDataStoreProcessor.this) {
                this.aggregationType = aggregationType;
            }
        }

        public void setWeightingType(int weightingType) {
            synchronized (AbstractDataStoreProcessor.this) {
                this.weightingType = weightingType;
            }
        }

        @Override
        public int compareTo(Object obj) {
            return (this.getName().compareTo(((AttributeData) obj).getName()));
        }
    }    
    
    protected class ImportProgressObservable extends Observable {

        protected void setProgress(int progress) {
            this.setChanged();
            this.notifyObservers(progress);
        }
    }
}
