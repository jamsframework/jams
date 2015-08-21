/*
 * TileDrainage.java
 *
 * Created on May 21, 2009, 4:57 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package tiledrain;

import static java.lang.Math.*;

/**
 *
 * @author od
 * @author ja
 */
public class TileDrainage {
    
    /*
     * This routine solves Hooghoudt's and Kirkham's equations to find the
     * current flux out the tile drain based on the lateral Ksats within the water
     * table, distances between and radius of drains, and depth to the water table
     *
     * @param horthk     i  depth of soil horizons [cm]
     * @param depimp     i  depth to impermeable layer [cm]
     * @param depdr      i  depth to tile drains [cm]
     * @param drspac     i  distance between tile drains [cm]
     * @param drrad      i  radius of tile drains [cm]
     * @param depwt      i  depth of water table [cm]
     * @param nhor       i  number of soil horizons
     * @param clat       i  lateral hydraulic conductivity [cm/hr]
     * @param stor       i  surface depressional storage [cm]
     * @param storro     i  surface depressional storage that must be filled before
     *                      water can move to the drain [cm]
     * @param dfluxl     o  flux out of drain by layer (cm/hr)
     *
     * @return  flux out drain based on current conditions [cm/hr]
     *
     */
    public static double tiledrain(double[] horthk, double depimp, double depdr,
            double drspac, double drrad, double depwt, double[] clat,
            double stor, double storro,  double[] dfluxl) {
        
        double dfluxt;
        
        if ((depdr - depwt) <= 0.0) {
            // water table is below tile drain - no drainage occurs
            dfluxt = 0.0;
        } else {
            assert drrad > 0;
            assert drspac > 0;
            assert horthk.length == dfluxl.length && clat.length == dfluxl.length;
            
            int nhor = horthk.length;
            double[] d = new double[nhor];
            double[] twtl = new double[nhor];
            double effdep;
            int idr = 0, iwt = 0;
            
            // calculate effective depth of tile drain
            double dd = depimp - depdr;
            double rat = dd / drspac;
            double alpha = 3.55 - 1.6 * rat + 2.0 * rat * rat;
            
            if (rat < 0.3) {
                effdep = dd / (1 + rat * (8.0 / PI * log(dd / drrad) - alpha));
            } else {
                effdep = drspac * PI / (8.0 * (log(drspac / drrad) - 1.15));
            }
            
            effdep = max(effdep, 0.0);
            double hordep = 0.0;
            double num = 0.0;
            double den = 0.0;
            
            // calculate effective lateral conductivity in saturated zone
            for (int i = 0; i < nhor; i++) {
                d[i] = 0.0;
                
                if (i != 0) {
                    hordep = horthk[i] - horthk[i - 1];
                } else {
                    hordep = horthk[i];
                }
                
                if (depwt < horthk[i]) {
                    d[i] = min(horthk[i] - depwt, hordep);
                }
                
                num += d[i] * clat[i];
                den += d[i];
                
                //    locate soil horizons containing water table and tile drain
                if (idr == 0) {
                    if (horthk[i] >= depdr) {
                        idr = i + 1;
                    }
                }
                
                if (iwt == 0) {
                    if (horthk[i] <= depwt) {
                        iwt = i + 1;
                    }
                }
            }
            
            double effk = num / den;
            
            // calculate total drainage flux
            if ((stor > storro) && (depwt < 1.0)) {
                //  use Kirkham's equation for surface ponded conditions
                double sum = 0;
                double tmp = sinh(PI * depimp / drspac);
                double t2 = tmp * tmp;
                tmp = sinh(PI * (2 * depdr - drrad) / drspac);
                double t3 = tmp * tmp;
                for (int n = 1; n <= 5; n++) {
                    tmp = sinh(2.0 * PI * n * depimp / drspac);
                    double t1 = tmp * tmp;
                    num = t1 - t2;
                    den = t1 - t3;
                    sum = sum + (pow(1, n) * log(num / den));
                }
                
                double f = 2.0 * log(sinh(PI * (2.0 * depdr - drrad) / drspac) / sinh(PI * drrad / drspac));
                double gee = f - 2.0 * sum;
                
                dfluxt = 4.0 * PI * effk * (depimp - effdep + stor) / (gee * drspac);
                if (dfluxt < 0.0) {
                    dfluxt = 0.0;
                }
            } else {
                // use Hooghoudt's equation
                double em = depimp - depwt - effdep;
                dfluxt = (8.0 * effk * effdep * em + 4. * effk * em * em) / (drspac * drspac);
                if (dfluxt < 0.0) {
                    dfluxt = 0.0;
                }
            }
            
            // calculate drainage flux by horizon in saturated zone above drain
            double tlsat = depdr - depwt;
            
            // weight drainage flux based on horizon thickness
            int il = -1;
            double wden = 0.0;
            
            for (int i = iwt; i <= idr; i++) {
                il++;
                double upr = max((i > 0) ? horthk[i - 1] : 0.0, depwt);
                twtl[il] = horthk[i] - upr;
                twtl[il] = max(twtl[il], 0.0);
                wden += (il + 1) * twtl[il];
            }
            
            double wt1 = tlsat / wden;
            il = -1;
            for (int i = iwt; i <= idr; i++) {
                il++;
                double wt = (il + 1) * wt1;
                dfluxl[i] = dfluxt * wt * twtl[il] / tlsat;
            }
        }
        return dfluxt;
    }
    
    //test
    public static void main(String[] args) {
        double depimp = 250.0, depdr = 110.0, drspac = 1000.0, drrad = 15.0,
                depwt = 70.0, stor = 0.2, storro = 0.1;
        
        double[] horthk = {25.0, 50.0, 75.0, 100.0, 125.0, 150.0, 175.0, 200.0, 225.0, 250.0};
        double[] clat = {2.7, 1.0, 4.5, 2.2, 8.2, 4.2, 5.2, 1.2, 1.2, 3.2};
        double[] dfluxl = new double[10];
        
        double dfluxt = tiledrain(horthk, depimp, depdr, drspac, drrad, depwt, clat, stor, storro, dfluxl);
        
        System.out.printf("total drainage flux = %8.5f cm/hr\n", dfluxt);
        for (int i = 0; i < dfluxl.length; i++) {
            System.out.printf("drainage flux from layer %d = %8.5f cm/hr\n", i, dfluxl[i]);
        }
    }
}
