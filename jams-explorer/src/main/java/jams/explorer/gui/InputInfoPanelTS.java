/*
 * InputInfoPanelTS.java
 * Created on 11. June 2009, 10:55
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

import jams.JAMS;
import jams.gui.tools.GUIHelper;
import jams.workspace.stores.StandardInputDataStore;
import jams.workspace.stores.TSDataStore;
import java.util.Calendar;
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

        GUIHelper.addGBComponent(this, mainLayout, new JLabel(JAMS.i18n("NAME:")), 1, 0, 1, 1, 0, 0);
        GUIHelper.addGBComponent(this, mainLayout, new JLabel(JAMS.i18n("TYP:")), 1, 1, 1, 1, 0, 0);
        GUIHelper.addGBComponent(this, mainLayout, new JLabel(JAMS.i18n("BEGINN:")), 1, 2, 1, 1, 0, 0);
        GUIHelper.addGBComponent(this, mainLayout, new JLabel(JAMS.i18n("ENDE:")), 1, 3, 1, 1, 0, 0);
        GUIHelper.addGBComponent(this, mainLayout, new JLabel(JAMS.i18n("SCHRITTWEITE:")), 1, 4, 1, 1, 0, 0);
        GUIHelper.addGBComponent(this, mainLayout, new JLabel(JAMS.i18n("LÜCKENWERT:")), 1, 5, 1, 1, 0, 0);
        GUIHelper.addGBComponent(this, mainLayout, new JLabel(JAMS.i18n("KOMMENTAR:")), 1, 6, 1, 1, 0, 0);

        indexMap.put(Calendar.YEAR, JAMS.i18n("JAHR(E)"));
        indexMap.put(Calendar.MONTH, JAMS.i18n("MONAT(E)"));
        indexMap.put(Calendar.DAY_OF_YEAR, JAMS.i18n("TAG(E)"));
        indexMap.put(Calendar.HOUR_OF_DAY, JAMS.i18n("STUNDE(N)"));
        indexMap.put(Calendar.MINUTE, JAMS.i18n("MINUTE(N)"));
        indexMap.put(Calendar.SECOND, JAMS.i18n("SEKUNDE(N)"));

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
