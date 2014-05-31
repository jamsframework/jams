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

import jams.server.entities.Job;
import jams.server.entities.JobState;
import jams.server.entities.Jobs;
import jams.server.entities.Workspace;
import jams.server.entities.WorkspaceFileAssociation;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.util.logging.Level;

/**
 *
 * @author christian
 */
public class JobController extends Controller {

    FileController fileController = null;

    public JobController(HTTPClient client, String serverUrl) {
        super(client, serverUrl);

        fileController = this.getFileController();
    }
    
    public Job create(Workspace ws, WorkspaceFileAssociation wfa) {
        return (Job) client.httpGet(serverURL + "/job/create?workspace=" + ws.getId() + "&file=" + wfa.getFile().getId(), Job.class);
    }
    
    public Jobs find() {
        return (Jobs) client.httpGet(serverURL + "/job/find", Jobs.class);
    }
    
    public Jobs getActiveJobs() {
        return (Jobs) client.httpGet(serverURL + "/job/findActive", Jobs.class);
    }
    
    public Jobs findAll() {
        if (super.user.getAdmin()==0)
            return null;
        return (Jobs) client.httpGet(serverURL + "/job/findAll", Jobs.class);
    }
    
    public JobState getState(Job job) {
        return (JobState) client.httpGet(serverURL + "/job/" + job.getId() + "/state", JobState.class);
    }
    
    public JobState kill(Job job) {
        return (JobState) client.httpGet(serverURL + "/job/" + job.getId() + "/kill", JobState.class);
    }
    
    public Job delete(Job job) {
        return (Job) client.httpGet(serverURL + "/job/" + job.getId() + "/delete", Job.class);
    }
    
    public String getInfoLog(Job job, int offset, int size) {
        InputStream is = client.getStream(serverURL + "/job/" + job.getId() + "/infolog");
        String t="";
        try{
            is.skip(offset);
            byte buffer[] = new byte[16384];
            int nread = 0;
            while ((nread=is.read(buffer,0,Math.min(buffer.length, size)))>0 && size>0){
                t+=new String(buffer);
                size-=nread;
            }            
        }catch(IOException ioe){
            log(Level.SEVERE, ioe.toString(), ioe);
        }finally{
            try{
                is.close();                
            }catch(IOException ioe){}
        }
        return t;
    }
    
    public String getErrorLog(Job job, int offset, int size) {
        InputStream is = client.getStream(serverURL + "/job/" + job.getId() + "/errorlog");
        String t="";
        try{
            is.skip(offset);
            byte buffer[] = new byte[16384];
            int nread = 0;
            while ((nread=is.read(buffer,0,Math.min(buffer.length, size)))>0 && size>0){
                t+=new String(buffer);
                size-=nread;
            }            
        }catch(IOException ioe){
            log(Level.SEVERE, ioe.toString(), ioe);
        }finally{
            try{
                is.close();                
            }catch(IOException ioe){}
        }
        return t;
    }   
    
    public File downloadWorkspace(File target, Job job) {
        Workspace ws = (Workspace)client.httpGet(serverURL + "/job/"+job.getId()+ "/refresh/", Workspace.class);
                
        return this.getWorkspaceController().downloadWorkspace(target, ws);
    }
}
