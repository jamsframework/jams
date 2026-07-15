/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package jams.components.calc;

import jams.data.Attribute;

/**
 *
 * @author christian
 */
public class GeneralPurposeFunctions {
                    
    public static double timestep_count(Attribute.TimeInterval I){
        return I.getNumberOfTimesteps();
    }
            
    public static double[] select(double[] in, int sel){                
        double y[] = {in[sel]};        
        return y;
    }
    
     public static double daysInMonth(Attribute.Calendar c){                
        return c.getActualMaximum(Attribute.Calendar.DAY_OF_MONTH);
    }
    
    public static int day(Attribute.Calendar time){
        return time.get(Attribute.Calendar.DAY_OF_MONTH);
    }
    
    public static int month(Attribute.Calendar time){
        return time.get(Attribute.Calendar.MONTH);
    }
    
    public static int year(Attribute.Calendar time){
        return time.get(Attribute.Calendar.YEAR);
    }
}
