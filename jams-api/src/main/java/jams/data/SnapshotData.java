/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package jams.data;

import jams.model.Context;
import jams.workspace.stores.InputDataStore;
import java.io.IOException;
import java.io.Serializable;

/**
 *
 * @author Christian Fischer
 */
public interface SnapshotData extends Serializable {
    
    public void addContextState(Context currentContext);
    public void addDataStoreState(InputDataStore store) throws IOException;
    public void getDataStoreState(InputDataStore store) throws IOException, ClassNotFoundException, Exception;
    public void getContextState(Context currentContext, boolean restoreIterator) throws Exception;
}
