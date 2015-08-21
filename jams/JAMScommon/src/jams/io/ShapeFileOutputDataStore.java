/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package jams.io;

import jams.data.NamedDataSupplier;
import jams.io.dbf.DBFField;
import jams.io.dbf.DBFReader;
import jams.io.dbf.DBFWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;

/**
 *
 * @author christian
 */
public class ShapeFileOutputDataStore {
    DBFWriter writer  = null;
    File shapeFileTemplate, targetDirectory, targetFile;
    
    public ShapeFileOutputDataStore(File shapeFileTemplate, File targetDirectory) throws IOException{
        init(shapeFileTemplate, targetDirectory);
    }
    
    public File getTargetDirectory(){
        return targetDirectory;
    }
    
    private void init(File shapeFileTemplate, File targetDirectory) throws IOException{
        //test precondition
        if (shapeFileTemplate == null)
            throw new IllegalArgumentException("ShapeFileOutputDateStore cannot be initialized, because shapeFileTemplate must not be null.");
        if (!shapeFileTemplate.isFile() || !shapeFileTemplate.exists() || !shapeFileTemplate.getName().endsWith(".shp")){
            throw new IllegalArgumentException("ShapeFileOutputDateStore cannot be initialized, because shapeFileTemplate (%1) is not a shapefile.".replace("%1", shapeFileTemplate.getAbsolutePath()));
        }
        this.shapeFileTemplate = shapeFileTemplate;
        
        if (targetDirectory == null){
            throw new IllegalArgumentException("ShapeFileOutputDateStore cannot be initialized, because targetDirectory must not be null.");
        }
        if (!targetDirectory.isDirectory()){
            throw new IllegalArgumentException("ShapeFileOutputDateStore cannot be initialized, because targetDirectory (%1) is not a directory.".replace("%1", targetDirectory.getAbsolutePath()));
        }
        this.targetDirectory = targetDirectory;  
        if (!targetDirectory.exists()){
            targetDirectory.mkdirs();
        }
        this.targetFile = new File(targetDirectory, shapeFileTemplate.getName().replace(".shp", ".dbf"));
        

        //copy data .. 
        File directory = shapeFileTemplate.getParentFile();
        String name = shapeFileTemplate.getName().replace(".shp", "");
        for (File srcFile : directory.listFiles()) {
            if (srcFile.getName().startsWith(name)) {
                try {                    
                    Path dest = Paths.get(targetDirectory.getAbsolutePath()+"/"+srcFile.getName());
                    Path src  = Paths.get(srcFile.getAbsolutePath());
                    
                    if (Files.exists(dest)) {                        
                        if (Files.size(dest) != Files.size(src)) {
                            Files.copy(src, dest, StandardCopyOption.REPLACE_EXISTING);
                        }
                    }else{
                        Files.copy(src, dest, StandardCopyOption.REPLACE_EXISTING);
                    }
                } catch (IOException ioe) {
                    ioe.printStackTrace();
                    throw new IOException("The file " + srcFile.getAbsolutePath() + " could not be copied to the output directy!\n" + ioe.toString());
                }
            }
        }
    }
    
    public String[] getFieldNames() throws IOException{
        InputStream inputStream = null;
        DBFReader dbfReader = null;
        FileOutputStream outStream = null;
        String fieldNames[] = null;
        try {            
            inputStream = new FileInputStream(new File(shapeFileTemplate.getParentFile(), shapeFileTemplate.getName().replace(".shp", ".dbf")));
            dbfReader = new DBFReader(inputStream);
            fieldNames = new String[dbfReader.getFieldCount()];
            for (int i=0;i<dbfReader.getFieldCount();i++){
                fieldNames[i] = dbfReader.getField(i).getName();
            }
        } catch (IOException ioe) {
            //getModel().getRuntime().getLogger().log(Level.SEVERE, MessageFormat.format(JAMS.i18n("The following DBF File was not found: " + dbfFileOriginal), getInstanceName()));
            ioe.printStackTrace();
            throw new IOException("Could not write shape file, because of: " + ioe.toString());
        } finally {
            try {
                if (inputStream!=null)
                    inputStream.close();
                if (outStream!=null)
                    outStream.close();
                if (writer!=null)
                    writer.close();
            } catch (IOException ioe2) {
            }
        }
        return fieldNames;
    }
    
