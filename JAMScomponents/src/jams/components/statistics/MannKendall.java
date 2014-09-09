/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package jams.components.statistics;

import jams.data.Attribute;
import jams.data.Attribute.Calendar;
import jams.math.distributions.CDF_Normal;
import jams.model.JAMSComponent;
import jams.model.JAMSVarDescription;
import java.util.Arrays;

/**
 *
 * @author christian
 */
public class MannKendall extends JAMSComponent{    
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
                        description = "current time interval")
    public Attribute.Calendar time;
    
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
                        description = "time interval for trend estimation")
    public Attribute.TimeInterval period;
    
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
                        description = "value of trend")        
    public Attribute.Double[] y;
    
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
                        description = "value of trend")        
    public Attribute.Boolean[] enabled;
    
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.WRITE,
                        description = "kendalls p value")        
    public Attribute.Double[] p;
    
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.WRITE,
                        description = "kendalls tau value")        
    public Attribute.Double[] tau;
    
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.WRITE,
                        description = "linreg ax+b")        
    public Attribute.Double[] a;
    
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.WRITE,
                        description = "linreg ax+b")        
    public Attribute.Double[] b;
    
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.WRITE,
                        description = "linreg r2")        
    public Attribute.Double[] r2;
    
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.WRITE,
                        description = "variationskoeffizient")        
    public Attribute.Double[] V;
    
    Calendar lastTimeStep = null;
    float timeserie[][] = null;

    int counter = 0;
    int n,m;
    @Override
    public void init(){
        n = (int)period.getNumberOfTimesteps();
        m = 0;
        
        for (int i=0;i<y.length;i++){
            if (isEnabled(i))
                m++;                
        }
        //this quite an amount of memory .. 
        timeserie = new float[m][n];
        counter = 0;
        lastTimeStep = null;
    }
    
    private boolean isEnabled(int i){
        return (enabled == null || enabled[i].getValue());
    }
    
    @Override
    public void run(){
        boolean considerData = true;
        if (time != null){
            if (lastTimeStep != null && 
                lastTimeStep.getTimeInMillis() == time.getTimeInMillis()){
                considerData = false;
            }else{
                lastTimeStep = time.getValue();
            }
            if (period!=null){
                if (time.before(period.getStart()) || 
                    time.after(period.getEnd())){
                    considerData = false;
                }
            }
        }
        
        if (!considerData){
            return;
        }
        if (counter>=n){
            getModel().getRuntime().sendHalt("Error: Capacity of array is not sufficient in Mann-Kendall Test" + "\nTimestep of time period is not consistent with real time step!");
            return;
        }
        int c=0;
        for (int i=0;i<y.length;i++){
            if (isEnabled(i))
                timeserie[c++][counter] = (float)y[i].getValue();
        }
        counter++;        
        
        //no further timesteps available
        time.add(period.getTimeUnit(), period.getTimeUnitCount());
        if (counter>=n || !time.before(period.getEnd())){
            for (int i=0;i<y.length;i++){
                double resultKendall[] = new double[5];
                double resultLinReg[] = new double[4];
                if (isEnabled(i)){
                    resultKendall = Kendall(timeserie[i]);
                    resultLinReg  = LinearRegression(timeserie[i]);
                }
                if (this.tau != null)
                    this.tau[i].setValue(resultKendall[0]);
                if (this.p != null)
                    this.p[i].setValue(resultKendall[1]);
                if (this.a != null)
                    this.a[i].setValue(resultLinReg[0]);
                if (this.b != null)
                    this.b[i].setValue(resultLinReg[1]);
                if (this.r2 != null)
                    this.r2[i].setValue(resultLinReg[2]);
                if (this.V != null)
                    this.V[i].setValue(resultLinReg[3]);
            }
        }
        time.add(period.getTimeUnit(), -period.getTimeUnitCount());
    }
    
    public static double[] LinearRegression(float y[]){
        int n=y.length;
        
        // sum(xi) / sum(yi)
        double sx = 0;
        double sy = 0;
            
        // values of time series are summed up (sy)
        for (int i = 0; i < n; i++) {                           
            sx += i;
            sy += y[i];
        }
        double xm = sx / n;
        double ym = sy / n;
        
        // sum((xi - xm)*(yi - ym))
        double numerator = 0;
        // sum((xi - xm)^2)
        double denominator = 0;
        // sum((yi - ym)^2)
        double sum_for_V = 0;
            
        // calculation of slope a, coefficient of variation V [%] and coefficient of determination r_squared
        for (int i = 0; i < n; i++) {                           
            numerator += (i - xm)*(y[i]-ym);
            denominator += (i-xm)*(i-xm);
            sum_for_V +=(y[i]-ym)*(y[i]-ym);
        }
        
        double a = numerator/denominator;
        double V = 0;
        if (ym != 0){
            V = Math.sqrt(sum_for_V / n) / ym;
        }else{
            V = Double.NaN;
        }        
        double r2 = numerator*numerator / (denominator * sum_for_V);
        // calculation of y-interception b
        double b = ym - a * xm;

        return new double[]{a,b,r2,V};
    }
    
    
    // Adapted from r package 'kendall'
    // TAUK2.
    // CALCULATE KENDALL'S TAU AND ITS P-VALUE FOR THE GENERAL
    // CASE OF 2 VARIATES.
    // KENDALL'S METHOD IS USED FOR TIES.
    //
    // REFERENCE: M.G. KENDALL, "RANK CORRELATION METHODS" PUBLISHED BY
    //            GRIFFIN & CO.
    //
    public static double[] Kendall(float y[]) throws ArithmeticException, IllegalArgumentException {
        int n = y.length;

        boolean sw, swy, ties = false; //this is potentially a bug in the originial code!!
        double iw[] = new double[n];
        Arrays.fill(iw, 0);

        double prob = 1.;
        double sltau = 1.0;
        double tau = 1.0;
        double denom = 0;
        double vars = 0;
        double s = 0;

        if (n < 2) {
            throw new IllegalArgumentException("n smaller than 2");
        }
        swy = true;

        for (int i = 1; i < n; i++) {
            if (y[i] != y[i - 1]) {
                swy = false;
            }
        }
        // sw is true if at least one of X or Y has no ties
        sw = false | swy;

        double cn = n * (n - 1);
        double dn = 0.5 * cn;
        double sumt = 0;
        double suma1 = 0;
        double suma2 = 0;

        for (int i = 0; i < n - 1; i++) {
            for (int j = i + 1; j < n; j++) {
                s += scoreK(i, y[i], j, y[j]);
            }
        }

        double d1 = Math.sqrt(dn);
        Arrays.fill(iw, 0);
        double sumu = 0;
        double sumb1 = 0;
        double sumb2 = 0;

        for (int i = 0; i < n - 1; i++) {
            double tmp = 1.0;
            for (int j = i + 1; j < n; j++) {
                if (y[i] == y[j]) {
                    if (iw[j] != 1) {
                        iw[j] = 1;
                        tmp++;
                        ties = true;
                    }
                }
            }
            sumu += tmp * (tmp - 1);
            sumb1 += tmp * (tmp - 1) * (2.0 * tmp + 5.);
            sumb2 += tmp * (tmp - 1) * (tmp - 2.);
        }
        double d2 = Math.sqrt(dn - 0.5 * sumu);
        if (d1 <= 0.0 || d2 <= 0.0) {
            throw new ArithmeticException("d1 or d2 is smaller than zero");
        }

        denom = d1 * d2;
        tau = s / denom;
        double vars1 = (cn * (2 * n + 5) - suma1 - sumb1) / 18.0;
        double vars2 = suma2 * sumb2 / (9. * cn * (n - 2));
        double vars3 = sumt * sumu / (cn + cn);
        vars = vars1 + vars2 + vars3;
        double sds = Math.sqrt(vars);

            // USE EXACT METHOD IF THERE ARE NO TIES.
        //that doesn't make any sense ???! .. will never call this method?
        if (sw) { //ties??!
            //CALCULATE P-VALUE USING EXACT METHOD
            int is = (int) s;
            prob = 1.0 - prtaus(is, n);
            //IF THERE ARE TIES, NEED ATLEAST SAMPLE SIZE OF ATLEAST 3
        } else if (n > 3) {
            //C USE CONTINUITY CORRECTION FOR S
            double scor = 0.0;
            if (s > 0) {
                scor = s - 1;
            }
            if (s < 0) {
                scor = s + 1;
            }
            //C CALCULATE P-VALUE USING NORMAL APPROXIMATION
            double zscore = scor / sds;
            prob = alnorm(zscore, false);
        } else {
            //C THIS ONLY HAPPENS WHEN N<4. aim. 06/07/2009
            throw new IllegalArgumentException("n smaller than 4");
        }
        if (prob > 0.5) {
            sltau = 2.0 * (1.0 - prob);
        } else {
            sltau = 2.0 * prob;
        }

        return new double[]{tau, sltau, s, denom, vars};
    }
    
    // Adapted from r package 'kendall'
    // TAUK2.
    // CALCULATE KENDALL'S TAU AND ITS P-VALUE FOR THE GENERAL
    // CASE OF 2 VARIATES.
    // KENDALL'S METHOD IS USED FOR TIES.
    //
    // REFERENCE: M.G. KENDALL, "RANK CORRELATION METHODS" PUBLISHED BY
    //            GRIFFIN & CO.
    //
    public static double[] MannKendall(double x[], double y[]) throws ArithmeticException, IllegalArgumentException{
        int n = x.length;
        
        if (n != y.length){
            System.out.println("Error x and y does not have the same length");
        }
        
        boolean sw, swx, swy, ties = false; //this is potentially a bug in the originial code!!
        double iw[] = new double[n];
        Arrays.fill(iw,0);
                
        double prob = 1.;
        double sltau = 1.0;
        double tau=1.0;
        double denom=0;
        double vars=0;
        double s = 0;
        
        if (n<2){
            throw new IllegalArgumentException("n smaller than 2");
        }
        swx = true;
        swy = true;
        for (int i=1; i<n;i++){
            if (x[i] != x[i-1])
                swx = false;
            if (y[i] != y[i-1])
                swy = false;
        }
        // sw is true if at least one of X or Y has no ties
        sw = swx | swy;
        if (!sw){
            double cn = n*(n-1);
            double dn = 0.5*cn;
            double sumt = 0;
            double suma1 = 0;
            double suma2 = 0;
            
            for (int i=0;i<n-1;i++){
                for (int j=i+1;j<n;j++){
                    s += scoreK(x[i],y[i],x[j],y[j]);
                }
            }
            
            for (int i=0;i<n-1;i++){
                double tmp = 1.0;
                for (int j=i+1;j<n;j++){
                    if (x[i]==x[j]){
                        if (iw[j] != 1){
                            iw[j] = 1;
                            tmp++;
                            ties = true;
                        }
                    }
                }
                sumt += tmp*(tmp-1);
                suma1 += tmp*(tmp-1)*(2.*tmp+5.);
                suma2 += tmp*(tmp-1)*(tmp-2.);
            }
            double d1 = Math.sqrt(dn - 0.5*sumt);
            Arrays.fill(iw,0);
            double sumu = 0;
            double sumb1 = 0;
            double sumb2 = 0;
            
            for (int i=0;i<n-1;i++){
                double tmp = 1.0;
                for (int j=i+1;j<n;j++){
                    if (y[i]==y[j]){
                        if (iw[j]!=1){
                            iw[j] = 1;
                            tmp++;
                            ties = true;
                        }
                    }
                }
                sumu += tmp*(tmp-1);
                sumb1 += tmp*(tmp-1) * (2.0*tmp+5.);
                sumb2 += tmp*(tmp-1) * (tmp-2.);
            }
            double d2 = Math.sqrt(dn - 0.5*sumu);
            if (d1 <= 0.0 || d2 <= 0.0){
                throw new ArithmeticException("d1 or d2 is smaller than zero");
            }
            
            denom = d1*d2;
            tau = s / denom;
            double vars1 = (cn*(2*n+5)-suma1-sumb1)/18.0;
            double vars2 = suma2*sumb2 / (9.*cn*(n-2));
            double vars3 = sumt*sumu / (cn+cn);
            vars = vars1+vars2+vars3;
            double sds = Math.sqrt(vars);
            
            
            // USE EXACT METHOD IF THERE ARE NO TIES.
            //that doesn't make any sense ???! .. will never call this method?
            if (sw){ //ties??!
                //CALCULATE P-VALUE USING EXACT METHOD
                int is = (int)s;
                prob = 1.0 - prtaus(is,n);                
            //IF THERE ARE TIES, NEED ATLEAST SAMPLE SIZE OF ATLEAST 3
            }else if (n > 3){                
                //C USE CONTINUITY CORRECTION FOR S
                double scor = 0.0;
                if (s>0) scor = s-1;
                if (s<0) scor = s+1;
                //C CALCULATE P-VALUE USING NORMAL APPROXIMATION
                double zscore = scor / sds;
                prob = alnorm(zscore, false);                
            }else{
                //C THIS ONLY HAPPENS WHEN N<4. aim. 06/07/2009
                throw new IllegalArgumentException("n smaller than 4");
            }
            if (prob > 0.5) {
                sltau = 2.0 * (1.0 - prob);
            } else {
                sltau = 2.0 * prob;
            }            
        }    
        return new double[]{tau, sltau, s, denom, vars};
    }
    
    private static int scoreK(double x1, double y1, double x2, double y2){        
        if ( (x1 > x2 && y1 < y2) || (x1<x2 && y1>y2)){
            return -1;
        }else if ( (x1 == x2) || (y1 == y2) ){
            return 0;
        }
        return 1;
    }
    
    private static  double alnorm(double z, boolean upper){
        //ALGORITHM AS 66 APPL. STATIST. (1973) VOL.22, NO.3
        //   EVALUATES THE TAIL AREA OF THE STANDARDISED NORMAL
        //   CURVE FROM X TO INFINITY IF UPPER IS .TRUE. OR
        //   FROM MINUS INFINITY TO X IF UPPER IS .FALSE.
        
        //this function is allready in optas.math.distributions .. 
        if (upper == true)
            return 1.0 - CDF_Normal.normp(z);
        else
            return CDF_Normal.normp(z);
    }
      
    //
    //     ALGORITHM AS 71  APPL. STATIST. (1974) VOL.23, NO.1
    //
    //    GIVEN A VALUE OF IS CALCULATED FROM TWO RANKINGS (WITHOUT TIES)
    //    OF N OBJECTS, THE FUNCTION COMPUTES THE PROBABILITY OF
    //    OBTAINING A VALUE GREATER THAN, OR EQUAL TO, IS.
    //
    private static  double prtaus(int is, int n) throws ArithmeticException{
        double H[] = new double[15];
        double L[][] = new double[2][15];
        
        //
        //        CHECK ON THE VALIDITY OF IS AND N VALUES
        //
        double prtaus = 1.0;
        if ( n < 1){
            throw new ArithmeticException("n smaller than 1");
        }
        
        int m = n*(n-1) / 2 - Math.abs(is);
        if (m < 0 || m % 2 == 1){ //why that mod operation?
            throw new ArithmeticException("m smaller 0 or m % 2 != 1");
        }
        if (m==0 && is <= 0){
            return prtaus;
        }
        
        if (n>8){
            //CALCULATION OF TCHEBYCHEFF-HERMITE POLYNOMIALS
            double x = (double)(is-1) / Math.sqrt((6.+n*(5-n*(3+2*n)))/18.);
            H[1] = x;
            H[2] = x*x - 1.0;
            for (int i=2;i<15;i++){
                H[i] = x*H[i-1] - (double)(i) * H[i-2];
            }
            
            //PROBABILITIES CALCULATED BY MODIFIED EDGEWORTH SERIES FOR
            //N GREATER THAN 8
            double R = 1. / n;
            double sc = R * (H[2]*(-9.0000E-2+R*(4.5000E-2+R*(-5.325E-1+R*5.06E-1))) +
                    R*(H[4]*(3.6735E-2+R*(-3.6735E-2+R*3.214E-1))+H[6]*(
                    4.0500E-3+R*(-2.3336E-2+R*7.787E-2))+R*(H[8]*(-3.3061E-3-R*
                    6.5166E-3)+H[10]*(-1.2150E-4+R*2.5927E-3)+R*(H[12]*1.4878E-4+
                    H[14]*2.7338E-6))));
            
            prtaus = alnorm(x, true) + sc * 0.398942 * Math.exp(-0.5*x*x);
            if (prtaus < 0.0) prtaus = 0;
            if (prtaus > 1.0) prtaus = 1.0;
        //PROBABILITIES CALCULATED BY RECURRENCE RELATION FOR
        //N LESS THAN 9
        }else{
            if (is<0){
                m = m -2;
            }            
            int im = m / 2 + 1;
            L[0][0] = 1;
            L[1][0] = 1;
            if (im > 2){
                for (int i=1;i<im;i++){
                    L[0][i] = 0;
                    L[1][i] = 0;
                }
            }
            int il = 1;
            int i = 1;
            m = 1;
            int j=1;
            int jj=2;
            int k = 0;
            
            while(i != n){
                il += i;
                i++;
                m = m*i;
                j = 3-j;
                jj = 3 -jj;
                int in = 1;
                int io = 0;
                k = Math.min(im, il);
                while(true){
                    in++;
                    if ( in >  k) {
                        break;
                    }
                    L[jj-1][in-1] = L[jj-1][in-2] + L[j-1][in-1];
                    if (in > i){
                        io++;
                        L[jj-1][in-1] = L[jj-1][in-1] - L[j-1][io-1];                        
                    }
                }
            }
            k = 0;
            for (i=0;i<im;i++){
                k+= L[jj-1][i-1];
            }
            prtaus = (double)k / (double)m;
            if (is < 0){
                prtaus = 1.0 - prtaus;
            }            
        }
        return prtaus;
    }
    
    public static void main(String[] args) {
        double x[]={1,13,13,13,5,6,7,8,9,10};
        double y[]={1,1,1,1,-5,-6,-7,8,9,10};
                        
        MannKendall mk = new MannKendall();
        double r[] = mk.MannKendall(x, y);
        System.out.println("tau:" + r[0]);
        System.out.println("sltau:" + r[1]);
        System.out.println("s:" + r[2]);
        System.out.println("denom:" + r[3]);
        System.out.println("vars:" + r[4]);
    }
    
}
