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
package org.unijena.juice;

import java.io.File;
import java.util.*;
import javax.swing.UIManager;
import jams.JAMS;
import jams.JAMSProperties;
import jams.JAMSTools;
import jams.gui.JAMSSplash;
import jams.gui.LHelper;
import jams.gui.WorkerDlg;
import jams.runtime.JAMSClassLoader;
import jams.runtime.StandardRuntime;
import org.unijena.juice.gui.JUICEFrame;
import org.unijena.juice.gui.ModelView;
import org.unijena.juice.gui.tree.LibTree;

/**
 *
 * @author S. Kralisch
 */
public class JUICE {

    public static final Class[] JAMS_DATA_TYPES = getJAMSDataClasses();
    public static final int SCREEN_WIDTH = 1200;
    public static final int SCREEN_HEIGHT = 850;
    public static final String APP_TITLE = "JUICE";
    private static JUICEFrame juiceFrame;
    private static JAMSProperties jamsProperties = JAMSProperties.createJAMSProperties();
    private static File baseDir = null;
    private static ArrayList<ModelView> modelViews = new ArrayList<ModelView>();
    private static ClassLoader loader;
    private static JUICECmdLine cmdLine;
    private static LibTree libTree;
    private static WorkerDlg loadLibsDlg;

    public static void main(String args[]) throws Exception {

        cmdLine = new JUICECmdLine(args);

        try {
            if (System.getProperty("os.name").contains("Windows")) {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            }/* else {
        PlasticLookAndFeel laf = new PlasticXPLookAndFeel();
        PlasticLookAndFeel.setPlasticTheme(new com.jgoodies.looks.plastic.theme.ExperienceBlue());
        Options.setPopupDropShadowEnabled(true);
        UIManager.setLookAndFeel(laf);
        }*/
        } catch (Exception evt) {
        }
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

            juiceFrame = new JUICEFrame();

            loadLibsDlg = new WorkerDlg(juiceFrame, "Loading Libraries");

            JAMSSplash splash = new JAMSSplash();
            splash.show(juiceFrame, JAMS.SPLASH_DISPLAY_TIME);
            Thread.sleep(JAMS.SPLASH_DISPLAY_TIME);
            //juiceFrame.setVisible(true);

            libTree = new LibTree();
            JUICE.updateLibs();
            juiceFrame.setLibTree(libTree);

            if (cmdLine.getModelFileName() != null) {
                juiceFrame.loadModel(cmdLine.getModelFileName());
            }

            getJamsProperties().addObserver(JAMSProperties.LIBS_IDENTIFIER, new Observer() {

                public void update(Observable obs, Object obj) {
                    JUICE.updateLibs();
                }
            });

        } catch (Exception e) {

            //if something goes wrong that has not been handled until now, catch it here
            String s = "";
            StackTraceElement[] st = e.getStackTrace();
            for (StackTraceElement ste : st) {
                s += "        at " + ste.toString() + "\n";
            }
            LHelper.showErrorDlg(JUICE.getJuiceFrame(), "An error occured during JUICE execution:\n" + e.toString() + "\n" + s, "JUICE Error");
        //            JUICE.getJuiceFrame().setCursor(new Cursor(Cursor.DEFAULT_CURSOR));

        }
    }

    public static void updateLibs() {
        loadLibsDlg.setTask(new Runnable() {

            public void run() {
                try {
                    JUICE.getJuiceFrame().getLibTreePanel().setEnabled(false);
                    JUICE.createClassLoader();
                    libTree.update(JUICE.getJamsProperties().getProperty(JAMSProperties.LIBS_IDENTIFIER));
                    JUICE.getJuiceFrame().getLibTreePanel().setEnabled(true);
                } catch (Exception e) {
                }
            }
        });
        loadLibsDlg.execute();
    }

    private static void createClassLoader() {
        String libs = getJamsProperties().getProperty(JAMSProperties.LIBS_IDENTIFIER);

        String[] libsArray = JAMSTools.toArray(libs, ";");

        JUICE.loader = JAMSClassLoader.createClassLoader(libsArray, new StandardRuntime());

    /*
     * This is the version the runtime is also using.
     * Disadvantage: changes to classes during JUICE runtime do not become visible since
     * older classes are not beeing overwritten
     *
    try {
    ClassManager.addLibs(libsArray, rt);
    JUICE.loader = Thread.currentThread().getContextClassLoader();
    } catch (IOException ex) {
    ex.printStackTrace();
    }
     *
     *
    
    if (rt.getErrorLog().length()>0) {
    System.out.println(rt.getErrorLog());
    }
    if (rt.getInfoLog().length()>0) {
    System.out.println(rt.getInfoLog());
    }
     *
     */

    }

    public static Class[] getJAMSDataClasses() {
        ArrayList<Class> classes = new ArrayList<Class>();
        try {
            classes.add(Class.forName("jams.data.JAMSBoolean"));
            classes.add(Class.forName("jams.data.JAMSCalendar"));
            classes.add(Class.forName("jams.data.JAMSDouble"));
            classes.add(Class.forName("jams.data.JAMSDirName"));
            classes.add(Class.forName("jams.data.JAMSEntity"));
            classes.add(Class.forName("jams.data.JAMSFloat"));
            classes.add(Class.forName("jams.data.JAMSFileName"));
            classes.add(Class.forName("jams.data.JAMSGeometry"));
            classes.add(Class.forName("jams.data.JAMSInteger"));
            classes.add(Class.forName("jams.data.JAMSLong"));
            classes.add(Class.forName("jams.data.JAMSString"));
            classes.add(Class.forName("jams.data.JAMSBooleanArray"));
            classes.add(Class.forName("jams.data.JAMSDoubleArray"));
            classes.add(Class.forName("jams.data.JAMSFloatArray"));
            classes.add(Class.forName("jams.data.JAMSIntegerArray"));
            classes.add(Class.forName("jams.data.JAMSLongArray"));
            classes.add(Class.forName("jams.data.JAMSStringArray"));
            classes.add(Class.forName("jams.data.JAMSTimeInterval"));
        } catch (ClassNotFoundException ex) {
            ex.printStackTrace();
        }

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
}
