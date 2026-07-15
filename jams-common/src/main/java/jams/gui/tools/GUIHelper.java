/*
 * GUIHelper.java
 * Created on 19. September 2006, 10:11
 *
 * This file is part of JAMS
 * Copyright (C) 2005 FSU Jena
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
package jams.gui.tools;

import java.awt.Component;
import java.awt.Container;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import javax.swing.JOptionPane;
import jams.JAMS;
import jams.SystemProperties;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.Window;
import java.io.File;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JMenuItem;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileSystemView;

/**
 *
 * @author S. Kralisch
 *
 */
public class GUIHelper {

    public static final int NO_OPTION = JOptionPane.NO_OPTION;
    public static final int YES_OPTION = JOptionPane.YES_OPTION;
    public static final int CANCEL_OPTION = JOptionPane.CANCEL_OPTION;

    /**
     * Remove swing component from container
     *
     * @param cont Container object
     * @param c Component object
     */
    public static void removeGBComponent(Container cont, Component c) {
        cont.remove(c);
    }
    
    /**
     * Add swing component to container using gridbag layout
     *
     * @param cont Container object
     * @param gbl Gridbag layout object
     * @param c Component to be added
     * @param x X position
     * @param y Y position
     * @param width Component width
     * @param height Component height
     * @param weightx Component weight in x direction
     * @param weighty Component weight in y direction
     * @return Component added
     */
    public static Component addGBComponent(Container cont, GridBagLayout gbl, Component c,
            int x, int y, int width, int height,
            double weightx, double weighty) {
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.BOTH;
        gbc.insets = new Insets(2, 2, 2, 2);
        gbc.gridx = x;
        gbc.gridy = y;
        gbc.gridwidth = width;
        gbc.gridheight = height;
        gbc.weightx = weightx;
        gbc.weighty = weighty;
        gbl.setConstraints(c, gbc);
        return cont.add(c);
    }    

    /**
     * Add swing component to container using gridbag layout
     *
     * @param cont Container object
     * @param gbl Gridbag layout object
     * @param c Component to be added
     * @param x X position
     * @param y Y position
     * @param width Component width
     * @param height Component height
     * @param weightx Component weight in x direction
     * @param weighty Component weight in y direction
     * @param filling Filling strategy 
     * @return Component added
     */
    public static Component addGBComponent(Container cont, GridBagLayout gbl, Component c,
            int x, int y, int width, int height,
            double weightx, double weighty, int filling) {
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = filling;
        gbc.insets = new Insets(2, 2, 2, 2);
        gbc.gridx = x;
        gbc.gridy = y;
        gbc.gridwidth = width;
        gbc.gridheight = height;
        gbc.weightx = weightx;
        gbc.weighty = weighty;
        gbl.setConstraints(c, gbc);
        return cont.add(c);
    }

    /**
     * Add swing component to container using gridbag layout
     *
     * @param cont Container object
     * @param gbl Gridbag layout object
     * @param c Component to be added
     * @param x X position
     * @param y Y position
     * @param width Component width
     * @param height Component height
     * @param topInset Top inset
     * @param leftInset Left inset
     * @param bottomInset Bottom inset
     * @param rightInset Right inset
     * @param weightx Component weight in x direction
     * @param weighty Component weight in y direction
     * @return Component added
     */
    public static Component addGBComponent(Container cont, GridBagLayout gbl, Component c,
            int x, int y, int width, int height,
            int topInset, int leftInset, int bottomInset, int rightInset,
            double weightx, double weighty) {
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.BOTH;
        gbc.insets = new Insets(topInset, leftInset, bottomInset, rightInset);
        gbc.gridx = x;
        gbc.gridy = y;
        gbc.gridwidth = width;
        gbc.gridheight = height;
        gbc.weightx = weightx;
        gbc.weighty = weighty;
        gbl.setConstraints(c, gbc);
        return cont.add(c);
    }

    /**
     * Add swing component to container using gridbag layout
     *
     * @param cont Container object
     * @param gbl Gridbag layout object
     * @param c Component to be added
     * @param x X position
     * @param y Y position
     * @param width Component width
     * @param height Component height
     * @param weightx Component weight in x direction
     * @param weighty Component weight in y direction
     * @param fill Defines how to resize the component
     * @param anchor Defines where to place component exactly
     * @return Component added
     */
    public static Component addGBComponent(Container cont, GridBagLayout gbl, Component c, int x, int y, int width, int height, double weightx, double weighty, int fill, int anchor) {
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = fill;
        gbc.anchor = anchor;
        gbc.insets = new Insets(2, 2, 2, 2);
        gbc.gridx = x;
        gbc.gridy = y;
        gbc.gridwidth = width;
        gbc.gridheight = height;
        gbc.weightx = weightx;
        gbc.weighty = weighty;
        gbl.setConstraints(c, gbc);
        return cont.add(c);
    }

