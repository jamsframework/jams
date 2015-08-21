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
import org.jams.j2k.s_n.management.*;
import jams.JAMS;
import jams.data.*;

/**
 *
 * @author c5ulbe
 */
public class StandardManagementParaReader_gui extends JAMSComponent {
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            update = JAMSVarDescription.UpdateType.INIT,
            description = "Crop parameter file name"
            )
            public JAMSString cropFileName;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            update = JAMSVarDescription.UpdateType.INIT,
            description = "Fertilization parameter file name"
            )
            public JAMSString fertFileName;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            update = JAMSVarDescription.UpdateType.INIT,
            description = "Tillage parameter file name"
            )
            public JAMSString tillFileName;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            update = JAMSVarDescription.UpdateType.INIT,
            description = "Management parameter file name"
            )
            public JAMSString managementFileName;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            update = JAMSVarDescription.UpdateType.INIT,
            description = "Rotation parameter file name"
            )
            public JAMSString rotationFileName;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            update = JAMSVarDescription.UpdateType.INIT,
            description = "HRU rotation mapping file name"
            )
            public JAMSString hruRotationFileName;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            update = JAMSVarDescription.UpdateType.RUN,
            description = "Collection of hru objects"
            )
            public JAMSEntityCollection hrus;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            update = JAMSVarDescription.UpdateType.INIT,
            description = "Attribute name of HRU ID"
            )
            public JAMSString aNameHRUID;
    
    
    
    
    
    // HashMap für Fertilizer anlegen
    protected HashMap<Integer, J2KSNFertilizer> readFertPara(String fileName) {
        
        BufferedReader reader;
        StringTokenizer tokenizer;
        String[] vals = new String[11];
        HashMap<Integer, J2KSNFertilizer> map = new HashMap<Integer, J2KSNFertilizer>();
        
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
                
                map.put(Integer.parseInt(vals[0]), new J2KSNFertilizer(vals));
                
                s = reader.readLine();
            }
            
        } catch (IOException ioe) {
            getModel().getRuntime().handle(ioe);
        }
        
        return map;
    }
    
    protected HashMap<Integer, J2KSNTillage> readTillPara(String fileName) {
        
        BufferedReader reader;
        StringTokenizer tokenizer;
        String[] vals = new String[5];
        // neu seit Netbeans 5.0 ist die nähere Definition der HashMaps gewünscht
        // HashMap für Tillage anlegen
        HashMap<Integer, J2KSNTillage> map = new HashMap<Integer, J2KSNTillage>();
        
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
                // Schreibe vals in die J2KSNTillage Hashmap
                map.put(Integer.parseInt(vals[0]), new J2KSNTillage(vals));
                
                
                
                s = reader.readLine();
            }
            
        } catch (IOException ioe) {
            getModel().getRuntime().handle(ioe);
        }
        
        return map;
    }
    
    // HashMap für Management anlegen; mit Verweis auf Tillage und Fertilizer
    private HashMap<Integer, ArrayList<J2KSNLMArable_gui>> readManagementPara(String fileName, HashMap<Integer, J2KSNTillage> tills, HashMap<Integer, J2KSNFertilizer> ferts, HashMap<Integer, J2KSNCrop_gui> crops) {
        
        BufferedReader reader;
        StringTokenizer tokenizer;
        String[] vals = new String[10];
        int oldCID = -1, cid = -1, id = -1, oldID = -1;
        ArrayList<J2KSNLMArable_gui> managements = null;
        // neu seit Java 5 ist die nähere Definition der Collections gewünscht
        // HashMap anlegen
        HashMap<Integer, ArrayList<J2KSNLMArable_gui>> map = new HashMap<Integer, ArrayList<J2KSNLMArable_gui>>();
        
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
                
                for (int i = 0; i < 10; i++) {
                    vals[i] = tokenizer.nextToken();
                }
                
                id = Integer.parseInt(vals[0]);
                //cid = Integer.parseInt(vals[1]);
                if (id != oldID) {
                    map.put(oldID, managements);
                    managements = new ArrayList<J2KSNLMArable_gui>();
                    oldID = id;
                }
                
                managements.add(new J2KSNLMArable_gui(vals, tills, ferts, crops));
                
                s = reader.readLine();
            }
            map.put(id, managements);
            
            
            
        } catch (IOException ioe) {
            getModel().getRuntime().handle(ioe);
        }
        
        return map;
    }
    
    // HashMap für Crop anlegen; mit Verweis auf Management, respective tillage und fertilizer
    protected HashMap<Integer, J2KSNCrop_gui> readCrops(String fileName) {
        
        BufferedReader reader;
        StringTokenizer tokenizer;
        String[] vals = new String[50];
        Integer cid;
        
        // HashMap anlegen
        HashMap<Integer, J2KSNCrop_gui> map = new HashMap<Integer, J2KSNCrop_gui>();
        
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
                
                for (int i = 0; i < 50; i++) {
                    vals[i] = tokenizer.nextToken();
                }
                
                cid = Integer.parseInt(vals[0]);
                J2KSNCrop_gui crop = new J2KSNCrop_gui(vals);
                map.put(crop.cid, crop);
                
                //map.put(id, new J2KSNCrop(vals, management.get(id)));
                
                s = reader.readLine();
            }
            
            
        } catch (IOException ioe) {
            getModel().getRuntime().handle(ioe);
        }
        
        return map;
    }
    // haspMap für crop rotations, respective vorherige Hashmaps
    protected HashMap<Integer, ArrayList <ManagementOption>> readRotations(String fileName, HashMap<Integer, ArrayList<J2KSNLMArable_gui>> managements) {
        
        BufferedReader reader;
        StringTokenizer tokenizer;
        Integer rid, mid, days;
        Integer [] gaparray;
        String gapstr;
        ManagementOption management;
        management = null;
        ArrayList <ManagementOption> managementList;
        
        
        // neu seit Java 5 ist die nähere Definition der Collections gewünscht
        // HashMap anlegen
        HashMap<Integer, ArrayList <ManagementOption>> map = new HashMap<Integer, ArrayList <ManagementOption>>();
        
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
                int count = tokenizer.countTokens();
                managementList = new ArrayList <ManagementOption>(count);
                int c = 0;
                
                
                rid = Integer.parseInt(tokenizer.nextToken());
                
                                     
                
                while (tokenizer.hasMoreElements()) {
                    gapstr = tokenizer.nextToken();                    
                    if (gapstr.startsWith("/")){
                      days = Integer.parseInt(gapstr.substring(1));
                      management = new ManagementGap(days);
                      managementList.add(c, management);
//                      gaparray[c] = gap;
                    }else{
                        
                    }
                        
                    mid = Integer.parseInt(tokenizer.nextToken());
                     
                    //int mID = 1;
                    
                             
                    
                    ArrayList<J2KSNLMArable_gui> mList = managements.get(mid);
                    management = new J2KSNManagentType(mid,mList);
                    managementList.add(c, management);
                    c++;
                    /*
                    J2KSNCrop crop = crops.get(cid);
                    cropList.add(crop);
                     */
                    
                    if (management instanceof ManagementGap) {
                        //1. Fall: Lücke
                        int d = ((ManagementGap) management).getDays();
                    } else {
                        //2. Fall: Management
                        J2KSNManagentType t = (J2KSNManagentType) management;
                    }
                        
                }
                
                
                
                map.put(rid, managementList);
               
                
                //map.put(id, new J2KSNCrop(vals, management.get(id)));
                
                s = reader.readLine();
            }
            
            
        } catch (IOException ioe) {
            getModel().getRuntime().handle(ioe);
        }
        
        return map;
    }
    
    protected void linkCrops(String fileName, HashMap<Integer, ArrayList <ManagementOption>> rotations) {
        
        BufferedReader reader;
        StringTokenizer tokenizer;
        Double hid, redu_fac;
        Integer rid;
        ArrayList <ManagementOption> rotation;
        Attribute.Entity hru;
        HashMap<Double, Attribute.Entity> hruMap = new HashMap<Double, Attribute.Entity>();
        
        //put all entities into a HashMap with their ID as key
        Iterator<Attribute.Entity> hruIterator = hrus.getEntities().iterator();
        while (hruIterator.hasNext()) {
            hru = hruIterator.next();
            try {
                hruMap.put(hru.getDouble(aNameHRUID.getValue()),  hru);
            } catch (JAMSEntity.NoSuchAttributeException nsae) {
//                getModel().sendHalt("Attribute " + aNameHRUID + " not found!");
            }
        }
        
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
                
                hid = Double.parseDouble(tokenizer.nextToken());
                if (hid == 600){
                    double dummy = 0;
                }
                rid = Integer.parseInt(tokenizer.nextToken());
                redu_fac = Double.parseDouble(tokenizer.nextToken());
                
                rotation = rotations.get(rid);
                hru = hruMap.get(hid);
                
                hru.setDouble("ReductionFactor",  redu_fac);
                hru.setObject("landuseRotation",  rotation);
                hru.setInt("rotPos", 0);
                hru.setInt("managementPos", 0);
                
                
                s = reader.readLine();
            }
            
            
        } catch (IOException ioe) {
            getModel().getRuntime().handle(ioe);
        }
        
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
        HashMap<Integer, J2KSNCrop_gui> crops = readCrops(getModel().getWorkspaceDirectory().getPath() + "/" + cropFileName.getValue());
        HashMap<Integer, ArrayList<J2KSNLMArable_gui>> managements;
        managements = readManagementPara(getModel().getWorkspaceDirectory().getPath() + "/" + managementFileName.getValue(), tills, ferts, crops);
        

        
        
        
        HashMap<Integer, ArrayList <ManagementOption>> rotations = readRotations(getModel().getWorkspaceDirectory().getPath() + "/" + rotationFileName.getValue(), managements);
        linkCrops(getModel().getWorkspaceDirectory().getPath() + "/" + hruRotationFileName.getValue(), rotations);
    }
    
    public static void main(String[] x) {
        StandardManagementParaReader r = new StandardManagementParaReader();
        r.run();
        
    }
}