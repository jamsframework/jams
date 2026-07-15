/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package optas.regression;

import java.util.TreeSet;

/**
 *
 * @author chris
 */
public class IDW extends SimpleInterpolation{
    
    int R = 3;
    double expWeights[];

    public void setR(int R){
        this.R = R;
    }

    public int getR(int R){
        return R;
    }

    private double d(double a[], double b[]){
        double d = 0;
        for (int i=0;i<n;i++){
            double di = (a[i]-b[i]);
            d+=expWeights[i]*di*di;
        }
        return d;
    }

    int nearestNeighbours[][];
    double distanceToNeighbour[][];

    @Override
    protected void calculate(){
        nearestNeighbours = new int[L][R];
        distanceToNeighbour = new double[L][R];

        int worstIndex = 0;
        double x_i[] = new double[n];
        double x_j[] = new double[n];

        for (int j=0;j<L;j++){
            int id_j = x[0].getId(j);

            for (int k=0;k<n;k++){
                x_j[k] = x[k].getValue(id_j);
            }

            for (int k=0;k<R;k++){
                nearestNeighbours[j][k] = -1;
                distanceToNeighbour[j][k] = Double.MAX_VALUE;
            }

            for (int i=0;i<L;i++){
                if (i==j)
                    continue;
                int id_i = x[0].getId(i);

                for (int k=0;k<n;k++){
                    x_i[k] = x[k].getValue(id_i);
                }
                double dist = d(x_j,x_i);

                if (dist < distanceToNeighbour[j][worstIndex]){
                    distanceToNeighbour[j][worstIndex] = dist;
                    nearestNeighbours[j][worstIndex] = id_i;

                    for (int k=0;k<R;k++){
                        if (distanceToNeighbour[j][worstIndex] < distanceToNeighbour[j][k])
                            worstIndex = k;
                    }
                }
            }
        }

    }

    protected double[][] getInterpolatedValue(TreeSet<Integer> validationSet) {
        int counter = 0;
        double ystar[][] = new double[validationSet.size()][m];

        for (Integer id : validationSet) {
            int nearestNeighbours[] = new int[R];
            double distanceToNeighbour[] = new double[R];

            int worstIndex = 0;

            for (int i = 0; i < R; i++) {
                nearestNeighbours[i] = -1;
                distanceToNeighbour[i] = Double.MAX_VALUE;
            }

            double x_i[] = new double[n];

            for (int i = 0; i < L; i++) {
                int id_i = x[0].getId(i);

                for (int j = 0; j < n; j++) {
                    x_i[j] = x[j].getValue(id_i);
                }
                double dist = d(getX(id), x_i);
                if (dist < distanceToNeighbour[worstIndex]) {
                    distanceToNeighbour[worstIndex] = dist;
                    nearestNeighbours[worstIndex] = id_i;

                    for (int j = 0; j < R; j++) {
                        if (distanceToNeighbour[worstIndex] < distanceToNeighbour[j]) {
                            worstIndex = j;
                        }
                    }
                }
            }
            double sum = 0;
            for (int i = 0; i < R; i++) {
                if (nearestNeighbours[i] == -1) {
                    return null;
                }
                sum += 1.0 / distanceToNeighbour[i];
            }

            for (int j = 0; j < m; j++) {
                for (int i = 0; i < R; i++) {
                    if (distanceToNeighbour[i] < 0.0000001) {
                        ystar[counter][j] = y[j].getValue(nearestNeighbours[i]);
                    } else {
                        ystar[counter][j] += (1.0 / distanceToNeighbour[i]) / sum * y[j].getValue(nearestNeighbours[i]);
                    }
                }
                counter++;
            }
        }
        return ystar;

            /*
            int neighbours[] = nearestNeighbours[leaveOneOut];
            double dist[] = distanceToNeighbour[leaveOneOut];

            double sum = 0;
            for (int i=0;i<R;i++){
            if (neighbours[i]==-1)
            return -1.0;
            sum += 1.0 / dist[i];
            }
            double ystar=0;

            for (int i=0;i<R;i++){
            if (dist[i] < 0.0000001)
            return y.getValue(neighbours[i]);
            else
            ystar += (1.0 / dist[i]) / sum * y.getValue(neighbours[i]);
            }
            return ystar;*/
        }

    public double[] getInterpolatedValue(double u[]){
        int      nearestNeighbours[] = new int[R];
        double   distanceToNeighbour[] = new double[R];

        int worstIndex = 0;

        for (int i=0;i<R;i++){
            nearestNeighbours[i] = -1;
            distanceToNeighbour[i] = Double.MAX_VALUE;
        }

        double x_i[] = new double[n];

        for (int i=0;i<L;i++){
            int id_i = x[0].getId(i);
                        
            for (int j=0;j<n;j++){
                x_i[j] = x[j].getValue(id_i);
            }
            double dist = d(u,x_i);
            if (dist < distanceToNeighbour[worstIndex]){
                distanceToNeighbour[worstIndex] = dist;
                nearestNeighbours[worstIndex] = id_i;

                for (int j=0;j<R;j++){
                    if (distanceToNeighbour[worstIndex] < distanceToNeighbour[j])
                        worstIndex = j;
                }
            }
        }
        double sum = 0;
        for (int i=0;i<R;i++){
            if (nearestNeighbours[i]==-1)
                return null;
            sum += 1.0 / distanceToNeighbour[i];
        }
        double ystar[] = new double[m];

        for (int j = 0; j < m; j++) {
            for (int i = 0; i < R; i++) {
                if (distanceToNeighbour[i] < 0.0000001) {
                    ystar[j] = y[j].getValue(nearestNeighbours[i]);
                } else {
                    ystar[j] += (1.0 / distanceToNeighbour[i]) / sum * y[j].getValue(nearestNeighbours[i]);
                }
            }
        }
        return ystar;
    }
}
