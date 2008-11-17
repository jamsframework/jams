/*
 * LHelper.java
 * Created on 19. September 2006, 10:11
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
package jams.gui;

import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.lang.reflect.Method;
import javax.swing.BorderFactory;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import jams.gui.input.BooleanInput;
import jams.gui.input.CalendarInput;
import jams.gui.input.FileInput;
import jams.gui.input.FloatInput;
import jams.gui.input.InputComponent;
import jams.gui.input.IntegerInput;
//import jams.gui.input.IntegerArrayInput;
import jams.gui.input.TextInput;
import jams.gui.input.TimeintervalInput;

/**
 *
 * @author S. Kralisch
 */
public class LHelper {

    private static final int JCOMP_HEIGHT = 20;
    private static final int NUMBERINPUT_WIDTH = 100;
    private static final int TEXTINPUT_WIDTH = 250;
    private static final int FILEINPUT_WIDTH = 250;

    /**
     * Remove swing component from container
     * @param cont Container object
     * @param c Component object
     */
    public static void removeGBComponent(Container cont, Component c) {
        cont.remove(c);
    }

    /**
     * Add swing component to container using gridbag layout
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
     * @param owner
     * @param message
     * @param title
     * @return
     */
    public static int showYesNoCancelDlg(Component owner, String message, String title) {
        Object[] options = {java.util.ResourceBundle.getBundle("resources/Bundle").getString("Yes"), java.util.ResourceBundle.getBundle("resources/Bundle").getString("No"), java.util.ResourceBundle.getBundle("resources/Bundle").getString("Cancel")};
        int result = JOptionPane.showOptionDialog(owner, message, title,
                JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE, null, options, options[0]);

        return result;
    }

    /**
     * Show Yes-No dialog
     * @param owner
     * @param message
     * @param title
     * @return
     */
    public static int showYesNoDlg(Component owner, String message, String title) {
        Object[] options = {java.util.ResourceBundle.getBundle("resources/Bundle").getString("Yes"), java.util.ResourceBundle.getBundle("resources/Bundle").getString("No")};
        int result = JOptionPane.showOptionDialog(owner, message, title,
                JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE, null, options, options[0]);
        //int result = JOptionPane.showConfirmDialog(JUICE.getJuiceFrame(), "Delete Attribute \"" + attrName + "\"?", "Confirm", JOptionPane.YES_NO_OPTION);

        return result;
    }

    /**
     * Show info dialog
     * @param owner
     * @param message
     * @param title
     */
    public static void showInfoDlg(Component owner, String message, String title) {
        JOptionPane.showMessageDialog(owner, message, title, JOptionPane.INFORMATION_MESSAGE);
    }

    /**
     * Show error dialog
     * @param owner
     * @param message
     * @param title
     */
    public static void showErrorDlg(Component owner, String message, String title) {
        JOptionPane.showMessageDialog(owner, message, title, JOptionPane.ERROR_MESSAGE);
    }

    /**
     * Show input dialog
     * @param owner
     * @param message
     * @param initalValue
     * @return
     */
    public static String showInputDlg(Component owner, String message, String initalValue) {
        return JOptionPane.showInputDialog(owner, message, initalValue);
    }

    /**
     * Show input dialog
     * @param owner
     * @param message
     * @param title
     * @param initalValue
     * @return
     */
    public static String showInputDlg(Component owner, String message, String title, String initalValue) {
        return (String) JOptionPane.showInputDialog(owner, message, title, JOptionPane.QUESTION_MESSAGE, null, null, initalValue);
    }

    /**
     * Create swing input component based on data type
     * @param type Data type
     * @return InputComponent object
     */
    public static InputComponent createInputComponent(String type) {
        InputComponent ic;
        if (type.equals("JAMSFileName")) {
            ic = new FileInput();
            //((Component) ic).setPreferredSize(new Dimension(FILEINPUT_WIDTH, JCOMP_HEIGHT));
        } else if (type.equals("JAMSDirName")) {
            ic = new FileInput(true);
            //((Component) ic).setPreferredSize(new Dimension(FILEINPUT_WIDTH, JCOMP_HEIGHT));
        } else if (type.equals("JAMSCalendar")) {
            ic = new CalendarInput();
        } else if (type.equals("JAMSTimeInterval")) {
            ic = new TimeintervalInput();
        } else if (type.equals("JAMSBoolean")) {
            ic = new BooleanInput();
        } else if ((type.equals("JAMSInteger")) || (type.equals("JAMSLong"))) {
            ic = new IntegerInput();
            ic.getComponent().setPreferredSize(new Dimension(NUMBERINPUT_WIDTH, JCOMP_HEIGHT));
            ic.getComponent().setBorder(BorderFactory.createEtchedBorder());
        } else if ((type.equals("JAMSFloat")) || (type.equals("JAMSDouble"))) {
            ic = new FloatInput();
            ic.getComponent().setPreferredSize(new Dimension(NUMBERINPUT_WIDTH, JCOMP_HEIGHT));
            ic.getComponent().setBorder(BorderFactory.createEtchedBorder());
        } else {
            ic = new TextInput();
            ic.getComponent().setPreferredSize(new Dimension(TEXTINPUT_WIDTH, JCOMP_HEIGHT));
            ic.getComponent().setBorder(BorderFactory.createEtchedBorder());
        }

        //ic.getComponent().setBorder(BorderFactory.createEtchedBorder());
        return ic;
    }

    /**
     * Create a new JFileChooser and support disabling of zipped folders 
     * @return new JFileChooser
     */
    public static JFileChooser getJFileChooser() {
        return new JFileChooser() {
            @Override
            public void updateUI() {
                //putClientProperty("FileChooser.useShellFolder", Boolean.FALSE);
                super.updateUI();
            }
        };
    }

    /**
     * Open URL in systems default web browser on Windows, OSX and Unix/Linux.
     * For Unix/Linux a number of different browsers are tested.
     * Taken from BrowserLauncher2 project (http://sourceforge.net/projects/browserlaunch2).
     * @param url URL to be opened
     */
    public static void openURL(String url) {

        String osName = System.getProperty("os.name");
        String errMsg = java.util.ResourceBundle.getBundle("resources/Bundle").getString("Error_attempting_to_launch_web_browser");

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
                    throw new Exception(java.util.ResourceBundle.getBundle("resources/Bundle").getString("Could_not_find_web_browser"));
                } else {
                    Runtime.getRuntime().exec(new String[]{browser, url});
                }
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, errMsg + ":\n" + e.getLocalizedMessage());
        }
    }
}
