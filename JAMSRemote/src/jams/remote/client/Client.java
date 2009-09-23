/*
 * Client.java
 * Created on 26. Mai 2007, 23:04
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

package jams.remote.client;

import jams.remote.common.ByteStream;
import jams.remote.common.Protocol;
import jams.remote.server.Server;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Observer;
import java.util.StringTokenizer;
import jams.tools.JAMSTools;
import jams.runtime.JAMSLog;

/**
 *
 * @author Sven Kralisch
 */

public class Client {
    
    private byte[] buffer;
    private Socket socket;
    private JAMSLog errorLog = new JAMSLog();
    private JAMSLog infoLog = new JAMSLog();
    private String host, account, password;
    private int port;
    private OutputStream outStream;
    private InputStream inStream;
    
    public Client(String host, int port, String account, String password) {
        this.host = host;
        this.port = port;
        this.account = account;
        this.password = password;
    }
    
    public void connect() {
        try {
            buffer = new byte[Server.BUFFER_SIZE];
            socket = new Socket(host, port);
            
            outStream = socket.getOutputStream();
            inStream = socket.getInputStream();
            
            ByteStream.toStream(outStream, account);
            ByteStream.toStream(outStream, password);
            
            int result = ByteStream.toInt(inStream);
            if (result == Protocol.ACCEPT_CONNECTION) {
                getInfoLog().print("Connection accepted by server!\n");
            } else {
                getInfoLog().print("Connection refused by server!\n");
                getErrorLog().print("Wrong account - closing connection!\n");
                stopClient();
            }
        } catch (UnknownHostException ex) {
            getErrorLog().print(ex.getMessage() + "\n");
            getErrorLog().print(JAMSTools.getStackTraceString(ex.getStackTrace()) + "\n");
        } catch (IOException ex) {
            getErrorLog().print(ex.getMessage() + "\n");
            getErrorLog().print(JAMSTools.getStackTraceString(ex.getStackTrace()) + "\n");
        }
    }
    
    public void addInfoLogObserver(Observer o) {
        getInfoLog().addObserver(o);
    }
    
    public void addErrorLogObserver(Observer o) {
        getErrorLog().addObserver(o);
    }
    
    public void getFile(String remoteFileName, String localFileName) throws IOException {
        
        if (socket == null) {
            getErrorLog().print("Not connected to server! Aborting ..\n");
            return;
        }
        
        ByteStream.toStream(outStream, Protocol.GET_FILE);
        ByteStream.toStream(outStream, remoteFileName);
        
        //get the name of the file being transfered
        String fileName = ByteStream.toString(inStream);
        
        File file = new File(localFileName);
        ByteStream.toFile(inStream, file);
        
        int result = ByteStream.toInt(inStream);
        if (result == Protocol.SUCCESS) {
            getInfoLog().print("Successfully received file " + fileName + "\n");
        } else {
            getErrorLog().print("Error receiving file " + fileName + "\n");
        }
    }
    
    public void pushFile(String remoteFileName, String localFileName) throws IOException {
        
        if (socket == null) {
            getErrorLog().print("Not connected to server! Aborting ..\n");
            return;
        }
        
        ByteStream.toStream(outStream, Protocol.PUSH_FILE);
        ByteStream.toStream(outStream, remoteFileName);
        
        //put file on stream
        File file = new File(localFileName);
        ByteStream.toStream(outStream, file);
        
        int result = ByteStream.toInt(inStream);
        if (result == Protocol.SUCCESS) {
            getInfoLog().print("Successfully sent file " + localFileName + "\n");
        } else {
            getErrorLog().print("Error sending file " + localFileName + "\n");
        }
    }
    
    public void createDir(String remotePath) throws IOException {
        
        if (socket == null) {
            getErrorLog().print("Not connected to server! Aborting ..\n");
            return;
        }
        
        ByteStream.toStream(outStream, Protocol.CREATE_DIR);
        ByteStream.toStream(outStream, remotePath);
        
        int result = ByteStream.toInt(inStream);
        if (result == Protocol.SUCCESS) {
            getInfoLog().print("Successfully created dir " + remotePath + "\n");
        } else {
            getErrorLog().print("Error creating dir " + remotePath + "\n");
        }
    }
    
    public void cleanDir(String remotePath) throws IOException {
        
        if (socket == null) {
            getErrorLog().print("Not connected to server! Aborting ..\n");
            return;
        }
        
        ByteStream.toStream(outStream, Protocol.CLEAN_DIR);
        ByteStream.toStream(outStream, remotePath);
        
        int result = ByteStream.toInt(inStream);
        if (result == Protocol.SUCCESS) {
            getInfoLog().print("Successfully cleaned dir " + remotePath + "\n");
        } else {
            getErrorLog().print("Error cleaning dir " + remotePath + "\n");
        }
    }
    
