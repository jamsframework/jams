/*
 * HymodCore.java
 * Created on 14. March 2007, 16:54
 *
 * This file is part of JAMS
 * Copyright (C) 2005 FSU Jena, Peter Krause
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

/*
 * this implementation of hymod is based on the Mathlab version of Hoshin Gupta
 */
package hymod;

import jams.data.*;
import jams.data.JAMSEntity;
import jams.model.JAMSComponent;
import jams.model.JAMSComponentDescription;
import jams.model.JAMSVarDescription;



/**
 *
 * @author c0krpe
 */
@JAMSComponentDescription(
        title="Hymod01",
        author="Peter Krause",
        description="The HYMOD Model implemented based on the MatLab sources" +
        "of Hoshin V. Gupta from 9/18/2005"
        )
        public class HymodCore1 extends JAMSComponent {
    
    /*
     *  Input data
     */
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "precipitation"
            )
            public Attribute.Double precip;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "potential evapotranspiration"
            )
            public Attribute.Double pet;
    
    /*
     * Model parameters
     */
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "Max height of soil moisture accounting tank"
            )
            public Attribute.Double cmax;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "Distribution function shape parameter"
            )
            public Attribute.Double bexp;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "Quick-slow split parameter"
            )
            public Attribute.Double alpha;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "Number of quickflow routing tanks"
            )
            public Attribute.Double nq;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "Quickflow routing tanks rate parameter"
            )
            public Attribute.Double kq;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "Slowflow routing tanks rate parameter"
            )
            public Attribute.Double ks;
    
    /*
     *Initialize state variables
     */
        
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            description = "Soil moisture accounting tank state contents"
            )
            public Attribute.Double xcuz;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            description = "Quickflow routing tanks state contents"
            )
            public Attribute.Double xq;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            description = "Slowflow routing tank state content"
            )
            public Attribute.Double xs;
    
    /*
     *model states
     */
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = "Model computed upper zone soil moisture tank state contents"
            )
            public Attribute.Double mxhuz;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = "Model computed upper zone soil moisture tank state contents"
            )
            public Attribute.Double mxcuz;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = "Model computed quickflow tank states contents"
            )
            public Attribute.DoubleArray mxq;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = "Model computed slowflow tank state contentss"
            )
            public Attribute.Double mxs;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = "Model computed evapotranspiration flux"
            )
            public Attribute.Double met;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = "Model computed precipitation excess flux"
            )
            public Attribute.Double mov;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = "Model computed quickflow flux"
            )
            public Attribute.Double mqq;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = "Model computed slowflow flux"
            )
            public Attribute.Double mqs;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = "Model computed total streamflow flux"
            )
            public Attribute.Double mq;
    
    /*
     *  Component run stages
     */
    
    boolean firstDay = true;
    
    public void init() throws JAMSEntity.NoSuchAttributeException {
       
    }
    
    public void run() throws JAMSEntity.NoSuchAttributeException {
       //if(this.mxq.getValue() == null){
        if(firstDay){
            int n = (int)this.nq.getValue();
            if(n <= 1)
                n = 1;
            double[] stors = new double[n];
            this.mxq.setValue(stors);
            this.mxs.setValue(0);
            double cpar = this.cmax.getValue() / (1 + this.bexp.getValue());
            double huzIni = this.cmax.getValue() * ( 1 - Math.pow((1-0/cpar),1/(1+this.bexp.getValue())));
            this.mxhuz.setValue(huzIni);
            firstDay = false;
       }
       //Run Pdm soil moisture accounting including evapotranspiration
       pdm();
       //Run Nash Cascade routing of quickflow
       double input = this.alpha.getValue() * this.mov.getValue();
       int n = (int)this.nq.getValue();
       if(n <= 1)
           n = 1;
       double[] qOut = nash(input, n, this.kq.getValue(), this.mxq.getValue());
       double[] stor = new double[qOut.length - 1]; 
       
       for(int i = 0; i < stor.length; i++)
           stor[i] = qOut[i];
       this.mxq.setValue(stor);
       
       this.mqq.setValue(qOut[stor.length]);
       
       //Run Infinite Linear tank (Nash Cascade with N=1) routing of slowflow
       input = (1-this.alpha.getValue()) * this.mov.getValue();
       stor = new double[1];
       stor[0] = this.mxs.getValue();
       double[] bOut = nash(input, 1, this.ks.getValue(), stor);
       this.mxs.setValue(bOut[0]);
       this.mqs.setValue(bOut[1]);
       this.mq.setValue(this.mqq.getValue() + this.mqs.getValue());
    }
    
    
    
    
    private void pdm(){
        double bpar, cpar;
        
        if(this.bexp.getValue() == 2){
            bpar = 1000000;
        }
        else{
            //Convert from scaled B (0-2) to unscaled b (0 - Inf)
            bpar = Math.log(1 - (this.bexp.getValue()/2)) / Math.log(0.5);
        }
        //Compute maximum capacity of soil zone
        cpar = this.cmax.getValue() / (1 + bpar);
        
        //Execute model
        //contents at beginning
        double cbeg = cpar * (1 - Math.pow((1-(this.mxhuz.getValue()/this.cmax.getValue())), (1+bpar)));
        //compute ov2 if enough precip
        double ov2 = Math.max(0, this.precip.getValue() + this.mxhuz.getValue() - this.cmax.getValue());
        //precip that does not go to ov2
        double ppinf = this.precip.getValue() - ov2;
        //intermediate height
        double hint = Math.min(this.cmax.getValue(), (ppinf+this.mxhuz.getValue()));
        //intermediate content
        double cint = cpar * (1 - Math.pow((1-(hint/this.cmax.getValue())),(1+bpar)));
        //compute ov1
        double ov1 = Math.max(0, ppinf + cbeg - cint);
        //compute total ov
        this.mov.setValue(ov1 + ov2);
        //compute et
        this.met.setValue(Math.min(this.pet.getValue(), cint));
        //final contents
        this.mxcuz.setValue(cint - this.met.getValue());
        //final height corresponding SMA contents
        this.mxhuz.setValue(this.cmax.getValue() * (1 - Math.pow((1-this.mxcuz.getValue()/cpar),1/(1+bpar))));
    }
    
    public double[] nash(double inp, int n, double k, double[] stor){
        double[] oo = new double[n];
        double[] xend = new double[n+1];
        
        for(int i = 0; i < n; i++){
            try {
                oo[i] = k * stor[i];
            } catch(java.lang.ArrayIndexOutOfBoundsException e) {
                System.out.println("ioofe");
            }
            
            xend[i] = stor[i] - oo[i];
            if(i == 0){
                xend[i] = xend[i] + inp; 
            }
            else{
                xend[i] = xend[i] + oo[i-1];
            }
        }
        xend[n] = oo[n-1];
        
        return xend;
    }
    
    public void cleanup() throws JAMSEntity.NoSuchAttributeException {
       
       this.mxs.setValue(0);
       this.firstDay = true;
    }
    
    
}
