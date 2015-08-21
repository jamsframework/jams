/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.unijena.j2k.statistics;

/**
 *
 * @author Peter Krause
 */
public class LatinHypercube {

    public static void main(String[] args) {
        double[] uB = {1, 10};
        double[] lB = {0, 0};
        int nPars = lB.length;
        int res = 100;
        double[] rL = randomLHS(lB, uB, res);
        double[] iL = improvedLHS(lB, uB, res, 2);
        //int[] oL = optimumLHS(lB,uB,res,10,0.01);
        int counter = 0;
        for (int r = 0; r < res; r++) {
            for (int i = 0; i < nPars; i++) {
                System.out.print(iL[i * res + r] + ", ");
                counter++;
            }
            System.out.println("");
        }
    }

    static double[] randomLHS(double[] lowBounds, double[] uppBounds, int resolution) {
        int outCounter = 0;
        int nPars = lowBounds.length;
        double[] randLHS = new double[nPars * resolution];
        for (int i = 0; i < nPars; i++) {
            double range = uppBounds[i] - lowBounds[i];
            double step = range / resolution;
            double start = lowBounds[i];
            double[] vals = new double[resolution];
            for (int r = 0; r < resolution; r++) {
                double end = start + step;
                vals[r] = start + (Math.random() * step);
                start = end;
            }
            shuffle(vals);
            for (int r = 0; r < resolution; r++) {
                randLHS[outCounter] = vals[r];
                outCounter++;
            }
        }
        return randLHS;
    }

    /*
     * Function to return the sum of the inverse of the distances between each
     * point in the matrix
     *
     */
    static double sumInvDistance(int[] matrix, int nr, int nc) {
        int i, j, k;
        int oneDistance;
        double totalInvDistance = 0.0;
        /* iterate the row of the first point from 0 to N-2 */
        for (i = 0; i < (nr - 1); i++) {
            /* iterate the row the second point from i+1 to N-1 */
            for (j = (i + 1); j < nr; j++) {
                oneDistance = 0;
                /* iterate through the columns, summing the squared differences */
                for (k = 0; k < nc; k++) {
                    /* calculate the square of the difference in one dimension between the
                     * points */
                    oneDistance += (matrix[i * (nc) + k] - matrix[j * (nc) + k]) * (matrix[i * (nc) + k] - matrix[j * (nc) + k]);
                }
                /* sum the inverse distances */
                totalInvDistance += (1.0 / Math.sqrt((double) oneDistance));
            }
        }
        return (totalInvDistance);
    }

