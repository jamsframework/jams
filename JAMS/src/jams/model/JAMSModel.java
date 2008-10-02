/*
 * JAMSModel.java
 * Created on 31. Mai 2006, 17:03
 *
 * This file is part of JAMS
 * Copyright (C) 2005 FSU Jena
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

import jams.data.JAMSDirName;
import jams.workspace.VirtualWorkspace;
import jams.workspace.stores.OutputDataStore;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.HashMap;
import jams.data.JAMSEntityCollection;
import jams.runtime.JAMSRuntime;

/**
 *
 * @author S. Kralisch
 */
@JAMSComponentDescription(title = "JAMS model",
author = "Sven Kralisch",
date = "26. September 2005",
description = "This component represents a JAMS model which is a special type of context component")
public class JAMSModel extends JAMSContext {
/*
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
    description = "Model workspace directory")
    public JAMSDirName workspaceDirectory;
*/
    private JAMSRuntime runtime;
    private String name, author, date;
    private VirtualWorkspace workspace;

    public JAMSModel(JAMSRuntime runtime) {
        this.runtime = runtime;
    }

    public JAMSRuntime getRuntime() {
        return runtime;
    }

    public void setRuntime(JAMSRuntime runtime) {
        this.runtime = runtime;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
        this.setInstanceName(name);
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    @Override
    public void init() {
        super.init();
        // save current model parameter to workspace output directory
        getRuntime().saveModelParameter();
    }

    public void setWorkspaceDir(String workspaceDir) {
        this.workspace = new VirtualWorkspace(new File(workspaceDir), runtime);
    }

    public VirtualWorkspace getWorkspace() {
        return workspace;
    }

    public File getWorkspaceDirectory() {
        return workspace.getDirectory();
    }

    public File getOutputDataDirectory() {
        return workspace.getOutputDataDirectory();
    }

    public File getInputDirectory() {
        return workspace.getInputDirectory();
    }

    public OutputDataStore[] getOutputDataStores(String contextName) {
        if (this.workspace == null) {
            return null;
        }
        return this.workspace.getOutputDataStores(contextName);
    }

    private void CollectEntityCollections(JAMSContext currentContext, JAMSComponent position, HashMap<String, JAMSEntityCollection> collection) {
        currentContext.updateEntityData(position);
        collection.put(currentContext.instanceName, currentContext.getEntities());

        for (int i = 0; i < currentContext.components.size(); i++) {
            JAMSComponent c = (JAMSComponent) currentContext.getComponents().get(i);
            if (c instanceof JAMSContext) {
                CollectEntityCollections((JAMSContext) c, position, collection);
            }
        }
    }

    private void RestoreEntityCollections(JAMSContext currentContext, HashMap<String, JAMSEntityCollection> collection) {
        JAMSEntityCollection e = collection.get(currentContext.instanceName);
        if (e != null) {
            currentContext.setEntities(e);
        }
        for (int i = 0; i < currentContext.components.size(); i++) {
            JAMSComponent c = (JAMSComponent) currentContext.getComponents().get(i);
            if (c instanceof JAMSContext) {
                RestoreEntityCollections((JAMSContext) c, collection);
            }
        }
        currentContext.initAccessors();
    }

    public Snapshot GetModelState(boolean holdInMemory, String fileName, JAMSComponent position) {
        ByteArrayOutputStream outStream = new ByteArrayOutputStream();
        ObjectOutputStream objOut = null;

        HashMap<String, JAMSEntityCollection> contextStates = new HashMap<String, JAMSEntityCollection>();
        CollectEntityCollections(this.getModel(), position, contextStates);

        try {
            objOut = new ObjectOutputStream(outStream);
            objOut.writeObject(contextStates);
        } catch (IOException e) {
            this.getRuntime().sendErrorMsg("Unable to save model state because," + e.toString());
        }

        return new Snapshot(holdInMemory, outStream.toByteArray(), fileName);
    }

    @SuppressWarnings("unchecked")
    public void SetModelState(Snapshot inData) {
        HashMap<String, JAMSEntityCollection> contextStates = null;
        try {
            ObjectInputStream objIn = new ObjectInputStream(new ByteArrayInputStream(inData.getData()));
            contextStates = (HashMap<String, JAMSEntityCollection>) objIn.readObject();

            objIn.close();
        } catch (Exception e) {
            this.getRuntime().sendErrorMsg("Unable to deserialize jamsentity collection, because" + e.toString());
        }
        RestoreEntityCollections(this.getModel(), contextStates);
    }
}
