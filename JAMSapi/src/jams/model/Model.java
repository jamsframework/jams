/*
 * Model.java
 * Created on 5. November 2009, 16:25
 *
 * This file is part of JAMS
 * Copyright (C) FSU Jena
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA
 *
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
    
    String getName();

    HashMap<Component, ArrayList<Field>> getNullFields();

    OutputDataStore[] getOutputDataStores(String contextName);

    JAMSRuntime getRuntime();

    Workspace getWorkspace();

    File getWorkspaceDirectory();

    void setAuthor(String author);

    void setDate(String date);
    
    void setName(String name);

    void setNullFields(HashMap<Component, ArrayList<Field>> nullFields);

    void setWorkspacePath(String workspaceDirectory);

    String getWorkspacePath();
}
