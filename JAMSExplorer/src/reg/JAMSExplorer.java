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

import java.awt.event.WindowEvent;
import reg.gui.ExplorerFrame;
import jams.JAMS;
import jams.JAMSProperties;
import jams.SystemProperties;
import jams.gui.tools.GUIHelper;
import jams.runtime.JAMSRuntime;
import jams.runtime.StandardRuntime;
import jams.workspace.JAMSWorkspace;
import java.awt.Window;
import java.awt.event.WindowListener;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Observable;
import java.util.Observer;
import javax.swing.UIManager;
import reg.viewer.Viewer;

/**
 *
 * @author Sven Kralisch <sven.kralisch at uni-jena.de>
 */
public class JAMSExplorer {

    public static final String APP_TITLE = java.util.ResourceBundle.getBundle("reg/resources/JADEBundle").getString("DATA_EXPLORER");

    public static final String APP_VERSION = "V0.2";

    public static final int SCREEN_WIDTH = 1200, SCREEN_HEIGHT = 750;

    private ExplorerFrame explorerFrame;

    private JAMSRuntime runtime;

    private SystemProperties properties;

    private DisplayManager displayManager;

    private JAMSWorkspace workspace;

    private ArrayList<Window> childWindows = new ArrayList<Window>();

    private boolean standAlone, tlugized;

    public JAMSExplorer(JAMSRuntime runtime) {
        this(runtime, true, true);
    }

    public JAMSExplorer(JAMSRuntime runtime, boolean standAlone, boolean tlugized) {

        this.standAlone = standAlone;
        this.tlugized = tlugized;

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

        properties = JAMSProperties.createProperties();
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

    public static void main(String[] args) {

        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception evt) {
        }

        // create the JAMSExplorer object
        JAMSExplorer explorer = new JAMSExplorer(null,true,true);
        explorer.getExplorerFrame().setVisible(true);
        if (explorer.tlugized) {
            Viewer.getViewer();
        }

        if (args.length > 0) {
            explorer.getExplorerFrame().open(new File(args[0]));
        }
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
    public SystemProperties getProperties() {
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

    public void setWorkspace(JAMSWorkspace workspace) {
        this.workspace = workspace;
    }

    public void registerChild(Window window) {

        synchronized (this) {
            // add the window to the list
            this.childWindows.add(window);

            // make sure the window is removed from the list once it has been closed
            window.addWindowListener(new WindowListener() {

                public void windowOpened(WindowEvent e) {
                }

                public void windowClosing(WindowEvent e) {
                }

                public void windowClosed(WindowEvent e) {
                    JAMSExplorer.this.getChildWindows().remove(e.getWindow());
                }

                public void windowIconified(WindowEvent e) {
                }

                public void windowDeiconified(WindowEvent e) {
                }

                public void windowActivated(WindowEvent e) {
                }

                public void windowDeactivated(WindowEvent e) {
                }
            });
        }
    }

    /**
     * @return the childWindows
     */
    public ArrayList<Window> getChildWindows() {
        return childWindows;
    }

    public void exit() {
        if (standAlone) {
            System.exit(0);
        }
    }

    /**
     * @return the tlugized
     */
    public boolean isTlugized() {
        return tlugized;
    }
}
