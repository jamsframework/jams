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

import jams.io.BufferedFileReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import javax.swing.JPanel;
import reg.gui.DataCollectionPanel;
import reg.gui.OutputDSPanel;
import reg.gui.SimpleOutputPanel;
import reg.spreadsheet.JAMSSpreadSheet;
import reg.spreadsheet.SpreadsheetConstants;

/**
 *
 * @author Sven Kralisch <sven.kralisch at uni-jena.de>
 */
public class OutputPanelFactory {

    public static JPanel getOutputDSPanel(JAMSExplorer explorer, File file, String id) throws FileNotFoundException, IOException {
        if (file.getAbsolutePath().endsWith("cdat")){
            return new DataCollectionPanel(explorer.getExplorerFrame(), file, null);
        }
        BufferedFileReader reader = new BufferedFileReader(new FileInputStream(file));
        String line = reader.readLine();
        reader.close();

        if (line == null) {
            return null;
        }

        if (line.startsWith("@context")) {
            return new OutputDSPanel(explorer, file, id);
        }

        if (line.startsWith(SpreadsheetConstants.LOAD_HEADERS)) {

            // create the spreadsheet
            JAMSSpreadSheet spreadsheet = new JAMSSpreadSheet(explorer);
            spreadsheet.init();
            spreadsheet.load(file);
            spreadsheet.setAsOutputSheet();
            spreadsheet.setID(id);

            return spreadsheet;
        }

        return new SimpleOutputPanel(file);
    }
}
