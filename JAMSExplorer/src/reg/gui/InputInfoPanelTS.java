/*
 * InputInfoPanelTS.java
 * Created on 11. June 2009, 10:55
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
import jams.tools.GUIHelper;
import jams.workspace.stores.StandardInputDataStore;
import jams.workspace.stores.TSDataStore;
import java.util.HashMap;
import java.util.Map;
import javax.swing.JLabel;

/**
 *
 * @author hbusch
 */
public class InputInfoPanelTS extends InputInfoPanelSimple {

    private Map<Integer, String> indexMap = new HashMap<Integer, String>();

    public InputInfoPanelTS() {
        super(6);

        GUIHelper.addGBComponent(this, mainLayout, new JLabel("Name:"), 1, 0, 1, 1, 0, 0);
        GUIHelper.addGBComponent(this, mainLayout, new JLabel("Typ:"), 1, 1, 1, 1, 0, 0);
        GUIHelper.addGBComponent(this, mainLayout, new JLabel("Beginn:"), 1, 2, 1, 1, 0, 0);
        GUIHelper.addGBComponent(this, mainLayout, new JLabel("Ende:"), 1, 3, 1, 1, 0, 0);
        GUIHelper.addGBComponent(this, mainLayout, new JLabel("Schrittweite:"), 1, 4, 1, 1, 0, 0);
        GUIHelper.addGBComponent(this, mainLayout, new JLabel("Lückenwert:"), 1, 5, 1, 1, 0, 0);
        GUIHelper.addGBComponent(this, mainLayout, new JLabel("Kommentar:"), 1, 6, 1, 1, 0, 0);

        indexMap.put(JAMSCalendar.YEAR, "Jahr(e)");
        indexMap.put(JAMSCalendar.MONTH, "Monat(e)");
        indexMap.put(JAMSCalendar.DAY_OF_YEAR, "Tag(e)");
        indexMap.put(JAMSCalendar.HOUR_OF_DAY, "Stunde(n)");
        indexMap.put(JAMSCalendar.MINUTE, "Minute(n)");
        indexMap.put(JAMSCalendar.SECOND, "Sekunde(n)");

    }

    @Override
    public void updateInfoPanel(StandardInputDataStore datastore) {
        TSDataStore store = (TSDataStore) datastore;
        fields[0].setText(store.getID());
        fields[1].setText(store.getClass().getSimpleName());
        fields[2].setText(store.getStartDate().toString());
        fields[3].setText(store.getEndDate().toString());
        fields[4].setText(Integer.toString(store.getTimeUnitCount()) + " " + indexMap.get(store.getTimeUnit()));
        fields[5].setText(store.getMissingDataValue());
        textArea.setText(store.getDescription());
    }
}
