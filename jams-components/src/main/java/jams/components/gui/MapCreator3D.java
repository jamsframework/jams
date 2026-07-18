/*
 * MapCreator3D.java
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
package jams.components.gui;

import jams.data.Attribute;
import jams.model.JAMSComponentDescription;
import jams.model.JAMSGUIComponent;
import jams.model.JAMSVarDescription;
import java.awt.BorderLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

/**
 * NOTE: The interactive 3D map preview is currently disabled. It was built on
 * GeoTools 2.5.2 plus Java3D, whose APIs were replaced/removed when the
 * framework was upgraded to GeoTools 33. The component still loads so that
 * existing models keep working — it simply no longer renders a map.
 *
 * @author C. Fischer
 */
@JAMSComponentDescription(
        title = "MapCreator3D",
        author = "Christian Fischer",
        description = "Viewer component for JAMS entities and parameter (3D). "
        + "NOTE: the map preview is currently disabled (pending GeoTools 33 map API migration).",
        date = "2010-10-22"
)
public class MapCreator3D extends JAMSGUIComponent {

    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
            description = "Name of SLD-File containing layer style information")
    public Attribute.String stylesFileName;

    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
            description = "ID of a style in the SLD-File")
    public Attribute.Integer styleID;

    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
            description = "Collection of hru objects")
    public Attribute.EntityCollection hrus;

    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
            description = "Name of hru attribute to add for mapping")
    public Attribute.StringArray showAttr;

    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
            description = "Number of ranges for classification attribute")
    public Attribute.StringArray numOfRanges;

    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
            description = "Color shading the ranges")
    public Attribute.StringArray rangeColor;

    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
            description = "Name of shapefile to add as a layer to the map")
    public Attribute.String shapeFileName1;

    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
            description = "Name of shapefile to add as a layer to the map")
    public Attribute.String shapeFileName2;

    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
            description = "Name of shapefile to add as a layer to the map")
    public Attribute.String shapeFileName3;

    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
            description = "Colors for extra shapefiles")
    public Attribute.StringArray shapeColors;

    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
            update = JAMSVarDescription.UpdateType.RUN, description = "Original shape file name")
    public Attribute.String baseShape;

    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
            description = "path to height map")
    public Attribute.String heightMap;

    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
            description = "resolution of 3d model, that means number of grid cells in each row",
            defaultValue = "256")
    public Attribute.Integer resolution = null;

    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
            description = "switch to toggle directional lighting of 3d model on and off",
            defaultValue = "true")
    public Attribute.Boolean light = null;

    private static final String DISABLED_MESSAGE =
            "<html><center>The 3D map preview is currently disabled.<br>"
            + "(pending GeoTools 33 map API migration)</center></html>";

    private transient JPanel panel;

    @Override
    public JPanel getPanel() {
        if (panel == null) {
            panel = new JPanel(new BorderLayout());
            panel.add(new JLabel(DISABLED_MESSAGE, SwingConstants.CENTER), BorderLayout.CENTER);
        }
        return panel;
    }

    @Override
    public void init() {
        getModel().getRuntime().println(
                "MapCreator3D: map preview is disabled in this build (GeoTools 33 map API migration pending).");
    }

    @Override
    public void run() {
    }
}
