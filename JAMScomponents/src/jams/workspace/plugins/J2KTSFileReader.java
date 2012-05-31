/*
 * J2KTSFileReader.java
 * Created on 25. August 2008, 16:50
 *
 * This file is part of JAMS
 * Copyright (C) FSU Jena
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public License
 * as published by the Free Software Foundation; either version 3
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, see <http://www.gnu.org/licenses/>.
 *
 */
package jams.workspace.plugins;

import jams.io.BufferedFileReader;
import jams.workspace.DataReader;
import jams.workspace.DefaultDataSet;
import jams.workspace.Workspace;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

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


    public ReaderType getReaderType(){
        return ReaderType.Empty;
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

     public DefaultDataSet getMetadata(int index){
        throw new UnsupportedOperationException("Not supported yet.");
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

    public void setWorkspace(Workspace ws) throws IOException{
        File file = new File(dataFileName);
        File file2 = new File(ws.getInputDirectory(),file.getName());
        long p = this.reader.getPosition();
        dataFileName = file2.getAbsolutePath();
        this.init();
        this.reader.setPosition(p);

    }

    private void writeObject(ObjectOutputStream out) throws IOException{
        out.defaultWriteObject();
        out.writeLong(this.reader.getPosition());
    }

    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException{
        in.defaultReadObject();
        long position = in.readLong();
        if (this.reader!=null){
            try{
                this.reader.close();
            }catch(Exception e){}
        }
        init();
        this.reader.setPosition(position);
    }
}
