/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.unijena.jams.model;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.Serializable;

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
                    System.out.println("Could not open or write snapshot file, because " + e.toString());
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
                    System.out.println("Could not open or read snapshot file, because " + e.toString());
                    return null;
                }
            }else{
                return data;
            }
        }
    }
