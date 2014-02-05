/*
 * ShapeFileWriter.java
 * Created on 05.02.2014, 15:57:22
 *
 * This file is part of JAMS
 * Copyright (C) FSU Jena
 *
 * JAMS is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public License
 * as published by the Free Software Foundation; either version 3
 * of the License, or (at your option) any later version.
 *
 * JAMS is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with JAMS. If not, see <http://www.gnu.org/licenses/>.
 *
 */
package jams.explorer.tools;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.net.MalformedURLException;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.geotools.data.DataStoreFactorySpi;
import org.geotools.data.DefaultTransaction;
import org.geotools.data.FeatureSource;
import org.geotools.data.FeatureStore;
import org.geotools.data.Transaction;
import org.geotools.data.shapefile.ShapefileDataStore;
import org.geotools.data.shapefile.ShapefileDataStoreFactory;
import org.geotools.feature.FeatureCollection;
import org.geotools.feature.FeatureCollections;
import org.geotools.feature.FeatureIterator;
import org.geotools.feature.simple.SimpleFeatureBuilder;
import org.geotools.feature.simple.SimpleFeatureTypeBuilder;
import org.geotools.referencing.CRS;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.feature.type.AttributeDescriptor;
import org.opengis.feature.type.FeatureType;
import org.opengis.feature.type.PropertyDescriptor;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

/**
 *
 * @author Sven Kralisch <sven.kralisch at uni-jena.de>
 */
public class ShapeFileWriter {

    /**
     * the the URI of the parent
     */
    private URI inShapefileURI = null;
    /**
     * the the URI of the child
     */
    private URI outShapefileURI = null;
    /**
     * the key column of the target structure
     */
    private String targetKeyName = null;
    /**
     * the name of the columns
     */
    private String[] names;
    /**
     * the ids
     */
    private double[] ids;
    /**
     * the data corresponding to ids
     */
    double[][] data;

    public ShapeFileWriter() {
    }

    public String getTargetKeyName() {
        return targetKeyName;
    }

    public void setTargetKeyName(String targetKeyName) {
        this.targetKeyName = targetKeyName;
    }

    public double[][] getData() {
        return data;
    }

    public void setData(double[][] data) {
        this.data = data;
    }

    public double[] getIds() {
        return ids;
    }

    public void setIds(double[] ids) {
        this.ids = ids;
    }

    public String[] getNames() {
        return names;
    }

    public void setNames(String[] names) {
        this.names = names;
    }

    public URI getInShapefileURI() {
        return inShapefileURI;
    }

    public void setInShapefileURI(URI shapefileURI) {
        this.inShapefileURI = shapefileURI;
    }

    public URI getOutShapefileURI() {
        return outShapefileURI;
    }

    public void setOutShapefileURI(URI outShapefileURI) {
        this.outShapefileURI = outShapefileURI;
    }

    public void writeShape() throws MalformedURLException, IOException {

        File targetFile = new File(outShapefileURI);
        if (!targetFile.canWrite() && !targetFile.getParentFile().canWrite()) {
            Logger.getLogger(ShapeFileWriter.class.getName()).log(Level.WARNING, "Target file " + targetFile.getPath()
                    + " is not writable. No output written!");
            return;
        }

        // create map ids -> data
        Map<Long, double[]> idMap = new HashMap();
        int n = data.length;
        for (int i = 0; i < ids.length; i++) {
            double[] values = new double[n];
            for (int j = 0; j < n; j++) {
                values[j] = data[j][i];
            }
            idMap.put((long) ids[i], values);
        }

        // get feature results
        ShapefileDataStore store = new ShapefileDataStore(inShapefileURI.toURL());
        // feature type name is defaulted to the name of shapefile (without extension)
        String typeName = store.getTypeNames()[0];
        FeatureSource source = store.getFeatureSource();
        FeatureCollection featureCollection = source.getFeatures();

        SimpleFeatureType sourceSchema = (SimpleFeatureType) featureCollection.getSchema();
        if (sourceSchema.getDescriptor(targetKeyName) == null) {
            Logger.getLogger(ShapeFileWriter.class.getName()).log(Level.WARNING, "Attribute name " + targetKeyName
                    + " does not exist in Shapefile " + new File(inShapefileURI) + ". No output written!");
            return;
        }
        SimpleFeatureType targetSchema = createFeatureType(sourceSchema);

//        for (AttributeDescriptor ad : newSchema.getAttributeDescriptors()) {
//            System.out.println(ad.toString());
//        }
        FeatureCollection<SimpleFeatureType, SimpleFeature> targetFeatureCollection = FeatureCollections.newCollection();

        int i = 0;
        FeatureIterator fi = featureCollection.features();
        while (fi.hasNext()) {

            SimpleFeature feature = (SimpleFeature) fi.next();
            SimpleFeature targetFeature = SimpleFeatureBuilder.template(targetSchema, Integer.toString(++i));

            for (PropertyDescriptor d : sourceSchema.getDescriptors()) {
                targetFeature.setAttribute(d.getName(), feature.getAttribute(d.getName()));
            }

            // get additional data for the current feature
            String idString = feature.getAttribute(targetKeyName).toString();
            long id = Long.parseLong(idString);
            double[] values = idMap.get(id);
            if (values != null) {
                for (int j = 0; j < n; j++) {
                    targetFeature.setAttribute(names[j], values[j]);
                }
            }

            targetFeatureCollection.add(targetFeature);
        }

        // get feature type to create new shapefile
        FeatureType ft = source.getSchema();
        // create new shapefile data store
        ShapefileDataStore newShapefileDataStore = new ShapefileDataStore(outShapefileURI.toURL());

        // create the schema using from the original shapefile
        newShapefileDataStore.createSchema(targetSchema);

        // grab the data source from the new shapefile data store
        FeatureSource newFeatureSource = newShapefileDataStore.getFeatureSource();

        // downcast FeatureSource to specific implementation of FeatureStore
        FeatureStore newFeatureStore = (FeatureStore) newFeatureSource;

        // accquire a transaction to create the shapefile from FeatureStore
        Transaction t = newFeatureStore.getTransaction();

        // add features got from the old file
        newFeatureStore.addFeatures(targetFeatureCollection);

        t.commit();
        t.close();

    }

    private SimpleFeatureType createFeatureType(FeatureType inSchema) {
        SimpleFeatureTypeBuilder builder = new SimpleFeatureTypeBuilder();
        builder.setName("jadeType");
        builder.setCRS(inSchema.getCoordinateReferenceSystem());

        // add all existing attributes
        for (PropertyDescriptor d : inSchema.getDescriptors()) {
            builder.add((AttributeDescriptor) d);
        }

        // add additional attributes
        for (String name : names) {
            builder.add(name, Double.class);
        }

        return builder.buildFeatureType();
    }

}
