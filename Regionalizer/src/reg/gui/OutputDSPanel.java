/*
 * OutputDSPanel.java
 * Created on 29. März 2009, 15:09
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
package reg.gui;

import java.io.File;
import javax.swing.JPanel;
import javax.swing.JSplitPane;
import reg.Regionalizer;
import reg.spreadsheet.JAMSSpreadSheet;

/**
 *
 * @author Sven Kralisch <sven.kralisch at uni-jena.de>
 */
public class OutputDSPanel extends JPanel {

    private JSplitPane splitPane;

    public static OutputDSPanel createPanel(File file) {
        return new OutputDSPanel(file);
    }

    private OutputDSPanel(File file) {

        splitPane = new JSplitPane();
        splitPane.setOrientation(JSplitPane.VERTICAL_SPLIT);

        // create the spreadsheet
        String[] default_headers = {""};
        JAMSSpreadSheet spreadsheet = new JAMSSpreadSheet(Regionalizer.getRegionalizerFrame(), default_headers);
        spreadsheet.init();

        // create the controller panel
        TimeSpaceDSPanel tsp = new TimeSpaceDSPanel();
        tsp.setParent(Regionalizer.getRegionalizerFrame());
        tsp.setOutputSpreadSheet(spreadsheet);
        tsp.createTsProc(file);

        splitPane.setLeftComponent(tsp);
        splitPane.setRightComponent(spreadsheet.getPanel());

        this.add(splitPane);
    }
}
