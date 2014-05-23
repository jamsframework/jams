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
import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
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
@Table(name = "job")
@XmlRootElement

public class Job implements Serializable {
    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "id")
    private Integer id;
    
    @ManyToOne(fetch=FetchType.LAZY)
    @JoinColumn(name="ownerID")
    private User owner;
    
    @Basic(optional = false)
    @Column(name = "ownerID", insertable = false, updatable = false)
    private Integer ownerID;
    
    @ManyToOne(fetch=FetchType.EAGER)
    @JoinColumn(name="workspaceID")
    private Workspace workspace;
    
    @ManyToOne(fetch=FetchType.EAGER)
    @JoinColumn(name="modelFileID")
    private WorkspaceFileAssociation modelFile;
        
    @Basic(optional = false)
    @Size(min = 1, max = 500)
    @Column(name = "server")
    private String server;
    
    @Basic(optional = true)
    @Column(name = "pid")
    private Integer PID;
    
    @Temporal(DATE)    
    @Column(name = "startTime")
    private Date startTime;
                
    public Job() {
    }

    public Job(Integer id) {
        this.id = id;
    }

    public Job(Integer id, User owner, Workspace workspace, WorkspaceFileAssociation modelFile) {
        this.id = id;
        this.owner = owner;
        this.workspace = workspace;
        this.modelFile = modelFile;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public User getOwner() {
        return owner;
    }

    public void setOwner(User owner) {
        this.owner = owner;
    }

    public User getOwnerID() {
        return owner;
    }
    
    public Workspace getWorkspace() {
        return workspace;
    }

    public void setWorkspace(Workspace workspace) {
        this.workspace = workspace;
    }
    
    public WorkspaceFileAssociation getModelFile(){
        return modelFile;
    }
    
    public void setModelFile(WorkspaceFileAssociation wfa){
        this.modelFile = wfa;
    }
    
    public String getServer() {
        return server;
    }

    public void setServer(String server) {
        this.server = server;
    }

    public int getPID() {
        return PID;
    }

    public void setPID(int PID) {
        this.PID = PID;
    }
    
    public void setStartTime(Date date){
        this.startTime = date;        
    }
    
    public Date getStartTime(){
        return startTime;
    }
    
    @Override
    public String toString() {
        return "jams.server.entities.Job[ id=" + id + " ]";        
    }    
}
