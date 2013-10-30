/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package jams.worldwind.ui.model;

import gov.nasa.worldwind.render.Material;
import jams.worldwind.shapefile.JamsShapeAttributes;
import jams.worldwind.ui.view.PropertyEditorView;
import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
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
    private List<String> columnIdentifiers;
    private List<Object> data;
    private int shapeAttributes = 0;

    private Object theObject;

    public PropertyEditorModel(Object cls, HashMap<String, Object> mapData) {
        this.theObject = cls;
        Set<Entry<String, Object>> d = ((JamsShapeAttributes) this.theObject).getShapeFileRecord().getAttributes().getEntries();
        int columnSize = d.size() + mapData.size();
        this.shapeAttributes = d.size() - 1;
        this.columnIdentifiers = new ArrayList<>(columnSize - 1);
        this.data = new ArrayList<>();
        Object[] o = new Object[columnSize];
        int count = 0;
        for (Map.Entry<String, Object> e : d) {
            super.addColumn(e.getKey());
            o[count] = e.getValue();
            count++;
        }
        for (Map.Entry<String, Object> e : mapData.entrySet()) {
            super.addColumn(e.getKey());
            o[count] = e.getValue();
            count++;
        }
        super.addRow(o);
    }

    @Override
    public boolean isCellEditable(int row, int column) {
        return column > this.shapeAttributes;
    }

    @Override
    public void setValueAt(Object aValue, int row, int column) {
        try {
            String propertyName = getColumnName(column);
            PropertyDescriptor pd = new PropertyDescriptor(propertyName, this.theObject.getClass());
            if (pd.getWriteMethod() != null) {
                pd.getWriteMethod().invoke(this.theObject, aValue);  //this.castCellTypeToObject(aValue, propertyName));
                Vector rowVector = (Vector) dataVector.elementAt(row);
                rowVector.setElementAt(aValue, column);
                fireTableCellUpdated(row, column);
            }
        } catch (IntrospectionException | IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
            Logger.getLogger(PropertyEditorView.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /*
     * Cast cell object if reflection method of proerty is primitiv
     * data type to prevent IllegalArgumentException during invoke
     * method
     */
    /*
    private Object castCellTypeToObject(Object aValue, String propertyName) {
        try {
            PropertyDescriptor pd = new PropertyDescriptor(propertyName, this.theObject.getClass());
            Class<?>[] cls = pd.getWriteMethod().getParameterTypes();
            Object castedObject = aValue;
            for (int i = 0; i < cls.length; i++) {
                //System.out.println("TYPE STRING: " + cls[i].toString());
                switch (cls[i].getName()) {
                    case "byte":
                        castedObject = Byte.parseByte(aValue.toString());
                        break;
                    case "short":
                        castedObject = Short.parseShort(aValue.toString());
                        break;
                    case "int":
                        castedObject = Integer.parseInt(aValue.toString());
                        break;
                    case "long":
                        castedObject = Long.parseLong(aValue.toString());
                        break;
                    case "float":
                        castedObject = Float.parseFloat(aValue.toString());
                        break;
                    case "double":
                        castedObject = Double.parseDouble(aValue.toString());
                        break;
                    case "boolean":
                        castedObject = Boolean.parseBoolean(aValue.toString());
                        break;
                    default:
                        break;
                }
            }
            if (castedObject == null) {
                return "null";
            }
            return castedObject;
        } catch (IntrospectionException | IllegalArgumentException ex) {
            Logger.getLogger(PropertyEditorView.class.getName()).log(Level.SEVERE, null, ex);
        }
        System.out.println(aValue.getClass());
        return aValue;
    }*/

    @Override
    public Class getColumnClass(int c) {
        Object o = getValueAt(0, c);
        if (o != null) {
            return o.getClass();
        } else {
            return Object.class;
        }
    }
}
