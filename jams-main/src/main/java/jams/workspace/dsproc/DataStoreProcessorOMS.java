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

import jams.JAMS;
import jams.data.Attribute;
import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Observer;
import java.util.TimeZone;
import oms3.io.CSTable;
import oms3.io.DataIO;

/**
 *
 * @author Sven Kralisch <sven.kralisch at uni-jena.de>
 */
public class DataStoreProcessorOMS extends AbstractDataStoreProcessor{

    //assumptions so far:
    //*id is in the first column
    //*default dateformat is yyyy MM dd HH mm ss
    //*first table of file contains the data .. 
    //*type mapping available?! otherwise I assume a JAMSDouble
    
    ArrayList<AttributeData> attributes = new ArrayList<AttributeData>();
    private ImportProgressObservable importProgressObservable = new ImportProgressObservable();
        
    ArrayList<String[]> data = null;
    
    File f = null;
    CSTable table = null;
    
    private int tableID = 0;
    ArrayList<AbstractDataStoreProcessor> dsList = null;
    boolean hasSubDataStores = false;        
    
    
    public DataStoreProcessorOMS(File f){
        this(f,0);
        
        this.collectSubDataStores();
        if (this.dsList.isEmpty()){
            hasSubDataStores = false;
        }else{
            hasSubDataStores = true;
        }
    }
    private DataStoreProcessorOMS(File f, int tableID){
        super(f);
        
        this.f = f;
        try{
            List<String> tables = DataIO.tables(f);
            if (tables.isEmpty()){
                throw new IOException("File " + f.getAbsolutePath() + " contains no tables");
            }
            String tableName = tables.get(tableID);
            System.out.println("Loading table " + tableName);
            table = DataIO.table(f, tableName);
        }catch(IOException ioe){
            ioe.printStackTrace();
        }
                
        String dateFormat = table.getInfo().get("date_format");
        if (dateFormat == null){
            dateFormat = "yyyy MM dd HH mm ss";
        }
        
        
        for (int i=1;i<=table.getColumnCount();i++){
            String colName = table.getColumnName(i);
            String jamsType = "JAMSDouble";
            String omsType = table.getColumnInfo(i).get("Type");
            if (omsType != null){
                if (omsType.equals("Date")){
                    jamsType = "JAMSCalendar";
                }else{
                    jamsType = "JAMSDouble";
                }
            }
            
            AttributeData a = new AttributeData(jamsType, colName);
            attributes.add(a);
        }   
        
        data = new ArrayList<String[]>();
        SimpleDateFormat sdf_in = new SimpleDateFormat(dateFormat);
        SimpleDateFormat sdf_out = new SimpleDateFormat(Attribute.Calendar.DEFAULT_FORMAT_PATTERN);
        sdf_in.setTimeZone(TimeZone.getTimeZone("GMT"));
        sdf_out.setTimeZone(TimeZone.getTimeZone("GMT"));
        
        
        for (String[] value : table.rows()){ //skip first column, because thats an id only)
            String row[] = new String[value.length-1];
            for (int i=0;i<attributes.size();i++){
                if (attributes.get(i).getType().equals("JAMSCalendar")){
                    try{
                        row[i] = sdf_out.format(sdf_in.parse(value[i+1]));
                    }catch(ParseException pe){
                        row[i] = value[i+1];
                    }
                }else
                    row[i] = value[i+1];
            }
            data.add(row);
        }
    }
    
    @Override
    public boolean isEmpty() {
        try {
            List<String> tables = DataIO.tables(f);
            return tables.isEmpty();
        } catch (IOException ioe) {
            return true;
        }
    }
    
    static public DataStoreType getDataStoreType(File file) {
        try{
            List<String> tables = DataIO.tables(file);
            if (tables.isEmpty()){
                return DataStoreType.Unsupported;
            }
            CSTable table = DataIO.table(file, tables.get(0));
            if (table.getColumnCount()<=1) {
                return DataStoreType.Unsupported;
            }
            
            String type = table.getColumnInfo(1).get("Type");
            if (type == null){
                type = table.getColumnInfo(1).get("type");
            }
            if (type != null && type.equals("Date")){
                return DataStoreType.Timeserie;
            }else {
                return DataStoreType.DataSerie1D;
            }
        }catch(IOException ioe){
            ioe.printStackTrace();    
        }
        
        return DataStoreType.Unsupported;
    }

    @Override
    public void createDB() throws IOException, SQLException, ClassNotFoundException {
        
    }

    @Override
    public void clearDB() throws SQLException {
        
    }

    @Override
    public void removeDBFiles() {
        
    }

