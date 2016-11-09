/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package jams.io;

import jams.data.DataSupplier;
import gnu.trove.map.hash.THashMap;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Collection;
import java.util.Locale;
import java.util.Set;
import java.util.TreeMap;

/**
 *
 * @author christian
 */
public class SimpleOutputDataStore {

    File file = null;
    BufferedWriter writer = null;
    RandomAccessFile raf = null;
        
    TreeMap<String, Long> entryMap = new TreeMap<String, Long>();
    THashMap<Double, Integer> entityMap = new THashMap<Double, Integer>();
            
    static StringBuffer strGlobalBuffer;
    StringBuffer strBuffer = null;
    
    
    
    public SimpleOutputDataStore(File file) throws IOException{
        this(file, true);
    }
    
    public SimpleOutputDataStore(File file, boolean threadSafe) throws IOException{
        this.file = file;        
        raf = new RandomAccessFile(file, "rw");        
        raf.setLength(0);
        
        if (threadSafe){
            strBuffer = new StringBuffer();
        }else{
            if (strGlobalBuffer==null)
                strGlobalBuffer = new StringBuffer(5120000);
            strBuffer = strGlobalBuffer;
        }
    }
    
    public void setHeader(Collection<Double> ids) throws IOException {
        raf.writeBytes("date" + "\t");

        int position = 0;
        entityMap.clear();
        for (double id : ids) {
            raf.writeBytes((int) id + "\t");
            entityMap.put(id, position++);
        }
        raf.writeBytes("\n");
    }
    
    public void setHeader(int maxID) throws IOException {
        raf.writeBytes("date" + "\t");

        int position = 0;
        entityMap.clear();
        for (int id=0;id<maxID;id++){
            raf.writeBytes((int) id + "\t");
            entityMap.put((double)id, position++);
        }
        raf.writeBytes("\n");
    }
    
    public int getPositionOfEntity(double id){
        Integer i = entityMap.get(id);
        if (i==null)
            return -1;
        return i;
    }
    
    public String[] getEntries(){
        return entryMap.keySet().toArray(new String[0]);
    }
    
    public void setHeader(Set<Double> ids) throws IOException {
        raf.writeBytes("date" + "\t");

        int position = 0;
        entityMap.clear();
        for (double id : ids) {
            raf.writeBytes((int) id + "\t");
            entityMap.put(id, position++);
        }
        raf.writeBytes("\n");
    }
       
    public File getFile(){
        return file;
    }
    //This is a fixed size ascii number format
    //DO NOT Change this format! 
    DecimalFormat df2EPos = new DecimalFormat( "+0.00000E0000;-0.00000E0000", new DecimalFormatSymbols(Locale.ENGLISH) );
    DecimalFormat df2ENeg = new DecimalFormat( "+0.00000E000;-0.00000E000", new DecimalFormatSymbols(Locale.ENGLISH) );
                
    public void writeData(String entry, DataSupplier<Double> values) throws IOException{
        raf.seek(raf.length());
        //write data
        entryMap.put(entry, raf.getFilePointer());
        strBuffer.delete(0, strBuffer.length());
        strBuffer.append(entry);
        //raf.writeBytes(entry);
        for (Double x : values){
            if (Double.isInfinite(x) || Double.isNaN(x)){
                x = -9999.;
            }            
            //make sure that every entry has exactly the size of 14 bytes!!
            String result = df2EPos.format(x);
            if (result.contains("E-"))
                result = df2ENeg.format(x);
            //raf.writeBytes("\t" + result);            
            strBuffer.append("\t").append(result);
        }
        strBuffer.append("\n");
        raf.writeBytes(strBuffer.toString());          
    }
    
    byte buffer[] = new byte[13];
    
    public double getData(String entry, int position) throws IOException{
        if (position==-1)
            return jams.JAMS.getMissingDataValue();
        //18 size of date
        //14 size of each double plus tab

        raf.seek(entryMap.get(entry)+17+14*position);     
        
        raf.readFully(buffer);
        String s = new String(buffer);
        
        return Double.parseDouble(s);
    }
    
    public void close() throws IOException{
        raf.close();
    }
}

