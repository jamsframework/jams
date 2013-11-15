package jams.worldwind.test;

import java.util.Random;

/**
 *
 * @author Ronny Berndt <ronny.berndt@uni-jena.de>
 */
public class RandomNumbers {
    
    private final double[] values;

    public RandomNumbers(double from, double to, int count) {
        this.values = new double[count];
        fill(from,to);
    }

    private void fill(double from, double to) {
        Random rnd = new Random(System.currentTimeMillis());
        for(int i=0;i<this.values.length;i++) {
            this.values[i]=from + rnd.nextDouble() *(to-from);
        }
    }
    
    public double[] getDoubleValues() {
        return this.values;
    }
    
    public int[] getIntegerValues() {
        int[] intvalues = new int[values.length];
        for(int i=0;i<intvalues.length;i++) {
            intvalues[i] = (int) this.values[i];
        }
        return intvalues;
    }
}
