package jams.worldwind.ui.model;

import gov.nasa.worldwind.render.SurfacePolygons;
import jams.worldwind.shapefile.JamsShapeAttributes;
import jams.worldwind.ui.view.PropertyEditorView;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.table.DefaultTableModel;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Ronny Berndt <ronny.berndt@uni-jena.de>
 */
public class PropertyEditorModel extends DefaultTableModel {

    private static final org.slf4j.Logger logger = LoggerFactory.getLogger(PropertyEditorModel.class);

    private int shapeAttributes = 0;
    final private List<?> theObjects;

    /**
     * Constructs a <code>PropertyEditorModel</code> with a list
     * of selected Polygons on top of the globe.
     * @param polyList list of selected polygons
     * @param attributesList list key/value pairs for Java Reflection access
     * 
     */
    public PropertyEditorModel(List<?> polyList, HashMap<String, Object> attributesList) {
        this.theObjects = new ArrayList(polyList);
        this.setColumnIdentifiers(polyList, attributesList);
        for (Object o : polyList) {
            SurfacePolygons s = (SurfacePolygons) o;
            JamsShapeAttributes sattr = (JamsShapeAttributes) s.getAttributes();
            Set<Entry<String, Object>> d = sattr.getShapeFileRecord().getAttributes().getEntries();
            int columnSize = d.size() + attributesList.size();
            this.shapeAttributes = d.size() - 1;
            Vector v = new Vector(columnSize);
            int count = 0;
            //Shapefile entries
            for (Map.Entry<String, Object> e : d) {
                v.add(e.getValue());
            }
            //SurfacePolygons entries
            for (Map.Entry<String, Object> e : attributesList.entrySet()) {
                v.add(e.getValue());
            }
            super.addRow(v);
        }
    }

    private void setColumnIdentifiers(List<?> polyList, HashMap<String, Object> attributesList) {
        SurfacePolygons s = (SurfacePolygons) polyList.get(0);
        JamsShapeAttributes sattr = (JamsShapeAttributes) s.getAttributes();
        Set<Entry<String, Object>> d = sattr.getShapeFileRecord().getAttributes().getEntries();
        for (Object o : polyList) {
            for (Map.Entry<String, Object> e : d) {
                super.addColumn(e.getKey());
            }
            //SurfacePolygons entries
            for (Map.Entry<String, Object> e : attributesList.entrySet()) {
                super.addColumn(e.getKey());
            }
            break;
        }
    }

    @Override
    public boolean isCellEditable(int row, int column) {
        return column > this.shapeAttributes;
    }

    @Override
    public void setValueAt(Object aValue, int row, int column) {
        try {
            String propertyName = getColumnName(column);
            SurfacePolygons obj = (SurfacePolygons)theObjects.get(row);
            JamsShapeAttributes sattr = (JamsShapeAttributes) obj.getAttributes();
            PropertyDescriptor pd = new PropertyDescriptor(propertyName, sattr.getClass().getSuperclass());
            if (pd.getWriteMethod() != null) {
                pd.getWriteMethod().invoke(sattr, aValue);  //this.castCellTypeToObject(aValue, propertyName));
                //Vector rowVector = (Vector) dataVector.elementAt(row);
                super.setValueAt(aValue, row, column);//setElementAt(aValue, column);
                fireTableCellUpdated(row, column);
            }
        } catch (IntrospectionException | IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
            Logger.getLogger(PropertyEditorView.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @Override
    public Class getColumnClass(int columnIndex) {
        Object o = getValueAt(0, columnIndex);
        if (o != null) {
            return o.getClass();
        } else {
            return Object.class;
        }
    }
}