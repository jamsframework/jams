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
import jams.server.entities.JobState;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 * @author christian
 */
public class LocalWindowsProcessManager implements ProcessManager {

    private static Integer tryPattern(String processName) {
        Integer result = null;

        /* tested on: */
        /* - windows xp sp 2, java 1.5.0_13 */
        /* - mac os x 10.4.10, java 1.5.0 */
        /* - debian linux, java 1.5.0_13 */
        /* all return pid@host, e.g 2204@antonius */
        Pattern pattern = Pattern.compile("^([0-9]+)@.+$", Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(processName);
        if (matcher.matches()) {
            result = new Integer(Integer.parseInt(matcher.group(1)));
        }
        return result;
    }

    @Override
    public Job deploy(Job job) throws IOException {

        WorkspaceBuilder builder = new WorkspaceBuilder();
        java.io.File f = builder.zipWorkspace(job.getWorkspace());

        java.io.File localExecDir = new java.io.File(ApplicationConfig.SERVER_EXEC_DIRECTORY + "/" + job.getWorkspace().getUser().getLogin() + "/" + job.getId() + "/");
        localExecDir.mkdirs();
        builder.unzip(f, localExecDir);

        String command = "win64_nogui.bat";

        ProcessBuilder pb = new ProcessBuilder(new String[]{"cmd.exe", "/C", command});
        pb.redirectError(new java.io.File(localExecDir, "error.nfo"));
        pb.redirectOutput(new java.io.File(localExecDir, "run.nfo"));

        pb.directory(localExecDir);
        Process process = pb.start();

        job.setPID(-1);
        try {
            RuntimeMXBean rtb = ManagementFactory.getRuntimeMXBean();
            String processName = rtb.getName();
            Integer pid = tryPattern(processName);
            job.setPID(pid);
        } catch (Throwable t) {
            t.printStackTrace();
        }
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
                        if (line.toLowerCase().contains("java") && line.contains(Integer.toString(pid))) {
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

        state.setSize(folderSize(new java.io.File(ApplicationConfig.SERVER_EXEC_DIRECTORY + "/" + job.getWorkspace().getUser().getLogin() + "/" + job.getId() + "/")));
        return state;
    }
}
