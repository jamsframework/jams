package gw.ui.util;

import java.io.IOException;
import javax.swing.table.AbstractTableModel;
import org.geotools.data.FeatureSource;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;

/**
 *
 * @author Ian
 */
public class ShapefileTableModel extends AbstractTableModel {

    SimpleFeatureType schema;
    SimpleFeature[] features;
    
    public ShapefileTableModel(FeatureSource<SimpleFeatureType, SimpleFeature> featureSource) throws IOException {
        schema = (SimpleFeatureType)featureSource.getSchema();
        Object[] obj = featureSource.getFeatures().toArray();
        features = new SimpleFeature[obj.length];
        System.arraycopy(obj, 0, features, 0, obj.length);
    }
        
    @Override
    public int getColumnCount() {
        return schema.getTypes().size();
    }

    @Override
    public int getRowCount() {
        return features.length;
    }

    @Override
    public String getColumnName(int col) {
        return schema.getType(col).getName().getLocalPart();
    }

    @Override
    public Object getValueAt(int row, int col) {
        return features[row].getAttribute(col);
    }

    @Override
    public Class getColumnClass(int c) {
        return schema.getType(c).getBinding();
    }

    @Override
    public void setValueAt(Object value, int row, int col) {
        //data.get(row)[col] = value;
        fireTableCellUpdated(row, col);
    }

    /*public void addRow(Object[] row) {
        //data.add(row);
    }*/
}
