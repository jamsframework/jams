/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package optas.optimizer.management;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.StringTokenizer;
import optas.gui.wizard.OPTASWizardException;

/**
 *
 * @author chris
 */
public class Tools {

    public static class Rectangle implements Serializable{
        public double lb[];
        public double ub[];

        public double[] m(){
            double x[] = new double[lb.length];
            for (int i=0;i<lb.length;i++)
                x[i] = (lb[i] + ub[i])/2.0;
            return x;
        }
    }

     public static double dist(double p1[],double p2[], double lb[], double ub[]){
        double d = 0;
        for (int i=0;i<p1.length;i++)
            d+=((p1[i]-p2[i])*(p1[i]-p2[i])) / (ub[i]-lb[i]);
        return d;

    }

    public static double[] clamp(Rectangle rect, double p[]){
        double clamp[] = new double[p.length];
        for (int j=0;j<p.length;j++){
            if (p[j] < rect.lb[j])
                clamp[j] = rect.lb[j];
            else if (p[j] > rect.ub[j])
                clamp[j] = rect.ub[j];
            else
                clamp[j] = p[j];

        }
        return clamp;
    }

    static public double[][] parseStringTo2dArray(String in) {
        int n = -1;
        int counter = 0;
        in = in.replaceAll("\n", "");
        in = in.replaceAll(" ", "");

        StringTokenizer tokStartValue = new StringTokenizer(in, ";");

        ArrayList<double[]> list = new ArrayList<double[]>();

        while (tokStartValue.hasMoreTokens()) {
            String param = tokStartValue.nextToken();

            param = param.replace("[", "");
            param = param.replace("]", "");

            StringTokenizer subTokenizer = new StringTokenizer(param, ",");

            double xi[] = new double[subTokenizer.countTokens()];

            if (n != -1) {
                if (xi.length != n) {
                    return null;
                }
            } else {
                n = subTokenizer.countTokens();
            }

            int subCounter = 0;
            while (subTokenizer.hasMoreTokens()) {
                try {
                    xi[subCounter] = Double.valueOf(subTokenizer.nextToken()).doubleValue();
                    subCounter++;
                } catch (NumberFormatException e) {
                    return null;
                }
            }
            list.add(xi);
            counter++;
        }
        double result[][] = new double[list.size()][n];
        for (int i = 0; i < list.size(); i++) {
            result[i] = list.get(i);
        }
        return result;
    }

    static public HashMap<String,OptimizerParameter> paramString2PropertyMap(String paramString) throws OPTASWizardException {
        HashMap<String,OptimizerParameter> propertyList = new HashMap<String,OptimizerParameter>();
        if (paramString.isEmpty()) {
            return new HashMap<String,OptimizerParameter>();
        }
        String params[] = paramString.split(";");
        for (int i = 0; i < params.length; i++) {
            String entry[] = params[i].split("=");
            if (entry.length != 2) {
                throw new OPTASWizardException("Invalid parameterization of SimpleOptimizationController. The Parameter in question is" + params[i]);
            } else {
                if (entry[1].equals("true") || entry[1].equals("false")) {
                    propertyList.put(entry[0],new BooleanOptimizerParameter(entry[0], "unknown", Boolean.parseBoolean(entry[1])));
                } else {
                    try {
                        double value = Double.parseDouble(entry[1]);
                        propertyList.put(entry[0],new NumericOptimizerParameter(entry[0], "unknown", value, Double.MIN_VALUE, Double.MAX_VALUE));
                    } catch (NumberFormatException nfe) {
                        propertyList.put(entry[0],new StringOptimizerParameter(entry[0], "unknown", entry[1]));
                    }
                }
            }
        }
        return propertyList;
    }

    static public OptimizerDescription getStandardOptimizerDesc() {
        OptimizerDescription desc = new OptimizerDescription();
        desc.setDoSpatialRelaxation(new BooleanOptimizerParameter("doSpatialRelaxation", "?", false));
        desc.setShortName("unknown optimizer");
        desc.setId(1);
        desc.setAssessNonUniqueness(new BooleanOptimizerParameter("AssessNonUniqueness", "?", false));
        desc.setOptimizerClassName(optas.optimizer.experimental.ParallelPostSampling.class.getName());
        desc.setMultiObjective(false);

        return desc;
    }

    static public OptimizerDescription getStandardOptimizerDesc(String paramString) throws OPTASWizardException {
        OptimizerDescription desc = getStandardOptimizerDesc();
        desc.setPropertyMap(paramString2PropertyMap(paramString));

        return desc;
    }
}
