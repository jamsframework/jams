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
title="Formparameter",
        author="Corina Manusch",
        description="Calculates minimum and maxium of timeseries."
        )
        public class Formparameter extends JAMSComponent {
    
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
                        
            double schiefe = schiefe(rhum);
            double woelbung = woelbung(rhum);
            
            //System.out.println("schiefe " + schiefe);
           //System.out.println("woelbung " + woelbung);
            
            }
            
   
    public void cleanup() throws Attribute.Entity.NoSuchAttributeException {
        
    }
    
    public static double schiefe(double [] timeseries) {
        double schiefe = 0;
        double mittel = Lageparameter.arith_mittel(timeseries);
        
        double [] F = new double [timeseries.length] ;
      
        for(int i = 0; i < timeseries.length; i++){
    
            F[i] = (timeseries[i] - mittel)*(timeseries[i] - mittel)*(timeseries[i] - mittel);
            schiefe = schiefe + F[i];
        }
        
        double sA = Streuungsparameter.standardAbweichung(timeseries);
        schiefe = (schiefe/(timeseries.length))/(sA*sA*sA);
       
        return schiefe;
    }
    
     public static double woelbung(double [] timeseries) {
      
        double woelbung = 0;
        double mittel = Lageparameter.arith_mittel(timeseries);
        
        double [] F = new double [timeseries.length] ;
      
        for(int i = 0; i < timeseries.length; i++){
    
            F[i] = (timeseries[i] - mittel)*(timeseries[i] - mittel)*(timeseries[i] - mittel)*(timeseries[i] - mittel);
            woelbung = woelbung + F[i];
        }
        
        double sA = Streuungsparameter.standardAbweichung(timeseries);
        woelbung = ((woelbung/(timeseries.length))/(sA*sA*sA*sA))-3;
       
        return woelbung;
     }
}

