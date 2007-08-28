/*
 * ClassManager.java
 * Created on 15. September 2006, 23:03
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

package org.unijena.jams.runtime;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.HashSet;
import java.util.Set;
import org.unijena.jams.JAMS;

/**
 *
 * @author S. Kralisch
 */
public class ClassManager {
    
    private static final Class[] parameters = new Class[]{URL.class};
    private static Set<URL> urls = new HashSet<URL>();    
    
    public static void addFile(String s, JAMSRuntime rt) throws IOException {
        File f = new File(s);
        addFile(f, rt);
    }
    
    public static void addFile(File f, JAMSRuntime rt) throws IOException, MalformedURLException {
        addURL(f.toURI().toURL(), rt);
    }
    
    //this is nasty, but works pretty good :]
    public static void addURL(URL u, JAMSRuntime rt) throws IOException {
        
        if (urls.contains(u))
            return;
        
        urls.add(u);
        
        URLClassLoader sysloader = (URLClassLoader)ClassLoader.getSystemClassLoader();
        Class<?> sysclass = URLClassLoader.class;
        
        try {
            Method method = sysclass.getDeclaredMethod("addURL",parameters);
            method.setAccessible(true);
            method.invoke(sysloader,new Object[]{ u });
        } catch (Throwable t) {
            t.printStackTrace();
            throw new IOException("Error, could not add URL to system classloader");
        }
        
    }
    
    public static void addLibs(String[] libdirs, JAMSRuntime rt) throws IOException {

        for (int i = 0; i < libdirs.length; i++) {
            File dir = new File(libdirs[i]);
            
            if (! dir.exists()) {
                rt.println("Library " + dir.getAbsolutePath() + " does not exist");
                continue;
            }
            if (dir.isDirectory()) {
                File[] f = dir.listFiles();
                for (int j = 0; j < f.length; j++) {
                    if (f[j].getName().endsWith(".jar"))
                        addFile(f[j], rt);
                }
            } else {
                addFile(dir, rt);
            }
        }
        rt.println("Created class loader using " + urls, JAMS.STANDARD);
    }
    
}
