/*
 * StandardEntityReader.java
 * Created on 2. November 2005, 15:49
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

package jams.components.io;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import jams.data.*;
import jams.model.*;
import java.util.*;

/**
 *
 * @author S. Kralisch
 */
public class StandardEntityReader extends JAMSComponent {
    
    @JAMSVarDescription(
    access = JAMSVarDescription.AccessType.READ,
            update = JAMSVarDescription.UpdateType.INIT,
            description = "HRU parameter file name"
            )
            public JAMSString hruFileName;
    
    @JAMSVarDescription(
    access = JAMSVarDescription.AccessType.WRITE,
            update = JAMSVarDescription.UpdateType.RUN,
            description = "Collection of hru objects"
            )
            public JAMSEntityCollection hrus;
    
    
    public void init() throws JAMSEntity.NoSuchAttributeException {
        hrus.setEntities(readParas(getModel().getWorkspaceDirectory().getPath() + "/" + hruFileName.getValue()));
    }
    
    public ArrayList<JAMSEntity> readParas(String fileName) {
        
        BufferedReader reader;
        ArrayList <JAMSEntity> entityList = new ArrayList<JAMSEntity>();
        StringTokenizer tokenizer;
        
        try {
            
            reader = new BufferedReader(new FileReader(fileName));
            
            String s = "#";
            
            // get rid of comments
            while (s.startsWith("#")) {
                s = reader.readLine();
            }
            
            //put the attribure names into a vector
            Vector<String> attributeNames = new Vector<String>();
            tokenizer = new StringTokenizer(s, "\t");
            while (tokenizer.hasMoreTokens()) {
                attributeNames.add(tokenizer.nextToken());
            }
            
            //process lower boundaries
            reader.readLine();
            
            //process upper boundaries
            reader.readLine();
            
            //process units
            reader.readLine();
            
            //get first line of hru data
            s = reader.readLine();
            
            while ((s != null) && !s.startsWith("#"))  {
                
                JAMSEntity e = (JAMSEntity) JAMSDataFactory.createInstance(JAMSEntity.class, getModel().getRuntime());
                tokenizer = new StringTokenizer(s, "\t");
                
                String token;
                for (int i = 0; i < attributeNames.size(); i++) {
                    token = tokenizer.nextToken();
                    try {
                        //hopefully these are double values :-)
                        e.setDouble(attributeNames.get(i), Double.parseDouble(token));
                        getModel().getRuntime().println(attributeNames.get(i) + ": " + token, 4);
                    } catch (NumberFormatException nfe) {
                        //most probably this happens because of string values within J2K parameter files
                        e.setObject(attributeNames.get(i), token);
                    }
                }
                
                entityList.add(e);
                s = reader.readLine();
            }
            
        } catch (IOException ioe) {
            getModel().getRuntime().handle(ioe);
        }
        
        return entityList;
        
    }
    
}
