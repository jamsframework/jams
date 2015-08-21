/*
 * ResourceBundleChecker.java
 * Created on 19. November 2009, 11:04
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
package jams;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.String;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.StringTokenizer;
import java.util.TreeMap;
import java.util.TreeSet;

/**
 *
 * @author Sven Kralisch <sven.kralisch at uni-jena.de>
 */
public class ResourceBundleChecker {

    private static ArrayList<String> getKeySet(String res) throws IOException {

        BufferedReader br = new BufferedReader(new InputStreamReader(ClassLoader.getSystemResourceAsStream(res)));

        String line;
        ArrayList<String> resSet = new ArrayList<String>();

        while ((line = br.readLine()) != null) {

            StringTokenizer tok = new StringTokenizer(line, "=");
            if (tok.countTokens() >= 2) {
                resSet.add(tok.nextToken());
            }
        }

        return resSet;
    }

    private static void compareBundles(String res1, String res2) throws IOException {

        System.out.println(">>>>>>>>>>>>>>>> Comparing " + res1 + " with " + res2);

        ArrayList<String> resKeys1 = getKeySet(res1);
        ArrayList<String> resKeys2 = getKeySet(res2);

        for (String key : resKeys1) {
            // check if key is existing
            if (!resKeys2.contains(key)) {
                System.out.println("Missing key in " + res2 + ": " + key);
            }
        }
    }

    private static void check4Duplicates(String res) throws IOException {

        ArrayList<String> resKeys = getKeySet(res);

        HashSet<String> set = new HashSet<String>();
        for (String key : resKeys) {
            // check if key is existing
            if (!set.contains(key)) {
                set.add(key);
            } else {
                System.out.println("Duplicate key in " + res + ": " + key);
            }
        }
    }

    private static ArrayList<String> check4UnusedKeys(String res, File sourcePaths[]) throws IOException {
        ArrayList<String> resKeys = getKeySet(res);
        
        TreeSet<String> usedKeys = new TreeSet<String>();
        
        for (File f : sourcePaths){
            if (!f.exists()){
                System.out.println("Path is non-existing: " + f.getAbsolutePath());
            }
            usedKeys.addAll(collectUsedKeys(resKeys,f));
        }
        
        resKeys.removeAll(usedKeys);
        for (String key : resKeys){
            System.out.println("Unused key in " + res + ": " + key);
        }
        
        return resKeys;
    }
    
    private static void cleanUp(String res, File sourcePaths[]) throws IOException {
        BufferedReader br = new BufferedReader(new InputStreamReader(ClassLoader.getSystemResourceAsStream(res)));

        String line;
        TreeMap<String,String> resSet = new TreeMap<String,String>();

        while ((line = br.readLine()) != null) {

            StringTokenizer tok = new StringTokenizer(line, "=");
            if (tok.countTokens() >= 2) {
                String key = tok.nextToken();
                String value = tok.nextToken();
                if (resSet.containsKey(key)){
                    if (resSet.get(key).compareTo(value) != 0){
                        System.out.println("Clean up failed in: " + res + " conflict for key " + key + "\n value1: " + value + "\n value2: " + resSet.get(key));
                    }
                }
                resSet.put(key,value);
            }
        }
        br.close();
        
        ArrayList<String> unusedKeys = check4UnusedKeys(res,sourcePaths);
        for (String key : unusedKeys){
            resSet.remove(key);
        }

        BufferedWriter writer = new BufferedWriter(new FileWriter(res.replace("/", ".") +".cleanup"));
        for (String s : resSet.keySet()){
            writer.write(s + "=" + resSet.get(s) + "\n");
        }
        writer.close();
    }
    
    private static TreeSet<String> collectUsedKeys(ArrayList<String> resKeys, File sourcePath) throws IOException {        
        TreeSet<String> usedKeys = new TreeSet<String>();
        
        for (File f : sourcePath.listFiles()){
            if (f.isDirectory()){
                usedKeys.addAll(collectUsedKeys(resKeys, f));
            }
            if (f.isFile() && f.getName().endsWith(".java")){
                BufferedReader reader = new BufferedReader(new FileReader(f));
                String line = null;
                while ((line = reader.readLine()) != null) {
                    if (line.contains("\"")) {
                        for (String key : resKeys) {
                            //line = line.replace("\\", "");
                            String key2 = key.replace("\\", "");
                            if (line.contains("\"" + key2 + "\")")) {
                                usedKeys.add(key);
                            }
                        }
                    }
                }
                reader.close();
            }
        }
        return usedKeys;
    }
            
    
    public static void main(String[] args) throws IOException {

        File sourcePaths[] = {
            new File("../../"),            
        };
        System.out.println("For JAMS sources using path: " + sourcePaths[0].getAbsolutePath());
        
        compareBundles("resources/i18n/JAMSBundle.properties", "resources/i18n/JAMSBundle_pt.properties");
        compareBundles("resources/i18n/JAMSBundle_pt.properties", "resources/i18n/JAMSBundle.properties");
        compareBundles("resources/i18n/JAMSBundle.properties", "resources/i18n/JAMSBundle_de.properties");
        compareBundles("resources/i18n/JAMSBundle_de.properties", "resources/i18n/JAMSBundle.properties");
        check4Duplicates("resources/i18n/JAMSBundle.properties");
        check4Duplicates("resources/i18n/JAMSBundle_pt.properties");
        check4Duplicates("resources/i18n/JAMSBundle_de.properties");
        /*check4UnusedKeys("resources/i18n/JAMSBundle_pt.properties", sourcePaths);  
        check4UnusedKeys("resources/i18n/JAMSBundle.properties", sourcePaths);
        check4UnusedKeys("resources/i18n/JAMSBundle_de.properties", sourcePaths);*/
        
        cleanUp("resources/i18n/JAMSBundle.properties", sourcePaths);
        cleanUp("resources/i18n/JAMSBundle_de.properties", sourcePaths);
        cleanUp("resources/i18n/JAMSBundle_pt.properties", sourcePaths);
        cleanUp("resources/i18n/JAMSBundle_vn.properties", sourcePaths);
    }
}
