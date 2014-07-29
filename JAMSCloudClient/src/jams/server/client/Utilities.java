/*
 * WorkspaceHandler.java
 * Created on 23.04.2014, 13:31:28
 *
 * This file is part of JAMS
 * Copyright (C) FSU Jena
 *
 * JAMS is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public License
 * as published by the Free Software Foundation; either version 3
 * of the License, or (at your option) any later version.
 *
 * JAMS is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with JAMS. If not, see <http://www.gnu.org/licenses/>.
 *
 */

package jams.server.client;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.jar.Attributes;
import java.util.jar.JarFile;
import java.util.jar.Manifest;

/**
 *
 * @author christian
 */
public class Utilities {                
    public static String formatSize(long value){
        if (value < 1000){
            return Long.toString(value);
        }else if (value < 1E6){
            Double d = (double)value / 1024.;
            return String.format("%.2f kb", d);
        }else if (value < 1E9){
            Double d = (double)value / (1024*1024.);
            return String.format("%.2f mb", d);
        }else if (value < 1E12){
            Double d = (double)value / (1024*1024*1024.);
            return String.format("%.2f gb", d);
        }else if (value < 1E15){
            Double d = (double)value / (1024*1024*1024*1024.);
            return String.format("%.2f pb", d);
        }else{
            return "really really big";
        }
    }
    
    public static File[] getClassPathManifest(File f) throws IOException {
        JarFile jar = new JarFile(f);
        Manifest mf = jar.getManifest();
        Attributes a = mf.getMainAttributes();
        if (a == null) {
            return null;
        }
        String classPath = a.getValue(Attributes.Name.CLASS_PATH);
        if (classPath == null) {
            return null;
        }
        String paths[] = classPath.split(" ");
        File files[] = new File[paths.length];
        for (int i = 0; i < paths.length; i++) {
            files[i] = new File(f.getParent(), paths[i]);
        }
        return files;
    }
    
    public static File[] buildFullClassPath(File f) {

        ArrayList<File> doneList = new ArrayList<>();
        ArrayList<File> todoList = new ArrayList<>();
        todoList.add(f);
        while (!todoList.isEmpty()) {
            File nextFile = todoList.remove(todoList.size() - 1);
            if (doneList.contains(nextFile)) {
                continue;
            }
            if (!nextFile.exists()) {
                continue;
            }
            if (nextFile.isDirectory()) {
                //TODO!!!
            }
            try {
                File files[] = Utilities.getClassPathManifest(nextFile);
                if (files != null) {
                    todoList.addAll(Arrays.asList(files));
                }
            } catch (IOException ioe) {
                ioe.printStackTrace();
            }
            doneList.add(nextFile);
        }
        return doneList.toArray(new File[0]);
    }
}
