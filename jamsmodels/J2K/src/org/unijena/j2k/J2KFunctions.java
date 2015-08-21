/*
 * J2KFunctions.java
 * Created on 10. November 2005, 10:31
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
package org.unijena.j2k;

import java.util.*;
import java.io.*;
import jams.data.*;
import jams.model.Model;

/**
 *
 * @author S. Kralisch
 */
public class J2KFunctions {
        
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

                String token=null;
                for (int i = 0; i < attributeNames.size(); i++) {
                    try{
                        token = tokenizer.nextToken();
                    }catch(NoSuchElementException nsee){
                        System.out.println("Error while reading hru parameter file in line " + s);
                    }
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

    public static ArrayList<Attribute.Entity> createStationEntities(String fileName, Model model) {
        ArrayList<Attribute.Entity> entityList = new ArrayList<Attribute.Entity>();
        //handle the j2k metadata descriptions
        int headerLineCount = 0;
        String dataName = null;
        String tres = null;
        String start = null;
        String end = null;
        double lowBound, uppBound, missData;

        String[] name, id;
        double[] statx = null;
        double[] staty = null;
        double[] statelev = null;


        String line = "#";
        try {
            BufferedReader reader = new BufferedReader(new FileReader(fileName));

            //skip comment lines
            while (line.charAt(0) == '#') {
                line = reader.readLine();
                headerLineCount++;
            }

            //metadata tags
            StringTokenizer strTok = new StringTokenizer(line, "\t");
            String token = strTok.nextToken();
            while (token.compareTo("@dataVal") != 0) {
                if (token.compareTo("@dataValueAttribs") == 0) {
                    line = reader.readLine();
                    headerLineCount++;
                    strTok = new StringTokenizer(line, "\t");
                    dataName = strTok.nextToken();
                    lowBound = Double.parseDouble(strTok.nextToken());
                    uppBound = Double.parseDouble(strTok.nextToken());
                    line = reader.readLine();
                    strTok = new StringTokenizer(line, "\t");
                    token = strTok.nextToken();
                    headerLineCount++;
                } else if (token.compareTo("@dataSetAttribs") == 0) {
                    int i = 0;
                    line = reader.readLine();
                    while (i < 4) {
                        headerLineCount++;
                        strTok = new StringTokenizer(line, "\t");
                        String desc = strTok.nextToken();
                        if (desc.compareTo("missingDataVal") == 0) {
                            missData = Double.parseDouble(strTok.nextToken());
                        } else if (desc.compareTo("dataStart") == 0) {
                            start = strTok.nextToken();
                        } else if (desc.compareTo("dataEnd") == 0) {
                            end = strTok.nextToken();
                        } else if (desc.compareTo("tres") == 0) {
                            tres = strTok.nextToken();
                        }
                        i++;
                        line = reader.readLine();
                        strTok = new StringTokenizer(line, "\t");
                        token = strTok.nextToken();
                    }
                } else if (token.compareTo("@statAttribVal") == 0) {
                    int i = 0;
                    line = reader.readLine();
                    while (i < 6) {
                        headerLineCount++;
                        strTok = new StringTokenizer(line, "\t");
                        String desc = strTok.nextToken();
                        int nstat = strTok.countTokens();

                        if (desc.compareTo("name") == 0) {
                            name = new String[nstat];
                            for (int j = 0; j < nstat; j++) {
                                name[j] = strTok.nextToken();
                            }
                        } else if (desc.compareTo("ID") == 0) {
                            id = new String[nstat];
                            for (int j = 0; j < nstat; j++) {
                                id[j] = strTok.nextToken();
                            }
                        } else if (desc.compareTo("elevation") == 0) {
                            statelev = new double[nstat];
                            for (int j = 0; j < nstat; j++) {
                                statelev[j] = Double.parseDouble(strTok.nextToken());
                            }
                        } else if (desc.compareTo("x") == 0) {
                            statx = new double[nstat];
                            for (int j = 0; j < nstat; j++) {
                                statx[j] = Double.parseDouble(strTok.nextToken());
                            }
                        } else if (desc.compareTo("y") == 0) {
                            staty = new double[nstat];
                            for (int j = 0; j < nstat; j++) {
                                staty[j] = Double.parseDouble(strTok.nextToken());
                            }
                        } else if (desc.compareTo("dataColumn") == 0) {
                            //do nothing for the moment just counting
                            headerLineCount++;
                            headerLineCount++;
                        }
                        i++;
                        line = reader.readLine();
                        strTok = new StringTokenizer(line, "\t");
                        token = strTok.nextToken();
                    }
                }
            }


        } catch (IOException ioe) {
            model.getRuntime().handle(ioe);
        }
        return entityList;
    }
}
