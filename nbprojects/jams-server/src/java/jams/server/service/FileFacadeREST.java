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
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigInteger;
import java.nio.file.Paths;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
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

    @POST
    @Path("exists")
    @Consumes({"application/xml", "application/json"})
    @Produces({"application/xml", "application/json"})
    public Response exists(Files entity, @Context HttpServletRequest req) {
        if (!users.isLoggedIn(req)) {
            return Response.status(Response.Status.FORBIDDEN).build();
        }

        Files result = new Files();        
        for (File f : entity.getFiles()) {
            result.add(findHash(f.getHash()).get(0));
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

        String hashCode = saveFile(fileInputStream, filePath);
        if (hashCode == null){
            return Response.status(200).entity("Error: I/O Error while saving file").build();
        }
        File f = new File();
        f.setHash(hashCode);
        List<File> list = findHash(hashCode);
        if (!list.isEmpty())
            return Response.status(200).entity(list.get(0)).build();        
        else if (create(f)){
            return Response.status(200).entity(f).build();
        }
        // save the file to the server        
        return Response.status(200).entity("Error: Something went wrong!").build();
    }

    // save uploaded file to a defined location on the server
    private String saveFile(InputStream uploadedInputStream, String serverLocation) {

        java.io.File serverFile = new java.io.File(serverLocation);
        try {
            OutputStream outpuStream = new FileOutputStream(serverFile);

            int read = 0;

            byte[] bytes = new byte[1024];

            while ((read = uploadedInputStream.read(bytes)) != -1) {
                outpuStream.write(bytes, 0, read);
            }
            outpuStream.flush();
            outpuStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            DigestInputStream dis = new DigestInputStream(new FileInputStream(new java.io.File(serverLocation)), md);
            BigInteger bigInt = new BigInteger(md.digest());
            dis.close();

            String hashCode = bigInt.toString(16);
            serverFile.renameTo(new java.io.File(serverFile.getParent(), hashCode));
            return hashCode;

        } catch (NoSuchAlgorithmException nsae) {
            nsae.printStackTrace();
        } catch (IOException fnfe) {
            fnfe.printStackTrace();
        }
        return null;
    }

}
