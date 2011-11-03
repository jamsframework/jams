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
package optas.remote.server;


import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.sql.SQLException;
import optas.remote.common.ByteStream;
import optas.remote.common.CommunicationException;
import optas.remote.common.JAMSCommand;
import optas.remote.common.JAMSConnection;
import optas.remote.common.Protocol;

/**
 *
 * @author S. Kralisch
 */
public abstract class ServerChildOPTAS {
    
    protected String baseDir;
    protected ServerOPTAS server;
    protected JAMSConnection connection;
    Socket client = null;
    public ServerChildOPTAS(JAMSConnection client, ServerOPTAS server) throws IOException, SQLException {
        this.connection = client;
        this.client = connection.getSocket();
        this.connection.setHandler(new JAMSConnection.CommandHandler() {

            @Override
            public boolean handle(JAMSConnection connection, JAMSCommand command) {
                return ServerChildOPTAS.this.handle(command);
            }
        });
        baseDir="";
        this.server = server;
    }
    
    public void stop() {
        server.writeLog("Child shut down.. Bye!");
        connection.close();
        server.removeChild(this);
    }

    public boolean handle(JAMSCommand cmd){
        connection.answer(cmd, new CommunicationException("unknown command" + cmd.getName()));
        return false;
    }

















    private void pushFile(String fileName) throws IOException {

        fileName = baseDir + fileName;

        if (client.isClosed()) {
            server.writeLog("Couldn't send file \"" + fileName + "\" because client is closed!");
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
        server.writeLog("Sent " + fileName);

        ByteStream.toStream(outStream, result);
    }

    private void getFile(String fileName) throws IOException {

        fileName = baseDir + fileName;

        if (client.isClosed()) {
            server.writeLog("Couldn't get file \"" + fileName + "\" because client is closed!");
            return;
        }

        InputStream inStream = client.getInputStream();

        int result = Protocol.ERROR;
        File file = new File(fileName);
        try {
            ByteStream.toFile(inStream, file);
            result = Protocol.SUCCESS;
            server.writeLog("Received " + fileName);
        } catch (IOException ex) {}
        ByteStream.toStream(client.getOutputStream(), result);
    }

    private void createDir(String fileName) throws IOException {

        fileName = baseDir + fileName;

        if (client.isClosed()) {
            server.writeLog("Couldn't create dir \"" + fileName + "\" because client is closed!");
            return;
        }

        int result = Protocol.ERROR;
        File dir= new File(fileName);

        if (dir.exists()) {
            result = Protocol.SUCCESS;
        } else if (dir.mkdirs()) {
            result = Protocol.SUCCESS;
            server.writeLog("Created directory " + dir);
        }
        ByteStream.toStream(client.getOutputStream(), result);

    }

    private void cleanDir(String fileName) throws IOException {

        if (client.isClosed()) {
            server.writeLog("Couldn't delete dir \"" + fileName + "\" because client is closed!");
            return;
        }

        int result = Protocol.ERROR;
        if (cleanDirRecursive(fileName)) {
            result = Protocol.SUCCESS;
            server.writeLog("Cleaned directory " + fileName);
        }
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

    private void pushListing(String fileName) throws IOException {

        //fileName = baseDir + fileName;

        if (client.isClosed()) {
            server.writeLog("Couldn't push listing \"" + fileName + "\" because client is closed!");
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

        server.writeLog("Sent listing of " + fileName);
    }

    
    

    public boolean handle(InputStream inStream, int input) throws IOException, SQLException{
        String fileName;
        switch (input) {
            case Protocol.GET_FILE:

                fileName = ByteStream.toString(inStream);
                pushFile(fileName);
                return false;

            case Protocol.PUSH_FILE:

                fileName = ByteStream.toString(inStream);
                getFile(fileName);
                return false;

            case Protocol.CREATE_DIR:

                fileName = ByteStream.toString(inStream);
                createDir(fileName);
                return false;

            case Protocol.CLEAN_DIR:

                fileName = ByteStream.toString(inStream);
                cleanDir(fileName);
                return false;

            case Protocol.LIST_PATH:

                fileName = ByteStream.toString(inStream);
                pushListing(fileName);
                return false;

            case Protocol.GET_BASE_DIR:

                ByteStream.toStream(client.getOutputStream(), baseDir);
                return false;

            case Protocol.GET_RUN_COUNT:

                //ByteStream.toStream(client.getOutputStream(), Server.getChildCount());
                ByteStream.toStream(client.getOutputStream(), server.getRuns());
                return false;

            case Protocol.GET_MAX_RUN_COUNT:

                //ByteStream.toStream(client.getOutputStream(), Server.getMaxChildCount());
                ByteStream.toStream(client.getOutputStream(), server.getMaxRuns());
                return false;

            case Protocol.GET_ADDRESS:

                ByteStream.toStream(client.getOutputStream(), client.getLocalAddress().toString());
                ByteStream.toStream(client.getOutputStream(), client.getLocalPort());
                return false;

            case Protocol.CLIENT_SHUT_DOWN:

                stop();
                return false;

            case Protocol.SERVER_SHUT_DOWN:

                server.stop();
                return false;
        }
        return false;
    }

    

    public Socket getClient() {
        return client;
    }
}
