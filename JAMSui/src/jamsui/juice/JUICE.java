/*
 * JUICE.java
 * Created on 4. April 2006, 14:44
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
package jamsui.juice;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.UIManager;
import jams.JAMS;
import jams.JAMSProperties;
import jams.tools.JAMSTools;
import jams.gui.tools.GUIHelper;
import jams.gui.WorkerDlg;
import jams.runtime.JAMSClassLoader;
import jams.runtime.StandardRuntime;
import jams.tools.StringTools;
import jamsui.juice.gui.JUICEFrame;
import jamsui.juice.gui.ModelView;
import jamsui.juice.gui.tree.LibTree;
import jamsui.cmdline.JAMSCmdLine;
import javax.swing.SwingUtilities;

/**
 *
 * @author S. Kralisch
 */
public class JUICE {

    public static final String APP_TITLE = "JUICE";
    public static final Class[] JAMS_DATA_TYPES = getJAMSDataClasses();
    public static final int SCREEN_WIDTH = 1200;
    public static final int SCREEN_HEIGHT = 850;
    private static JUICEFrame juiceFrame;
    private static JAMSProperties jamsProperties = JAMSProperties.createProperties();
    private static File baseDir = null;
    private static ArrayList<ModelView> modelViews = new ArrayList<ModelView>();
    private static ClassLoader loader;
    private static JAMSCmdLine cmdLine;
    private static LibTree libTree;
    private static WorkerDlg loadLibsDlg;

    public static void main(String args[]) {

        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            //UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
        } catch (Exception lnfe) {
            try {
                UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
            } catch (Exception ex) {
                JAMSTools.handle(ex);
            }
        }

        cmdLine = new JAMSCmdLine(args, JUICE.APP_TITLE);

        try {

            //try to load property values from file
            if (cmdLine.getConfigFileName() != null) {
                //check for file provided at command line
                getJamsProperties().load(cmdLine.getConfigFileName());
                baseDir = new File(cmdLine.getConfigFileName()).getParentFile();
            } else {
                //check for default file
                String defaultFile = System.getProperty("user.dir") + System.getProperty("file.separator") + JAMS.DEFAULT_PARAMETER_FILENAME;
                baseDir = new File(System.getProperty("user.dir"));
                File file = new File(defaultFile);
                if (file.exists()) {
                    getJamsProperties().load(defaultFile);
                }
            }

            JAMSTools.configureLocaleEncoding(getJamsProperties());

            createJUICEFrame();

            if (cmdLine.getModelFileName() != null) {
                juiceFrame.loadModel(cmdLine.getModelFileName());
            }

        } catch (Throwable t) {

            //if something goes wrong that has not been handled until now, catch it here
            String s = "";
            StackTraceElement[] st = t.getStackTrace();
            for (StackTraceElement ste : st) {
                s += "        at " + ste.toString() + "\n";
            }
            System.out.println(JAMS.resources.getString("JUICE_Error"));
            GUIHelper.showErrorDlg(JUICE.getJuiceFrame(), JAMS.resources.getString("An_error_occured_during_JUICE_execution") + t.toString() + "\n" + s, JAMS.resources.getString("JUICE_Error"));
        }
    }

    public static void createJUICEFrame() {
        juiceFrame = new JUICEFrame();

        juiceFrame.setVisible(true);

        libTree = new LibTree(new ComponentCollection());

        JUICE.updateLibs();

        juiceFrame.setLibTree(libTree);

        getJamsProperties().addObserver(JAMSProperties.LIBS_IDENTIFIER, new Observer() {

            public void update(Observable obs, Object obj) {
                JUICE.updateLibs();
            }
        });
    }

    public static void updateLibs() {
        if (loadLibsDlg == null) {
            loadLibsDlg = new WorkerDlg(juiceFrame, JAMS.resources.getString("Loading_Libraries"));
        }
        try {
            loadLibsDlg.setTask(new Runnable() {

                public void run() {
                    JUICE.getJuiceFrame().getLibTreePanel().setEnabled(false);
                    JUICE.createClassLoader();
                    getLibTree().update(JUICE.getJamsProperties().getProperty(JAMSProperties.LIBS_IDENTIFIER));
                    JUICE.getJuiceFrame().getLibTreePanel().setEnabled(true);
                }
            });
            loadLibsDlg.execute();
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }

    private static void createClassLoader() {
        String libs = getJamsProperties().getProperty(JAMSProperties.LIBS_IDENTIFIER);

        String[] libsArray = StringTools.toArray(libs, ";");

        JUICE.loader = JAMSClassLoader.createClassLoader(libsArray, new StandardRuntime());
    }

    private static Class[] getJAMSDataClasses() {
        ArrayList<Class> classes = new ArrayList<Class>();
        classes.add(jams.data.Attribute.Boolean.class);
        classes.add(jams.data.Attribute.Calendar.class);
        classes.add(jams.data.Attribute.Double.class);
        classes.add(jams.data.Attribute.DirName.class);
        classes.add(jams.data.Attribute.Entity.class);
        classes.add(jams.data.Attribute.Float.class);
        classes.add(jams.data.Attribute.FileName.class);
        classes.add(jams.data.Attribute.Geometry.class);
        classes.add(jams.data.Attribute.Integer.class);
        classes.add(jams.data.Attribute.Long.class);
        classes.add(jams.data.Attribute.String.class);
        classes.add(jams.data.Attribute.BooleanArray.class);
        classes.add(jams.data.Attribute.DoubleArray.class);
        classes.add(jams.data.Attribute.FloatArray.class);
        classes.add(jams.data.Attribute.IntegerArray.class);
        classes.add(jams.data.Attribute.LongArray.class);
        classes.add(jams.data.Attribute.StringArray.class);
        classes.add(jams.data.Attribute.TimeInterval.class);

        Class[] classesA = new Class[classes.size()];
        classes.toArray(classesA);
        return classesA;
    }

    public static JAMSProperties getJamsProperties() {
        return JUICE.jamsProperties;
    }

    public static void setJamsProperties(JAMSProperties jamsProperties) {
        JUICE.jamsProperties = jamsProperties;
    }

    public static File getBaseDir() {
        return baseDir;
    }

    public static ArrayList<ModelView> getModelViews() {
        return modelViews;
    }

    public static JUICEFrame getJuiceFrame() {
        return juiceFrame;
    }

    public static ClassLoader getLoader() {
        return loader;
    }

    public static void setStatusText(String status) {
        JUICE.getJuiceFrame().getStatusLabel().setText(status);
    }

    /**
     * @return the libTree
     */
    public static LibTree getLibTree() {
        return libTree;
    }
}
