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

import jams.server.entities.Job;
import jams.server.entities.WorkspaceFileAssociation;
import jams.tools.FileTools;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author christian
 */
public class MultiLinuxProcessManager extends AbstractProcessManager {
    
    Logger logger = Logger.getLogger(MultiLinuxProcessManager.class.getName());
        
    
    
    public MultiLinuxProcessManager(){
        
    }
    
    @Override
    protected Integer getProcessPid(Process process) throws IOException {
        logger.fine("obtaining process id");
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
    public double getLoad(){
        String command[] = {
            "/bin/sh",
            "-c",
            "echo $(uptime | awk '{sub(\",\",\"\",$11); print $11}') / $(cat /proc/cpuinfo | grep processor | wc -l) | bc -l"
        };
        
        BufferedReader reader1 = null, reader2 = null;
        double result = Double.NaN;
        
        try {
            Runtime runtime = Runtime.getRuntime();
            Process getCPULoad = runtime.exec(command);
            
            reader1 = new BufferedReader(new InputStreamReader(getCPULoad.getInputStream()));            
            int code = getCPULoad.waitFor();
            String line = reader1.readLine();            
            if (line != null){
                result = Double.parseDouble(line);
            }            
            if (getCPULoad.getErrorStream().available()>0){
                reader2 = new BufferedReader(new InputStreamReader(getCPULoad.getInputStream()));            
                line = null;
                while( (line = reader2.readLine()) != null){
                    logger.log(Level.SEVERE, line);
                }
            }
        } catch (Exception e) {
            logger.log(Level.SEVERE, e.toString(), e);
            
            return result;
        } finally{
            try{
                if (reader1 != null)
                    reader1.close();
            }catch(IOException ioe){};
            try{
                if (reader2 != null)
                    reader2.close();
            }catch(IOException ioe){};
        }
        
        return result;
    }
    
    @Override
    public ProcessBuilder getProcessBuilder(Job job) throws IOException {                
        String modelFile = FileTools.normalizePath(job.getModelFile().getPath());
        WorkspaceFileAssociation wfa = job.getExecutableFile();
        if (wfa == null)
            return null;
        String runnableFile = FileTools.normalizePath(wfa.getPath());
        
        String command[] = {
            "java",
            "-Xms128M", 
            "-Xmx"+DEFAULT_MAX_MEMORY, 
            "-Dsun.java2d.d3d=false", 
            //"-Xbootclasspath/a:$(find "+"lib -name \\*jar | awk {'sub(/$/,\":\",$1); printf $1'})",
            "-classpath",
            "" + runnableFile + ":lib/*:lib/lib/*",
            //"-jar", 
            //runnableFile, 
            "jamsui.launcher.JAMSui",
            "-c", 
            DEFAULT_JAP_FILE,
            "-n",
            "-m",
            modelFile, 
            ">" + DEFAULT_INFO_LOG + " 2>&1&"};
                
        logger.fine("start process: " + Arrays.toString(command));
        
        return new ProcessBuilder(command);                
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
