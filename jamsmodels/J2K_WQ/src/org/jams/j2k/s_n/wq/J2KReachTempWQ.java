/*
 * J2KReachTempWQ.java
 * Created on 03.08.2010, 14:00
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



import jams.data.*;
import jams.model.*;

/**
 *
 * @author Marcel Wetzel
 */
@JAMSComponentDescription(
        title="Title",
        author="Author",
        description="Description"
        )
        public class J2KReachTempWQ extends JAMSComponent {
    
    /*
     *  Component variables
     */
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            update = JAMSVarDescription.UpdateType.RUN,
            description = "The reach collection"
            )
            public JAMSEntityCollection entities;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            update = JAMSVarDescription.UpdateType.RUN,
            description = "attribute slope"
            )
            public JAMSDouble slope;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            update = JAMSVarDescription.UpdateType.RUN,
            description = "attribute width"
            )
            public JAMSDouble width;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            update = JAMSVarDescription.UpdateType.RUN,
            description = "attribute roughness"
            )
            public JAMSDouble roughness;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            update = JAMSVarDescription.UpdateType.RUN,
            description = "Reach statevar RD1 inflow"
            )
            public JAMSDouble inRD1;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            update = JAMSVarDescription.UpdateType.RUN,
            description = "Reach statevar RD2 inflow"
            )
            public JAMSDouble inRD2;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            update = JAMSVarDescription.UpdateType.RUN,
            description = "Reach statevar RG1 inflow"
            )
            public JAMSDouble inRG1;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            update = JAMSVarDescription.UpdateType.RUN,
            description = "Reach statevar RG2 inflow"
            )
            public JAMSDouble inRG2;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            update = JAMSVarDescription.UpdateType.RUN,
            description = "Reach statevar additional inflow",
            defaultValue= "0"
            )
            public JAMSDouble inAddIn;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            update = JAMSVarDescription.UpdateType.RUN,
            description = "Reach statevar RD1 outflow"
            )
            public JAMSDouble outRD1;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            update = JAMSVarDescription.UpdateType.RUN,
            description = "Reach statevar RD2 outflow"
            )
            public JAMSDouble outRD2;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            update = JAMSVarDescription.UpdateType.RUN,
            description = "Reach statevar RG1 outflow"
            )
            public JAMSDouble outRG1;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            update = JAMSVarDescription.UpdateType.RUN,
            description = "Reach statevar RG2 outflow"
            )
            public JAMSDouble outRG2;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            update = JAMSVarDescription.UpdateType.RUN,
            description = "Reach statevar additional outflow",
            defaultValue= "0"
            )
            public JAMSDouble outAddIn;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            update = JAMSVarDescription.UpdateType.RUN,
            description = "Reach statevar simulated Runoff"
            )
            public JAMSDouble simRunoff;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            update = JAMSVarDescription.UpdateType.RUN,
            description = "Reach statevar RD1 storage"
            )
            public JAMSDouble actRD1;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            update = JAMSVarDescription.UpdateType.RUN,
            description = "Reach statevar RD2 storage"
            )
            public JAMSDouble actRD2;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            update = JAMSVarDescription.UpdateType.RUN,
            description = "Reach statevar RG1 storage"
            )
            public JAMSDouble actRG1;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            update = JAMSVarDescription.UpdateType.RUN,
            description = "Reach statevar RG2 storage"
            )
            public JAMSDouble actRG2;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            update = JAMSVarDescription.UpdateType.RUN,
            description = "Reach statevar additional inflow storage",
            defaultValue= "0"
            )
            public JAMSDouble actAddIn;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            update = JAMSVarDescription.UpdateType.RUN,
            description = "Channel storage"
            )
            public JAMSDouble channelStorage;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            update = JAMSVarDescription.UpdateType.INIT,
            description = "flow routing coefficient TA"
            )
            public JAMSDouble flowRouteTA;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            update = JAMSVarDescription.UpdateType.RUN,
            description = "Catchment outlet RD1 storage"
            )
            public JAMSDouble catchmentRD1;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            update = JAMSVarDescription.UpdateType.RUN,
            description = "Catchment outlet RD2 storage"
            )
            public JAMSDouble catchmentRD2;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            update = JAMSVarDescription.UpdateType.RUN,
            description = "Catchment outlet RG1 storage"
            )
            public JAMSDouble catchmentRG1;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            update = JAMSVarDescription.UpdateType.RUN,
            description = "Catchment outlet RG2 storage"
            )
            public JAMSDouble catchmentRG2;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            update = JAMSVarDescription.UpdateType.RUN,
            description = "Catchment additional input outlet storage",
            defaultValue= "0"
            )
            public JAMSDouble catchmentAddIn;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            update = JAMSVarDescription.UpdateType.RUN,
            description = "Catchment outlet RG2 storage"
            )
            public JAMSDouble catchmentSimRunoff;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            update = JAMSVarDescription.UpdateType.INIT,
            description = "temporal resolution [d or h]"
            )
            public JAMSString tempRes;
      @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            update = JAMSVarDescription.UpdateType.RUN,
            description = "Reach statevar RG2 storage"
            )
            public JAMSDouble reachID;
       @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            update = JAMSVarDescription.UpdateType.RUN,
            description = "soil temperature in different layerdepths in °C"
            )
            public JAMSDoubleArray Soil_Temp_Layer;

     
    
    /*
     *  Component run stages
     */
    
    public void init() throws Attribute.Entity.NoSuchAttributeException {
        
    }
    
    public void run() throws Attribute.Entity.NoSuchAttributeException {
        
        Attribute.Entity entity = entities.getCurrent();
        
        Attribute.Entity DestReach = (Attribute.Entity)entity.getObject("to_reach");
        if (DestReach.isEmpty()) {
            DestReach = null;
        }
        Attribute.Entity DestReservoir = null;
        
        try{
            DestReservoir = (Attribute.Entity)entity.getObject("to_reservoir");
        }catch(Attribute.Entity.NoSuchAttributeException e){
            DestReservoir = null;
        }

        double[] soil_temp = Soil_Temp_Layer.getValue();
        double cal_soil_rd2 = 0;
         int  n =  soil_temp.length;
         int i = 0;
         while (i < n){
//             cal_soil_rd2 = cal_soil_rd2 + (soil_temp[i] * rd2_out[i]);
             i=i+1;
        }

  //      double Temp = Twater + Tdisp1 + Tdisp2 + W + Jair + Jsed;
    }
    
    public void cleanup() {
        
    }

    /**
     * Calculates discharge depended water temperature in specific reach
     * @param Qin the inflow to reach from the upstream reach
     * @param Qreach the outflow of reach to the downstream reach
     * @param Qout the total outflow from the reach due to point and nonpoint withdrawals
     * @param V the volume of the reach
     * @param Tin the inflow water temperature from the upstream reach
     * @param Treach the outflow water temperature from the reach
     * @return discharge depended water temperature in specific reach in °C
     */
    public static double calcDischargeTemp(double Qin, double Qreach, double Qout, double V, double Tin, double Treach){
        double Twater = 0;



        return Twater;
    }

    /**
     * Calculates the bulk dispersion coefficient between reaches
     * @param E the bulk dispersion coefficient
     * @param L1 the length of the reach
     * @param L2 the length of the downstream reach
     * @param Across the cross sectional area of the reach
     * @return bulk dispersion coefficient
     */
    public static double calcDispCoeff(double E, double L1, double L2, double Across){
        double disp = 0;



        return disp;
    }
    
    /**
     * Calculates net heat load from sources in specific reach
     * @param Dwater the density of water
     * @param Cwater the specific heat of water
     * @param Qps the point source inflow to the reach
     * @param Qnps the nonpoint source inflow to the reach
     * @param Tps the point source inflow water temperature
     * @param Tnps the nonpoint source inflow water temperature
     * @return net heat load from sources in specific reach
     */
    public static double calcPsnpsNetHeatLoad(double Dwater, double Cwater, double Qps, double Qnps, double Tps, double Tnps){
        double W = 0;



        return W;
    }

    /**
     * Calculates the air water heat flux in specific reach
     * @param I the net solar shortwave radiation at the water surface
     * @param Jan the net atmospheric lomgwave radiation
     * @param Jbr the longwave back radiation from the water
     * @param Jc conduction and convection
     * @param Je evaporation and condensation
     * @return surface heat flux in specific reach
     */
    public static double calcAirWaterHeatFlux(double I, double Jan, double Jbr, double Jc, double Je){
        double Jair = 0;



        return Jair;
    }

    /**
     * Calculates the sediment water heat flux in specific reach
     * @param Dsed the densit of the sediments
     * @param Csed the specific heat of the sediments
     * @param Ased the sediment thermal diffusivity
     * @param Hsed the effective thickness of the sediment layer
     * @param Tsed the temperature of the bottom sediment below the reach
     * @param Treach the water temperature in the reach
     * @return sediment water heat flux in specific reach
     */
    public static double calcSedimentWaterHeatFlux(double Dsed, double Csed, double Ased, double Hsed, double Tsed, double Treach){
        double Jsed = 0;



        return Jsed;
    }
}
    
  