/*
 * ServerChild.java
 * Created on 31. Mai 2007, 14:50
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


import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.beans.ExceptionListener;
import java.beans.XMLDecoder;
import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.Serializable;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import optas.remote.client.ClientOPTAS;
import optas.remote.common.ByteStream;
import optas.remote.common.Commands.GetFileCommand;
import optas.remote.common.Commands.GetFileListCommand;
import optas.remote.common.Commands.GetJobListCommand;
import optas.remote.common.Database;
import optas.remote.common.Database.UserInfo;
import optas.remote.common.FileInfo;
import optas.remote.common.HostInfo;
import optas.remote.common.JAMSCommand;
import optas.remote.common.JAMSConnection;
import optas.remote.common.JobState;
import optas.remote.common.Protocol;

/**
 *
 * @author S. Kralisch
 */
public class ManagementServerChild extends ServerChildOPTAS {

    private UserInfo userInfo;
    private Database db;
    static HostInfo hosts[] = null;
    HashMap<HostInfo, ClientOPTAS> hostMap = new HashMap<HostInfo, ClientOPTAS>();

    public ManagementServerChild(JAMSConnection client, ServerOPTAS server, Database db, UserInfo userInfo) throws SQLException, FileNotFoundException, IOException {
        super(client, server);

        this.baseDir = System.getProperty("java.io.tmpdir");
        this.db = db;
        this.userInfo = userInfo;

        server.writeLog("loading workers.xml");
        File inFile = new File("workers.xml");
        XMLDecoder encoder = new XMLDecoder(
                new BufferedInputStream(
                new FileInputStream(inFile)));
        encoder.setExceptionListener(new ExceptionListener() {
            public void exceptionThrown(Exception e) {
                ManagementServerChild.this.server.writeLog("unable to load hosts.xml!");
            }
        });
        hosts = (HostInfo[])encoder.readObject();
        server.writeLog("workers:");
        for (int i=0;i<hosts.length;i++){
            server.writeLog(hosts[i].getHost() + ":" + hosts[i].getPort());
        }
        client.setHandler(new JAMSConnection.CommandHandler() {
            @Override
            public boolean handle(JAMSConnection connection, JAMSCommand command) {
                return ManagementServerChild.this.handle(command);
            }
        });
        //hosts.add(new HostInfo("localhost", "9001", "admin", "borlandc"));
    }

    private File getTmpFile() {
        return new File(baseDir + "/" + System.currentTimeMillis());
    }
    
    private ClientOPTAS findHost(String name) {
        ClientOPTAS hostClient = null;
        
        for (int i=0;i<hosts.length;i++){
            if (hosts[i].getHost().equals(name)){
                hostClient = this.hostMap.get(hosts[i]);
                if (hostClient == null || hostClient.isClosed()){
                    hostClient = new ClientOPTAS( hosts[i].getHost(), Integer.parseInt(hosts[i].getPort()), userInfo.userName, userInfo.password);
                    if (hostClient.connect())
                        hostMap.put(hosts[i], hostClient);
                    else
                        return null;
                }

            }
        }
        return hostClient;
    }

    private ArrayList<JobState> getJobList() throws SQLException {
        return db.getJobList(this.userInfo);
    }

    private JobState getJob(long jobId) throws SQLException {
        ArrayList<JobState> jobList = db.getJobList(this.userInfo);
        for (JobState job : jobList) {
            if (job.getId() == jobId) {
                return job;
            }
        }
        return null;
    }

    private ArrayList<FileInfo> getFileList(long jobId) throws SQLException, IOException {
        JobState job = getJob(jobId);
        String host = job.getHost();
        ClientOPTAS hostClient = findHost(host);
        if (hostClient == null)
            throw new IOException("Failed to connect with worker!");

        ArrayList<FileInfo> fileList = hostClient.getFileListing(job);
        for (FileInfo fi : fileList){
            fi.setHost(host);
            fi.setJob(jobId);
        }
        return fileList;
    }

    private byte[] getFile(long jobId, String fileName) throws SQLException, IOException {
        System.out.print("Receiving file for job" + jobId + " with name " + " from ");

        JobState job = getJob(jobId);
        if (job == null)
            return null;
        String host = job.getHost();
        System.out.println(host);

        ClientOPTAS hostClient = findHost(host);
        if (hostClient != null)
            System.out.println("Client found!");

        File f = getTmpFile();
        System.out.println("Saving file to" + f);
        hostClient.getFile(f, (int)jobId, fileName);

        if (!f.exists())
            return null;
        BufferedInputStream bis = new BufferedInputStream(new FileInputStream(f));
        if (f.length()>Integer.MAX_VALUE){
            throw new IOException("file length exceeds 2gb");
        }
        byte data[] = new byte[(int)f.length()];
        bis.read(data,0,(int)f.length());
        bis.close();
        return data;
    }

