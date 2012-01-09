/*
 * ServerChild.java
 * Created on 31. Mai 2007, 14:50
 *
 * This file is part of JAMS
 * Copyright (C) 2007 FSU Jena
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

package jams.remote.server;

import jams.remote.common.ByteStream;
import jams.remote.common.Protocol;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.net.Socket;
import java.net.SocketException;
import java.util.Observable;
import java.util.Observer;
import jams.JAMSProperties;
import jams.tools.XMLTools;
import jams.io.XMLProcessor;
import jams.runtime.JAMSRuntime;
import jams.runtime.StandardRuntime;
import jams.tools.FileTools;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

/**
 *
 * @author S. Kralisch
 */
public class ServerChild {
    
    private Socket client;
    private String baseDir;
    private PrintStream infoStream, errorStream;
    private JAMSRuntime runtime;
    
    public ServerChild(Socket client, String baseDir) {
        this.client = client;
        this.baseDir = baseDir;
    }
    
    private void pushFile(String fileName) throws IOException {
        
        fileName = baseDir + fileName;
        
        if (client.isClosed()) {
            Server.writeLog("Couldn't send file \"" + fileName + "\" because client is closed!");
            return;
        }
        
        OutputStream outStream = client.getOutputStream();
        
        //provide file name
        ByteStream.toStream(outStream, fileName);
        
        int result = Protocol.ERROR;
        //put file on stream
        File file = new File(fileName);
        ByteStream.toStream(outStream, file);
        result = Protocol.SUCCESS;
        Server.writeLog("Sent " + fileName);
        
        ByteStream.toStream(outStream, result);
    }
    
    private void getFile(String fileName) throws IOException {
        
        fileName = baseDir + fileName;
        
        if (client.isClosed()) {
            Server.writeLog("Couldn't get file \"" + fileName + "\" because client is closed!");
            return;
        }
        
        InputStream inStream = client.getInputStream();
        
        int result = Protocol.ERROR;
        File file = new File(fileName);
        try {
            ByteStream.toFile(inStream, file);
            result = Protocol.SUCCESS;
            Server.writeLog("Received " + fileName);
        } catch (IOException ex) {}
        ByteStream.toStream(client.getOutputStream(), result);
    }
    
    private void createDir(String fileName) throws IOException {
        
        fileName = baseDir + fileName;
        
        if (client.isClosed()) {
            Server.writeLog("Couldn't create dir \"" + fileName + "\" because client is closed!");
            return;
        }
        
        int result = Protocol.ERROR;
        File dir= new File(fileName);
        
        if (dir.exists()) {
            result = Protocol.SUCCESS;
        } else if (dir.mkdirs()) {
            result = Protocol.SUCCESS;
            Server.writeLog("Created directory " + dir);
        }
        ByteStream.toStream(client.getOutputStream(), result);
        
    }
    
    private void cleanDir(String fileName) throws IOException {
        
        if (client.isClosed()) {
            Server.writeLog("Couldn't delete dir \"" + fileName + "\" because client is closed!");
            return;
        }
        
        int result = Protocol.ERROR;
        if (cleanDirRecursive(fileName)) {
            result = Protocol.SUCCESS;
            Server.writeLog("Cleaned directory " + fileName);
        }
        ByteStream.toStream(client.getOutputStream(), result);
        
    }
    
    private void pushListing(String fileName) throws IOException {
        
        //fileName = baseDir + fileName;
        
        if (client.isClosed()) {
            Server.writeLog("Couldn't push listing \"" + fileName + "\" because client is closed!");
            return;
        }
        
        int result = Protocol.SUCCESS;
        String dirListing = listDirs(fileName);
        String fileListing = listFiles(fileName);
        if (dirListing == null) {
            dirListing = "";
            result = Protocol.ERROR;
        }
        if (fileListing == null) {
            fileListing = "";
            result = Protocol.ERROR;
        }
        
        OutputStream outStream = client.getOutputStream();
        ByteStream.toStream(outStream, dirListing);
        ByteStream.toStream(outStream, fileListing);
        ByteStream.toStream(outStream, result);
        
        Server.writeLog("Sent listing of " + fileName);
    }
    
