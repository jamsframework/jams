/*
 * AbstractFacade.java
 * Created on 01.03.2014, 21:31:22
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
import java.util.List;
import javax.persistence.EntityManager;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.ws.rs.core.Context;

/**
 *
 * @author Sven Kralisch <sven.kralisch at uni-jena.de>
 */
public abstract class AbstractFacade<T> {
    private Class<T> entityClass;

    public AbstractFacade(Class<T> entityClass) {
        this.entityClass = entityClass;
    }

    protected abstract EntityManager getEntityManager();

    public boolean create(T entity) {        
        getEntityManager().persist(entity);
        getEntityManager().flush();
        getEntityManager().refresh(entity);
        return true;
    }

    public void edit(T entity) {
        getEntityManager().merge(entity);
    }

    public void remove(T entity) {
        getEntityManager().remove(getEntityManager().merge(entity));
    }

    public T find(Object id) {
        return getEntityManager().find(entityClass, id);
    }

    public List<T> findAll() {
        javax.persistence.criteria.CriteriaQuery cq = getEntityManager().getCriteriaBuilder().createQuery();
        cq.select(cq.from(entityClass));
        return getEntityManager().createQuery(cq).getResultList();
    }

    public List<T> findRange(int[] range) {
        javax.persistence.criteria.CriteriaQuery cq = getEntityManager().getCriteriaBuilder().createQuery();
        cq.select(cq.from(entityClass));
        javax.persistence.Query q = getEntityManager().createQuery(cq);
        q.setMaxResults(range[1] - range[0] + 1);
        q.setFirstResult(range[0]);
        return q.getResultList();
    }

    public int count() {
        javax.persistence.criteria.CriteriaQuery cq = getEntityManager().getCriteriaBuilder().createQuery();
        javax.persistence.criteria.Root<T> rt = cq.from(entityClass);
        cq.select(getEntityManager().getCriteriaBuilder().count(rt));
        javax.persistence.Query q = getEntityManager().createQuery(cq);
        return ((Long) q.getSingleResult()).intValue();
    }
    
    public File findFileByID(Integer id){
        return getEntityManager().find(File.class, id);
    }
    
     public User getCurrentUser(@Context HttpServletRequest req) {
        HttpSession session = req.getSession(true);
        Object userid = session.getAttribute("userid");
        if (userid == null || userid == "-1") {
            return null;
        }
        return getEntityManager().find(User.class, userid);
    }
    
     public boolean isLoggedIn(HttpServletRequest req) {
        HttpSession session = req.getSession(true);
        Object userid = session.getAttribute("userid");
        if (userid == null || userid == "-1") {
            return false;
        }
        return true;
    }
     
    public boolean isAdmin(HttpServletRequest req) {
        User user = getCurrentUser(req);
        
        return user != null && user.getAdmin() == 1;
    }
}
