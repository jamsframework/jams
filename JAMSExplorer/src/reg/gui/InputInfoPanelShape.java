/*
 * InputInfoPanelShape.java
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
        GUIHelper.addGBComponent(this, mainLayout, new JLabel("Name:"), 1, 0, 1, 1, 0, 0);
        GUIHelper.addGBComponent(this, mainLayout, new JLabel("Typ:"), 1, 1, 1, 1, 0, 0);
        GUIHelper.addGBComponent(this, mainLayout, new JLabel("Datei:"), 1, 2, 1, 1, 0, 0);
        GUIHelper.addGBComponent(this, mainLayout, new JLabel("URI:"), 1, 3, 1, 1, 0, 0);
        GUIHelper.addGBComponent(this, mainLayout, new JLabel("ID-Feld:"), 1, 4, 1, 1, 0, 0);
        GUIHelper.addGBComponent(this, mainLayout, new JLabel("Kommentar:"), 1, 5, 1, 1, 0, 0);
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
