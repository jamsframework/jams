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
public class ScalarFunctions {
                                            
    public static double ifCondition(boolean condition, double a, double b){
        if (condition)
            return a;
        else
            return b;
    }
    
    public static double min(double a, double b){        
        return a<b ? a : b;
    }
    
    public static double max(double a, double b){        
        return a>b ? a : b;
    }
              
    public static SimpleContext getContext(){
        SimpleContext context = new SimpleContext();
         
        try{                        
            context.setFunction("", "day", GeneralPurposeFunctions.class.getMethod("day", Attribute.Calendar.class));
            context.setFunction("", "month", GeneralPurposeFunctions.class.getMethod("month", Attribute.Calendar.class));
            context.setFunction("", "year", GeneralPurposeFunctions.class.getMethod("year", Attribute.Calendar.class));
            context.setFunction("", "daysInMonth", GeneralPurposeFunctions.class.getMethod("daysInMonth", Attribute.Calendar.class));            
            context.setFunction("", "timestep_count", GeneralPurposeFunctions.class.getMethod("timestep_count", Attribute.TimeInterval.class));            
            context.setFunction("", "if", ScalarFunctions.class.getMethod("ifCondition", boolean.class, double.class, double.class));   
            context.setFunction("", "min", ScalarFunctions.class.getMethod("min", double.class, double.class));                    
            context.setFunction("", "max", ScalarFunctions.class.getMethod("max", double.class, double.class));        
            
        }catch(NoSuchMethodException nsme){
            nsme.printStackTrace();
        }
        return context;
    }
}
