/*
 * JobState.java
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
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 *
 * @author Sven Kralisch <sven.kralisch at uni-jena.de>
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "jobState")
public class JobState implements Serializable {
    
    @XmlElement(name = "jobid", type = Long.class)
    private long jobID;
    
    @XmlElement(name = "active", type = Boolean.class)
    private boolean active;
    
    @XmlElement(name = "startDate", type = Date.class)
    private Date startDate;
    
    @XmlElement(name = "duration", type = Long.class)
    private long duration;
    
    @XmlElement(name = "size", type = Long.class)
    private long size;
    
    @XmlElement(name = "progress", type = Float.class)
    private float progress;
                
    public JobState() {
    }
    
    public JobState(long jobID, boolean active, Date startDate) {
        this.jobID = jobID;
        this.active = active;
        this.startDate = startDate;
    }

    public long getJobID(){
        return jobID;
    }
    
    public boolean isActive(){
        return active;                
    }
    
    public Date getStartDate(){
        return startDate;
    }
    
    public long getDuration(){
        return duration;
    }
    
    public long getSize(){
        return size;
    }
    
    public void setJobID(long jobID){
        this.jobID = jobID;
    }
    
    public void setActive(boolean active){
        this.active = active;
    }
    
    public void setStartDate(Date date){
        this.startDate = date;
    }
    
    public void setDuration(long duration){
        this.duration = duration;
    }
    
    public void setSize(long size){
        this.size = size;
    }
    
    @Override
    public String toString() {
        return "jams.server.entities.JobState";
    }    

    /**
     * @return the progress
     */
    public float getProgress() {
        return progress;
    }

    /**
     * @param progress the progress to set
     */
    public void setProgress(float progress) {
        this.progress = progress;
    }
}
