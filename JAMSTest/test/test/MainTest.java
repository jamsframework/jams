/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package test;

import jams.test.J2KFileComparator;
import jams.test.J2KFileComparator.Report;
import java.io.File;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 *
 * @author chris
 */
public class MainTest {

    public MainTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    public void testJ2KGehlberg(){
        System.out.println("performing JAMS - Gehlberg Test");

        System.out.println(System.getProperty("user.dir"));
        String args[] = new String[]{
            "-c", "../test.jap",
            "-m", "../../../modeldata/JAMS-Gehlberg/j2k_gehlberg.jam"
        };
        jamsui.launcher.JAMSui.main(args);

        //test output
        try{
            Report report = J2KFileComparator.compare(
                    new File("../../../modeldata/JAMS-Gehlberg/output/current/TimeLoop.dat"),
                    new File("../../../modeldata/JAMS-Gehlberg/test/reference/TimeLoop.dat"),0.001);
            report.print(System.out);
            org.junit.Assert.assertFalse(report.isReportEmpty());
        }catch(Exception e){
            org.junit.Assert.fail("Exception while comparing Gehlberg - Results:" + e.toString());
        }
    }

     public void testCalibration(){
        System.out.println("performing Calibration Test");

        System.out.println(System.getProperty("user.dir"));
        String args[] = new String[]{
            "-c", "../test.jap",
            "-m", "../../../modeldata/Calibration/dixon1.jam"
        };
        jamsui.launcher.JAMSui.main(args);

        //test output
        try{
            Report report = J2KFileComparator.compare(
                    new File("../../../modeldata/Calibration/output/current/result.dat"),
                    new File("../../../modeldata/Calibration/test/reference/reference.dat"),0.001);
            report.print(System.out);
            org.junit.Assert.assertFalse(report.isReportEmpty());
        }catch(Exception e){
            org.junit.Assert.fail("Exception while comparing Calibration - Results:" + e.toString());
        }
     }
    /**
     * Test of main method, of class Main.
     */
    @Test
    public void testMain() {
        testJ2KGehlberg();
        testCalibration();
    }

}