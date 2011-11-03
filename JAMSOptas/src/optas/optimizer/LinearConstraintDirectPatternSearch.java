/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package optas.optimizer;
import Jama.Matrix;
import java.util.Vector;
import optas.optimizer.management.SampleFactory.SampleSO;


/**
 *
 * @author Christian Fischer
 */
public class LinearConstraintDirectPatternSearch {
    Matrix A;
    Matrix b;
    
    int n;
    
    public void SetLinearConstraints(Matrix A,Matrix b){
        this.A = A;
        this.b = b;
        
        n = A.getColumnDimension();
    }
    
    public boolean FeasibleDirection(Matrix x,Matrix d,double delta){        
        Matrix b_star = A.times(x.plus(d.times(delta)));
            
        for (int k=0;k<A.getRowDimension();k++){
            if ( b_star.get(k,0) > b.get(k,0)){
                return false;
            }                                                                
        }
        return true;
    }
    
    public Vector<Matrix> UpdateDirections(SampleSO x,Matrix P,double delta){                        
        Vector<Matrix> P_k = new Vector<Matrix>();        
        Matrix mx = new Matrix(x.x,x.x.length);
        //1. check if p_k direction can be used
        for (int i=0;i<P.getColumnDimension();i++){
            boolean isFeasible = true;
            Matrix P_i = P.getMatrix(0,P.getRowDimension()-1,i,i);
                                                
            if (FeasibleDirection(mx,P_i,delta)){
                P_k.add(P_i.times(delta));
            }            
        }
        if (true)
            return P_k;
        //2. use boundary direction (project p_k onto boundary??)   
        
        //we can construct the so called "tangent cone" through active index set
        //determination
        boolean fullRowRank = false;
        Matrix V = null;
        
        while(!fullRowRank){
            Vector<Integer> ActiveIndexSet = new Vector<Integer>();
        
            Matrix x_mat = new Matrix(x.x,1);
            for(int i=0;i<A.getRowDimension();i++){
                Matrix a_i = new Matrix(n,1);
                for (int j=0;j<n;j++){
                    a_i.set(j, 0, A.get(i, j));
                }            
                double bx = x_mat.times(a_i).get(0,0);
        
                double d = Math.abs(bx-b.get(i, 0));
                        
                if (d < delta)
                    ActiveIndexSet.add(i);            
            }
            //no boundary in sight .. so skip rest of procedure
            if (ActiveIndexSet.size() == 0){
                return P_k;
            }
            
            V = new Matrix(ActiveIndexSet.size(),n);
            for (int j=0;j<ActiveIndexSet.size();j++){
                int index = ActiveIndexSet.get(j);
                for (int k=0;k<n;k++){                    
                    V.set(j, k, A.get(index, k));
                }
            }
            if ( V.rank() >= V.getRowDimension())
                fullRowRank = true;
            else
                delta *= 0.9;
        }
        
        V = V.transpose();
        Matrix VTV = V.transpose().times(V);        
        Matrix V_VTV_i = V.times(VTV.inverse());
        Matrix tmp = V_VTV_i.times(V.transpose());
        Matrix eye = Matrix.identity(tmp.getColumnDimension(), tmp.getColumnDimension());
        Matrix N = eye.minus(tmp);
        
        Matrix Gamma[] = new Matrix[3];
        
        Gamma[0] = N;
        Gamma[1] = N.uminus();
        Gamma[2] = V_VTV_i.uminus();
        //check if direction in gamma are really possible
        boolean DirectionAdded = false;
        double delta_bound = delta;
        while (!DirectionAdded){
            for (int j=0;j<3;j++){
                Matrix currentGamma = Gamma[j];
                for (int i=0;i<currentGamma.getColumnDimension();i++){
                    Matrix Gamma_i = currentGamma.getMatrix(0,currentGamma.getRowDimension()-1,i,i);
            
                    if (Gamma_i.norm1() < 0.000001){
                        continue;
                    }
                    
                    if (FeasibleDirection(mx,Gamma_i,delta_bound)){
                        P_k.add(Gamma_i.times(delta_bound));
                        DirectionAdded = true;
                    }
                    if (FeasibleDirection(mx,Gamma_i.uminus(),delta_bound)){
                        P_k.add(Gamma_i.times(delta_bound).uminus());
                        DirectionAdded = true;
                    }
                }
            }
            if (!DirectionAdded){
                delta_bound /= 2.0;
            }
        }        
        return P_k;
    }
    
    public void search(SampleSO Iterate){
        SampleSO nextIterate = Iterate;
        boolean successSearch = false;
        
    }
}
  