    public void pushDir(String remotePath, String localPath, String excludes) throws IOException {
        pushDir(remotePath, localPath, JAMSTools.toArray(excludes, ";"));
    }
    
    public void pushDir(String remotePath, String localPath) throws IOException {
        pushDir(remotePath, localPath, new String[0]);
    }
    
    public void pushDir(String remotePath, String localPath, String[] excludes) throws IOException {
        
        File src = new File(localPath);
        
        if (src.isDirectory()) {
            
            createDir(remotePath);
            
            String list[] = src.list();
            
            for (int i = 0; i < list.length; i++) {
                String dest1 = remotePath + "/" + list[i];
                String src1 = src.getAbsolutePath() + "/" + list[i];
                if (!endsWithMultiple(src1, excludes)) {
                    pushDir(dest1, src1, excludes);
                }
            }
            
        } else {
            
            if (!endsWithMultiple(localPath, excludes)) {
                pushFile(remotePath, localPath);
            }
            
        }
    }
    
    private boolean endsWithMultiple(String string, String[] suffixes) {
        for (String suffix : suffixes) {
            if (string.endsWith(suffix)) {
                return true;
            }
        }
        return false;
    }
    
    public void getDir(String remotePath, String localPath, String excludes) throws IOException {
        getDir(remotePath, localPath, JAMSTools.toArray(excludes, ";"));
    }
    
    public void getDir(String remotePath, String localPath) throws IOException {
        getDir(remotePath, localPath, new String[0]);
    }
    
    public void getDir(String remotePath, String localPath, String[] excludes) throws IOException {
        
        if (socket == null) {
            getErrorLog().print("Not connected to server! Aborting ..\n");
            return;
        }
        
        ByteStream.toStream(outStream, Protocol.LIST_PATH);
        ByteStream.toStream(outStream, remotePath);
        
        String dirList = ByteStream.toString(inStream);
        String fileList = ByteStream.toString(inStream);
        
        int result = ByteStream.toInt(inStream);
        if (result != Protocol.SUCCESS) {
            getErrorLog().print("Error listing dir " + remotePath + "\n");
        }
        
        if (dirList.isEmpty() && fileList.isEmpty()) {
            getInfoLog().print("No files found!\n");
            return;
        }
        
        //create all directories
        File rootPath = new File(localPath);
        rootPath.mkdirs();
        StringTokenizer dirTok = new StringTokenizer(dirList, ";");
        while (dirTok.hasMoreTokens()) {
            String dirName = dirTok.nextToken();
            dirName = dirName.substring(remotePath.length(), dirName.length());
            if ((dirName.length()==0) || endsWithMultiple(dirName, excludes)) {
                continue;
            }
            File dir = new File(rootPath.getAbsolutePath() + "/" + dirName);
            dir.mkdirs();
        }
        
        //get all files
        StringTokenizer fileTok = new StringTokenizer(fileList, ";");
        while (fileTok.hasMoreTokens()) {
            String fileName = fileTok.nextToken();
            String localFileName = fileName.substring(remotePath.length(), fileName.length());
            File localFile = new File(rootPath.getAbsolutePath() + "/" + localFileName);
            //file.getParentFile().mkdirs()
            if (!endsWithMultiple(fileName, excludes)) {
                getFile(fileName, localFile.getAbsolutePath());
            }
        }
    }
    
    public String getFileListing(String remotePath) throws IOException {
        
        if (socket == null) {
            getErrorLog().print("Not connected to server! Aborting ..\n");
            return null;
        }
        
        ByteStream.toStream(outStream, Protocol.LIST_PATH);
        ByteStream.toStream(outStream, remotePath);
        
        String dirList = ByteStream.toString(inStream);
        String fileList = ByteStream.toString(inStream);
        
        int result = ByteStream.toInt(inStream);
        if (result != Protocol.SUCCESS) {
            getErrorLog().print("Error listing dir " + remotePath + "\n");
        }
        
        
        getInfoLog().print("Retrieved file list for " + remotePath + "\n");
        
        return fileList;
    }
    
    public String getDirListing(String remotePath) throws IOException {
        
        if (socket == null) {
            getErrorLog().print("Not connected to server! Aborting ..\n");
            return null;
        }
        
        ByteStream.toStream(outStream, Protocol.LIST_PATH);
        ByteStream.toStream(outStream, remotePath);
        
        String dirList = ByteStream.toString(inStream);
        String fileList = ByteStream.toString(inStream);
        
        int result = ByteStream.toInt(inStream);
        if (result != Protocol.SUCCESS) {
            getErrorLog().print("Error listing dir " + remotePath + "\n");
        }
        
        getInfoLog().print("Retrieved dir list for " + remotePath + "\n");
        
        return dirList;
    }
    
