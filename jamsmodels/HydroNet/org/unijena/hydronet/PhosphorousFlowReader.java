/*
 * StandardNitrogenFlowReader.java
 * Created on 22. September 2006, 15:53
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
import org.unijena.j2k.*;
import jams.data.*;
import jams.model.*;
import java.util.*;
import jams.JAMS;
import jams.data.Attribute.Entity;

/**
 *
 * @author C. Fischer
 */

public class PhosphorousFlowReader extends JAMSComponent {
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "Soil types parameter file name"
            )
            public Attribute.String Scenario0pFileName;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "Soil types parameter file name"
            )
            public Attribute.String ScenarioFileNames;
        
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "Soil types parameter file name"
            )
            public Attribute.String ScenarioISTFileName;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            description = "Collection of hru objects"
            )
            public Attribute.EntityCollection hrus;
            
    @Override
    public void init() throws Attribute.Entity.NoSuchAttributeException {
	StringTokenizer tok = new StringTokenizer(ScenarioFileNames.getValue(), ";");
        
	int N = tok.countTokens();
        Attribute.EntityCollection[] FlowData = new Attribute.EntityCollection[N+1];
	
	for (int i=0;i<N;i++) {
	    FlowData[i] = getModel().getRuntime().getDataFactory().createEntityCollection();
	    FlowData[i].setEntities(J2KFunctions.readParas(getModel().getWorkspacePath()+"/"+tok.nextToken(), getModel()));
	}
	FlowData[N] = getModel().getRuntime().getDataFactory().createEntityCollection();
	FlowData[N].setEntities(J2KFunctions.readParas(getModel().getWorkspacePath()+"/"+ScenarioISTFileName.getValue(), getModel()));
	
        HashMap<Double, Entity[]> fdMap = new HashMap<Double, Entity[]>();
        Entity fd, e;
	Entity[] field_fd;
        Object[] attrs;
        		
	for (int i=0;i<N+1;i++) {
	    //put all entities into a HashMap with their ID as key
	    Iterator<Entity> fdIterator = FlowData[i].getEntities().iterator();	    
		    
	    while (fdIterator.hasNext()) {
		fd = fdIterator.next();
		field_fd = fdMap.get(fd.getDouble("ID"));
		
		if ( field_fd == null) {
		    field_fd = new Attribute.Entity[N+1];
		    fdMap.put(fd.getDouble("ID"),  field_fd);
		}
		
		field_fd[i] = fd;
	    }
	}
               	
        Iterator<Entity> hruIterator = hrus.getEntities().iterator();
        while (hruIterator.hasNext()) {
        	
            e = hruIterator.next();
            //System.out.println("Processing hruNO: " + e.getDouble("ID"));
            field_fd = fdMap.get(e.getDouble("ID"));
	    
	    if (field_fd == null)
		continue;
	
	    Matrix M = new Matrix(N, 2);		
	    //berechne verhältnis von 
	    double interflow = 0;
	    double percolation = 0; 
		
	    for (int i=0;i<N;i++) {
	        interflow += field_fd[i].getDouble("sum_SurfaceSolubleP") + field_fd[i].getDouble("sum_activP") + field_fd[i].getDouble("sum_org_P") 
                         + field_fd[i].getDouble("sum_residue_P") + field_fd[i].getDouble("sum_stableP"); 
	        percolation += 0;
	    }
		
	    interflow /= N;
	    percolation /= N;
		
	    double ratio_interflow = interflow / (interflow + percolation);
	    double ratio_percolation = 1 - ratio_interflow;
		
	    for (int j=0;j<N;j++) {
	        M.element[j][0] = field_fd[j].getDouble("sum_sum_Pinput");
	        M.element[j][1] = field_fd[j].getDouble("sum_SurfaceSolubleP") + field_fd[j].getDouble("sum_activP") + field_fd[j].getDouble("sum_org_P") 
                         + field_fd[j].getDouble("sum_residue_P") + field_fd[j].getDouble("sum_stableP");	
		}
		
            
            
	    LinApprox lin = new LinApprox(M);
	    GenericFunction gen = new GenericFunction(lin);
		
	    //System.out.println("AktivationFunction:" + M.toString() + "\n");
		
	    e.setObject("ActivationFunction",gen);		
	    e.setDouble("new_input",-1.0);
	    e.setDouble("interflow_weight",ratio_interflow);
	    e.setDouble("percolation_weight",ratio_percolation);
	    e.setDouble("ist_input",field_fd[N].getDouble("sum_Ninput_2000"));
	    e.setDouble("min_input",field_fd[0].getDouble("sum_Ninput_2000"));
        }
        
        getModel().getRuntime().println("NitrogenFlow parameter file processed ...", JAMS.STANDARD);        
    }
}