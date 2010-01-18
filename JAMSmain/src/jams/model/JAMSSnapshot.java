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
import jams.data.SnapshotData;
import jams.tools.SnapshotTools.JAMSSnapshotData;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

/**
 *
 * @author Christian Fischer
 */
public class JAMSSnapshot implements Snapshot {

    boolean inMemory;
    byte[] data;
    String fileName;

    public JAMSSnapshot(boolean inMemory, JAMSSnapshotData snapshotData, String fileName) throws IOException {
        ByteArrayOutputStream outStream = new ByteArrayOutputStream();
        ObjectOutputStream objOut = new ObjectOutputStream(outStream);
        objOut.writeObject(snapshotData);
        objOut.flush();
        outStream.flush();

        this.inMemory = inMemory;
        if (inMemory) {
            this.data = outStream.toByteArray();
        } else {
            this.fileName = fileName;
            try {
                FileOutputStream fos = new FileOutputStream(fileName);
                fos.write(data);
                fos.close();
            } catch (Exception e) {
                System.out.println(JAMS.resources.getString("Could_not_open_or_write_snapshot_file,_because_") + e.toString());
            }
        }
        objOut.close();
        outStream.close();
    }

    public JAMSSnapshot(String fileName) {
        this.inMemory = false;
        this.fileName = fileName;
        this.data = null;
    }

    public SnapshotData getData(){
        byte data[] = null;
        if (!inMemory) {
            try {
                FileInputStream fis = new FileInputStream(fileName);
                byte[] snapShotByteArray = new byte[fis.available()];
                fis.read(snapShotByteArray);
                fis.close();
                data = snapShotByteArray;
            } catch (Exception e) {
                System.out.println(JAMS.resources.getString("Could_not_open_or_read_snapshot_file,_because_") + e.toString());

            }
        } else {
            data = this.data;
        }
        //convert to snapshotdata
        ByteArrayInputStream inStream = new ByteArrayInputStream(data);
        try{
            ObjectInputStream objIn = new ObjectInputStream(inStream);
            SnapshotData snapshotData = (SnapshotData) objIn.readObject();
            objIn.close();
            inStream.close();        
            return snapshotData;
        }catch(Exception e){
            return null;
        }        
    }
}
