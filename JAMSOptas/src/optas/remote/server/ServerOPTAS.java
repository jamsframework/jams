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
package optas.remote.server;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;
import optas.remote.common.Commands.ConnectCommand;
import optas.remote.common.Database;
import optas.remote.common.Database.UserInfo;
import optas.remote.common.JAMSCommand;
import optas.remote.common.JAMSConnection;

/**
 *
 * @author Sven Kralisch
 */
public class ServerOPTAS {

    public static final int BUFFER_SIZE = 1024 * 50;
    public static final int CHILD_COUNT = 100;
    public static final int STANDARD_PORT = 9000;
    public static final String STANDARD_PW_FILENAME = "jamsaccess.txt";
    public static final int STANDARD_MAX_RUNS = 8;
    public static final int WORKER = 0;
    public static final int MANAGER = 1;
    private ServerSocket server;
    private Set<ServerChildOPTAS> childs = new HashSet<ServerChildOPTAS>();
    private String baseDir;
    private boolean anonAccess;
    private int runs = 0;
    private int maxRuns = STANDARD_MAX_RUNS;
    private UserInfo userInfo;
    private Database db;
    private int serverType;
    private Properties accessProperties;

    public void startServer(String baseDir, String dbHost, String dbName, String dbAccount, String dbPassword, int port, boolean anonAccess, int serverType) throws IOException, InterruptedException, SQLException {
        writeLog("Starting JAMS execution server (type:" + serverType + ") on port " + port + " .. ");

        server = new java.net.ServerSocket(port);

        this.baseDir = baseDir + "/";
        this.anonAccess = anonAccess;
        this.serverType = serverType;

        writeLog("Success!");
        writeLog("    basedir   : \"" + baseDir + "\"");
        writeLog("    dbAccount   : \"" + dbAccount + "\"");

        writeLog("    anonaccess: \"" + anonAccess + "\"");


        writeLog("");

        if (serverType == MANAGER) {
            writeLog("Connecting with mysql db: " + dbHost + "/" + dbName);
            db = new Database(dbHost, dbName, dbAccount, dbPassword);
        } else {
            accessProperties = new Properties();
            writeLog("    access-file: \"" + "access" + "\"");
            BufferedInputStream stream = new BufferedInputStream(new FileInputStream("access"));
            accessProperties.load(stream);
            stream.close();
            writeLog("known users:[");
            String line = "";
            for (Object o : accessProperties.keySet()) {
                line += o + ",";
            }
            writeLog(line + "]");
            db = null;
        }
        spawnChilds();
    }