    /**
     * Show Yes-No-Cancel dialog
     *
     * @param owner The parent component
     * @param message
     * @param title
     * @return
     */
    public static int showYesNoCancelDlg(Component owner, String message, String title) {
        Object[] options = {JAMS.i18n("Yes"), JAMS.i18n("No"), JAMS.i18n("Cancel")};
        int result = JOptionPane.showOptionDialog(owner, message, title,
                JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE, null, options, options[0]);

        return result;
    }

    /**
     * Show Yes-No dialog
     *
     * @param owner The parent component
     * @param message
     * @param title
     * @return
     */
    public static int showYesNoDlg(Component owner, String message, String title) {
        Object[] options = {JAMS.i18n("Yes"), JAMS.i18n("No")};
        int result = JOptionPane.showOptionDialog(owner, message, title,
                JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE, null, options, options[0]);
        //int result = JOptionPane.showConfirmDialog(JUICE.getJuiceFrame(), "Delete Attribute \"" + attrName + "\"?", "Confirm", JOptionPane.YES_NO_OPTION);

        return result;
    }

    /**
     * Show info dialog
     *
     * @param owner The parent component
     * @param message
     * @param title
     */
    public static void showInfoDlg(Component owner, String message, String title) {
        JOptionPane.showMessageDialog(owner, message, title, JOptionPane.INFORMATION_MESSAGE);
    }

    /**
     * Show info dialog
     *
     * @param owner The parent component
     * @param message
     */
    public static void showInfoDlg(Component owner, String message) {
        JOptionPane.showMessageDialog(owner, message, JAMS.i18n("INFO"), JOptionPane.INFORMATION_MESSAGE);
    }

    /**
     * Show error dialog
     *
     * @param owner The parent component
     * @param message
     * @param title
     */
    public static void showErrorDlg(Component owner, String message, String title) {
        JOptionPane.showMessageDialog(owner, message, title, JOptionPane.ERROR_MESSAGE);
    }

    /**
     * Show error dialog
     *
     * @param owner The parent component
     * @param message
     */
    public static void showErrorDlg(Component owner, String message) {
        JOptionPane.showMessageDialog(owner, message, JAMS.i18n("ERROR"), JOptionPane.ERROR_MESSAGE);
    }

    /**
     * Show input dialog
     *
     * @param owner The parent component
     * @param message
     * @param initalValue
     * @return
     */
    public static String showInputDlg(Component owner, String message, String initalValue) {
        return JOptionPane.showInputDialog(owner, message, initalValue);
    }

    /**
     * Show input dialog
     *
     * @param owner The parent component
     * @param message The message to be shown
     * @param title The dialog window title
     * @param initalValue Some default value
     * @return
     */
    public static String showInputDlg(Component owner, String message, String title, String initalValue) {
        return (String) JOptionPane.showInputDialog(owner, message, title, JOptionPane.QUESTION_MESSAGE, null, null, initalValue);
    }

    /**
     * Create a new JFileChooser and support disabling of zipped folders
     *
     * @param useShellFolder If true, the native file chooser dialog will be
     * displayed, a JAVA version else
     * @return new JFileChooser
     */
    public static JFileChooser getJFileChooser(boolean useShellFolder) {
        if (useShellFolder) {
            try {
                return new JFileChooser();
            } catch (Throwable t) {
                // catch possible exceptions in sun.swing.WindowsPlacesBar that
                // occur while using strange windows themes (missing icons!)
                return getJFileChooser(false);
            }
        } else {
            return new JFileChooser(new File("."), new RestrictedFileSystemView()) {
                @Override
                protected void setup(FileSystemView view) {
                    putClientProperty("FileChooser.useShellFolder", Boolean.FALSE);
                    super.setup(view);
                }
            };
        }
    }
    private static HashMap<FileFilter, JFileChooser> choosers = new HashMap<FileFilter, JFileChooser>();

    /**
     * Create a new JFileChooser
     *
     * @return new JFileChooser
     */
    public static JFileChooser getJFileChooser(FileFilter filter) {

        JFileChooser jfc = choosers.get(filter);
        if (jfc == null) {
            jfc = getJFileChooser(true);
            jfc.setFileFilter(filter);
            choosers.put(filter, jfc);
        }
        return jfc;
    }

    /**
     * Create a new JFileChooser
     *
     * @return new JFileChooser
     */
    public static JFileChooser getJFileChooser() {
        return getJFileChooser(true);
    }

