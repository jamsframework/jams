/*
 * ShapeEntityReader.java
 * Created on 21. Juni 2006, 15:00
 *
 * This file is part of JAMS
 * Copyright (C) 2005 FSU Jena
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

package jams.util;

import org.unijena.jams.data.*;
import org.unijena.jams.model.*;
import java.util.*;
import org.unijena.jams.JAMS;
import java.net.*;
import java.io.IOException;
import org.geotools.data.shapefile.*;
import org.geotools.data.*;

import org.geotools.feature.AttributeType;
import org.geotools.feature.Feature;
import org.geotools.feature.FeatureType;

/**
 *
 * @author Christian Fischer
 */
public class ShapeEntityReader extends StandardEntityReader {
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            update = JAMSVarDescription.UpdateType.INIT,
            description = "Workspace directory name"
            )
            public JAMSString dirName;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            update = JAMSVarDescription.UpdateType.INIT,
            description = "HRU parameter file name"
            )
            public JAMSString hruFileName;
        
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            update = JAMSVarDescription.UpdateType.INIT,
            description = "Reach parameter file name"
            )
            public JAMSString reachFileName = null;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            update = JAMSVarDescription.UpdateType.INIT,
            description = "Collection of hru objects"
            )
            public JAMSEntityCollection hrus;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            update = JAMSVarDescription.UpdateType.INIT,
            description = "Collection of reach objects"
            )
            public JAMSEntityCollection reaches;
    
    boolean firstRun = true;

    public static ArrayList<JAMSEntity> readParasFromShpFile(String fileName, JAMSModel model) {
	ArrayList <JAMSEntity> entityList = new ArrayList<JAMSEntity>();
	
	URL shapeURL = null;
	ShapefileDataStore store = null;
	FeatureSource source = null;
	FeatureResults fsShape = null;
	    
	try {
	    shapeURL = new URL("file://" + fileName);
	    store = new ShapefileDataStore(shapeURL);
	}
	catch (MalformedURLException badURL) {
	    model.getRuntime().println("Ungültiger Pfad: " + badURL.getMessage(), JAMS.STANDARD);
	}
	    	
	String name = store.getTypeNames()[0];
	    
	try {
	    source = store.getFeatureSource(name);
	    fsShape = source.getFeatures();
	}
	catch (IOException ioError) {
	    model.getRuntime().println("Shapefile kann nicht gelesen werden: " + ioError.getMessage(), JAMS.STANDARD);
	}

	FeatureType ft = source.getSchema();
	
	String debuginfo = "";
	
	for (int i = 0; i < ft.getAttributeCount(); i++) {
	    AttributeType at = ft.getAttributeType( i );
	    if (!Geometry.class.isAssignableFrom(at.getType()))
		debuginfo += at.getType().getName() + "\t";
		
	}
	model.getRuntime().println(debuginfo, 4);

	debuginfo = "";
	for (int i = 0; i < ft.getAttributeCount(); i++) {
	    AttributeType at = ft.getAttributeType( i );
	    
	    if (!Geometry.class.isAssignableFrom(at.getType()))
		debuginfo += at.getType().getName() + "\t";
	}

	model.getRuntime().println(debuginfo, 4);
	
	try {
	    FeatureReader reader = fsShape.reader();
	    while (reader.hasNext()) {
		Feature feature = reader.next();
		System.out.print(feature.getID() + "\t");
		    
		JAMSEntity e = JAMSDataFactory.createEntity();

		debuginfo = "";
		
		for (int i = 0; i < feature.getNumberOfAttributes(); i++) {
		    Object attribute = feature.getAttribute( i );
		    AttributeType at = ft.getAttributeType( i );
		    String attrName = at.getName();

		    //this should be done in shp file!?
		    if (attrName.equals("AREA")) {
			attrName = "area";
		    }
		    if (attrName.equals("RECHTS")) {
			attrName = "x";
		    }
		    if (attrName.equals("HOCH")) {
			attrName = "y";
		    }
		    if (attrName.equals("HRUPOLC_ID")) {
			attrName = "ID";
		    }
		    if (attrName.equals("TO_POLY")) {
			attrName = "to_poly";
		    }
		    if (attrName.equals("TO_REACH")) {
			attrName = "to_reach";
		    }
   		    if (attrName.equals("TYPE")) {
			attrName = "type";
		    }
   		    if (attrName.equals("ELEVATION")) {
			attrName = "elevation";
		    }
    		    if (attrName.equals("SLOPE")) {
			attrName = "slope";
		    }
		    if (attrName.equals("ASPECT")) {
			attrName = "aspect";
		    }
		    if (attrName.equals("LANDUSE")) {
			attrName = "landuseID";
		    }
		    if (attrName.equals("SOIL_TYPE")) {
			attrName = "soilID";
		    }		    
		    if (attrName.equals("HGEO_TYPE")) {
			attrName = "hgeoID";
		    }
		    if (attrName.equals("PERIMETER")) {  //correct???
			attrName = "flowlength";
		    }
		    
		    if (!(attribute instanceof Geometry)) {
			debuginfo += attribute.toString() + "\t";

			if (at.getType().getName().equals("java.lang.Double") )
			    e.setDouble(attrName, Double.parseDouble(attribute.toString()));
			else if (at.getType().getName().equals("java.lang.Long") )
			    e.setDouble(attrName, Double.parseDouble(attribute.toString()));
			else if (at.getType().getName().equals("java.lang.Integer") )
			    e.setDouble(attrName, Double.parseDouble(attribute.toString()));
			else if (at.getType().getName().equals("java.lang.Float") )
			    e.setDouble(attrName, Double.parseDouble(attribute.toString()));
			else
			    e.setObject(at.getName(), attribute);			
			}
		    }
		entityList.add(e);
		
		model.getRuntime().println(debuginfo, 4);		
		}
	    reader.close();
	    }
	catch (Exception Error) {
	    model.getRuntime().println("Shapefile kann nicht gelesen werden: " + Error.getMessage(), JAMS.STANDARD);
	}
        		
    return entityList;
    }
    
    
    public void init() throws JAMSEntity.NoSuchAttributeException {
        
        if (firstRun) {	    	    
	    //read hru parameter
	    hrus.setEntities(readParasFromShpFile(dirName.getValue() + "/" + hruFileName.getValue(), getModel()));
            	    
            //read reach parameter
	    reaches.setEntities(readParasFromShpFile(dirName.getValue() + "/" + reachFileName.getValue(), getModel()));
            
            //create object associations from id attributes for hrus and reaches
            createTopology();
            	    	    
            //create total order on hrus and reaches that allows processing them subsequently
            getModel().getRuntime().println("Create ordered hru-list", JAMS.STANDARD);
            createOrderedList(hrus, "to_poly");
            getModel().getRuntime().println("Create ordered reach-list", JAMS.STANDARD);
            createOrderedList(reaches, "to_reach");
            getModel().getRuntime().println("Entities read successfull!", JAMS.STANDARD);
            
            firstRun = false;
        }
    }
    //do depth first search to find cycles

}
