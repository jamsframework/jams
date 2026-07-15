/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package optas.datamining;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Set;
import optas.core.AbstractFunction;
import optas.data.DataCollection;
import optas.data.Parameter;
import optas.data.SimpleEnsemble;

import optas.optimizer.SCE;
import optas.core.SampleLimitException;
import optas.core.ObjectiveAchievedException;
import optas.optimizer.management.SampleFactory.Sample;

/**
 *
 * @author chris
 */
public class FindLargestEmptyRectangle{
    int n = 0;
    int K = 0;

    double initialPoint[] = null;
    double pointList[][] = null;
    double lowerBound[]  = null,
           upperBound[]  = null;

    double result[][] = null;

    private double constraintPenalty(double l_in[], double u_in[]){
        double l[] = Arrays.copyOf(l_in, n);
        double u[] = Arrays.copyOf(u_in, n);

        double penalty = 0;
        //check if inital point is inside
        for (int i=0;i<n;i++){
            double v = Math.min(initialPoint[i] - l[i], u[i] - initialPoint[i]);
            if (v < 0){
                penalty = -v*10*volume(l, u);
                if (initialPoint[i] < l[i])
                    l[i] = initialPoint[i];
                else if (initialPoint[i] > u[i])
                    u[i] = initialPoint[i];
            }
        }
        //check against forbidden points        
        for (int j=0;j<K;j++){
            double min = Double.MAX_VALUE;
            for (int i=0;i<n;i++){
                double v = Math.min(pointList[j][i] - l[i], u[i] - pointList[j][i]);
                if (v > 0){
                    v /= Math.abs(u[i]-l[i]);                    
                }
                min = Math.min(v,min);
            }
            if (min > 0 )
                penalty += min;
        }
        for (int i = 0; i < n; i++) {
            double v = Math.max(lowerBound[i] - l[i], u[i] - upperBound[i]);
            if (v > 0) {
                penalty += v / Math.abs(u[i] - l[i]);
            }

        }
        //check against boundaries
        return 2.0*penalty*volume(l, u); //such that it is always better to trade volume for a decrease in penalty 
    }

    private double volume(double l[], double u[]){
        double v = 1;
        for (int i=0;i<l.length;i++){
            v*=Math.max((Math.min(u[i],upperBound[i])-Math.max(l[i],lowerBound[i])),0); //make sure the V >= 0
        }
        return v;
    }

    private double[][] expandRectangle(double w[]){
        double d=1.0;
        double lastGoodPlus[] = null;
        double lastGoodMinus[] = null;

        double eps = 0.0001;
        double iter = -Math.ceil(Math.log(eps)/Math.log(2.0));

        double test_plus[] = new double[n];
        double test_minus[] = new double[n];
        for (int k=1;k<=iter;k++){
            //check if valid
            for (int i=0;i<n;i++){
                test_plus[i] = initialPoint[i];
                test_minus[i] = initialPoint[i];
            }

            boolean isValid = true;

            for (int i=0;i<n;i++){
                test_plus[i] += d*w[i]*(upperBound[i]-lowerBound[i]);
                test_plus[i] = Math.min(upperBound[i], test_plus[i]);

                test_minus[i] -= d*w[n+i]*(upperBound[i]-lowerBound[i]);
                test_minus[i] = Math.max(lowerBound[i], test_minus[i]);
            }
            isValid = isValid & constraintPenalty(test_minus, test_plus) <= 0;
            if (!isValid){
                d-=Math.pow(2.0,-k);
            }else{
                lastGoodPlus = test_plus;
                lastGoodMinus = test_minus;
                d+=Math.pow(2.0,-k);
            }
        }
        if (lastGoodPlus != null && lastGoodMinus != null){
            return new double[][]{lastGoodMinus, lastGoodPlus};
        }else
            return new double[][]{initialPoint, initialPoint};
    }

