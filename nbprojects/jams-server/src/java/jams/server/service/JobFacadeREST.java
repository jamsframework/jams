/*
 * UserFacadeREST.java
 * Created on 01.03.2014, 21:37:11
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
import jams.server.entities.User;
import jams.server.entities.Workspace;
import java.io.IOException;
import java.util.List;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

/**
 *
 * @author Sven Kralisch <sven.kralisch at uni-jena.de>
 */
@Stateless
@Path("job")
public class JobFacadeREST extends AbstractFacade<Job> {

    @PersistenceContext(unitName = "jams-serverPU")
    private EntityManager em;

    ProcessManager processManager = new LocalWindowsProcessManager();
    
    public JobFacadeREST() {
        super(Job.class);
    }

    private List<Workspace> getWorkspaceWithID(int id){
        return em.createQuery(
                "SELECT w FROM Workspace w WHERE w.id = :id")
                .setParameter("id", id)
                .getResultList();
    }
       
    @PUT
    @Path("create")
    @Consumes({"application/xml", "application/json"})
    @Produces({"application/xml"})
    public Response create(Workspace entity, @Context HttpServletRequest req) {
        User currentUser = getCurrentUser(req);
        if (currentUser == null) {
            return Response.status(Status.FORBIDDEN).build();
        }
        
        List<Workspace> list = getWorkspaceWithID(entity.getId());
        if (list == null || list.isEmpty()){
            return Response.status(Status.NOT_FOUND).build();
        }
        
        Workspace ws = list.get(0);
        if (ws.getUser().getId() != currentUser.getId()){
            return Response.status(Status.FORBIDDEN).build();
        }
        Job job = new Job(0, currentUser, ws);
        if (!create(job) || job.getId()==null){
            return Response.status(Status.INTERNAL_SERVER_ERROR).build();
        }
        
        try{            
            job = processManager.deploy(job);       
            em.persist(job);
            return Response.ok(job, MediaType.APPLICATION_XML_TYPE).build();
        }catch(IOException ioe){
            ioe.printStackTrace();
            return Response.status(Status.INTERNAL_SERVER_ERROR).build();
        }                
    }
        
    @POST
    @Path("state")
    @Consumes({"application/xml", "application/json"})
    @Produces({"application/xml"})
    public Response getState(Job entity, @Context HttpServletRequest req) {
        User currentUser = getCurrentUser(req);
        if (currentUser == null) {
            return Response.status(Status.FORBIDDEN).build();
        }
        
        Job job = find(entity.getId());
        if (job == null){
            return Response.status(Status.NOT_FOUND).build();
        }
        if (job.getOwner().getId() != currentUser.getId()){
            return Response.status(Status.FORBIDDEN).build();
        }
        
        try{
            JobState state = processManager.state(job);
            if (!state.isActive()){
                job.setPID(-1);
                em.persist(job);
            }
            return Response.ok(state, MediaType.APPLICATION_XML_TYPE).build();
        }catch(IOException ioe){
            ioe.printStackTrace();
            return Response.status(Status.INTERNAL_SERVER_ERROR).build();
        }
    }
    @Override
    protected EntityManager getEntityManager() {
        return em;
    }    
}
