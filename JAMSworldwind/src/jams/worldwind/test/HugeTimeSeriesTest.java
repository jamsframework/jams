/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package jams.worldwind.test;



/**
 *
 * @author Ronny Berndt <ronny.berndt at uni-jena.de>
 */
public class HugeTimeSeriesTest {


    
    public HugeTimeSeriesTest() {

        double[] a = new double[40000000];

        for(int i=0;i<40000000;i++) {
            //System.out.println(d.toString());
            //series.add(s, 1.0);
            //s=(Second)s.next();
            a[i] = 1.0;
            if(i % 1000000 == 0)
                System.out.println(i);
        }
        
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        new HugeTimeSeriesTest();
    }
    
}
