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

import jams.io.BufferedFileReader;
import jams.workspace.DataReader;
import jams.workspace.DefaultDataSet;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

/**
 *
 * @author Sven Kralisch <sven.kralisch at uni-jena.de>
 */
    public class J2KTSFileReader implements DataReader {

    private String dataFileName;
    transient private BufferedFileReader reader;
    
    @Override
    public int init() {
        int result;
        File file = new File(dataFileName);
        if (file.exists()) {
            try {                
                this.reader = new BufferedFileReader(new FileInputStream(file));
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
    public DefaultDataSet[] getData() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public int numberOfColumns() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void setDataFileName(String dataFileName) {
        this.dataFileName = dataFileName;
    }
    
    public static class J2KTSFileReaderState implements DataReaderState{
        String fileName;
        long position;
    }
    public J2KTSFileReaderState getState(){
        J2KTSFileReaderState state = new J2KTSFileReaderState();
        state.fileName = this.dataFileName;
        state.position = this.reader.getPosition();      
        return state;
    }
    
    public void setState(DataReaderState state) throws IOException{
        J2KTSFileReaderState J2KTSstate = (J2KTSFileReaderState)state;
        this.dataFileName = J2KTSstate.fileName;
        if (this.reader!=null){
            try{
                this.reader.close();
            }catch(Exception e){}
        }
        init();
        this.reader.setPosition(J2KTSstate.position);        
    }
    
}
