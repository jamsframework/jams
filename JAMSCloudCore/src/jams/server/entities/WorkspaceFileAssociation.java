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
import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.PrimaryKeyJoinColumn;
import javax.persistence.Table;
import javax.validation.constraints.Size;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

/**
 *
 * @author Sven Kralisch <sven.kralisch at uni-jena.de>
 */

@Table(name = "file2ws")
@XmlRootElement
@NamedQueries({
    @NamedQuery(name = "WorkspaceFileAssociation.findByFile", query = "SELECT u FROM WorkspaceFileAssociation u WHERE u.file_id = :fid")
})
@Entity 
public class WorkspaceFileAssociation implements Serializable, Comparable<WorkspaceFileAssociation> {    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "id")
    private Integer id;
    
    @Column(name = "ws_id")
    private Integer ws_id;
    
    @Column(name = "file_id")
    private Integer file_id;
    
    @Column(name = "role")
    @Basic(optional = true)
    private Integer role;
    
    @Column(name = "path")
    @Size(min = 0, max = 1000)
    @Basic(optional = false)
    private String path;
    
    @XmlTransient
    @ManyToOne
    @PrimaryKeyJoinColumn(name="ws_id", referencedColumnName="ID")
    private Workspace ws;
    
    @ManyToOne
    @PrimaryKeyJoinColumn(name="file_id", referencedColumnName="ID")
    private File file;
    
    public transient final static int ROLE_INPUT = 0;
    public transient final static int ROLE_OUTPUT = 1;
    public transient final static int ROLE_MODEL = 2;
    public transient final static int ROLE_CONFIG = 3;
    public transient final static int ROLE_OTHER = 4;
    public transient final static int ROLE_COMPONENTSLIBRARY = 5;
    public transient final static int ROLE_RUNTIMELIBRARY = 6;
    public transient final static int ROLE_EXECUTABLE = 7;
    public transient final static int ROLE_JAPFILE = 8;
    
    public WorkspaceFileAssociation() {
    }

    public WorkspaceFileAssociation(Workspace ws, File f, String path) {
        this.file_id = f.getId();
        this.ws_id = ws.getId();
        this.file = f;
        this.ws = ws;
        this.role = 0;
        setPath(path);
    }

    public WorkspaceFileAssociation(Workspace ws, File f, int role, String path) {
        this.file_id = f.getId();
        this.ws_id = ws.getId();
        this.file = f;
        this.ws = ws;
        this.role = role;
        setPath(path);
    }

    @XmlTransient //this is important to avoid cyclic references in workspace!!
    public Workspace getWorkspace() {
        return ws;
    }

    public void setWorkspace(Workspace ws) {
        this.ws = ws;
        if (ws!=null)
            this.ws_id = ws.getId();
        else
            this.ws_id = 0;
    }

    public Integer getID(){
        return id;
    }
    
    public void setID(Integer id){
        this.id = id;
    }
    
    @XmlElement(name = "file", type = File.class)
    public File getFile() {
        return file;
    }

    public void setFile(File f) {
        this.file = f;
        if (f != null){
            this.file_id = file.getId();
        }else{
            this.file_id = 0;
        }
    }
        
    public void setRole(int role){
        this.role = role;
    }
    
    public int getRole(){
        return role;
    }
    
    public void setPath(String path){        
        this.path = FileTools.normalizePath(path);
    }
    
    public String getPath(){
        return path;
    }
    
    public String getFileExtension(){
        //get type
        int lastDot = path.lastIndexOf(".");        
        if (lastDot != -1){
            return path.substring(lastDot, path.length());
        }
        return "";
    }
    
    public String getFileName(){
        int index = Math.max(path.lastIndexOf("/"), path.lastIndexOf("\\"));
        if (index == -1){
            return path;
        }else{
            return path.substring(index+1, path.length());
        }
    }
    
    public String getFileDirectory(){
        int lastSlash = Math.max(path.lastIndexOf("/"), path.lastIndexOf("\\"));
        if (lastSlash != -1){
            return path.substring(0, lastSlash+1);
        }else{
            return "/";
        }
    }
    
    @Override
    public int hashCode() {
        int hash = 0;
        hash += (ws_id != null ? ws_id : 0);
        hash += (file_id != null ? file_id : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof WorkspaceFileAssociation)) {
            return false;
        }
        return compareTo((WorkspaceFileAssociation)object)==0;
    }

    @Override
    public String toString() {
        return "jams.server.entities.WorkspaceFileAssociation[ ws->file = [" + ws_id + "->" + file_id + "]";
    }

    @Override
    public int compareTo(WorkspaceFileAssociation o) {
        return Integer.compare(this.getID(), o.getID());
    }
}
