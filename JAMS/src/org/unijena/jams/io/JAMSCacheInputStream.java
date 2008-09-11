/*
 * JAMSCacheInputStream.java
 * Created on 17. November 2006, 15:53
 *
 * This file is part of JAMS
 * Copyright (C) 2006 FSU Jena
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

package org.unijena.jams.io;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import org.unijena.jams.data.JAMSTimeInterval;

/**
 *
 * @author S. Kralisch
 */
public class JAMSCacheInputStream extends ObjectInputStream {
    
    private int neededByteLength, givenByteLength;
    private String givenID, neededID;
    private JAMSTimeInterval givenTimeInterval, neededTimeInterval;
    private long givenMultiplier, neededMultiplier;
    
    public JAMSCacheInputStream(InputStream in, int bitLength, String id, JAMSTimeInterval timeInterval, long multiplier) throws IOException {
        super(in);
        neededByteLength = Math.round(bitLength / 8);
        neededID = id;
        neededTimeInterval = timeInterval;
        neededMultiplier = multiplier;
        init();
    }
    
    private void init() throws IOException {
        try {
            //read header information
            givenByteLength = Math.round(this.readInt() / 8);
            givenID = (String) this.readObject();
            givenTimeInterval = new JAMSTimeInterval();
            givenTimeInterval.setValue((String) this.readObject());
            givenMultiplier = this.readLong();
            
            //compare given and needed parameter
            if (givenByteLength != neededByteLength)
                throw new CacheDataException("Given and needed data types do not match in cache input stream: " + givenByteLength + " <-> " + neededByteLength);
            if (!givenID.equals(neededID))
                throw new CacheDataException("Given and needed data IDs do not match in cache input stream " + givenID + " <-> " + neededID);
            if (givenMultiplier != neededMultiplier)
                throw new CacheDataException("Given and needed multipliers do not match in cache input stream " + givenMultiplier + " <-> " + neededMultiplier);
            if (!givenTimeInterval.encloses(neededTimeInterval))
                throw new CacheDataException("Given and needed time intervals do not match in cache input stream " + givenTimeInterval + " <-> " + neededTimeInterval);
            
            //skip some data if necessary
            long offset = givenTimeInterval.getStartOffset(neededTimeInterval);
            this.skip(offset * givenByteLength * givenMultiplier);
            
        } catch (ClassNotFoundException ex) {
            ex.printStackTrace();
        }
        
    }
    
    public static void main(String[] args) {

        ObjectInputStream reader;
        ObjectOutputStream writer;
        
        JAMSTimeInterval ti = new JAMSTimeInterval();
        ti.setValue("1996-11-01 7:30 2000-10-31 7:30 6 1");
        
        int hruCount = 37;
        
        try {
            
            writer = new JAMSCacheOutputStream(new BufferedOutputStream(new FileOutputStream("d:/test.bin")), Double.SIZE, "myData", ti, hruCount);
            for (int i = 0; i < 1000; i++) {
                for (int j = 0; j < hruCount; j++) {
                    writer.writeDouble(i);
                }
            }
            writer.flush();
            writer.close();
            
        } catch (FileNotFoundException ex) {
            ex.printStackTrace();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        
        
        JAMSTimeInterval ti2 = new JAMSTimeInterval();
        ti2.setValue("1997-01-03 7:30 1997-12-31 7:30 6 1");
        try {
            
            reader = new JAMSCacheInputStream(new BufferedInputStream(new FileInputStream("d:/test.bin")), Double.SIZE, "myData", ti2, hruCount);
            
            for (int j = 0; j < hruCount; j++) {
                System.out.println(reader.readDouble());
            }
            
            System.out.println(reader.readDouble());
            
        } catch (CacheDataException ex) {
            System.out.println("cache data problem");
        } catch (IOException ex) {
            System.out.println("file problem");
        }
    }
}
