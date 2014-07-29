/*
 * UserController.java
 * Created on 20.04.2014, 14:46:13
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
package jams.server.client;

import jams.JAMS;
import jams.server.entities.Job;
import jams.server.entities.JobState;
import jams.server.entities.Jobs;
import jams.server.entities.Workspace;
import jams.server.entities.WorkspaceFileAssociation;
import jams.tools.FileTools;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author christian
 */
public class JobController {
    private static final Logger log = Logger.getLogger( Controller.class.getName() );
    
    Controller ctrl = null;
    FileController fileController = null;

    public JobController(Controller ctrl) {
        this.ctrl = ctrl;

        fileController = ctrl.getFileController();
    }
    
    public Job create(Workspace ws, WorkspaceFileAssociation wfa) {
        log.fine(JAMS.i18n("creating_job_for_workspace%1_and_model%2")
                .replace("%1", "\n" + ws.getName() + "\n")
                .replace("%2", wfa.getPath()));                
        return (Job) ctrl.getClient().httpGet(ctrl.getServerURL() + "/job/create?workspace=" + ws.getId() + "&file=" + wfa.getFile().getId(), Job.class);
    }
    
    public Jobs find() {
        log.fine(JAMS.i18n("retrieving_jobs_of_user"));
        return (Jobs) ctrl.getClient().httpGet(ctrl.getServerURL() + "/job/find", Jobs.class);
    }
    
    public Jobs getActiveJobs() {
        log.fine(JAMS.i18n("retrieving_active_jobs_of_user"));        
        return (Jobs) ctrl.getClient().httpGet(ctrl.getServerURL() + "/job/findActive", Jobs.class);
    }
    
    public Jobs findAll() {
        log.fine(JAMS.i18n("retrieving_all_jobs"));
        if (ctrl.user.getAdmin()==0){
            log.severe(JAMS.i18n("operation_denied_since_user_is_not_an_admin"));
            return null;
        }
        return (Jobs) ctrl.getClient().httpGet(ctrl.getServerURL() + "/job/findAll", Jobs.class);
    }
    
    public JobState getState(Job job) {
        log.fine(JAMS.i18n("getting_state_of_job") + " " + job.getId());
        return (JobState) ctrl.getClient().httpGet(ctrl.getServerURL() + "/job/" + job.getId() + "/state", JobState.class);
    }
    
    public JobState kill(Job job) {
        log.fine(JAMS.i18n("killing_job") + " " + job.getId());
        return (JobState) ctrl.getClient().httpGet(ctrl.getServerURL() + "/job/" + job.getId() + "/kill", JobState.class);
    }
    
    public Job remove(Job job) {
        log.fine(JAMS.i18n("deleting_job") + " " + job.getId());
        return (Job) ctrl.getClient().httpGet(ctrl.getServerURL() + "/job/" + job.getId() + "/delete", Job.class);
    }
    
    public void removeAll() {        
        ctrl.getClient().httpGet(ctrl.getServerURL() + "/job/reset", String.class);
        return;
    }
        
    public String getInfoLog(Job job, int offset, int size) {
        log.fine(JAMS.i18n("retrieving_info_log_stream_of_job") + " " + job.getId());
        InputStream is = ctrl.getClient().getStream(ctrl.getServerURL() + "/job/" + job.getId() + "/infolog");
        try {
            return FileTools.streamToString(is, offset, size);
        } catch (IOException ioe) {
            log.log(Level.SEVERE, ioe.toString(), ioe);
        } finally {
            try {
                is.close();
            } catch (IOException ioe) {
            }
        }
        return null;
    }
    
    public String getErrorLog(Job job, int offset, int size) {
        log.fine(JAMS.i18n("retrieving_error_log_stream_of_job") + " " + job.getId());
        InputStream is = ctrl.getClient().getStream(ctrl.getServerURL() + "/job/" + job.getId() + "/errorlog");
        try {
            return FileTools.streamToString(is, offset, size);
        } catch (IOException ioe) {
            log.log(Level.SEVERE, ioe.toString(), ioe);
        } finally {
            try {
                is.close();
            } catch (IOException ioe) {
            }
        }
        return null;        
    }   
    
    public File downloadWorkspace(File target, Job job) {
        log.fine(JAMS.i18n("downloading_workspace_of_job") + " " + job.getId());
        Workspace ws = (Workspace)ctrl.getClient().httpGet(ctrl.getServerURL() + "/job/"+job.getId()+ "/refresh/", Workspace.class);
                
        return ctrl.getWorkspaceController().downloadWorkspace(target, ws);
    }
}
