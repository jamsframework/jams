/*
 * GeomReader.java
 *
 * This file is part of JAMS
 * Copyright (C) 2005 FSU Jena
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

package jams.components.io;

import com.vividsolutions.jts.geom.Geometry;

import jams.data.Attribute;
import org.geotools.data.shapefile.ShapefileDataStore;

import jams.data.JAMSEntityCollection;
import jams.data.JAMSString;
import jams.model.JAMSComponent;
import jams.model.JAMSVarDescription;

import java.util.HashMap;
import java.util.List;
import org.geotools.data.FeatureSource;
import org.geotools.feature.FeatureIterator;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.feature.type.Name;

/**
 * Reads geometries from HRU shapefile and adds them to JAMS entities as spatial attribute.
 * Is used by MapCreator for instance.
 *
 * @author C. Schwartze
 */

@JAMSComponentDescription(
        title="GeomReader",
        author="Christian Schwartze",
        description="Reads geometries from HRU shapefile and adds them to JAMS entities as spatial attributes.",
        process="Visualization",
        version="1.0",
        date="2010-10-22"

        )

public class GeomReader extends JAMSComponent {

    @JAMSVarDescription(
        access = JAMSVarDescription.AccessType.READ,
        update = JAMSVarDescription.UpdateType.INIT,
        description = "shape file name"
        )
        public JAMSString shapeFileName;

    @JAMSVarDescription(
        access = JAMSVarDescription.AccessType.READ,
        update = JAMSVarDescription.UpdateType.RUN,
        description = "Collection of hru objects"
        )
        public JAMSEntityCollection hrus;

    @JAMSVarDescription(
        access = JAMSVarDescription.AccessType.READ,
        update = JAMSVarDescription.UpdateType.INIT,
        description = "Name of identifier column in shape file"
        )
        public JAMSString idName;

    @JAMSVarDescription(
        access = JAMSVarDescription.AccessType.WRITE,
        update = JAMSVarDescription.UpdateType.RUN,
        description = "Original Shape file name"
        )
        public JAMSString baseShape;

    @Override
    public void run() throws Exception {

        java.net.URL shapeUrl = (new java.io.File(getModel().getWorkspaceDirectory().getPath() + "/" + shapeFileName.getValue()).toURI().toURL());
        ShapefileDataStore shapefile = new ShapefileDataStore(shapeUrl);
        List<Name> featureNames = shapefile.getNames();
        FeatureSource<SimpleFeatureType, SimpleFeature> featureSource = shapefile.getFeatureSource(featureNames.get(0));

        baseShape.setValue(getModel().getWorkspaceDirectory().getPath() + "/" + shapeFileName.getValue() + ";" + idName);

        FeatureIterator<SimpleFeature> iterator = featureSource.getFeatures().features();
        
        HashMap<Long, Geometry> geomMap = new HashMap<Long, Geometry>();
        while (iterator.hasNext()) {
            SimpleFeature f = iterator.next();
            Long id = new Long(f.getAttribute(idName.getValue()).toString());       
            geomMap.put(id, (Geometry) f.getDefaultGeometry());
        }

        for (Attribute.Entity e : hrus.getEntities()) {
            long id = new Double(e.getDouble("ID")).longValue();
            e.setGeometry("geom", geomMap.get(id));
        }
    }
}