/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package optas.test;

import java.util.Arrays;
import optas.core.AbstractFunction;
import optas.core.AbstractModel;
import optas.core.ObjectiveAchievedException;
import optas.core.SampleLimitException;
import optas.efficiencies.LogLikelihood;

/**
 *
 * @author christian
 */
public class LeafRiverExample {
    
    static AbstractFunction f = new AbstractFunction() {

            @Override
            public double[] evaluate(double[] x) throws SampleLimitException, ObjectiveAchievedException {
                double result[] = LeafRiverExample.LeafRiverTest(x[0], x[1], x[2], x[3], x[4], 3);
                if (Double.isNaN(result[0]) || Double.isInfinite(result[0]) || result[0] > 100){
                    return new double[]{100.0,100.0};
                }else{
                    return result;
                }
            }

            @Override
            public String[] getInputFactorNames(){
                return new String[]{"alpha", "bexp", "cmax", "kq", "ks"};
            }
            
            public String[] getOutputFactorNames(){
                return new String[]{"e1","e2"};
            }
            
            @Override
            public int getInputDimension() {
                return 5;
            }

            @Override
            public int getOutputDimension() {
                return 2;
            }

            @Override
            public double[][] getRange() {
                return new double[][]{
                    {0.1,0.99}, //alpha
                    {0.1,2.0}, //beta
                    {1.0,500}, //cmax
                    {0.1,0.99}, //kq
                    {0.0,0.1}  //ks
                };
            }

            @Override
            public void log(String msg) {
                System.out.println(msg);
            }
        };
    
    static public AbstractModel getModel(){
        return f;
    }
    
    static public double[] LeafRiverTest(double alpha, double bexp, double cmax, double kq, double ks, double nq){
        //create components
        DataReader reader = new DataReader();
        HymodVrugt hymod = new HymodVrugt();
        LogLikelihood logLikelihood = new LogLikelihood();
        NashSutcliffe e1 = new NashSutcliffe(1);
        NashSutcliffe e2 = new NashSutcliffe(2);
        
        //setup data
        reader.fileName = "E:/ModelData/Testgebiete/Hymod/LeafCatch2.dat";
        hymod.alpha = alpha;
        hymod.bexp = bexp;
        hymod.cmax = cmax;
        hymod.kq = kq;
        hymod.ks = ks;
        hymod.nq = nq;
        
        reader.init();
        hymod.init();
        //run it 5000 timesteps
        int TIMESTEP_COUNT_TOTAL = 4110;
        int TIMESTEP_COUNT_INITIAL = 1554;

        double obsQ[] = new double[TIMESTEP_COUNT_TOTAL-TIMESTEP_COUNT_INITIAL];
        double simQ[] = new double[TIMESTEP_COUNT_TOTAL-TIMESTEP_COUNT_INITIAL];
        
        for (int i = 0; i<TIMESTEP_COUNT_TOTAL; i++){
            reader.run();
            
            hymod.precip = reader.precip;
            hymod.pet = reader.pet;
            hymod.run();
            
            if (i > TIMESTEP_COUNT_INITIAL){
                simQ[i-TIMESTEP_COUNT_INITIAL] = hymod.mq;
                obsQ[i-TIMESTEP_COUNT_INITIAL] = reader.obsRunoff;
            }
        }
        
        return new double[]{
            //logLikelihood.calc(obsQ, simQ),
            e1.calcNormative(obsQ, simQ),
            e2.calcNormative(obsQ, simQ)
        };        
    }
    
    public static void main(String[] args) {
        System.out.println(Arrays.toString(LeafRiverTest(0.98,0.58,293,0.48,0.0001,3)));
        
    }
}
