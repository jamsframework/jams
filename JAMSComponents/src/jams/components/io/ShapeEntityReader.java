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

import com.vividsolutions.jts.geom.Geometry;
import jams.data.JAMSData;
import jams.data.JAMSDouble;
import jams.data.JAMSEntityCollection;
import jams.data.JAMSFloat;
import jams.data.JAMSGeometry;
import jams.data.JAMSInteger;
import jams.data.JAMSLong;
import jams.data.JAMSString;
import jams.model.JAMSComponent;
import jams.model.JAMSVarDescription;
import java.net.URL;
import java.util.HashMap;
import java.util.Iterator;
import org.geotools.data.shapefile.ShapefileDataStore;
import org.geotools.feature.AttributeType;
import org.geotools.feature.Feature;

/**
 *
 * @author Sven Kralisch <sven.kralisch at uni-jena.de>
 */
public class ShapeEntityReader extends JAMSComponent {

    @JAMSVarDescription (access = JAMSVarDescription.AccessType.READ,
                         description = "Name of the shape file")
    public JAMSString shapeFileName;

    @JAMSVarDescription (access = JAMSVarDescription.AccessType.READ,
                         description = "Name of identifier column in shape file")
    public JAMSString idName;

    @JAMSVarDescription (access = JAMSVarDescription.AccessType.WRITE,
                         description = "Entity collection to be created")
    public JAMSEntityCollection entities;

    public void init() throws Exception {

        URL shapeUrl = (new java.io.File(getModel().getWorkspaceDirectory().getPath() + "/" + shapeFileName.getValue()).toURI().toURL());
        ShapefileDataStore store = new ShapefileDataStore(shapeUrl);

        Iterator featureIterator = store.getFeatureSource(store.getTypeNames()[0]).getFeatures().iterator();

        AttributeType[] types = store.getFeatureSource(store.getTypeNames()[0]).getSchema().getAttributeTypes();
        for (AttributeType type : types) {
            System.out.println(type.getName() + " " + type.getType());
        }

        HashMap<Long, Geometry> geomMap = new HashMap<Long, Geometry>();
        while (featureIterator.hasNext()) {
            Feature f = (Feature) featureIterator.next();

            for (int i = 0; i < f.getNumberOfAttributes(); i++) {
                System.out.print(f.getAttribute(i) + " ");
            }
        //System.out.println("");


//        	Long id = new Long(f.getAttribute(idName.getValue()).toString());
//        	geomMap.put(id, f.getDefaultGeometry());
        }

//        JAMSEntity e;
//        Iterator<JAMSEntity> hruIterator = entities.getEntities().iterator();
//        while (hruIterator.hasNext()) {
//            e = hruIterator.next();
//            long id = new Double(e.getDouble("ID")).longValue();
//            e.setGeometry("geom", geomMap.get(id));
//        }
    }

    private JAMSData getDataValue(Object value) {
        Class type = value.getClass();
        JAMSData result;

        if (type.equals(Integer.class)) {
            JAMSInteger v = new JAMSInteger();
            v.setValue(((Integer) value).intValue());
            result = v;
        } else if (type.equals(Long.class)) {
            JAMSLong v = new JAMSLong();
            v.setValue(((Long) value).longValue());
            result = v;
        } else if (type.equals(Float.class)) {
            JAMSFloat v = new JAMSFloat();
            v.setValue(((Float) value).floatValue());
            result = v;
        } else if (type.equals(Double.class)) {
            JAMSDouble v = new JAMSDouble();
            v.setValue(((Double) value).doubleValue());
            result = v;
        } else if (type.equals(String.class)) {
            JAMSString v = new JAMSString();
            v.setValue(value.toString());
            result = v;
        } else if (Geometry.class.isAssignableFrom(type)) {
            JAMSGeometry v = new JAMSGeometry((Geometry) value);
            result = v;
        } else {
            result = new jams.data.JAMSString();
            result.setValue(value.toString());
        }

        return result;
    }
}
