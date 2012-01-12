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

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.Serializable;
import java.lang.management.ManagementFactory;
import java.sql.SQLException;
import java.util.ArrayList;
import optas.remote.common.ByteStream;
import optas.remote.common.Commands.GetFileCommand;
import optas.remote.common.Commands.GetFileListCommand;
import optas.remote.common.FileInfo;
import optas.remote.common.JAMSCommand;
import optas.remote.common.JAMSConnection;
import optas.remote.common.Protocol;

/**
 *
 * @author C. Fischer
 */
public class WorkerServerChild extends ServerChildOPTAS {

    private String hostName="?";

    public WorkerServerChild(JAMSConnection client, ServerOPTAS server, String baseDir) throws SQLException, IOException{
        super(client,server);
        this.baseDir = baseDir;

        client.setHandler(new JAMSConnection.CommandHandler() {
            @Override
            public boolean handle(JAMSConnection connection, JAMSCommand command) {
                return WorkerServerChild.this.handle(command);
            }
        });
    }

    private ArrayList<FileInfo> recursiveGetFileList(File dir, String subPath, File workspace){
        ArrayList<FileInfo> list = new ArrayList<FileInfo>();
        if (!dir.exists())
            return new ArrayList<FileInfo>();

        for (File f : dir.listFiles()){
            if (f.isDirectory())
                list.addAll(recursiveGetFileList(f, subPath + "/" + f.getName(), workspace));
            else if (f.isFile()){
                FileInfo fi = new FileInfo();
                fi.setHost(hostName);
                fi.setName(subPath + "/" + f.getName());
                fi.setPath(workspace.getAbsolutePath());
                fi.setSize(f.length());
                list.add(fi);
            }
        }
        return list;
    }

    private byte[] getFile(File f) throws IOException{
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
    public boolean handle(JAMSCommand cmd){
        if (cmd instanceof GetFileListCommand){
            int jobId = ((GetFileListCommand)cmd).getJobId();
            Serializable fileList = null;
            try{
                File workspace = new File(baseDir + "/" + jobId);
                fileList = (Serializable) recursiveGetFileList(workspace, "", workspace);
            }catch(Throwable t){
                t.printStackTrace();
                this.connection.answer(cmd, t);
                return true;
            }
            this.connection.answer(cmd, fileList);
            return true;
        }
        else if (cmd instanceof GetFileCommand){
            int jobId = ((GetFileCommand)cmd).getJobId();
            String subPath = ((GetFileCommand)cmd).getSubPath();

            File f = new File(baseDir + "/" + jobId + "/" + subPath);

            Serializable file = null;
            try{
                file = (Serializable) getFile(f);
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
    private void runJAMS(String workspaceDir, String modelFilename, String debug) throws IOException {       
        workspaceDir = baseDir + workspaceDir;
        modelFilename = baseDir + modelFilename;
        String libDir = baseDir + "/lib/";
        
        if (client.isClosed()) {
            throw new IOException("Couldn't exec JAMS because client is closed!");
        }
        
        server.writeLog("Executing JAMS: " + workspaceDir + " - " + libDir);

        String cmd = "java";
        String dOption = "-Djava.ext.dirs=" + libDir;
        String memOption1 = "-Xms200m";
        String memOption2 = "Xmx4000m";
        String jarOption  = "-jar";
        String jarFile = workspaceDir + "/jams-start.jar";
        String optionC = "-c";
        String propertyFile = workspaceDir + "/../../default.jap";
        String optionM = "-m";
        String modelFile = workspaceDir + "/optimization.jam";

        //check number of processes running for user
        ProcessBuilder pb = new ProcessBuilder(cmd, dOption, memOption1, memOption2, jarOption, jarFile, optionC, propertyFile, optionM, modelFile);
        Process p = pb.start();

        System.out.println(ManagementFactory.getRuntimeMXBean().getName());
    }

    private void configureModel(String workspaceDir, String modelFilename) throws IOException {
        workspaceDir = baseDir + workspaceDir;
        modelFilename = baseDir + modelFilename;
        String libDir = baseDir + "/lib/";

        if (client.isClosed()) {
            throw new IOException("Couldn't exec JAMS because client is closed!");
        }

        server.writeLog("Executing Model Modification Process: " + workspaceDir + " - " + libDir);

        String cmd = "java";
        String dOption = "-Djava.ext.dirs=" + libDir;
        String memOption1 = "-Xms600m";
        String memOption2 = "Xmx600m";
        String jarOption  = "-jar";
        String jarFile = workspaceDir + "/jams-ui.jar";
        String modeOption = "modify";
        String modelFile = workspaceDir + "/" + modelFilename;
        String propertyFile = workspaceDir + "/../../default.jap";
        String optimizationFile = workspaceDir + "/schema.odd";
        String workspace = workspaceDir;
        //check number of processes running for user
        ProcessBuilder pb = new ProcessBuilder(cmd, dOption, memOption1, memOption2, jarOption, jarFile, modeOption, modelFile, propertyFile, optimizationFile, workspace);
        Process p = pb.start();

        System.out.println(ManagementFactory.getRuntimeMXBean().getName());
    }
    
    private void getModelLog(String fileName, int offset) throws IOException {
        if (client.isClosed()) {
            server.writeLog("Couldn't get log data because client is closed!");
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
        
    /*

    @Override
    public boolean handle(InputStream inStream, int input) throws IOException, SQLException {
        switch (input) {
            case Protocol.JAMS_RUN:{

                String workspaceDir = ByteStream.toString(inStream);
                String modelFilename = ByteStream.toString(inStream);

                WorkerServerChild.this.runJAMS(workspaceDir, modelFilename, "true");

                return true;
            }
            case Protocol.GET_FILELIST:{
                
                Long jobId = (Long) ByteStream.toObject(inStream);

                File workspace = new File(baseDir + "/" + jobId);

                ByteStream.toStream(client.getOutputStream(), (Object) recursiveGetFileList(workspace, "", workspace));
                ByteStream.toStream(client.getOutputStream(), Protocol.SUCCESS);

                return true;
            }
            case Protocol.GET_FILE:{                
                Long jobId = (Long) ByteStream.toObject(inStream);
                String file = (String) ByteStream.toObject(inStream);

                File f = new File(baseDir + "/" + jobId + "/" + file);
                try{
                    ByteStream.toStream(client.getOutputStream(), f);
                    ByteStream.toStream(client.getOutputStream(), Protocol.SUCCESS);
                }catch(Exception e){
                    e.printStackTrace();
                    ByteStream.toStream(client.getOutputStream(), Protocol.ERROR);
                }
                return true;
            }
            case Protocol.GET_BASE_DIR:{

                ByteStream.toStream(client.getOutputStream(), baseDir);
                return true;
            }
            case Protocol.GET_MODEL_INFO_LOG:{

                String workspaceDir = baseDir + ByteStream.toString(inStream);
                int offset = ByteStream.toInt(inStream);
                getModelLog(workspaceDir + "/$info.log", offset);
                return true;
            }
            case Protocol.GET_MODEL_ERROR_LOG:

                String workspaceDir = baseDir + ByteStream.toString(inStream);
                int offset = ByteStream.toInt(inStream);
                getModelLog(workspaceDir + "/$error.log", offset);
                return true;

            case Protocol.CONFIGURE_MODEL:{
                String workspace = ByteStream.toString(inStream);
                String modelFile = ByteStream.toString(inStream);
                WorkerServerChild.this.configureModel(workspace, modelFile);

                return true;
            }
        }
        if (super.handle(inStream, input))
            return true;
        return false;
    }*/
}
