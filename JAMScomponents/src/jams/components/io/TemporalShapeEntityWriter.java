/*
 * ShapeEntityReader.java
 * Created on 22. Januar 2009, 21:32
 *
 * This file is part of JAMS
 * Copyright (C) FSU Jena
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public License
 * as published by the Free Software Foundation; either version 3
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, see <http://www.gnu.org/licenses/>.
 *
 */
package jams.components.io;

import jams.JAMS;
import jams.components.aggregate.ShapeFileOutputDataStore;
import jams.components.aggregate.SimpleOutputDataStore;
import jams.components.aggregate.SpatialOutputDataStore;
import jams.data.AbstractDataSupplier;
import jams.data.Attribute;
import jams.model.JAMSComponent;
import jams.model.JAMSComponentDescription;
import jams.model.JAMSVarDescription;
import jams.tools.FileTools;
import java.io.File;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashSet;

/**
 *
 * @author Sven Kralisch <sven.kralisch at uni-jena.de>
 */
@JAMSComponentDescription (title = "ShapeEntityReader",
                           author = "Sven Kralisch",
                           description = "Reads a shape file and creates a " +
"list of JAMS entities containing an entity for each feature. An attribute " +
"name must be provided in order to identify the id field used in the shape file")
public class TemporalShapeEntityWriter extends JAMSComponent {

    @JAMSVarDescription (access = JAMSVarDescription.AccessType.READ,
                         description = "Name of the shape file")
    public Attribute.Boolean isEnabled[];
    
    @JAMSVarDescription (access = JAMSVarDescription.AccessType.READ,
                         description = "Name of the shape file")
    public Attribute.String srcShapeFile;
    
    @JAMSVarDescription (access = JAMSVarDescription.AccessType.READ,
                         description = "Name of identifying attribute in shape file")
    public Attribute.String idName;
    
    @JAMSVarDescription (access = JAMSVarDescription.AccessType.READ,
                         description = "names of attributes to be created")
    public Attribute.String attributeNames[];
    
    @JAMSVarDescription (access = JAMSVarDescription.AccessType.READ,
                         description = "names of attributes to be created")
    public Attribute.String attributes[];
    
