/*
 * InputInfoPanelShape.java
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
import jams.workspace.stores.ShapeFileDataStore;
import jams.workspace.stores.StandardInputDataStore;
import java.net.URI;
import javax.swing.JLabel;

/**
 *
 * @author hbusch
 */
public class InputInfoPanelShape extends InputInfoPanelSimple {


    public InputInfoPanelShape() {
        super(5);
        GUIHelper.addGBComponent(this, mainLayout, new JLabel(JAMS.i18n("NAME:")), 1, 0, 1, 1, 0, 0);
        GUIHelper.addGBComponent(this, mainLayout, new JLabel(JAMS.i18n("TYP:")), 1, 1, 1, 1, 0, 0);
        GUIHelper.addGBComponent(this, mainLayout, new JLabel(JAMS.i18n("DATEI:")), 1, 2, 1, 1, 0, 0);
        GUIHelper.addGBComponent(this, mainLayout, new JLabel(JAMS.i18n("URI:")), 1, 3, 1, 1, 0, 0);
        GUIHelper.addGBComponent(this, mainLayout, new JLabel(JAMS.i18n("ID-FELD:")), 1, 4, 1, 1, 0, 0);
        GUIHelper.addGBComponent(this, mainLayout, new JLabel(JAMS.i18n("KOMMENTAR:")), 1, 5, 1, 1, 0, 0);
    }

    @Override
    public void updateInfoPanel(StandardInputDataStore datastore) {
        ShapeFileDataStore store = (ShapeFileDataStore) datastore;
        fields[0].setText(store.getID());
        fields[1].setText(store.getClass().getSimpleName());
        fields[2].setText(store.getFileName());
        URI uri = store.getUri();
        if (uri != null) {
            fields[3].setText(uri.toString());
        }
        fields[4].setText(store.getKeyColumn());
        textArea.setText(store.getDescription());
    }
}
