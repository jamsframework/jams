/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package jams.model;

import jams.tools.JAMSTools;
import jams.workspace.stores.DataStore;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.HashMap;

/**
 *
 * @author Christian Fischer
 */
public class JAMSSmallModelState implements SmallModelState{    
    HashMap<String, byte[]> dataStoreStateMap;
    long executionTime;
    
    public JAMSSmallModelState(){        
        dataStoreStateMap = new HashMap<String, byte[]>();
    }
                    
    public void recoverDataStoreState(DataStore store) throws IOException{
        System.out.println("recover:" + store.getID());
        byte[] data = dataStoreStateMap.get(store.getID());
        if (data == null){
            System.out.println("could not recover:" + store.getID());
            throw new IOException("could not recover:" + store.getID());
        }
        ByteArrayInputStream inStream = new ByteArrayInputStream(data);
        ObjectInputStream in = new ObjectInputStream(inStream);
        try{
            JAMSTools.cloneInto(store, in.readObject(),store.getClass());
        }catch(ClassNotFoundException cnfe){
            cnfe.printStackTrace();
        }
    }
    public void saveDataStoreState(DataStore state){
        ByteArrayOutputStream outStream = new ByteArrayOutputStream();
        try{
            ObjectOutputStream objOut = new ObjectOutputStream(outStream);
            objOut.writeObject(state);
            objOut.flush();
            objOut.close();
            outStream.close();
        }catch(IOException ioe){
            ioe.printStackTrace();
            System.out.println(ioe);
        }
        dataStoreStateMap.put(state.getID(), outStream.toByteArray());
    }
    
    public void setExecutionTime(long time){
        executionTime = time;
    }
    public long getExecutionTime(){
        return executionTime;
    }
}
