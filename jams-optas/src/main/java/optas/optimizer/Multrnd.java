package optas.optimizer;




public class Multrnd {

    public int n;
    public double[] p;
    public int m;
    public double[][] X;
    public double[][] Y;

    public void process() {
        int row_p = p.length;
        X = new double[m][row_p];
        Y = new double[m][row_p];
                
        double[] s = new double[row_p];
        double P = 0;
        double count = 0;
        
        for (int i = 0; i < row_p; i++) {
            P +=  p[i];
        }

        for (int i = 0; i < row_p; i++) {
            s[i] = (p[i] / P) + count;
            count = s[i];
        }
        
        for (int i = 0; i < m; i++) {
            double[] o = new double[n];
            double[] r = new double[n];
            double[] alpha = new double[n];
            
            for (int k = 0; k < n; k++) {
                o[k] = 1.0;
                r[k] = Math.random();
            }

            for (int j = 0; j < row_p; j++) {                
                for (int in = 0; in < n; in++) {
                    if (r[in] > s[j]) {
                        alpha[in] = 1;
                    } else {
                        alpha[in] = 0;
                    }
                }
                for (int u = 0; u < n; u++) {
                    o[u] += alpha[u];
                }
            }

            /*
             * this should be the same as .. 
            for (int j = 0; j < row_p; j++) {                
                for (int in = 0; in < n; in++) {
                    if (r[in] > s[j]) {
                        o[in]++;
                    } 
                }
            }
             */
            for (int j = 0; j < row_p; j++) {
                double somma = 0;
                for (int ind_o = 0; ind_o < o.length; ind_o++) {                  
                    if (o[ind_o] == (j + 1)) {
                        somma++;
                    }
                }
                X[i][j] = somma;
            }
            for (int row = 0; row < X.length; row++) {
                for (int col = 0; col < X[0].length; col++) {
                    Y[row][col] = X[row][col] / n;
                }
            }
        }
    }
}
