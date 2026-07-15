package jams.worldwind.ui.model;

import gov.nasa.worldwind.render.AbstractSurfaceShape;
import gov.nasa.worldwind.render.SurfacePolygons;
import jams.worldwind.data.shapefile.JamsShapeAttributes;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.Vector;
import javax.swing.table.DefaultTableModel;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Ronny Berndt <ronny.berndt at uni-jena.de>
 */
public class ShapefileAttributesModel extends DefaultTableModel {

    private static final org.slf4j.Logger logger = LoggerFactory.getLogger(ShapefileAttributesModel.class);

    final private List<?> theObjects;

    /**
     * Constructs a <code>ShapefileAttributesModel</code> with a list of
     * selected Polygons on top of the globe.
     *
     * @param polyList list of selected polygons
     *
     */
    public ShapefileAttributesModel(List<?> polyList) {
        this.theObjects = new ArrayList(polyList);
        this.fillTablewithData();
    }

    private void fillTablewithData() {
        this.setColumnIdentifiers();
        this.addRows();
    }

    private void setColumnIdentifiers() {
        //first column for Shapefile attributes
        super.addColumn("PROPERTIES");
        
        
        //SurfacePolygons s = (SurfacePolygons) this.theObjects.get(0);
        //JamsShapeAttributes sattr = (JamsShapeAttributes) s.getAttributes();
        
        AbstractSurfaceShape s = (AbstractSurfaceShape)this.theObjects.get(0);
        JamsShapeAttributes sattr = (JamsShapeAttributes) s.getAttributes();
        
        Set<Entry<String, Object>> d = sattr.getShapeFileRecord().getAttributes().getEntries();
        for (Object o : this.theObjects) {
            for (Map.Entry<String, Object> e : d) {
                super.addColumn(e.getKey());
            }
            break;
        }
    }

    private void addRows() {
        for (Object o : this.theObjects) {
            
            //SurfacePolygons s = (SurfacePolygons) o;
            //JamsShapeAttributes sattr = (JamsShapeAttributes) s.getAttributes();
            
            AbstractSurfaceShape s = (AbstractSurfaceShape) o;
            JamsShapeAttributes sattr = (JamsShapeAttributes) s.getAttributes();
            
            Set<Entry<String, Object>> d = sattr.getShapeFileRecord().getAttributes().getEntries();
            //Number of Shapefile entries + first Column
            int columnCount = d.size() + 1;
            //this.shapeAttributes = d.size() - 1;
            Vector v = new Vector(columnCount);
            int count = 0;
            v.add(s);
            //Shapefile entries
            for (Map.Entry<String, Object> e : d) {
                v.add(e.getValue());
            }
            super.addRow(v);
        }
    }
    
    @Override
    public boolean isCellEditable(int row, int column) {
        return column == 0;
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
