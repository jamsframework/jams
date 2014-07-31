/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jams.server.client.sync;

import jams.server.client.WorkspaceController;
import jams.tools.FileTools;
import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.Set;
import java.util.TreeSet;

/**
 *
 * @author christian
 */
public class DirectorySync extends FileSync {

    TreeSet<FileSync> children = new TreeSet<>();

    public DirectorySync(WorkspaceController wc, DirectorySync parent, File localDirectory) {
        this.parent = parent;
        this.wc = wc;
        
        if (parent != null){
            this.localFileName = localDirectory.getName();
        }else{
            this.localFileName = localDirectory.getPath();
        }
        
        setTargetFileName(localFileName);
        this.serverFile = null;
        this.isFileExisting = super.isLocalFileExisting();
        this.isFileModified = false;                
    }

    @Override
    public ArrayList<FileSync> getList(ArrayList<FileSync> list){
        if (list == null){
            list = new ArrayList<>();
        }
        list.add(this);
        for (FileSync fs : children){
            fs.getList(list);
        }
        return list;
    }
    
    public Set<FileSync> getChildren() {
        return children;
    }

    public void createSyncEntry(String path, jams.server.entities.WorkspaceFileAssociation serverFile) {
        path = FileTools.normalizePath(path);
        int index = path.indexOf("/");
                        
        if (index == -1) {
            addFileSync(new FileSync(wc, this, serverFile, path));
        } else {
            String subDirName = path.substring(0, index);
            doSync = false;
            for (FileSync fs : children) {
                if (fs.getLocalFileName().equals(subDirName)) {
                    ((DirectorySync)fs).createSyncEntry(path.substring(index + 1), serverFile);
                    return;
                }
                doSync |= fs.doSync;
            }
            File subDirectory = new File(getLocalFile(), subDirName);
            addFileSync(new DirectorySync(wc, this, subDirectory));            
        }
    }

    @Override
    public void setDoSync(boolean doSync, boolean recursive) {
        super.setDoSync(doSync, recursive);
        
        if (recursive) {
            for (FileSync fs : children) {
                fs.setDoSync(doSync, true);
            }
        }
    }
            
    private void addFileSync(FileSync filesync) {
        if (filesync.isModified())
            markAsModified();
        this.children.add(filesync);
    }

    @Override
    public void applySyncFilter(SyncFilter filter){
        setDoSync(filter.isFiltered(this), false);
        
        for (FileSync fs : children){
            fs.applySyncFilter(filter);
        }
    }
    
    @Override
    public boolean setSyncMode(SyncMode mode) {
        SyncMode options[] = getSyncOptions();

        for (SyncMode option : options) {
            if (option.equals(mode)) {                                
                this.syncMode = mode;                
                
                if (syncMode == SyncMode.DUPLICATE) {
                    if (getLocalFileName().contains(".")){
                        setTargetFileName(getLocalFileName().replace(".", sdf.format(new Date()) + "."));
                    }else{
                        setTargetFileName(getLocalFileName() + "_" + sdf.format(new Date()));
                    }
                }else{
                    setTargetFileName(getLocalFileName());
                }                
                
                for (FileSync fs : children){
                    fs.updateSyncMode();
                }
                return true;
            }
        }
        return false;
    }        
}
