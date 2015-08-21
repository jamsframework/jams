/*
 * ManageLanduse.java
 *
 * Created on 16. März 2006, 13:20
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */
package org.jams.j2k.s_n.management;

import java.util.ArrayList;
import org.jams.j2k.s_n.crop.*;
import jams.model.*;
import jams.data.*;

/**
 *
 * @author c8fima
 */
@JAMSComponentDescription(
        title = "Tillage_Operation",
        author = "Manfred Fink",
        description = "Calculates redistribution of nutrient pools in soils in Soil. Method after SWAT2000 with adaptions",
        version = "1.0",
        date = "2015-05-06"
)

public class Tillage_Operation extends JAMSComponent {

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "Mixing efficiency",
            unit = "-",
            lowerBound = 0,
            upperBound = 1
    )
    public Attribute.Double Mixeff;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "Mixing depth",
            unit = "mm",
            lowerBound = 0,
            upperBound = 1
    )
    public Attribute.Double Mixdepth;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "Depth of the single soil layers",
            unit = "cm",
            lowerBound = 0,
            upperBound = 2000
    )
    public Attribute.DoubleArray soillayerdepth;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = "List of pools to mix"
    )
    public Attribute.DoubleArray[] Pool;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = " number of soil layers",
            unit = "-",
            lowerBound = 0,
            upperBound = 100
    )
    public Attribute.Double Layer;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = " daily mixing rates due to biological mixing",
            unit = "-",
            lowerBound = 0.00005,
            upperBound = 0.001,
            defaultValue = "0.0005"
    )
    public Attribute.Double BioMix;
    
/*    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = " sum of derivation",
            unit = "kg*ha^-1"
           )
    public Attribute.Double cum_testout;*/

   

    public void run() {
        double biomixdepth = 40.0; // depth of the bioturbation in cm (after SWAT 30 cm)  
        int layernum = (int) Layer.getValue() + 1;
        double[] depth = new double[layernum];
        double[] depthsum = new double[layernum];
        int v = 0;
        int varnum = Pool.length;
        double[] restpool = new double[layernum];
        double[] newpool;
        double[] partpool = new double[layernum];
        double[] mixpool = new double[layernum];
        double mixpoolsum = 0;
        double runMixdepth = Mixdepth.getValue() / 10; // mixdepth in cm
        double runMixeff = Mixeff.getValue();
        
        
        //Daily bioturbation values
        if (runMixeff == 0){
            runMixeff = BioMix.getValue();
            runMixdepth = biomixdepth;
        }
        
        
        
        int i = 0;

        while (i < layernum) {
            if (i == 0) {
                depth[i] = 1;
                depthsum[i] = 1;
            } else if (i == 1) {
                depth[i] = this.soillayerdepth.getValue()[i - 1] - 1.0;
                depthsum[i] = depthsum[i - 1] + depth[i];
                
            } else {
                depth[i] = this.soillayerdepth.getValue()[i - 1];
                depthsum[i] = depthsum[i - 1] + depth[i];
            }
            
            i++;
        }
        i = 0;
        
        while (v < varnum) {
            double tillrest = runMixdepth;
            
            i = 0;
            double testinsum = 0;
            double testoutsum = 0;
            
            while (i < layernum) {
                
                
                testinsum = testinsum + Pool[v].getValue()[i];
                tillrest = tillrest - depth[i];
                
                if (tillrest >= 0) {
                    mixpool[i] = Pool[v].getValue()[i] * runMixeff;
                    //calculation for completely mixed horizons
                    mixpoolsum = mixpoolsum + mixpool[i];
                    partpool[i] = depth[i] / runMixdepth; 
                    restpool[i] = Pool[v].getValue()[i] - mixpool[i];
                }else if (depthsum[i - 1] < runMixdepth){
                    mixpool[i] = Pool[v].getValue()[i] * runMixeff;                    
                    //calculation for partly mixed horizons
                    double parthor = (depthsum[i] + tillrest - depthsum[i - 1])  / depth[i];
                    double partsub = mixpool[i] * parthor;
                    mixpoolsum = mixpoolsum + partsub;
                    restpool[i] = (Pool[v].getValue()[i] - partsub);
                    partpool[i] = (depthsum[i] + tillrest - depthsum[i - 1]) / runMixdepth;
                }else{
                    //calculation for unmixed horizons
                    mixpool[i] = 0;
                    restpool[i] = Pool[v].getValue()[i];
                    partpool[i] = 0;                    
                }

                i++;
            }
            
            i = 0;
            
            newpool = Pool[v].getValue();//new double[layernum];
            
            while (i < layernum) {
                
                newpool[i] = (mixpoolsum * partpool[i]) + restpool[i];
                
                testoutsum = testoutsum +  newpool[i];
                
                i++;
                
            }
            
            
            /* double deriva = testoutsum - testinsum;
            cum_testout.setValue(cum_testout.getValue() + deriva);
            
            if (testoutsum > testinsum + 0.0000001 || testoutsum < testinsum - 0.000001){
                
                getModel().getRuntime().println("Tillage calculation problem in pool balance, derivation: " +  deriva);
            } */
            mixpoolsum = 0;          
//            Pool[v].setValue(newpool);
            
            v++;
        }
        
        
        
    }

}
