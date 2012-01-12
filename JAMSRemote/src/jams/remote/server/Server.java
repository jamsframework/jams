/*
 * Server.java
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

package jams.remote.server;

import jams.remote.common.ByteStream;
import jams.remote.common.Protocol;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;

/**
 *
 * @author Sven Kralisch
 */

public class Server {
    
    public static final int BUFFER_SIZE = 1024 * 50;
    public static final int CHILD_COUNT = 100;
    public static final int STANDARD_PORT = 9000;
    public static final String STANDARD_PW_FILENAME = "jamsaccess.txt";
    public static final int STANDARD_MAX_RUNS = 8;
    
    private static ServerSocket server;
    private static Set<ServerChild> childs = new HashSet<ServerChild>();
    private static String baseDir;
    private static Properties accessData = new Properties();
    private static boolean anonAccess;
    private static int runs = 0;
    private static int maxRuns = STANDARD_MAX_RUNS;
    
    public static void startServer(String pwFilename, String baseDir, int port, boolean anonAccess) throws IOException, InterruptedException {
        
        Server.writeLog("Starting JAMS execution server on port " + port + " .. ");
        
        server = new java.net.ServerSocket(port);
        
        Server.baseDir = baseDir + "/";
        Server.anonAccess = anonAccess;
        Server.accessData.load(new FileInputStream(pwFilename));
        
        Server.writeLog("Success!");
        Server.writeLog("    basedir   : \"" + Server.baseDir + "\"");
        Server.writeLog("    PW file   : \"" + pwFilename + "\"");
        Server.writeLog("    anonaccess: \"" + anonAccess + "\"");
        Server.writeLog("");
        spawnChilds();
    }
    
    private static void spawnChilds() throws IOException, InterruptedException {
        
        while (!server.isClosed()) {
            
            if (!(childs.size() < CHILD_COUNT)) {
                Server.writeLog("All server slots are currently in use. Please wait or try later ..");
                Thread.sleep(1000);
            } else {
                try {
                    Socket client = server.accept();
                    
                    InputStream inStream = client.getInputStream();
                    String account = ByteStream.toString(inStream);
                    String password = ByteStream.toString(inStream);
                    
                    boolean accepted = false;
                    
                    if (!anonAccess) {
                        //check if client is authorized
                        if (password.equals(accessData.getProperty(account))) {
                            accepted = true;
                        }
                    } else if (!account.equals("")) {
                        accepted = true;
                    }
                    
                    if (!accepted) {
                        ByteStream.toStream(client.getOutputStream(), Protocol.WRONG_ACCOUNT);
                        Server.writeLog("Refusing connection from " + client.getInetAddress() + " because of wrong account/password");
                        client.close();
                        continue;
                    } else {
                        ByteStream.toStream(client.getOutputStream(), Protocol.ACCEPT_CONNECTION);
                        Server.writeLog("Accepting connection from " + client.getInetAddress());
                    }
                    
                    String clientBaseDir = baseDir + account + "/";
                    ServerChild child = new ServerChild(client, clientBaseDir);
                    Server.writeLog("Spawning new child with basedir " + clientBaseDir);
                    childs.add(child);
                    child.startChild();
                } catch (SocketException se) {
                    //socket has been closed!
                }
            }
        }
    }
    
    public static void removeChild(ServerChild child) {
        childs.remove(child);
    }
    
    public static void stop() throws IOException {
        for (ServerChild sc : childs) {
            sc.stop();
        }
        Server.writeLog("Server shut down.. Bye!");
        server.close();
    }
    
    public static int getChildCount() {
        return childs.size();
    }
    
    public static int getMaxChildCount() {
        return CHILD_COUNT;
    }
    
    public static void writeLog(String message) {
        DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
        Date date = new Date();
        System.out.println(dateFormat.format(date) + ": " + message);
    }
    
    public static void main(String[] args) {
        
        //three arguments: <pwfilename> <basedir> <port>
        try {
            
            //init with standard values
            String pwFilename = STANDARD_PW_FILENAME;
            String dir = ".";
            int port = STANDARD_PORT;
            
            boolean anonAccess = false;
            
            String configFile = null;
            try {
                if (args.length > 0) {
                    configFile = args[0];
                } else {
                    configFile = new File("./config.txt").getAbsolutePath();
                }
                Properties props = new Properties();
                props.load(new FileInputStream(configFile));
                
                pwFilename = new File(props.getProperty("accounts", pwFilename)).getAbsolutePath();
                dir = new File(props.getProperty("basedir", dir)).getAbsolutePath();
                port = Integer.parseInt(props.getProperty("port", "" + port));
                anonAccess = Boolean.parseBoolean(props.getProperty("anonaccess", "" + anonAccess));
                maxRuns = Integer.parseInt(props.getProperty("maxrun", "" + getMaxRuns()));
                
            } catch (IOException ioe) {
                Server.writeLog("Could not open config file \"" + configFile + "\", using default values");
            }
            
            //create the server
            Server.startServer(pwFilename, dir, port, anonAccess);
            
        } catch (NumberFormatException ex) {
            Server.writeLog("Failed! (malformed port number)");
        } catch (Exception ex) {
            Server.writeLog("Failed!");
            ex.printStackTrace();
        }
    }

    public static int getRuns() {
        return runs;
    }

    public static void setRuns(int aRuns) {
        runs = aRuns;
    }

    public static int getMaxRuns() {
        return maxRuns;
    }
    
}
