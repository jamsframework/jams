/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package jams.test;

import jams.test.J2KFileComparator.Report;
import java.io.File;

/**
 *
 * @author chris
 */
public class Main {

    /**
     * @param args the command line arguments
     */    
        public static void main(String arg[]){
            System.out.println("performing Calibration Test");

        System.out.println(System.getProperty("user.dir"));
        String args[] = new String[]{
            "-c", "../test.jap",
            "-m", "../../../modeldata/Calibration/dixon1.jam"
        };
        //jamsui.launcher.JAMSui.main(args);

        //test output
        try{
            Report report = J2KFileComparator.compare(                    
                    new File("../../../modeldata/calibration/test/reference/reference.dat"),
                    new File("../../../modeldata/calibration/output/current/result.dat"),0.001);
            report.print(System.out);
            org.junit.Assert.assertFalse(report.isReportEmpty());
        }catch(Exception e){
            org.junit.Assert.fail("Exception while comparing Calibration - Results:" + e.toString());
        }
    }

}
