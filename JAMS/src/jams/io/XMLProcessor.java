/*
 * XMLProcessor.java
 * Created on 20. März 2006, 09:02
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

package jams.io;

import java.io.*;

/**
 *
 * @author S. Kralisch
 */
public class XMLProcessor {
    
    static String[] regex = {"jamsvar class=\".*\" name", "compoundcomponent", "jamsvar", "providervar", "spatattrib", "provider=", "org\\.unijena\\.jams\\.gui", "org\\.unijena\\.jamscomponents", "org\\.unijena\\.jams\\."};
    static String[] replace = {"jamsvar name", "contextcomponent", "var", "cvar", "attribute", "context=", "jams.components.gui", "jams.components", "jams."};
/*    static String[] regex = {"jamsvar class=\".*\" "};
    static String[] replace = {"jamsvar "};
 */
    
    /** Creates a new instance of XMLProcessor */
    public XMLProcessor() {
    }
    
    public static String modelDocConverter(String inFileName) {
        
        String outFileName = inFileName;
        
        try {
            
            BufferedReader reader = new BufferedReader(new FileReader(inFileName));
            
            String s, newDoc = "", oldDoc;
            
            while ((s = reader.readLine()) != null) {
                newDoc += s + "\n";
            }
            newDoc = newDoc.substring(0, newDoc.length()-1);
            oldDoc = newDoc;
            
            for (int i = 0; i < regex.length; i++) {
                newDoc = newDoc.replaceAll(regex[i], replace[i]);
            }
            
            if (!newDoc.contentEquals(oldDoc)) {
                File f = new File(inFileName);
                String fName = "_" + f.getCanonicalFile().getName();
                String pName = f.getParent();
                
                outFileName = pName + File.separator + fName;
                
                BufferedWriter writer = new BufferedWriter(new FileWriter(outFileName));
                writer.write(newDoc);
                writer.close();
            }
            
            
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
        
        return outFileName;
    }
    
//        String msg = "The model definition in \"" + inFileName + "\" has been adapted in order to meet modifications of the JAMS model DTD.\nThe new definition has been stored in \"" + outFileName + "\" while your original file was left untouched.";
    
}
