/*
 * Client.java
 * Created on 26. Mai 2007, 23:04
 *
 * This file is part of JAMS
 * Copyright (C) 2007 FSU Jena
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
package optas.remote.client;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Observer;
import java.util.StringTokenizer;
import jams.runtime.JAMSLog;
import jams.tools.StringTools;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;
import optas.remote.common.ByteStream;
import optas.remote.common.Commands.ConnectCommand;
import optas.remote.common.Commands.GetFileCommand;
import optas.remote.common.Commands.GetFileListCommand;
import optas.remote.common.Commands.GetJobListCommand;
import optas.remote.common.CommunicationException;
import optas.remote.common.FileInfo;
import optas.remote.common.JAMSConnection;
import optas.remote.common.JobState;
import optas.remote.common.Protocol;
import optas.remote.server.ServerOPTAS;

/**
 *
 * @author Sven Kralisch
 */
public class ClientOPTAS {

    private byte[] buffer;
    private Socket socket;
    private JAMSLog errorLog = new JAMSLog();
    private JAMSLog infoLog = new JAMSLog();
    private String host, account, password;
    private int port;
    private OutputStream outStream;
    private InputStream inStream;
    private static String libDirectory = "C:/Arbeit/JAMS/lib/";
    private static final int TIMEOUT = 10000;
    JAMSConnection connection;

    public ClientOPTAS(String host, int port, String account, String password) {
        this.host = host;
        this.port = port;
        this.account = account;
        this.password = password;
    }

    public JAMSConnection getConnection() {
        return connection;
    }

    public boolean connect() {
        try {
            buffer = new byte[ServerOPTAS.BUFFER_SIZE];
            socket = new Socket(host, port);

            outStream = socket.getOutputStream();
            inStream = socket.getInputStream();

            connection = new JAMSConnection(socket, null, getInfoLog(), getErrorLog());

            Object result = null;
            try {
                result = connection.perform(new ConnectCommand(account, password), TIMEOUT);
                if (result.equals(ConnectCommand.GRANTED)) {
                    getInfoLog().print("Connection accepted by server!\n");
                } else {
                    getInfoLog().print("Connection refused by server!\n");
                    getErrorLog().print("Wrong account - closing connection!\n");
                    stopClient();
                    return false;
                }
            } catch (CommunicationException ce) {
                ce.printStackTrace();
                getInfoLog().print("Connection refused by server!\n");
                getErrorLog().print("Wrong account - closing connection!\n");
                stopClient();
                return false;
            }

        } catch (UnknownHostException ex) {
            getErrorLog().print(ex.getMessage() + "\n");
            getErrorLog().print(StringTools.getStackTraceString(ex.getStackTrace()) + "\n");
            return false;
        } catch (IOException ex) {
            getErrorLog().print(ex.getMessage() + "\n");
            getErrorLog().print(StringTools.getStackTraceString(ex.getStackTrace()) + "\n");
            return false;
        }
        return true;
    }

    public void getFile(File fDst, int jobId, String fileName) throws IOException {
        GetFileCommand command = new GetFileCommand(jobId, fileName);

        Object result = null;
        try {
            result = connection.perform(command, TIMEOUT);
        } catch (CommunicationException ce) {
            ce.printStackTrace();
        }

        if (result instanceof Exception) {
            getErrorLog().print("Error receiving file " + fileName + "\n");
            throw new IOException(result.toString());
        } else if (!(result instanceof byte[])) {
            getErrorLog().print("Error receiving file " + fileName + "\n");
            throw new IOException(result.toString());
        }

        byte[] buffer = (byte[]) result;

        File f = null;

        if (fDst.isDirectory()) {
            f = new File(fDst.getAbsolutePath() + "/" + fileName);
        } else {
            if (!fDst.getParentFile().exists()) {
                if (!fDst.getParentFile().mkdirs()) {
                    System.out.println("Could not create parent directory");
                }
            }
            f = fDst;
        }

        FileOutputStream fos = new FileOutputStream(f);
        fos.write(buffer);

        getInfoLog().print("Successfully received file " + fileName + "\n");

        System.out.println("Receiving file for job " + jobId + " name: " + fileName + " to " + fDst.getAbsolutePath() + "\n");
    }

