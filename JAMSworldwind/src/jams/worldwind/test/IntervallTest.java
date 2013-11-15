package jams.worldwind.test;

import jams.worldwind.ui.ColorRamp;
import jams.worldwind.ui.ColorRampPanel;
import java.awt.Color;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.swing.JFrame;
import org.apache.commons.math3.stat.descriptive.SummaryStatistics;

/**
 *
 * @author Ronny Berndt <ronny.berndt@uni-jena.de>
 */
public class IntervallTest {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        new IntervallTest();
    }

    public IntervallTest() {
        int numbers = 171;
        //double [] test = {0.05,0.24,0.25,0.285,0.49,0.50,0.51,0.74,0.76,0.99};
        RandomNumbers rn = new RandomNumbers(0, 100, numbers);
        IntervallCalculation ic = new IntervallCalculation(rn.getDoubleValues());

        System.out.println("Minimum : " + ic.getMinimumValue());
        System.out.println("Maximum : " + ic.getMaximumValue());
        System.out.println("Mean    : " + ic.getMean());
        System.out.println("Median  : " + ic.getMedian());
        System.out.println("Variance: " + ic.getVariance());
        System.out.println("Std.Dev.: " + ic.getStandardDeviation());

        // Get a SummaryStatistics instance
        SummaryStatistics stats = new SummaryStatistics();
        for(int i=0;i<ic.getValues().size();i++) {
            stats.addValue(ic.getValue(i));
        }
        
        System.out.println(stats.getMin());
        System.out.println(stats.getMax());
        System.out.println(stats.getMean());
        System.out.println(stats.getVariance());
        System.out.println(stats.getStandardDeviation());
        
        int classes = 5;
        double width = 15;

        List list = new ArrayList(ic.getValues());
        Collections.sort(list);
        //System.out.println("Wert   : " + list.get(numbers/2-1));
        //System.out.println("Wert   : " + list.get(numbers/2));
        System.out.println("Intervall  : " + ic.getEqualIntervall(classes));
        System.out.println("Intervall  : " + ic.getDefinedIntervall(width));
        System.out.println("Intervall  : " + ic.getQuantilIntervall(classes));
        System.out.println("List: " + list);
        System.out.println("################################################");
        System.out.println("EqualIntervall");
        System.out.println("################################################");
        ic.printHistogramm(ic.getEqualIntervall(classes));
        System.out.println("################################################");
        System.out.println("DefiniedIntervall");
        System.out.println("################################################");
        ic.printHistogramm(ic.getDefinedIntervall(width));
        System.out.println("################################################");
        System.out.println("QuantilIntervall");
        System.out.println("################################################");
        ic.printHistogramm(ic.getQuantilIntervall(classes));

        ColorRamp c = new ColorRamp(new Color(255,0,0), new Color(255,255,0), 20);
        JFrame f = new JFrame("COLORRAMP TEST");
        f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        f.add(new ColorRampPanel(c));
        f.setSize(600,400);
        f.setVisible(true);
        
        
    }

}
