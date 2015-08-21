/*
 * HydroNETCreator.java
 * Created on 12. Mai 2006, 17:41
 *
 * This file is part of JAMS
 * Copyright (C) 2005 S. Kralisch and P. Krause
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

package org.unijena.hydronet;

import jams.data.*;
import jams.data.Attribute.Entity.NoSuchAttributeException;
import jams.model.*;
import java.util.*;

/**
 *
 * @author Christian Fischer
 */
public class HydroNETCreator extends JAMSComponent {
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            description = "Collection of hru objects"
            )
            public Attribute.EntityCollection hrus;
       
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            description = "Collection of hru objects"
            )
            public Attribute.Entity NitrogenOutEntity;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            description = "Collection of hru objects"
            )
            public Attribute.Entity CostOutEntity;
    
    
    DistNeuron NitrogenOutNeuron = new DistNeuron();
    DistNeuron CostOutNeuron = new DistNeuron();
    
    @Override
    public void init() throws Attribute.Entity.NoSuchAttributeException {        
        Attribute.Entity e,downstreamPoly;
	ArrayList<Attribute.Entity> list = new ArrayList<Attribute.Entity>();
        NONeuron nitr_neuron;
        CostNeuron cost_neuron;
        DistNeuron dist_neuron;
        Neuron downstream_neuron=null;
        	
        getModel().getRuntime().println("Setup HydroNET");
         
        //setup out neurons
        NitrogenOutNeuron.setSizeOfArea(1);
        CostOutNeuron.setSizeOfArea(1.0);
        
        //id function for testing        
	Matrix M2 = new Matrix(2, 2);
        M2.element[0][0] = -100;
        M2.element[0][1] = 0;
        M2.element[1][0] = 100;
        M2.element[1][1] = -0.0;
	
       
	LinApprox lin_id2 = new LinApprox(M2);
        GenericFunction gen_id2 = new GenericFunction(lin_id2);
        //setup net
        for (int i=hrus.getEntities().size()-1;i>=0;i--) {
            e = (Attribute.Entity)hrus.getEntities().get(i);
	    e.setDouble("reduction",0.0);
            //setup nitrogen neuron for each hru
            nitr_neuron = new NONeuron();
            
            nitr_neuron.addFilter((GenericFunction)e.getObject("ActivationFunction"));            
	    nitr_neuron.setID((long)e.getDouble("ID"));
	    
            downstreamPoly = (Attribute.Entity)e.getObject("to_poly");
            //look if id is in hashmap
            if ( downstreamPoly == null || downstreamPoly.getId()==-1) {
                nitr_neuron.setDownstreamNeuron(null,0);
                nitr_neuron.setOutputNeuron(NitrogenOutNeuron,1.0);
                getModel().getRuntime().println("Last HRU in cascade:" + e.getId());
            }
            else {
                try{
                    downstream_neuron = (Neuron)downstreamPoly.getObject("NITROGEN_NEURON");
                }catch(NoSuchAttributeException e2){
                    getModel().getRuntime().println("WARNING: Downstream Neuron = Null");
                }
                if (downstream_neuron == null)
                    getModel().getRuntime().println("WARNING: Downstream Neuron = Null");

                nitr_neuron.setDownstreamNeuron(downstream_neuron,e.getDouble("interflow_weight"));
                nitr_neuron.setOutputNeuron(NitrogenOutNeuron,e.getDouble("percolation_weight"));
            }
            e.setObject("NITROGEN_NEURON",nitr_neuron);
            
            //setup cost neuron for each hru
            cost_neuron = new CostNeuron();
            
            cost_neuron.setSizeOfArea(e.getDouble("area"));
            cost_neuron.addFilter(gen_id2);
            cost_neuron.setCostFactor(1.0);
            cost_neuron.setOutputNeuron(CostOutNeuron);
            cost_neuron.setID((long)e.getDouble("ID"));

            e.setObject("COST_NEURON",cost_neuron);
            
            dist_neuron = new DistNeuron();
            
            dist_neuron.setCostNeuron(cost_neuron);
            dist_neuron.setNitrNeuron(nitr_neuron);
            dist_neuron.setInitialExternInput(e.getDouble("ist_input"));
	    dist_neuron.setMaxInput(e.getDouble("ist_input"));
	    dist_neuron.setMinInput(e.getDouble("min_input"));
	    dist_neuron.setID((long)e.getDouble("ID"));
	    
            e.setObject("DIST_NEURON",dist_neuron);
	    
	    NitrogenOutEntity.setObject("NEURON",this.NitrogenOutNeuron);
	    CostOutEntity.setObject("NEURON",this.CostOutNeuron);	    	   	    
        }
    }
    

/*    public void run() throws Attribute.Entity.NoSuchAttributeException {
        Attribute.Entity e;
        
        NONeuron nitr_neuron;
        CostNeuron cost_neuron;
        DistNeuron dist_neuron;
	while (true) {
	    NitrogenOutNeuron.reset();
	    CostOutNeuron.reset();
	    for (int i=0;i<hrus.getEntities().size();i++) {
	        e = hrus.getEntities().get(i);
            
                nitr_neuron = (NONeuron)e.getObject("NITROGEN_NEURON");
	        cost_neuron = (CostNeuron)e.getObject("COST_NEURON");
	        dist_neuron = (DistNeuron)e.getObject("DIST_NEURON");
	    	    
		nitr_neuron.reset();
                cost_neuron.reset();	    
	    }
            //first step propagate
	    for (int i=0;i<hrus.getEntities().size();i++) {
	        e = hrus.getEntities().get(i);
            
                nitr_neuron = (NONeuron)e.getObject("NITROGEN_NEURON");
	        cost_neuron = (CostNeuron)e.getObject("COST_NEURON");
	        dist_neuron = (DistNeuron)e.getObject("DIST_NEURON");
            
                dist_neuron.propagate();
	        nitr_neuron.propagate();
	        cost_neuron.propagate();
            }
	    NitrogenOutNeuron.calc();
	    CostOutNeuron.calc();
        
            getModel().getRuntime().println("NO - Output:" + new Double(NitrogenOutNeuron.getActivation()).toString() + 
	                     " Cost - Output:" + new Double(CostOutNeuron.getActivation()).toString() );
        
	    //second step backpropagate        
            NitrogenOutNeuron.setDelta(-NitrogenOutNeuron.getActivation());
	    CostOutNeuron.setDelta(-CostOutNeuron.getActivation());
        
	    for (int i=hrus.getEntities().size()-1;i>=0;i--) {
		e = hrus.getEntities().get(i);
            
                nitr_neuron = (NONeuron)e.getObject("NITROGEN_NEURON");
	        cost_neuron = (CostNeuron)e.getObject("COST_NEURON");
	        dist_neuron = (DistNeuron)e.getObject("DIST_NEURON");
            
                nitr_neuron.backpropagate();
	        cost_neuron.backpropagate();
	        dist_neuron.backpropagate();
	    }
        
            //third step modify weights
	    for (int i=0;i<hrus.getEntities().size();i++) {
	        e = hrus.getEntities().get(i);
            
                dist_neuron = (DistNeuron)e.getObject("DIST_NEURON");
	        dist_neuron.modifyWeight();
	    }
       }
    }*/
}


