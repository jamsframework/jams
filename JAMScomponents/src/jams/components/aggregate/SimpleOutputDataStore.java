/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package jams.components.aggregate;

import gnu.trove.map.hash.THashMap;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
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
            
    public SimpleOutputDataStore(File file) throws IOException{
        this.file = file;        
        raf = new RandomAccessFile(file, "rw");        
        raf.setLength(0);
    }
    
    public void setHeader(double[] ids) throws IOException {
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
    DecimalFormat df2EPos = new DecimalFormat( "+0.00000E000;-0.00000E000", new DecimalFormatSymbols(Locale.ENGLISH) );
    DecimalFormat df2ENeg = new DecimalFormat( "+0.00000E00;-0.00000E00", new DecimalFormatSymbols(Locale.ENGLISH) );
    
    private double roundToSignificantFigures(double num, int n) {
        if (num == 0) {
            return 0;
        }

        final double d = Math.ceil(Math.log10(num < 0 ? -num : num));
        final int power = n - (int) d;

        final double magnitude = Math.pow(10, power);
        final long shifted = Math.round(num * magnitude);
        return shifted / magnitude;
    }
        
    static boolean text = true;
    StringBuffer strBuffer = new StringBuffer(5120000);
    public void writeData(String entry, double values[]) throws IOException{
        raf.seek(raf.length());
        //write data
        entryMap.put(entry, raf.getFilePointer());
        strBuffer.delete(0, strBuffer.length());
        strBuffer.append(entry);
        //raf.writeBytes(entry);
        for (int i=0;i<values.length;i++){
            double x = values[i];//roundToSignificantFigures(,5);
            if (Double.isInfinite(x) || Double.isNaN(x)){
                x = -9999;
            }            
            //make sure that every entry has exactly the size of 13 bytes!!
            String result = df2EPos.format(x);
            if (result.contains("E-"))
                result = df2ENeg.format(x);
            //raf.writeBytes("\t" + result);            
            strBuffer.append("\t" + result);
        }
        strBuffer.append("\n");
        raf.writeBytes(strBuffer.toString());          
    }
    
    byte buffer[] = new byte[12];
    
    public double getData(String entry, int position) throws IOException{
        if (position==-1)
            return jams.JAMS.getMissingDataValue();
        //18 size of date
        //13 size of each double plus tab

        raf.seek(entryMap.get(entry)+17+13*position);     
        
        raf.readFully(buffer);
        String s = new String(buffer);
        
        return Double.parseDouble(s);
    }
    
    public void close() throws IOException{
        raf.close();
    }
}

