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

package org.unijena.jams.gui;

import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import javax.swing.BorderFactory;
import javax.swing.JOptionPane;
import org.unijena.jams.gui.input.BooleanInput;
import org.unijena.jams.gui.input.CalendarInput;
import org.unijena.jams.gui.input.FileInput;
import org.unijena.jams.gui.input.FloatInput;
import org.unijena.jams.gui.input.InputComponent;
import org.unijena.jams.gui.input.IntegerInput;
import org.unijena.jams.gui.input.TextInput;
import org.unijena.jams.gui.input.TimeintervalInput;

/**
 *
 * @author S. Kralisch
 */
public class LHelper {
    
    private static final int JCOMP_HEIGHT = 20;
    private static final int NUMBERINPUT_WIDTH = 100;
    private static final int TEXTINPUT_WIDTH = 250;
    private static final int FILEINPUT_WIDTH = 250;
    
    public static void removeGBComponent(Container cont, Component c) {
        
        cont.remove(c);
        
    }
    
    public static Component addGBComponent(Container cont, GridBagLayout gbl, Component c, int x, int y, int width, int height, double weightx, double weighty) {
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.BOTH;
        gbc.insets = new Insets(0, 2, 2, 2);
        gbc.gridx = x; gbc.gridy = y;
        gbc.gridwidth = width; gbc.gridheight = height;
        gbc.weightx = weightx; gbc.weighty = weighty;
        gbl.setConstraints(c, gbc);
        return cont.add(c);
    }
    
    public static Component addGBComponent(Container cont, GridBagLayout gbl, Component c, int x, int y, int width, int height, double weightx, double weighty, int fill) {
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = fill;
        gbc.insets = new Insets(0, 2, 2, 2);
        gbc.gridx = x; gbc.gridy = y;
        gbc.gridwidth = width; gbc.gridheight = height;
        gbc.weightx = weightx; gbc.weighty = weighty;
        gbl.setConstraints(c, gbc);
        return cont.add(c);
    }
    
    public static int showYesNoCancelDlg(Component owner, String message, String title) {
        Object[] options = {"Yes", "No", "Cancel"};
        int result = JOptionPane.showOptionDialog(owner, message, title,
                JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE, null, options, options[0]);
        
        return result;
    }
    
    public static int showYesNoDlg(Component owner, String message, String title) {
        Object[] options = {"Yes", "No"};
        int result = JOptionPane.showOptionDialog(owner, message, title,
                JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE, null, options, options[0]);
        //int result = JOptionPane.showConfirmDialog(JUICE.getJuiceFrame(), "Delete Attribute \"" + attrName + "\"?", "Confirm", JOptionPane.YES_NO_OPTION);
        
        return result;
    }
    
    public static void showInfoDlg(Component owner, String message, String title) {
        JOptionPane.showMessageDialog(owner, message, title, JOptionPane.INFORMATION_MESSAGE);
    }
    
    public static void showErrorDlg(Component owner, String message, String title) {
        JOptionPane.showMessageDialog(owner, message, title, JOptionPane.ERROR_MESSAGE);
    }

    public static String showInputDlg(Component owner, String message, String initalValue) {
        return JOptionPane.showInputDialog(owner, message, initalValue);
    }
    
    public static InputComponent createInputComponent(String type) {
        InputComponent ic;
        if (type.equals("JAMSFile")) {
            ic = new FileInput();
            ((Component) ic).setPreferredSize(new Dimension(FILEINPUT_WIDTH, JCOMP_HEIGHT));
        } else if (type.equals("JAMSDir")) {
            ic = new FileInput(true);
            ((Component) ic).setPreferredSize(new Dimension(FILEINPUT_WIDTH, JCOMP_HEIGHT));
        } else if (type.equals("JAMSCalendar")) {
            ic = new CalendarInput();
        } else if (type.equals("JAMSTimeInterval")) {
            ic = new TimeintervalInput();
        } else if (type.equals("JAMSBoolean")) {
            ic = new BooleanInput();
        } else if ((type.equals("JAMSInteger")) || (type.equals("JAMSLong"))) {
            ic = new IntegerInput();
            ic.getComponent().setPreferredSize(new Dimension(NUMBERINPUT_WIDTH, JCOMP_HEIGHT));
        } else if ((type.equals("JAMSFloat")) || (type.equals("JAMSDouble"))) {
            ic = new FloatInput();
            ic.getComponent().setPreferredSize(new Dimension(NUMBERINPUT_WIDTH, JCOMP_HEIGHT));
        } else {
            ic = new TextInput();
            ic.getComponent().setPreferredSize(new Dimension(TEXTINPUT_WIDTH, JCOMP_HEIGHT));
        }
        
        ic.getComponent().setBorder(BorderFactory.createEtchedBorder());
        return ic;
    }
    
}