    @JAMSVarDescription (access = JAMSVarDescription.AccessType.READ,
                         description = "time ",
                         defaultValue = "1970-01-01 00:00")
    public Attribute.Calendar time;
    
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
    description = "custom number of hrus to filter",
    defaultValue = "")
    public Attribute.String idFilters;
    
    @JAMSVarDescription (access = JAMSVarDescription.AccessType.WRITE,
                         description = "Entity collection to be created")
    public Attribute.EntityCollection entities;
        
    SimpleOutputDataStore outData[] = null;
    SpatialOutputDataStore outData2[] = null;
    File dbfFileOriginal = null;
    ShapeFileOutputDataStore shpStore[] = null;
    HashSet<Double> selectedIds = null;
    
    boolean isHeaderWritten = false;
           
    Attribute.Calendar lastTimeStep = null;
    int n = 0;
    
    private class EntityDataProvider extends AbstractDataSupplier<Double, Attribute.EntityCollection>{
        String name = "";
        public EntityDataProvider(String name, Attribute.EntityCollection input){
            super(input);
            this.name = name;
        }
        @Override
        public int size(){
            return input.getEntities().size();
        }
        @Override
        public Double get(int i){
            return input.getEntities().get(i).getDouble(name);
        }
    }
    
    EntityDataProvider entityDataProviders[];
    
    protected boolean isIDSelected(double id) {      
        if (!this.idFilters.getValue().isEmpty()) {
            if (selectedIds != null){
                return selectedIds.contains(id);
            }
            selectedIds = new HashSet<Double>();
            
            String idFilter[] = idFilters.getValue().split(";");
            for (String filter : idFilter) {
                try {
                    if (filter.contains("[")) {
                        String ids[] = filter.split("-");
                        ids[0] = ids[0].replace("[", "");
                        ids[0] = ids[0].replace("]", "");
                        ids[1] = ids[1].replace("[", "");
                        ids[1] = ids[1].replace("]", "");
                        double id0 = Double.parseDouble(ids[0]);
                        double id1 = Double.parseDouble(ids[1]);
                        while (id0 <= id1){
                            selectedIds.add(id0);
                            id0++;
                        }
                    } else {
                        double idF = Double.parseDouble(filter);
                        selectedIds.add(idF);
                    }                    
                } catch (Throwable nfe) {
                    getModel().getRuntime().sendErrorMsg("Error: Could not parse filter string:" + filter);
                    return false;
                }
            }
            return selectedIds.contains(id);
            
        } else {
            return true;
        }        
    }
    @Override
    public void init() {
        n = attributeNames.length;
        
        isHeaderWritten = false;
        outData = new SimpleOutputDataStore[n];        
        outData2 = new SpatialOutputDataStore[n];        
        shpStore = new ShapeFileOutputDataStore[n];
        entityDataProviders = new EntityDataProvider[n];
                
        //copy shapefile to output directory
        for (int i = 0; i < n; i++) {            
            if (!isEnabled[i].getValue()){
                continue;
            }
            String path = getModel().getWorkspace().getOutputDataDirectory().getAbsolutePath();
            String fileName = this.getInstanceName()+"_"+attributeNames[i];
                    
            File f = new File(FileTools.createAbsoluteFileName(path,fileName + ".dat"));
            File f2 = new File(FileTools.createAbsoluteFileName(path,fileName + "_SODS.dat"));
            entityDataProviders[i] = new EntityDataProvider(this.attributes[i].getValue(), entities);
            try {
                outData[i] = new SimpleOutputDataStore(f);
                outData2[i] = new SpatialOutputDataStore(f2);
            } catch (IOException ioe) {
                getModel().getRuntime().sendHalt("Can't write to output file:" + f);
            } 
            
            File originalShpFile = new File(FileTools.createAbsoluteFileName(getModel().getWorkspacePath(), srcShapeFile.getValue()));
            File newDBFFile = new File(path + "/" + fileName);
            newDBFFile.mkdirs();
            try {
                shpStore[i] = new ShapeFileOutputDataStore(originalShpFile, newDBFFile);
            } catch (IOException ioe) {
                getModel().getRuntime().sendErrorMsg(MessageFormat.format(ioe.toString(), getInstanceName()));
            }
        }
        
    }
                           
    @Override
    public void run(){
        /*if (lastTimeStep==null){
            lastTimeStep = time.getValue();
            return;
        }*/
        if (this.time.getTimeInMillis() == JAMS.getMissingDataValue(Long.class)){
            return;
        }
        if (lastTimeStep != null && this.time.getTimeInMillis() == lastTimeStep.getTimeInMillis()){
            return;
        }
        lastTimeStep = time.getValue();
        try {
            for (int i = 0; i < n; i++) {
                if (!isEnabled[i].getValue()) {
                    continue;
                }
                if (!isHeaderWritten) {
                    ArrayList<Double> ids = new ArrayList<Double>();
                    for (int j = 0; j < entities.getEntities().size(); j++) {
                        double iid = entities.getEntities().get(j).getId();
                        //if not selected, do nothing        
                        if (!isIDSelected(iid)){
                            return;
                        }
                        ids.add(iid);
                    }
                    outData[i].setHeader(ids);
                    outData2[i].setHeader(ids);
                }
                               
                outData[i].writeData(time.toString(), entityDataProviders[i]);                        
                outData2[i].writeData(time.toString(), entityDataProviders[i]);
            }
            isHeaderWritten = true;
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
    }
    
    public void cleanup() {
        for (int i = 0; i < n; i++) {
            if (isEnabled[i].getValue()) {
                getModel().getRuntime().sendInfoMsg("Transfering data to shapefile from dataset: " + outData[i].getFile().getName());
                try {
                    shpStore[i].addDataToShpFiles(outData[i], this.idName.getValue());
                } catch (IOException ioe) {
                    getModel().getRuntime().sendHalt("Can't write to output file:" + outData[i].getFile() + "\n" + ioe.toString());
                }
            }
        }
    }
}
