/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jams.server.client;

import java.io.File;

/**
 *
 * @author christian
 */
public class WorkspaceFile implements Comparable<WorkspaceFile> {

    private final File localFile;
    private final String relativPath;
    private final int role;

    /**
     *
     * @param localFile
     * @param role
     * @param relativePath
     */
    public WorkspaceFile(File localFile, int role, String relativePath) {
        this.localFile = localFile;
        this.relativPath = relativePath;
        this.role = role;
    }

    /**
     *
     * @return
     */
    public File getLocalFile() {
        return localFile;
    }

    /**
     *
     * @return
     */
    public int getRole() {
        return role;
    }

    /**
     *
     * @return
     */
    public String getRelativePath() {
        return relativPath;
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof WorkspaceFile) {
            return compareTo((WorkspaceFile) o) == 0;
        }
        return false;
    }

    @Override
    public int compareTo(WorkspaceFile o) {
        return this.getRelativePath().compareToIgnoreCase(o.getRelativePath());
    }
}
