/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package reg.shape;

import com.vividsolutions.jts.geom.*;
import gw.ui.util.ProxyTableModel;
import java.io.File;
import java.io.Serializable;
import java.net.URI;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
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
import org.geotools.referencing.CRS;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.feature.type.AttributeDescriptor;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

/**
 *
 * @author hbusch
 */
public class ShapeFactory {

    public static final String ID = "ID";
    public static final String ELEVATION = "ELEVATION";
    public static final String defaultCRS = "EPSG:31468";

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
            featureId = Integer.toString(r + 1);
            SimpleFeature targetFeature = SimpleFeatureBuilder.template(targetSchema, featureId);
            for (int c = 0; c < cols; c++) {
                columnName = columnNames[c];
                Object attrObj = tableModel.getValueAt(r, c);
                if (targetSchema.indexOf(columnName) > -1) {
                    if (attrObj != null) {
                        //System.out.println("getFeatureCollection. added attribute:" + columnName + " (" + attrObj.getClass().getName() + "): " + attrObj);
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
            if (tableModel.isGeomColumn(c) || targetColumnNames == null || targetColumnNames.contains(columnName)) {
                System.out.println("createFeatureType. added column:" + columnName + " (" + colType + ")");
                builder.add(columnName, colType);
            }
        }
        return builder.buildFeatureType();
    }

    /**
     * create feature type with id, geom and some attributes
     *
     * @param theGeomClass - Point/Polygon/MultiPolygon
     * @param attributes - optional
     * @param crs - the Coordinate Reference System
     * @return feature type
     */
    public static SimpleFeatureType createSimpleFeatureType(Class theGeomClass, Map<String, Class> attributes, CoordinateReferenceSystem crs) {

        SimpleFeatureTypeBuilder builder = new SimpleFeatureTypeBuilder();
        String typeName = theGeomClass.getSimpleName();
        builder.setName(typeName + "Type");
        builder.setCRS(crs);

        builder.add(typeName, theGeomClass);
        builder.add(ID, Long.class);

        if (attributes != null) {
            Set<String> keys = attributes.keySet();
            for (String key : keys) {
                builder.add(key, attributes.get(key));
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
            crs = CRS.decode(defaultCRS);
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

    /**
     * get attribute names of shape-file
     * @param shapeUri
     * @return names of attributes
     * @throws Exception
     */
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

    /**
     * create a dummy shape with one point in it
     * @param lat
     * @param lon
     * @param elevation
     * @param directoryName
     * @return complete file name, if it is successful, otherwise null
     */
    @SuppressWarnings("deprecation")
    public static String createShapeFromPoint(double lat, double lon, double elevation, String directoryName)
            throws Exception {
        String fileName = directoryName + File.separator + "point.shp";

            CoordinateReferenceSystem crs = CRS.decode(defaultCRS);

        // create feature type
        Class theGeomClass = Point.class;
        HashMap<String, Class> attributes = new HashMap<String, Class>();
        attributes.put(ELEVATION, Double.class);
        SimpleFeatureType schema = createSimpleFeatureType(theGeomClass, attributes, crs);

        // create a new feature and fill it
        Long id = new Long(1);
        String featureId = Long.toString(id);
        SimpleFeature theFeature = SimpleFeatureBuilder.template(schema, featureId);

        Coordinate coordinate = new Coordinate(lon, lat, elevation);
        Point point = new Point(coordinate, null, -1);
        theFeature.setAttribute(theGeomClass.getSimpleName(), point);

        theFeature.setAttribute(ID, id);
        theFeature.setAttribute(ELEVATION, new Double(elevation));

        // create feature collection
        FeatureCollection<SimpleFeatureType, SimpleFeature> featureCollection = FeatureCollections.newCollection();
        featureCollection.add(theFeature);

        // write to file
        try {
            write2Shape(featureCollection, schema, null, fileName);
            return fileName;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * create a shape file from grid
     * @param lat1
     * @param lon1
     * @param lat2
     * @param lon2
     * @param distance (in m)
     * @param directoryName
     * @return complete file name, if it is successful, otherwise null
     */
    public static String createShapeFromGrid(double lat1, double lon1, double lat2, double lon2, double distance, String directoryName, boolean coordinatesAsDegree)
            throws Exception {
        String fileName = directoryName + File.separator + "grid.shp";
        Class theGeomClass = Polygon.class;
        double elevation = 400; // unfortunately we don't have a real value.
        HashMap<String, Class> attributes = new HashMap<String, Class>();
        attributes.put(ELEVATION, Double.class);

        // create feature type
        CoordinateReferenceSystem crs = CRS.decode(defaultCRS);
        SimpleFeatureType schema = createSimpleFeatureType(theGeomClass, attributes, crs);

        Long id;
        String featureId;
        SimpleFeature theFeature;
        FeatureCollection<SimpleFeatureType, SimpleFeature> featureCollection = FeatureCollections.newCollection();

        int i = 1;
        Vector<Polygon> polygons = createPolygons(lat1, lon1, lat2, lon2, distance, coordinatesAsDegree);
        for (Polygon polygon : polygons) {

            id = new Long(i);
            featureId = Long.toString(id);
            theFeature = SimpleFeatureBuilder.template(schema, featureId);
            theFeature.setAttribute(theGeomClass.getSimpleName(), polygon);

            theFeature.setAttribute(ID, id);
            theFeature.setAttribute(ELEVATION, new Double(elevation));
            featureCollection.add(theFeature);
            i++;
        }
        // write to file
        try {
            write2Shape(featureCollection, schema, null, fileName);
            return fileName;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * creates a polygon by determined edges
     * @param workLon
     * @param workLat
     * @param nextLon
     * @param nextLat
     * @return polygon
     */
    @SuppressWarnings("deprecation")
    private static Polygon createPolygon(double workLon, double workLat, double nextLon, double nextLat) {
        Polygon polygon;
        LinearRing linearRing;
        Coordinate[] points;
        Coordinate c1, c2, c3, c4, c5;
        c1 = new Coordinate(workLon, workLat);
        c2 = new Coordinate(workLon, nextLat);
        c3 = new Coordinate(nextLon, nextLat);
        c4 = new Coordinate(nextLon, workLat);
        c5 = c1;
        points = new Coordinate[5];
        points[0] = c1;
        points[1] = c2;
        points[2] = c3;
        points[3] = c4;
        points[4] = c5;
        linearRing = new LinearRing(points, null, -1);
        polygon = new Polygon(linearRing, null, -1);
        return polygon;
    }

    /**
     * create a list of polygons
     *
     * @param latFrom
     * @param lonFrom
     * @param latTo
     * @param lonTo
     * @param distance
     * @return list of polygons
     */
    private static Vector<Polygon> createPolygons(double latFrom, double lonFrom, double latTo, double lonTo, double distance, boolean coordinatesAsDegree) {

        Vector<Polygon> result = new Vector<Polygon>();
        double workLat, workLon, nextLat, nextLon;
        Polygon polygon;

        workLat = latFrom;
        while (workLat > latTo) {
            nextLat = getNextLat(workLat, distance, coordinatesAsDegree);
            workLon = lonFrom;
            while (workLon < lonTo) {
                nextLon = getNextLon(workLon, distance, coordinatesAsDegree);
                //System.out.println("createPolygons: " + workLon + ", " + workLat + ", " + nextLon + ", " + nextLat);
                polygon = createPolygon(workLon, workLat, nextLon, nextLat);
                result.add(polygon);

                workLon = nextLon;
            }
            workLat = nextLat;
        }
        return result;
    }

    /**
     * this is a dirty hack due to pressure of time
     * but it works :-)
     *
     * @param actLon (in degree or in meter)
     * @param distance (in meter)
     * @param coordinateAsDegree
     * @return nextLon
     */
    private static double getNextLon(double actLon, double distance, boolean coordinateAsDegree) {
        if (coordinateAsDegree) {
            double degreeMeters = 111120.0;
            return actLon + (distance / degreeMeters); // 1 degree = 111120 meter

        } else {
            return actLon + distance;
        }
    }

    /**
     * this is a dirty hack due to pressure of time
     * but at north hemissphere it works :-)
     *
     * @param actLat (in degree or in meter)
     * @param distance (in meter)
     * @param coordinateAsDegree
     * @param distance
     * @return nextLon
     */
    private static double getNextLat(double actLat, double distance, boolean coordinateAsDegree) {
        if (coordinateAsDegree) {
            double degreeMeters = 111120.0 * Math.cos(Math.toRadians(actLat)); // 111120 * cosinus(lon)
            return actLat - (distance / degreeMeters); // north
        } else {
            return actLat - distance; // north
        }
    }

}
