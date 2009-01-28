/*
 * ShapeEntityReader.java
 * Created on 22. Januar 2009, 21:32
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
package jams.components.io;

import com.vividsolutions.jts.algorithm.CentroidArea;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import jams.data.Attribute;
import jams.data.JAMSDataFactory;
import jams.data.JAMSEntity;
import jams.data.JAMSEntityCollection;
import jams.data.JAMSString;
import jams.model.JAMSComponent;
import jams.model.JAMSComponentDescription;
import jams.model.JAMSVarDescription;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import org.geotools.data.shapefile.ShapefileDataStore;
import org.geotools.feature.AttributeType;
import org.geotools.feature.Feature;
import org.geotools.filter.AreaFunction;

/**
 *
 * @author Sven Kralisch <sven.kralisch at uni-jena.de>
 */
@JAMSComponentDescription (title = "ShapeEntityReader",
                           author = "Sven Kralisch",
                           description = "Reads a shape file and creates a " +
"list of JAMS entities containing an entity for each feature. An attribute " +
"name must be provided in order to identify the id field used in the shape file")
public class ShapeEntityReader extends JAMSComponent {

    @JAMSVarDescription (access = JAMSVarDescription.AccessType.READ,
                         description = "Name of the shape file")
    public JAMSString shapeFileName;

    @JAMSVarDescription (access = JAMSVarDescription.AccessType.READ,
                         description = "Name of identifying attribute in shape file")
    public JAMSString idName;

    @JAMSVarDescription (access = JAMSVarDescription.AccessType.READ,
                         description = "Name of the area attribute to be created",
                         defaultValue = "area")
    public JAMSString areaAttribute;

    @JAMSVarDescription (access = JAMSVarDescription.AccessType.READ,
                         description = "Name of the x attribute to be created",
                         defaultValue = "x")
    public JAMSString xAttribute;

    @JAMSVarDescription (access = JAMSVarDescription.AccessType.READ,
                         description = "Name of the y attribute to be created",
                         defaultValue = "y")
    public JAMSString yAttribute;

    @JAMSVarDescription (access = JAMSVarDescription.AccessType.WRITE,
                         description = "Entity collection to be created")
    public JAMSEntityCollection entities;

    @Override
    public void init() throws Exception {

        URL shapeUrl = (new java.io.File(getModel().getWorkspaceDirectory().getPath() + "/" + shapeFileName.getValue()).toURI().toURL());
        ShapefileDataStore store = new ShapefileDataStore(shapeUrl);

        Iterator featureIterator = store.getFeatureSource(store.getTypeNames()[0]).getFeatures().iterator();

        AttributeType[] types = store.getFeatureSource(store.getTypeNames()[0]).getSchema().getAttributeTypes();
        int idAttributeIndex = -1;

        for (int i = 0; i < types.length; i++) {
            if (types[i].getName().equals(idName.getValue()) &&
                    ((types[i].getType() == Long.class) || (types[i].getType() == Integer.class))) {
                idAttributeIndex = i;
            }
        }

        ArrayList<JAMSEntity> entityList = new ArrayList<JAMSEntity>();

        while (featureIterator.hasNext()) {
            Feature f = (Feature) featureIterator.next();

            Attribute.Entity e = JAMSDataFactory.createEntity();

            for (int i = 0; i < types.length; i++) {
                e.setObject(types[i].getName(), JAMSDataFactory.createInstance(f.getAttribute(i)));
            }
            Geometry geom = f.getDefaultGeometry();
            AreaFunction af = new AreaFunction();
            e.setDouble(areaAttribute.getValue(), af.getArea(geom));

            CentroidArea c2d = new CentroidArea();
            c2d.add(geom);
            Coordinate coord = c2d.getCentroid();
            e.setDouble(xAttribute.getValue(), coord.x);
            e.setDouble(yAttribute.getValue(), coord.y);

            if (idAttributeIndex != -1) {
                try {
                    long id = Long.parseLong(f.getAttribute(idAttributeIndex).toString());
                    e.setId(id);
                } catch (NumberFormatException nfe) {
                    getModel().getRuntime().sendErrorMsg("Could not parse " + f.getAttribute(idAttributeIndex) + " as long value!");
                }
            }

//            System.out.println(e.getValue());
            entityList.add((JAMSEntity) e);
        }

        entities.setEntities(entityList);
    }
}
