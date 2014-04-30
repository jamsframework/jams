/*
 * WindowsProcessManager.java
 * Created on 23.04.2014, 18:17:59
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

import com.sun.jna.Pointer;
import com.sun.jna.platform.win32.Kernel32;
import com.sun.jna.platform.win32.WinNT;
import jams.server.entities.File;
import jams.server.entities.Job;
import jams.server.entities.JobState;
import jams.server.entities.Workspace;
import jams.server.entities.WorkspaceFileAssociation;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.nio.file.Path;
import java.util.Collection;
import java.util.Date;
import java.util.Set;
import java.util.TreeSet;
import javax.persistence.EntityManager;
import javax.ws.rs.core.StreamingOutput;

/**
 *
 * @author christian
 */
public class LocalWindowsProcessManager implements ProcessManager {

    private java.io.File getLocalExecDir(Job job){
        return new java.io.File(ApplicationConfig.SERVER_EXEC_DIRECTORY + "/" + job.getWorkspace().getUser().getLogin() + "/" + job.getId() + "/");
    }

    private Integer getPid(Process process) {
        if (process.getClass().getName().equals("java.lang.Win32Process")
                || process.getClass().getName().equals("java.lang.ProcessImpl")) {
            /* determine the pid on windows plattforms */
            try {
                Field f = process.getClass().getDeclaredField("handle");
                f.setAccessible(true);
                long handl = f.getLong(process);

                Kernel32 kernel = Kernel32.INSTANCE; 
                WinNT.HANDLE handle = new WinNT.HANDLE();
                handle.setPointer(Pointer.createConstant(handl));
                return kernel.GetProcessId(handle);
            } catch (Throwable e) {
                return -1;
            }
        }
        return -1;
    }

    @Override
    public Job deploy(Job job) throws IOException {

        WorkspaceBuilder builder = new WorkspaceBuilder();
        java.io.File f = builder.zipWorkspace(job.getWorkspace());

        java.io.File localExecDir = getLocalExecDir(job);
        localExecDir.mkdirs();
        builder.unzip(f, localExecDir);

        String command = "win64_nogui.bat";

        ProcessBuilder pb = new ProcessBuilder(new String[]{"cmd.exe", "/C", command});
        pb.redirectError(new java.io.File(localExecDir, "error.nfo"));
        pb.redirectOutput(new java.io.File(localExecDir, "run.nfo"));

        pb.directory(localExecDir);
        Process process = pb.start();

        job.setPID(this.getPid(process));
        
        job.setStartTime(new Date());
        return job;
    }

    private long folderSize(java.io.File directory) {
        long length = 0;
        for (java.io.File file : directory.listFiles()) {
            if (file.isFile()) {
                length += file.length();
            } else {
                length += folderSize(file);
            }
        }
        return length;
    }

    @Override
    public JobState state(Job job) throws IOException {
        int pid = job.getPID();
        boolean pidWasFound = false;

        if (pid != -1) {
            ProcessBuilder pb = new ProcessBuilder(new String[]{"tasklist.exe", "/FI", "\"PID eq " + pid + "\""});
            Process proc = pb.start();
            InputStreamReader inputstreamreader = new InputStreamReader(proc.getInputStream());
            BufferedReader reader = new BufferedReader(inputstreamreader);
            String line;
            try {
                if (0 == proc.waitFor()) {
                    while ((line = reader.readLine()) != null) {
                        if (line.toLowerCase().contains("cmd") && line.contains(Integer.toString(pid))) {
                            pidWasFound = true;
                        }
                    }
                } else {
                    inputstreamreader = new InputStreamReader(proc.getErrorStream());
                    reader = new BufferedReader(inputstreamreader);
                    while ((line = reader.readLine()) != null) {
                        System.out.println(line);
                    }

                }
            } catch (InterruptedException ie) {

            }
        }

        JobState state = new JobState();
        state.setActive(pidWasFound);
        state.setStartDate(job.getStartTime());
        state.setDuration((new Date()).getTime() - job.getStartTime().getTime());
        state.setJob(job);

        state.setSize(folderSize(getLocalExecDir(job)));
        return state;
    }
    
    private Collection<java.io.File> listFileTree(java.io.File dir) {
        Set<java.io.File> fileTree = new TreeSet<java.io.File>();
        for (java.io.File entry : dir.listFiles()) {
            if (entry.isFile()) fileTree.add(entry);
            else fileTree.addAll(listFileTree(entry));
        }
    return fileTree;
}
    
    private Workspace updateWorkspace(Job job, Workspace ws, EntityManager em){
        java.io.File dir = getLocalExecDir(job);
        Path wsPath = dir.toPath();
        if (dir.isDirectory()){
            Collection<java.io.File> files = listFileTree(dir);            
            for (java.io.File file : files){
                Path filePath = file.toPath();                
                String relPath = filePath.relativize(wsPath).toString();
                
                if (!ws.containsFile(relPath)){
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
        try{
            Runtime.getRuntime().exec("taskkill /F /PID " + job.getPID()).wait(2000);
        }catch(InterruptedException ie){
            ie.printStackTrace();
        }
        return this.state(job);
    }
    
    public StreamingOutput streamInfoLog(Job job) throws IOException{
        java.io.File file = new java.io.File(ApplicationConfig.SERVER_EXEC_DIRECTORY + "/" + job.getWorkspace().getUser().getLogin() + "/" + job.getId() + "/" + "info.log");
        WorkspaceBuilder wb = new WorkspaceBuilder();
        return wb.streamFile(file);
    }
    
    public StreamingOutput streamErrorLog(Job job) throws IOException{
        java.io.File file = new java.io.File(ApplicationConfig.SERVER_EXEC_DIRECTORY + "/" + job.getWorkspace().getUser().getLogin() + "/" + job.getId() + "/" + "error.log");
        WorkspaceBuilder wb = new WorkspaceBuilder();
        return wb.streamFile(file);
    }
}
