/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package jams.components.aggregate;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Iterator;
import java.util.Locale;
import java.util.Set;

/**
 *
 * @author christian
 */
public class SpatialOutputDataStore {

    File file = null;
    BufferedWriter writer = null;
                   
    Set<Double> ids = null;
    
    public SpatialOutputDataStore(File file) throws IOException{
        this.file = file;        
        writer = new BufferedWriter(new FileWriter(file));
    }
    
    /*public void setHeader(double[] ids) throws IOException {
        writer.write("@context\n");
        writer.write("jams.model.JAMSSpatialContext	HRULoop	" + ids.length + "\n") ;
        writer.write("@ancestors\n");
        writer.write("jams.model.JAMSTemporalContext	TimeLoop	9999\n");
        writer.write("@filters\n");
        writer.write("@attributes\n");
        writer.write("ID	value\n");
        writer.write("@types\n");
        writer.write("JAMSLong	JAMSDouble\n");
        writer.write("@data\n");

        this.ids = ids;   
    }*/
    
    /*public void setHeader(int maxID) throws IOException {
        writer.write("@context\n");
        writer.write("jams.model.JAMSSpatialContext	HRULoop	" + ids.length + "\n") ;
        writer.write("@ancestors\n");
        writer.write("jams.model.JAMSTemporalContext	TimeLoop	9999\n");
        writer.write("@filters\n");
        writer.write("@attributes\n");
        writer.write("ID	value\n");
        writer.write("@types\n");
        writer.write("JAMSLong	JAMSDouble\n");
        writer.write("@data\n");

        this.ids = new double[maxID];
        for (int i=0;i<maxID;i++){
            ids[i] = i;
        }
    }*/
            
    public void setHeader(Set<Double> ids) throws IOException {
        writer.write("@context\n");
        writer.write("jams.model.JAMSSpatialContext	HRULoop	" + ids.size() + "\n") ;
        writer.write("@ancestors\n");
        writer.write("jams.model.JAMSTemporalContext	TimeLoop	9999\n");
        writer.write("@filters\n");
        writer.write("@attributes\n");
        writer.write("ID	value\n");
        writer.write("@types\n");
        writer.write("JAMSLong	JAMSDouble\n");
        writer.write("@data\n");

        this.ids = ids;   
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
    public void writeData(String entry, double values[]) throws IOException{
        writer.write("TimeLoop	" + entry + "\n");
        writer.write("@start\n");

        Iterator<Double> iter = ids.iterator();
        
        for (int i=0;i<values.length;i++){
            double x = values[i];//roundToSignificantFigures(,5);
            if (Double.isInfinite(x) || Double.isNaN(x)){
                x = -9999;
            }
            //make sure that every entry has exactly the size of 13 bytes!!
            String result = df2EPos.format(x);
            if (result.contains("E-"))
                result = df2ENeg.format(x);
            if (iter.hasNext())
                writer.write(iter.next().longValue() + "\t" + result + "\n");
        }
        writer.write("@end\n");        
    }
            
    public void close() throws IOException{
        writer.close();
    }
}

