package reg.dsproc;

import Jama.Matrix;

public class DataMatrix extends Matrix {

    private Object[] ids;

    DataStoreProcessor outer;

    public DataMatrix(double[][] data, Object[] ids, DataStoreProcessor outer) {
        super(data);
        this.outer = outer;
        this.ids = ids;
    }

    public DataMatrix(Matrix matrix, Object[] ids, DataStoreProcessor outer) {
        super(matrix.getArray());
        this.outer = outer;
        this.ids = ids;
    }

    @Override
    public DataMatrix plus(Matrix other) {
        Matrix result = super.plus(other);
        return new DataMatrix(result, ids, outer);
    }

    @Override
    public DataMatrix times(double d) {
        Matrix result = super.times(d);
        return new DataMatrix(result, ids, outer);
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

    public static void main(String[] args) {
        double[][] x = {{1, 2, 3}, {4, 5, 6}, {4, 5, 6}};
        String[] ids = {"a", "b", "c"};
        DataMatrix dm = new DataMatrix(x, ids, null);

        for (double v : dm.getAvgRow()) {
            System.out.print(v + " ");
        }
        System.out.println("");
    }
}