    /**
     * create a nearly optimal latin hypercube design with respect to
     * the S optimality criterion using the Columnwise-pairwise (CP)
     * Algorithm
     * The implementation is based on the C sources and the R implementation
     * of Rob Carnell, May 2005
     * @param xData the independent data array (x)
     * @param yData the dependent data array (y)
     * @return (the optimum latin hypercube set of length realisation * variables)
     */
    public static int[] optimumLHS(double[] lowBounds, double[] uppBounds, int nPoints, int maxsweeps, double eps) {

        double gOld;
        //double deltag1 = 0.0;
        //double deltag;
        //int test, iter, i, j, k, r, posit, row, col, total;
        int j = 0;
        int nPars = lowBounds.length;
        int[] pOld = new int[nPoints * nPars];
        int[] pNew = new int[nPoints * nPars];
        int bc = (int) binomialCoefficient(nPoints, nPars) + 1;
        int[] J1 = new int[bc];
        int[] J2 = new int[bc];
        int[] J3 = new int[bc];
        for (int i = 0; i < pOld.length; i++) {
            pOld[i] = i + 1;
        /* find the initial optimality measure */
        }
        gOld = sumInvDistance(pOld, nPoints, nPars);

        System.out.println("Beginning Optimality Criterion: " + gOld);

        /*#if printResult
        
        for(row = 0; row < *N; row++)
        {
        for(col = 0; col < *K; col++)
        {
        Rprintf("%d ", pOld[row * (*K) + col]);
        }
        Rprintf("\n");
        }
        
        #endif*/

        int test = 0;
        int iter = 0;

        while (test == 0) {
            if (iter == maxsweeps) {
                break;
            }
            iter++;
            /* iterate over the columns */
            for (j = 0; j < nPars; j++) {
                int r = 0;
                /* iterate over the rows for the first point from 0 to N-2 */
                for (int i = 0; i < (nPoints - 1); i++) {
                    /* iterate over the rows for the second point from i+1 to N-1 */
                    for (int k = (i + 1); k < nPoints; k++) {
                        /* put the values from pOld into pNew */
                        for (int row = 0; row < nPoints; row++) {
                            for (int col = 0; col < nPars; col++) {
                                pNew[row * nPars + col] = pOld[row * nPars + col];
                            }
                        }
                        /* exchange two values (from the ith and kth rows) in the jth column
                         * and place them in the new matrix */
                        pNew[i * nPars + j] = pOld[k * nPars + j];
                        pNew[k * nPars + j] = pOld[i * nPars + j];

                        /* store the optimality of the newly created matrix and the rows that
                         * were interchanged */
                        J1[r] = (int) sumInvDistance(pNew, nPoints, nPars);
                        J2[r] = i;
                        J3[r] = k;
                        r++;
                    }
                }
                /* once all combinations of the row interchanges have been completed for
                 * the current column j, store the old optimality measure (the one we are
                 * trying to beat) */
                J1[r] = (int) gOld;
                J2[r] = 0;
                J3[r] = 0;

                /* Find which optimality measure is the lowest for the current column
                 * In other words, which two row interchanges made the hypercube better in
                 * this column */

                int posit = 0;
                for (int k = 0; k < bc; k++) {
                    if (J1[k] < J1[posit]) {
                        posit = k;
                    }
                }

                /* If the new minimum optimality measure is better than the old measure */
                if (J1[posit] < gOld) {
                    /* put pOld in pNew */
                    for (int row = 0; row < nPoints; row++) {
                        for (int col = 0; col < nPars; col++) {
                            pNew[row * nPars + col] = pOld[row * nPars + col];
                        }
                    }
                    /* Interchange the rows that were the best for this column */
                    pNew[J2[posit] * nPars + j] = pOld[J3[posit] * nPars + j];
                    pNew[J3[posit] * nPars + j] = pOld[J2[posit] * nPars + j];

                    /* put pNew back in pOld for the next iteration */
                    for (int row = 0; row < nPoints; row++) {
                        for (int col = 0; col < nPars; col++) {
                            pOld[row * nPars + col] = pNew[row * nPars + col];
                        }
                    }

                    /* if this is not the first column we have used for this sweep */
                    double deltag1 = 0;
                    if (j > 0) {
                        /* check to see how much benefit we gained from this sweep */
                        double deltag = Math.abs(J1[posit] - gOld);
                        if (deltag < (eps * deltag1)) {
                            test = 1;
                            System.out.println("Algorithm stopped when the change in the inverse distance measure was smaller than " + (eps * deltag1));
                        }
                    } /* if this is first column of the sweep, then store the benefit gained */ else {
                        deltag1 = Math.abs(J1[posit] - gOld);
                    }

                    /* replace the old optimality measure with the current one */
                    gOld = J1[posit];
                } /* if the new and old optimality measures are equal */ else if (J1[posit] == gOld) {
                    test = 1;
                    System.out.println("Algorithm stopped when changes did not impove design optimality");
                } /* if the new optimality measure is worse */ else if (J1[posit] > gOld) {
                    System.out.println("Unexpected Result: Algorithm produced a less optimal design");
                    test = 1;
                }
                /* if there is a reason to exit... */
                if (test == 1) {
                    break;
                }
            }
        }

        /* if we made it through all the sweeps */
        if (iter == maxsweeps) {
            System.out.println("full sweeps completed: " + maxsweeps);
        } /* if we didn't make it through all of them */ else {
            System.out.println("Algorithm used" + (iter - 1) + " sweep(s) and " + j + " extra column(s)");
        }

        System.out.println("Final Optimality Criterion: " + gOld);

        /*
         * verify that the result is a latin hypercube.  One easy check is to ensure
         * that the sum of the rows is the sum of the 1st N integers.  This check can
         * be fooled in one unlikely way...
         * if a column should be 1 2 3 4 6 8 5 7 9 10
         * the sum would be 10*11/2 = 55
         * the same sum could come from 5 5 5 5 5 5 5 5 5 10
         * but this is unlikely
         */
        test = 1;
        for (int col = 0; col < nPars; col++) {
            int total = 0;
            for (int row = 0; row < nPoints; row++) {
                total += pOld[row * nPars + col];
            }
            if (total != nPoints * (nPoints + 1) / 2) {
                test = 0;
            }
        }
        if (test == 0) {
            /* the error function should send an error message through R */
            System.out.println("Invalid Hypercube");
        }
        
        for (int row = 0; row < nPoints; row++) {
            for (int col = 0; col < nPars; col++) {
                System.out.print(pOld[row * nPars + col]+", ");
            }
            System.out.println("");
        }
        
        return pOld;
    }

