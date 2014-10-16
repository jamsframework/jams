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

import jams.tools.FileTools;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import javax.persistence.Basic;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EntityManager;
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
import javax.xml.bind.annotation.XmlElement;
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
    @NamedQuery(name = "Workspace.findByUserId", query = "SELECT u FROM Workspace u WHERE u.user.id = :id"),    
})

public class Workspace implements Serializable, Comparable<Workspace> {
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
    
    @Basic(optional = true)
    @Column(name = "readonly")
    private Integer readOnly;
    
    @Basic(optional = true)
    @Column(name = "workspaceSize")
    private Long workspaceSize;
    
    @ManyToOne(fetch=FetchType.LAZY)
    @JoinColumn(name = "ancestor")
    private Workspace ancestor;
        
    @OneToMany(mappedBy="ws", cascade = {CascadeType.PERSIST}, orphanRemoval=true)
    private List<WorkspaceFileAssociation> files = new ArrayList<WorkspaceFileAssociation>();
                
    public Workspace() {
        init();
    }
    
    public Workspace(Workspace ws) {
        this.creation = new Date();
        this.files = new ArrayList<>(Arrays.asList(ws.files.toArray(new WorkspaceFileAssociation[0])));
        this.id = 0;
        this.name = ws.getName();
        this.readOnly = ws.readOnly;
        this.user = ws.user;
        this.ancestor = ws;
        this.workspaceSize = ws.getWorkspaceSize();
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
    
    public boolean isReadOnly(){
        return readOnly!=null&&readOnly>0;
    }
    
    public void setReadOnly(boolean readOnly){
        this.readOnly = readOnly ? 1 : 0;
    }
    
    @XmlElement(name = "WorkspaceFileAssociation", type = WorkspaceFileAssociation.class)
    public List<WorkspaceFileAssociation> getFiles(){
        return files;
    }
    
    public void setFiles(List<WorkspaceFileAssociation> files){
        this.files = files;
        
        for (WorkspaceFileAssociation wfa : getFiles()){
            wfa.setWorkspace(this);
        }
    }
    
    public WorkspaceFileAssociation getFile(String path){
        path = FileTools.normalizePath(path);
        for (WorkspaceFileAssociation wfa : files){
            if (wfa.getPath().equalsIgnoreCase(path))
                return wfa;
        }
        return null;
    }
    
    public WorkspaceFileAssociation assignFile(File f, int role, String path){
        WorkspaceFileAssociation wfa = new WorkspaceFileAssociation(this, f, role, path);        
        //delete all other occurences of that file .. 
        detachFile(path);
        files.add(wfa);                
        updateSize();
        return wfa;
    }
    
    public File detachFile(String path){
        path = FileTools.normalizePath(path);
        for (WorkspaceFileAssociation wfa : files){
            if (wfa.getPath().equals(path)){
                files.remove(wfa);
                wfa.setWorkspace(null);
                updateSize();
                return wfa.getFile();
            }
        } 
        return null;
    }
    
    public void detachAllFiles(){
        while(files.size()>0){
            WorkspaceFileAssociation wfa = files.get(0);
            files.remove(wfa);
            wfa.setWorkspace(null);
        } 
    }
        
    public Workspace getAncestor(){
        return ancestor;
    }
    
    public void setAncestor(Workspace ancestor){
        this.ancestor = ancestor;
    }
    
    private void updateSize(){
        this.workspaceSize = 0L;
        if (files==null)
            return;
        for (WorkspaceFileAssociation wfa : files){
            workspaceSize += wfa.getFile().getFileSize();
        }
    }
    
    public long getWorkspaceSize(){        
        if (workspaceSize==null)
            updateSize();
        return workspaceSize;
    }
    
    public void setWorkspaceSize(long size){
        this.workspaceSize = size;        
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
        return compareTo(other)==0;
    }

    @Override
    public String toString() {
        return "jams.server.entities.Workspace[ id=" + id + " ]";
    }

    @Override
    public int compareTo(Workspace o) {        
        return Integer.compare(getId(), o.getId()); 
    }    
}
