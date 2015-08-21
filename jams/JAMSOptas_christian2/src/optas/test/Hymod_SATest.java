/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package optas.test;

import optas.SA.SobolsMethod;
import optas.SA.SobolsMethod.Measure;
import optas.core.AbstractFunction;
import optas.core.ObjectiveAchievedException;
import optas.core.SampleLimitException;

/**
 *
 * @author christian
 */
public class Hymod_SATest {
    
    public static void main(String[] args) {
        
        SobolsMethod rsa = new SobolsMethod(Measure.Total);
        rsa.setModel(LeafRiverExample.getModel());
        rsa.setSampleSize(10000);
        rsa.calculate();
        
        for (int i=0;i<5;i++){
            System.out.println("Normalized senstivity of " + rsa.getModel().getInputFactorNames()[i] + " is: " + rsa.getSensitivity(i));
        }        
        
        System.exit(0);
    }
}
