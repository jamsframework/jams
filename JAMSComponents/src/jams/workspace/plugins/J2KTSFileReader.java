/*
 * J2KTSFileReader.java
 * Created on 25. August 2008, 16:50
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
package jams.workspace.plugins;

import jams.workspace.DataReader;
import jams.workspace.DataSet;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

/**
 *
 * @author Sven Kralisch <sven.kralisch at uni-jena.de>
 */
    public class J2KTSFileReader implements DataReader {

    private String dataFileName;
    private BufferedReader reader;

    @Override
    public int init() {
        int result;
        File file = new File(dataFileName);
        if (file.exists()) {
            try {
                this.reader = new BufferedReader(new FileReader(file));
                readMetaData();
                result = 0;
            } catch (IOException ioe) {
                System.err.println("J2KTSFileReader: " + ioe);
                result = -1;
            }
        } else {
            result = -2;
        }
        return result;
    }
    
    private void readMetaData() {
        
    }

    @Override
    public int cleanup() {
        int result = 0;
        if (this.reader != null) {
            try {
                this.reader.close();
            } catch (IOException ioe) {
                System.err.println("J2KTSFileReader: " + ioe);
                result = -1;
            }
        }
        return result;
    }

    @Override
    public int fetchValues() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public int fetchValues(int count) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public DataSet[] getData() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public int numberOfColumns() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void setDataFileName(String dataFileName) {
        this.dataFileName = dataFileName;
    }
}