    public ArrayList<FileInfo> getFileListing(JobState job) throws IOException {
        GetFileListCommand command = new GetFileListCommand((int) job.getId());

        Object result = null;
        try {
            result = connection.perform(command, TIMEOUT);
        } catch (CommunicationException ce) {
            ce.printStackTrace();
        }

        if (result instanceof Exception) {
            getErrorLog().print("Error receiving file list for job id " + job.getId() + "\n");
            throw new IOException(result.toString());
        } else if (!(result instanceof ArrayList)) {
            getErrorLog().print("Error receiving file list for job id " + job.getId() + "\n");
            throw new IOException(result.toString());
        }

        ArrayList<FileInfo> list = (ArrayList<FileInfo>) result;
        getInfoLog().print("Retrieved file list for " + account + "and job " + job.getId() + "\n");

        return list;
    }

    public ArrayList<JobState> getJobListing() throws IOException {
        GetJobListCommand command = new GetJobListCommand();

        Object result = null;
        try {
            result = connection.perform(command, TIMEOUT);
        } catch (CommunicationException ce) {
            ce.printStackTrace();
        }

        if (result instanceof Exception) {
            getErrorLog().print("Error receiving job list\n");
            throw new IOException(result.toString());
        } else if (!(result instanceof ArrayList)) {
            getErrorLog().print("Error receiving job list\n");
            throw new IOException(result.toString());
        }
        ArrayList<JobState> list = (ArrayList<JobState>) result;
        getInfoLog().print("Retrieved dir list for " + account + "\n");

        return list;
    }

    public void stopClient() throws IOException {
        getInfoLog().print("Client shut down.. Bye!\n");
        connection.close();
    }

