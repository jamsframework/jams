/*
 * Model.java
 * Created on 5. November 2009, 16:25
 *
 * This file is part of JAMS
 * Copyright (C) FSU Jena
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public License
 * as published by the Free Software Foundation; either version 3
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, see <http://www.gnu.org/licenses/>.
 *
 */
package jams.model;

import jams.runtime.JAMSRuntime;
import jams.workspace.Workspace;
import java.io.File;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;

/**
 *
 * @author Sven Kralisch <sven.kralisch at uni-jena.de>
 */
public interface Model extends Context {

    public String getName();

    public void setName(String name);

    public JAMSRuntime getRuntime();

    public Workspace getWorkspace();

    public File getWorkspaceDirectory();

    public void setProfiling(boolean profiling);
    
    public HashMap<Component, ArrayList<Field>> getNullFields();
    
    public void setNullFields(HashMap<Component, ArrayList<Field>> nullFields);

    public String getWorkspacePath();

    public void setWorkspacePath(String workspaceDirectory);

    public void measureTime(long startTime, Component c);

    public boolean isProfiling();        
    
    public long[] getProgress();  
    
    public void updateProgress();
        
}