    private void spawnChilds() {

        while (!server.isClosed()) {

            if (!(childs.size() < CHILD_COUNT)) {
                writeLog("All server slots are currently in use. Please wait or try later ..");
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException ie) {
                    ie.printStackTrace();
                }
            } else {
                try {
                    Socket client = server.accept();

                    JAMSConnection connection = new JAMSConnection(client, new JAMSConnection.CommandHandler() {

                        @Override
                        public boolean handle(JAMSConnection connection, JAMSCommand command) {
                            boolean accepted = false;
                            String account = null, password = null;

                            if ((command instanceof ConnectCommand)) {
                                account = ((ConnectCommand) command).getAccount();
                                password = ((ConnectCommand) command).getPassword();

                                if (serverType == MANAGER) {
                                    try {
                                        if (!anonAccess) {
                                            userInfo = db.getUserInfo(account);
                                            if (userInfo == null)
                                                accepted = false;
                                            else
                                                //check if client is authorized
                                                accepted = db.checkAutorization(userInfo.userID, password);
                                        } else if (!account.equals("")) {
                                            userInfo = db.getUserInfo("guest");
                                            password = "guest";
                                            accepted = true;
                                        }
                                    } catch (SQLException se) {
                                        se.printStackTrace();
                                    }
                                } else {
                                    userInfo = null;
                                    Object value = accessProperties.get(account);

                                    if (value != null && value.toString().equals(password)) {
                                        accepted = true;
                                    }
                                }

                                if (!accepted) {
                                    connection.answer(command, ConnectCommand.REFUSED);
                                    writeLog("Refusing connection from " + connection.getSocket().getInetAddress() + " because of wrong account/password[" + account + "/" + password + "]");                                    
                                    connection.close();
                                    return true;
                                } else {
                                    writeLog("Accepting connection from " + connection.getSocket().getInetAddress());
                                    connection.answer(command, ConnectCommand.GRANTED);
                                }

                                String clientBaseDir = baseDir + "/" + account + "/";
                                ServerChildOPTAS child = null;

                                try {
                                    if (serverType == WORKER) {
                                        child = new WorkerServerChild(connection, ServerOPTAS.this, clientBaseDir);
                                    } else {
                                        child = new ManagementServerChild(connection, ServerOPTAS.this, db, userInfo);
                                    }
                                } catch (SQLException se) {
                                    se.printStackTrace();
                                } catch (IOException ioe) {
                                    ioe.printStackTrace();
                                }

                                writeLog("Spawning new child with basedir " + clientBaseDir);
                                childs.add(child);                                
                                return true;
                            }
                            return false;
                        }
                    }, null, null);


                } catch (SocketException se) {
                    se.printStackTrace();
                    return;
                } catch (IOException ioe){
                    ioe.printStackTrace();
                    return;
                }
            }
        }
    }

    public void removeChild(ServerChildOPTAS child) {
        childs.remove(child);
    }

    public void stop() throws IOException {
        for (ServerChildOPTAS sc : childs) {
            sc.stop();
        }
        writeLog("Server shut down.. Bye!");
        server.close();
    }

    public int getChildCount() {
        return childs.size();
    }

    public int getMaxChildCount() {
        return CHILD_COUNT;
    }

    public void writeLog(String message) {
        DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
        Date date = new Date();
        System.out.println(dateFormat.format(date) + ": " + message);
    }

    public int getRuns() {
        return runs;
    }

    public void setRuns(int aRuns) {
        runs = aRuns;
    }

    public int getMaxRuns() {
        return maxRuns;
    }

    public static void startWorker(final String baseDir, final int port) throws Exception {
        final ServerOPTAS workServer = new ServerOPTAS();

        Thread tWorkerThread = new Thread() {

            @Override
            public void run() {
                try {
                    workServer.startServer(baseDir, null, null, null, null, port, true, ServerOPTAS.WORKER);
                } catch (IOException ioe) {
                    ioe.printStackTrace();
                } catch (InterruptedException ioe) {
                    ioe.printStackTrace();
                } catch (SQLException ioe) {
                    ioe.printStackTrace();
                }
            }
        };
        tWorkerThread.start();
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

                String dbHost = props.getProperty("databaseHost");
                String dbName = props.getProperty("databaseName");
                String dbAccount = props.getProperty("databaseUser");
                String dbPassword = props.getProperty("databasePassword");
                String serverType = props.getProperty("serverType");

                int servType = 0;
                if (serverType.toLowerCase().equals("worker")) {
                    servType = WORKER;
                } else {
                    servType = MANAGER;
                }

                dir = new File(props.getProperty("basedir", dir)).getAbsolutePath();
                port = Integer.parseInt(props.getProperty("port", "" + port));
                anonAccess = Boolean.parseBoolean(props.getProperty("anonaccess", "" + anonAccess));

                //create the server
                ServerOPTAS server = new ServerOPTAS();
                server.startServer(dir, dbHost, dbName, dbAccount, dbPassword, port, anonAccess, servType);

            } catch (IOException ioe) {
                ioe.printStackTrace();
                //server.writeLog("Could not open config file \"" + configFile + "\", using default values");
            }


        } catch (NumberFormatException ex) {
            //Server.writeLog("Failed! (malformed port number)");
            ex.printStackTrace();
        } catch (Exception ex) {
            //Server.writeLog("Failed!");
            ex.printStackTrace();
        }
    }
}
