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
import static jams.tools.LogTools.log;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.logging.Level;
import javax.ws.rs.ProcessingException;

/**
 *
 * @author Christian Fischer <christian.fischer.2@uni-jena.de>
 */
public class JobController {

    private final Controller ctrl;
    private final HTTPClient client;
    private final String urlStr;

    /**
     * ensures the construction of a working JobController
     *
     * @param ctrl the parent controller
     */
    public JobController(Controller ctrl) {
        this.ctrl = ctrl;
        this.client = ctrl.getClient();
        this.urlStr = ctrl.getServerURL();
    }

    /**
     * creates a new job from a workspace and a model file
     *
     * @param ws the workspace from which the job is created
     * @param wfa the model file
     * @return the new Job
     */
    public Job create(Workspace ws, WorkspaceFileAssociation wfa) {
        log(this.getClass(),
                Level.FINE, JAMS.i18n("creating_job_from_workspace_{0}_with_model_{1}"),
                ws.getName(), wfa.getFileName());
        return client.httpGet(urlStr + "/job/create?workspace=" + ws.getId() + "&file=" + wfa.getFile().getId(), Job.class);
    }

    /**
     * finds all jobs associated with the user
     *
     * @return the jobs of the user
     */    
    public Jobs find() {
        log(this.getClass(), Level.FINE, JAMS.i18n("retrieving_jobs_of_user"));
        return client.httpGet(urlStr + "/job/find", Jobs.class);
    }
        
    /**
     * finds all jobs associated with the user which are running
     *
     * @return the active jobs of the user
     */  
    public Jobs findActive() {
        log(this.getClass(), Level.FINE, JAMS.i18n("retrieving_active_jobs_of_user"));
        return client.httpGet(urlStr + "/job/findActive", Jobs.class);
    }

     /**
     * finds all jobs of all users, requires administration rights
     *
     * @return the jobs of all user
     */  
    public Jobs findAll() {
        log(this.getClass(), Level.FINE, JAMS.i18n("retrieving_all_jobs"));
        if (ctrl.getUser().getAdmin() == 0) {
            throw new ProcessingException(JAMS.i18n("operation_denied_since_user_is_not_an_admin"));
        }
        return client.httpGet(urlStr + "/job/findAll", Jobs.class);
    }
     /**
     * retrieves the state of a job
     *
     * @param job the job in question
     * @return the jobs state
     */  
    public JobState getState(Job job) {
        log(this.getClass(), Level.FINE, JAMS.i18n("getting_the_state_of_job:{0}"), job.getId());
        return client.httpGet(urlStr + "/job/" + job.getId() + "/state", JobState.class);
    }

    /**
     * stops execution of a job
     * @param job in question
     * @return the state of the job which was killed
     */
    public JobState kill(Job job) {
        log(this.getClass(), Level.FINE, JAMS.i18n("killing_the_job_with_id_{0}"), job.getId());
        return client.httpGet(urlStr + "/job/" + job.getId() + "/kill", JobState.class);
    }

    /**
     * deletes a job 
     * @param job in question
     * @return the job which was deleted
     */
    public Job delete(Job job) {
        log(this.getClass(), Level.FINE, JAMS.i18n("deleting_the_job_with_id_{0}"), job.getId());
        return client.httpGet(urlStr + "/job/" + job.getId() + "/delete", Job.class);
    }

    /**
     * deletes all jobs of the user
     */
    public void deleteAll() {
        log(this.getClass(), Level.FINE, JAMS.i18n("deleting_all_jobs"));
        client.httpGet(urlStr + "/job/reset", String.class);
    }

    /**
     * retrieves a part from the jobs inflolog
     * @param job of which the infolog is retrieved
     * @param offset from start
     * @param size maximum size in bytes which should be retrieved 
     * @return a string representation of the infolog
     * @throws IOException
     */
    public String infolog(Job job, int offset, int size) throws IOException {
        log(this.getClass(), Level.FINE, JAMS.i18n("retrieving_info_log_stream_of_job_{0}"), job.getId());
        InputStream is = client.getStream(urlStr + "/job/" + job.getId() + "/infolog");
        try {
            return FileTools.streamToString(is, offset, size);
        } finally {
            try {
                is.close();
            } catch (IOException ioe) {
                log(this.getClass(), Level.WARNING, ioe, ioe.toString());
            }
        }
    }

    /**
     * retrieves a part from the jobs errorlog
     * @param job of which the error is retrieved
     * @param offset from start
     * @param size maximum size in bytes which should be retrieved 
     * @return a string representation of the errorlog
     * @throws IOException
     */
    public String errorlog(Job job, int offset, int size) throws IOException {
        log(this.getClass(), Level.FINE, JAMS.i18n("retrieving_error_log_stream_of_job_{0}"), job.getId());
        InputStream is = client.getStream(urlStr + "/job/" + job.getId() + "/errorlog");
        try {
            return FileTools.streamToString(is, offset, size);
        } finally {
            try {
                is.close();
            } catch (IOException ioe) {
                log(this.getClass(), Level.WARNING, ioe, ioe.toString());
            }
        }
    }

    /**
     * downloads the workspace of the job
     * @param target location in local file system
     * @param job which should be downloaded
     * @return file object where the job was saved to
     * @throws IOException
     */
    public File download(File target, Job job) throws IOException {
        log(this.getClass(), Level.FINE, JAMS.i18n("downloading_workspace_of_job_{0}"), job.getId());
        Workspace ws = client.httpGet(urlStr + "/job/" + job.getId() + "/refresh/", Workspace.class);
        return ctrl.workspaces().downloadWorkspace(target, ws);
    }
}
