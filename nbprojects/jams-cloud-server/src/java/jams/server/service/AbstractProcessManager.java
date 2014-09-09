/*
 * ProcessManager.java
 * Created on 23.04.2014, 18:11:51
 *
 * This file is part of JAMS
 * Copyright (C) FSU Jena
 *
 * JAMS is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public License
 * as published by the Free Software Foundation; either version 3
 * of the License, or (at your option) any later version.
 *
 * JAMS is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with JAMS. If not, see <http://www.gnu.org/licenses/>.
 *
 */

package jams.server.service;

import jams.server.entities.File;
import jams.server.entities.Job;
import jams.server.entities.JobState;
import jams.server.entities.Workspace;
import jams.server.entities.WorkspaceFileAssociation;
import jams.tools.FileTools;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Collection;
import java.util.Date;
import javax.persistence.EntityManager;
import javax.ws.rs.core.StreamingOutput;

/**
 *
 * @author christian
 */
public abstract class AbstractProcessManager implements ProcessManager {    
    final protected String DEFAULT_JAP_FILE = "default.jap";
    final protected String DEFAULT_INFO_LOG = "info.log";
    final protected String DEFAULT_ERROR_LOG = "error.log";
    
    final protected String DEFAULT_MAX_MEMORY = "24g";
    
    protected abstract Integer getProcessPid(Process process) throws IOException;
    public abstract boolean isProcessActive(int pid) throws IOException;
    public abstract ProcessBuilder getProcessBuilder(Job job) throws IOException;
    
    
    private java.io.File getLocalExecDir(Job job){
        return new java.io.File(ApplicationConfig.SERVER_EXEC_DIRECTORY + "/" + job.getWorkspace().getUser().getLogin() + "/" + job.getId() + "/");
    }
    
    private void deleteDirectory(java.io.File f){
        for (java.io.File sub : f.listFiles()){
            if (sub.isDirectory()){
                deleteDirectory(sub);
            }else{
                sub.delete();
            }
        }
        f.delete();
    }
    
    @Override
    public Job deploy(Job job) throws IOException{
        Workspace ws = job.getWorkspace();
        java.io.File target = new java.io.File(ApplicationConfig.SERVER_TMP_DIRECTORY + "/workspace_" + ws.getId());
        java.io.File f = Utilities.zipWorkspace(target, ws);

        java.io.File localExecDir = getLocalExecDir(job);
        localExecDir.mkdirs();
        
        FileTools.unzipFile(f, localExecDir, true);

        //delete tmp directory
        deleteDirectory(target);
        
        ProcessBuilder pb = getProcessBuilder(job);
        if (pb == null){
            return null;
        } 
        BufferedWriter writer = null;
        try {
            writer = new BufferedWriter(new FileWriter(new java.io.File(localExecDir,"/start.sh")));
            for (String cmdString : pb.command()) {
                writer.write(cmdString + " ");
            }
            
        } catch (Throwable t) {

        } finally{
            try {
                writer.close();
            }catch (Throwable t) {}
        }
        pb.directory(localExecDir);
        pb.redirectOutput(new java.io.File(localExecDir + "/" + DEFAULT_INFO_LOG));
        pb.redirectError(new java.io.File(localExecDir + "/" + DEFAULT_ERROR_LOG));
        Process process = pb.start();

        job.setPID(this.getProcessPid(process));        
        job.setStartTime(new Date());
        try{
        }finally{
            
        }
        return job;
    }
    
    
    @Override
    public JobState state(Job job) throws IOException {
        int pid = job.getPID();
        boolean pidWasFound = isProcessActive(pid);
        
        JobState state = new JobState();
        state.setActive(pidWasFound);
        state.setStartDate(job.getStartTime());
        state.setDuration((new Date()).getTime() - job.getStartTime().getTime());
        state.setJob(job);
        state.setSize(FileTools.getDirectorySize(getLocalExecDir(job)));
        return state;
    }
    
    private Workspace updateWorkspace(Job job, Workspace ws, EntityManager em){
        java.io.File dir = getLocalExecDir(job);
        Path wsPath = dir.toPath();
        ws.detachAllFiles();
        
        if (dir.isDirectory()){
            Collection<java.io.File> files = FileTools.getFilesByRegEx(dir, null, true);
            for (java.io.File file : files){
                Path filePath = file.toPath();                
                String relPath = wsPath.relativize(filePath).toString();
                //änderungen nachverfolgen .. 
                if (ws.getFile(relPath)==null){
                    File dbFile = new File();
                    dbFile.setHash("0");
                    dbFile.setLocation(filePath.toString());
                    em.persist(dbFile);
                    em.flush();
                    em.refresh(dbFile);
                    
                    ws.assignFile(dbFile, WorkspaceFileAssociation.ROLE_OUTPUT , relPath);
                }
            }            
        }
        return ws;
    }
    
    @Override
    public Workspace updateWorkspace(Job job, EntityManager em){
        Workspace ws = job.getWorkspace();
        if (ws == null){
            ws = job.getWorkspace();
            ws.setId(0);                        
        }
        ws = updateWorkspace(job, ws, em);
        em.persist(ws);
        em.flush();
        em.refresh(ws);
        return ws;
    }
    
    @Override
    public JobState kill(Job job) throws IOException {
        if (job.getPID()==-1){
            return null;
        }
        JobState jobState = this.state(job);
        if (!jobState.isActive()){
            return jobState;
        }
        killProcess(job.getPID());
        return this.state(job);
    }
    
    abstract protected void killProcess(int pid);
    
    @Override
    public StreamingOutput streamInfoLog(Job job) throws IOException{
        java.io.File file = new java.io.File(getLocalExecDir(job) + "/" + DEFAULT_INFO_LOG);
        if (file.exists())
            return Utilities.streamFile(file);
        return null;
    }
    
    @Override
    public StreamingOutput streamErrorLog(Job job) throws IOException{
        java.io.File file = new java.io.File(getLocalExecDir(job) + "/" + DEFAULT_ERROR_LOG);
                if (file.exists())
            return Utilities.streamFile(file);
        return null;
    }        
}
