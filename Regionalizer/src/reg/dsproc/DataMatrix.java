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
}
