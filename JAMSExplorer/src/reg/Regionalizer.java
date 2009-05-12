/*
 * Regionalizer.java
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

import reg.gui.RegionalizerFrame;
import jams.JAMS;
import jams.JAMSProperties;
import jams.JAMSTools;
import jams.gui.LHelper;
import jams.runtime.JAMSRuntime;
import jams.runtime.StandardRuntime;
import jams.workspace.JAMSWorkspace;
import java.io.File;
import java.io.IOException;
import java.util.Observable;
import java.util.Observer;
import javax.swing.JPanel;
import javax.swing.UIManager;

/**
 *
 * @author Sven Kralisch <sven.kralisch at uni-jena.de>
 */
public class Regionalizer {

    public static final String APP_TITLE = "DataReg";

    public static final int SCREEN_WIDTH = 1200,  SCREEN_HEIGHT = 750;

    private RegionalizerFrame regionalizerFrame;

    private JAMSRuntime runtime;

    private JAMSProperties properties;

    private DisplayManager displayManager;

    private JAMSWorkspace workspace;

    public Regionalizer(File path) {
        this(null, path);
    }

    public Regionalizer(JAMSRuntime runtime, File path) {

        if (runtime == null) {
            this.runtime = new StandardRuntime();
            this.runtime.setDebugLevel(JAMS.VERBOSE);
            this.runtime.addErrorLogObserver(new Observer() {

                public void update(Observable o, Object arg) {
                    LHelper.showErrorDlg(regionalizerFrame, arg.toString(), JAMS.resources.getString("Error"));
                }
            });
            this.runtime.addInfoLogObserver(new Observer() {

                public void update(Observable o, Object arg) {
                    //LHelper.showInfoDlg(regFrame, arg.toString(), JAMS.resources.getString("Info"));
                }
            });
        } else {
            this.runtime = runtime;
        }

        properties = JAMSProperties.createJAMSProperties();
        String defaultFile = System.getProperty("user.dir") + System.getProperty("file.separator") + JAMS.DEFAULT_PARAMETER_FILENAME;
        System.out.println(defaultFile);
        File file = new File(defaultFile);
        if (file.exists()) {
            try {
                properties.load(defaultFile);
            } catch (IOException ioe) {
                ioe.printStackTrace();
            }
        }

        displayManager = new DisplayManager(this);
        regionalizerFrame = new RegionalizerFrame(this);

        if (path != null) {
            open(path);
        }

    }

    public void open(File workspaceFile) {

        try {
            String[] libs = JAMSTools.toArray(this.getProperties().getProperty("libs", ""), ";");
            workspace = new JAMSWorkspace(workspaceFile, this.getRuntime(), true);
            workspace.setLibs(libs);
            regionalizerFrame.setTitle(Regionalizer.APP_TITLE + " [" + workspace.getDirectory().toString() + "]");
            this.getDisplayManager().getTreePanel().update(workspace);
            regionalizerFrame.updateMainPanel(new JPanel());
        } catch (JAMSWorkspace.InvalidWorkspaceException iwe) {
            this.getRuntime().sendHalt(iwe.getMessage());
        }

    }

    public static void main(String[] args) {
        try {
            if (System.getProperty("os.name").contains("Windows")) {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            }
        } catch (Exception evt) {
        }
        Regionalizer reg = new Regionalizer(new File("D:/jamsapplication/JAMS-Gehlberg"));

        reg.getRegionalizerFrame().setVisible(true);
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
    public RegionalizerFrame getRegionalizerFrame() {
        return regionalizerFrame;
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
