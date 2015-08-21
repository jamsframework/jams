/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */


package org.unijena.j2k.regionWK.AP5;

import jams.data.*;
import jams.model.*;

/**
 *
 * @author Corina Manusch
 */
@JAMSComponentDescription(
title="Extremwerte",
        author="Corina Manusch",
        description="Calculates minimum and maxium of timeseries."
        )
        public class Streuungsparameter extends JAMSComponent {
    
    /*
     *  Component variables
     */
    @JAMSVarDescription(
    access = JAMSVarDescription.AccessType.READ,
            description = "the relative humidity values"
            )
            public Attribute.DoubleArray rhum;
    
    @JAMSVarDescription(
    access = JAMSVarDescription.AccessType.READ,
            description = "temperature for the computation"
            )
            public Attribute.DoubleArray temperature;
   
    
    /*
     *  Component run stages
     */
    
    public void init() throws Attribute.Entity.NoSuchAttributeException {
        
    }
    
    public void run() throws Attribute.Entity.NoSuchAttributeException {
        
            double[] rhum = this.rhum.getValue();
            
            
            double varianz = varianz(rhum);
            double spannweite = spannweite(rhum);
            double dA = durchschnittl_Abweichung(rhum);
            double sA = standardAbweichung(rhum);
            
           /*System.out.println("varianz " + varianz);
           System.out.println("spannweite " + spannweite);
           System.out.println("durchschn Abweichung " + dA);
           System.out.println("StandardAbweichung " + sA);*/
           
            
            }
            
   
    public void cleanup() throws Attribute.Entity.NoSuchAttributeException {
        
    }
    
    public static double varianz(double [] timeseries) {
        
        double varianz = 0;
        double mittel = Lageparameter.arith_mittel(timeseries);
        double [] F = new double [timeseries.length] ;
      
        for(int i = 0; i < timeseries.length; i++){
    
            F[i] = (timeseries[i] - mittel)*(timeseries[i] - mittel);
            varianz = varianz + F[i];
        }
  
        varianz = varianz/(timeseries.length-1); 
        
         return varianz;
    }
    
     public static double spannweite(double [] timeseries) {
        double spannweite = Extremwerte.maximum(timeseries) - Extremwerte.minimum(timeseries);
        return spannweite;
     }
     
     public static double durchschnittl_Abweichung(double [] timeseries) {
      
        double dA = 0;
        double mittel = Lageparameter.arith_mittel(timeseries);
        double [] F = new double [timeseries.length] ;
      
        for(int i = 0; i < timeseries.length; i++){
    
            F[i] = Math.abs(timeseries[i] - mittel);
            dA = dA + F[i];
        }
  
        dA = dA/(timeseries.length); 
        
         return dA;
     }
     
     public static double standardAbweichung(double [] timeseries) {
        
         double stabw = Math.sqrt(varianz(timeseries));
         return stabw;
     }
}

