/*
 * JAMSSnapshot.java
 * Created on 5. November 2009, 16:25
 *
 * This file is part of JAMS
 * Copyright (C) FSU Jena
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

package jams.model;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import jams.JAMS;

/**
 *
 * @author Christian Fischer
 */
public class JAMSSnapshot implements Snapshot {
        boolean inMemory;
        byte[] data;
        String fileName;
        
        public JAMSSnapshot(boolean inMemory, byte[] data, String fileName){
            this.inMemory = inMemory;
            if (inMemory)
                this.data = data;
            else{
                this.fileName = fileName;            
                try{
                    FileOutputStream fos = new FileOutputStream(fileName);
                    fos.write(data);
                    fos.close();
                }catch(Exception e){
                    System.out.println(JAMS.resources.getString("Could_not_open_or_write_snapshot_file,_because_") + e.toString());
                }
            }
        }
        public byte[] getData(){
            if (!inMemory){
                try{
                    FileInputStream fis = new FileInputStream(fileName);
                    byte[] snapShotByteArray = new byte[fis.available()];
                    fis.read(snapShotByteArray);
                    fis.close(); 
                    return snapShotByteArray;
                }catch(Exception e){
                    System.out.println(JAMS.resources.getString("Could_not_open_or_read_snapshot_file,_because_") + e.toString());
                    return null;
                }
            }else{
                return data;
            }
        }
    }