    public void addDataToShpFiles(NamedDataSupplier<Double> I[], String idFieldName) throws IOException{
        InputStream inputStream = null;
        DBFReader dbfReader = null;
        FileOutputStream outStream = null;
        try {            
            inputStream = new FileInputStream(new File(shapeFileTemplate.getParentFile(), shapeFileTemplate.getName().replace(".shp", ".dbf")));
            dbfReader = new DBFReader(inputStream);

            DBFWriter writer = new DBFWriter();
            
            DBFField dbfFields[] = new DBFField[dbfReader.getFieldCount()+I.length];
            int k = dbfReader.getFieldCount();
            int m = I.length;
            int n = k+m;  
            
            int idFieldIndex = -1;
            for (int i=0;i<k;i++){
                dbfFields[i] = dbfReader.getField(i);
                if (idFieldName!=null && dbfFields[i].getName().compareToIgnoreCase(idFieldName)==0){
                    idFieldIndex = i;
                }
            }
            if (idFieldIndex == -1){
                System.out.println("Error field with name " + idFieldName + " was not found in shapefile!");
                idFieldIndex = 0;
            }
            for (int i=0;i<I.length;i++){
                dbfFields[i+k] = new DBFField();
                String name = I[i].getName()
                        .replaceAll(" [0-9][0-9]:[0-9][0-9]", "");
                name = name.substring(0, Math.min(10, name.length()));
                
                dbfFields[i+k].setName(name);
                dbfFields[i+k].setDataType(DBFField.FIELD_TYPE_N);
                dbfFields[i+k].setFieldLength(12);
                dbfFields[i+k].setDecimalCount(5);                
            }
            writer.setFields(dbfFields);
            outStream = new FileOutputStream(targetFile);
            writer.write(outStream);
            outStream.close();
            outStream = null;
            
            writer = new DBFWriter(targetFile);
            
            for (int j=0;j<dbfReader.getRecordCount();j++){
                Object objIn[] = dbfReader.nextRecord();
                Object objOut[] = new Object[n];
                
                double id = Double.parseDouble(objIn[idFieldIndex].toString());
                                
                for (int i=0;i<k;i++){
                    objOut[i] = objIn[i];
                }
                
                for (int i=0;i<m;i++){
                    objOut[i+k] = I[i].get((int)id);
                }
                try{
                    writer.addRecord(objOut);
                }catch(jams.io.dbf.DBFException dbfe){
                    System.out.println("Error writing field: " + dbfe.toString());
                    System.out.println("Fields in question are: " + Arrays.toString(objOut));
                }
            }
            writer.write();
            writer = null;
            
        } catch (IOException ioe) {
            //getModel().getRuntime().getLogger().log(Level.SEVERE, MessageFormat.format(JAMS.i18n("The following DBF File was not found: " + dbfFileOriginal), getInstanceName()));
            ioe.printStackTrace();
            throw new IOException("Could not write shape file, because of: " + ioe.toString());
        } finally {
            try {
                if (inputStream!=null)
                    inputStream.close();
                if (outStream!=null)
                    outStream.close();
                if (writer!=null)
                    writer.close();
            } catch (IOException ioe2) {
            }
        }
    }
    
    //TODO: rewrite this method by using the other one .. 
    public void addDataToShpFiles(SimpleOutputDataStore store, String idFieldName) throws IOException{
        InputStream inputStream = null;
        DBFReader dbfReader = null;
        FileOutputStream outStream = null;
        try {
            String fields[] = store.getEntries();
            inputStream = new FileInputStream(new File(shapeFileTemplate.getParentFile(), shapeFileTemplate.getName().replace(".shp", ".dbf")));
            dbfReader = new DBFReader(inputStream);

            DBFWriter writer = new DBFWriter();
            
            DBFField dbfFields[] = new DBFField[fields.length+dbfReader.getFieldCount()];
            int k = dbfReader.getFieldCount();
            int m = fields.length;
            int n = k+m;  
            
            int idFieldIndex = -1;
            for (int i=0;i<k;i++){
                dbfFields[i] = dbfReader.getField(i);
                if (idFieldName!=null && dbfFields[i].getName().compareToIgnoreCase(idFieldName)==0){
                    idFieldIndex = i;
                }
            }
            if (idFieldIndex == -1){
                System.out.println("Error field with name " + idFieldName + " was not found in shapefile!");
                idFieldIndex = 0;
            }
            for (int i=0;i<fields.length;i++){
                dbfFields[i+k] = new DBFField();
                dbfFields[i+k].setName(fields[i].replaceAll(" [0-9][0-9]:[0-9][0-9]", ""));
                dbfFields[i+k].setDataType(DBFField.FIELD_TYPE_N);
                dbfFields[i+k].setFieldLength(12);
                dbfFields[i+k].setDecimalCount(5);                
            }
            writer.setFields(dbfFields);
            outStream = new FileOutputStream(targetFile);
            writer.write(outStream);
            outStream.close();
            outStream = null;
            
            writer = new DBFWriter(targetFile);
            
            for (int j=0;j<dbfReader.getRecordCount();j++){
                Object objIn[] = dbfReader.nextRecord();
                Object objOut[] = new Object[n];
                
                double id = Double.parseDouble(objIn[idFieldIndex].toString());
                int position = store.getPositionOfEntity(id);//this.aggregatedValues.headMap(id).size();
                
                for (int i=0;i<k;i++){
                    objOut[i] = objIn[i];
                }
                
                for (int i=0;i<m;i++){
                    if (position==-1){
                        objOut[i+k] = null;
                    }else{
                        objOut[i+k] = new Double(store.getData(fields[i], position));
                    }
                    
                }
                try{
                    writer.addRecord(objOut);
                }catch(jams.io.dbf.DBFException dbfe){
                    System.out.println("Error writing field: " + dbfe.toString());
                    System.out.println("Fields in question are: " + Arrays.toString(objOut));
                }
            }
            writer.write();
            writer = null;
            
        } catch (IOException ioe) {
            //getModel().getRuntime().getLogger().log(Level.SEVERE, MessageFormat.format(JAMS.i18n("The following DBF File was not found: " + dbfFileOriginal), getInstanceName()));
            ioe.printStackTrace();
            throw new IOException("Could not write shape file, because of: " + ioe.toString());
        } finally {
            try {
                if (inputStream!=null)
                    inputStream.close();
                if (outStream!=null)
                    outStream.close();
                if (writer!=null)
                    writer.close();
            } catch (IOException ioe2) {
            }
        }
    }
}
