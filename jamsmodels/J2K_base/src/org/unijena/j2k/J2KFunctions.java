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
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author S. Kralisch
 */
public class J2KFunctions {

    private static String[] breakArrayString(String value){
        if (value.startsWith("[") && value.endsWith("]")) {
            value = value.substring(1, value.length() - 1);
            return value.split(",");
        }
        return null;
    }
    
    private static void setTypedValue(Attribute.Entity e, String name, String value, Class type){
        if (type.equals(Attribute.Long.class)) {
            e.setLong(name, Long.parseLong(value));
        } else if (type.equals(Attribute.LongArray.class)) {
            String values[] = breakArrayString(value);
            if (values == null){
                Logger.getLogger(J2KFunctions.class.getName()).log(Level.SEVERE, "Not an array: " + value);
            }
            long doubles[] = new long[values.length];
            int j=0;
            for (String subString : values) {
                doubles[j++] = Long.parseLong(subString);
            }
            Attribute.LongArray array = DefaultDataFactory.getDataFactory().createLongArray();
            array.setValue(doubles);
            e.setObject(name, array);
        } else if (type.equals(Attribute.Boolean.class)) {
             e.setObject(name, Boolean.parseBoolean(value));
        } else if (type.equals(Attribute.BooleanArray.class)) {
            String values[] = breakArrayString(value);
            if (values == null){
                Logger.getLogger(J2KFunctions.class.getName()).log(Level.SEVERE, "Not an array: " + value);
            }
            boolean doubles[] = new boolean[values.length];
            int j=0;
            for (String subString : values) {
                doubles[j++] = Boolean.parseBoolean(subString);
            }
            Attribute.BooleanArray array = DefaultDataFactory.getDataFactory().createBooleanArray();
            array.setValue(doubles);
            e.setObject(name, array);
        } else if (type.equals(Attribute.Double.class)) {
            e.setDouble(name, Double.parseDouble(value));
        } else if (type.equals(Attribute.DoubleArray.class)) {
            String values[] = breakArrayString(value);
            if (values == null){
                Logger.getLogger(J2KFunctions.class.getName()).log(Level.SEVERE, "Not an array: " + value);
            }
            double doubles[] = new double[values.length];
            int j=0;
            for (String subString : values) {
                doubles[j++] = Double.parseDouble(subString);
            }
            Attribute.DoubleArray array = DefaultDataFactory.getDataFactory().createDoubleArray();
            array.setValue(doubles);
            e.setObject(name, array);
        } else if (type.equals(Attribute.Integer.class)) {
            e.setInt(name, Integer.parseInt(value));
        } else if (type.equals(Attribute.IntegerArray.class)) {
            String values[] = breakArrayString(value);
            if (values == null){
                Logger.getLogger(J2KFunctions.class.getName()).log(Level.SEVERE, "Not an array: " + value);
            }
            int doubles[] = new int[values.length];
            int j=0;
            for (String subString : values) {
                doubles[j++] = Integer.parseInt(subString);
            }
            Attribute.IntegerArray array = DefaultDataFactory.getDataFactory().createIntegerArray();
            array.setValue(doubles);
            e.setObject(name, array);
        } else if (type.equals(Attribute.Float.class)) {
            e.setFloat(name, Float.parseFloat(value));
        } else if (type.equals(Attribute.FloatArray.class)) {
            String values[] = breakArrayString(value);
            if (values == null){
                Logger.getLogger(J2KFunctions.class.getName()).log(Level.SEVERE, "Not an array: " + value);
            }
            float doubles[] = new float[values.length];
            int j=0;
            for (String subString : values) {
                doubles[j++] = Float.parseFloat(subString);
            }
            Attribute.FloatArray array = DefaultDataFactory.getDataFactory().createFloatArray();
            array.setValue(doubles);
            e.setObject(name, array);
        } else if (type.equals(Attribute.Calendar.class)) {            
            Attribute.Calendar calendar = DefaultDataFactory.getDataFactory().createCalendar();
            calendar.setValue(value);
            e.setObject(name, calendar);
        } else if (type.equals(Attribute.TimeInterval.class)) {            
            Attribute.TimeInterval interval = DefaultDataFactory.getDataFactory().createTimeInterval();
            interval.setValue(value);
            e.setObject(name, interval);
        } else if (type.equals(Attribute.StringArray.class)) {
            String values[] = breakArrayString(value);
            if (values == null){
                Logger.getLogger(J2KFunctions.class.getName()).log(Level.SEVERE, "Not an array: " + value);
            }
            String doubles[] = new String[values.length];
            int j=0;
            for (String subString : values) {
                doubles[j++] = subString;
            }
            Attribute.StringArray array = DefaultDataFactory.getDataFactory().createStringArray();
            array.setValue(doubles);
            e.setObject(name, array);
        } else if (type.equals(Attribute.String.class)) {
            e.setObject(name, value);
        }
    }
    
