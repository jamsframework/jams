/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jams.server.client.sync;

import jams.server.client.WorkspaceController;
import jams.server.entities.WorkspaceFileAssociation;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author christian
 */
public class FileSync implements Comparable {

    Logger logger = Logger.getLogger(FileSync.class.getName());
    
    /**
     *
     */
    public enum SyncMode{

        /**
         *
         */
        CREATE,

        /**
         *
         */
        UPDATE,

        /**
         *
         */
        DUPLICATE,

        /**
         *
         */
        NOTHING};
    
    WorkspaceController wc = null;
    DirectorySync parent;
    
    WorkspaceFileAssociation serverFile;
    String localFileName, targetFileName;

    SyncMode syncMode;

    boolean isFileExisting;
    boolean isFileModified;

    boolean doSync = true;

    SimpleDateFormat sdf = new SimpleDateFormat("yyyy_MM_dd_HH_mm_ss");

    /**
     *
     */
    public static abstract class SyncFilter{

        /**
         *
         */
        public SyncFilter(){}

        /**
         *
         * @param fs
         * @return
         */
        abstract public boolean isFiltered(FileSync fs);

        /**
         *
         * @param ds
         * @return
         */
        abstract public boolean isFiltered(DirectorySync ds);
    }
    
    /**
     *
     */
    public FileSync() {
    }

    /**
     *
     * @param client
     * @param parent
     * @param serverFile
     * @param localFileName
     */
    public FileSync(WorkspaceController wc, DirectorySync parent, jams.server.entities.WorkspaceFileAssociation serverFile, String localFileName) {
        this.wc = wc;
        this.parent = parent;
        this.serverFile = serverFile;   
        
        setLocalFileName(localFileName);
        setTargetFileName(localFileName);
    }
        
    /**
     *
     * @return
     */
    public boolean isModified(){
        return isFileModified;
    }
    
    /**
     *
     * @return
     */
    public boolean isExisting(){
        return isFileExisting;
    }
    
    private boolean isIdenticalFile() throws IOException{
        if (isFileExisting) {
            String localHashCode = getLocalHashCode();
            String remoteHashCode = serverFile.getFile().getHash();
            //can happen if local file is directory
            if (localHashCode == null || !serverFile.getFile().isHashValid()) {
                return false;
            }
            /*if (!localHashCode.equals(remoteHashCode)) {
                System.out.println("test");
            }*/
            return localHashCode.equals(remoteHashCode);
        }
        return false;
    }

    private String getLocalHashCode() throws IOException{
        File f = getLocalFile();
        if (f.exists() && f.isFile()) {
            return wc.getFileController().getHashCode(f);
        }
        return null;
    }

    /**
     *
     * @return
     */
    final protected boolean isLocalFileExisting() {
        return getLocalFile().exists();
    }

    /**
     *
     * @return
     */
    public DirectorySync getParent() {
        return parent;
    }
    
    /**
     *
     * @param filter
     */
    public void applySyncFilter(SyncFilter filter){
        setDoSync(filter.isFiltered(this), false);                
    }
    
    /**
     *
     * @param list
     * @return
     */
    public ArrayList<FileSync> getList(ArrayList<FileSync> list){
        if (list == null){
            list = new ArrayList<FileSync>();
        }
        list.add(this);
        return list;
    }

    /**
     *
     * @return
     */
    public boolean isDoSync() {
        return doSync;
    }

    /**
     *
     * @param doSync
     * @param recursive
     */
    public void setDoSync(boolean doSync, boolean recursive) {
        this.doSync = doSync;
        
        if (this.parent != null){
            if (doSync && !this.parent.isDoSync()){
                this.parent.setDoSync(doSync, false);
            }
        }
    }
    
    /**
     *
     */
    protected void markAsModified(){
        this.isFileModified = true;
        if (this.parent!=null){            
            this.parent.markAsModified();
        }
    }
    
    /**
     *
     * @return
     */
    public FileSync getRoot(){
        if (this.parent == null){
            return this;
        }else{
            return parent.getRoot();
        }
    }