    @Override
    public void close() throws SQLException {
        
    }

    @Override
    public boolean existsH2DB() throws SQLException {
        return true;
    }

    @Override
    public boolean isDBObsolete() {
        return false;
    }

    @Override
    public boolean existsH2DBFiles() {
        return true;
    }

    @Override
    public Connection getH2Connection(boolean checkForDB) throws SQLException {
        return null;
    }

    @Override
    public void cancelCreateIndex() {
        
    }

    @Override
    public boolean fillBlock(long position) throws IOException, SQLException {
        return true;
    }

    @Override
    public boolean isTimeSpaceDatastore() {
        return false;
    }

    @Override
    public boolean isEnsembleTimeSeriesDatastore() {
        return false;
    }

    @Override
    public boolean isSimpleTimeSerieDatastore() {
        if (this.attributes.get(0).getType().equals("JAMSCalendar"))
            return true;
        return false;
    }

    @Override
    public boolean isSimpleDataSerieDatastore() {
        if (this.attributes.get(0).getType().equals("JAMSLong"))
            return true;
        return false;
    }

    @Override
    public ArrayList<ContextData> getContexts() {
        ArrayList<ContextData> context = new ArrayList<ContextData>();
        context.add(new ContextData("omsContext", "omsContext", Integer.toString(this.data.size()), this.attributes.get(0).getType()));
        return context;
    }

    @Override
    public ArrayList<FilterData> getFilters() {
        return new ArrayList<FilterData>();
    }

    @Override
    public ArrayList<AttributeData> getAttributes() {
        return attributes;
    }

    @Override
    public File getFile() {
        return f;
    }

    @Override
    public void addImportProgressObserver(Observer o) {
        importProgressObservable.addObserver(o);
    }

    @Override
    public int getSize() {
        return data.size();
    }

    @Override
    public long getStartPosition() throws IOException {
        return 0;
    }

    @Override
    public DataMatrix getData(long position) throws IOException {
        int i, j, numSelected = 0;

        boolean selected[] = new boolean[attributes.size()];
        ArrayList<String> attributeNames = new ArrayList<String>();
        i = 0;
        for (AttributeData a : attributes) {
            if (a.isSelected() && a.getType().equals("JAMSDouble")) {
                selected[i] = true;
                attributeNames.add(a.getName());
                numSelected++;
            } else {
                selected[i] = false;
            }
            i++;
        }

        ArrayList<double[]> rows = new ArrayList<double[]>();
        ArrayList<String> idList = new ArrayList<String>();

        for (int k=(int)position;k<data.size();k++){
            double[] cols = new double[numSelected];
                        
            idList.add(data.get(k)[0]);
            j=0;
            for (i = 0; i < attributes.size(); i++) {
                if (selected[i]) {
                    cols[j] = Double.parseDouble(data.get(k)[i]);
                    //TODO: review this decision .. 
                    if (cols[j] == JAMS.getMissingDataValue()) {
                        cols[j] = Double.NaN;
                    }
                    j++;
                }
            }
            rows.add(cols);
        }
        
        double[][] data = rows.toArray(new double[rows.size()][numSelected]);
        String ids[] = idList.toArray(new String[idList.size()]);

        return new DataMatrix(data, ids, attributeNames.toArray(new String[attributeNames.size()]));
    }

    @Override
    public String[] getSelectedDoubleAttribs() {
        // get number of selected attributes
        ArrayList<String> attribs = new ArrayList<String>();
        for (AttributeData a : getAttributes()) {
            if (a.isSelected() && a.getType().equals("JAMSDouble")) {
                attribs.add(a.getName());
            }
        }
        return attribs.toArray(new String[attribs.size()]);
    }

    private void collectSubDataStores(){
        dsList = new ArrayList<AbstractDataStoreProcessor>();
        try{
            List<String> tables = DataIO.tables(f);
            if (tables.isEmpty()){
                throw new IOException("File " + f.getAbsolutePath() + " contains no tables");
            }            
            for (String s : tables){
                CSTable table = DataIO.table(f, s);
                
                dsList.add(new DataStoreProcessorOMS(f, tableID++));
            }
        }catch(IOException ioe){
            ioe.printStackTrace();
        }   
    }
    
    @Override
    public AbstractDataStoreProcessor[] getSubDataStores() {
             
        return dsList.toArray(new AbstractDataStoreProcessor[0]);
    }
    
    @Override
    public String toString(){
        if (hasSubDataStores)
            return this.f.getName();
        else
            return this.f.getName() + ":" + this.table.getName();
    }

}
