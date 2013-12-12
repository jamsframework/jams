/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package jams.components.aggregate;

import jams.data.Attribute;
import jams.model.JAMSComponent;
import jams.model.JAMSComponentDescription;
import jams.model.JAMSVarDescription;

/**
 *
 * @author christian
 */
@JAMSComponentDescription(
        title = "DayOfYear",
        author = "Christian Fischer",
        description = "calculates the first and last day in year where a boolean value is set to tre")
public class DayOfYear extends JAMSComponent {
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
    description = "Current time")
    public Attribute.Calendar time;
    
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
    description = "boolean inputs",
    unit="-")
    public Attribute.Double[] input;
    
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.WRITE,
    description = "day of year",
    unit="°-")
    public Attribute.Double[] day;
    
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.WRITE,
    description = "day of year, when first half year",
    unit="°-")
    public Attribute.Double[] dayFirstHalfYear;
    
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.WRITE,
    description = "day of year, when first half year",
    unit="°-")
    public Attribute.Double[] daySecondHalfYear;
        
    @Override
    public void run(){
        for (int i=0;i<input.length;i++){
            day[i].setValue(jams.JAMS.getMissingDataValue());
            dayFirstHalfYear[i].setValue(jams.JAMS.getMissingDataValue());
            daySecondHalfYear[i].setValue(jams.JAMS.getMissingDataValue());
            
            if (input[i].getValue()==1.0){
                day[i].setValue(time.get(Attribute.Calendar.DAY_OF_YEAR));
                if (time.get(Attribute.Calendar.DAY_OF_YEAR) <= 182){
                    dayFirstHalfYear[i].setValue(time.get(Attribute.Calendar.DAY_OF_YEAR));
                }else{
                    daySecondHalfYear[i].setValue(time.get(Attribute.Calendar.DAY_OF_YEAR));
                }
            }
        }
    }
}