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
import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.IdClass;
import javax.persistence.ManyToOne;
import javax.persistence.PrimaryKeyJoinColumn;
import javax.persistence.Table;
import javax.xml.bind.annotation.XmlRootElement;

/**
 *
 * @author Sven Kralisch <sven.kralisch at uni-jena.de>
 */

@Table(name = "file2ws")
@XmlRootElement
@Entity 
public class WorkspaceFileAssociation implements Serializable {    
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
    
    @ManyToOne
    @PrimaryKeyJoinColumn(name="ws_id", referencedColumnName="ID")
    private Workspace ws;
    
    @ManyToOne
    @PrimaryKeyJoinColumn(name="file_id", referencedColumnName="ID")
    private File file;
    
    public WorkspaceFileAssociation() {
    }

    public WorkspaceFileAssociation(Workspace ws, File f) {
        this.file_id = f.getId();
        this.ws_id = ws.getId();
        this.file = f;
        this.ws = ws;
        this.role = 0;
    }

    public WorkspaceFileAssociation(Workspace ws, File f, int role) {
        this.file_id = f.getId();
        this.ws_id = ws.getId();
        this.file = f;
        this.ws = ws;
        this.role = role;
    }

    public Workspace getWorkspace() {
        return ws;
    }

    public void setWorkspace(Workspace ws) {
        this.ws = ws;
    }
    
    public File getFile() {
        return file;
    }

    public void setFile(File f) {
        this.file = f;
    }
        
    public void setRole(int role){
        this.role = role;
    }
    
    public int getRole(){
        return role;
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
        WorkspaceFileAssociation other = (WorkspaceFileAssociation) object;
        if ((this.ws_id == null && other.ws_id != null) || (this.ws_id != null && !this.ws_id.equals(other.ws_id))) {            
            return false;
        }
        if ((this.file_id == null && other.file_id != null) || (this.file_id != null && !this.file_id.equals(other.file_id))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "jams.server.entities.WorkspaceFileAssociation[ ws->file = [" + ws_id + "->" + file_id + "]";
    }
}
