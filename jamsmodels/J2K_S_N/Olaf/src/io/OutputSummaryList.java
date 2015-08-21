/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package io;

import oms3.annotations.*;

import java.io.PrintWriter;
import java.io.File;
import java.text.DecimalFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import oms3.Conversions;

public class OutputSummaryList {

    static Map<Class,String> m= new HashMap<Class,String>();
    static {
        m.put(Double.class, "Double");
        m.put(Integer.class, "Integer");
        m.put(int.class, "Integer");
        m.put(double.class, "Double");
        m.put(Calendar.class, "Date");
        m.put(GregorianCalendar.class, "Date");
    }

    static Map<Class,String> fmt= new HashMap<Class,String>();
    static {
        fmt.put(Double.class, "");
        fmt.put(Integer.class, "");
        fmt.put(int.class, "");
        fmt.put(double.class, "");
        fmt.put(Calendar.class, "yyyy-MM-dd");
        fmt.put(GregorianCalendar.class, "yyyy-MM-dd");
    }

    @In public List list;

    @Role(Role.PARAMETER + Role.OUTPUT)
    @In public File outFile;

    @Role(Role.PARAMETER)
    @In public String attrSet;

    // starting with output
    @Role(Role.VARIABLE + Role.OUTPUT)
    @In public Calendar time;

    PrintWriter w;
    String[] out;

    boolean disabled = false;

    @Execute
    public void execute() throws Exception {
        if (disabled) {
            return;
        }
        if (w == null) {
            if (outFile.getName().equals("-")) {
                disabled = true;
                return;
            }
            out = attrSet.split("\\s*;\\s*");
            w = new PrintWriter(outFile);
            w.println("@T, output");
            w.println(" Created, \"" + new Date() + "\"");
            String v = System.getProperty("oms3.digest");
            if (v != null) {
                w.println(" Digest," + v);
            }
            printHeader();
        }
        printVals();
    }

    private void printHeader() {
        w.print("@H, time");
        for (String field : out) {
            w.print(", " + field);
        }
        w.println();
        w.print(" Type, Date");
        for (String field : out) {
            w.print(", Double");
        }
        w.println();
    }

    private void printVals() throws Exception {
        String t = format(time);
        for (Object object : list) {
            w.print("," + t);
            for (String f : out) {
                w.print("," + format(object.getClass().getField(f).get(object)));
            }
            w.println();
        }
    }

    DecimalFormat df = new DecimalFormat("##0.#####");
    
    private String format(Object val) {
        if (val instanceof Number) {
            return df.format(val);
        }
        return Conversions.convert(val, String.class);
    }

    @Finalize
    public void done() {
        if (w!=null) {
            w.close();
        }
    }

//    public static void main(String[] args) throws Exception {
//        OutputSummary o = new OutputSummary();
//        o.outFile = new File("c:/tmp/a.csv");
//        o.time = new GregorianCalendar();
//        o.solRad = 2.34;
//        o.execute();
//        o.execute();
//        o.done();
//    }
}