    private void addFilesToZip(ZipOutputStream out, String subPath, File files[]) {
        int read = 0;
        FileInputStream in;
        byte[] data = new byte[1024];

        for (File f : files) {
            try {
                if (f.isDirectory()) {
                    ZipEntry entry = new ZipEntry(subPath + "/" + f.getName());

                    // Neuer Eintrag dem Archiv hinzufügen
                    //out.putNextEntry(entry);
                    if (!subPath.isEmpty() && !subPath.endsWith("/")) {
                        subPath += "/";
                    }
                    addFilesToZip(out, subPath + f.getName(), f.listFiles());
                } else if (f.isFile()) {
                    System.out.println(f);
                    // Eintrag für neue Datei anlegen
                    if (!subPath.isEmpty() && !subPath.endsWith("/")) {
                        subPath += "/";
                    }
                    ZipEntry entry = new ZipEntry(subPath + f.getName());
                    in = new FileInputStream(f);
                    // Neuer Eintrag dem Archiv hinzufügen
                    out.putNextEntry(entry);
                    // Hinzufügen der Daten zum neuen Eintrag
                    while ((read = in.read(data, 0, 1024)) != -1) {
                        out.write(data, 0, read);
                    }
                    out.closeEntry(); // Neuen Eintrag abschließen
                    in.close();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void zipFile(File dstZip, File srcFiles[]) {
        try {
            // Zip-Archiv mit Stream verbinden
            ZipOutputStream out =
                    new ZipOutputStream(new FileOutputStream(dstZip));
            // Archivierungs-Modus setzen
            out.setMethod(ZipOutputStream.DEFLATED);
            // Hinzufügen der einzelnen Einträge
            for (File f : srcFiles) {
                //we do not want to locate srcFiles in subDir .. thats why this explicit enumeration
                if (f.isDirectory()) {
                    addFilesToZip(out, "", f.listFiles());
                } else if (f.isFile()) {
                    addFilesToZip(out, "", new File[]{f});
                }

            }

            out.close();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    private void unzipFile(File dstDir, File srcZip) {
        int read = 0;
        FileOutputStream out;
        byte[] data = new byte[1024];

        try {
            // Zip-Archiv mit Stream verbinden
            ZipInputStream in =
                    new ZipInputStream(new FileInputStream(srcZip));
            // Hinzufügen der einzelnen Einträge
            ZipEntry e = null;
            while ((e = in.getNextEntry()) != null) {
                if (e.isDirectory()) {
                    File directory = new File(dstDir + "/" + e.getName());
                    directory.mkdirs();
                } else {
                    File directory = new File(dstDir + "/" + e.getName());
                    directory.getParentFile().mkdirs();
                    out = new FileOutputStream(dstDir + "/" + e.getName());

                    while ((read = in.read(data, 0, 1024)) != -1) {
                        out.write(data, 0, read);
                    }
                    out.close();
                }
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    public void run(File directory, File model, boolean useStandardLibs) throws IOException {
        if (socket == null) {
            getErrorLog().print("Not connected to server! Aborting ..\n");
            return;
        }

        String tmpDir = System.getProperty("java.io.tmpdir");
        File tmpFile = new File(tmpDir + "/" + System.currentTimeMillis() + ".zip");
        zipFile(tmpFile, new File[]{directory, model});

        ByteStream.toStream(outStream, Protocol.MANAGEMENT_RUN_MODEL);
        ByteStream.toStream(outStream, tmpFile.getName());
        ByteStream.toStream(outStream, model.getName());
        ByteStream.toStream(outStream, Boolean.toString(useStandardLibs));

        //put file on stream
        ByteStream.toStream(outStream, tmpFile);

        int result = ByteStream.toInt(inStream);
        if (result == Protocol.SUCCESS) {
            getInfoLog().print("Successfully copied workspace and started model\n");
        } else {
            getErrorLog().print("Error starting model");
        }

        tmpFile.delete();
    }

    public void extractWorkspace(String userName, String jobID, File zipFile) throws IOException {
        String workspace = userName + "/" + jobID;

        String tmpDir = System.getProperty("java.io.tmpdir");
        File tmpFile = new File(tmpDir + "/" + System.currentTimeMillis() + "/");
        tmpFile.mkdirs();

        unzipFile(tmpFile, zipFile);

        this.pushDir(workspace, tmpFile.getAbsolutePath());
    }

    public void copyLibs(String userName, String jobId) throws IOException {
        String workspace = userName + "/" + jobId + "/lib/";
        this.pushDir(workspace, libDirectory);
    }

    public void configureModel(String userName, String jobId, String modelName) throws IOException {
        String modelPath = "/" + userName + "/" + jobId + "/" + modelName;

        ByteStream.toStream(outStream, Protocol.CONFIGURE_MODEL);
        ByteStream.toStream(outStream, modelPath);
        ByteStream.toStream(outStream, modelName);
        int result = ByteStream.toInt(inStream);
        if (result == Protocol.SUCCESS) {
            getInfoLog().print("Successfully copied workspace and started model\n");
        } else {
            getErrorLog().print("Error starting model");
        }
    }

    public int runJAMS(String userName, String jobId) throws IOException {
        String workspace = "/" + userName + "/" + jobId + "/";
        String modelFile = "/" + userName + "/" + jobId + "/" + "optimization.jam";

        if (socket == null) {
            this.connect();
            if (socket == null) {
                getErrorLog().print("Not connected to server! Aborting ..\n");
                return -1;
            }
        }

        ByteStream.toStream(outStream, Protocol.JAMS_RUN);
        ByteStream.toStream(outStream, workspace);
        ByteStream.toStream(outStream, modelFile);

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

    public void addInfoLogObserver(Observer o) {
        getInfoLog().addObserver(o);
    }

    public void addErrorLogObserver(Observer o) {
        getErrorLog().addObserver(o);
    }

    public void getFile(String remoteFileName, String localFileName) throws IOException {
        if (socket == null) {
            this.connect();
            if (socket == null) {
                getErrorLog().print("Not connected to server! Aborting ..\n");
                return;
            }
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
            this.connect();
            if (socket == null) {
                getErrorLog().print("Not connected to server! Aborting ..\n");
                return;
            }
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
            this.connect();
            if (socket == null) {
                getErrorLog().print("Not connected to server! Aborting ..\n");
                return;
            }
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
            this.connect();
            if (socket == null) {
                getErrorLog().print("Not connected to server! Aborting ..\n");
                return;
            }
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
        pushDir(remotePath, localPath, StringTools.toArray(excludes, ";"));
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
        getDir(remotePath, localPath, StringTools.toArray(excludes, ";"));
    }

    public void getDir(String remotePath, String localPath) throws IOException {
        getDir(remotePath, localPath, new String[0]);
    }

    public void getDir(String remotePath, String localPath, String[] excludes) throws IOException {

        if (socket == null) {
            this.connect();
            if (socket == null) {
                getErrorLog().print("Not connected to server! Aborting ..\n");
                return;
            }
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
            if ((dirName.length() == 0) || endsWithMultiple(dirName, excludes)) {
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
            this.connect();
            if (socket == null) {
                getErrorLog().print("Not connected to server! Aborting ..\n");
                return null;
            }
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
            this.connect();
            if (socket == null) {
                getErrorLog().print("Not connected to server! Aborting ..\n");
                return null;
            }
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

    public void stopServer() throws IOException {

        if (socket == null) {
            this.connect();
            if (socket == null) {
                getErrorLog().print("Not connected to server! Aborting ..\n");
                return;
            }
        }

        ByteStream.toStream(outStream, Protocol.SERVER_SHUT_DOWN);
    }

    public int getRunCount() throws IOException {
        if (socket == null) {
            this.connect();
            if (socket == null) {
                getErrorLog().print("Not connected to server! Aborting ..\n");
                return -1;
            }
        }

        ByteStream.toStream(outStream, Protocol.GET_RUN_COUNT);
        return ByteStream.toInt(inStream);
    }

    public String getBaseDir() throws IOException {
        if (socket == null) {
            this.connect();
            if (socket == null) {
                getErrorLog().print("Not connected to server! Aborting ..\n");
                return null;
            }
        }

        ByteStream.toStream(outStream, Protocol.GET_BASE_DIR);
        return ByteStream.toString(inStream);
    }

    public int getMaxRunCount() throws IOException {
        if (socket == null) {
            this.connect();
            if (socket == null) {
                getErrorLog().print("Not connected to server! Aborting ..\n");
                return 0;
            }
        }
        ByteStream.toStream(outStream, Protocol.GET_MAX_RUN_COUNT);
        return ByteStream.toInt(inStream);
    }

    public String getServerAddress() throws IOException {
        if (socket == null) {
            this.connect();
            if (socket == null) {
                getErrorLog().print("Not connected to server! Aborting ..\n");
                return null;
            }
        }

        ByteStream.toStream(outStream, Protocol.GET_ADDRESS);
        String address = ByteStream.toString(inStream);
        int port = ByteStream.toInt(inStream);
        return address + ":" + port;
    }

    public String getModelInfoLog(String workspaceDir, int offset) throws IOException {
        if (socket == null) {
            this.connect();
            if (socket == null) {
                getErrorLog().print("Not connected to server! Aborting ..\n");
                return null;
            }
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

    public String getModelErrorLog(String workspaceDir, int offset) throws IOException {
        if (socket == null) {
            this.connect();
            if (socket == null) {
                getErrorLog().print("Not connected to server! Aborting ..\n");
                return null;
            }
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

    public static void main(String[] args) throws IOException {
        ClientOPTAS client = new ClientOPTAS("localhost", 9000, "admin", "borlandc");
        client.connect();
        /*client.run(new File("C:/Users/chris/Desktop/web_test/exampleWorkspace/"),
        new File("C:/Users/chris/Desktop/web_test/j2k_gehlberg.jam/"), true);*/

        ArrayList<JobState> list = client.getJobListing();
        for (JobState state : list) {
            System.out.println(state);
        }
    }
}
