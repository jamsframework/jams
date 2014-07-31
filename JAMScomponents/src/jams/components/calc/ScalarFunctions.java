/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package jams.components.calc;

import de.odysseus.el.util.SimpleContext;
import jams.data.Attribute;
import jams.model.Context;
import jams.workspace.stores.FilterFunctions;

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
            context.setFunction("", "timestep_count", GeneralPurposeFunctions.class.getMethod("timestep_count", Attribute.TimeInterval.class));            
            context.setFunction("", "if", ScalarFunctions.class.getMethod("ifCondition", boolean.class, double.class, double.class));   
            context.setFunction("", "min", ScalarFunctions.class.getMethod("min", double.class, double.class));                    
            context.setFunction("", "max", ScalarFunctions.class.getMethod("max", double.class, double.class));        
            
            context.setFunction("", "day", FilterFunctions.class.getMethod("day", Attribute.Calendar.class));
            context.setFunction("", "month", FilterFunctions.class.getMethod("month", Attribute.Calendar.class));
            context.setFunction("", "year", FilterFunctions.class.getMethod("year", Attribute.Calendar.class));
            context.setFunction("", "daysInMonth", FilterFunctions.class.getMethod("lastDayInMonth", Attribute.Calendar.class));           
            context.setFunction("", "isLastDayInMonth", FilterFunctions.class.getMethod("isLastDayInMonth", Attribute.Calendar.class));           
            context.setFunction("", "dateCompare", FilterFunctions.class.getMethod("dateCompare", Attribute.Calendar.class, Attribute.Calendar.class, int.class));   
            
            context.setFunction("", "toCalendar", FilterFunctions.class.getMethod("toCalendar", String.class)); 
            context.setFunction("", "toTimeInterval", FilterFunctions.class.getMethod("toTimeInterval", String.class)); 
            context.setFunction("interval", "start", FilterFunctions.class.getMethod("start", Attribute.TimeInterval.class)); 
            context.setFunction("interval", "end", FilterFunctions.class.getMethod("end", Attribute.TimeInterval.class)); 
            context.setFunction("", "getAttribute", FilterFunctions.class.getMethod("getAttribute", Context.class, String.class)); 
        }catch(NoSuchMethodException nsme){
            nsme.printStackTrace();
        }
        return context;
    }
}
