/*
 * OutputDSPanel.java
 * Created on 29. März 2009, 15:09
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
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, see <http://www.gnu.org/licenses/>.
 *
 */
package jams.explorer.gui;

import jams.JAMSException;
import jams.JAMSLogging;
import jams.workspace.dsproc.AbstractDataStoreProcessor;
import java.awt.BorderLayout;
import java.io.File;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JPanel;
import jams.explorer.JAMSExplorer;
import jams.explorer.spreadsheet.JAMSSpreadSheet;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;

/**
 *
 * @author Sven Kralisch <sven.kralisch at uni-jena.de>
 */
public class OutputDSPanel extends JPanel {

    private JAMSSpreadSheet spreadsheet;

    public OutputDSPanel(JAMSExplorer explorer, File file, String id) {

        this.setLayout(new BorderLayout());

        JAMSLogging.registerLogger(JAMSLogging.LogOption.Show, Logger.getLogger(OutputDSPanel.class.getName()));

        // create the spreadsheet
        this.spreadsheet = new JAMSSpreadSheet(explorer);
        this.spreadsheet.init();
        this.spreadsheet.setAsOutputSheet();
        this.spreadsheet.setID(id);
        this.setName(file.getName());

        DSPanel tsp = null;
        // create the controller panel
        switch (AbstractDataStoreProcessor.getDataStoreType(file)) {
            case Unsupported:
                Logger.getLogger(OutputDSPanel.class.getName()).log(Level.WARNING, "Unsupported datastore");
                break;
            case TimeDataSerie:
                tsp = new EnsembleTimeSeriesPanel();
                break;
            case SpatioTemporal:
                tsp = new TimeSpaceDSPanel();
                break;
            case DataSerie1D:
                tsp = new SimpleDSPanel();
                break;
            case Timeserie:
                tsp = new SimpleDSPanel();
                break;
            default:
                Logger.getLogger(OutputDSPanel.class.getName()).log(Level.WARNING, "Unsupported datastore");
                break;
        }

        if (tsp != null) {
            tsp.setExplorer(explorer);
            tsp.setOutputSpreadSheet(this.spreadsheet);
            tsp.createProc(file);

            JSplitPane split = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
            split.add(tsp);
            split.add(spreadsheet);
            split.setDividerLocation(300);
            this.add(split, BorderLayout.CENTER);
//            this.add(tsp, BorderLayout.NORTH);
//            this.add(this.spreadsheet, BorderLayout.CENTER);
        } else {
            throw new JAMSException("Unsupported datastore");
        }
    }

    /**
     * @return the spreadsheet
     */
    public JAMSSpreadSheet getSpreadsheet() {
        return spreadsheet;
    }
}
