/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package optas.remote.common;

import java.io.Serializable;

/**
 *
 * @author chris
 */
public class FileInfo implements Serializable{
    private String name;
    private String owner;
    private long job;
    private String host;
    private String path;
    private long size;

    /**
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * @param name the name to set
     */
    public void setName(String name) {
        this.name = name;
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
     * @return the path
     */
    public String getPath() {
        return path;
    }

    /**
     * @param path the path to set
     */
    public void setPath(String path) {
        this.path = path;
    }

    /**
     * @return the size
     */
    public long getSize() {
        return size;
    }

    /**
     * @param size the size to set
     */
    public void setSize(long size) {
        this.size = size;
    }

    /**
     * @return the owner
     */
    public String getOwner() {
        return owner;
    }

    /**
     * @param owner the owner to set
     */
    public void setOwner(String owner) {
        this.owner = owner;
    }

    /**
     * @return the job
     */
    public long getJob() {
        return job;
    }

    /**
     * @param job the job to set
     */
    public void setJob(long job) {
        this.job = job;
    }
}
