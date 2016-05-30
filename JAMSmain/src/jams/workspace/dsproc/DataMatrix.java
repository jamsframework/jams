package jams.workspace.dsproc;

import Jama.Matrix;
import java.util.HashMap;

public class DataMatrix extends Matrix {

    private Object[] ids;

    private String[] attributeIDs;

    private HashMap<String, Integer> idPositions;

    public DataMatrix(double[][] data, Object[] ids, String[] attributeIDs) {
        super(data);
        this.ids = ids;
        this.attributeIDs = attributeIDs;
    }

    public DataMatrix(Matrix matrix, Object[] ids, String[] attributeIDs) {
        super(matrix.getArray());
        this.ids = ids;
        this.attributeIDs = attributeIDs;
    }

    @Override
    public DataMatrix plus(Matrix other) {
        Matrix result = super.plus(other);
        return new DataMatrix(result, ids, attributeIDs);
    }

    @Override
    public DataMatrix times(double d) {
        Matrix result = super.times(d);
        return new DataMatrix(result, ids, attributeIDs);
    }

    /**
     * @return the id
     */
    public Object[] getIds() {
        return ids;
    }

    public void elementMultiply(DataMatrix m) {

        int colCount = this.getColumnDimension();
        int rowCount = this.getRowDimension();
        double[][] data1 = this.getArray();
        double[][] data2 = m.getArray();

        for (int i = 0; i < colCount; i++) {
            for (int j = 0; j < rowCount; j++) {
                data1[j][i] = data1[j][i] * data2[j][i];
            }
        }
    }

    public void elementDivide(DataMatrix m) {

        int colCount = this.getColumnDimension();
        int rowCount = this.getRowDimension();
        double[][] data1 = this.getArray();
        double[][] data2 = m.getArray();

        for (int i = 0; i < colCount; i++) {
            for (int j = 0; j < rowCount; j++) {
                data1[j][i] = data1[j][i] / data2[j][i];
            }
        }
    }

    public double[] getAvgRow() {

        double[] result = new double[this.getColumnDimension()];
        int colCount = this.getColumnDimension();
        int rowCount = this.getRowDimension();
        double[][] data = this.getArray();

        for (int i = 0; i < colCount; i++) {
            result[i] = 0;
            int counter = 0;
            for (int j = 0; j < rowCount; j++) {
                if (data[j][i] != -9999 && Double.isFinite(data[j][i])) {
                    result[i] += data[j][i];
                    counter++;
                }
            }
            result[i] /= counter;
        }

        return result;
    }

    public double[] getAvgCol() {

        double[] result = new double[this.getRowDimension()];
        int colCount = this.getColumnDimension();
        int rowCount = this.getRowDimension();
        double[][] data = this.getArray();

        for (int i = 0; i < rowCount; i++) {
            result[i] = 0;
            int counter = 0;
            for (int j = 0; j < colCount; j++) {
                if (data[i][j] != -9999 && Double.isFinite(data[i][j])) {
                    result[i] += data[i][j];
                    counter++;
                }
            }
            result[i] /= counter;
        }

        return result;
    }

    public double[] getSumRow() {

        double[] result = new double[this.getColumnDimension()];
        int colCount = this.getColumnDimension();
        int rowCount = this.getRowDimension();
        double[][] data = this.getArray();

        for (int i = 0; i < colCount; i++) {
            result[i] = 0;
            for (int j = 0; j < rowCount; j++) {
                result[i] += data[j][i];
            }
        }

        return result;
    }

    public int getIDPosition(String id) {

        if (idPositions == null) {
            int i = 0;
            idPositions = new HashMap<String, Integer>();
            for (Object o : ids) {
                idPositions.put(o.toString(), i);
                i++;
            }
        }

        return idPositions.get(id);

//        int i = 0;
//        boolean found = false;
//        for (Object o : ids) {
//            if (o.toString().equals(id)) {
//                found = true;
//                break;
//            }
//            i++;
//        }
//        if (found) {
//            return i;
//        } else {
//            return -1;
//        }
    }

    public double[] getRow(int position) {
        return getArray()[position];
    }

    public double[] getCol(int position) {
        double array[][] = this.getArray();
        int n = array.length;
        double[] column = new double[n];
        for (int i = 0; i < n; i++) {
            column[i] = array[i][position];
        }
        return column;
    }

    public void output() {
        System.out.println(this.getIds()[0].getClass());
        for (Object o : this.getIds()) {
            System.out.print(o + " ");
        }
        System.out.println();
//        this.print(5, 3);
    }

    public boolean equals(DataMatrix mat, double accuracy) {
        int N = this.getRowDimension();
        int M = this.getColumnDimension();
        if (N != mat.getRowDimension()
                || M != mat.getColumnDimension()) {
            return false;
        }
        for (int i = 0; i < M; i++) {
            for (int j = 0; j < N; j++) {
                if (Math.abs(this.get(i, j) - mat.get(i, j)) > accuracy) {
                    return false;
                }
            }
        }
        return true;
    }

    public static void main(String[] args) {
        double[][] x = {{1, 2, 3}, {4, 5, 6}, {8, 10, 12}};
        String[] ids = {"a", "b", "c"};
        String[] atributeids = {"x", "y", "z"};
        DataMatrix dm = new DataMatrix(x, ids, atributeids);

        for (double v : dm.getAvgRow()) {
            System.out.print(v + " ");
        }

        System.out.println("");

        for (double v : dm.getRow(dm.getIDPosition("c"))) {
            System.out.print(v + " ");
        }

        System.out.println("");

    }

    /**
     * @return the attributeIDs
     */
    public String[] getAttributeIDs() {
        return attributeIDs;
    }

    /**
     * @param attributeIDs the attributeIDs to set
     */
    public void setAttributeIDs(String[] attributeIDs) {
        this.attributeIDs = attributeIDs;
    }
}
