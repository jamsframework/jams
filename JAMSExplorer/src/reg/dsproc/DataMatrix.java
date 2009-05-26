package reg.dsproc;

import Jama.Matrix;

public class DataMatrix extends Matrix {

    private Object[] ids;
    private String[] attributeIDs;

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

    public double[] getAvgRow() {

        double[] result = new double[this.getColumnDimension()];
        int colCount = this.getColumnDimension();
        int rowCount = this.getRowDimension();
        double[][] data = this.getArray();

        for (int i = 0; i < colCount; i++) {
            result[i] = 0;
            for (int j = 0; j < rowCount; j++) {
                result[i] += data[j][i];
            }
            result[i] /= rowCount;
        }

        return result;
    }

    public int getIDPosition(String id) {
        int i = 0;
        boolean found = false;
        for (Object o : ids) {
            if (o.toString().equals(id)) {
                found = true;
                break;
            }
            i++;
        }
        if (found) {
            return i;
        } else {
            return -1;
        }
    }

    public double[] getRow(int position) {
        return getArray()[position];
    }

    public void output() {
        System.out.println(this.getIds()[0].getClass());
        for (Object o : this.getIds()) {
            System.out.print(o + " ");
        }
        System.out.println();
        this.print(5, 3);
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
