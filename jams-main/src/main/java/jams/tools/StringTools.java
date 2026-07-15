/*
 * StringTools.java
 * Created on 13. Februar 2010, 15:17
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
package jams.tools;

import java.text.MessageFormat;
import java.util.Locale;
import java.util.StringTokenizer;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Sven Kralisch <sven.kralisch at uni-jena.de>
 */
public class StringTools {

    /**
     *
     */
    public static final Locale STANDARD_LOCALE = Locale.US;

    /**
     * Checks if a string is empty (i.e. if its null, has length 0 or contains
     * only whitespaces
     *
     * @param theString The string to be checked
     * @return True, if theString is empty, false otherwise
     */
    public static boolean isEmptyString(String theString) {
        if (theString == null) {
            return true;
        }
        theString = theString.trim();
        if (theString.length() == 0) {
            return true;
        } else {
            return false;
        }
    }

    /**
     *
     * @param theString
     * @return
     */
    public static boolean parseToBoolean(String theString) {
        if (isEmptyString(theString)) {
            return false;
        }

        if (theString.equals("1")
                || theString.equalsIgnoreCase("true")
                || theString.equalsIgnoreCase("on")
                || theString.equalsIgnoreCase("yes")
                || theString.equalsIgnoreCase("ja")) {
            return true;
        }

        return false;
    }

    /**
     * Creates a string representation of a stack trace
     *
     * @param stea The stack trace
     * @return The stack trace string
     */
    public static String getStackTraceString(StackTraceElement[] stea) {
        String result = "";

        for (StackTraceElement ste : stea) {
            result += "        at " + ste.toString() + "\n";
        }
        return result;
    }

    /**
     * Splits a string into tokens and fills a string array with them.
     * Whitespaces will be used as delimiters, while subsequent whitespaces will
     * be regarded as one.
     *
     * @param str The string to be splitted
     * @return A string array with the tokens
     */
    public static String[] toArray(String str) {
        return toArray(str, null);
    }

    /**
     * Splits a string into tokens using {@link String#split}. If delim is null,
     * whitespaces will be used as delimiters, while subsequent whitespaces will
     * be regarded as one.
     *
     * @param str The string to be splitted
     * @param delim A delimiter defining where to split
     * @return A string array with the tokens
     */
    public static String[] toArray(String str, String delim) {

        if (str == null) {
            return null;
        }

        if (delim == null) {
            delim = "[\\s]+";
        }

        return str.split(delim);
    }

    /**
     * get one special part of token
     *
     * @param theToken
     * @param thePart (int)
     * @param delimiter
     * @return string
     */
    public static String getPartOfToken(String theToken, int thePart, String delimiter) {
        String result = null;
        StringTokenizer tokenizer = new StringTokenizer(theToken, delimiter);
        int i = 0;
        while (tokenizer.hasMoreTokens()) {
            result = tokenizer.nextToken();
            i++;
            if (i == thePart) {
                return result;
            } else {
                result = null;
            }
        }
        return result;
    }

    /**
     *
     * @param attribName
     * @return
     */
    public static String getGetterName(String attribName) {
        return "get" + attribName.substring(0, 1).toUpperCase(STANDARD_LOCALE) + attribName.substring(1);
    }

    /**
     *
     * @param attribName
     * @return
     */
    public static String getSetterName(String attribName) {
        return "set" + attribName.substring(0, 1).toUpperCase(STANDARD_LOCALE) + attribName.substring(1);
    }

    /**
     * checks, whether a string could be parsed to double
     *
     * @param aString
     * @return result
     */
    public static boolean isDouble(String aString) {
        boolean isDouble = false;
        try {
            Double.parseDouble(aString);
            isDouble = true;
        } catch (NumberFormatException e) {
            System.out.println("could not parse " + aString + " to double.");
        }
        return isDouble;
    }

    /**
     * applies MessageFormat.format and catches possible execptions
     *
     * @param msg
     * @param arguments
     * @return
     */
    public static String format(String msg, Object... arguments) {
        try {
            return MessageFormat.format(msg, arguments);
        } catch (Throwable t) {
            Logger.getLogger(StringTools.class.getName()).log(Level.WARNING, t.toString(), t);
            return msg;
        }
    }

    /**
     * formats a file size value into a human readable string
     *
     * @param bytes number to be formated
     * @param si yes if output should be formated as si unit
     * @return the formatted string
     */
    public static String humanReadableByteCount(long bytes, boolean si) {
        int unit = si ? 1000 : 1024;
        if (bytes < unit) {
            return bytes + " B";
        }
        int exp = (int) (Math.log(bytes) / Math.log(unit));
        String pre = (si ? "kMGTPE" : "KMGTPE").charAt(exp - 1) + (si ? "" : "i");
        return String.format("%.1f %sB", bytes / Math.pow(unit, exp), pre);
    }
}