    @Override
    public boolean handle(JAMSCommand cmd) {
        if (cmd instanceof GetJobListCommand){
            Serializable jobList = null;
            try{
                jobList = (Serializable) getJobList();
            }catch(Throwable t){
                t.printStackTrace();
                this.connection.answer(cmd, t);
                return true;
            }
            this.connection.answer(cmd, jobList);
            return true;
        }else if (cmd instanceof GetFileListCommand){
            int jobId = ((GetFileListCommand)cmd).getJobId();
            Serializable fileList = null;
            try{
                fileList = (Serializable) getFileList(jobId);
            }catch(Throwable t){
                t.printStackTrace();
                this.connection.answer(cmd, t);
                return true;
            }
            this.connection.answer(cmd, fileList);
            return true;
        }else if (cmd instanceof GetFileCommand){
            int jobId = ((GetFileCommand)cmd).getJobId();
            String subPath = ((GetFileCommand)cmd).getSubPath();
            
            Serializable file = null;
            try{
                file = (Serializable) getFile(jobId, subPath);
            }catch(Throwable t){
                t.printStackTrace();
                this.connection.answer(cmd, t);
                return true;
            }
            this.connection.answer(cmd, file);
            return true;
        }
        return super.handle(cmd);
    }

    /*
    @Override
    public boolean handle(InputStream inStream, int input) throws IOException, SQLException {
        switch (input) {
            case Protocol.GET_JOBLIST:
                try {
                    ByteStream.toStream(client.getOutputStream(), (Object) getJobList());
                    ByteStream.toStream(client.getOutputStream(), Protocol.SUCCESS);
                } catch (SQLException sqlEx) {
                    ByteStream.toStream(client.getOutputStream(), Protocol.ERROR);
                    this.server.writeLog(sqlEx.toString());
                    sqlEx.printStackTrace();
                }
                return false;
            case Protocol.GET_FILELIST:
                try {
                    Long jobId = (Long) ByteStream.toObject(inStream);
                    ByteStream.toStream(client.getOutputStream(), (Object) getFileList(jobId));
                    ByteStream.toStream(client.getOutputStream(), Protocol.SUCCESS);
                } catch (SQLException sqlEx) {
                    ByteStream.toStream(client.getOutputStream(), Protocol.ERROR);
                    this.server.writeLog(sqlEx.toString());
                    sqlEx.printStackTrace();
                }
                return false;
            case Protocol.GET_FILE:
                try {
                    Long jobId = (Long) ByteStream.toObject(inStream);
                    String fileName = (String) ByteStream.toObject(inStream);
                    try{
                        ByteStream.toStream(client.getOutputStream(), getFile(jobId, fileName));
                        ByteStream.toStream(client.getOutputStream(), Protocol.SUCCESS);
                    }catch(Exception e){
                        e.printStackTrace();
                        ByteStream.toStream(client.getOutputStream(), Protocol.ERROR);
                    }
                } catch (Exception e) {

                }
            case Protocol.GET_RUN_COUNT:

                //ByteStream.toStream(client.getOutputStream(), Server.getChildCount());
                ByteStream.toStream(client.getOutputStream(), server.getRuns());
                return false;

            case Protocol.GET_MAX_RUN_COUNT:

                //ByteStream.toStream(client.getOutputStream(), Server.getMaxChildCount());
                ByteStream.toStream(client.getOutputStream(), server.getMaxRuns());
                return false;

            case Protocol.MANAGEMENT_RUN_MODEL:
                String zipArchive = ByteStream.toString(inStream);
                String modelFile = ByteStream.toString(inStream);
                String useLib = ByteStream.toString(inStream);

                runJAMS(zipArchive, modelFile, useLib);
                return true;
        }
        if (super.handle(inStream, input)) {
            return true;
        }
        return false;
    }*/














    //    connector = new JdbcSQLConnector(db., "optas", "root", "KlroF7G6fN", "jdbc:mysql");

    public void runJAMS(String zipArchive, String modelName, String useStdLibraries) throws SQLException, IOException {
        if (client.isClosed()) {
            server.writeLog("Couldn't run model, because client is closed!");
            return;
        }

        File tmpFile = new File(baseDir + "/" + System.currentTimeMillis() + ".zip");

        InputStream inStream = client.getInputStream();

        int result = Protocol.ERROR;
        try {
            ByteStream.toFile(inStream, tmpFile);
            result = Protocol.SUCCESS;
            server.writeLog("Received " + tmpFile);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        ByteStream.toStream(client.getOutputStream(), result);

        //get next id from db
        int jobID = this.db.getNextID(this.userInfo.userID);

        //search host
        ClientOPTAS clientForHost = findHost();
        clientForHost.connect();
        clientForHost.extractWorkspace(this.userInfo.userName, Integer.toString(jobID), tmpFile);

        if (useStdLibraries.toLowerCase().equals("true")) {
            clientForHost.copyLibs(this.userInfo.userName, Integer.toString(jobID));
        }

        clientForHost.configureModel(userInfo.userName, Integer.toString(jobID), modelName);
        clientForHost.runJAMS(userInfo.userName, Integer.toString(jobID));
    }

    private ClientOPTAS findHost() {
        HostInfo info = hosts[0];

        ClientOPTAS hostClient = new ClientOPTAS( info.getHost(), Integer.parseInt(info.getPort()), info.getUser(), info.getPw());
        return hostClient;
    }

    

    

    

    

    

    
}
