/*
 * JAMSClassLoader.java
 * Created on 4. Juli 2005, 15:47
 *
 * This file is part of JAMS
 * Copyright (C) 2005 FSU Jena
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
package jams.runtime;

import java.util.*;
import java.net.*;
import java.io.*;
import jams.JAMS;

public class JAMSClassLoader extends URLClassLoader {

    private URL[] urls;

    public JAMSClassLoader(URL[] urls) {
        super(urls, ClassLoader.getSystemClassLoader());
        this.urls = urls;
    }

    public JAMSClassLoader(URL[] urls, ClassLoader parent) {
        super(urls, parent);
        this.urls = urls;
    }

    @Override
    public Class<?> loadClass(String name) throws ClassNotFoundException {
        Class<?> clazz = loadClass(name, true);
        return clazz;
    }

    public Class load(String name, byte[] data) {
        return defineClass(name, data, 0, data.length);
    }

    private static void addFile(Set<URL> urls, File f, JAMSLog log) {
        try {
            URL url = f.toURI().toURL();
            if (!urls.add(url)) {
                log.println(JAMS.i18n("WARNING_:_The_file_") + f.getAbsolutePath() + JAMS.i18n("_is_already_loaded"));
            }
        } catch (MalformedURLException murle) {
            log.println(JAMS.i18n("WARNING_:_The_file_") + f.getAbsolutePath() + JAMS.i18n("_could_not_be_converted_to_URL."));
        }
    }

    public static ClassLoader createClassLoader(String[] libs, JAMSLog log) {
        Set<URL> urls = new HashSet<URL>();
        for (String lib : libs) {

            File dir = new File(lib);

            if (!dir.exists()) {
                log.println(JAMS.i18n("DANGER_-_directory_") + dir.getAbsolutePath() + JAMS.i18n("_does_not_exist"));
                continue;
            }

            if (dir.isDirectory()) {
                File[] files = dir.listFiles();
                for (File file : files) {
                    if (file.getName().endsWith(".jar")) {
                        addFile(urls, file, log);
                    }
                }
            } else {

                addFile(urls, dir, log);

            }
        }

        log.println(JAMS.i18n("created_class_loader_using_"));
        for (URL url : urls) {
            log.println("\t" + url.toString());
        }


        URL[] urlArray = urls.toArray(new URL[urls.size()]);

        JAMSClassLoader cl = new JAMSClassLoader(urlArray);
        return cl;
    }
}
