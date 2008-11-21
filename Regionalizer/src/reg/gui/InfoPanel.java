/*
 * InfoPanel.java
 * Created on 19. November 2008, 10:47
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

import jams.data.JAMSCalendar;
import jams.gui.LHelper;
import jams.workspace.stores.DataStore;
import jams.workspace.stores.TSDataStore;
import java.awt.Font;
import java.awt.GridBagLayout;
import java.util.HashMap;
import java.util.Map;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.UIManager;

/**
 *
 * @author Sven Kralisch <sven.kralisch at uni-jena.de>
 */
public class InfoPanel extends JPanel {

    private TSPanel tsPanel;

    public InfoPanel() {
        super();
    }

    public void updateDS(DataStore store) {
        if (store == null) {
            this.removeAll();
            this.updateUI();
            return;
        }

        if (store instanceof TSDataStore) {
            updateTSPanel((TSDataStore) store);
            this.removeAll();
            this.add(tsPanel);
            this.updateUI();
        }
    }

    private void updateTSPanel(TSDataStore store) {
        if (tsPanel == null) {
            tsPanel = new TSPanel();
        }
        tsPanel.updateDS(store);
    }

    private class TSPanel extends JPanel {

        private GridBagLayout mainLayout;
        private int FIELD_COUNT = 6;
        private JTextField[] fields;//idField, typField, startField, endField, stepUnitField, stepSizeField, missingDataField;
        private JTextArea textArea;
        private Map<Integer, String> indexMap = new HashMap<Integer, String>();

        public TSPanel() {

            mainLayout = new GridBagLayout();
            this.setLayout(mainLayout);

            LHelper.addGBComponent(this, mainLayout, new JLabel("Name:"), 1, 0, 1, 1, 0, 0);
            LHelper.addGBComponent(this, mainLayout, new JLabel("Typ:"), 1, 1, 1, 1, 0, 0);
            LHelper.addGBComponent(this, mainLayout, new JLabel("Beginn:"), 1, 2, 1, 1, 0, 0);
            LHelper.addGBComponent(this, mainLayout, new JLabel("Ende:"), 1, 3, 1, 1, 0, 0);
            LHelper.addGBComponent(this, mainLayout, new JLabel("Schrittweite:"), 1, 4, 1, 1, 0, 0);
            LHelper.addGBComponent(this, mainLayout, new JLabel("Lückenwert:"), 1, 5, 1, 1, 0, 0);
            LHelper.addGBComponent(this, mainLayout, new JLabel("Kommentar:"), 1, 6, 1, 1, 0, 0);

            indexMap.put(JAMSCalendar.YEAR, "Jahr(e)");
            indexMap.put(JAMSCalendar.MONTH, "Monat(e)");
            indexMap.put(JAMSCalendar.DAY_OF_YEAR, "Tag(e)");
            indexMap.put(JAMSCalendar.HOUR_OF_DAY, "Stunde(n)");
            indexMap.put(JAMSCalendar.MINUTE, "Minute(n)");
            indexMap.put(JAMSCalendar.SECOND, "Sekunde(n)");

            fields = new JTextField[FIELD_COUNT];
            for (int i = 0; i < fields.length; i++) {
                fields[i] = new JTextField();
                fields[i].setColumns(20);
                fields[i].setEditable(false);
                LHelper.addGBComponent(this, mainLayout, fields[i], 2, i, 1, 1, 0, 0);
            }

            textArea = new JTextArea();
            textArea.setRows(5);
            textArea.setColumns(20);
            textArea.setEditable(false);
            Font textFont = (Font) UIManager.getDefaults().get("Label.font");
            textFont = new Font(textFont.getName(), Font.PLAIN, textFont.getSize() - 1);
            textArea.setFont(textFont);
            JScrollPane textScrollPane = new JScrollPane(textArea);
            LHelper.addGBComponent(this, mainLayout, textScrollPane, 2, FIELD_COUNT, 1, 2, 0, 0);


        }

        public void updateDS(TSDataStore store) {
            fields[0].setText(store.getID());
            fields[1].setText(store.getClass().getSimpleName());
            fields[2].setText(store.getStartDate().toString());
            fields[3].setText(store.getEndDate().toString());
            fields[4].setText(Integer.toString(store.getTimeUnitCount()) + " " + indexMap.get(store.getTimeUnit()));
            fields[5].setText(store.getMissingDataValue());
            textArea.setText(store.getDescription());
        }
    }
}
