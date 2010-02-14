/*
 * SimpleOutputPanel.java
 * Created on 5. April 2009, 00:53
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
 * but WITHOUT ANY WARRtpANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA
 *
 */

package reg.gui;

import jams.tools.FileTools;
import java.io.File;
import javax.swing.JPanel;
import javax.swing.JTextArea;

/**
 *
 * @author Sven Kralisch <sven.kralisch at uni-jena.de>
 */
public class SimpleOutputPanel extends JPanel {

    private JTextArea textArea;

    public SimpleOutputPanel(File file) {

        String text = FileTools.fileToString(file.getAbsolutePath());
        textArea = new JTextArea(text);
        textArea.setEditable(false);
        this.add(textArea);
        
    }
    
}
