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
import jams.server.entities.WorkspaceFileAssociations;
import jams.server.entities.Workspaces;
import static jams.server.service.FileFacadeREST.removeUnusedFiles;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
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

    public WorkspaceFacadeREST() {
        super(Workspace.class);
    }

    @PUT
    @Path("create")
    @Consumes({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Response create(Workspace entity, @Context HttpServletRequest req) {
        User currentUser = getCurrentUser(req);
        if (currentUser == null) {
            return Response.status(Status.FORBIDDEN).build();
        }
        entity.setUser(currentUser);
        super.create(entity);
        if (entity.getId() == null) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        }
        return Response.ok(entity).build();
    }

    @PUT
    @Path("{id}")
    @Consumes({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Response edit(@PathParam("id") Integer id, Workspace entity, @Context HttpServletRequest req) {
        User user = getCurrentUser(req);
        if (user == null) {
            return Response.status(Response.Status.FORBIDDEN).build();
        }
        Workspace ws = find(id);
        if (ws == null) {
            return Response.status(Status.NOT_FOUND).build();
        }
        if (ws.isReadOnly()) {
            return Response.status(Status.FORBIDDEN).build();
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

    @GET
    @Path("{id}/delete")
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Response remove(@PathParam("id") Integer id, @Context HttpServletRequest req) {
        User user = getCurrentUser(req);
        if (user == null) {
            return Response.status(Response.Status.FORBIDDEN).build();
        }
        Workspace ws = find(id);
        if (ws == null) {
            return Response.status(Status.NOT_FOUND).build();
        }
        if (ws.isReadOnly()) {
            return Response.status(Status.FORBIDDEN).build();
        }

        if (user.getAdmin() > 0 || user.getId() == ws.getUser().getId()) {
            //check if files are still in use
            List<WorkspaceFileAssociation> files = ws.getFiles();
            super.remove(ws);

            for (WorkspaceFileAssociation wfa : files) {
                if (getReferenceCount(wfa.getFile().getId()) == 0) {
                    em.remove(wfa.getFile());
                }
            }
            removeUnusedFiles(em);
            return Response.ok(ws).build();
        }
        return Response.status(Status.FORBIDDEN).build();
    }

    @GET
    @Path("{id}")
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Response find(@PathParam("id") Integer id, @Context HttpServletRequest req) {
        User user = getCurrentUser(req);
        if (user == null) {
            return Response.status(Response.Status.FORBIDDEN).build();
        }
        Workspaces workspaces = new Workspaces();
        Workspace ws = find(id);
        if (ws != null) {
            workspaces.add(ws);
        }else{
            return Response.ok(workspaces).build();
        }
        if (user.getAdmin() > 0 || user.getId() == ws.getUser().getId()) {
            return Response.ok(workspaces).build();
        }
        return Response.ok(workspaces).build();
    }

    @GET
    @Path("find")
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Response findAll(@QueryParam("name") String name, @Context HttpServletRequest req) {
        User user = getCurrentUser(req);

        List<Workspace> workspaces = em.createNamedQuery("Workspace.findByUserId")
                .setParameter("id", user.getId())
                .getResultList();

        List<Workspace> filteredList = new ArrayList<>();
        if (name != null) {
            for (Workspace ws : workspaces) {
                if (ws.getName().equals(name)) {
                    filteredList.add(ws);
                }
            }
        } else {
            filteredList = workspaces;
        }

        return Response.ok(new Workspaces(filteredList)).build();
    }

    @POST
    @Path("{id}/assign")
    @Consumes({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Response assignFile(@PathParam("id") Integer wsID,
            WorkspaceFileAssociations entity, @Context HttpServletRequest req) {
        User user = getCurrentUser(req);
        if (user == null) {
            return Response.status(Response.Status.FORBIDDEN).build();
        }
        Workspace ws = find(wsID);
        if (ws == null) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, "Workspace with ID {0} not found", wsID);
            return Response.status(Status.NOT_FOUND).build();
        }
        if (ws.isReadOnly()) {
            return Response.status(Status.FORBIDDEN).build();
        }

        for (WorkspaceFileAssociation wfa : entity.getWorkspaceFileAssociations()) {
            File f = wfa.getFile();//findFileByID(wfa.);

            if (f == null) {
                Logger.getLogger(getClass().getName()).log(Level.SEVERE, "File with ID {0} not found", wfa.getID());
                return Response.status(Status.NOT_FOUND).build();
            }
            if (user.getAdmin() > 0 || user.getId() == ws.getUser().getId()) {
                ws.assignFile(f, wfa.getRole(), wfa.getPath());
            }else{
                return Response.status(Status.FORBIDDEN).build();
            }
        }
        getEntityManager().flush();
        return Response.ok(ws).build();
    }

    @GET
    @Path("{id}/detach")
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Response detachFile(@PathParam("id") Integer wsID,
            @QueryParam("RELATIVE_PATH") String path, @Context HttpServletRequest req) {
        User user = getCurrentUser(req);
        if (user == null) {
            return Response.status(Response.Status.FORBIDDEN).build();
        }
        Workspace ws = find(wsID);

        if (ws == null) {
            return Response.status(Status.NOT_FOUND).build();
        }

        if (ws.isReadOnly()) {
            return Response.status(Status.FORBIDDEN).build();
        }
        
        if (user.getAdmin() > 0 || user.getId() == ws.getUser().getId()) {
            File f = ws.detachFile(path);
            if (f == null) {
                return Response.status(Status.NOT_FOUND).build();
            }
            return Response.ok(ws).build();
        }
        return Response.status(Status.FORBIDDEN).build();
    }

    @GET
    @Path("{id}/detachAll")
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Response detachAllFiles(@PathParam("id") Integer wsID,
            @Context HttpServletRequest req) {
        User user = getCurrentUser(req);
        if (user == null) {
            return Response.status(Response.Status.FORBIDDEN).build();
        }
        Workspace ws = find(wsID);
        if (ws == null) {
            return Response.status(Status.NOT_FOUND).build();
        }
        
        if (ws.isReadOnly()) {
            return Response.status(Status.FORBIDDEN).build();
        }
        
        if (user.getAdmin() > 0 || user.getId() == ws.getUser().getId()) {
            ws.detachAllFiles();            
            return Response.ok(ws).build();
        }
        return Response.status(Status.FORBIDDEN).build();
    }
    
    @GET
    @Path("reset")
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Response reset(@PathParam("id") Integer wsID, @Context HttpServletRequest req) {
        User user = getCurrentUser(req);
        if (user == null) {
            return Response.status(Response.Status.FORBIDDEN).build();
        }
        List<Workspace> workspaces = em.createNamedQuery("Workspace.findByUserId")
                .setParameter("id", user.getId())
                .getResultList();

        for (Workspace ws : workspaces) {
            if (!ws.isReadOnly()) {
                remove(ws);
            }
        }
        removeUnusedFiles(em);
        return Response.ok("Successful").build();
    }

    @GET
    @Path("download/{id_ws}/{id_wfa}")
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    public Response downloadFile(@PathParam("id_ws") Integer wsID,
            @PathParam("id_wfa") Integer wfaID,
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
        for (WorkspaceFileAssociation wfa2 : ws.getFiles()) {
            if (wfa2.getID().equals(wfaID)) {
                f = wfa2.getFile();
                wfa = wfa2;
                break;
            }
        }

        if (f == null || wfa == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }

        if (user.getAdmin() > 0 || user.getId() == ws.getUser().getId()) {
            final java.io.File file = new java.io.File(f.getLocation());

            try {
                StreamingOutput so = Utilities.streamFile(file);
                return Response.ok(so).header("fileName", wfa.getPath()).build();
            } catch (IOException ioe) {
                ioe.printStackTrace();
                return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
            }
        }
        return Response.status(Response.Status.FORBIDDEN).build();
    }

    @GET
    @Path("download/{id}")
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    public Response downloadWorkspace(@PathParam("id") Integer wsID,
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
            try {
                java.io.File target = new java.io.File(ApplicationConfig.SERVER_TMP_DIRECTORY + "/workspace_" + ws.getId());
                StreamingOutput so = Utilities.streamWorkspace(target, ws);
                if (so == null) {
                    return Response.status(Response.Status.NOT_FOUND).build();
                }
                return Response.ok(so).header("fileName", ws.getName() + ".zip").build();
            } catch (Throwable ioe) {
                ioe.printStackTrace();
                return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
            }
        }
        return Response.status(Response.Status.FORBIDDEN).build();
    }

    @GET
    @Path("count")
    @Produces({MediaType.TEXT_PLAIN, MediaType.APPLICATION_JSON})
    public Response countREST(@Context HttpServletRequest req) {
        return Response.ok(String.valueOf(super.count())).build();
    }

    @Override
    protected EntityManager getEntityManager() {
        return em;
    }
}
