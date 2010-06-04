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

import jams.JAMS;
import jams.data.Attribute;
import jams.data.JAMSDataFactory;
import jams.workspace.JAMSWorkspace;
import jams.workspace.stores.OutputDataStore;
import java.io.File;
import java.util.HashMap;
import jams.runtime.JAMSRuntime;
import jams.tools.StringTools;
import jams.workspace.InvalidWorkspaceException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.Field;
import java.util.ArrayList;

/**
 *
 * @author S. Kralisch
 */
@JAMSComponentDescription(title = "JAMS model",
author = "Sven Kralisch",
date = "26. September 2005",
description = "This component represents a JAMS model which is a special type of context component")
public class JAMSModel extends JAMSContext implements Model {

    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ)
    public Attribute.DirName workspaceDirectory = JAMSDataFactory.createDirName();
    private JAMSRuntime runtime;
    private String name, author, date;
    public JAMSWorkspace workspace;
    transient private HashMap<Component, ArrayList<Field>> nullFields;
    private HashMap<Component, Long> execTime = new HashMap<Component, Long>();
    private boolean profiling = false;

    public JAMSModel(JAMSRuntime runtime) {
        this.runtime = runtime;
    }

    public JAMSRuntime getRuntime() {
        return runtime;
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

        runtime.println("", JAMS.STANDARD);
        runtime.println(JAMS.resources.getString("starting_simulation"), JAMS.STANDARD);
        runtime.println(JAMS.resources.getString("*************************************"), JAMS.STANDARD);

        // check if workspace directory was specified
        if (workspaceDirectory.getValue() == null) {
            runtime.sendHalt(JAMS.resources.getString("No_workspace_directory_specified,_stopping_execution!"));
            return;
        }

        // save current model parameter to workspace output directory
        getRuntime().saveModelParameter();

        super.init();

        if (!doRun) {
            return;
        }

        if (!getNullFields().isEmpty() && (runtime.getDebugLevel() >= JAMS.VVERBOSE)) {
            runtime.println("");
            runtime.println(JAMS.resources.getString("UNDEFINED_FIELDS"));
            runtime.println(JAMS.resources.getString("*************************************"));
            for (Component comp : getNullFields().keySet()) {
                ArrayList<Field> nf = getNullFields().get(comp);
                if (nf.isEmpty()) {
                    continue;
                }
                String str = "  " + comp.getInstanceName() + ": ";
                for (Field field : nf) {
                    str += field.getName() + " ";
                }
                getRuntime().println(str);
            }
            runtime.println(JAMS.resources.getString("*************************************"));
        }
        setupDataTracer();
    }

    @Override
    public void cleanup() {
        super.cleanup();

        if (profiling) {
            printExecTimes();
        }
    }

    private void printExecTimes(Context context, String indent) {

        runtime.println(indent + context.getInstanceName() + "\t" + execTime.get(context));

        for (Component c : context.getComponents()) {

            if (c instanceof Context) {
                printExecTimes((Context) c, indent + "\t");
            } else {
                runtime.println(indent + "\t" + c.getInstanceName() + "\t" + execTime.get(c));
            }

        }
    }

    private void printExecTimes() {

        runtime.println("");
        runtime.println("JAMS profiler results");
        runtime.println(JAMS.resources.getString("*************************************"));
        for (Component c : this.getComponents()) {

            if (c instanceof Context) {
                printExecTimes((Context) c, "");
            } else {
                runtime.println(c.getInstanceName() + "\t" + execTime.get(c));
            }

        }
    }

    public boolean moveWorkspaceDirectory(String workspaceDirectory) {
        setWorkspacePath(workspaceDirectory);
        // create output dir
        try {
            this.workspace.init();
            this.workspace.checkValidity(false);
        } catch (InvalidWorkspaceException e) {
            getRuntime().sendHalt("Error during model setup: \""
                    + this.workspace.getDirectory().getAbsolutePath() + "\" is not a valid datastore, because: " + e.toString());
            return false;
        }
        // reanimate data tracers
        setupDataTracer();
        this.components.size();
        return true;
    }

    public void setWorkspacePath(String workspacePath) {
        this.workspaceDirectory.setValue(workspacePath);
        if (StringTools.isEmptyString(workspacePath)) {
            this.workspace = null;
            getRuntime().sendInfoMsg(JAMS.resources.getString("no_workspace_defined"));
        } else {
            this.workspace = new JAMSWorkspace(new File(workspacePath), getRuntime());
        }
    }

    public JAMSWorkspace getWorkspace() {
        return this.workspace;
    }

    public File getWorkspaceDirectory() {
        if (workspace == null) {
            return null;
        } else {
            return workspace.getDirectory();
        }
    }

    public String getWorkspacePath() {
        return workspaceDirectory.getValue();
    }

    public OutputDataStore[] getOutputDataStores(String contextName) {
        if (this.workspace == null) {
            return new OutputDataStore[0];
        }
        return this.workspace.getOutputDataStores(contextName);
    }

    public HashMap<Component, ArrayList<Field>> getNullFields() {
        return nullFields;
    }

    public void setNullFields(HashMap<Component, ArrayList<Field>> nullFields) {
        this.nullFields = nullFields;
    }

    public void measureTime(long startTime, Component c) {
        long time = System.currentTimeMillis() - startTime;
        Long current = execTime.get(c);
        if (current == null) {
            current = new Long(0);
        }
        current += time;
        execTime.put(c, current);
    }

    /**
     * @return True, if the model is in profiling mode, false otherwise
     */
    public boolean isProfiling() {
        return profiling;
    }

    /**
     * Set static attribute profile which defines profiling for contexts
     * @param profile Profile or not
     */
    public void setProfiling(boolean profiling) {
        this.profiling = profiling;
    }

    private void readObject(ObjectInputStream objIn) throws IOException, ClassNotFoundException {
        objIn.defaultReadObject();

        this.getRuntime().initGUI((String)objIn.readObject(),
                objIn.readBoolean(),
                objIn.readInt(),objIn.readInt());
    }

    private void writeObject(ObjectOutputStream objOut) throws IOException {
        objOut.defaultWriteObject();
        objOut.writeObject(this.getRuntime().getFrame().getTitle());
        objOut.writeBoolean(this.getRuntime().getFrame().isAlwaysOnTop());
        objOut.writeInt(this.getRuntime().getFrame().getWidth());
        objOut.writeInt(this.getRuntime().getFrame().getHeight());
    }
}
