/*
 * J2KProcessInterception_conv_potET
 * Created on 29-03-2013 after J2KProcessInterception.java by P. Krause
 * 
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 * 
 * Dans ce module, on a change l'initialisation de l'ET0
 * et on a applique un changement d'unite pour potET (conversion de mm en L)
 * 
 */
package interception;


import jams.data.*;
import jams.model.*;

/**
*
* @author Francois Tilmant
*/
@JAMSComponentDescription(
        title="J2KProcessInterception_conv_potET",
        author="Francois Tilmant",
        description="Calculates daily interception based on DICKINSON 1984",
        version="1.0_0",
        date="2013-03-19"
        )
public class J2KProcessInterception_conv_potET extends JAMSComponent {

    /*
     *  Component variables
     */
   @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            update = JAMSVarDescription.UpdateType.RUN,
            description = "attribute area",
            unit="m^2"
            )
            public JAMSDouble area;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            update = JAMSVarDescription.UpdateType.RUN,
            description = "state variable mean tempeature",
            unit="degC"
            )
            public JAMSDouble tmean;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            update = JAMSVarDescription.UpdateType.RUN,
            description = "state variable rain",
            unit="L"
            )
            public JAMSDouble rain;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            update = JAMSVarDescription.UpdateType.RUN,
            description = "state variable snow",
            unit="L"
            )
            public JAMSDouble snow;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            update = JAMSVarDescription.UpdateType.RUN,
            description = "state variable potET",
            unit="mm(i)-L(o)"
            )
            public JAMSDouble potET;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            update = JAMSVarDescription.UpdateType.RUN,
            description = "state variable actET",
            unit="L"
            )
            public JAMSDouble actET;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            update = JAMSVarDescription.UpdateType.RUN,
            description = "state variable LAI"
            )
            public JAMSDouble actLAI;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            update = JAMSVarDescription.UpdateType.INIT,
            description = "Snow parameter TRS",
            lowerBound = -10.0,
            upperBound = 10.0,
            defaultValue = "0.0",
            unit = "degC"
            )
            public JAMSDouble snow_trs;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            update = JAMSVarDescription.UpdateType.INIT,
            description = "Snow parameter TRANS",
            lowerBound = 0.0,
            upperBound = 5.0,
            defaultValue = "2.0",
            unit = "K"
            )
            public JAMSDouble snow_trans;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            update = JAMSVarDescription.UpdateType.INIT,
            description = "Interception parameter a_rain",
            lowerBound = 0.0,
            upperBound = 5.0,
            defaultValue = "0.2",
            unit = "mm"
            )
            public JAMSDouble a_rain;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            update = JAMSVarDescription.UpdateType.INIT,
            description = "Interception parameter a_snow",
            lowerBound = 0.0,
            upperBound = 5.0,
            defaultValue = "0.5",
            unit = "mm"
            )
            public JAMSDouble a_snow;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            update = JAMSVarDescription.UpdateType.RUN,
            description = "state variable net-rain",
            unit="L"
            )
            public JAMSDouble netRain;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            update = JAMSVarDescription.UpdateType.RUN,
            description = "state variable net-snow",
            unit="L"
            )
            public JAMSDouble netSnow;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            update = JAMSVarDescription.UpdateType.RUN,
            description = "state variable throughfall",
            unit="L"
            )
            public JAMSDouble throughfall;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            update = JAMSVarDescription.UpdateType.RUN,
           description = "state variable dy-interception",
            unit="L"
            )
            public JAMSDouble interception;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            update = JAMSVarDescription.UpdateType.RUN,
            description = "state variable interception storage",
            unit="L"
            )
            public JAMSDouble intercStorage;

        
     /* @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            update = JAMSVarDescription.UpdateType.RUN,
            description = "id de la HRU pour Debug",
            unit="-"
            )

     @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            update = JAMSVarDescription.UpdateType.RUN,
            description = "id de la HRU pour debug"
           )
        public JAMSDouble HRU_id;
      
      @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
            description = "Current time step",
            unit = "d")
        public Attribute.Calendar time;

            
    /*
     *  Component run stages
     */

    public void init() throws JAMSEntity.NoSuchAttributeException{
        this.intercStorage.setValue(0);
    }

    public void run() throws JAMSEntity.NoSuchAttributeException{

        double alpha = 0;
        double out_throughfall = 0;
        double out_interception = 0;
        
        /*double ID_HRU = HRU_id.getDouble();/*/

        double in_rain = rain.getValue();
        double in_snow = snow.getValue();
        double in_temp = tmean.getValue();

        double in_potETP = potET.getValue()*area.getValue();
        double in_actETP = 0;

        double in_LAI = this.actLAI.getValue();
        double in_Area = area.getValue();

        double out_InterceptionStorage = intercStorage.getValue();
        double out_actETP = in_actETP;

        double sum_precip = in_rain + in_snow;
        
      // if (HRU_id.getValue() == 1.0){
           //System.out.println(time.toString());
       // }
        
        double deltaETP = in_potETP - in_actETP;

        double relRain, relSnow;
        if(sum_precip > 0){
            relRain = in_rain / sum_precip;
            relSnow = in_snow / sum_precip;
        } else{
            relRain = 1.0; //throughfall without precip is in general considered to be liquid
            relSnow = 0;
        }

        //determining if precip falls as rain or snow
        if(in_temp < (snow_trs.getValue() - snow_trans.getValue())){
            //alpha = alpha_snow;
            alpha = a_snow.getValue();
        } else{
            //alpha = alpha_rain;
            alpha = a_rain.getValue();
        }

        //determinining maximal interception capacity of actual day
        double maxIntcCap = (in_LAI * alpha) * in_Area;

        //if interception storage has changed from snow to rain then throughfall
        //occur because interception storage of antecedend day might be larger
        //then the maximum storage capacity of the actual time step.
        if(out_InterceptionStorage > maxIntcCap){
            out_throughfall = out_InterceptionStorage - maxIntcCap;
            out_InterceptionStorage = maxIntcCap;
        }

        //determining the potential storage volume for daily Interception
        double deltaIntc = maxIntcCap - out_InterceptionStorage;

        //reducing rain and filling of Interception storage
        if(deltaIntc > 0){
            //double save_rain = sum_precip;
            if(sum_precip > deltaIntc){
                out_InterceptionStorage = maxIntcCap;
                sum_precip = sum_precip - deltaIntc;
                out_throughfall = out_throughfall + sum_precip;
                out_interception = deltaIntc;
                deltaIntc = 0;
            } else{
                out_InterceptionStorage = (out_InterceptionStorage + sum_precip);
                out_interception = sum_precip;
                sum_precip = 0;
            }
        } else{
            out_throughfall = out_throughfall + sum_precip;
        }
        
        
    //    if (HRU_id.getValue() == 1.0){
    //       System.out.println(time.toString());
    //    }
        
        //depletion of interception storage; beside the throughfall from above interc.
        //storage can only be depleted by evapotranspiration

        if(deltaETP > 0){
            if(out_InterceptionStorage > deltaETP){
                out_InterceptionStorage = out_InterceptionStorage - deltaETP;
                out_actETP = in_actETP + deltaETP;
                deltaETP = 0;

            } else{
                out_actETP = in_actETP + out_InterceptionStorage;
                out_InterceptionStorage = 0;
                deltaETP = 0;
            }
        } 
        
     //   if (HRU_id.getValue() == 1.0){
     //      System.out.println(time.toString());
     //   }
        
        this.netRain.setValue(out_throughfall * relRain);
        this.netSnow.setValue(out_throughfall * relSnow);
        this.actET.setValue(out_actETP);
        this.potET.setValue(in_potETP);
        this.intercStorage.setValue(out_InterceptionStorage);
        this.interception.setValue(out_interception);
        this.throughfall.setValue(out_throughfall);
        
    }

    public void cleanup() {
        this.intercStorage.setValue(0);
    }

}