    static public double[] improvedLHS(double[] lowBounds, double[] uppBounds, int nPoints, int duplication) {
        int nPars = lowBounds.length;
        int[] avail = new int[nPars * nPoints];
        int[] intResult = new int[nPars * nPoints];
        double[] lhsResult = new double[nPars * nPoints];
        int[] list1 = new int[duplication * (nPoints - 1)];
        int[] point1 = new int[nPars * duplication * (nPoints - 1)];
        int[] vec = new int[nPars];
        /* optimum spacing between points */
        double OPT = (double) nPoints / (Math.pow((double) nPoints, (1.0 / (double) nPars)));
        /* the square of the optimum spacing between points */
        double OPT2 = OPT * OPT;

        /* iterators */
        int row, col;
        int count;
        int j, k;
        /* index of the current candidate point */
        int point_index;
        /* index of the optimum point */
        int best;
        /* the squared distance between points */
        int distSquared;
        /*
         * the minimum difference between the squared distance and the squared
         * optimum distance
         */
        double min_all;
        /*  The minumum candidate squared difference between points */
        int min_candidate;
        /* the length of the point1 columns and the list1 vector */
        int len = duplication * (nPoints - 1);
        /* used in testing the output */
        int total;
        int test = 1;

        /* initialize the avail matrix */
        for (row = 0; row < nPars; row++) {
            for (col = 0; col < nPoints; col++) {
                avail[row * nPoints + col] = col + 1;
            }
        }

        /*
         * come up with an array of K integers from 1 to N randomly
         * and put them in the last column of result
         */
        for (row = 0; row < nPars; row++) {
            intResult[row * nPoints + (nPoints - 1)] = (int) Math.floor(Math.random() * nPoints + 1);
        }

        /*
         * use the random integers from the last column of result to place an N value
         * randomly through the avail matrix
         */
        for (row = 0; row < nPars; row++) {
            avail[row * nPoints + (intResult[row * nPoints + (nPoints - 1)] - 1)] = nPoints;
        }

        /* move backwards through the result matrix columns */
        for (count = (nPoints - 1); count > 0; count--) {
            for (row = 0; row < nPars; row++) {
                for (col = 0; col < duplication; col++) {
                    /* create the list1 vector */
                    for (j = 0; j < count; j++) {
                        list1[(j + count * col)] = avail[row * nPoints + j];
                    }
                }
                /* create a set of points to choose from */
                for (col = (count * duplication); col > 0; col--) {
                    point_index = (int) Math.floor(Math.random() * col + 1);
                    point1[row * len + (col - 1)] = list1[point_index];
                    list1[point_index] = list1[(col - 1)];
                }
            }
            min_all = 10E99;
            best = 0;
            for (col = 0; col < (duplication * count - 1); col++) {
                min_candidate = 10000000;
                for (j = count; j < nPoints; j++) {
                    distSquared = 0;
                    /*
                     * find the distance between candidate points and the points already
                     * in the sample
                     */
                    for (k = 0; k < nPars; k++) {
                        vec[k] = point1[k * len + col] - intResult[k * nPoints + j];
                        distSquared += vec[k] * vec[k];
                    }
                    /* original code compared dist1 to OPT, but using the squareroot
                     * function and recasting distSquared to a double was unncessary.
                     * dist1 = sqrt((double) distSquared);
                     * if(min_candidate > dist1) min_candidate = dist1;
                     */

                    /*
                     * if the distSquard value is the smallest so far place it in
                     * min candidate
                     */
                    if (min_candidate > distSquared) {
                        min_candidate = distSquared;
                    }
                }
                /*
                 * if the difference between min candidate and OPT2 is the smallest so
                 * far, then keep that point as the best.
                 */
                if (Math.abs((double) min_candidate - OPT2) < min_all) {
                    min_all = Math.abs((double) min_candidate - OPT2);
                    best = col;
                }
            }

            /* take the best point out of point1 and place it in the result */
            for (row = 0; row < nPars; row++) {
                intResult[row * nPoints + (count - 1)] = point1[row * len + best];
            }
            /* update the numbers that are available for the future points */
            for (row = 0; row < nPars; row++) {
                for (col = 0; col < nPoints; col++) {
                    if (avail[row * nPoints + col] == intResult[row * nPoints + (count - 1)]) {
                        avail[row * nPoints + col] = avail[row * nPoints + (count - 1)];
                    }
                }
            }
        }

        /*
         * once all but the last points of result are filled in, there is only
         * one choice left
         */
        for (row = 0; row < nPars; row++) {
            intResult[row * nPoints + 0] = avail[row * nPoints + 0];
        }

        /*
         * verify that the result is a latin hypercube.  One easy check is to ensure
         * that the sum of the rows is the sum of the 1st N integers.  This check can
         * be fooled in one unlikely way...
         * if a column should be 1 2 3 4 6 8 5 7 9 10
         * the sum would be 10*11/2 = 55
         * the same sum could come from 5 5 5 5 5 5 5 5 5 10
         * but this is unlikely
         */
        for (row = 0; row < nPars; row++) {
            total = 0;
            for (col = 0; col < nPoints; col++) {
                total += intResult[row * nPoints + col];
            }
            if (total != nPoints * (nPoints + 1) / 2) {
                test = 0;
            }
        }
        if (test == 0) {
            /* the error function should send an error message through R */
            System.out.println("Invalid Hypercube");
        }



        for (row = 0; row < nPars; row++) {
            for (col = 0; col < nPoints; col++) {
                double lhsVal = (intResult[row * nPoints + col] - 1 + Math.random()) / nPoints;
                lhsResult[row * nPoints + col] = lowBounds[row] + (uppBounds[row] - lowBounds[row]) * lhsVal;
                //System.out.print(lhsResult[row * nPoints + col] + ",");
            }
            //System.out.println("");
        }
        return lhsResult;
    }

    public static void shuffle(double[] valArray) {
        for (int i = 0; i < valArray.length; i++) {
            int pos = i + (int) (Math.random() * (valArray.length - i));
            double swap = valArray[i];
            valArray[i] = valArray[pos];
            valArray[pos] = swap;

        }
    }

    public static long binomialCoefficient(int n, int r) {
        long t = 1;

        int m = n - r; // r = Math.max(r, n - r);
        if (r < m) {
            r = m;
        }

        for (int i = n, j = 1; i > r; i--, j++) {
            t = t * i / j;
        }

        return t;
    }
}
