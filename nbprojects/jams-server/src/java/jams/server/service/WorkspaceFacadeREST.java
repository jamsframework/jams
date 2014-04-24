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
import jams.server.entities.WorkspaceFileAssociation;
import jams.server.entities.Workspaces;
import java.io.BufferedOutputStream;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
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
import javax.ws.rs.WebApplicationException;
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
@Path("workspace")
public class WorkspaceFacadeREST extends AbstractFacade<Workspace> {

    @PersistenceContext(unitName = "jams-serverPU")
    private EntityManager em;

    private boolean EAGER_REMOVAL = true;    
    
    public WorkspaceFacadeREST() {
        super(Workspace.class);
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
        entity.setUser(currentUser);
        super.create(entity);        
        if (entity.getId()==null) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        }
        return Response.ok(entity, MediaType.APPLICATION_XML_TYPE).build();
    }

    @PUT
    @Path("{id}")
    @Consumes({"application/xml", "application/json"})
    public Response edit(@PathParam("id") Integer id, Workspace entity, @Context HttpServletRequest req) {
        User user = getCurrentUser(req);
        if (user == null) {
            return Response.status(Response.Status.FORBIDDEN).build();
        }
        Workspace ws = find(id);
        if (ws == null) {
            return Response.status(Status.NOT_FOUND).build();
        }
        if (user.getAdmin() > 0 || user.getId() == ws.getUser().getId()) {
            super.edit(entity);
            return Response.ok(ws).build();
        }
        return Response.status(Status.FORBIDDEN).build();
    }

    private int getReferenceCount(int id) {
        return em.createNamedQuery("WorkspaceFileAssociation.findByFile")
                .setParameter("fid", id)
                .getResultList().size();
    }

    @DELETE
    @Path("{id}")
    @Produces({"application/xml", "application/json"})
    public Response remove(@PathParam("id") Integer id, @Context HttpServletRequest req) {
        User user = getCurrentUser(req);
        if (user == null) {
            return Response.status(Response.Status.FORBIDDEN).build();
        }
        Workspace ws = find(id);
        if (ws == null) {
            return Response.status(Status.NOT_FOUND).build();
        }
        if (user.getAdmin() > 0 || user.getId() == ws.getUser().getId()) {
            //check if files are still in use
            List<WorkspaceFileAssociation> files = ws.getAssociatedFiles();
            super.remove(ws);

            for (WorkspaceFileAssociation wfa : files) {
                if (getReferenceCount(wfa.getFile().getId()) == 0) {
                    em.remove(wfa.getFile());
                }
            }

            return Response.ok(ws).build();
        }
        return Response.status(Status.FORBIDDEN).build();
    }

    @GET
    @Path("{id}")
    @Produces({"application/xml", "application/json"})
    public Response find(@PathParam("id") Integer id, @Context HttpServletRequest req) {
        User user = getCurrentUser(req);
        if (user == null) {
            return Response.status(Response.Status.FORBIDDEN).build();
        }
        Workspace ws = find(id);
        if (ws == null) {
            return Response.status(Status.NOT_FOUND).build();
        }
        if (user.getAdmin() > 0 || user.getId() == ws.getUser().getId()) {
            return Response.ok(super.find(id), MediaType.APPLICATION_XML).build();
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

        return Response.ok(new Workspaces(workspaces), MediaType.APPLICATION_XML).build();
    }

    @GET
    @Path("assign")
    @Produces({"application/xml", "application/json"})
    public Response assignFile(@QueryParam("WORKSPACE_ID") Integer wsID,
            @QueryParam("FILE_ID") Integer fileID,
            @QueryParam("ROLE") Integer role,
            @QueryParam("RELATIVE_PATH") String path, @Context HttpServletRequest req) {
        User user = getCurrentUser(req);
        if (user == null) {
            return Response.status(Response.Status.FORBIDDEN).build();
        }
        Workspace ws = find(wsID);
        File f = findFileByID(fileID);

        if (ws == null || f == null) {
            return Response.status(Status.NOT_FOUND).build();
        }
        if (user.getAdmin() > 0 || user.getId() == ws.getUser().getId()) {
            ws.assignFile(f, role, path);
            return Response.ok(ws).build();
        }

        return Response.status(Status.FORBIDDEN).build();
    }

    @GET
    @Path("download/file/")
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    public Response downloadFile(@QueryParam("WORKSPACE_ID") Integer wsID,
            @QueryParam("FILE_ID") Integer fileID,
            @Context HttpServletRequest req) {
        User user = getCurrentUser(req);
        if (user == null) {
            return Response.status(Response.Status.FORBIDDEN).build();
        }
        Workspace ws = find(wsID);
        if (ws == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
        
        File f = null;
        WorkspaceFileAssociation wfa = null;
        for (WorkspaceFileAssociation wfa2 : ws.getAssociatedFiles()) {
            if (wfa2.getFile().getId().equals(fileID)) {
                f = wfa2.getFile();
                wfa = wfa2;
                break;
            }
        }

        if (f == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
        
        if (user.getAdmin() > 0 || user.getId() == ws.getUser().getId()) {
            WorkspaceBuilder wb = new WorkspaceBuilder();
            
            final java.io.File file = new java.io.File(ApplicationConfig.SERVER_UPLOAD_DIRECTORY, f.getHash());

            try{
                StreamingOutput so = wb.streamFile(file);
                return Response.ok(so).header("fileName", wfa.getPath()).build();
            }catch(IOException ioe){
                ioe.printStackTrace();
                return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
            }
        }
        return Response.status(Response.Status.FORBIDDEN).build();
    }
    
    @GET
    @Path("download/workspace/")
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    public Response downloadWorkspace(@QueryParam("WORKSPACE_ID") Integer wsID,
            @Context HttpServletRequest req) {
        User user = getCurrentUser(req);
        if (user == null) {
            return Response.status(Response.Status.FORBIDDEN).build();
        }
        Workspace ws = find(wsID);
        if (ws == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
        if (user.getAdmin() > 0 || user.getId() == ws.getUser().getId()) {
            try{
                WorkspaceBuilder wsBuilder = new WorkspaceBuilder();
                StreamingOutput so = wsBuilder.streamWorkspace(ws);
                if (so == null){
                    return Response.status(Response.Status.NOT_FOUND).build();
                }
                return Response.ok(so).header("fileName", ws.getName() + ".zip").build();
            }catch(Throwable ioe){
                ioe.printStackTrace();
                return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
            }
        }
        return Response.status(Response.Status.FORBIDDEN).build();
    }

    @GET
    @Path("count")
    @Produces("text/plain")
    public Response countREST(@Context HttpServletRequest req) {
        return Response.ok(String.valueOf(super.count()), MediaType.TEXT_PLAIN).build();
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
