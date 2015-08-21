/*
 * TwoCompGroundwater.java
 * Created on 21. May 2009, 16:54
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

package org.unijena.j2000g;

import jams.data.*;
import jams.model.*;

/**
 *
 * @author c0krpe
 */
@JAMSComponentDescription(
        title="TwoCompGroundwater",
        author="Peter Krause",
        description="Groundwater module with two components (matrix and fissures" +
        "or clefts). The incoming water is distributed among the components with" +
        "a parameter alpha (0;1). The two components are represented by linear" +
        "flow cascades with the two parmeters n1,2 (number of storages) and k1,2 " +
        "(recession coefficients) which hold the water back for some time to " +
        "simulate translation and retention. The two components are aggregated " +
        "at the end to provide the outflow of the module."
        )
        public class TwoCompGroundwater extends JAMSComponent {
    
    /*
     *  Component variables
     */
     @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "groundwater recharge from antecedent module"
            )
            public Attribute.Double gwRecharge;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "number of storages of cascade 1"
            )
            public Attribute.Integer n1;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "recision coefficient of cascade 1"
            )
            public Attribute.Double k1;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            description = "groundwater storages of cascade 1"
            )
            public Attribute.DoubleArray gwStor1;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            description = "total groundwater storage content of cascade 1"
            )
            public Attribute.Double gwStorCont1;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            description = "outflow from cascade 1"
            )
            public Attribute.Double q1;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "number of storages of cascade 2"
            )
            public Attribute.Integer n2;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "recision coefficient k2"
            )
            public Attribute.Double k2;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            description = "groundwater storages of cascade 2"
            )
            public Attribute.DoubleArray gwStor2;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            description = "total groundwater storage content of cascade 2"
            )
            public Attribute.Double gwStorCont2;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            description = "outflow from cascade 2"
            )
            public Attribute.Double q2;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            description = "aggregated outflow from both cascades"
            )
            public Attribute.Double basQ;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "distribution coefficient alpha"
            )
            public Attribute.Double alpha;
    
    /*
     *  Component run stages
     */
    
    public void init() {
        //cascade 1 setup
        double[] stor1 = new double[this.n1.getValue()];
        for(int i = 0; i < this.n1.getValue(); i++)
            stor1[i] = 0;
        //gwStor1 = getModel().getRuntime().getDataFactory().createDoubleArray();
        gwStor1.setValue(stor1);

        //cascade 2 setup
        double[] stor2 = new double[this.n2.getValue()];
        for(int i = 0; i < this.n2.getValue(); i++)
            stor2[i] = 0;
        //gwStor2 = getModel().getRuntime().getDataFactory().createDoubleArray();
        gwStor2.setValue(stor2);
    }
    
    public void run() {
       double inflow = this.gwRecharge.getValue();
       double inC1 = this.alpha.getValue() * inflow;
       double inC2 = (1 - this.alpha.getValue()) * inflow;

       //the first cascade
       //retreive the current storages
       double[] stor1 = gwStor1.getValue();
       //the ouflows from the single storages in the cascade
       double[] out1 = new double[stor1.length];
       //add input to first tank
        stor1[0] = stor1[0] + inC1;
        for(int i = 0; i < this.n1.getValue(); i++){
            //outflow of each tank
            out1[i] = 1 / this.k1.getValue() * stor1[i];
            stor1[i] = stor1[i] - out1[i];
            //adding outflow to next tank in cascade
            if(i < this.n1.getValue() - 1){
                stor1[i+1] = stor1[i+1] + out1[i];
            }
        }
        //outflow of last tank in cascade
        this.q1.setValue(out1[this.n1.getValue()-1]);
        //saving storages
        this.gwStor1.setValue(stor1);

        //the second cascade
       //retreive the current storages
       double[] stor2 = gwStor2.getValue();
       //the ouflows from the single storages in the cascade
       double[] out2 = new double[stor2.length];
       //add input to first tank
        stor2[0] = stor2[0] + inC2;
        for(int i = 0; i < this.n2.getValue(); i++){
            //outflow of each tank
            out2[i] = 1 / this.k2.getValue() * stor2[i];
            stor2[i] = stor2[i] - out2[i];
            //adding outflow to next tank in cascade
            if(i < this.n2.getValue() - 1){
                stor2[i+1] = stor2[i+1] + out2[i];
            }
        }
        //outflow of last tank in cascade
        this.q2.setValue(out2[this.n2.getValue()-1]);
        //saving storages
        this.gwStor2.setValue(stor2);

        //the total storage contents of cascade 1
        double cont1 = 0;
        for(int i = 0; i < this.n1.getValue(); i++){
            cont1 = cont1 + stor1[i];
        }
        this.gwStorCont1.setValue(cont1);

        //the total storage contents of cascade 2
        double cont2 = 0;
        for(int i = 0; i < this.n2.getValue(); i++){
            cont2 = cont2 + stor2[i];
        }
        this.gwStorCont2.setValue(cont2);

        //the cumulative outflow from both components
        this.basQ.setValue(this.q1.getValue() +  this.q2.getValue());
        //this.gwRecharge.setValue(0);
       
    }
    
    public void cleanup() {
        
    }
    
    
    
    
}
