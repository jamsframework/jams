/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package jams.components.aggregate;

import jams.data.DataSupplier;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Collection;
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
                   
    Collection<Double> ids = null;
    
    public SpatialOutputDataStore(File file) throws IOException{
        this.file = file;        
        writer = new BufferedWriter(new FileWriter(file));
    }
                
    public void setHeader(Collection<Double> ids) throws IOException {
        writer.write("@context\n");
        writer.write("jams.components.core.SpatialContext	HRULoop	" + ids.size() + "\n") ;
        writer.write("@ancestors\n");
        writer.write("jams.components.core.TemporalContext	TimeLoop	9999\n");
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
            
    static boolean text = true;
    public void writeData(String entry, DataSupplier<Double> values) throws IOException{
        writer.write("TimeLoop	" + entry + "\n");
        writer.write("@start\n");

        Iterator<Double> iter = ids.iterator();
        
        for (double x : values){
            if (Double.isInfinite(x) || Double.isNaN(x)){
                x = -9999.;
            }
            //make sure that every entry has exactly the size of 13 bytes!!
            String result = df2EPos.format(x);
            if (result.contains("E-"))
                result = df2ENeg.format(x);
            if (iter.hasNext())
                writer.write(iter.next().longValue() + "\t" + result + "\n");
        }
        writer.write("@end\n");     
        writer.flush();
    }
            
    public void close() throws IOException{
        writer.close();
    }
}

