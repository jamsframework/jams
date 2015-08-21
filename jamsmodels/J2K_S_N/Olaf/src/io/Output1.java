/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package io;

import oms3.annotations.*;

import java.io.PrintWriter;
import java.io.File;
import java.io.IOException;
import java.util.Calendar;
import java.util.Date;

public class Output1 {

    @Role(Role.PARAMETER + Role.OUTPUT)
    @In public File outFile;

    @In public Calendar time;
    @In public double solRad;
    @In public double rhum;
    @In public double catchmentSimRunoff;
    @In public double catchmentRD1;
    @In public double catchmentRD2;
    @In public double catchmentRG1;
    @In public double catchmentRG2;
    
    PrintWriter w;

    @Execute
    public void execute() {
        if (w == null) {
            try {
                w = new PrintWriter(outFile);
            } catch (IOException E) {
                throw new RuntimeException(E);
            }
            w.println("@T, ceap");
            w.println(" Created, " + new Date());
            String v = System.getProperty("oms3.digest");
            if (v != null) {
                w.println(" Digest," + v);
            }
            w.println("@H, date, solRad, rhum, catchmentSimRunoff, catchmentRD1, catchmentRD2, catchmentRG1, catchmentRG2");
            w.println(" Type, Date, Double, Double, Double, Double, Double, Double, Double");
            w.println(" Format, yyyy-MM-dd,,,,,,,");
        }
        String s = String.format(",%1$tY-%1$tm-%1$td, %2$7.3f, %3$7.3f, %4$7.3f,  %5$7.3f,  %6$7.3f,  %7$7.3f,  %8$7.3f", time, solRad, rhum,
                catchmentSimRunoff, catchmentRD1, catchmentRD2, catchmentRG1, catchmentRG2);
        w.println(s);
    }

//    private static double[] getOutput(Object comp) {
//        for (Object object : col) {
//
//        }
//    }

    @Finalize
    public void done() {
        w.close();
    }
}
