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
import jams.server.entities.Files;
import jams.server.entities.User;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import org.glassfish.jersey.media.multipart.FormDataContentDisposition;
import org.glassfish.jersey.media.multipart.FormDataParam;

/**
 *
 * @author Sven Kralisch <sven.kralisch at uni-jena.de>
 */
@Stateless
@Path("file")
public class FileFacadeREST extends AbstractFacade<File> {
    
    @PersistenceContext(unitName = "jams-serverPU")
    private EntityManager em;    

    public FileFacadeREST() {
        super(File.class);
    }

    @Override
    protected EntityManager getEntityManager() {
        return em;
    }

    private List<File> findHash(String hash) {
        return em.createQuery(
                "SELECT f FROM File f WHERE f.hash = :hash")
                .setParameter("hash", hash)
                .getResultList();
    }

    @POST
    @Path("exists")
    @Consumes({"application/xml", "application/json"})
    @Produces({"application/xml", "application/json"})
    public Response exists(Files files, @Context HttpServletRequest req) {
        if (!isLoggedIn(req)) {
            return Response.status(Response.Status.FORBIDDEN).build();
        }

        Files result = new Files();        
        for (File file : files.getFiles()) {
            List<File> list = findHash(file.getHash());            
            if (!list.isEmpty())
                result.add(list.get(0));                
        }
        return Response.ok(result, MediaType.APPLICATION_XML_TYPE).build();
    }

    @GET
    @Path("{id}")
    @Produces({"application/xml", "application/json"})
    public Response find(@PathParam("id") Integer id, @Context HttpServletRequest req) {
        User user = getCurrentUser(req);
        if (user == null) {
            return Response.status(Response.Status.FORBIDDEN).build();
        }
        File f = find(id);
        if (f == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
        if (user.getAdmin() > 0) {
            return Response.ok(f, MediaType.APPLICATION_XML).build();
        }
        return Response.status(Response.Status.FORBIDDEN).build();
    }
    
    @POST
    @Path("/upload")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    public Response file(
            @FormDataParam("file") InputStream fileInputStream,
            @FormDataParam("file") FormDataContentDisposition contentDispositionHeader) {

        String filePath = ApplicationConfig.SERVER_UPLOAD_DIRECTORY + contentDispositionHeader.getFileName();

        String hashCode = saveFile(fileInputStream, filePath);
        if (hashCode == null){
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        }
        File f = new File();
        f.setHash(hashCode);        
        f.setLocation(filePath + "/" + hashCode);
        
        List<File> list = findHash(hashCode);
        if (!list.isEmpty())
            return Response.status(200).entity(list.get(0)).build();        
        else if (create(f)){
            return Response.status(200).entity(f).build();
        }
        // save the file to the server        
        return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
    }
        
    // save uploaded file to a defined location on the server
    private String saveFile(InputStream uploadedInputStream, String serverLocation) {

        java.io.File serverFile = new java.io.File(serverLocation);

        try (OutputStream outpuStream = new FileOutputStream(serverFile)) {
            int read = 0;

            byte[] bytes = new byte[1024];

            while ((read = uploadedInputStream.read(bytes)) != -1) {
                outpuStream.write(bytes, 0, read);
            }
            outpuStream.flush();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }

        String hash;
        try (FileInputStream fis = new FileInputStream(serverFile)) {
            hash = org.apache.commons.codec.digest.DigestUtils.md5Hex(fis);
        } catch (IOException fnfe) {
            fnfe.printStackTrace();
            return null;
        }
        serverFile.renameTo(new java.io.File(serverFile.getParent(), hash));
        return hash;
    }
}
