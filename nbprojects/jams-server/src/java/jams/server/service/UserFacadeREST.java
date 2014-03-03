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
import java.util.List;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;

/**
 *
 * @author Sven Kralisch <sven.kralisch at uni-jena.de>
 */
@Stateless
@Path("user")
public class UserFacadeREST extends AbstractFacade<User> {

    @PersistenceContext(unitName = "jams-serverPU")
    private EntityManager em;

    public UserFacadeREST() {
        super(User.class);
    }

    @POST
    @Consumes({"application/xml", "application/json"})
    public void create(User entity, @Context HttpServletRequest req) {
        if (isAdmin(req)) {
            super.create(entity);
        }
    }

    @PUT
    @Path("{id}")
    @Consumes({"application/xml", "application/json"})
    public void edit(@PathParam("id") Integer id, User entity, @Context HttpServletRequest req) {
        HttpSession session = req.getSession(true);
        Object userid = session.getAttribute("userid");
        if (userid == id) {
            super.edit(entity);
        }
    }

    @DELETE
    @Path("{id}")
    public void remove(@PathParam("id") Integer id, @Context HttpServletRequest req) {
        if (isAdmin(req)) {
            super.remove(super.find(id));
        }
    }

    @GET
    @Path("{id}")
    @Produces({"application/xml", "application/json"})
    public User find(@PathParam("id") Integer id, @Context HttpServletRequest req) {
        if (isAdmin(req)) {
            return super.find(id);
        } else {
            return null;
        }
    }

    @GET
    @Produces({"application/xml", "application/json"})
    public User find(@Context HttpServletRequest req) {
        HttpSession session = req.getSession(true);
        Object userid = session.getAttribute("userid");
        if (userid == null || userid == "-1") {
            return null;
        }
        return super.find(userid);
    }

    @GET
    @Path("all")
    @Produces({"application/xml", "application/json"})
    public List<User> findAll(@Context HttpServletRequest req) {
        if (isAdmin(req)) {
            return super.findAll();
        } else {
            return null;
        }
    }

    @GET
    @Path("{from}/{to}")
    @Produces({"application/xml", "application/json"})
    public List<User> findRange(@PathParam("from") Integer from, @PathParam("to") Integer to, @Context HttpServletRequest req) {
        return super.findRange(new int[]{from, to});
    }

    @GET
    @Path("count")
    @Produces("text/plain")
    public String countREST(@Context HttpServletRequest req) {
        return String.valueOf(super.count());
    }

    @Override
    protected EntityManager getEntityManager() {
        return em;
    }

    @GET
    @Path("login")
    @Produces("text/plain")
    public String login(@QueryParam("login") String login, @QueryParam("password") String password, @Context HttpServletRequest req) {

        HttpSession session = req.getSession(true);

        List result = findWithName(login, password);

        User user;
        if (result.size() > 0) {
            user = (User) result.get(0);
            session.setAttribute("userid", user.getId());
            session.setAttribute("userlogin", user.getLogin());
            return "1";
        } else {
            session.setAttribute("userid", "-1");
            session.setAttribute("userlogin", "");
            return "0";
        }
    }

    private List findWithName(String login, String password) {
        return em.createQuery(
                "SELECT u FROM User u WHERE u.login LIKE :login AND u.password LIKE :password")
                .setParameter("login", login)
                .setParameter("password", password)
                .getResultList();
    }

    private boolean isAdmin(HttpServletRequest req) {
        HttpSession session = req.getSession(true);
        Object userid = session.getAttribute("userid");
        if (userid == null || userid == "-1") {
            return false;
        }

        User user = super.find(userid);

        if (user != null && user.getAdmin() == 1) {
            return true;
        } else {
            return false;
        }
    }

}