    public boolean isClosed() {
        if (socket == null) {
            return true;
        } else {
            return socket.isClosed();
        }
    }
    
    public void stopClient() throws IOException {
        
        if (socket == null) {
            getErrorLog().print("Not connected to server! Aborting ..\n");
            return;
        }
        
        getInfoLog().print("Client shut down.. Bye!\n");
        outStream.flush();
        socket.close();
        //ByteStream.toStream(socket.getOutputStream(), Protocol.CLIENT_SHUT_DOWN);
        socket = null;
        //socket.close();
    }
    
    public void stopServer() throws IOException {
        
        if (socket == null) {
            getErrorLog().print("Not connected to server! Aborting ..\n");
            return;
        }
        
        ByteStream.toStream(outStream, Protocol.SERVER_SHUT_DOWN);
    }
    
    public int runJAMS(String workspaceDir, String libDir, String modelFilename, String debug) throws IOException {
        
        if (socket == null) {
            getErrorLog().print("Not connected to server! Aborting ..\n");
            return -1;
        }
        
        ByteStream.toStream(outStream, Protocol.JAMS_RUN);
        ByteStream.toStream(outStream, workspaceDir);
        ByteStream.toStream(outStream, libDir);
        ByteStream.toStream(outStream, modelFilename);
        ByteStream.toStream(outStream, debug);
        
/*
        String errorMsg = ByteStream.toString(inStream);
        String infoMsg = ByteStream.toString(inStream);
 */
        
        int result = ByteStream.toInt(inStream);
        if (result == Protocol.SUCCESS) {
            getInfoLog().print("Successfully started JAMS model\n");
        } else {
            getErrorLog().print("Error starting JAMS model\n");
        }
/*
        String[] resultArray = new String[2];
        resultArray[0] = infoMsg;
        resultArray[1] = errorMsg;
 */
        return result;
    }
    
    public int getRunCount() throws IOException {
        
        if (socket == null) {
            getErrorLog().print("Not connected to server! Aborting ..\n");
            return -1;
        }
        
        ByteStream.toStream(outStream, Protocol.GET_RUN_COUNT);
        return ByteStream.toInt(inStream);
    }
    
    public String getBaseDir() throws IOException {
        
        if (socket == null) {
            getErrorLog().print("Not connected to server! Aborting ..\n");
            return null;
        }
        
        ByteStream.toStream(outStream, Protocol.GET_BASE_DIR);
        return ByteStream.toString(inStream);
    }
    
    public int getMaxRunCount() throws IOException {
        
        if (socket == null) {
            getErrorLog().print("Not connected to server! Aborting ..\n");
            return -1;
        }
        
        ByteStream.toStream(outStream, Protocol.GET_MAX_RUN_COUNT);
        return ByteStream.toInt(inStream);
    }
    
    public String getServerAddress() throws IOException {
        
        if (socket == null) {
            getErrorLog().print("Not connected to server! Aborting ..\n");
            return null;
        }
        
        ByteStream.toStream(outStream, Protocol.GET_ADDRESS);
        String address = ByteStream.toString(inStream);
        int port = ByteStream.toInt(inStream);
        return address + ":" + port;
    }
    
    public String getModelInfoLog(String workspaceDir, int offset)  throws IOException {
        if (socket == null) {
            getErrorLog().print("Not connected to server! Aborting ..\n");
            return null;
        }
        
        ByteStream.toStream(outStream, Protocol.GET_MODEL_INFO_LOG);
        ByteStream.toStream(outStream, workspaceDir);
        ByteStream.toStream(outStream, offset);
        
        String info = ByteStream.toString(inStream);
        int result = ByteStream.toInt(inStream);
        
        if (result != Protocol.SUCCESS) {
            getErrorLog().print("Error reading model info log\n");
        }
        return info;
    }
    
    public String getModelErrorLog(String workspaceDir, int offset)  throws IOException {
        if (socket == null) {
            getErrorLog().print("Not connected to server! Aborting ..\n");
            return null;
        }
        
        ByteStream.toStream(outStream, Protocol.GET_MODEL_ERROR_LOG);
        ByteStream.toStream(outStream, workspaceDir);
        ByteStream.toStream(outStream, offset);
        
        String error = ByteStream.toString(inStream);
        int result = ByteStream.toInt(inStream);
        
        if (result != Protocol.SUCCESS) {
            getErrorLog().print("Error reading model error log\n");
        }
        return error;
    }

    public JAMSLog getErrorLog() {
        return errorLog;
    }
    
    public JAMSLog getInfoLog() {
        return infoLog;
    }
    
}
