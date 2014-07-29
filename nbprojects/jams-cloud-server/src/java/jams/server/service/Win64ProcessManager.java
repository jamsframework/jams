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
import jams.server.entities.Job;
import jams.server.entities.WorkspaceFileAssociation;
import jams.tools.FileTools;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Field;

/**
 *
 * @author christian
 */
public class Win64ProcessManager extends AbstractProcessManager {
    
    @Override
    protected Integer getProcessPid(Process process) throws IOException{
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
    public ProcessBuilder getProcessBuilder(Job job) throws IOException {

        String modelFile = FileTools.normalizePath(job.getModelFile().getPath());
        WorkspaceFileAssociation wfa = job.getExecutableFile();
        if (wfa == null)
            return null;
        String runnableFile = FileTools.normalizePath(wfa.getPath());
        
        String command = "java -Xms128M -Xmx2096M -Dsun.java2d.d3d=false -jar " + runnableFile + " -c " + DEFAULT_JAP_FILE + " -n -m " + modelFile + ">" + DEFAULT_INFO_LOG + " 2>&1&";

        return new ProcessBuilder(new String[]{"cmd.exe", "/C", command});        
    }
   
    @Override
    public boolean isProcessActive(int pid) throws IOException {
        if (pid == -1) {
            return false;
        }
        
        ProcessBuilder pb = new ProcessBuilder(new String[]{"tasklist.exe", "/FI", "\"PID eq " + pid + "\""});
        Process proc = pb.start();
        InputStreamReader inputstreamreader = new InputStreamReader(proc.getInputStream());
        BufferedReader reader = new BufferedReader(inputstreamreader);
        String line;
        try {
            if (0 == proc.waitFor()) {
                while ((line = reader.readLine()) != null) {
                    if (line.toLowerCase().contains("cmd") && line.contains(Integer.toString(pid))) {
                        return true;
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

        return false;
    }
                
    @Override
    public void killProcess(int pid){
        try{
            Runtime.getRuntime().exec("taskkill /F /PID " + pid).wait(2000);
        }catch(  InterruptedException | IOException ie){
            ie.printStackTrace();
        }        
    }        
}
