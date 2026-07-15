/*
 * OutputPanelFactory.java
 * Created on 4. April 2009, 21:11
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
package jams.explorer;

import jams.io.BufferedFileReader;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import optas.data.DataCollection;
import jams.workspace.dsproc.DataStoreProcessor;
import jams.explorer.gui.ExplorerFrame;
import jams.explorer.gui.ImportMonteCarloDataPanel;
import jams.explorer.gui.OutputDSPanel;
import jams.explorer.gui.SimpleOutputPanel;
import jams.explorer.spreadsheet.JAMSSpreadSheet;
import jams.explorer.spreadsheet.SpreadsheetConstants;
import jams.explorer.tree.FileObject;

/**
 *
 * @author Sven Kralisch <sven.kralisch at uni-jena.de>
 */
public class OutputPanelFactory {

    public static DataCollectionViewController constructDataCollection(ExplorerFrame frame, DataCollection dc, File f) {
        ImportMonteCarloDataPanel importDlg = new ImportMonteCarloDataPanel(frame, dc, f);
        importDlg.addActionEventListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {

            }
        });

        JDialog dialog = importDlg.getDialog();
        dialog.setLocationRelativeTo(null);
        dialog.setModal(true);
        dialog.setVisible(true);

        //ImportMonteCarloDataPanel importDialog = (ImportMonteCarloDataPanel) e.getSource();
        DataCollection collection = importDlg.getEnsemble();
        DataCollectionViewController controller = new DataCollectionViewController(collection);

        return controller;
    }

    public static Component getOutputDSPanel(JAMSExplorer explorer, FileObject file, String id) throws FileNotFoundException, IOException {
        if (file.getFile().getAbsolutePath().endsWith("cdat")){
            DataCollection collection = DataCollection.createFromFile(file.getFile());
            if (collection == null){
                JOptionPane.showMessageDialog(explorer.getExplorerFrame(), "failed to load data collection from file: " + file.getFile().getName());
                return null;
            }
            DataCollectionViewController controller = new DataCollectionViewController(collection);
            //tPane.addTab("New Ensemble", controller.getView());
            return controller.getView();
        }
        
        if (file.getFile().getAbsolutePath().endsWith(".csv")){
            if (DataStoreProcessor.getDataStoreType(file.getFile()) == DataStoreProcessor.DataStoreType.TimeDataSerie ||
                DataStoreProcessor.getDataStoreType(file.getFile()) == DataStoreProcessor.DataStoreType.DataSerie1D){
                DataCollectionViewController controller = OutputPanelFactory.constructDataCollection(explorer.getExplorerFrame(), null, file.getFile());
                return controller.getView();
            }else {
                return new OutputDSPanel(explorer, file.getFile(), id);
            }
        }
        
        BufferedFileReader reader = new BufferedFileReader(new FileInputStream(file.getFile()));
        String line = reader.readLine();
        reader.close();

        if (line == null) {
            return null;
        }

        if (line.startsWith("@context")) {
            return new OutputDSPanel(explorer, file.getFile(), id);
        }
        
        if (line.startsWith(SpreadsheetConstants.LOAD_HEADERS)) {

            // create the spreadsheet
            JAMSSpreadSheet spreadsheet = new JAMSSpreadSheet(explorer);
            spreadsheet.init();
            spreadsheet.load(file.getFile());
            spreadsheet.setAsOutputSheet();
            spreadsheet.setID(id);

            return spreadsheet;
        }

        return new SimpleOutputPanel(file.getFile());
    }
}
