/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package jams.components.statistics;

import jams.data.Attribute;
import jams.data.Attribute.Calendar;
import jams.math.distributions.CDF_Normal;
import jams.model.JAMSComponent;
import jams.model.JAMSVarDescription;
import java.util.Arrays;

/**
 *
 * @author christian
 */
public class MovingAverages extends JAMSComponent{    
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
                        description = "current time interval")
    public Attribute.Calendar time;
    
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
                        description = "current time interval")
    public Attribute.TimeInterval period;
    
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
                        description = "value")        
    public Attribute.Double[] y;
    
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.WRITE,
                        description = "moving average value")        
    public Attribute.Integer windowSize;
    
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
                        description = "enabled flag")
    public Attribute.Boolean[] enabled;
    
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.WRITE,
                        description = "moving average value")        
    public Attribute.Double[] movingAvg;
    
    Calendar lastTimeStep = null;
    float timeserie[][] = null;
    int n=0,m=0;
    int counter=0;
    
    @Override
    public void init(){
        n = windowSize.getValue();
        m = 0;
        
        for (int i=0;i<y.length;i++){
            if (isEnabled(i))
                m++;                
        }
        //this quite an amount of memory .. 
        timeserie = new float[m][n];
        counter = 0;
        lastTimeStep = null;
    }
    
    private boolean isEnabled(int i){
        return (enabled == null || enabled[i].getValue());
    }
    
    @Override
    public void run(){
        boolean considerData = true;
        if (time != null){
            if (lastTimeStep != null && 
                lastTimeStep.getTimeInMillis() == time.getTimeInMillis()){
                considerData = false;
            }else{
                lastTimeStep = time.getValue();
            }
            if (period!=null){
                if (time.before(period.getStart()) || 
                    time.after(period.getEnd())){
                    considerData = false;
                }
            }
        }
        
        if (!considerData){
            return;
        }        
        int c=0;
        for (int i=0;i<y.length;i++){
            double avg = 0;
            if (isEnabled(i)){
                timeserie[c][counter] = (float)y[i].getValue();
                for (int j=0;j<n;j++){
                    avg += timeserie[c][j];
                }
                movingAvg[i].setValue(avg);                
                c++;
            }                        
        }
        counter = (counter+1) % n;        
    }    
}
