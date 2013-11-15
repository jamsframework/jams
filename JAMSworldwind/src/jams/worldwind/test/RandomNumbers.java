package jams.worldwind.test;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 *
 * @author Ronny Berndt <ronny.berndt@uni-jena.de>
 */
public class RandomNumbers {
    
    private final List<Double> valueList;
    //private final double[] values;

    public RandomNumbers(double from, double to, int count) {
        //this.values = new double[count];
        this.valueList = new ArrayList<>(count);
        fill(from,to,count);
    }

    private void fill(double from, double to, int count) {
        Random rnd = new Random(System.currentTimeMillis());
        for(int i=0;i<count;i++) {
            //this.values[i]=from + rnd.nextDouble() * (to-from);
            valueList.add(from + rnd.nextDouble() * (to-from));
        }
    }
    
    public List<?> getDoubleValues() {
        return this.valueList;
    }
    
    public Object[] getIntegerValues() {
        Object[] intvalues = new Object[valueList.size()];
        for(int i=0;i<intvalues.length;i++) {
            intvalues[i] = (Object) Math.round(this.valueList.get(i));
        }
        return intvalues;
    }
    
    
}
