/*
 * MapCreator.java
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
 * NOTE: The interactive 2D map preview is currently disabled. It was built on
 * GeoTools 2.5.2, whose map/rendering API was replaced when the framework was
 * upgraded to GeoTools 33 (the old API is incompatible with modern JDKs). The
 * component still loads so that existing models keep working — it simply no
 * longer renders a map. A reimplementation on the GeoTools 33 gt-swing API is
 * planned.
 *
 * @author Christian Schwartze
 */
@JAMSComponentDescription(
        title = "MapCreator",
        author = "Christian Schwartze",
        description = "Viewer component for JAMS entities, parameter and optional vector layers. "
        + "NOTE: the map preview is currently disabled (pending GeoTools 33 map API migration).",
        date = "2010-10-22"
)
public class MapCreator extends JAMSGUIComponent {

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "Name of SLD-File containing layer style information"
    )
    public Attribute.String stylesFileName;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "current time step"
    )
    public Attribute.Calendar time;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "ID of a style in the SLD-File"
    )
    public Attribute.Integer styleID;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "Collection of hru objects"
    )
    public Attribute.EntityCollection hrus;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "Name of hru attribute to add for mapping"
    )
    public Attribute.StringArray showAttr;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "Number of ranges for classification attribute"
    )
    public Attribute.StringArray numOfRanges;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "Color shading the ranges"
    )
    public Attribute.StringArray rangeColor;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "Name of shapefile to add as a layer to the map"
    )
    public Attribute.String shapeFileName1;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "Name of shapefile to add as a layer to the map"
    )
    public Attribute.String shapeFileName2;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "Name of shapefile to add as a layer to the map"
    )
    public Attribute.String shapeFileName3;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "Colors for extra shapefiles"
    )
    public Attribute.StringArray shapeColors;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "Original shape file name"
    )
    public Attribute.String baseShape;

    private static final String DISABLED_MESSAGE =
            "<html><center>The map preview is currently disabled.<br>"
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
                "MapCreator: map preview is disabled in this build (GeoTools 33 map API migration pending).");
    }

    @Override
    public void run() {
    }
}
