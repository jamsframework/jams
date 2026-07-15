/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jams.worldwind.ui.model;

import gov.nasa.worldwind.render.SurfacePolygons;
import gov.nasa.worldwind.render.SurfacePolylines;
import jams.worldwind.data.shapefile.JamsShapeAttributes;
import jams.worldwind.ui.view.ShapefileAttributesView;
import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.table.DefaultTableModel;

/**
 *
 * @author Ronny Berndt <ronny.berndt at uni-jena.de>
 */
public class ShapefilePropertiesModel extends DefaultTableModel {

    private Object thePoly;

    public ShapefilePropertiesModel(SurfacePolygons poly) {
        this.thePoly = poly;
        this.fillTablewithData();
    }
    
    public ShapefilePropertiesModel(SurfacePolylines poly) {
        this.thePoly = poly;
        this.fillTablewithData();
    }

    private void fillTablewithData() {
        this.fill();
    }

    private void fill() {
        JamsShapeAttributes sattr = null;
        if (this.thePoly instanceof SurfacePolygons) {
            sattr = (JamsShapeAttributes) ((SurfacePolygons) this.thePoly).getAttributes();
        } else if (this.thePoly instanceof SurfacePolylines) {
            sattr = (JamsShapeAttributes) ((SurfacePolylines) this.thePoly).getAttributes();
        }
        if (sattr != null) {
            BeanInfo beanInfo;
            HashMap<String, Object> data = new HashMap<>();
            try {
                beanInfo = Introspector.getBeanInfo(sattr.getClass().getSuperclass());
                for (PropertyDescriptor pd : beanInfo.getPropertyDescriptors()) {
                    if (pd.getReadMethod() != null && !"class".equals(pd.getName())) {
                        data.put(pd.getDisplayName(), pd.getReadMethod().invoke(sattr));
                    }
                }
            } catch (IntrospectionException | IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
                System.err.println(ex);
            }
            int columnCount = data.size();
            Vector v = new Vector(columnCount);
            for (Map.Entry<String, Object> e : data.entrySet()) {
                super.addColumn(e.getKey());
                //Shapefile entries
                v.add(e.getValue());
            }
            super.addRow(v);
        }
    }

    @Override
    public void setValueAt(Object aValue, int row, int column) {
        try {
            String propertyName = getColumnName(column);
            JamsShapeAttributes sattr = null;
            if (this.thePoly instanceof SurfacePolygons) {
                SurfacePolygons obj = (SurfacePolygons) this.thePoly;
                sattr = (JamsShapeAttributes) obj.getAttributes();
            } else if (this.thePoly instanceof SurfacePolylines) {
                SurfacePolylines obj = (SurfacePolylines) this.thePoly;
                sattr = (JamsShapeAttributes) obj.getAttributes();
            }
            //SurfacePolygons obj = this.thePoly;
            //sattr = (JamsShapeAttributes) obj.getAttributes();
            if (sattr != null) {
                PropertyDescriptor pd = new PropertyDescriptor(propertyName, sattr.getClass().getSuperclass());
                if (pd.getWriteMethod() != null) {
                    pd.getWriteMethod().invoke(sattr, aValue);  //this.castCellTypeToObject(aValue, propertyName));
                    //Vector rowVector = (Vector) dataVector.elementAt(row);
                    super.setValueAt(aValue, row, column);//setElementAt(aValue, column);
                    fireTableCellUpdated(row, column);
                }
            }
        } catch (IntrospectionException | IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
            Logger.getLogger(ShapefileAttributesView.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @Override
    public boolean isCellEditable(int row, int column) {
        return true;
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
