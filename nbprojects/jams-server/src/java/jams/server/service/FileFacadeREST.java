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
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
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

    final String UPLOAD_DIRECTORY = "e:/uploaded/";

    @PersistenceContext(unitName = "jams-serverPU")
    private EntityManager em;

    UserFacadeREST users = new UserFacadeREST();

    public FileFacadeREST() {
        super(File.class);
    }

    @Override
    protected EntityManager getEntityManager() {
        return em;
    }

    private List<File> findHash(String hash) {
        CriteriaBuilder cb = getEntityManager().getCriteriaBuilder();
        CriteriaQuery cq = cb.createQuery();
        Root<File> root = cq.from(File.class);
        Predicate predicate = cb.equal(root.get("hash"), hash);
        cq.select(cq.from(File.class));
        cq.where(predicate);

        javax.persistence.Query q = getEntityManager().createQuery(cq);
        return q.getResultList();
    }

    @GET
    @Consumes({"application/xml", "application/json"})
    @Produces({"application/xml", "application/json"})
    public Response exists(Files entity, @Context HttpServletRequest req) {
        if (!users.isLoggedIn(req)) {
            return Response.status(Response.Status.FORBIDDEN).build();
        }

        boolean result[] = new boolean[entity.getFiles().size()];
        int i = 0;
        for (File f : entity.getFiles()) {
            result[i] = findHash(f.getHash()).isEmpty();
        }
        return Response.ok(result, MediaType.APPLICATION_XML_TYPE).build();
    }

    @POST
    @Path("/upload")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    public Response file(
            @FormDataParam("file") InputStream fileInputStream,
            @FormDataParam("file") FormDataContentDisposition contentDispositionHeader) {

        String filePath = UPLOAD_DIRECTORY + contentDispositionHeader.getFileName();

        // save the file to the server
        saveFile(fileInputStream, filePath);

        String output = "File saved to server location : " + filePath;

        return Response.status(200).entity(output).build();
    }

    // save uploaded file to a defined location on the server
    private void saveFile(InputStream uploadedInputStream, String serverLocation) {

        try {
            OutputStream outpuStream = new FileOutputStream(new java.io.File(serverLocation));

            int read = 0;

            byte[] bytes = new byte[1024];

            outpuStream = new FileOutputStream(new java.io.File(serverLocation));

            while ((read = uploadedInputStream.read(bytes)) != -1) {

                outpuStream.write(bytes, 0, read);

            }
            outpuStream.flush();
            outpuStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
