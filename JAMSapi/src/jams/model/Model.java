/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package jams.model;

import jams.runtime.JAMSRuntime;
import jams.workspace.InvalidWorkspaceException;
import jams.workspace.Workspace;
import jams.workspace.stores.OutputDataStore;
import java.io.File;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;

/**
 *
 * @author Sven Kralisch <sven.kralisch at uni-jena.de>
 */
public interface Model extends Context {

    String getAuthor();

    String getDate();

    Snapshot getModelState(boolean holdInMemory, String fileName, Component position);

    String getName();

    HashMap<Component, ArrayList<Field>> getNullFields();

    OutputDataStore[] getOutputDataStores(String contextName);

    JAMSRuntime getRuntime();

    Workspace getWorkspace();

    File getWorkspaceDirectory();

    void setAuthor(String author);

    void setDate(String date);

    @SuppressWarnings (value = "unchecked")
    void setModelState(Snapshot inData);

    void setName(String name);

    void setNullFields(HashMap<Component, ArrayList<Field>> nullFields);

    void setWorkspaceDirectory(String workspaceDirectory);

    void initWorkspace() throws InvalidWorkspaceException;

}
