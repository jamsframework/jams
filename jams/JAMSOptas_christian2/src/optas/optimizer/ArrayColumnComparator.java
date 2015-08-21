/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package optas.optimizer;

import java.util.Comparator;

/**
 *
 * @author chris
 */
public class ArrayColumnComparator implements Comparator {

    private int col = 0;
    private int order = 1;

    public ArrayColumnComparator(int col, boolean decreasing_order) {
        this.col = col;
        if (decreasing_order) {
            order = -1;
        } else {
            order = 1;
        }
    }

    public int compare(Object d1, Object d2) {

        double[] b1 = (double[]) d1;
        double[] b2 = (double[]) d2;

        if (b1[col] < b2[col]) {
            return -1 * order;
        } else if (b1[col] == b2[col]) {
            return 0 * order;
        } else {
            return 1 * order;
        }
    }
}
