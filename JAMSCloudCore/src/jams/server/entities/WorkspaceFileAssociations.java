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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 *
 * @author Sven Kralisch <sven.kralisch at uni-jena.de>
 */
@XmlRootElement(name = "workspacefileassociatons")
@XmlAccessorType(XmlAccessType.FIELD)
public class WorkspaceFileAssociations {    
    @XmlElement(name = "workspacefileassociatons", type = WorkspaceFileAssociation.class)
    private List<WorkspaceFileAssociation> workspaceFileAssociatons = new ArrayList<>();
 
    public WorkspaceFileAssociations() {}
 
    public WorkspaceFileAssociations(Collection<WorkspaceFileAssociation> workspaceFileAssociatons) {
        this.workspaceFileAssociatons.addAll(workspaceFileAssociatons);
    }
    
    public WorkspaceFileAssociations(WorkspaceFileAssociation workspaceFileAssociaton) {
        workspaceFileAssociatons.add(workspaceFileAssociaton);        
    }
 
    public void add(WorkspaceFileAssociation workspaceFileAssociaton){
        workspaceFileAssociatons.add(workspaceFileAssociaton);
    }
        
    public void setWorkspaces(List<WorkspaceFileAssociation> workspaceFileAssociatons){
        this.workspaceFileAssociatons = workspaceFileAssociatons;
    }
    
    public List<WorkspaceFileAssociation> getWorkspaceFileAssociations() {
        return workspaceFileAssociatons;
    }
 
    @Override
    public String toString(){
        return Arrays.toString(workspaceFileAssociatons.toArray());
    }   
}
