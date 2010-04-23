/*
 * FileTools.java
 * Created on 13. Februar 2010, 15:17
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
package jams.tools;

import jams.JAMS;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.nio.channels.FileChannel;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Vector;

/**
 *
 * @author Sven Kralisch <sven.kralisch at uni-jena.de>
 */
public class FileTools {

    public static void copyFile(String inFile, String outFile) throws IOException {

        FileChannel inChannel = new FileInputStream(new File(inFile)).getChannel();
        FileChannel outChannel = new FileOutputStream(new File(outFile)).getChannel();
        try {
            inChannel.transferTo(0, inChannel.size(), outChannel);
        } catch (IOException e) {
            throw e;
        } finally {
            if (inChannel != null) {
                inChannel.close();
            }
            if (outChannel != null) {
                outChannel.close();
            }
        }
    }

    /**
     * delete all given files
     * @param theFiles
     */
    public static void deleteFiles(File[] theFiles) {
        for (File theFile : theFiles) {
            theFile.delete();
        }
    }

    /**
     * get array of files
     *
     * @param directoryName
     * @param fileExtension
     * @return filearray
     * @throws IOException
     */
    public static File[] getFiles(String directoryName, String fileExtension) throws IOException {
        //check for existing of the requested directory
        File directory = assertDirectory(directoryName);
        if (!directory.isDirectory()) {
            throw new IOException("Can't load filelist because directory '" + directoryName + "' not found.");
        }
        return getFiles(directory, fileExtension);
    }

    /**
     * get array of files
     *
     * @param directory
     * @param fileExtension
     * @return filearray
     * @throws IOException
     */
    public static File[] getFiles(File directory, String fileExtension) {
        //write all files within the given directory in the File-Array
        String ext = null;
        if (fileExtension != null) {
            ext = fileExtension.toLowerCase();
        }
        final String fileExtensionLower = ext;
        File[] fileArray;
        fileArray = directory.listFiles(new FilenameFilter() {

            public boolean accept(File dir, String name) {
                String nameLower = name.toLowerCase();
                if (fileExtensionLower == null || nameLower.endsWith("." + fileExtensionLower)) {
                    return true;
                } else {
                    return false;
                }
            }
        });

        // remove directories
        Vector<File> files = new Vector<File>();
        for (int i = 0; i < fileArray.length; i++) {
            File file = fileArray[i];
            if (!file.isDirectory()) {
                files.add(file);
            }
        }
        fileArray = new File[files.size()];
        for (int i = 0; i < files.size(); i++) {
            File file = files.elementAt(i);
            fileArray[i] = file;
        }

        Arrays.sort(fileArray, new Comparator<File>() {

            public int compare(File o1, File o2) {
                String fileName1 = o1.getName();
                String fileName2 = o1.getName();
                return fileName1.compareToIgnoreCase(fileName2);
            }
        });
        return fileArray;
    }

    /**
     * Asserts that the given directory is existing
     *
     * @param dirName the full directory name
     * @throws IOException
     */
    public static File assertDirectory(String dirName)
            throws IOException {
        File dir = new File(dirName);
        if (dir.isDirectory() && dir.exists()) {
            return dir;
        }
        dir.mkdirs();
        if (!(dir.isDirectory() && dir.exists())) {
            throw new IOException("Could not create directory '" + dirName + "' !");
        }
        return dir;
    }

    public static void stringToFile(String fileName, String string) throws IOException {

        String newString = new String(string.getBytes(JAMS.charset));

        BufferedWriter writer = new BufferedWriter(new FileWriter(fileName));
        writer.write(string);
        writer.close();
    }

    /**
     * Reads a file and returns its content as string
     * @param fileName The name of the file
     * @return The file content
     */
    public static String fileToString(String fileName) throws IOException {

        String result = "";

        FileInputStream in = new FileInputStream(fileName);
        result = streamToString(in);

        return result;
    }

    /**
     * Reads from a stream and returns its content as string
     * @param in The stream
     * @return The stream content
     */
    public static String streamToString(InputStream in) throws IOException {
        String content = "";

        byte[] buffer = new byte[in.available()];
        in.read(buffer);

        content = new String(buffer, JAMS.charset);

        return content;
    }

    /**
     * Creates an abolute path from dir and file name. If dir name is null, user.dir is used instead.
     * @param dirName The dir name
     * @param fileName The file name
     * @return A fully qualified name of the file.
     */
    public static String createAbsoluteFileName(String dirName, String fileName) {
        //if relative path is provided, make absolute path!
        if (dirName.isEmpty()) {
            dirName = System.getProperty("user.dir");
        }
        //in case directory is not terminated with slash, add slash
        File file = new File(dirName, fileName);

        String canonicalPath;
        try {
            canonicalPath = file.getCanonicalPath();
        } catch (IOException ex) {
            canonicalPath = file.getAbsolutePath();
        }

        return canonicalPath;
    }

    /**
     * replace string in file
     * @param fileName
     * @param findString
     * @param replaceString
     * @return true, if findString could be replaced
     * @throws IOException
     */
    public static boolean replaceWithinFile(String fileName, String findString, String replaceString)
            throws IOException {
        String theFileString = fileToString(fileName);
        if (theFileString.indexOf(findString) > -1) {
            theFileString = theFileString.replaceAll(findString, replaceString);
            stringToFile(fileName, theFileString);
            return true;
        }
        return false;
    }
}
