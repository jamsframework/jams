/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package jams.model;

import jams.workspace.stores.DataStore;
import jams.workspace.stores.DataStoreState;
import java.io.IOException;
import java.util.HashMap;

/**
 *
 * @author Christian Fischer
 */
public class JAMSSmallModelState implements SmallModelState{    
    HashMap<String, DataStoreState> dataStoreStateMap;
    long executionTime;
    
    public JAMSSmallModelState(){        
        dataStoreStateMap = new HashMap<String, DataStoreState>();
    }
                    
    public void recoverDataStoreState(DataStore store) throws IOException{
        store.setState(dataStoreStateMap.get(store.getID()));
    }
    public void saveDataStoreState(DataStore state){
        dataStoreStateMap.put(state.getID(), state.getState());
    }
    
    public void setExecutionTime(long time){
        executionTime = time;
    }
    public long getExecutionTime(){
        return executionTime;
    }
}
