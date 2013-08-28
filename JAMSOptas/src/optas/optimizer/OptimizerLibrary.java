/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package optas.optimizer;

import jams.JAMS;
import jams.runtime.JAMSRuntime;
import optas.optimizer.management.OptimizerDescription;
import optas.optimizer.management.NumericOptimizerParameter;
import java.util.Set;
import java.util.TreeSet;

/**
 *
 * @author chris
 */
public class OptimizerLibrary {
    static Set<Optimizer> optimizerPool= new TreeSet<Optimizer>();

    public static void register(Optimizer clazz) {
        if (clazz instanceof optas.optimizer.Optimizer) {
            optimizerPool.add(clazz);
        }
    }

    static public Set<Optimizer> getAvailableOptimizer() {
        optimizerPool.clear();
        optimizerPool.add(new optas.optimizer.Direct());
        optimizerPool.add(new optas.optimizer.SCE());
        optimizerPool.add(new optas.optimizer.BranchAndBound());
        optimizerPool.add(new optas.optimizer.DREAM());
        optimizerPool.add(new optas.sampler.LatinHyperCubeSampler());
        optimizerPool.add(new optas.optimizer.MOCOM());
        optimizerPool.add(new optas.optimizer.NSGA2());
        optimizerPool.add(new optas.optimizer.NelderMead());
        optimizerPool.add(new optas.sampler.RandomSampler());
        optimizerPool.add(new optas.sampler.SobolsSequence());
        optimizerPool.add(new optas.sampler.HaltonSequence());
        
        return optimizerPool;
    }
    static public Optimizer getDefaultOptimizer(){
        return new optas.optimizer.Direct();
    }

    static public OptimizerDescription getDefaultOptimizerDescription(String shortName, String className, int id, boolean multiObj) {
        OptimizerDescription defDesc = new OptimizerDescription(shortName, id, multiObj);
        defDesc.setOptimizerClassName(className);
        defDesc.addParameter(new NumericOptimizerParameter(
                "maxn", JAMS.i18n("maximum_number_of_iterations"),
                500, 1, 100000));
        
        return defDesc;
    }

    static public Optimizer loadOptimizer(ClassLoader cl, String name) {
        Class optimizerClass = null;
        Object objOptimizer = null;
        try {
            optimizerClass = cl.loadClass(name);
            objOptimizer = optimizerClass.newInstance();
        } catch (ClassNotFoundException cnfe) {
            System.out.println("could not find optimizer class, " + name);
            cnfe.printStackTrace();
            return null;
        } catch (InstantiationException ie) {
            System.out.println("could not instantiate optimizer class, " + name);
            ie.printStackTrace();
            return null;
        } catch (IllegalAccessException ie) {
            System.out.println("could not instantiate optimizer class, " + name);
            ie.printStackTrace();
            return null;
        }

        if (!(objOptimizer instanceof optas.optimizer.Optimizer)) {
            System.out.println("class " + name + " is not assignable of optas.Optimizer!");
            return null;
        }

        return (optas.optimizer.Optimizer) objOptimizer;
    }

    static public Optimizer getOptimizer(String className){
        for (Optimizer o : getAvailableOptimizer()){
            if (o.getDescription().getOptimizerClassName().equals(className))
                return o;
        }
        return null;
    }
    
    static public Optimizer loadOptimizer(JAMSRuntime rt, String name) {
        Class optimizerClass = null;
        Object objOptimizer = null;
        try {
            optimizerClass = rt.getClassLoader().loadClass(name);
            objOptimizer = optimizerClass.newInstance();
        } catch (ClassNotFoundException cnfe) {
            rt.sendHalt("could not find optimizer class, " + name);
            cnfe.printStackTrace();
            return null;
        } catch (InstantiationException ie) {
            rt.sendHalt("could not instantiate optimizer class, " + name);
            ie.printStackTrace();
            return null;
        } catch (IllegalAccessException ie) {
            rt.sendHalt("could not instantiate optimizer class, " + name);
            ie.printStackTrace();
            return null;
        }

        if (!(objOptimizer instanceof optas.optimizer.Optimizer)) {
            rt.sendHalt("class " + name + " is not assignable of optas.Optimizer!");
            return null;
        }

        return (optas.optimizer.Optimizer) objOptimizer;
    }
}
