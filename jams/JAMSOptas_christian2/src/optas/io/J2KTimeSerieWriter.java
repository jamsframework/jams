/*
 * TSDataReader.java
 * Created on 11. November 2005, 10:10
 *
 * This file is part of JAMS
 * Copyright (C) 2005 S. Kralisch and P. Krause
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public License
 * as published by the Free Software Foundation; either version 3
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, see <http://www.gnu.org/licenses/>.
 *
 */

package optas.io;

import java.util.*;
import java.io.*;
import java.text.SimpleDateFormat;
import optas.core.OPTASException;

/**
 *
 * @author S. Kralisch
 */
public class J2KTimeSerieWriter{
    
    protected SimpleDateFormat contentFormat = new SimpleDateFormat("yyyy-MM-dd\tHH:mm"){{setTimeZone(TimeZone.getTimeZone("GMT"));}};    
    protected SimpleDateFormat headerFormat = new SimpleDateFormat("dd.MM.yyyy\tHH:mm"){{setTimeZone(TimeZone.getTimeZone("GMT"));}};    
    
    private File fileHandle = null;
    private BufferedWriter writer = null;
    
    public J2KTimeSerieWriter(File target){
        fileHandle = target;
    }
            
    private void open() throws IOException{
        writer = new BufferedWriter(new FileWriter(fileHandle));
    }
    
    public void write(J2KTimeSerie timeserie) throws IOException{
        
        try{
        open();
        
        writeHeader(timeserie);
        writeContent(timeserie);
        
        }catch(Throwable t){
            throw t;
        } finally{
            try{
                writer.close();
            }catch(Throwable t){
                t.printStackTrace();
            }
        }
    }
       
    public void writeHeader(J2KTimeSerie timeserie) throws IOException {
        String header = "";
        header += "#created by " + this.getClass().getName() + " - projection is " + timeserie.getProjection() + "\n";
        header += "@dataValueAttribs\ndata\t0.0\t0.0\t?\n";
        header += "@dataSetAttribs\nmissingDataVal\t"+timeserie.getMissingDataString()+"\n";
        header += "dataStart\t" + headerFormat.format(timeserie.getTemporalDomain().getStart()) + "\n";
        header += "dataEnd\t" + headerFormat.format(timeserie.getTemporalDomain().getEnd()) + "\n";
        header += "tres\t"+getTresString(timeserie)+"\n";
        header += "@statAttribVal\n";
        
        String strStation = "name";
        String strID = "ID";
        String strElevation = "elevation";
        String strX = timeserie.getNameOfDimension(J2KTimeSerieHeader.Dimension.X);
        String strY = timeserie.getNameOfDimension(J2KTimeSerieHeader.Dimension.Y);
        String strDataColumn = "datacolumn";

        int m = timeserie.getTimeserieCount();
        for (int i = 0; i < m; i++) {
            strStation += "\t" + timeserie.getAttributeNames()[i];
            strID += "\t" + timeserie.getIds()[i];
            strElevation += "\t" + timeserie.getElevation()[i];                        
            strX += "\t" + timeserie.getLocation(i)[0];
            strY += "\t" + timeserie.getLocation(i)[1];            
            strDataColumn += "\t" + (i + 1);
        }

        header += strStation + "\n" + strID + "\n" + strElevation + "\n" + strX + "\n" + strY + "\n" + strDataColumn + "\n";
        header += "@dataVal";
       
        this.writer.write(header);
    }
    
    public void writeContent(J2KTimeSerie timeserie) throws IOException {
        StringBuilder content = new StringBuilder();
        
        for (int i=0;i<timeserie.getNumberOfTimesteps();i++){
            content.append("\n");
            content.append(contentFormat.format(timeserie.getTime(i)));
            for (int j=0;j<timeserie.getTimeserieCount();j++){
                content.append(timeserie.getValue(i)[j]);
            }
        }
        writer.write(content.toString());                
    }
    
    private String getTresString(J2KTimeSerie timeserie){
        int timeunit = timeserie.getTemporalDomain().getTimeUnit();
        switch(timeunit){
            case 1: return "y";
            case 2: return "m";
            case 6: return "d";
            case 11: return "h";
        }
        throw new OPTASException("Unsupported time unit " + timeunit);
    }    
}
