/*
 * StandardFertTillParaReader.java
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
import org.jams.j2k.s_n.Fertilizer;
import org.jams.j2k.s_n.Tillage;


/**
 *
 * @author c5ulbe
 */
public class StandardFertTillParaReader extends JAMSComponent {
    
    
    public HashMap<String, Fertilizer> readFertPara(String fileName) {
        
        BufferedReader reader;
        StringTokenizer tokenizer;
        String[] vals = new String[11];
        HashMap<String, Fertilizer> map = new HashMap<String, Fertilizer>();
        
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
                
                for (int i = 0; i < 11; i++) {
                    vals[i] = tokenizer.nextToken();
                }
                
                map.put(vals[0], new Fertilizer(vals));
                
                s = reader.readLine();
            }
            
        } catch (IOException ioe) {
            getModel().getRuntime().handle(ioe);
        }
        
        return map;
    }
    
    public HashMap<String, Tillage> readTillPara(String fileName) {
        
        BufferedReader reader;
        StringTokenizer tokenizer;
        String[] vals = new String[5];
        // neu seit Netbeans 5.0 ist die nähere Definition der HashMaps
        HashMap<String, Tillage> map = new HashMap<String, Tillage>();
        
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
                
                for (int i = 0; i < 5; i++) {
                    vals[i] = tokenizer.nextToken();
                }
                // Schreibe vals in die Tillage Hashmap
                map.put(vals[0], new Tillage(vals));
                
                
                
                s = reader.readLine();
            }
            
        } catch (IOException ioe) {
            getModel().getRuntime().handle(ioe);
        }
        
        return map;
    }
    
    public static void main(String[] x) {
        StandardFertTillParaReader r = new StandardFertTillParaReader();
        HashMap<String, Fertilizer> result1 = r.readFertPara("C:\\jamsmodels\\JAMS-Application\\Crop_Para\\fert.par");
        HashMap<String, Tillage> result2 = r.readTillPara("C:\\jamsmodels\\JAMS-Application\\Crop_Para\\till.par.txt");
    }
    
}