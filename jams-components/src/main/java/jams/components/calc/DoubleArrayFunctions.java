/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package jams.components.calc;

import de.odysseus.el.util.SimpleContext;
import jams.data.Attribute;

/**
 *
 * @author christian
 */
public class DoubleArrayFunctions {
            
    public static double[] add(double a[], double b[]){
        int n = a.length;
        double y[] = new double[n];
        for (int i=0;i<n;i++){
            y[i] = a[i]+b[i];
        }
        return y;
    }
    
    public static double[] acc(double[] y, double[] x){
        int n = x.length;        
        for (int i=0;i<n;i++){
            y[i] += x[i];
        }
        return y;
    }
    
    public static double[] mac(double[] y, double[] x, double w){
        int n = x.length;        
        for (int i=0;i<n;i++){
            y[i] += x[i]/w;
        }
        return y;
    }
             
    public static double[] mul(double[] a, double[] b){
        int n = Math.max(a.length, b.length);
        
        double y[] = new double[n];
        
        for (int i=0;i<n;i++){            
            y[i] = a[i]*b[i];            
        }
        return y;
    }
    
    public static double[] mul(double[] a, double b){
        int n = a.length;
        
        double y[] = new double[n];
        
        for (int i=0;i<n;i++){                        
            y[i] = a[i]*b;            
        }
        return y;
    }
            
    public static double[] div(double[] a, double b){
        int n = a.length;
        
        double y[] = new double[n];
        
        for (int i=0;i<n;i++){            
            y[i] = a[i]/b;
        }
        return y;
    }
    
    public static double[] min(double[] a, double b[]){
        int n = a.length;
        
        double y[] = new double[n];
        
        for (int i=0;i<n;i++){            
            y[i] = a[i]<b[i] ? a[i] : b[i];
        }
        return y;
    }
    
    public static double[] max(double[] a, double b[]){
        int n = a.length;
        
        double y[] = new double[n];
        
        for (int i=0;i<n;i++){            
            y[i] = a[i]>b[i] ? a[i] : b[i];
        }
        return y;
    }
       
    public static double[] select(double[] in, int sel){                
        double y[] = {in[sel]};        
        return y;
    }
    
    public static double[] zero(double[] y){                
        for (int i=0;i<y.length;i++){
            y[i] = 0;
        }
        return y;
    }
    
    public static double[] ifCondition(boolean condition, double[] a, double[] b){
        if (condition)
            return a;
        else
            return b;
    }
    
    public static double timestep_count(Attribute.TimeInterval I){
        return I.getNumberOfTimesteps();
    }
            
    public static SimpleContext getContext(){
        SimpleContext context = new SimpleContext();
         
        try{            
            context.setFunction("", "select", DoubleArrayFunctions.class.getMethod("select", double[].class, int.class));
            context.setFunction("", "zero", DoubleArrayFunctions.class.getMethod("zero", double[].class));
            context.setFunction("", "timestep_count", GeneralPurposeFunctions.class.getMethod("timestep_count", Attribute.TimeInterval.class));
            context.setFunction("", "day", GeneralPurposeFunctions.class.getMethod("day", Attribute.Calendar.class));
            context.setFunction("", "month", GeneralPurposeFunctions.class.getMethod("month", Attribute.Calendar.class));
            context.setFunction("", "year", GeneralPurposeFunctions.class.getMethod("year", Attribute.Calendar.class));
            context.setFunction("", "daysInMonth", GeneralPurposeFunctions.class.getMethod("daysInMonth", Attribute.Calendar.class));            
            
            context.setFunction("", "if", DoubleArrayFunctions.class.getMethod("ifCondition", boolean.class, double[].class, double[].class));   
            
            context.setFunction("", "array_acc", DoubleArrayFunctions.class.getMethod("acc", double[].class, double[].class));
            context.setFunction("", "array_mac", DoubleArrayFunctions.class.getMethod("mac", double[].class, double[].class, double.class));
            context.setFunction("", "array_add", DoubleArrayFunctions.class.getMethod("add", double[].class, double[].class));
            context.setFunction("", "array_mul", DoubleArrayFunctions.class.getMethod("mul", double[].class, double[].class));                    
            context.setFunction("", "array_min", DoubleArrayFunctions.class.getMethod("min", double[].class, double[].class));                    
            context.setFunction("", "array_max", DoubleArrayFunctions.class.getMethod("max", double[].class, double[].class));        
            
            context.setFunction("", "scalar_mul", DoubleArrayFunctions.class.getMethod("mul", double[].class, double.class));        
            context.setFunction("", "scalar_div", DoubleArrayFunctions.class.getMethod("div", double[].class, double.class)); 
        }catch(NoSuchMethodException nsme){
            nsme.printStackTrace();
        }
        return context;
    }
}
