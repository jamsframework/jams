/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package reg.shape;

import gw.ui.util.ProxyTableModel;
import java.io.File;
import java.io.Serializable;
import java.net.URI;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;
import org.geotools.data.DataStoreFactorySpi;
import org.geotools.data.DefaultTransaction;
import org.geotools.data.FeatureStore;
import org.geotools.data.Transaction;
import org.geotools.data.shapefile.ShapefileDataStore;
import org.geotools.data.shapefile.ShapefileDataStoreFactory;
import org.geotools.feature.FeatureCollection;
import org.geotools.feature.FeatureCollections;
import org.geotools.feature.simple.SimpleFeatureBuilder;
import org.geotools.feature.simple.SimpleFeatureTypeBuilder;
import org.geotools.referencing.crs.DefaultGeographicCRS;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.feature.type.AttributeDescriptor;
import org.opengis.feature.type.Name;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

/**
 *
 * @author hbusch
 */
public class ShapeFactory {

    /**
     * create shape
     * @param tableModel - table with all data
     * @param crs - the CoordinateReferenceSystem
     * @param targetColumns - all wished columns for target shape
     * @param fileName - of the new shapefile
     * @throws java.lang.Exception
     */
    public static void createShape(
            ProxyTableModel tableModel,
            CoordinateReferenceSystem crs,
            Vector<String> targetColumns,
            String fileName)
            throws Exception {

        System.out.println("createFeatureType ..");
        SimpleFeatureType targetSchema = createFeatureType(tableModel, targetColumns, crs);
        System.out.println("getFeatureCollection ..");
        FeatureCollection<SimpleFeatureType, SimpleFeature> fc = getFeatureCollection(tableModel, targetSchema);
        System.out.println("write2Shape ..");
        write2Shape(fc, targetSchema, crs, fileName);
    }

    /**
     * create feature collection
     * @param tableModel - table with all data
     * @param targetSchema - the wished schema
     * @return feature collection
     */
    public static FeatureCollection<SimpleFeatureType, SimpleFeature> getFeatureCollection(
            ProxyTableModel tableModel,
            SimpleFeatureType targetSchema) {
        FeatureCollection<SimpleFeatureType, SimpleFeature> targetFeatureCollection = FeatureCollections.newCollection();
        String[] columnNames = tableModel.getColumnNames();
        String columnName;
        String featureId;
        int rows = tableModel.getRowCount();
        int cols = tableModel.getColumnCount();
        for (int r = 0; r < rows; r++) {

            // create a new empty feature and fill it
            featureId = Integer.toString(r+1);
            SimpleFeature targetFeature = SimpleFeatureBuilder.template(targetSchema, featureId);
            for (int c = 0; c < cols; c++) {
                columnName = columnNames[c];
                Object attrObj = tableModel.getValueAt(r, c);
                if (targetSchema.indexOf(columnName) > -1) {
                    if (attrObj != null) {
                        targetFeature.setAttribute(columnName, attrObj);
                    }
                }
            }
            targetFeatureCollection.add(targetFeature);
        }
        return targetFeatureCollection;
    }

    /**
     * create feature type
     * @param tableModel - table with all data
     * @param targetColumnNames
     * @param crs - the CoordinateReferenceSystem
     * @return simpleFeatureType
     */
    public static SimpleFeatureType createFeatureType(ProxyTableModel tableModel, Vector<String> targetColumnNames, CoordinateReferenceSystem crs) {

        SimpleFeatureTypeBuilder builder = new SimpleFeatureTypeBuilder();
        builder.setName("customType");
        builder.setCRS(crs);

        int cols = tableModel.getColumnCount();
        for (int c = 0; c < cols; c++) {
            String columnName = tableModel.getColumnName(c);
            Class colType = tableModel.getColumnClass(c);
            if (tableModel.isGeomColumn(c) || targetColumnNames.contains(columnName)) {
                System.out.println("createFeatureType. added column:" + columnName + " (" + colType + ")");
                builder.add(columnName, colType);
            }
        }
        return builder.buildFeatureType();
    }

    /**
     * write shape file
     * @param featureCollection - collection of features
     * @param schema - the desired schema
     * @param crs - the coordinate reference system
     * @param filename
     * @throws java.lang.Exception
     */
    public static void write2Shape(FeatureCollection<SimpleFeatureType, SimpleFeature> featureCollection,
            SimpleFeatureType schema,
            CoordinateReferenceSystem crs,
            String filename)
            throws Exception {

        if (crs == null) {
            crs = DefaultGeographicCRS.WGS84;
        }

        File newFile = new File(filename);
        DataStoreFactorySpi factory = new ShapefileDataStoreFactory();

        Map<String, Serializable> create = new HashMap<String, Serializable>();
        create.put("url", newFile.toURI().toURL());
        create.put("create spatial index", Boolean.TRUE);

        ShapefileDataStore newDataStore = (ShapefileDataStore) factory.createNewDataStore(create);
        newDataStore.createSchema(schema);
        newDataStore.forceSchemaCRS(crs);

        Transaction transaction = new DefaultTransaction("create");

        String typeName = newDataStore.getTypeNames()[0];
        FeatureStore<SimpleFeatureType, SimpleFeature> featureStore;
        featureStore = (FeatureStore<SimpleFeatureType, SimpleFeature>) newDataStore.getFeatureSource(typeName);

        featureStore.setTransaction(transaction);
        try {
            featureStore.addFeatures(featureCollection);
            transaction.commit();
        } catch (Exception problem) {
            problem.printStackTrace();
            transaction.rollback();
        } finally {
            transaction.close();
        }
        return;
    }

    public static Vector<String> getAttributeNames(URI shapeUri) throws Exception {
        Vector<String> names = new Vector<String>();
        ShapefileDataStore shapefile = new ShapefileDataStore(shapeUri.toURL());

        SimpleFeatureType featureType = shapefile.getSchema();
        List<AttributeDescriptor> attributeDescriptors = featureType.getAttributeDescriptors();
        for (AttributeDescriptor ad : attributeDescriptors) {
            names.add(ad.getLocalName());
        }
        return names;
    }
}