    private void runJAMS(String workspaceDir, String libDir, String modelFilename, String debug) throws IOException {
        
        workspaceDir = baseDir + workspaceDir;
        modelFilename = baseDir + modelFilename;
        libDir = baseDir + libDir;
        
        if (client.isClosed()) {
            Server.writeLog("Couldn't exec JAMS because client is closed!");
            return;
        }
        
        Server.writeLog("Executing JAMS: " + workspaceDir + " - " + libDir);
        
        //String modelFilename = workspaceDir + "/$model.jam";
        JAMSProperties properties = JAMSProperties.createProperties();
        properties.setProperty("verbose", "0");
        properties.setProperty("windowenable", "0");
        properties.setProperty("debug", debug);
        properties.setProperty(JAMSProperties.LIBS_IDENTIFIER, libDir);
        
        
        String info = "";
        
        // do some search and replace on the input file and create new file if necessary
        String newModelFilename = XMLProcessor.modelDocConverter(modelFilename);
        if (!newModelFilename.equalsIgnoreCase(modelFilename)) {
            info = "The model definition in \"" + modelFilename + "\" has been adapted in order to meet modifications of the JAMS model DTD.\nThe new definition has been stored in \"" + newModelFilename + "\" while your original file was left untouched.";
            modelFilename = newModelFilename;
        }
        
        String modelDocString = FileTools.fileToString(modelFilename);
        
        String[] args = null;//StringTools.toArray(cmdLineParameterValues, ";");
        if (args != null) {
            for (int i = 0; i < args.length; i++) {
                modelDocString = modelDocString.replaceAll("%"+i, args[i]);
            }
        }
        
        int result = Protocol.ERROR;
        Document modelDoc = null;
        
        try {
            modelDoc = XMLTools.getDocumentFromString(modelDocString);
        } catch (SAXException se) {
            se.printStackTrace();
            return;
        }
        
        runtime = new StandardRuntime(properties);
        
        infoStream = new PrintStream(workspaceDir + "/$info.log");
        errorStream = new PrintStream(workspaceDir + "/$error.log");
        
        // add info and error log output
        runtime.addInfoLogObserver(new Observer() {
            public void update(Observable obs, Object obj) {
                infoStream.print(obj);
            }
        });
        runtime.addErrorLogObserver(new Observer() {
            public void update(Observable obs, Object obj) {
                errorStream.print(obj);
            }
        });
        
        runtime.loadModel(modelDoc, null);
        
        if (!info.equals("")) {
            runtime.println(info);
        }
        
        if (Server.getRuns() < Server.getMaxRuns()) {
            
            Runnable runnable = new Runnable() {
                public void run() {
                    Server.setRuns(Server.getRuns() + 1);
                    try {
                        runtime.runModel();
                    } catch (Exception ex) {
                        runtime.handle(ex);
                    }
                    Server.setRuns(Server.getRuns() - 1);
                    infoStream.close();
                    errorStream.close();
                }
            };
            new Thread(runnable).start();
            result = Protocol.SUCCESS;
            
        } else {
            
            result = Protocol.ERROR;
            
        }
        
        ByteStream.toStream(client.getOutputStream(), result);
/*
        ByteStream.toStream(outStream, runtime.getErrorLog());
        ByteStream.toStream(outStream, runtime.getInfoLog());
 */
    }
    
    private void getModelLog(String fileName, int offset) throws IOException {

        if (client.isClosed()) {
            Server.writeLog("Couldn't get log data because client is closed!");
            return;
        }
        
        String s, log = "";
        int result = Protocol.ERROR;
        
        try {
            BufferedReader reader = new BufferedReader(new FileReader(fileName));
            reader.skip(offset);
            while ((s = reader.readLine()) != null) {
                log += s + "\n";
            }
            reader.close();
            result = Protocol.SUCCESS;
        } catch (FileNotFoundException ex) {
            ex.printStackTrace();
        }
        
        ByteStream.toStream(client.getOutputStream(), log);
        ByteStream.toStream(client.getOutputStream(), result);
    }
        
    private String listFiles(String fileName) {
        
        File file = new File(baseDir + fileName);
        if (!file.exists()) {
            return "";
        }
        
        String result = "";
        
        if (file.isDirectory()) {
            
            String list[] = file.list();
            if (list == null) {
                return null;
            }
            
            for (int i = 0; i < list.length; i++) {
                String file1 = file.getAbsolutePath() + "/" + list[i];
                String filePath = listFiles(cutPrefix(file1, baseDir));
                if (filePath.length()>0) {
                    result += filePath + ";";
                }
            }
            
            if (result.length()>0) {
                result = result.substring(0, result.length()-1);
            }
            
        } else {
            
            result = cutPrefix(file.getAbsolutePath(), baseDir);
            
        }
        
        return result;
    }
    
    private boolean cleanDirRecursive(String fileName) throws IOException {
        boolean result = true;
        File dir = new File(baseDir + fileName);
        if (dir.isDirectory()) {
            String list[] = dir.list();
            if (list == null) {
                return false;
            }
            for (int i = 0; i < list.length; i++) {
                result = result && deleteDirRecursive(new File(dir + "/" + list[i]).getCanonicalFile());
            }
        }
        return result;
    }
    
    private static boolean deleteDirRecursive(File dir) {
        
        File candir;
        try {
            candir = dir.getCanonicalFile();
        } catch (IOException e) {
            return false;
        }
        
        if (!candir.equals(dir.getAbsoluteFile())) {
            return false;
        }
        
        File[] files = candir.listFiles();
        if (files != null) {
            for (int i = 0; i < files.length; i++) {
                File file = files[i];
                boolean deleted = file.delete();
                if (!deleted) {
                    if (file.isDirectory()) {
                        deleteDirRecursive(file);
                    }
                }
            }
        }
        
        return dir.delete();
    }
    
