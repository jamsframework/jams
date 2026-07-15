/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package optas.test;

import java.util.ArrayList;
import java.util.Arrays;
import optas.core.AbstractFunction;
import optas.core.ObjectiveAchievedException;
import optas.core.SampleLimitException;
import optas.optimizer.NSGA2;
import optas.optimizer.management.SampleFactory;

/**
 *
 * @author christian
 */
public class Hymod_OptimizerTest {

    public static void main(String[] args) {
        NSGA2 nsga = new NSGA2();
        nsga.setFunction((AbstractFunction)LeafRiverExample.getModel());
        nsga.setCrossoverDistributionIndex(20);
        nsga.setCrossoverProbability(0.9);
        nsga.setMaxGeneration(100);
        nsga.setMaxn(5000);
        nsga.setMutationDistributionIndex(20);
        nsga.setMutationProbability(1.0);
        nsga.setPopulationSize(50);
        nsga.setVerbose(true);
        
        nsga.init();
        try {
            nsga.procedure();
        } catch (SampleLimitException sle) {
            System.out.println("Finished because SampleLimit exceded!");
        } catch (ObjectiveAchievedException oae) {
            //...
        }

        ArrayList<SampleFactory.Sample> solution = nsga.getSolution();
        for (int i = 0; i < solution.size(); i++) {
            System.out.println(Arrays.toString(solution.get(i).x) + Arrays.toString(solution.get(i).F()));
        }
    }
}
