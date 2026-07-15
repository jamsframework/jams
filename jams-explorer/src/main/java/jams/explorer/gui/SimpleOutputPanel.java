/*
 * SimpleOutputPanel.java
 * Created on 5. April 2009, 00:53
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
 * but WITHOUT ANY WARRtpANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, see <http://www.gnu.org/licenses/>.
 *
 */
package jams.explorer.gui;

import jams.tools.FileTools;
import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JPanel;
import javax.swing.JTextArea;

/**
 *
 * @author Sven Kralisch <sven.kralisch at uni-jena.de>
 */
public class SimpleOutputPanel extends JPanel {

    private JTextArea textArea;

    public SimpleOutputPanel(File file) {

        String text;
        try {
            text = FileTools.fileToString(file.getAbsolutePath());
        } catch (IOException ex) {
            Logger.getLogger(SimpleOutputPanel.class.getName()).log(Level.SEVERE, null, ex);
            text = "";
        }
        textArea = new JTextArea(text);
        textArea.setEditable(false);
        this.add(textArea);

    }
}
