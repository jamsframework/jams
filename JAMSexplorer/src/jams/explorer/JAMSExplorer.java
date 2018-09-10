/*
 * JAMSExplorer.java
 * Created on 18. November 2008, 21:37
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
package jams.explorer;

import jams.workspace.InvalidWorkspaceException;
import java.awt.event.WindowEvent;
import java.lang.reflect.InvocationTargetException;
import java.util.logging.Level;
import java.util.logging.Logger;
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
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import jams.explorer.gui.ExplorerFrame;

/**
 *
 * @author Sven Kralisch <sven.kralisch at uni-jena.de>
 */
public class JAMSExplorer {

    public static final String APP_TITLE = JAMS.i18n("JADE");
    public static final int SCREEN_WIDTH = 1200, SCREEN_HEIGHT = 760;
    protected ExplorerFrame explorerFrame;
    protected JAMSRuntime runtime;
    protected SystemProperties properties;
    protected DisplayManager displayManager;
    protected JAMSWorkspace workspace;
    protected ArrayList<Window> childWindows = new ArrayList<Window>();
    private boolean standAlone;
    protected static JAMSExplorer explorer;

    public JAMSExplorer(JAMSRuntime runtime) {
        this(runtime, true);
    }

    public JAMSExplorer(JAMSRuntime runtime, boolean standAlone) {

        this.standAlone = standAlone;

        properties = JAMSProperties.createProperties();
        File defaultFile = new File(JAMS.getBaseDir(), JAMS.DEFAULT_PROPERTY_FILENAME);
        if (defaultFile.exists() && standAlone) {
            try {
                properties.load(defaultFile.getAbsolutePath());
            } catch (IOException ioe) {
                Logger.getLogger(JAMSExplorer.class.getName()).log(Level.SEVERE, ioe.getMessage(), ioe);
            }
        }

        if (runtime == null) {
            this.runtime = new StandardRuntime(properties);
            this.runtime.setDebugLevel(JAMS.VERBOSE);
            this.runtime.addErrorLogObserver(new Observer() {
                public void update(Observable o, Object arg) {
                    GUIHelper.showErrorDlg(explorerFrame, arg.toString(), JAMS.i18n("Error"));
                }
            });
            this.runtime.addInfoLogObserver(new Observer() {
                public void update(Observable o, Object arg) {
                    //GUIHelper.showInfoDlg(regFrame, arg.toString(), JAMS.i18n("Info"));
                }
            });
        } else {
            this.runtime = runtime;
        }

        displayManager = new DisplayManager(this);
        explorerFrame = new ExplorerFrame(JAMSExplorer.this);

    }

    public static void main(String[] args) {

        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception evt) {
        }

        try {
            SwingUtilities.invokeAndWait(new Runnable() {
                public void run() {
                    // create the JAMSExplorer object
                    explorer = new JAMSExplorer(null, true);
                    /*try {
                        explorer.explorerFrame.open(new File("D:/jamsmodeldata/J2K_Yzeron/j2k_yzeron_h"));
                    } catch (InvalidWorkspaceException ex) {
                        Logger.getLogger(JAMSExplorer.class.getName()).log(Level.SEVERE, null, ex);
                    }*/
                }
            });
        } catch (InterruptedException ex) {
            Logger.getLogger(JAMSExplorer.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
        } catch (InvocationTargetException ex) {
            Logger.getLogger(JAMSExplorer.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
        }

        explorer.getExplorerFrame().setVisible(true);

        if (args.length > 0) {
            try {
                explorer.getExplorerFrame().open(new File(args[0]));
            } catch (InvalidWorkspaceException ex) {
                Logger.getLogger(JAMSExplorer.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
            }
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
        if (isStandAlone()) {
            System.exit(0);
        }
    }

    /**
     * @return the standAlone
     */
    public boolean isStandAlone() {
        return standAlone;
    }
}
