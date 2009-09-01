/*
 * JAMSExplorer.java
 * Created on 18. November 2008, 21:37
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
package reg;

import reg.gui.ExplorerFrame;
import jams.JAMS;
import jams.JAMSProperties;
import jams.JAMSTools;
import jams.gui.GUIHelper;
import jams.runtime.JAMSRuntime;
import jams.runtime.StandardRuntime;
import jams.workspace.JAMSWorkspace;
import java.io.File;
import java.io.IOException;
import java.util.Observable;
import java.util.Observer;
import javax.swing.UIManager;
//import reg.viewer.Viewer;

/**
 *
 * @author Sven Kralisch <sven.kralisch at uni-jena.de>
 */
public class JAMSExplorer {

    public static final String APP_TITLE = "Data Explorer";

    public static final String APP_VERSION = "V0.2";

    public static boolean GEOWIND_ENABLE = false;

    public static final int SCREEN_WIDTH = 1200, SCREEN_HEIGHT = 750;

    private ExplorerFrame explorerFrame;

    private JAMSRuntime runtime;

    private JAMSProperties properties;

    private DisplayManager displayManager;

    private JAMSWorkspace workspace;

    public JAMSExplorer() {
        this(null);
    }

    public JAMSExplorer(JAMSRuntime runtime) {

        if (runtime == null) {
            this.runtime = new StandardRuntime();
            this.runtime.setDebugLevel(JAMS.VERBOSE);
            this.runtime.addErrorLogObserver(new Observer() {

                public void update(Observable o, Object arg) {
                    GUIHelper.showErrorDlg(explorerFrame, arg.toString(), JAMS.resources.getString("Error"));
                }
            });
            this.runtime.addInfoLogObserver(new Observer() {

                public void update(Observable o, Object arg) {
                    //GUIHelper.showInfoDlg(regFrame, arg.toString(), JAMS.resources.getString("Info"));
                }
            });
        } else {
            this.runtime = runtime;
        }

        properties = JAMSProperties.createJAMSProperties();
        String defaultFile = System.getProperty("user.dir") + System.getProperty("file.separator") + JAMS.DEFAULT_PARAMETER_FILENAME;
        File file = new File(defaultFile);
        if (file.exists()) {
            try {
                properties.load(defaultFile);
            } catch (IOException ioe) {
                ioe.printStackTrace();
            }
        }

        displayManager = new DisplayManager(this);
        explorerFrame = new ExplorerFrame(this);

    }

    public void open(JAMSWorkspace workspace) {
        this.workspace = workspace;
        explorerFrame.update();
    }

    public static void main(String[] args) {

        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            if (GEOWIND_ENABLE) {
                //Viewer.getViewer();
            }
        } catch (Exception evt) {
        }

        // create the JAMSExplorer object
        JAMSExplorer explorer = new JAMSExplorer();

        if (args.length > 0) {

            try {
                String[] libs = JAMSTools.toArray(explorer.getProperties().getProperty("libs", ""), ";");
                JAMSWorkspace workspace = new JAMSWorkspace(new File(args[0]), explorer.getRuntime(), true);
                workspace.setLibs(libs);

                explorer.open(workspace);

            } catch (JAMSWorkspace.InvalidWorkspaceException iwe) {
                explorer.getRuntime().sendHalt(iwe.getMessage());
            }
        }


        explorer.getExplorerFrame().setVisible(true);
    }

    /**
     * @return the runtime
     */
    public JAMSRuntime getRuntime() {
        return runtime;
    }

    /**
     * @return the properties
     */
    public JAMSProperties getProperties() {
        return properties;
    }

    /**
     * @return the regFrame
     */
    public ExplorerFrame getExplorerFrame() {
        return explorerFrame;
    }

    /**
     * @return the displayManager
     */
    public DisplayManager getDisplayManager() {
        return displayManager;
    }

    /**
     * @return the workspace
     */
    public JAMSWorkspace getWorkspace() {
        return workspace;
    }
}
