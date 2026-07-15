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
import java.net.URL;
import java.util.Date;
import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import javax.persistence.Temporal;
import static javax.persistence.TemporalType.DATE;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import javax.xml.bind.annotation.XmlRootElement;

/**
 *
 * @author Sven Kralisch <sven.kralisch at uni-jena.de>
 */
@Entity
@Table(name = "files")
@XmlRootElement
@NamedQueries({
    @NamedQuery(name = "File.findById", query = "SELECT u FROM File u WHERE u.id = :id"),
    @NamedQuery(name = "File.findByHash", query = "SELECT u FROM File u WHERE u.hash = :hash"),
    @NamedQuery(name = "File.findUnused", query = "SELECT f FROM File f LEFT join WorkspaceFileAssociation wfa ON (f.id = wfa.file_id) WHERE wfa.file_id is null")
})
public class File implements Serializable {
    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "id")
    private Integer id;
    
    @Basic(optional = false)
    @NotNull
    @Size(min = 1, max = 50)
    @Column(name = "hash")
    private String hash;

    @Temporal(DATE)    
    @Column(name = "creation")    
    private Date creation;
    
    @Basic(optional = false)    
    @Column(name = "serverLocation")
    private String location;
    
    @Basic(optional = true)    
    @Column(name = "fileSize")
    private Long fileSize;
    
    public static final File NON_FILE = new File(0,"0");
    
    public File() {
        init();
    }

    public File(Integer id) {
        this.id = id;
        init();
    }

    public File(Integer id, String hash) {
        this.id = id;
        this.hash = hash;
        
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

    public String getHash() {
        return hash;
    }

    public void setHash(String hash) {
        this.hash = hash;
    }

    public boolean isHashValid(){
        return !hash.isEmpty() && !hash.equals("0");
    }
    
    public Date getCreationDate(){
        return this.creation;
    }
    
    public void setCreationDate(Date date){
        this.creation = date;
    }
    
    public String getLocation(){
        return this.location;
    }
    
    public void setLocation(String location){
        this.location = location;
        
        if (getLocation()==null)
            return;        
        java.io.File f = new java.io.File(this.getLocation());
        if (f.exists())           
            fileSize = f.length();
    }
    
    public long getFileSize(){        
        return fileSize == null ? 0 : fileSize;        
    }
    
    public void setFileSize(long size){
        this.fileSize = size;
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
        if (!(object instanceof File)) {
            return false;
        }
        File other = (File) object;
        if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "jams.server.entities.Files[ id=" + id + " ]";
    }
}
