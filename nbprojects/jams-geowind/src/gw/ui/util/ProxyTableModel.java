/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package gw.ui.util;

import com.vividsolutions.jts.geom.MultiPoint;
import com.vividsolutions.jts.geom.MultiPolygon;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.Polygon;
import gw.util.ColorBlend;
import java.awt.Color;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableModel;

/**
 *
 * @author Ian
 */
public class ProxyTableModel extends AbstractTableModel implements TableModelListener {

    public static class ColorPair {
        Color a, b;
        ColorPair(Color x, Color y) {
            a = x;
            b = y;
        }
        
        public Color getA() {
            return a;
        }
        
        public Color getB() {
            return b;
        }
        
        public void setA(Color a) {
            this.a = a;
        }
        
        public void setB(Color b) {
            this.b = b;
        }
    }

    ArrayList<TableModel> models = new ArrayList();
    ColorPair[] tableColors;
    ArrayList<Double> columnMinValue = new ArrayList<Double>();
    ArrayList<Double> columnMaxValue = new ArrayList<Double>();

    /**
     * cache for classes
     */
    HashMap<Integer, Class> columnClass = new HashMap<Integer, Class>();

    private int highlightedColumn = -1;

    public ProxyTableModel(Color[] maxColors) {
        this.tableColors = new ColorPair[maxColors.length];
        for(int i=0; i<maxColors.length; i++) {
            tableColors[i] = new ColorPair(Color.BLACK, maxColors[i]);
        }
    }
    
    public Color getMaxColor(int i) {
        return tableColors[i].getB();
    }

    public void setMaxColor(Color col, int i) {
        tableColors[i].setB(col);
        fireTableDataChanged();
    }

    public Color getMinColor(int i) {
        return tableColors[i].getA();
    }

    public void setMinColor(Color col, int i) {
        tableColors[i].setA(col);
        fireTableDataChanged();
    }


    private double[] findColumnMinMax(TableModel m, int col) {
        double min = Double.MAX_VALUE;
        double max = Double.MIN_VALUE;
        for (int row = 0; row < m.getRowCount(); row++) {
            Object value = m.getValueAt(row, col);

            double numericValue = 0;
            if (value instanceof Number) {
                numericValue = ((Number) value).doubleValue();
            } else {
                return null;
            }
            if (min > numericValue) {
                min = numericValue;
            }
            if (max < numericValue) {
                max = numericValue;
            }
        }
        return new double[]{min, max};
    }

    private void addColumnMinMax(TableModel m) {
        //figure out min and max values of each column in the model
        for (int c = 0; c < m.getColumnCount(); c++) {
            double[] minmax = findColumnMinMax(m, c);
            if (minmax == null) {
                columnMinValue.add(null);
                columnMaxValue.add(null);
            } else {
                columnMinValue.add(minmax[0]);
                columnMaxValue.add(minmax[1]);
            }
        }
    }

    public void addTableModel(TableModel m) {
        models.add(m);
        addColumnMinMax(m);
        m.addTableModelListener(this);
        columnClass.clear();
        fireTableStructureChanged();
    }

    public ColorPair getColorsForColumn(int col) {
        //int modelIndex = getModelIndexForTableColumn(col);
        //int colorIndex = modelIndex % tableColors.length;
        int colorIndex = 0; // hb: fake, because here is some inconsistency between models, columns and colours
        return tableColors[colorIndex];
    }

    public double getMinValue(int col) {
        return this.columnMinValue.get(col);
    }

    public double getMaxValue(int col) {
        return this.columnMaxValue.get(col);
    }

    public Color getColorForRowAndColumn(int row, int col) {
        ColorPair baseColor = getColorsForColumn(col);
        if (columnMinValue.get(col) == null || columnMaxValue.get(col) == null) {
            return baseColor.getB();
        }
        double min = this.columnMinValue.get(col);
        double max = this.columnMaxValue.get(col);

        Object value = getValueAt(row, col);
        if (value instanceof Number) {
            double dval = ((Number) value).doubleValue();
            double percentage = (dval - min) / (max - min);
            return ColorBlend.mixColors(baseColor.getA(), baseColor.getB(), (float) Math.max(0.0, Math.min(1.0, percentage)));
        }

        return baseColor.getB();

    }

    private int getModelIndexForTableColumn(int col) {
        int modelIndex = 0;
        while (col >= models.get(modelIndex).getColumnCount()) {
            col -= models.get(modelIndex).getColumnCount();
            modelIndex++;
        }
        return modelIndex;
    }

    private TableModel getModelForTableColumn(int col) {
        return models.get(getModelIndexForTableColumn(col));
    }

    private int getModelColumnFromTableColumn(int col) {
        int modelIndex = 0;
        while (col >= models.get(modelIndex).getColumnCount()) {
            col -= models.get(modelIndex).getColumnCount();
            modelIndex++;
        }
        return col;
    }

