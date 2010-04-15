/*
 * BranchAndBound.java
 *
 * Created on 8. Februar 2008, 10:46
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package jams.components.optimizer;

//muss noch überarbeitet und integriert werden .. .

import java.util.ArrayList;
import jams.model.JAMSComponentDescription;
import jams.JAMS;
import java.util.Vector;

@JAMSComponentDescription(
        title="Branch and Bound Optimizer",
        author="Christian Fischer",
        description="Performs a branch and bound optimization. Advantage: It can be shown, that this method will find the global optimum. Disadvantage: This method usally requires many function evaluations. So it should only be used, if model execution is very fast"
        )
public class Direct extends SOOptimizer{
    Vector<SampleSO> Q = new Vector<SampleSO>();
    
    int lengths[][] = null;
    double fc[] = null;
    double c[][] = null;
    
    double szes[] = null;    
    double l_thirds[] = null;           
    int maxevals = 0;
    int maxdeep = 100;
    double ep = 0.0001;
                      
    @SuppressWarnings("unchecked")
    public Direct() {
    }
            
    private double[] reTransform(double[] in){
        double x[] = new double[n];
        for (int j=0;j<n;j++){
            x[j] = in[j]*(upBound[j]-lowBound[j])+lowBound[j];
        }  
        return x;
    }
    
    public void DirInit(){                
        // length array will store # of slices in each dimension for
        // each rectangle. dimension will be rows; each rectangle
        // will be a column
        
        // first rectangle is the whole unit hyperrectangle
        for (int i=0;i<n;i++){
            lengths[0][i] = 0;
            c[0][i] = 0.5;
        }
        
        // store size of hyperrectangle in vector szes
        szes[0] = 1;
        
        // first element of f is going to be the function evaluated
        // at the center of the unit hyper-rectangle.                         
        fc[0] = this.funct(reTransform(c[0]));
        Q.add(new SampleSO(reTransform(c[0]),fc[0]));       
    }
    
    public void init(){        
        super.init();
        
        if (!enable.getValue())
            return; 
        
        if (x0 != null){
            this.getModel().getRuntime().sendInfoMsg(JAMS.resources.getString("start_value_not_supported_by_branch_and_bound"));
        }
              
        maxevals = this.maxn.getValue();                
                        
        lengths =       new int[maxevals+(int)Math.floor(0.1*maxevals)][n];
        c =             new double[maxevals+(int)Math.floor(0.1*maxevals)][n];
        fc =            new double[maxevals+(int)Math.floor(0.1*maxevals)];
        szes =          new double[maxevals+(int)Math.floor(0.1*maxevals)];                                
        
        l_thirds = new double[maxdeep];
        l_thirds[0] = 1.0/3.0;
        // start by calculating the thirds array
        // here we precalculate (1/3)^i which we will use frequently
        for (int i=1;i<maxdeep;i++){
            l_thirds[i] = l_thirds[i-1]/3.0;
        }                
    }
     
    ArrayList<Integer> find_po(double minval){
        ArrayList<Integer> hull = new ArrayList<Integer>();
        int sum_lengths[] = new int[Q.size()];        
        int tmp_max = Integer.MIN_VALUE;
        
        for (int i=0;i<Q.size();i++){            
            for (int j=0;j<n;j++)
                sum_lengths[i] += lengths[i][j];            
            tmp_max = (int)Math.max(sum_lengths[i],tmp_max);
        }
                                    
        for (int i=1;i<=tmp_max+1;i++){
            double tmp_n = Double.MAX_VALUE;
            
            int min_index = -1;
            for (int k=0;k<sum_lengths.length;k++){                
                if (sum_lengths[k]==i-1 && fc[k]<tmp_n){
                    tmp_n = fc[k];
                    min_index = k;
                }
            }            
            if (min_index != -1){
                hull.add(min_index);
                for (int k=0;k<sum_lengths.length;k++){  
                    if (sum_lengths[k]==i-1 && fc[k]<=tmp_n+1e-13 && k != min_index){
                        hull.add(k);
                    }
                }            
            }
        }
        
        // 2. Compute lb and ub for rects on hub
        double bounds[][] = calc_bounds(hull);
                
        // 3. Find indeces of hull who satisfy
        //    1st condition
        ArrayList<Integer> final_pos = new ArrayList<Integer>();
        for (int i=0;i<bounds.length;i++){
            if (bounds[i][0]-bounds[i][1]>0)
                continue;
            
            if (minval != 0){
                if ( (minval - fc[hull.get(i)])/Math.abs(minval) + szes[hull.get(i)]*bounds[i][1]/Math.abs(minval) >= ep )
                    final_pos.add(hull.get(i));
            }else{
                if (fc[hull.get(i)] + szes[hull.get(i)]*bounds[i][1] <= 0)
                    final_pos.add(hull.get(i));
            }
        }
        return final_pos;
    }
    
    double[][] calc_bounds(ArrayList<Integer>hull){
        double bound[][] = new double[hull.size()][2];                                        
                
        for (int i=0;i<hull.size();i++){
            double sum1[] = new double[hull.size()];
            double sum2 = 0;
            bound[i][0] = Double.MIN_VALUE;
            bound[i][1] = Double.MAX_VALUE;
            
            for (int k=0;k<n;k++){
                for (int j=0;j<hull.size();j++){                
                    sum1[j] += lengths[hull.get(j)][k];
                }
                sum2 += lengths[hull.get(i)][k];
            }
                        
            for (int j=0;j<sum1.length;j++){                                
                double tmp_f = fc[hull.get(j)];
                double tmp_szes = szes[hull.get(j)];
                
                if (sum1[j]>sum2)
                    bound[i][0] = Math.max((fc[hull.get(i)]-tmp_f)/(szes[hull.get(i)]-tmp_szes),bound[i][0]);                              
                if (sum1[j]<sum2)
                    bound[i][1] = Math.min((fc[hull.get(i)]-tmp_f)/(szes[hull.get(i)]-tmp_szes),bound[i][1]);              
            }            
        }
        return bound;
    }
                     
    void DIRDivide(int index){
        int []li = lengths[index];
        double oldc[] = c[index];
        int oldfcncounter = Q.size();
        int biggy = Integer.MAX_VALUE;
        ArrayList<Integer> ls = new ArrayList<Integer>();
        
        // 1. Determine which sides are the largest
        for (int i=0;i<li.length;i++){
            if (li[i]<biggy){
                biggy = li[i];
                ls.clear();
                ls.add(new Integer(i));
            }else if (li[i]==biggy){
                ls.add(new Integer(i));
            }            
        }
                
        double delta = l_thirds[biggy];
     
        // 2. Evaluate function in directions of biggest size
        //    to determine which direction to make divisions
        double newc_left[][] = new double[ls.size()][oldc.length];
        double newc_right[][] = new double[ls.size()][oldc.length];
        double f_left[] = new double[ls.size()];
        double f_right[] = new double[ls.size()];
        
        for (int i=0;i<oldc.length;i++){
            for (int j=0;j<ls.size();j++){
                newc_left[j][i] = oldc[i];
                newc_right[j][i] = oldc[i];
                
                f_left[j] = 0.0;
                f_right[j] = 0.0;
            }
        }
        
        for (int i=0;i<ls.size();i++){
            int lsi = ls.get(i);
            
            newc_left[i][lsi] -= delta;           
            newc_right[i][lsi] += delta;           
            
            double x1[] = new double[n];
            double x2[] = new double[n];
            
            x1 = reTransform(newc_left[i]);
            x2 = reTransform(newc_right[i]);                        
            f_left[i] = this.funct(x1);
            f_right[i] = this.funct(x2);
            Q.add(new SampleSO(reTransform(x1),f_left[i]));
            Q.add(new SampleSO(reTransform(x2),f_right[i]));                        
        }
        double w[][] = new double[ls.size()][3];
        for (int i=0;i<ls.size();i++){
            w[i][0] = Math.min(f_left[i], f_right[i]);
            w[i][1] = ls.get(i).intValue();
            w[i][2] = i;
        }
        
        SCE_Comparator comparator = new SCE_Comparator(0,false);
        java.util.Arrays.sort(w,comparator);
        
        // 4. Make divisions in order specified by order
        for (int i=0;i<ls.size();i++){
            int newleftindex  = oldfcncounter+2*(i);
            int newrightindex    = oldfcncounter+2*(i)+1;
        // 4.1 create new rectangles identical to the old one
            int oldrect[] = lengths[index];            
            for (int j=0;j<n;j++){
                lengths[newleftindex][j] = oldrect[j];
                lengths[newrightindex][j] = oldrect[j];
            }            
        // old, and new rectangles have been sliced in order(i) direction
            
            lengths[newleftindex][ls.get((int)w[i][2])]  = lengths[index][ls.get((int)w[i][2])] + 1;
            lengths[newrightindex][ls.get((int)w[i][2])] = lengths[index][ls.get((int)w[i][2])] + 1;
            lengths[index][ls.get((int)w[i][2])]         = lengths[index][ls.get((int)w[i][2])] + 1;
   

        // add new columns to c
            c[newleftindex] = newc_left[(int)w[i][2]];
            c[newrightindex] = newc_right[(int)w[i][2]];
   
        // add new values to fc
            fc[newleftindex]  = f_left[(int)w[i][2]];
            fc[newrightindex] = f_right[(int)w[i][2]];

        // store sizes of each rectangle
            szes[newleftindex] = 0;
            szes[newrightindex] = 0;
            for (int j=0;j<n;j++){
                szes[newleftindex] += Math.pow(1.0/3.0, 2.0*lengths[newleftindex][j]);
                szes[newrightindex] += Math.pow(1.0/3.0, 2.0*lengths[newrightindex][j]);
            }            
            szes[newleftindex] = 0.5*Math.sqrt(szes[newleftindex]);
            szes[newrightindex] = 0.5*Math.sqrt(szes[newrightindex]);
        }
        szes[index] = 0;
        for (int j=0;j<n;j++){
            szes[index] += Math.pow(1.0/3.0, 2.0*lengths[index][j]);                
        }      
        szes[index] = 0.5*Math.sqrt(szes[index]);
    }
    
    public void run() {
        if (!this.enable.getValue()){
            singleRun();
            return;
        }
        
        DirInit();
               
        double minval = fc[0];
        double[] xatmin = new double[2];
        xatmin[0] = c[0][0];
        xatmin[1] = c[0][1];
        int iter = 0;
        while (Q.size()<maxn.getValue()){
            ArrayList<Integer> final_pos = find_po(minval);
            
            for (int i=0;i<final_pos.size();i++){
                DIRDivide(final_pos.get(i).intValue());
            }            
            for (int i=0;i<Q.size();i++){                
                if (fc[i]<minval){
                    minval = fc[i];      
                    xatmin = reTransform(c[i]);                    
                }
            }
            System.out.print("Iter:\t" + iter++);
            System.out.print("\tfmin:\t" + minval);
            System.out.print("\tfn_evals:\t" + Q.size());
            System.out.print("\tmin_pos:\t[");
            for (int j=0;j<n;j++){                        
                System.out.print(xatmin[j] + ",");
            }
            System.out.println("]");            
        }                
    }    
}
