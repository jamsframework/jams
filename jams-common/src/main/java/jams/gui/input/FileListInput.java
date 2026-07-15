/*
 * ListInput.java
 * Created on 16. Juni 2008, 11:41
 *
 * This file is part of JAMS
 * Copyright (C) 2008 FSU Jena
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public License
 * as published by the Free Software Foundation; either version 3
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, see <http://www.gnu.org/licenses/>.
 *
 */
package jams.gui.input;

import jams.gui.tools.GUIHelper;
import java.io.File;
import javax.swing.*;
import jams.JAMSFileFilter;

/**
 *
 * @author S. Kralisch
 */
public class FileListInput extends ListInput {

    private JFileChooser jfc;

    public FileListInput() {
        super();
        jfc = GUIHelper.getJFileChooser();
        jfc.setFileFilter(JAMSFileFilter.getJarFilter());
        jfc.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
    }

    /**
     * Adds a new item that represents a filename
     */
    @Override
    protected void addItem() {
        // Get the text field value
        String stringValue = null;

        Object o = getListbox().getSelectedValue();
        if (o != null) {
            File file = new File((String) o);
            if (file.exists()) {
                jfc.setSelectedFile(file);
            }
        }

        int result = jfc.showOpenDialog(FileListInput.this);

        if (result == JFileChooser.APPROVE_OPTION) {
            stringValue = jfc.getSelectedFile().getAbsolutePath();
        }
        // add this item to the list and refresh
        if (stringValue != null && !listData.getValue().contains(stringValue)) {
            listData.addElement(stringValue);
            scrollPane.revalidate();
            scrollPane.repaint();
        }
    }

    /**
     * Edit existing item
     */
    @Override
    protected void editItem() {
        //get the current selection
        String newValue = null;

        int selection = getListbox().getSelectedIndex();
        if (selection >= 0) {
            // edit this item
            Object value = listData.getElementAt(selection);
            File file = new File(value.toString());
            if (file.exists()) {
                jfc.setSelectedFile(file);
            }
            int result = jfc.showOpenDialog(FileListInput.this);

            if (result == JFileChooser.APPROVE_OPTION) {
                newValue = jfc.getSelectedFile().getAbsolutePath();
            }
            if (newValue != null && !listData.getValue().contains(newValue)) {
                listData.setElementAt(selection, newValue);
                scrollPane.revalidate();
                scrollPane.repaint();
            }
        }
    }
}
