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
public class LinuxProcessManager extends AbstractProcessManager {
    
    @Override
    protected Integer getProcessPid(Process process) throws IOException {
        if (process.getClass().getName().equals("java.lang.UNIXProcess")) {
            /* get the PID on unix/linux systems */
            try {
                Field f = process.getClass().getDeclaredField("pid");
                f.setAccessible(true);
                return f.getInt(process);
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

        return new ProcessBuilder(new String[]{command});        
    }
   
    @Override
    public boolean isProcessActive(int pid) throws IOException {
        if (pid == -1) {
            return false;
        }

        try {
            Runtime runtime = Runtime.getRuntime();
            Process killProcess = runtime.exec(new String[]{"kill", "-0", "" + pid});

            int killProcessExitCode = killProcess.waitFor();
            return killProcessExitCode == 0;
        } catch (Exception e) {
            return false;
        }
    }
                
    @Override
    public void killProcess(int pid){
        if (pid == -1) {
            return;
        }

        try {
            Runtime runtime = Runtime.getRuntime();
            Process killProcess = runtime.exec(new String[]{"kill", ""+pid});

            int killProcessExitCode = killProcess.waitFor();
            return;
        } catch (Exception e) {
            return;
        }      
    }        
}
