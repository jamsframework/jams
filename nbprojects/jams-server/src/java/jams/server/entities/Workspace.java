/*
 * User.java
 * Created on 01.03.2014, 21:30:28
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

package jams.server.entities;

import java.io.Serializable;
import java.util.Date;
import java.util.List;
import javax.persistence.Basic;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Temporal;
import static javax.persistence.TemporalType.DATE;
import javax.validation.constraints.Size;
import javax.xml.bind.annotation.XmlRootElement;

/**
 *
 * @author Sven Kralisch <sven.kralisch at uni-jena.de>
 */
@Entity
@Table(name = "workspace")
@XmlRootElement
@NamedQueries({
    @NamedQuery(name = "Workspace.findById", query = "SELECT u FROM Workspace u WHERE u.id = :id"),    
    @NamedQuery(name = "Workspace.findByUserId", query = "SELECT u FROM Workspace u WHERE u.user = :id")
})

public class Workspace implements Serializable {
    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "id")
    private Integer id;
    
    @Basic(optional = false)
    @Size(min = 1, max = 45)
    @Column(name = "name")
    private String name;

    @Temporal(DATE)    
    @Column(name = "creation")    
    private Date creation;
    
    @ManyToOne(fetch=FetchType.LAZY)
    @JoinColumn(name="ownerID")
    private User user;
               
    @OneToMany(mappedBy="ws", cascade = CascadeType.PERSIST)
    private List<WorkspaceFileAssociation> files;
        
    public Workspace() {
        init();
    }

    public Workspace(Integer id) {
        this.id = id;
        init();
    }

    public Workspace(Integer id, String name) {
        this.id = id;
        this.name = name;
        
        init();
    }

    private void init(){
        setCreationDate(new Date());        
    }
    
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Date getCreationDate(){
        return this.creation;
    }
    
    public void setCreationDate(Date date){
        this.creation = date;
    }
    
    public void setUser(User u){
        this.user = u;
    }
    
    public User getUser(){
        return user;
    }
    
    public WorkspaceFileAssociation assignFile(File f, int role){
        WorkspaceFileAssociation wfa = new WorkspaceFileAssociation(this, f, role);        
        if (!files.contains(wfa))
            this.files.add(wfa);
        else{
            int i = files.lastIndexOf(wfa);
            files.get(i).setRole(role);
        }
        return wfa;
    }
    
    @Override
    public int hashCode() {
        int hash = 0;
        hash += (id != null ? id.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof Workspace)) {
            return false;
        }
        Workspace other = (Workspace) object;
        if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "jams.server.entities.Workspace[ id=" + id + " ]";
    }
}
