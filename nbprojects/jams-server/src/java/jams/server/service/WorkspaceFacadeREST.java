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

import jams.server.entities.File;
import jams.server.entities.User;
import jams.server.entities.Workspace;
import jams.server.entities.Workspaces;
import java.util.List;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

/**
 *
 * @author Sven Kralisch <sven.kralisch at uni-jena.de>
 */
@Stateless
@Path("workspace")
public class WorkspaceFacadeREST extends AbstractFacade<Workspace> {

    @PersistenceContext(unitName = "jams-serverPU")
    private EntityManager em;
        
    public WorkspaceFacadeREST() {
        super(Workspace.class);
    }
        
    @PUT
    @Path("create")
    @Consumes({"application/xml", "application/json"})
    @Produces({"application/xml"})
    public Response create(Workspace entity, @Context HttpServletRequest req) {
        User currentUser = getCurrentUser(req);
        if (currentUser == null){
            return Response.status(Status.FORBIDDEN).build();              
        }
        entity.setUser(currentUser);
        super.create(entity);
        List<Workspace> list = findByName(entity.getName());
        if (list.isEmpty()){
            //error
        }
        return Response.ok(list.get(0),MediaType.APPLICATION_XML_TYPE).build();                
    }

    @PUT
    @Path("{id}")
    @Consumes({"application/xml", "application/json"})
    public Response edit(@PathParam("id") Integer id, Workspace entity, @Context HttpServletRequest req) {        
        User user = getCurrentUser(req);
        if (user == null){
            return Response.status(Response.Status.FORBIDDEN).build();
        }
        Workspace ws = find(id);
        if (ws == null){
            return Response.status(Status.NOT_FOUND).build();
        }
        if (user.getAdmin()>0 || user.getId() == ws.getUser().getId()) {
            super.edit(entity);
            return Response.ok(true).build();
        }
        return Response.status(Status.FORBIDDEN).build();
    }

    @DELETE
    @Path("{id}")
    @Produces({"application/xml", "application/json"})
    public Response remove(@PathParam("id") Integer id, @Context HttpServletRequest req) {
        User user = getCurrentUser(req);
        if (user == null){
            return Response.status(Response.Status.FORBIDDEN).build();
        }
        Workspace ws = find(id);
        if (ws == null){
            return Response.status(Status.NOT_FOUND).build();
        }
        if (user.getAdmin()>0 || user.getId() == ws.getUser().getId()) {
            super.remove(ws);
            return Response.ok(true).build();
        }
        return Response.status(Status.FORBIDDEN).build();
    }

    @GET
    @Path("{id}")
    @Produces({"application/xml", "application/json"})
    public Response find(@PathParam("id") Integer id, @Context HttpServletRequest req) {
        User user = getCurrentUser(req);
        if (user == null){
            return Response.status(Response.Status.FORBIDDEN).build();
        }
        Workspace ws = find(id);
        if (ws == null){
            return Response.status(Status.NOT_FOUND).build();
        }
        if (user.getAdmin()>0 || user.getId() == ws.getUser().getId()) {
            return Response.ok(super.find(id),MediaType.APPLICATION_XML).build();
        }
        return Response.status(Status.FORBIDDEN).build();
    }
    
    @GET
    @Path("all")
    @Produces({"application/xml", "application/json"})
    public Response findAll(@Context HttpServletRequest req) {
        User user = getCurrentUser(req);
        
        List<Workspace> workspaces = em.createNamedQuery("findByUserId")
            .setParameter("id", user.getId())
            .getResultList();
                
        return Response.ok(new Workspaces(workspaces),MediaType.APPLICATION_XML).build();       
    }
    
    @GET
    @Path("assign")
    @Produces({"application/xml", "application/json"})
    public Response assignFile(@QueryParam("WORKSPACE_ID") Integer wsID, 
                               @QueryParam("FILE_ID") Integer fileID,
                               @QueryParam("ROLE") Integer role, @Context HttpServletRequest req) {
        User user = getCurrentUser(req);
        if (user == null){
            return Response.status(Response.Status.FORBIDDEN).build();
        }
        Workspace ws = find(wsID);
        File f = findFileByID(fileID);
        
        if (ws == null || f == null){
            return Response.status(Status.NOT_FOUND).build();
        }
        if (user.getAdmin()>0 || user.getId() == ws.getUser().getId()) {
            ws.assignFile(f, role);
            return Response.ok(ws).build();
        }
                
        return Response.status(Status.FORBIDDEN).build();
    }
    
    @GET
    @Path("count")
    @Produces("text/plain")
    public Response countREST(@Context HttpServletRequest req) {
        return Response.ok(String.valueOf(super.count()),MediaType.TEXT_PLAIN).build();       
    }

    @Override
    protected EntityManager getEntityManager() {
        return em;
    }

    private List findByName(String name) {
        return em.createQuery(
                "SELECT u FROM Workspace u WHERE u.name = :name")
                .setParameter("name", name)
                .getResultList();
    }
}
