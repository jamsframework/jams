/*
 * StandardManagementParaReader.java
 *
 * Created on 6. MÃ¤rz 2006, 13:35
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
package io;

import crop.J2KSNTillage;
import crop.J2KSNLMArable;
import crop.J2KSNCrop;
import crop.J2KSNFertilizer;
import ages.types.HRU;
import java.util.*;
import java.io.*;
import oms3.annotations.*;
import oms3.io.CSTable;
import oms3.io.DataIO;
import static oms3.annotations.Role.*;

@Description
    ("Management (crop, rotation, tillage, fertilization) parameter file reader")
@Author
    (name="c5ulbe")
@Keywords
    ("I/O")
@SourceInfo
    ("$HeadURL: http://svn.javaforge.com/svn/oms/branches/oms3.prj.ceap/src/io/ManagementParaReader.java $")
@VersionInfo
    ("$Id: ManagementParaReader.java 996 2010-02-19 21:17:43Z odavid $")
@License
    ("http://www.gnu.org/licenses/gpl-2.0.html")

public class ManagementParaReader  {

    @Description("Crop parameter file name")
    @Role(PARAMETER)
    @In public File cropFile;

    @Description("Fertilization parameter file name")
    @Role(PARAMETER)
    @In public File fertFile;

    @Description("Tillage parameter file name")
    @Role(PARAMETER)
    @In public File tillFile;
    
    @Description("Management parameter file name")
    @Role(PARAMETER)
    @In public File mgmtFile;

    @Description("Rotation parameter file name")
    @Role(PARAMETER)
    @In public File rotFile;

    @Description("HRU rotation mapping file name")
    @Role(PARAMETER)
    @In public File hruRotFile;

    @Description("Collection of hru objects")
    @In @Out public List<HRU> hrus;

    // Fertilizer
    private HashMap<Integer, J2KSNFertilizer> readFertPara(File fileName) throws IOException {
        HashMap<Integer, J2KSNFertilizer> map = new HashMap<Integer, J2KSNFertilizer>();
        CSTable table = DataIO.table(fileName, "Parameter");
        for (String[] row : table.rows()) {
            map.put(new Integer(row[1]), new J2KSNFertilizer(row));
        }
        return map;
    }

    private HashMap<Integer, J2KSNTillage> readTillPara(File fileName) throws IOException {
        HashMap<Integer, J2KSNTillage> map = new HashMap<Integer, J2KSNTillage>();
        CSTable table = DataIO.table(fileName, "Parameter");
        for (String[] row : table.rows()) {
            map.put(new Integer(row[1]), new J2KSNTillage(row));
        }
        return map;
    }

    private HashMap<Integer, ArrayList<J2KSNLMArable>> readManagementPara(File fileName,
            HashMap<Integer, J2KSNTillage> tills, HashMap<Integer, J2KSNFertilizer> ferts) throws IOException {

        int oldCID = -1;
        int cid = -1;
        ArrayList<J2KSNLMArable> managements = null;
        HashMap<Integer, ArrayList<J2KSNLMArable>> map = new HashMap<Integer, ArrayList<J2KSNLMArable>>();
        CSTable table = DataIO.table(fileName, "Parameter");
        for (String[] row : table.rows()) {
            cid = Integer.parseInt(row[1]);
            if (cid != oldCID) {
                map.put(oldCID, managements);
                managements = new ArrayList<J2KSNLMArable>();
                oldCID = cid;
            }
            managements.add(new J2KSNLMArable(row, tills, ferts));
        }
        map.put(cid, managements);
        return map;
    }

    private HashMap<Integer, J2KSNCrop> readCrops(File fileName,
            HashMap<Integer, ArrayList<J2KSNLMArable>> management) throws IOException {
        HashMap<Integer, J2KSNCrop> map = new HashMap<Integer, J2KSNCrop>();
        CSTable table = DataIO.table(fileName, "Parameter");
        for (String[] row : table.rows()) {
            Integer id = Integer.parseInt(row[1]);     // TODO check this
            J2KSNCrop crop = new J2KSNCrop(row, management.get(id));
            map.put(crop.cid, crop);

        }
        return map;
    }

    private HashMap<Integer, ArrayList<J2KSNCrop>> readRotations(File fileName, HashMap<Integer, J2KSNCrop> crops) throws IOException {
        HashMap<Integer, ArrayList<J2KSNCrop>> map = new HashMap<Integer, ArrayList<J2KSNCrop>>();
        CSTable table = DataIO.table(fileName, "Parameter");
        for (String[] row : table.rows()) {
            ArrayList<J2KSNCrop> cropList = new ArrayList<J2KSNCrop>();
            Integer rid = Integer.parseInt(row[1]);
            for (int i = 2; i < row.length; i++) {
                if (row[i].isEmpty()) {
                    break;
                }
                Integer cid = Integer.parseInt(row[i]);
                cropList.add(crops.get(cid));
            }
            map.put(rid, cropList);
        }
        return map;
    }

    private void linkCrops(File fileName, HashMap<Integer, ArrayList<J2KSNCrop>> rotations) throws IOException {
        HashMap<Integer, HRU> hruMap = new HashMap<Integer, HRU>();
        for (HRU hru : hrus) {
            hruMap.put(hru.ID, hru);
        }

        CSTable table = DataIO.table(fileName, "Parameter");
        for (String[] row : table.rows()) {
            Integer hid = new Integer(row[1]);
            Integer rid = new Integer(row[2]);
            double redu_fac = Double.parseDouble(row[3]);
            ArrayList<J2KSNCrop> rotation = rotations.get(rid);
            HRU hru = hruMap.get(hid);
            hru.reductionFactor = redu_fac;
            hru.landuseRotation = rotation;
            hru.rotPos = 0;
            hru.managementPos = 0;
        }
    }

    @Execute
    public void execute() throws IOException {
        System.out.println("Reading Ferilizer Parameter ...");
        HashMap<Integer, J2KSNFertilizer> ferts = readFertPara(fertFile);

        System.out.println("Reading Tillage Parameter ...");
        HashMap<Integer, J2KSNTillage> tills = readTillPara(tillFile);

        System.out.println("Reading Management Parameter ...");
        HashMap<Integer, ArrayList<J2KSNLMArable>> mgmt = readManagementPara(mgmtFile, tills, ferts);

        System.out.println("Reading Crops Parameter ...");
        HashMap<Integer, J2KSNCrop> crops = readCrops(cropFile, mgmt);

        System.out.println("Reading Crop Rotation Parameter ...");
        HashMap<Integer, ArrayList<J2KSNCrop>> rotations = readRotations(rotFile, crops);

        System.out.println("Linking Crops...");
        linkCrops(hruRotFile, rotations);
        System.out.println("Done Management init.");
    }
}