    private String listDirs(String fileName) {
        
        File file = new File(baseDir + fileName);
        String result = "";
        
        if (file.isDirectory()) {
            
            String list[] = file.list();
            if (list == null) {
                return null;
            }
            
            //result = file.getAbsolutePath() + ";";
            result = cutPrefix(file.getAbsolutePath(), baseDir) + ";";
            
            for (int i = 0; i < list.length; i++) {
                String file1 = file.getAbsolutePath() + "/" + list[i];
                String filePath = listDirs(cutPrefix(file1, baseDir));
                if (filePath.length()>0) {
                    result += filePath + ";";
                }
            }
            result = result.substring(0, result.length()-1);
        }
        
        return result;
    }
    
    private static String cutPrefix(String path, String baseDir) {
        //return path;
        return path.substring(baseDir.length()-1, path.length());
    }
    
    public void stop() {
        Server.writeLog("Child shut down.. Bye!");
        try {
            getClient().getOutputStream().flush();
            getClient().getOutputStream().close();
            getClient().close();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        Server.removeChild(this);
    }
    
    public void startChild() {
        
        Runnable child = new Runnable() {
            public void run() {
                
                try {
                    
                    InputStream inStream = client.getInputStream();
                    int input;
                    
                    while ((input = ByteStream.toInt(inStream)) >=0) {
                        
                        Server.writeLog("Receiving command " + Protocol.getName(input) + " from " + client.getInetAddress());
                        String fileName;
                        
                        switch (input) {
                            case Protocol.GET_FILE:
                                
                                fileName = ByteStream.toString(inStream);
                                pushFile(fileName);
                                break;
                                
                            case Protocol.PUSH_FILE:
                                
                                fileName = ByteStream.toString(inStream);
                                getFile(fileName);
                                break;
                                
                            case Protocol.CREATE_DIR:
                                
                                fileName = ByteStream.toString(inStream);
                                createDir(fileName);
                                break;
                                
                            case Protocol.CLEAN_DIR:
                                
                                fileName = ByteStream.toString(inStream);
                                cleanDir(fileName);
                                break;
                                
                            case Protocol.LIST_PATH:
                                
                                fileName = ByteStream.toString(inStream);
                                pushListing(fileName);
                                break;
                                
                            case Protocol.JAMS_RUN:
                                
                                String workspaceDir = ByteStream.toString(inStream);
                                String libDir = ByteStream.toString(inStream);
                                String modelFilename = ByteStream.toString(inStream);
                                String debug = ByteStream.toString(inStream);
                                
                                runJAMS(workspaceDir, libDir, modelFilename, debug);
                                
                                break;
                                
                            case Protocol.GET_BASE_DIR:
                                
                                ByteStream.toStream(client.getOutputStream(), baseDir);
                                break;
                                
                            case Protocol.GET_RUN_COUNT:
                                
                                //ByteStream.toStream(client.getOutputStream(), Server.getChildCount());
                                ByteStream.toStream(client.getOutputStream(), Server.getRuns());
                                break;
                                
                            case Protocol.GET_MAX_RUN_COUNT:
                                
                                //ByteStream.toStream(client.getOutputStream(), Server.getMaxChildCount());
                                ByteStream.toStream(client.getOutputStream(), Server.getMaxRuns());
                                break;
                                
                            case Protocol.GET_ADDRESS:
                                
                                ByteStream.toStream(client.getOutputStream(), client.getLocalAddress().toString());
                                ByteStream.toStream(client.getOutputStream(), client.getLocalPort());
                                break;
                                
                            case Protocol.GET_MODEL_INFO_LOG:
                                
                                workspaceDir = baseDir + ByteStream.toString(inStream);
                                int offset = ByteStream.toInt(inStream);
                                getModelLog(workspaceDir + "/$info.log", offset);
                                break;
                                
                            case Protocol.GET_MODEL_ERROR_LOG:
                                
                                workspaceDir = baseDir + ByteStream.toString(inStream);
                                offset = ByteStream.toInt(inStream);
                                getModelLog(workspaceDir + "/$error.log", offset);
                                break;
                                
                            case Protocol.CLIENT_SHUT_DOWN:
                                
                                stop();
                                return;
                                
                            case Protocol.SERVER_SHUT_DOWN:
                                
                                Server.stop();
                                return;
                                
                        }
                    }
                    stop();
                    
                } catch (SocketException se) {
                    stop();
                } catch (IOException ioe) {
                    ioe.printStackTrace();
                }
                
            }
        };
        
        new Thread(child).start();
        
    }
    
    public Socket getClient() {
        return client;
    }
    
}
