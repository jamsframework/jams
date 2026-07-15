/*
 * FileTools.java
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

import jams.JAMS;
import java.beans.ExceptionListener;
import java.beans.XMLDecoder;
import java.beans.XMLEncoder;
import jams.meta.ModelDescriptor;
import jams.meta.ModelIO;
import java.io.*;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.logging.Filter;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.ValidationEvent;
import javax.xml.bind.ValidationEventHandler;
import org.w3c.dom.Document;

/**
 *
 * @author Sven Kralisch <sven.kralisch at uni-jena.de>
 */
public class FileTools {

    /**
     * delete all given files
     *
     * @param theFiles
     */
    public static void deleteFiles(File[] theFiles) {
        for (File theFile : theFiles) {
            theFile.delete();
        }
    }

    /**
     * delete all given files
     *
     * @param theFile
     */
    public static void deleteRecursive(File theFile) {
        if (theFile.isDirectory()) {
            for (File f : theFile.listFiles()) {
                deleteRecursive(f);
            }
        }
        Logger log = Logger.getLogger(FileTools.class.getName());
        log.setLevel(Level.ALL);
        log.log(Level.INFO, "Deleted the file during cleanUp {0}", theFile.getAbsolutePath());
        theFile.delete();
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
        ArrayList<File> files = new ArrayList<File>();
        for (int i = 0; i < fileArray.length; i++) {
            File file = fileArray[i];
            if (!file.isDirectory()) {
                files.add(file);
            }
        }
        fileArray = new File[files.size()];
        for (int i = 0; i < files.size(); i++) {
            File file = files.get(i);
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
     * get array of files
     *
     * @param directory
     * @param regex
     * @param isPostiveRegEx
     * @return filearray
     */
    public static Collection<File> getFilesByRegEx(File directory, String regex, boolean isPostiveRegEx) {
        ArrayList<File> list = new ArrayList<File>();
        if (directory.isFile()) {
            String path = normalizePath(directory.getPath());
            if (regex == null
                    || regex.isEmpty()
                    || (isPostiveRegEx && path.toLowerCase().matches(regex))
                    || (!isPostiveRegEx && !path.toLowerCase().matches(regex))) {
                list.add(directory);
            }
            return list;
        }

        for (File f : directory.listFiles()) {
            String path = normalizePath(f.getPath());
            if (f.isDirectory()) {
                list.addAll(getFilesByRegEx(f, regex, isPostiveRegEx));
            } else if (regex == null
                    || regex.isEmpty()
                    || (isPostiveRegEx && path.toLowerCase().matches(regex))
                    || (!isPostiveRegEx && !path.toLowerCase().matches(regex))) {
                list.add(f);
            }
        }
        return list;
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

        //BufferedWriter writer = new BufferedWriter(new FileWriter(fileName));
        BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(new File(fileName)), JAMS.getCharset()));
        writer.write(string);
        writer.close();
    }

    /**
     * Reads a file and returns its content as string
     *
     * @param fileName The name of the file
     * @param encoding
     * @return The file content
     */
    public static String fileToString(String fileName, String encoding) throws IOException {

        String result = "";

        FileInputStream in = new FileInputStream(fileName);
        result = streamToString(in, encoding);
        in.close();
        return result;
    }

    /**
     * Reads a file and returns its content as string
     *
     * @param fileName The name of the file
     * @return The file content
     */
    public static String fileToString(String fileName) throws IOException {

        String result = "";

        FileInputStream in = new FileInputStream(fileName);
        result = streamToString(in);
        in.close();
        return result;
    }

    /**
     * Reads from a stream and returns its content as string
     *
     * @param in The stream
     * @return The stream content
     * @throws java.io.IOException
     */
    public static String streamToString(InputStream in) throws IOException {
        return streamToString(in, JAMS.getCharset());
    }

    /**
     * Reads from a stream and returns its content as string
     *
     * @param in The stream
     * @param encoding
     * @return The stream content
     * @throws java.io.IOException
     */
    public static String streamToString(InputStream in, String encoding) throws IOException {
        String content = "";

        byte[] buffer = new byte[in.available()];
        in.read(buffer);

        content = new String(buffer, encoding);

        return content;
    }

