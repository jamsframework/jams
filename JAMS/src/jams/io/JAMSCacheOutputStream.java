/*
 * JAMSCacheOutputStream.java
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

package jams.io;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import jams.data.JAMSTimeInterval;

/**
 *
 * @author S. Kralisch
 */
public class JAMSCacheOutputStream extends ObjectOutputStream {
  
    public JAMSCacheOutputStream(OutputStream out, int bitLength, String id, JAMSTimeInterval timeInterval, long multiplier) throws IOException {
        
        super(out);
        
        this.writeInt(bitLength);
        this.writeObject(id);
        this.writeObject(timeInterval.toString());
        this.writeLong(multiplier);
    }
        
}
