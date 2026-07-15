/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package jams.model;

import jams.workspace.stores.DataStore;
import java.io.IOException;
import java.io.Serializable;

/**
 *
 * @author Christian Fischer
 */
public interface SmallModelState extends Serializable {            
    public void recoverDataStoreState(DataStore state) throws IOException;
    public void saveDataStoreState(DataStore state);
    public long getExecutionTime();
    public void setExecutionTime(long time);
}