    private double[] determineStartvalue(){
        double d=1.0;        
        double lastGoodMinus[]=null;
        double lastGoodPlus[]=null;

        double eps = 0.001;
        double iter = -Math.ceil(Math.log(eps)/Math.log(2.0));
        for (int k=1;k<=iter;k++){
            //check if valid
            double test_plus[] = Arrays.copyOf(initialPoint, n);
            double test_minus[] = Arrays.copyOf(initialPoint, n);

            boolean isValid = true;

            for (int i=0;i<n;i++){
                test_plus[i] += d*(upperBound[i]-lowerBound[i]);
                test_plus[i] = Math.min(upperBound[i], test_plus[i]);
                
                test_minus[i] -= d*(upperBound[i]-lowerBound[i]);
                test_minus[i] = Math.max(lowerBound[i], test_minus[i]);
            }
            isValid = isValid & constraintPenalty(test_minus, test_plus) <= 0;
            if (!isValid){
                d-=Math.pow(2.0,-k);
            }else{
                lastGoodMinus = test_minus;
                lastGoodPlus = test_plus;
                d+=Math.pow(2.0,-k);
            }            
        }
        double result[] = new double[2*n];
        for (int i=0;i<n;i++){
            result[i]   = lastGoodMinus[i];
            result[i+n] = lastGoodPlus[i]-lastGoodMinus[i];
        };
        return result;
    }

    public FindLargestEmptyRectangle(double initialPoint[], double lowerBound[], double upperBound[], double pointList[][]){
        n = initialPoint.length;
        K = pointList.length;

        this.lowerBound = lowerBound;
        this.upperBound = upperBound;
        
        this.pointList = Arrays.copyOf(pointList, K+2);
        this.pointList[K] = lowerBound;
        this.pointList[K+1] = upperBound;
        
        this.pointList  = pointList;
        this.initialPoint = initialPoint;

        System.out.println("Find largest empty rectangle .. ");
        System.out.println("Dimension is:" + n);
        if (lowerBound.length != n){
            System.out.println("Dimension of lower bound does not fit to n" + lowerBound.length + " vs " + n);            
        }
        if (upperBound.length != n){
            System.out.println("Dimension of upper bound does not fit to n" + lowerBound.length + " vs " + n);            
        }
    }
    
    private double[][] expand(double src[]){
        return expandRectangle(src);
        /*double lb[] = new double[n];
        double ub[] = new double[n];
        
        for (int i=0;i<n;i++){
            lb[i] = src[i];
            ub[i] = src[i+n] + lb[i];
        }
        return new double[][]{lb,ub};*/
    }
   
    private void procedure() {
        SCE sce = new SCE();
        sce.setVerbose(false);

        double sceStartValue[] = new double[2*n];//this.determineStartvalue();//new double[2*n];//this.determineStartvalue();
        Arrays.fill(sceStartValue,1.0);
        
        System.out.println("Lower Bound is:" + Arrays.toString(this.lowerBound));
        System.out.println("Upper Bound is:" + Arrays.toString(this.upperBound));
        System.out.println("Start value lb:" + Arrays.toString(expand(sceStartValue)[0]));
        System.out.println("Start value ub:" + Arrays.toString(expand(sceStartValue)[1]));
        System.out.println("Volume of startvalue is:" + volume(expand(sceStartValue)[0],expand(sceStartValue)[1]));
        System.out.println("Penalty of startvalue is:" + constraintPenalty(expand(sceStartValue)[0],expand(sceStartValue)[1]));
        sce.setMaxn(200000);
        sce.setStartValue(sceStartValue);

        sce.setFunction(new AbstractFunction() {

            public int getInputDimension(){
                return 2*n;
            }
            
            public int getOutputDimension(){
                return 1;
            }
            
            @Override
            public double[] evaluate(double[] x) throws SampleLimitException, ObjectiveAchievedException {
                double t[][] = expand(x);
                double l[] = t[0];
                double u[] = t[1];
                return new double[]{-volume(l, u) + constraintPenalty(l, u)};
            }

            @Override
            public void log(String msg) {
                System.out.println(msg);
            }
            
            public String[] getParameterNames(){
                String names[] = new String[2*n];
                for (int i=0;i<n;i++){
                    names[2*i+0] = "li_" + i;
                    names[2*i+1] = "wi_" + i;
                }
                return names;
            }            
            @Override
            public double[][] getRange(){
                double range[][] = new double[2*n][2];
                for (int i=0;i<2*n;i++){
                    range[i][0] = 0.0;
                    range[i][1] = 1.0;
                }
                return range;
            }
        });
        sce.setDebugMode(false);
        sce.setVerbose(false);
        sce.kstop = 500;
        sce.complexesCount = 5;
        ArrayList<Sample> result = sce.optimize();
        System.out.println("Finished optimization of largest empty rectangle ..");
        System.out.println("Result is ..");
        double x[] = result.get(0).x;
        double lopt[] = expand(x)[0];
        double uopt[] = expand(x)[1];
       
        System.out.println("lower boundary:" + Arrays.toString(lopt));
        System.out.println("upper boundary:" + Arrays.toString(uopt));
        System.out.println("volume:" + volume(lopt, uopt));
        System.out.println("penalty:" + constraintPenalty(lopt, uopt));

        this.result = new double[][]{
                    lopt, uopt
                };
    }

