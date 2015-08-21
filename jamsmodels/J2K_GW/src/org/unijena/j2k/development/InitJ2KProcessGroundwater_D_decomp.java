 /*
 * InitJ2KProcessGroundwater.java
 * Created on 25. November 2005, 16:54
 *
 * This file is part of JAMS
 * Copyright (C) 2005 FSU Jena, c0krpe
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
package org.unijena.j2k.development;
 
 import jams.data.*;
 import jams.model.*;
 
 @JAMSComponentDescription(title="J2KGroundwater", author="Peter Krause modifications Daniel Varga", description="Description")
 public class InitJ2KProcessGroundwater_D_decomp extends JAMSComponent
 {
 
   @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
   update = JAMSVarDescription.UpdateType.RUN,
   description = "The current hru entity")
   public JAMSEntityCollection hrus;
 
   @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
   update = JAMSVarDescription.UpdateType.RUN,
   description = "The elevation of the current hru entity")
   public JAMSDouble elevation;
 
   @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
   update = JAMSVarDescription.UpdateType.RUN,
           description = "The current reach entity")
   public JAMSEntityCollection reaches;
 
   @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
   update = JAMSVarDescription.UpdateType.RUN,
           description = "attribute area")
   public JAMSDouble area;
 
   @JAMSVarDescription(access = JAMSVarDescription.AccessType.WRITE,
   update = JAMSVarDescription.UpdateType.RUN,
           description = "Distance between adjacent entities")
   public JAMSDouble gwFlowLength;
 
   @JAMSVarDescription(access = JAMSVarDescription.AccessType.WRITE,
   update = JAMSVarDescription.UpdateType.RUN,
           description = "Length of the border arc between adjacent entities")
   public JAMSDouble gwArcLength;
 
   @JAMSVarDescription(access = JAMSVarDescription.AccessType.WRITE,
   update = JAMSVarDescription.UpdateType.RUN,
           description = "maximum RG1 storage")
   public JAMSDouble maxRG1;
 
   @JAMSVarDescription(access = JAMSVarDescription.AccessType.WRITE,
   update = JAMSVarDescription.UpdateType.RUN,
           description = "maximum GW storage")
   public JAMSDouble maxGW;
 
   @JAMSVarDescription(access = JAMSVarDescription.AccessType.WRITE,
   update = JAMSVarDescription.UpdateType.RUN,
           description = "maximum GW storage")
   public JAMSDouble satGW;
 
   @JAMSVarDescription(access = JAMSVarDescription.AccessType.WRITE,
   update = JAMSVarDescription.UpdateType.RUN,
           description = "actual RG1 storage")
   public JAMSDouble actRG1;
 
   @JAMSVarDescription(access = JAMSVarDescription.AccessType.WRITE,
   update = JAMSVarDescription.UpdateType.RUN,
           description = "actual GW storage")
   public JAMSDouble actGW;
 
   @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
   update = JAMSVarDescription.UpdateType.INIT,
           description = "relative initial RG1 storage")
   public JAMSDouble initRG1;
 
   @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
   update = JAMSVarDescription.UpdateType.INIT,
           description = "relative initial GW storage")
   public JAMSDouble initGW;
 
   @JAMSVarDescription(access = JAMSVarDescription.AccessType.READWRITE,
   update = JAMSVarDescription.UpdateType.RUN,
           description = "Downstream hru entity")
   public JAMSEntity toPoly;
 
   @JAMSVarDescription(access = JAMSVarDescription.AccessType.READWRITE,
   update = JAMSVarDescription.UpdateType.RUN,
           description = "Downstream reach entity")
   public JAMSEntity toReach;
 
   @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
   description = "entity x-coordinate")
   public JAMSDouble entityX;
 
   @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
   description = "entity y-coordinate")
   public JAMSDouble entityY;
 
   @JAMSVarDescription(access = JAMSVarDescription.AccessType.WRITE,
   update = JAMSVarDescription.UpdateType.RUN,
           description = "HRU Groundwater Level"
           )
   public JAMSDouble gwTable;
 
   @JAMSVarDescription(
   access = JAMSVarDescription.AccessType.WRITE,
   description = "Reach Water Level")
   public JAMSDouble waterTable_NN;
 
   @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
   update = JAMSVarDescription.UpdateType.RUN,
           description = "estimated hydraulic conductivity in cm/d")
   public JAMSDouble Kf_geo;
 
   @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
   update = JAMSVarDescription.UpdateType.RUN,
           description = "estimated hydraulic conductivity in cm/d")
   public JAMSDouble KfAdaptation;
 
   @JAMSVarDescription(access = JAMSVarDescription.AccessType.WRITE,
   update = JAMSVarDescription.UpdateType.RUN,
           description = "estimated porosity")
   public JAMSDouble Peff;
 
   @JAMSVarDescription(access = JAMSVarDescription.AccessType.WRITE,
   update = JAMSVarDescription.UpdateType.RUN,
           description = "adapted Kf_geo")
   public JAMSDouble Kf_geo_adapt;
 
   @JAMSVarDescription(access = JAMSVarDescription.AccessType.READWRITE,
   update = JAMSVarDescription.UpdateType.RUN,
           description = "Height of the aquifer base in m + NN")
   public JAMSDouble baseHeigth;
 
   @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
   update = JAMSVarDescription.UpdateType.RUN,
           description = "Adaptation of Height of the aquifer base in m")
   public JAMSDouble baseHeigth_v;
 
   @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
   update = JAMSVarDescription.UpdateType.RUN,
           description = "Minimum Depth of the aquifer in m")
   public JAMSDouble minimumAQ_h;
 
   @JAMSVarDescription(access = JAMSVarDescription.AccessType.READWRITE,
   update = JAMSVarDescription.UpdateType.RUN,
           description = "Groundwater outflow")
   public JAMSDouble pot_outGW;
 
   @JAMSVarDescription(access = JAMSVarDescription.AccessType.WRITE,
   update = JAMSVarDescription.UpdateType.RUN,
           description = "Height of potential groundwater Table (in Init the same as gwTable")
   public JAMSDouble pot_gwTable;
 
   @JAMSVarDescription(access = JAMSVarDescription.AccessType.WRITE,
   update = JAMSVarDescription.UpdateType.RUN,
           description = "Height of potential groundwater Table (in Init the same as gwTable")
   public JAMSDouble preOutGW;
 
   @JAMSVarDescription(access = JAMSVarDescription.AccessType.WRITE,
   update = JAMSVarDescription.UpdateType.RUN,
           description = "Height of potential groundwater Table (in Init the same as gwTable")
   public JAMSDouble preActGW;
 
   @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
   update = JAMSVarDescription.UpdateType.RUN,
           description = "Flowlength")
   public JAMSDouble fll;
 
   @JAMSVarDescription(access = JAMSVarDescription.AccessType.WRITE,
   update = JAMSVarDescription.UpdateType.RUN,
           description = "Thickness of the Aquifer")
   public JAMSDouble aqThickness;



    /*
     *  Component run stages
     */

    double run_area, run_Peff, run_Kf_geo, run_KfAdaptation, run_aqThickness, run_baseHeigth, upFL, downFL, run_Kf_geo_adapted, run_gwFlowLength,
          run_gwDepth, run_gwTable, run_actGW, run_maxGW, run_satGW;


    public void init() throws Attribute.Entity.NoSuchAttributeException {

    }
 
   public void run() throws Attribute.Entity.NoSuchAttributeException {

     this.run_area = this.area.getValue();
     this.run_Kf_geo = this.Kf_geo.getValue();
     this.run_KfAdaptation = this.KfAdaptation.getValue();
     this.run_baseHeigth = this.baseHeigth.getValue();
 
     double run_baseHeigth_v = this.baseHeigth_v.getValue();

     double min_AQ_h = this.minimumAQ_h.getValue();

     double HRU_elev = this.elevation.getValue();
 

     this.upFL = this.fll.getValue();
 
     if (this.toPoly.getValue() != null) {
       this.downFL = this.toPoly.getDouble("fll");
     }
     else if (this.toReach.getValue() != null) {
       this.downFL = 0.0D;

     }
     else {
      getModel().getRuntime().println("Current entity has no receiver.");
     }
 












this.run_gwFlowLength = (this.upFL / 2.0D + this.downFL / 2.0D);
 
/* 264 */     this.run_Kf_geo_adapted = (this.run_Kf_geo * this.run_KfAdaptation / 100.0D / 86400.0D);
 
/* 267 */     this.run_Peff = (0.462D + 0.045D * Math.log(this.run_Kf_geo_adapted));
/* 268 */     if (this.run_Peff < 0.01D) {
/* 269 */       this.run_Peff = 0.01D;
     }
 
/* 273 */     this.run_Kf_geo_adapted *= 86400.0D;
 
/* 277 */     this.run_baseHeigth += run_baseHeigth_v;
 
/* 279 */     this.run_aqThickness = (HRU_elev - this.run_baseHeigth);
 
/* 281 */     if (this.run_aqThickness < min_AQ_h) {
/* 282 */       this.run_baseHeigth = (HRU_elev - min_AQ_h);
/* 283 */       this.run_aqThickness = min_AQ_h;
     }
 
/* 287 */     this.run_maxGW = (this.run_aqThickness * this.run_area * this.run_Peff * 1000.0D);
/* 288 */     this.run_actGW = (this.run_maxGW * this.initGW.getValue());
 
/* 290 */    
 
/* 294 */     this.run_satGW = (this.run_actGW / this.run_maxGW);
 
/* 296 */     this.run_gwDepth = (this.run_actGW / 1000.0D / this.run_area / this.run_Peff);
 
/* 298 */     this.run_gwTable = (this.run_gwDepth + this.run_baseHeigth);
 
/* 302 */     this.baseHeigth.setValue(this.run_baseHeigth);
/* 303 */     this.aqThickness.setValue(this.run_aqThickness);
/* 304 */     this.gwFlowLength.setValue(this.run_gwFlowLength);
/* 305 */     this.gwTable.setValue(this.run_gwTable);
/* 306 */     this.pot_gwTable.setValue(this.run_gwTable);
/* 307 */     this.Kf_geo_adapt.setValue(this.run_Kf_geo_adapted);
/* 308 */     this.Peff.setValue(this.run_Peff);
/* 309 */     this.actRG1.setValue(0.0D);
/* 310 */     this.actGW.setValue(this.run_actGW);
/* 311 */     this.maxGW.setValue(this.run_maxGW);
/* 312 */     this.satGW.setValue(this.run_satGW);
/* 313 */     this.pot_outGW.setValue(0.0D);
/* 314 */     this.preOutGW.setValue(0.0D);
/* 315 */     this.preActGW.setValue(this.run_actGW);
   }
 
   public void cleanup()
   {
   }
 }