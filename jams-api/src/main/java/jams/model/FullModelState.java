/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package jams.model;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;

/**
 *
 * @author Christian Fischer
 */
public interface FullModelState extends Serializable{           
    SmallModelState getSmallModelState();
    void setSmallModelState(SmallModelState state);

    Model getModel();
    void  setModel(Model data) throws IOException;
        
    void writeToFile(File file) throws IOException;
}