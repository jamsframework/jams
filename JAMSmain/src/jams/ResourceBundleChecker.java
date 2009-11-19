/*
 * ResourceBundleChecker.java
 * Created on 19. November 2009, 11:04
 *
 * This file is part of JAMS
 * Copyright (C) FSU Jena
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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.StringTokenizer;

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

    public static void main(String[] args) throws IOException {

        compareBundles("resources/i18n/JAMSBundle.properties", "resources/i18n/JAMSBundle_pt.properties");
        compareBundles("resources/i18n/JAMSBundle_pt.properties", "resources/i18n/JAMSBundle.properties");
        compareBundles("resources/i18n/JAMSBundle.properties", "resources/i18n/JAMSBundle_de.properties");
        compareBundles("resources/i18n/JAMSBundle_de.properties", "resources/i18n/JAMSBundle.properties");
        check4Duplicates("resources/i18n/JAMSBundle.properties");
        check4Duplicates("resources/i18n/JAMSBundle_pt.properties");
        check4Duplicates("resources/i18n/JAMSBundle_de.properties");
    }
}