    /**
     *
     * @return
     */
    public SyncMode[] getSyncOptions() {
        if (parent == null || parent.getSyncMode() == null || 
            parent.getSyncMode() == SyncMode.UPDATE) {
            if (!isModified()) {
                return new SyncMode[]{SyncMode.NOTHING};
            } else if (isExisting()) {
                return new SyncMode[]{SyncMode.UPDATE, SyncMode.DUPLICATE};
            } else {
                return new SyncMode[]{SyncMode.CREATE};
            }
        }
        if (parent.getSyncMode() == SyncMode.NOTHING){
            return new SyncMode[]{SyncMode.NOTHING};
        }
        if (parent.getSyncMode() == SyncMode.CREATE) {
            return new SyncMode[]{SyncMode.CREATE};
        }
        if (parent.getSyncMode() == SyncMode.DUPLICATE) {
            return new SyncMode[]{SyncMode.CREATE};
        }
        return null;
    }

    /**
     *
     */
    public void updateSyncMode(){
        SyncMode options[] = getSyncOptions();
        
        for (SyncMode option : options) {
            if (option.equals(getSyncMode())) {                
                return;
            }
        }
        setSyncMode(options[0]);
    }
    
    /**
     *
     * @param mode
     * @return
     */
    public boolean setSyncMode(SyncMode mode) {
        SyncMode options[] = getSyncOptions();

        for (SyncMode option : options) {
            if (option.equals(mode)) {
                if (syncMode == SyncMode.DUPLICATE) {
                    setTargetFileName(getLocalFileName());
                }
                this.syncMode = mode;
                if (syncMode == SyncMode.DUPLICATE) {
                    setLocalFileName(getLocalFileName().replace(".", sdf.format(new Date()) + "."));
                }
                return true;
            }
        }
        return false;
    }

    /**
     *
     * @return
     */
    public SyncMode getSyncMode() {
        return syncMode;
    }

    /**
     *
     * @param localFileName
     */
    public void setLocalFileName(String localFileName) {
        this.localFileName = localFileName;
        this.isFileExisting = isLocalFileExisting();
        try{
            this.isFileModified = !isIdenticalFile();
        }catch(IOException ioe){
            logger.log(Level.WARNING, ioe.toString(), ioe);
            this.isFileModified = true;
        }
        
        doSync = false;
        if (isFileExisting && isFileModified){
            doSync = true;
        }
        if (!isFileExisting)
            doSync = true;                
    }
    
    /**
     *
     * @param targetFileName
     */
    public void setTargetFileName(String targetFileName){
        this.targetFileName = targetFileName;
    }
    
    /**
     *
     * @return
     */
    public String getTargetFileName(){
        return targetFileName;
    }

    /**
     *
     * @return
     */
    public String getLocalFileName() {
        return localFileName;
    }

    /**
     *
     * @return
     */
    public File getLocalFile() {
        if (parent != null)
            return (new File(parent.getLocalFile(), localFileName));
        else{
            return new File(localFileName);
        }
    }
    
    /**
     *
     * @return
     */
    public File getTargetFile() {
        if (parent != null)
            return (new File(parent.getTargetFile(), targetFileName));
        else{
            return new File(targetFileName);
        }
    }   

    /**
     *
     * @return
     */
    public WorkspaceFileAssociation getServerFile() {
        return serverFile;
    }

    /**
     *
     * @param root
     * @return
     */
    public boolean synchronizeWorkspace(FileSync root) {
        return true;
    }

    @Override
    public int compareTo(Object o) {
        if ( (o instanceof FileSync && this instanceof FileSync) || 
             (o instanceof DirectorySync && this instanceof DirectorySync)) {
            FileSync f = (FileSync) o;
            
            if (this.getServerFile() == null)
                return 1;
            if (this.getServerFile().getPath() == null)
                return 1;
            if (f.getServerFile() == null)
                return -1;
            if ( f.getServerFile().getPath() == null)
                return -1;
            
            return this.getServerFile().getPath().compareTo(f.getServerFile().getPath());
        }else if (o instanceof FileSync && this instanceof FileSync) {
            return -1;
        }else 
            return 1;
    }
}