    /**
     * Open URL in systems default web browser on Windows, OSX and Unix/Linux.
     * For Unix/Linux a number of different browsers are tested. Taken from
     * BrowserLauncher2 project
     * (http://sourceforge.net/projects/browserlaunch2).
     *
     * @param url URL to be opened
     */
    public static void openURL(String url) {

        String osName = System.getProperty("os.name");
        String errMsg = JAMS.i18n("Error_attempting_to_launch_web_browser");

        try {
            if (osName.startsWith("Mac OS")) {
                Class<?> fileMgr = Class.forName("com.apple.eio.FileManager");
                Method openURL = fileMgr.getDeclaredMethod("openURL",
                        new Class[]{String.class});
                openURL.invoke(null, new Object[]{url});
            } else if (osName.startsWith("Windows")) {
                Runtime.getRuntime().exec("rundll32 url.dll,FileProtocolHandler " + url);
            } else { //assume Unix or Linux
                String[] browsers = {
                    "firefox", "opera", "konqueror", "epiphany", "mozilla", "netscape"
                };
                String browser = null;
                for (int count = 0; count < browsers.length && browser == null; count++) {
                    if (Runtime.getRuntime().exec(
                            new String[]{"which", browsers[count]}).waitFor() == 0) {
                        browser = browsers[count];
                    }
                }
                if (browser == null) {
                    throw new Exception(JAMS.i18n("Could_not_find_web_browser"));
                } else {
                    Runtime.getRuntime().exec(new String[]{browser, url});
                }
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, errMsg + ":\n" + e.getLocalizedMessage());
        }
    }

    static public void setupLogHandler(Logger logger, final Component owner) {
        logger.addHandler(new Handler() {
            @Override
            public void publish(LogRecord record) {
                String msg = record.getMessage();
                int i = 0;
                Object[] params = record.getParameters();
                if (params != null) {
                    for (Object o : params) {
                        msg = msg.replace("{" + i + "}", o.toString());
                        i++;
                    }
                }
                if (record.getLevel().intValue() <= Level.INFO.intValue()) {
                    GUIHelper.showInfoDlg(owner, msg, JAMS.i18n("INFO"));
                } else if (record.getLevel().intValue() <= Level.SEVERE.intValue()) {
                    GUIHelper.showErrorDlg(owner, msg, JAMS.i18n("ERROR"));
                }
            }

            @Override
            public void flush() {
            }

            @Override
            public void close() {
            }
        });
    }

    public static JMenuItem getRecentMenu(SystemProperties properties) {

        JMenuItem recentMenu = new JMenuItem(JAMS.i18n("Recent_Files"));

        return recentMenu;

    }

    // Center on screen ( absolute true/false (exact center or 25% upper left) )
    public static void centerOnScreen(final Component c, final boolean absolute) {
        final int width = c.getWidth();
        final int height = c.getHeight();
        final Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        int x = (screenSize.width / 2) - (width / 2);
        int y = (screenSize.height / 2) - (height / 2);
        if (!absolute) {
            x /= 2;
            y /= 2;
        }
        c.setLocation(x, y);
    }

    // Center on parent ( absolute true/false (exact center or 25% upper left) )
    public static void centerOnParent(final Window child, final boolean absolute) {
        child.pack();
        boolean useChildsOwner = child.getOwner() != null ? ((child.getOwner() instanceof JFrame) || (child.getOwner() instanceof JDialog)) : false;
        final Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        final Dimension parentSize = useChildsOwner ? child.getOwner().getSize() : screenSize;
        final Point parentLocationOnScreen = useChildsOwner ? child.getOwner().getLocationOnScreen() : new Point(0, 0);
        final Dimension childSize = child.getSize();
        childSize.width = Math.min(childSize.width, screenSize.width);
        childSize.height = Math.min(childSize.height, screenSize.height);
        child.setSize(childSize);
        int x;
        int y;
        if ((child.getOwner() != null) && child.getOwner().isShowing()) {
            x = (parentSize.width - childSize.width) / 2;
            y = (parentSize.height - childSize.height) / 2;
            x += parentLocationOnScreen.x;
            y += parentLocationOnScreen.y;
        } else {
            x = (screenSize.width - childSize.width) / 2;
            y = (screenSize.height - childSize.height) / 2;
        }
        if (!absolute) {
            x /= 2;
            y /= 2;
        }
        child.setLocation(x, y);
    }

    public static Window findWindow(Component c) {
        if (c == null) {
            return JOptionPane.getRootFrame();
        } else if (c instanceof Window) {
            return (Window) c;
        } else {
            return findWindow(c.getParent());
        }
    }
}
