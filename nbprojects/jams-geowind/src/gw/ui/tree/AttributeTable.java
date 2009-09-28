/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package gw.ui.tree;

import java.io.File;

/**
 *
 * @author Ian Overgard
 */
public class AttributeTable {

    public File file;

    public AttributeTable(File f) {
        setFile(f);
    }

    public void setFile(File f) {
        file = f;
    }

    public File getFile() {
        return file;
    }

    @Override
    public String toString() {
        return file.getName();
    }
}
