/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package optas.remote.common;

import java.io.Serializable;
import java.util.Date;

/**
 *
 * @author chris
 */
public class JobState implements Serializable{
    private long   id;
    private String description;
    private boolean isRunning;
    private long startTime;
    private String host;
    private String workspace;

    @Override
    public String toString(){
        return id + "\t" + startTime + "\t" + host + "\t" + isRunning + "\t" + description;
    }
    /**
     * @return the id
     */
    public long getId() {
        return id;
    }

    /**
     * @param id the id to set
     */
    public void setId(long id) {
        this.id = id;
    }

    /**
     * @param description the description to set
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * @return the isRunning
     */
    public boolean isIsRunning() {
        return isRunning;
    }

    /**
     * @param isRunning the isRunning to set
     */
    public void setIsRunning(boolean isRunning) {
        this.isRunning = isRunning;
    }

    /**
     * @return the startTime
     */
    public long getStartTime() {
        return startTime;
    }
    public Date getStartDate() {
        return new Date(startTime*1000);
    }
    /**
     * @param startTime the startTime to set
     */
    public void setStartTime(long startTime) {
        this.startTime = startTime;
    }

    /**
     * @return the host
     */
    public String getHost() {
        return host;
    }

    /**
     * @param host the host to set
     */
    public void setHost(String host) {
        this.host = host;
    }

    /**
     * @return the description
     */
    public String getDescription() {
        return description;
    }

    /**
     * @return the workspace
     */
    public String getWorkspace() {
        return workspace;
    }

    /**
     * @param workspace the workspace to set
     */
    public void setWorkspace(String workspace) {
        this.workspace = workspace;
    }

    //data size?
}
