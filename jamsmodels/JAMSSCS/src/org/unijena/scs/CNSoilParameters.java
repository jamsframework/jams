/*
 * CNSoilParameters.java
 * Created on 17. July 2006, 17:15
 *
 * This file is part of JAMS
 * Copyright (C) 2005 S. Kralisch and P. Krause
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
package org.unijena.scs;
import jams.data.Attribute;
import jams.model.JAMSComponent;
import jams.model.JAMSComponentDescription;
import jams.model.JAMSVarDescription;
import jams.model.Model;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.Locale;
import java.util.StringTokenizer;
import java.util.Vector;



/**
 * Computes the catchment's mean CN value based on spatial distributed input values
 * read from file
 * @author Peter Krause
 */
@JAMSComponentDescription(
title="CN-SoilParameters",
        author="Peter Krause",
        description="Preliminary class for estimation of soil CN values"
        )
        public class CNSoilParameters extends JAMSComponent {
    
    /**
     * the model's workspace directory<br>
     * access: READ<br>
     * update: INIT<br>
     * unit: n/a
     */
    @JAMSVarDescription(
    access = JAMSVarDescription.AccessType.READ,
            description = "Workspace directory name"
            )
            public Attribute.String dirName;
    
    /**
     * the filename of the CN-parameter file containing CN-values for specific landuse
     * and soil combinations<br>
     * access: READ<br>
     * update: INIT<br>
     * unit: n/a
     */
    @JAMSVarDescription(
    access = JAMSVarDescription.AccessType.READ,
            description = "CN-Number parameter file name"
            )
            public Attribute.String cnFileName;
    
    /**
     * the filename of the parameter file containing the spatial distributed information
     * of landuse and soiltypes in form of response unit entries. File must contain:
     * ID, LID (landuse-id), SID (soil-id), area (square meter)<br>
     * access: READ<br>
     * update: INIT<br>
     * unit: n/a
     */
    @JAMSVarDescription(
    access = JAMSVarDescription.AccessType.READ,
            description = "HRUs parameter file name"
            )
            public Attribute.String hruFileName;
    
    /**
     * the catchment's mean CN value<br>
     * access: WRITE<br>
     * update: RUN<br>
     * unit: n/a
     */
    @JAMSVarDescription(
    access = JAMSVarDescription.AccessType.WRITE,
            description = "catchment CN value"
            )
            public Attribute.Double cnValue;
    
    /**
     * the catchment's area in square meters<br>
     * access: WRITE<br>
     * update: RUN<br>
     * unit: m^2
     */
    @JAMSVarDescription(
    access = JAMSVarDescription.AccessType.WRITE,
            description = "catchment area"
            )
            public Attribute.Double catchmentArea;
    
    int luEntries = 0;
    int hruEntries = 0;
    
    /**
     * the components init method
     * @throws org.unijena.jams.data.Attribute.Entity.NoSuchAttributeException thrown when a model entity tries to access a non existent attribute
     */
    public void init() {
        this.catchmentArea.setValue(0);
        this.cnValue.setValue(0);
        
        luEntries = 0;
        hruEntries = 0;
        
        String cnFN = this.dirName.getValue() + java.io.File.separator + this.cnFileName.getValue();
        String hruFN = this.dirName.getValue() + java.io.File.separator + this.hruFileName.getValue();
        readParameters(cnFN, hruFN, this.getModel());
        
        getModel().getRuntime().println("Einzugsgebietsgr��e [km�]: " + String.format(Locale.US,"%.2f",this.catchmentArea.getValue()/ 1000000));
    }
    
    /**
     *
     * @param cnFileName
     * @param hruFileName
     * @param model
     */
    private void readParameters(String cnFileName, String hruFileName, Model model){
        BufferedReader cnReader, hruReader;
        StringTokenizer tokenizer;
        
        //scan the cn-file to get number of entries
        try{
            cnReader = new BufferedReader(new FileReader(cnFileName));
            String line = "#";
            // get rid of comments
            while (line.startsWith("#")) {
                line = cnReader.readLine();
            }
            
            // get first line of data
            line = cnReader.readLine();
            // counting entries
            while (line != null)  {
                luEntries++;
                line = cnReader.readLine();
            }
            
            cnReader.close();
        }catch (IOException ioe) {
            model.getRuntime().handle(ioe);
        }
        
        // scan the hru-file to get number of entries
        try{
            hruReader = new BufferedReader(new FileReader(hruFileName));
            String line = "#";
            // get rid of comments
            while (line.startsWith("#")) {
                line = hruReader.readLine();
            }
            
            // get first line of data containing the columns
            line = hruReader.readLine();
            // counting entries
            while (line != null)  {
                hruEntries++;
                line = hruReader.readLine();
            }
            
            hruReader.close();
        }catch (IOException ioe) {
            model.getRuntime().handle(ioe);
        }
        
        //matrix to read in the cn file values
        double[][] cnMatrix = new double[luEntries][5];
        
        //scan the cn-file to set up cnMatrix
        try{
            cnReader = new BufferedReader(new FileReader(cnFileName));
            String line = "#";
            // get rid of comments
            while (line.startsWith("#")) {
                line = cnReader.readLine();
            }
            
            // get first line of data
            line = cnReader.readLine();
            // filling the matrix
            int entry = 0;
            while (line != null)  {
                tokenizer = new StringTokenizer(line, "\t");
                //retreiving the landuse id
                cnMatrix[entry][0] = new Double(tokenizer.nextToken()).doubleValue();
                //skipping literal description
                tokenizer.nextToken();
                //retreiving the cn-Values [A to D]
                for(int i = 1; i < 5; i++){
                    cnMatrix[entry][i] = new Double(tokenizer.nextToken()).doubleValue();
                }
                entry++;
                line = cnReader.readLine();
            }
            cnReader.close();
        }catch (IOException ioe) {
            model.getRuntime().handle(ioe);
        }
        
        int[] idVal = new int[hruEntries];
        double[] areaVal = new double[hruEntries];
        String[] sidVal = new String[hruEntries];
        int[] lidVal = new int[hruEntries];
        
        //scan the hru file to set up hruMatrix
        try {
            hruReader = new BufferedReader(new FileReader(hruFileName));
            
            String line = "#";
            
            // get rid of comments
            while (line.startsWith("#")) {
                line = hruReader.readLine();
            }
            
            //put the attribure names into a vector
            Vector<String> attributeNames = new Vector<String>();
            tokenizer = new StringTokenizer(line, ",\t");
            while (tokenizer.hasMoreTokens()) {
                attributeNames.add(tokenizer.nextToken());
            }
            
            //determine in which columns the specific attributes are stored
            tokenizer = new StringTokenizer(line, "\t,");
            int counter = 0;
            int idCol = -1;
            int sidCol = -1;
            int areaCol = -1;
            int lidCol = -1;
            while(tokenizer.hasMoreTokens()){
                String tok = tokenizer.nextToken();
                if(!tok.equalsIgnoreCase("\"id\"") && !tok.equalsIgnoreCase("id"))
                    counter++;
                else
                    idCol = counter;
            }
            if(idCol < 0){
                System.out.println("ID-Column is not existent in HRU File!");
            }
            
            counter = 0;
            tokenizer = new StringTokenizer(line, ",\t");
            while(tokenizer.hasMoreTokens()){
                String tok = tokenizer.nextToken();
                if(!tok.equalsIgnoreCase("\"area\"") && !tok.equalsIgnoreCase("area"))
                    counter++;
                else
                    areaCol = counter;
            }
            if(areaCol < 0){
                System.out.println("Area-Column is not existent in HRU File!");
            }
            
            counter = 0;
            tokenizer = new StringTokenizer(line, ",\t");
            while(tokenizer.hasMoreTokens()){
                String tok = tokenizer.nextToken();
                if(!tok.equalsIgnoreCase("\"sid\"") && !tok.equalsIgnoreCase("sid"))
                    counter++;
                else
                    sidCol = counter;
            }
            if(sidCol < 0){
                System.out.println("SID-Column is not existent in HRU File!");
            }
            
            counter = 0;
            tokenizer = new StringTokenizer(line, ",\t");
            while(tokenizer.hasMoreTokens()){
                String tok = tokenizer.nextToken();
                if(!tok.equalsIgnoreCase("\"lid\"") && !tok.equalsIgnoreCase("lid"))
                    counter++;
                else
                    lidCol = counter;
            }
            if(lidCol < 0){
                System.out.println("LID-Column is not existent in HRU File!");
            }
            
            //get first line of hru data
            line = hruReader.readLine();
            int currentEntry = 0;
            while (line != null)  {
                tokenizer = new StringTokenizer(line, ",\t");
                //retreiving the relevant values
                int tk_entries = tokenizer.countTokens();
                String[] toks = new String[tk_entries];
                for(int i = 0; i < tk_entries; i++){
                    String tk = tokenizer.nextToken();
                    toks[i] = tk;
                }
                idVal[currentEntry] = new Integer(toks[idCol]).intValue();
                sidVal[currentEntry] = toks[sidCol];
                lidVal[currentEntry] = new Integer(toks[lidCol]).intValue();
                areaVal[currentEntry] = new Double(toks[areaCol]).doubleValue();
                
                
                currentEntry++;
                line = hruReader.readLine();
            }
            hruReader.close();
            
        } catch (IOException ioe) {
            model.getRuntime().handle(ioe);
        }
        
        //now everything is put together
        
        //compute catchment area
        for(int i = 0; i < hruEntries; i++){
            this.catchmentArea.setValue(this.catchmentArea.getValue() + areaVal[i]);
        }
        
        //compute catchment cn-value
        for(int i = 0; i < hruEntries; i++){
            double relArea = areaVal[i] / this.catchmentArea.getValue();
            //int landuseID = new Integer(hruMatrix[i][2]).intValue();
            //String soilType = hruMatrix[i][3];
            
            //looking for the correct entry in cnMatrix
            for(int j = 0; j < luEntries; j++){
                if(lidVal[i] == (int)cnMatrix[j][0]){
                    if(sidVal[i].equals("A")){
                        this.cnValue.setValue(this.cnValue.getValue() + (cnMatrix[j][1] * relArea));
                        break;
                    } else if(sidVal[i].equals("B")){
                        this.cnValue.setValue(this.cnValue.getValue() + (cnMatrix[j][2] * relArea));
                        break;
                    } else if(sidVal[i].equals("C")){
                        this.cnValue.setValue(this.cnValue.getValue() + (cnMatrix[j][3] * relArea));
                        break;
                    } else if(sidVal[i].equals("D")){
                        this.cnValue.setValue(this.cnValue.getValue() + (cnMatrix[j][4] * relArea));
                        break;
                    } else{
                        getModel().getRuntime().println("Soil entry of HRU " + idVal[i] + " is not valid");
                        break;
                    }
                }
            }
        }
        double cn = this.cnValue.getValue();
        int cni = (int)cn;
        double rest = cn - cni;
        if(rest >= 0.5)
            cni++;
        this.cnValue.setValue(cni);
    }
    
}