    public static ArrayList<Attribute.Entity> readParas(String fileName, Model model) {

        BufferedReader reader;
        ArrayList<Attribute.Entity> entityList = new ArrayList<Attribute.Entity>();
        StringTokenizer tokenizer;

        try {

            reader = new BufferedReader(new FileReader(fileName));

            String s = "#";
            boolean typedFile = false;
            
            // get rid of comments
            while (s.startsWith("#")) {
                s = reader.readLine();
                if (s.equalsIgnoreCase("#TYPED")){
                    typedFile = true;
                }
            }

            //put the attribure names into a vector
            Vector<String> attributeNames = new Vector<String>();
            Vector<Class> attributeTypes = new Vector<Class>();
            tokenizer = new StringTokenizer(s, "\t");
            while (tokenizer.hasMoreTokens()) {
                String aName = tokenizer.nextToken();

                // this is just a bloody workaround for old J2000 reach parameter files
                if (aName.equals("to-reach")) {
                    aName = "to_reach";
                }
                attributeNames.add(aName);
            }
            //process types
            if (typedFile) {
                s = reader.readLine();
                tokenizer = new StringTokenizer(s, "\t");
                while (tokenizer.hasMoreTokens()) {
                    String aType = tokenizer.nextToken();
                    if (aType.compareTo("Long")==0){
                        attributeTypes.add(Attribute.Long.class);
                    }else if (aType.compareTo("LongArray")==0){
                        attributeTypes.add(Attribute.LongArray.class);
                    }else if (aType.compareTo("Boolean")==0){
                        attributeTypes.add(Attribute.Boolean.class);
                    }else if (aType.compareTo("BooleanArray")==0){
                        attributeTypes.add(Attribute.BooleanArray.class);
                    }else if (aType.compareTo("Double")==0){
                        attributeTypes.add(Attribute.Double.class);
                    }else if (aType.compareTo("DoubleArray")==0){
                        attributeTypes.add(Attribute.DoubleArray.class);
                    }else if (aType.compareTo("Integer")==0){
                        attributeTypes.add(Attribute.Integer.class);
                    }else if (aType.compareTo("IntegerArray")==0){
                        attributeTypes.add(Attribute.IntegerArray.class);
                    }else if (aType.compareTo("Float")==0){
                        attributeTypes.add(Attribute.Float.class);
                    }else if (aType.compareTo("FloatArray")==0){
                        attributeTypes.add(Attribute.FloatArray.class);
                    }else if (aType.compareTo("Calendar")==0){
                        attributeTypes.add(Attribute.Calendar.class);
                    }else if (aType.compareTo("TimeInterval")==0){
                        attributeTypes.add(Attribute.TimeInterval.class);
                    }else if (aType.compareTo("StringArray")==0){
                        attributeTypes.add(Attribute.StringArray.class);
                    }else if (aType.compareTo("String")==0){
                        attributeTypes.add(Attribute.String.class);
                    }else{
                        Logger.getLogger(J2KFunctions.class.getName()).log(Level.SEVERE, "Unknown type: " + aType);
                    }
                }
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

                Attribute.Entity e = DefaultDataFactory.getDataFactory().createEntity();
                String string[] = s.split("\t");
                //tokenizer = new StringTokenizer(s, "\t");

                for (int i = 0; i < attributeNames.size(); i++) {
                    if (typedFile) {
                        setTypedValue(e, attributeNames.get(i), string[i], attributeTypes.get(i));
                    } else {
                        try {
                            //hopefully these are double values :-)                                                
                            e.setDouble(attributeNames.get(i), Double.parseDouble(string[i]));
                        } catch (NumberFormatException nfe) {
                            if (string[i].startsWith("[") && string[i].endsWith("]")) {
                                string[i] = string[i].substring(1, string[i].length() - 1);
                                int j = 0;
                                String subStrings[] = string[i].split(",");
                                Attribute.DoubleArray array = DefaultDataFactory.getDataFactory().createDoubleArray();
                                double doubles[] = new double[subStrings.length];
                                for (String subString : subStrings) {
                                    doubles[j++] = Double.parseDouble(subString);
                                }
                                array.setValue(doubles);
                                e.setObject(attributeNames.get(i), array);
                            } else {
                                //most probably this happens because of string values within J2K parameter files
                                Attribute.String stringValue = DefaultDataFactory.getDataFactory().createString();
                                stringValue.setValue(string[i]);
                                e.setObject(attributeNames.get(i), stringValue);
                            }
                        } catch (ArrayIndexOutOfBoundsException ex) {
                            Logger.getLogger(J2KFunctions.class.getName()).log(Level.SEVERE, "Too few columns in row \"" + s + "\"!", ex);
                        }

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
