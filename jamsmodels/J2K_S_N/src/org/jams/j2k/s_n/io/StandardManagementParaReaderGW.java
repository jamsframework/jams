/*
 * StandardManagementParaReader.java
 *
 * Created on 6. März 2006, 13:35
 *
 * * This file is part of JAMS
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

package org.jams.j2k.s_n.io;

import jams.model.*;
import java.util.*;
import java.io.*;
import org.jams.j2k.s_n.crop.*;
import jams.JAMS;
import jams.data.*;

/**
 *
 * @author c5ulbe
 */
public class StandardManagementParaReaderGW extends StandardManagementParaReader {
    
  
    // HashMap für Fertilizer anlegen

    
    // HashMap für Management anlegen; mit Verweis auf Tillage und Fertilizer
    private HashMap<Integer, ArrayList<J2KSNLMArable>> readManagementPara(String fileName, HashMap<Integer, J2KSNTillage> tills, HashMap<Integer, J2KSNFertilizer> ferts) {
          
        BufferedReader reader;
        StringTokenizer tokenizer;
        String[] vals = new String[9];
        int oldCID = -1, cid = -1;
        ArrayList<J2KSNLMArable> managements = null;
        // neu seit Java 5 ist die nähere Definition der Collections gewünscht
        // HashMap anlegen
        HashMap<Integer, ArrayList<J2KSNLMArable>> map = new HashMap<Integer, ArrayList<J2KSNLMArable>>();
        
        try {
            
            reader = new BufferedReader(new FileReader(fileName));
            
            String s = "#";
            
            // get rid of comments
            while (s.startsWith("#")) {
                s = reader.readLine();
            }
            s = reader.readLine();
            
            while ((s != null) && !s.startsWith("#"))  {
                
                tokenizer = new StringTokenizer(s, "\t");
                
                for (int i = 0; i < 9; i++) {
                    vals[i] = tokenizer.nextToken();
                }
                
                cid = Integer.parseInt(vals[0]);
                if (cid != oldCID) {
                    map.put(oldCID, managements);
                    managements = new ArrayList<J2KSNLMArable>();
                    oldCID = cid;
                }
                
                managements.add(new J2KSNLMArableGW(vals, tills, ferts));
                
                s = reader.readLine();
            }
            map.put(cid, managements);
            
        } catch (IOException ioe) {
            getModel().getRuntime().handle(ioe);
        }
        
        return map;
    }
    
       public void run() {
        /*
        HashMap<Integer, J2KSNFertilizer> ferts = readFertPara("C:\\jamsmodels\\JAMS-Application\\JAMS-Arnstadt\\parameter\\fert.par");
        HashMap<Integer, J2KSNTillage> tills = readTillPara("C:\\jamsmodels\\JAMS-Application\\JAMS-Arnstadt\\parameter\\till.par");
        HashMap<Integer, ArrayList<J2KSNLMArable>> managements = readManagementPara("C:\\jamsmodels\\JAMS-Application\\JAMS-Arnstadt\\parameter\\lmArable.par", tills, ferts);
        HashMap<Integer, J2KSNCrop> crops = readCrops("C:\\jamsmodels\\JAMS-Application\\JAMS-Arnstadt\\parameter\\crop.par", managements);
        HashMap<Integer, ArrayList<J2KSNCrop>> rotations = readRotations("C:\\jamsmodels\\JAMS-Application\\JAMS-Arnstadt\\parameter\\croprotation.par", crops);
        linkCrops("C:\\jamsmodels\\JAMS-Application\\JAMS-Arnstadt\\parameter\\hrus_rot.par", rotations);
         */
        
        HashMap<Integer, J2KSNFertilizer> ferts = readFertPara(getModel().getWorkspaceDirectory().getPath() + "/" + fertFileName.getValue());
        HashMap<Integer, J2KSNTillage> tills = readTillPara(getModel().getWorkspaceDirectory().getPath() + "/" + tillFileName.getValue());
        HashMap<Integer, ArrayList<J2KSNLMArable>> managements = readManagementPara(getModel().getWorkspaceDirectory().getPath() + "/" + managementFileName.getValue(), tills, ferts);
        HashMap<Integer, J2KSNCrop> crops = readCrops(getModel().getWorkspaceDirectory().getPath() + "/" + cropFileName.getValue(), managements);
        HashMap<Integer, ArrayList<J2KSNCrop>> rotations = readRotations(getModel().getWorkspaceDirectory().getPath() + "/" + rotationFileName.getValue(), crops);
        linkCrops(getModel().getWorkspaceDirectory().getPath() + "/" + hruRotationFileName.getValue(), rotations);
    }
    
    // HashMap für Crop anlegen; mit Verweis auf Management, respective tillage und fertilizer
    
    

}