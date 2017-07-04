/*
 * GUIState.java
 * Created on 28.10.2016, 17:20:00
 *
 * This file is part of JAMS
 * Copyright (C) FSU Jena
 *
 * JAMS is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public License
 * as published by the Free Software Foundation; either version 3
 * of the License, or (at your option) any later version.
 *
 * JAMS is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with JAMS. If not, see <http://www.gnu.org/licenses/>.
 *
 */
package jams.gui.tools;

import jams.meta.ModelDescriptor;
import jams.workspace.Workspace;
import java.awt.Window;
import java.io.File;
import java.nio.file.Paths;

/**
 *
 * @author Sven Kralisch <sven.kralisch at uni-jena.de>
 */
public class GUIState {

    private static Workspace workspace;
    private static File savePath = Paths.get("").toFile();
    private static ModelDescriptor modelDescriptor;
    private static Window mainWindow;

    /**
     * @return the workspace
     */
    public static Workspace getWorkspace() {
        return GUIState.workspace;
    }

    /**
     * @param ws the workspace to set
     */
    public static void setWorkspace(Workspace ws) {
        GUIState.workspace = ws;
    }

    /**
     * @return the savePath
     */
    public static File getSavePath() {
        return GUIState.savePath;
    }

    /**
     * @param savePath the savePath to set
     */
    public static void setSavePath(File savePath) {
        GUIState.savePath = savePath;
    }

    /**
     * @return the modelDescriptor
     */
    public static ModelDescriptor getModelDescriptor() {
        return modelDescriptor;
    }

    /**
     * @param modelDescriptor the modelDescriptor to set
     */
    public static void setModelDescriptor(ModelDescriptor modelDescriptor) {
        GUIState.modelDescriptor = modelDescriptor;
    }

    /**
     * @return the mainWindow
     */
    public static Window getMainWindow() {
        return mainWindow;
    }

    /**
     * @param aMainWindow the mainWindow to set
     */
    public static void setMainWindow(Window aMainWindow) {
        mainWindow = aMainWindow;
    }

}
