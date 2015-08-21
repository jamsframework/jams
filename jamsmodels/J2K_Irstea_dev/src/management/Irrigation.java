/*
 * SewerOverflowDevice.java
 * Created on 05. October 2012, 17:02
 *
 * This file is part of JAMS
 * Copyright (C) FSU Jena
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA
 *
 */
package management;

import jams.data.*;
import jams.model.*;
import java.util.ArrayList;
import java.util.Calendar;

/**
 *
 * @author c6gohe2
 */
@JAMSComponentDescription(title = "Irrigation",
        author = "Francois Tilmant",
        description = "Component used for the calculation of RU, ETR and ETM for different crops",
        version = "3.0_0",
        date = "2015-02-04")

public class Irrigation extends JAMSComponent {
        
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "HRU actual Transpiration in L"
            )
            public Attribute.Double aTP;
    
     @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "HRU potential Transpiration in L"
            )
            public Attribute.Double pTP;  
    
        
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.WRITE,
    description = "HRU irrigation")
    public Attribute.Double irrigation;
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READWRITE,
    description = "Sum of HRU irrigation on the catchment")
    public Attribute.Double irrigationsum;
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
    description = "Start of the Irrigation season in day of the year (-)")
    public Attribute.Double Irr_Start;
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
    description = "End of the Irrigation season in day of the year (-)")
    public Attribute.Double Irr_End;
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
    description = "HRU crop class")
    public Attribute.Double cropid;
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
    description = "Current time")
    public Attribute.Calendar time;
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
    description = "LID which will be irrigated")
    public Attribute.Double IrrLid;
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
            description = "Available water (L)")
    public Attribute.DoubleArray StorageArray;
        @JAMSVarDescription(access = JAMSVarDescription.AccessType.WRITE,
    description = "HRU irrigation")
    public Attribute.DoubleArray irrigationArray;

        
    public void init() {
        double t = 1;
    }
    
    public void run() {
        
            
        double run_wt_rqrmt = 0;
        double run_aTP = aTP.getValue();
        double run_pTP = pTP.getValue();
        
     
        run_wt_rqrmt = run_pTP - run_aTP;
        
//      irrigationArray[t].setValue(run_wt_rqrmt);
      

      
      
      
      
      
      
      
              double value=0;
 //       double[] sourceData = dataArray.getValue();
        //Attribute.Entity entity = entities.getCurrent();
        //double Smax = entity.getDouble("Smax");
//       double[] Nom = this.names.getValue();
//        if (this.Smax.getValue() > 0) {
//        double reach = this.ID.getValue();
        int t = 0;
 //       while (Nom[t] != reach) {
            t++;        }
 //       value = sourceData[t];
 //       dataValue.setValue(value);
        }
    
      
      
      
      
      
      
      
      
      

 //       int act = time.get(Calendar.DAY_OF_YEAR);

            


 //       double irrsum = irrigationsum.getValue();
//        double irr = 0;
//        double cropID = cropid.getValue();
       
//       if (act >= Irr_Start.getValue() & act <= Irr_End.getValue()) {
//            if (cropID != IrrLid.getValue()) {
//                irr = 0;
//           } else {
//                irr = run_wt_rqrmt;
//                }

//            }
        

        
    
//    irrsum  = irrsum + irr;

//    irrigation.setValue (irr);

//    irrigationsum.setValue (irrsum);
// }
//}
