/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package jams.model;

import jams.tools.JAMSTools;
import jams.workspace.stores.DataStore;
import jams.workspace.stores.DefaultOutputDataStore;
import jams.workspace.stores.DefaultOutputDataStore.DefaultFilter;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

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
                    
    @Override
    public void recoverDataStoreState(DataStore store) throws IOException{
        Logger.getLogger(JAMSSmallModelState.class.getName()).log(Level.FINE, "recover:{0}", store.getID());
        byte[] data = dataStoreStateMap.get(store.getID());
        if (data == null){
            Logger.getLogger(JAMSSmallModelState.class.getName(),"recover:" + store.getID());
            Logger.getLogger(JAMSSmallModelState.class.getName()).log(Level.SEVERE, "could not recover:{0}", store.getID());
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
