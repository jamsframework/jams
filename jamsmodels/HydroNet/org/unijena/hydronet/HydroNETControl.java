/*
 * HydroNETControl.java
 * Created on 2. Juni 2006, 18:00
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


/**
 * Description:  This Class manages a set of 2D points and provides a method to compute
 *				 a polynom as result of nonlinear regression via solution of a least
 *				 squares minimization problem. Most of the code, especially the classes
 *				 Matrix, Numeric and specialFunctions, comes from Brian Lewis,
 *				 url: http://www.mcs.kent.edu/~blewis/
 * Copyright:    Copyright (c) 2000
 * Company:      FSU Jena
 * @author:      Christian Fischer
 */

package org.unijena.hydronet;

import jams.model.*;
import jams.data.*;

@JAMSComponentDescription(
        title="HydroNETControl",
        author="Christian Fischer",
        description="Context Component which controls optimization"
        )
	
public class HydroNETControl extends JAMSContext {
   @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            description = "Collection of hru objects"
            )
            public Attribute.EntityCollection hrus;
   
   @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = "Nitrongen Output Neuron"
            )
            public Attribute.Entity NitrogenOutEntity;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = "Cost Output Neuron"
            )
            public Attribute.Entity CostOutEntity;    
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "smallest improvement which is accepted"
            )
            public Attribute.Double delta_min;  
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "learning rate"
            )
            public Attribute.Double learningrate;  
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "momentum",
            defaultValue="0.9"
            )
            public Attribute.Double momentum;  
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "largest accepted nitrogen value"
            )
            public Attribute.Double nitrogen_goal;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = "largest accepted nitrogen value"
            )
            public Attribute.Double current_output;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = "largest accepted nitrogen value"
            )
            public Attribute.Double current_cost;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = "largest accepted nitrogen value"
            )
            public Attribute.Double current_sum;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = "largest accepted nitrogen value"
            )
            public Attribute.Double current_iteration;
    
    private double errorNO,errorCost;   
    private double avgperformance=1000,performance;
    private double minError = 1000000000000000.0;
    private double lasterror;    
    private int iteration;
    //private double delta_min = 0.5;    
    //private double learningrate = 0.000005;
    private int breakcount = 550;
    
    public boolean hasNext() throws Attribute.Entity.NoSuchAttributeException {
	DistNeuron NitrogenOutNeuron = (DistNeuron)NitrogenOutEntity.getObject("NEURON");
	DistNeuron CostOutNeuron = (DistNeuron)CostOutEntity.getObject("NEURON");
			
	errorNO = NitrogenOutNeuron.getActivation() - nitrogen_goal.getValue();
	double outbefore  = NitrogenOutNeuron.getActivation();
        errorCost = CostOutNeuron.getActivation();
		
	if (iteration<=2) {
	    lasterror = Math.abs(errorNO) + Math.abs(errorCost);
	    return true;
	}
	    
	
	if (Math.abs(errorNO)+Math.abs(errorCost) < minError)
	    minError = Math.abs(errorNO)+Math.abs(errorCost);
	
	performance = lasterror - (Math.abs(errorNO) + Math.abs(errorCost));
	
	avgperformance = 0.9*avgperformance + 0.1 * performance;
	
	if (Math.abs(errorNO + errorCost ) > Math.abs(minError)) {
              //learningrate /= 1.05;
              breakcount--;
            }
            else
            {
                minError = errorNO + errorCost;
                if (performance < delta_min.getValue()) {
                    learningrate.setValue(learningrate.getValue()*1.035);
                }
                else {
                    breakcount = 200;
                }
            }
	lasterror = Math.abs(errorNO)+Math.abs(errorCost);
	
	DistNeuron.eta = learningrate.getValue();
			
	System.out.println("Output before learning : " + outbefore + " NO - Output : " + new Double(NitrogenOutNeuron.getActivation()).toString() + "\t" + 
                         "  Cost - Output : " + new Double(CostOutNeuron.getActivation()).toString() + 
                         "  Summe - Output : " + 
		         "  AvgPerf : " + new Double(avgperformance).toString());
	
	current_output.setValue(NitrogenOutNeuron.getActivation());
        current_cost.setValue(CostOutNeuron.getActivation());
        current_sum.setValue(NitrogenOutNeuron.getActivation()+CostOutNeuron.getActivation());
	current_iteration.setValue(iteration);
	
	NitrogenOutNeuron.setActivation(current_output.getValue() - nitrogen_goal.getValue());
	
	return (breakcount >= 0 /*&& learningrate > 0.000000000001*/ && avgperformance >= delta_min.getValue() );
	
	
    }
    
    @Override
    public void init () {
        iteration = 0;

	DistNeuron.alpha = momentum.getValue();
	DistNeuron.eta = learningrate.getValue();
        
        super.init();
	
	/*if (runEnumerator == null) {
            runEnumerator = super.getChildrenEnumerator();
        }
	
	runEnumerator.reset();
        while(runEnumerator.hasNext() && doRun) {
            Component comp = runEnumerator.next();
            //comp.
            try {
                comp.init();
            } catch (Exception e) {
                
            }
        }*/
    }
    
    public void singleRun() {  		
	if (runEnumerator == null) {
            runEnumerator = super.getChildrenEnumerator();
        }
			
	runEnumerator.reset();
        while(runEnumerator.hasNext() && doRun) {
            Component comp = runEnumerator.next();
            //comp.updateRun();
            try {
                comp.run();
            } catch (Exception e) {
                System.out.println(e.toString());
            }
        }	
    }
    
    @Override
    public void run () {
	try {
	    while (hasNext()) {
		singleRun();
		iteration++;
		}
	    
	    for (int i=hrus.getEntities().size()-1;i>=0;i--) {
		Attribute.Entity e = (Attribute.Entity)hrus.getEntities().get(i);
	    
		DistNeuron d = (DistNeuron)e.getObject("DIST_NEURON");
	
		if (d.getInitalExternInput() != 0)
		    e.setDouble("reduction",d.getInput() / d.getInitalExternInput());
		else
		    e.setDouble("reduction",0.0);
		
		e.setDouble("new_input",((DistNeuron)e.getObject("DIST_NEURON")).getInput());
	    }	    	    
	}
	catch (Attribute.Entity.NoSuchAttributeException e) {
	    getModel().getRuntime().sendInfoMsg("No such attribute Exception: " + e.getMessage());		    
	}		
    }
    
    @Override
    public void cleanup() {
	if (runEnumerator == null) {
            runEnumerator = super.getChildrenEnumerator();
        }
	runEnumerator.reset();
        while(runEnumerator.hasNext() && doRun) {
            Component comp = runEnumerator.next();
            try {
                comp.cleanup();
            } catch (Exception e) {
                
            }
        }
    }
}
