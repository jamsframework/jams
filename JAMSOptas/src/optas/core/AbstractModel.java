/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package optas.core;

/**
 *
 * @author christian
 */
    abstract public class AbstractModel{
        
        abstract public int getInputDimension();
        abstract public int getOutputDimension();
        
        abstract public double[][] getRange();
        
        public String[] getInputFactorNames(){
            String defaultNames[] = new String[getInputDimension()];
            for (int i=0;i<defaultNames.length;i++){
                defaultNames[i] = "p_" + i;
            }
            return defaultNames;
        }
        public String[] getOutputFactorNames(){
            String defaultNames[] = new String[getOutputDimension()];
            for (int i=0;i<defaultNames.length;i++){
                defaultNames[i] = "f(x)_" + i;
            }
            return defaultNames;
        }
                
        abstract public void log(String msg);
    }
