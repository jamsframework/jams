/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */


package org.unijena.j2k.regionWK.AP5;

import jams.data.*;
import jams.model.*;
import java.util.*;

/**
 *
 * @author Corina Manusch
 */
@JAMSComponentDescription(
title="Extremwerte",
        author="Corina Manusch",
        description="Calculates minimum and maxium of timeseries."
        )
        public class Lageparameter extends JAMSComponent {
    
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
            
            // 1. Werte sortieren
            java.util.Arrays.sort( rhum );
            
            double median = median(rhum);
            double arith_mittel = arith_mittel(rhum);
            double Q1 = erstesQuartil(rhum);
            double Q3 = drittesQuartil(rhum);
            
            /*System.out.println("median " + median);
            System.out.println("arith_mittel " + arith_mittel);
            System.out.println("Q1 " + Q1);
            System.out.println("Q3 " + Q3);*/
            
         
            }
            
   
    public void cleanup() throws Attribute.Entity.NoSuchAttributeException {
        
    }
    
    public static double median(double [] timeseries) {
        double median = 0;
        if(timeseries.length%2 == 0){
            median = (timeseries[timeseries.length/2] + timeseries[(timeseries.length/2) + 1])/ 2;
        }
        else{
            median  = timeseries[(timeseries.length+1)/2];
        }
        return median;
    }
    
     public static double arith_mittel(double [] timeseries) {
        double mittel = 0;
         
        for (int i = 0; i < timeseries.length; i++){
            mittel = mittel + timeseries[i];
        }
         mittel = mittel/ timeseries.length;
         return mittel;
     }
     
     public static double erstesQuartil(double [] timeseries) {
         
         int l = timeseries.length + 1;
         int erstesQ = (int) (0.25 * l);
         double eQ = timeseries[erstesQ];
         return eQ;
     }
     
     public static double drittesQuartil(double [] timeseries) {
      
         int l = timeseries.length + 1;
         int drittesQ = (int) (0.75 * l);
         double dQ = timeseries[drittesQ];
         return dQ;
     }
}

