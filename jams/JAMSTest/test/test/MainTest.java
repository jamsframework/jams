/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package test;

import jams.test.J2KFileComparator;
import jams.test.J2KFileComparator.Report;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Properties;
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
                    new File("../../../modeldata/JAMS-Gehlberg/output/current/TimeLoop22.dat"),
                    new File("../../../modeldata/JAMS-Gehlberg/test/reference/TimeLoop.dat"),0.001);
            report.print(System.out);
            org.junit.Assert.assertTrue(report.getFailure()==null);
            org.junit.Assert.assertFalse(report.isReportEmpty());
        }catch(Exception e){
            org.junit.Assert.fail("Exception while comparing Gehlberg - Results:" + e.toString());
        }
    }

     public void testCalibration(){
        System.out.println("performing Calibration Test(NSGA - Dixon-1)");

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
                    new File("../../../modeldata/Calibration/test/reference/reference.dat"),0.005);
            report.print(System.out);
            org.junit.Assert.assertFalse(report.isReportEmpty());
            org.junit.Assert.assertFalse(report.isReportEmpty());
        }catch(Exception e){
            org.junit.Assert.fail("Exception while comparing Calibration - Results:" + e.toString());
        }

        System.out.println("performing Calibration Test(Direct - J2K - Gehlberg -Lumped)");

        System.out.println(System.getProperty("user.dir"));
        String args2[] = new String[]{
            "-c", "../test.jap",
            "-m", "../../../modeldata/Calibration/JAMS-Gehlberg-Lumped/j2k_gehlberg_calibration.jam"
        };
        jamsui.launcher.JAMSui.main(args2);

        //test output
        try{
            Report report = J2KFileComparator.compare(
                    new File("../../../modeldata/Calibration/JAMS-Gehlberg-Lumped/output/current/result.dat"),
                    new File("../../../modeldata/Calibration/JAMS-Gehlberg-Lumped/test/reference/reference.dat"),0.002);
            report.print(System.out);
            org.junit.Assert.assertFalse(report.isReportEmpty());
            org.junit.Assert.assertFalse(report.isReportEmpty());
        }catch(Exception e){
            org.junit.Assert.fail("Exception while comparing Calibration - Results:" + e.toString());
        }
     }
    /**
     * Test of main method, of class Main.
     */
    public ArrayList<File> searchTestFiles(File srcDirectory){
        ArrayList<File> list = new ArrayList<File>();
        if (srcDirectory.exists()){
            for (File file : srcDirectory.listFiles()){
                if (file.isDirectory())
                    list.addAll(searchTestFiles(file));
                else{
                    if (file.getName().endsWith("jam")){
                        list.add(file);
                    }
                }
            }
        }
        return list;
    }

    public boolean compareDirectory(File dirTest, File dirReference){
        for (File referenceFile : dirReference.listFiles()){
            File testFile = new File(dirTest.getAbsoluteFile() + "/" + referenceFile.getName());

            if (!referenceFile.getAbsolutePath().endsWith("*.dat"))
                continue;
            if (!testFile.exists()){
                System.err.println("file:" + testFile + " does not exists!");
                return false;
            }else{
                try{
                    Report report = J2KFileComparator.compare(
                        testFile,
                        referenceFile,0.005);
                    report.print(System.out);
                    if (report.getFailure()!=null){
                        return false;
                    }
                    if (!report.isReportEmpty()){
                        return false;
                    }
                }catch(Exception e){
                    org.junit.Assert.fail("Exception while comparing Calibration - Results:" + e.toString());
                    return false;
                }
            }

        }
        return true;
    }
    public void runTest(File modelFile){
        System.out.println("performing Test:" + modelFile.getAbsolutePath());
        System.out.println(System.getProperty("user.dir"));
        String args[] = new String[]{
            "-c", "../test.jap",
            "-m", modelFile.getAbsolutePath()
        };
        jamsui.launcher.JAMSui.main(args);

        org.junit.Assert.assertTrue(
                compareDirectory(new File(modelFile.getParent()+"/output/current/"),
                new File(modelFile.getParent()+"/test/reference/")));
    }

    @Test
    public void testMain() {
        //try to read property-file
        Properties props = new Properties();
        try{
            props.load(new FileReader("test.properties"));
        }catch(Exception e){
            org.junit.Assert.fail("could not open test property file");
        }
        String testDataPath = (String)props.get("test-data-path");
        System.out.println("test-data-path:" + testDataPath);
        org.junit.Assert.assertFalse(testDataPath==null);

       ArrayList<File> list = searchTestFiles(new File(testDataPath));
       for (File file : list){
           runTest(file);
       }
        //testJ2KGehlberg();
        //testCalibration();
    }

    
}