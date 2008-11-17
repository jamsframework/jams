/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package jams.model;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.Serializable;
import jams.JAMS;

/**
 *
 * @author Christian Fischer
 */
public class Snapshot implements Serializable{
        boolean inMemory;
        byte[] data;
        String fileName;
        
        public Snapshot(boolean inMemory, byte[] data, String fileName){
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
        byte[] getData(){
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