    /**
     * Reads from a stream and returns its content as string
     *
     * @param in The stream
     * @param offset number of bytes to be skipped in the beginning
     * @param size total size to be read
     * @return The stream content
     * @throws java.io.IOException
     */
    public static String streamToString(InputStream in, int offset, int size) throws IOException {
        String t = "";
        in.skip(offset);
        byte buffer[] = new byte[16384];
        int nread = 0;
        while ((nread = in.read(buffer, 0, Math.min(buffer.length, size))) > 0 && size > 0) {
            t += new String(buffer);
            size -= nread;
        }
        return t;
    }

    /**
     * Reads from a stream and returns its content as string
     *
     * @param target target file
     * @param in The stream
     * @throws java.io.IOException
     */
    public static void streamToFile(File target, InputStream in) throws IOException {
        FileOutputStream writer = new FileOutputStream(target);
        byte[] buffer = new byte[65535];
        int fread = 0;
        while ((fread = in.read(buffer)) > 0) {
            writer.write(buffer, 0, fread);
        }
        writer.flush();
        writer.close();
    }

    /**
     * Creates an abolute path from dir and file name. If dir name is null,
     * user.dir is used instead.
     *
     * @param dirName The dir name
     * @param fileName The file name
     * @return A fully qualified name of the file.
     */
    public static String createAbsoluteFileName(String dirName, String fileName) {
        //if relative path is provided, make absolute path!
        if (dirName.isEmpty()) {
            dirName = JAMS.getBaseDir().getAbsolutePath();
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
     *
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

    /**
     * zip a file to a zipoutputstream
     *
     * @param file file to be zipped
     * @param fileName name of file to be used
     * @param zipOut resulting outputstrwam
     * @throws IOException, ZipException
     */
    public static void zipFile(java.io.File file, String fileName, ZipOutputStream zipOut) throws IOException {
        FileInputStream inFile = new FileInputStream(file);
        zipOut.putNextEntry(new ZipEntry(fileName));

        byte[] buf = new byte[65536];
        int len;
        // Der Inhalt der Datei wird in die Zip-Datei kopiert.
        while ((len = inFile.read(buf)) > 0) {
            zipOut.write(buf, 0, len);
        }
        zipOut.closeEntry();
        inFile.close();
    }

    /**
     * buildDirectoryHierarchyFor .. creates directories which are necessary to
     * unzip a file
     *
     * @param entryName name of entry in zip
     * @param File destDir
     * @throws IOException, ZipException
     */
    static private File buildDirectoryHierarchyFor(String entryName, File destDir) {
        entryName = entryName.replace("\\", "/");
        int lastIndex = entryName.lastIndexOf('/');
        //String entryFileName = entryName.substring(lastIndex + 1);
        String internalPathToEntry = entryName.substring(0, lastIndex + 1);
        //System.out.println("extract to_:" + internalPathToEntry + " file:" + entryFileName);
        return new File(destDir, internalPathToEntry);
    }

    /**
     * unzip a file to a directory
     *
     * @param zip zipped file
     * @param destDir destination
     * @param deleteZip
     * @throws IOException, ZipException
     */
    public static void unzipFile(File zip, File destDir, boolean deleteZip) throws IOException {
        if (!destDir.exists()) {
            destDir.mkdir();
        }

        ZipInputStream zipFile = new ZipInputStream(new FileInputStream(zip));
        byte[] buffer = new byte[16384];
        int len;
        ZipEntry entry;
        while ((entry = zipFile.getNextEntry()) != null) {
            String entryFileName = entry.getName();
            entryFileName = entryFileName.replace("\\", "/");
            File dir = buildDirectoryHierarchyFor(entryFileName, destDir);
            if (!dir.exists()) {
                dir.mkdirs();
            }
            if (!entry.isDirectory()) {
                File f = new File(destDir, entryFileName);
                if (f.isDirectory()) {
                    continue;
                }
                
                try {
                BufferedOutputStream bos = new BufferedOutputStream(
                        new FileOutputStream(f));

                while ((len = zipFile.read(buffer)) > 0) {
                    bos.write(buffer, 0, len);
                }

                bos.flush();
                bos.close();
                } catch (IOException ioe) {
                    System.out.println("sht");
                }
            }
        }
        zipFile.close();
        if (deleteZip) {
            zip.delete();
        }
    }

    /**
     * getDirectorySize
     *
     * @param directory
     * @return
     */
    static public long getDirectorySize(java.io.File directory) {
        long length = 0;
        if (directory==null || directory.listFiles() == null)
            return 0;
        for (java.io.File file : directory.listFiles()) {
            if (file.isFile()) {
                length += file.length();
            } else {
                length += getDirectorySize(file);
            }
        }
        return length;
    }

    /**
     * normalizePath
     *
     * @param s
     * @return
     */
    public static String normalizePath(String s) {
        removeSlashes(s);
        s = s.replace("\\", "/").replace("//", "/");
        return s;
    }

    /**
     * getParent from File's string representation
     *
     * @param s - Path to a file, it is not necessary that the file exists or
     * that the path is valid in current file system
     * @return parent of file
     */
    public static String getParent(String s) {
        s = normalizePath(s);
        int index = s.lastIndexOf("/");
        if (index != -1) {
            return s.substring(0, index);
        } else {
            return "";
        }
    }

    /**
     * removeSlashes
     *
     * @param s
     * @return
     */
    private static String removeSlashes(String s) {
        while (s.startsWith("/") || s.startsWith("\\")) {
            s = s.substring(1);
        }
        return s;
    }

    public static <T> void serializeObjectToXMLwithJAXB(T o, File file) throws Exception {
        FileWriter writer = new FileWriter(file);
        JAXBContext context = JAXBContext.newInstance(o.getClass());
        Marshaller m = context.createMarshaller();
        m.setEventHandler(new ValidationEventHandler() {

            @Override
            public boolean handleEvent(ValidationEvent event) {
                System.out.println(event.getMessage());
                return false;
            }
        });
        m.marshal(o, writer);
        writer.close();
    }

    public static <T> T deserializeObjectFromXMLwithJAXB(File file, Class<T> c) throws Exception {
        FileReader reader = new FileReader(file);
        JAXBContext context = JAXBContext.newInstance(c);
        Unmarshaller um = context.createUnmarshaller();
        um.setEventHandler(new ValidationEventHandler() {

            @Override
            public boolean handleEvent(ValidationEvent event) {
                System.out.println(event.getMessage());
                return false;
            }
        });
        T o = (T) um.unmarshal(reader);
        reader.close();
        return o;
    }

    public static <T> void serializeObjectToXML(T o, File file) throws Exception {
        XMLEncoder encoder
                = new XMLEncoder(
                        new BufferedOutputStream(
                                new FileOutputStream(file)));
        encoder.setExceptionListener(new ExceptionListener() {

            @Override
            public void exceptionThrown(Exception e) {
                e.printStackTrace();
            }
        });
        encoder.writeObject(o);
        encoder.close();
    }

    public static <T> T deserializeObjectFromXML(File file) throws Exception {
        XMLDecoder decoder
                = new XMLDecoder(new BufferedInputStream(
                                new FileInputStream(file)));
        T o = (T) decoder.readObject();
        decoder.close();
        return o;
    }
    
    public static boolean validateModelFile(File f) {
        if (f.isDirectory()) {
            return false;
        }
        if (!f.exists()) {
            return false;
        }
        if (!f.getName().endsWith(".xml") && !f.getName().endsWith(".jam")) {
            return false;
        }

        //try to load model file
        ModelIO io = ModelIO.getStandardModelIO();
        Document d = XMLTools.getDocument(f.getAbsolutePath());
        if (d == null) {
            return false;
        }
        //deactive logs
        Filter filterAll = new Filter() {
            @Override
            public boolean isLoggable(LogRecord record) {
                return false;
            }
        };
        Logger.getLogger(ModelIO.class.getName()).setFilter(filterAll);
        Logger.getLogger(jams.meta.ModelDescriptor.class.getName()).setFilter(filterAll);

        try {
            ModelDescriptor desc = io.loadModelDescriptor(d, null, true);
            if (desc == null) {
                return false;
            }
            if (desc.getModelName() == null || desc.getModelName().isEmpty()) {
                return false;
            }
        } catch (Throwable t) {
            t.printStackTrace();
            return false;
        } finally {
            //reactive logs
            Logger.getLogger(ModelIO.class.getName()).setFilter(null);
            Logger.getLogger(jams.meta.ModelDescriptor.class.getName()).setFilter(null);
        }
        return true;
    }    
}
