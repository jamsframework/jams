/*
 * StandardEntityReader.java
 * Created on 2. November 2005, 15:49
 *
 * This file is part of JAMS
 * Copyright (C) 2005 S. Kralisch and P. Krause
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

package optas.io;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import jams.data.*;
import jams.model.*;
import java.io.BufferedWriter;
import java.io.FileWriter;
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
    
    
    public void init() {
        hrus.setEntities(readParas(getModel().getWorkspaceDirectory().getPath() + "/" + hruFileName.getValue(),this.getModel()));
    }

    static public void writeParas(Collection<Attribute.Entity> entityList, String fileName, Model model){
        BufferedWriter writer;

        Iterator<Attribute.Entity> iter = entityList.iterator();
        Attribute.Entity example = null;
        while(iter.hasNext()){
            if ( (example = iter.next())!=null)
                break;
        }
        if (example==null){
            model.getRuntime().sendHalt("writeParas: error no hrus to write");
        }

        try {

            writer = new BufferedWriter(new FileWriter(fileName));

            writer.write("# reduced hru.par created " + (new GregorianCalendar().toString()) + "\n");


            //put the attribure names into a vector
            Vector<String> attributeNames = new Vector<String>();
            Object[] keys = example.getKeys();
            for(Object key : keys){
                attributeNames.add((String)key);
                writer.write((String)key + "\t");
            }
            writer.write("\n");
            //process lower boundaries
            writer.write("lower Boundaries not available\n");

            //process upper boundaries
            writer.write("upper Boundaries not available\n");

            //process units
            writer.write("process units not available\n");

            iter = entityList.iterator();
            while (iter.hasNext()){
                Attribute.Entity e = iter.next();
                if (e==null)
                    continue;
                for (int i=0;i<attributeNames.size();i++){
                    String attribute = attributeNames.get(i);
                    try{
                        writer.write(e.getDouble(attribute)+"\t");
                    }catch(Attribute.Entity.NoSuchAttributeException nsae){
                        model.getRuntime().sendHalt("announced attribute \"" + attributeNames.get(i) + "\" is not existing!");
                    }
                }
                writer.write("\n");
            }
            writer.flush();
            writer.close();
        } catch (IOException ioe) {
            model.getRuntime().handle(ioe);
        }
    }
    public static ArrayList<Attribute.Entity> readParas(String fileName, Model model) {

        BufferedReader reader;
        ArrayList<Attribute.Entity> entityList = new ArrayList<Attribute.Entity>();
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
                String aName = tokenizer.nextToken();

                // this is just a bloody workaround for old J2000 reach parameter files
                if (aName.equals("to-reach")) {
                    aName = "to_reach";
                }
                attributeNames.add(aName);
            }

            //process lower boundaries
            reader.readLine();

            //process upper boundaries
            reader.readLine();
            //coment
            //process units
            reader.readLine();

            //get first line of hru data
            s = reader.readLine();

            while ((s != null) && !s.startsWith("#")) {

                Attribute.Entity e;

                e = DefaultDataFactory.getDataFactory().createEntity();
                tokenizer = new StringTokenizer(s, "\t");

                String token;
                for (int i = 0; i < attributeNames.size(); i++) {
                    token = tokenizer.nextToken();
                    try {
                        //hopefully these are double values :-)
                        e.setDouble(attributeNames.get(i), Double.parseDouble(token));
                    } catch (NumberFormatException nfe) {
                        //most probably this happens because of string values within J2K parameter files
                        e.setObject(attributeNames.get(i), token);
                    }
                }

                entityList.add(e);

                s = reader.readLine();
            }

        } catch (IOException ioe) {
            model.getRuntime().handle(ioe);
        }

        return entityList;

    }
    
}
