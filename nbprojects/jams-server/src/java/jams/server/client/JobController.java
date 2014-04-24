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
import jams.server.entities.Workspace;

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
    
    public Job create(Workspace ws) throws JAMSClientException {
        return (Job) client.httpPost(serverURL + "/job/create", "PUT", ws, Job.class);
    }
    
    public JobState getState(Job job) throws JAMSClientException {
        return (JobState) client.httpPost(serverURL + "/job/state", "POST", job, JobState.class);
    }
}