    public double[][] getFLER(){
        procedure();
        return result;
    }

    static public double format(double d){
        return ((double)Math.round(d*100))/100.0;
    }

    static public void main(String[] args){
        /*double initialPoint[] = {0,0};
        double lowerBound[] = {-100,-100};
        double upperBound[] = {+100,+100};
        double pointList[][] = {
            {5,50},
            {20,30},
            {-10,56},
            {-50,24},
            {20,20},
            {-65,32}
        };

        FindLargestEmptyRectangle FLER = new FindLargestEmptyRectangle(initialPoint, lowerBound, upperBound, pointList);
        System.out.println(Arrays.toString(FLER.getFLER()));*/

        DataCollection dc = DataCollection.createFromFile(new File("C:/Arbeit/ModelData/Testgebiete/J2000/Gehlberg/output/20120331_013625/complete2.cdat"));
        Set<String> parameterSets = dc.getDatasets(Parameter.class);
        ArrayList<SimpleEnsemble> ps = new ArrayList<SimpleEnsemble>();
        for (String parameterSet : parameterSets){
            ps.add((SimpleEnsemble)dc.getDataSet(parameterSet));
        }
        SimpleEnsemble objective = (SimpleEnsemble)dc.getDataSet("bias_normalized");

        ArrayList<double[]> exclude = new ArrayList<double[]>();
        double dmin = objective.getMin();
        double dthresh = dmin*1.1;
        int n = ps.size();
        for (int i=0;i<objective.getSize();i++){
            int id = objective.getId(i);
            double value = objective.getValue(id);
            if (value > dthresh){
                //System.out.println("Exclude id" + objective.getId(i) + " with value:" +value);
                double excludePoint[] = new double[n];
                for (int j=0;j<n;j++){
                    excludePoint[j] = ps.get(j).getValue(id);
                }
                exclude.add(excludePoint);
            }else{
                System.out.println("Keep id" + objective.getId(i) + " with value:" +value);
            }
        }
        double pointList[][] = exclude.toArray(new double[0][]);

        double lowerBound[] = new double[n];
        double upperBound[] = new double[n];
        double initialPoint[] = new double[n];
        int idbest = objective.findArgMin();
        for (int j=0;j<n;j++){
            lowerBound[j] = ps.get(j).getMin();
            upperBound[j] = ps.get(j).getMax();
            initialPoint[j] = ps.get(j).getValue(idbest);
        }
        FindLargestEmptyRectangle FLER = new FindLargestEmptyRectangle(initialPoint, lowerBound, upperBound, pointList);
        double ranges[][] = FLER.getFLER();
        for (int i=0;i<n;i++){
            System.out.println(ps.get(i).getName() + "=[" + format(ranges[0][i]) + "-" + format(ranges[1][i]) + "]");
        }
    }
}
