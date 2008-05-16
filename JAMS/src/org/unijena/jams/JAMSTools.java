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

package org.unijena.jams;

import java.io.FileInputStream;
import java.util.StringTokenizer;

/**
 *
 * @author Sven Kralisch
 */
public class JAMSTools {
    
    
    public static String[] toArray(String str) {
        return toArray(str, null);
    }
    
    public static String[] toArray(String str, String delim) {
        
        if (str == null)
            return null;
        
        StringTokenizer tok;
        
        if (delim == null)
            tok = new StringTokenizer(str);
        else
            tok = new StringTokenizer(str, delim);
        
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
            FileInputStream file = new FileInputStream(fileName);
            byte[] b = new byte[file.available()];
            file.read(b);
            file.close();
            result = new String(b);
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        return result;
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
        if ( theString == null ||
             theString.length() == 0 ||
             theString.trim().length() == 0)
            return true;
        else
            return false;
    }
}
