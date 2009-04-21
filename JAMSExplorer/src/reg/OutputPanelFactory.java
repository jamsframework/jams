/*
 * OutputPanelFactory.java
 * Created on 4. April 2009, 21:11
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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import javax.swing.JPanel;
import reg.dsproc.BufferedFileReader;
import reg.gui.OutputDSPanel;
import reg.gui.SimpleOutputPanel;

/**
 *
 * @author Sven Kralisch <sven.kralisch at uni-jena.de>
 */
public class OutputPanelFactory {

    public static JPanel getOutputDSPanel(File file) throws FileNotFoundException, IOException {
        BufferedFileReader reader = new BufferedFileReader(new FileInputStream(file));
        String line = reader.readLine();
        reader.close();

        if (line == null) {
            return null;
        }

        if (line.startsWith("@context")) {
            return OutputDSPanel.createPanel(file);
        }

        return new SimpleOutputPanel(file);
    }
}