    public int getTableColumnForModelColumn(TableModel model, int modelcol) {
        int tablecol = modelcol;
        int modelIndex = models.indexOf(model);
        for (int i = 0; i < modelIndex; i++) {
            tablecol += models.get(i).getColumnCount();
        }
        return tablecol;
    }

    public String[] getStringDataForColumn(int tableColumn) {
        //figure out which table model the data is in

        int mindex = getModelIndexForTableColumn(tableColumn);
        int col = getModelColumnFromTableColumn(tableColumn);
        TableModel model = models.get(mindex);
        String[] rval = new String[model.getRowCount()];
        for (int row = 0; row < model.getRowCount(); row++) {
            rval[row] =  model.getValueAt(row, col).toString();
        }
        return rval;
    }

    public Map<Integer, Double> getDataForColumn(int tableColumn) {
        //figure out which table model the data is in
        int mindex = getModelIndexForTableColumn(tableColumn);
        int col = getModelColumnFromTableColumn(tableColumn);
        TableModel model = models.get(mindex);

        Map<Integer, Double> rval = new HashMap<Integer, Double>();
        Class theColumnClass = model.getColumnClass(col);
        //System.out.println("class for column " + tableColumn + ": " + theColumnClass.getName());
        if (Number.class.isAssignableFrom(theColumnClass)) {
            for (int row = 0; row < models.get(mindex).getRowCount(); row++) {
                double value = ((Number) model.getValueAt(row, col)).doubleValue();
                rval.put(row, value);
            }
        } else {
            return null;
        }
        return rval;
    }

    @Override
    public void tableChanged(TableModelEvent e) {
        try {
            //Recalculate column min/max
            int tableColumn = this.getTableColumnForModelColumn((TableModel) e.getSource(), e.getColumn());
            double[] minMax = this.findColumnMinMax((TableModel) e.getSource(), e.getColumn());
            columnMinValue.set(tableColumn, minMax[0]);
            columnMaxValue.set(tableColumn, minMax[1]);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public int getColumnCount() {
        int count = 0;
        for (TableModel m : models) {
            count += m.getColumnCount();
        }
        return count;
    }

    @Override
    public int getRowCount() {
        if (models.size() > 0) {
            return models.get(0).getRowCount();
        }
        return 0;
    }

    @Override
    public String getColumnName(int col) {
        return getModelForTableColumn(col).getColumnName(getModelColumnFromTableColumn(col));
    }

    public String[] getColumnNames() {
        int max = getColumnCount();
        String[] columnNames = new String[max];
        String columnName;
        for (int i = 0; i < max; i++) {
            columnName = getColumnName(i);
            columnNames[i] = columnName;
        }
        return columnNames;
    }

    @Override
    public Object getValueAt(int row, int col) {
        TableModel localModel = getModelForTableColumn(col);
        int localColumn = getModelColumnFromTableColumn(col);
        return localModel.getValueAt(row, localColumn);
    }

    @Override
    public Class getColumnClass(int c) {

        Integer col = new Integer(c);
        Class theClass = columnClass.get(col);
        if (theClass != null)
            return theClass;
        else {
            int modelColumn = getModelColumnFromTableColumn(c);
            TableModel tableModel = getModelForTableColumn(c);
            // System.out.println("column: " + c + "->" + modelColumn + ", type of model: " + tableModel.getClass().getName());
            theClass = tableModel.getColumnClass(modelColumn);
            columnClass.put(col, theClass);
            return theClass;
        }
    }

    public boolean isGeomColumn(int i) {
        Class theClass = getColumnClass(i);
        if (theClass.equals(Polygon.class)) {
            return true;
        }
        if (theClass.equals(Point.class)) {
            return true;
        }
        if (theClass.equals(MultiPolygon.class)) {
            return true;
        }
        if (theClass.equals(MultiPoint.class)) {
            return true;
        }
        return false;
    }

    private void checkMinMax(Object value, int col) {

        //Fix min/max values
        if (value instanceof Number) {
            double num = ((Number) value).doubleValue();
            if (num > this.columnMaxValue.get(col)) {
                columnMaxValue.set(col, num);
            }
            if (num < this.columnMinValue.get(col)) {
                columnMinValue.set(col, num);
            }
        }
    }

    @Override
    public void setValueAt(Object value, int row, int col) {
        TableModel localModel = getModelForTableColumn(col);
        int localColumn = getModelColumnFromTableColumn(col);
        localModel.setValueAt(value, row, localColumn);
        //data.get(row)[col] = value;
        fireTableCellUpdated(row, col);
    }

    public int getHighlightedColumn() {
        return highlightedColumn;
    }

    public void setHighlightedColumn(int highlightedColumn) {
        this.highlightedColumn = highlightedColumn;
    }
}
