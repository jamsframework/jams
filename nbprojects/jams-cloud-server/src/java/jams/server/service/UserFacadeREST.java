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

import jams.server.entities.User;
import jams.server.entities.Users;
import java.util.List;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
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
 * @author Christian Fischer <christian.fischer.2@uni-jena.de>
 */
@Stateless
@Path("user")
public class UserFacadeREST extends AbstractFacade<User> {

    @PersistenceContext(unitName = "jams-serverPU")
    private EntityManager em;

    public UserFacadeREST() {
        super(User.class);
    }
        
    @PUT
    @Path("create")
    @Consumes({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Response create(User entity, @Context HttpServletRequest req) {
        if (isAdmin(req)) {
            if (!findByName(entity.getLogin()).isEmpty())
                return Response.status(Response.Status.CONFLICT).build();
            
            super.create(entity);
            if (entity.getId() != null)
                return Response.ok(entity).build();
            else{
                return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
            }
        }
        return Response.status(Response.Status.FORBIDDEN).build();
    }

    @PUT
    @Path("{id}")
    @Consumes({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Response edit(@PathParam("id") Integer id, User entity, @Context HttpServletRequest req) {
        User user = getCurrentUser(req);

        if (user == null || (user.getId() != id && user.getAdmin() == 0)) {
            return Response.status(Response.Status.FORBIDDEN).build();
        }

        // Get unchanged version of entity
        User originalEntity = getEntityManager().find(User.class, id);

        // Prevent creating new user
        if (originalEntity == null) {
            return Response.status(Response.Status.FORBIDDEN).build();
        }

        // Prevent changing the login to another user’s login
        if (!isLoginAvailable(entity.getLogin(), id)) {
            return Response.status(Response.Status.CONFLICT).build();
        }

        originalEntity.setEmail(entity.getEmail());
        originalEntity.setLogin(entity.getLogin());
        originalEntity.setName(entity.getName());

        // Only update admin role if user is actually admin
        if (user.getAdmin() > 0) {
            originalEntity.setAdmin(entity.getAdmin());
        }

        // Only update password if it is not empty
        if (!entity.getPassword().isEmpty()) {
            originalEntity.setPassword(entity.getPassword());
        }

        super.edit(originalEntity);
        return Response.ok(user).build();
    }

    @DELETE
    @Path("{id}")
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Response remove(@PathParam("id") Integer id, @Context HttpServletRequest req) {
        if (isAdmin(req)) {
            User o = super.find(id);
            if (o == null)
                return null;
            super.remove(o);
            return Response.ok(o).build();
        }
        return Response.status(Response.Status.FORBIDDEN).build();
    }

    @GET
    @Path("{id}")
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Response find(@PathParam("id") Integer id, @Context HttpServletRequest req) {
        if (isAdmin(req)) {
            return Response.ok(super.find(id)).build();
        } else {
            return Response.status(Response.Status.FORBIDDEN).build();
        }
    }
   
    @GET
    @Path("all")
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Response findAll(@Context HttpServletRequest req) {
        if (isAdmin(req)) {
            return Response.ok(new Users(super.findAll())).build();
        } else {
            return Response.status(Response.Status.FORBIDDEN).build();
        }
    }

    @GET
    @Path("{from}/{to}")
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Response findRange(@PathParam("from") Integer from, @PathParam("to") Integer to, @Context HttpServletRequest req) {
        if (!isAdmin(req)) {
            return Response.status(Response.Status.FORBIDDEN).build();
        }
        return Response.ok(new Users(super.findRange(new int[]{from, to}))).build();
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

    @GET
    @Path("login")
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Response login(@QueryParam("login") String login, @QueryParam("password") String password, @Context HttpServletRequest req) {
        HttpSession session = req.getSession(true);

        List result = findByNameAndPassword(login, password);

        User user;
        if (result.size() > 0) {
            user = (User) result.get(0);
            session.setAttribute("userid", user.getId());
            session.setAttribute("userlogin", user.getLogin());
            return Response.ok(user).build();
        } else {
            session.setAttribute("userid", "-1");
            session.setAttribute("userlogin", "");
            return Response.status(Status.FORBIDDEN).build();            
        }
    }
    
    @GET
    @Path("logout")
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Response logout(@Context HttpServletRequest req) {
        HttpSession session = req.getSession(true);
        session.setAttribute("userid", "-1");
        session.setAttribute("userlogin", "");
        return Response.ok().build();
    }

    @GET
    @Path("isConnected")
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Response isConnected(@Context HttpServletRequest req) {
        if (!isLoggedIn(req)) {
            return Response.ok(Boolean.toString(false)).build();
        }        
        return Response.ok(Boolean.toString(true)).build();
    }

    private List findByNameAndPassword(String login, String password) {
        return em.createQuery(
                "SELECT u FROM User u WHERE u.login = :login AND u.password = :password")
                .setParameter("login", login)
                .setParameter("password", password)
                .getResultList();
    }
    
    private List findByName(String login) {
        return em.createQuery(
                "SELECT u FROM User u WHERE u.login LIKE :login")
                .setParameter("login", login)
                .getResultList();
    }

    private boolean isLoginAvailable(String login, Integer id) {
        return em.createQuery(
                "SELECT u FROM User u WHERE u.login LIKE :login AND u.id != :id")
                .setParameter("id", id)
                .setParameter("login", login)
                .getResultList()
                .isEmpty();
    }
}
