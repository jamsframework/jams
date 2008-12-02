/*
 * JAMSTools.java
 * Created on 2. Februar 2007, 21:57
 *
 * This file is part of JAMS
 * Copyright (C) 2006 FSU Jena
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
package jams;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.StringTokenizer;

/**
 *
 * @author Sven Kralisch
 */
public class JAMSTools {

    public static String CreateAbsoluteFileName(String dirName, String fileName){        
        //if relative path is provided, make absolute path!
        if (dirName.isEmpty()){
            dirName = System.getProperty("user.dir");
        }
        //in case directory is not terminated with slash, add slash
        dirName += File.separator;

        return dirName + fileName;
    }
    
    public static String[] toArray(String str) {
        return toArray(str, null);
    }

    public static String[] toArray(String str, String delim) {

        if (str == null) {
            return null;
        }
        StringTokenizer tok;

        if (delim == null) {
            tok = new StringTokenizer(str);
        } else {
            tok = new StringTokenizer(str, delim);
        }
        String[] result = new String[tok.countTokens()];
        int i = 0;
        while (tok.hasMoreTokens()) {
            result[i++] = tok.nextToken();
        }
        return result;
    }

    public static String fileToString(String fileName) {

        String result = "";

        try {
            FileInputStream in = new FileInputStream(fileName);
            result = streamToString(in);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return result;
    }

    public static String streamToString(InputStream in) {
        String content = "";

        try {
            byte[] buffer = new byte[in.available()];
            in.read(buffer);
            content = new String(buffer);
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }

        return content;
    }


    public static String getStackTraceString(StackTraceElement[] stea) {
        String result = "";

        for (StackTraceElement ste : stea) {
            result += "        at " + ste.toString() + "\n";
        }
        return result;
    }

    public static String[] arrayStringAsStringArray(String arrayString) {
        return toArray(arrayString, ";");
    }

    public static boolean isEmptyString(String theString) {
        if (theString == null ||
                theString.length() == 0 ||
                theString.trim().length() == 0) {
            return true;
        } else {
            return false;
        }
    }
}
