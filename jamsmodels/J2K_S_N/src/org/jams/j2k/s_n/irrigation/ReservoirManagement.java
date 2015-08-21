/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.jams.j2k.s_n.irrigation;
import jams.data.*;
import jams.model.*;
/**
 *
 * @author c6gohe2
 */
@JAMSComponentDescription(
        title="J2KPlantGrowthNitrogenStress",
        author="c6gohe2",
        description="Calculation of the plant groth nitrogen factor after SWAT"
        )
        public class ReservoirManagement extends JAMSComponent {


    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "HRU crop class"
            )
            public Attribute.Double storageInput;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = "HRU crop class"
            )
            public Attribute.Double storage;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = "HRU crop class"
            )
    public Attribute.Double irrigationsum;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            description = "HRU crop class"
            )
    public Attribute.Double irrigationpart;


//Berechnung
    public void init(){
        irrigationpart.setValue(0.0);
    }
    public void run (){

        double irripart = 0;

        storage.setValue(storageInput.getValue()*1000); // from m³/day to l/day

        irripart = irrigationsum.getValue()/storage.getValue();
        
        if (irripart > 1) {
            irripart = 1;
        }
        
        irrigationpart.setValue(irripart);



        irrigationsum.setValue(0.0);


    }



}
