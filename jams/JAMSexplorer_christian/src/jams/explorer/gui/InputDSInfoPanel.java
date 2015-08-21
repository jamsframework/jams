/*
 * InputDSInfoPanel.java
 * Created on 19. November 2008, 10:47
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

import jams.workspace.stores.ShapeFileDataStore;
import jams.workspace.stores.StandardInputDataStore;
import jams.workspace.stores.TSDataStore;
import java.util.HashMap;
import javax.swing.JPanel;

/**
 *
 * @author Sven Kralisch <sven.kralisch at uni-jena.de>
 */
public class InputDSInfoPanel extends JPanel {

    /**
     * one info panel for each input source type
     */
    private HashMap<Class, InputInfoPanelSimple> infoPanels = new HashMap<Class, InputInfoPanelSimple>();

    public InputDSInfoPanel() {
        super();
    }

    public void updateDS(StandardInputDataStore store) {
        if (store == null) {
            this.removeAll();
            this.updateUI();
            return;
        }

        Class storeClass = store.getClass();
        InputInfoPanelSimple infoPanel = infoPanels.get(storeClass);
        if (infoPanel == null) {
            if (store instanceof TSDataStore) {
                infoPanel = new InputInfoPanelTS();
            }
            if (store instanceof ShapeFileDataStore) {
                infoPanel = new InputInfoPanelShape();
            }
        }
        this.removeAll();
        if (infoPanel != null) {
            infoPanel.updateInfoPanel(store);
            this.add(infoPanel);
            infoPanels.put(storeClass, infoPanel);
        }
        this.updateUI();
    }
}
