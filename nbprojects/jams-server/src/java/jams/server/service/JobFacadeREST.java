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
import jams.server.entities.Jobs;
import jams.server.entities.User;
import jams.server.entities.Workspace;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.StreamingOutput;

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
    
    private List<Job> getJobsForUser(int ownerID){
        return em.createQuery(
                "SELECT w FROM Job w WHERE w.ownerID = :ownerID")
                .setParameter("ownerID", ownerID)
                .getResultList();
    }
    
    private List<Job> getAllJobs() {
        return em.createQuery(
                "SELECT w FROM Job w ")
                .getResultList();
    }
       
    private Workspace duplicateWorkspace(Workspace ws){
        Workspace ws_clone = new Workspace(ws);        
        ws_clone.setReadOnly(true);
        getEntityManager().persist(ws_clone);
        getEntityManager().flush();
        getEntityManager().refresh(ws_clone);
        return ws;
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
        Job job = new Job(0, currentUser, duplicateWorkspace(ws));
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
    
    @GET
    @Path("find")
    @Produces({"application/xml"})
    public Response find(@Context HttpServletRequest req) {
        User currentUser = getCurrentUser(req);
        if (currentUser == null) {
            return Response.status(Status.FORBIDDEN).build();
        }
        
        List<Job> list = getJobsForUser(currentUser.getId());
        if (list == null || list.isEmpty()){
            return Response.status(Status.NOT_FOUND).build();
        }
        
        return Response.ok(new Jobs(list), MediaType.APPLICATION_XML_TYPE).build();
    }
    
    @GET
    @Path("findActive")
    @Produces({"application/xml"})
    public Response activeJobs(@Context HttpServletRequest req) {
        User currentUser = getCurrentUser(req);
        if (currentUser == null) {
            return Response.status(Status.FORBIDDEN).build();
        }
        
        List<Job> list = getJobsForUser(currentUser.getId());
        if (list == null || list.isEmpty()){
            return Response.status(Status.NOT_FOUND).build();
        }
        ArrayList<Job> activeList = new ArrayList<Job>();
        for (Job job : list){
            if (job.getPID()!=-1){
                try {
                    JobState state = processManager.state(job);
                    if (!state.isActive()) {
                        job.setPID(-1);
                        em.persist(job);
                    } else {
                        activeList.add(job);
                    }
                } catch (IOException ioe) {
                    ioe.printStackTrace();
                }
            }
        }
        
        return Response.ok(new Jobs(activeList), MediaType.APPLICATION_XML_TYPE).build();
    }
    
    @GET
    @Path("findAll")
    @Produces({"application/xml"})
    public Response findAll(@Context HttpServletRequest req) {
        User currentUser = getCurrentUser(req);
        if (currentUser == null) {
            return Response.status(Status.FORBIDDEN).build();
        }
        if (currentUser.getAdmin()==0)
            return Response.status(Status.FORBIDDEN).build();
        
        List<Job> list = getAllJobs();
        if (list == null || list.isEmpty()){
            return Response.status(Status.NOT_FOUND).build();
        }
        
        return Response.ok(new Jobs(list), MediaType.APPLICATION_XML_TYPE).build();
    }
    
    @GET
    @Path("{id}/infolog")
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    public Response infolog(@PathParam("id") Integer id, @Context HttpServletRequest req) {
        User currentUser = getCurrentUser(req);
        if (currentUser == null) {
            return Response.status(Status.FORBIDDEN).build();
        }
        
        Job job = find(id);
        if (job == null){
            return Response.status(Status.NOT_FOUND).build();
        }
        if (job.getOwner().getId() != currentUser.getId()){
            return Response.status(Status.FORBIDDEN).build();
        }
        try{
            StreamingOutput so = processManager.streamInfoLog(job);        
            return Response.ok(so).header("fileName", "info.log").build();
        }catch(IOException ioe){
            ioe.printStackTrace();
            return Response.status(Status.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    @GET
    @Path("{id}/errorlog")
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    public Response errorlog(@PathParam("id") Integer id, @Context HttpServletRequest req) {
        User currentUser = getCurrentUser(req);
        if (currentUser == null) {
            return Response.status(Status.FORBIDDEN).build();
        }
        
        Job job = find(id);
        if (job == null){
            return Response.status(Status.NOT_FOUND).build();
        }
        if (job.getOwner().getId() != currentUser.getId()){
            return Response.status(Status.FORBIDDEN).build();
        }
        try{
            StreamingOutput so = processManager.streamErrorLog(job);        
            return Response.ok(so).header("fileName", "error.log").build();
        }catch(IOException ioe){
            ioe.printStackTrace();
            return Response.status(Status.INTERNAL_SERVER_ERROR).build();
        }
    }
        
    @GET
    @Path("{id}/state")
    @Produces({"application/xml"})
    public Response getState(@PathParam("id") Integer id, @Context HttpServletRequest req) {
        User currentUser = getCurrentUser(req);
        if (currentUser == null) {
            return Response.status(Status.FORBIDDEN).build();
        }
        
        Job job = find(id);
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
    
    @GET
    @Path("{id}/kill")
    @Produces({"application/xml"})
    public Response kill(@PathParam("id") Integer id, @Context HttpServletRequest req) {
        User currentUser = getCurrentUser(req);
        if (currentUser == null) {
            return Response.status(Status.FORBIDDEN).build();
        }
        
        Job job = find(id);
        if (job == null){
            return Response.status(Status.NOT_FOUND).build();
        }
        if (job.getOwner().getId() != currentUser.getId()){
            return Response.status(Status.FORBIDDEN).build();
        }
        
        try{
            JobState state = processManager.kill(job);
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
    
    @GET
    @Path("{id}/delete")
    @Produces({"application/xml"})
    public Response delete(@PathParam("id") Integer id, @Context HttpServletRequest req) {
        User currentUser = getCurrentUser(req);
        if (currentUser == null) {
            return Response.status(Status.FORBIDDEN).build();
        }
        
        Job job = find(id);
        if (job == null){
            return Response.status(Status.NOT_FOUND).build();
        }
        if (job.getOwner().getId() != currentUser.getId()){
            return Response.status(Status.FORBIDDEN).build();
        }
        
        try{
            JobState state = processManager.kill(job);            
            if (state!=null && state.isActive()){
                return Response.status(Status.REQUEST_TIMEOUT).build();
            }else{
                super.remove(job);
                em.remove(job.getWorkspace());
            }
            return Response.ok(job, MediaType.APPLICATION_XML_TYPE).build();
        }catch(IOException ioe){
            ioe.printStackTrace();
            return Response.status(Status.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    @GET
    @Path("{id}/refresh")
    @Produces({"application/xml"})
    public Response refresh(@PathParam("id") Integer id, @Context HttpServletRequest req) {
        User currentUser = getCurrentUser(req);
        if (currentUser == null) {
            return Response.status(Status.FORBIDDEN).build();
        }
        
        Job job = find(id);
        if (job == null){
            return Response.status(Status.NOT_FOUND).build();
        }
        if (job.getOwner().getId() != currentUser.getId()){
            return Response.status(Status.FORBIDDEN).build();
        }
        
        Workspace ws = processManager.updateWorkspace(job, em);
        return Response.ok(ws).build();            
    }
    
    @Override
    protected EntityManager getEntityManager() {
        return em;
    }    
}
