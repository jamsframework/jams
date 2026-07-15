/*
 * FileObject.java
 * Created on 19. Dezember 2008, 13:56
 *
 * This file is part of JAMS
 * Copyright (C) FSU Jena
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
package jams.explorer.tree;

import jams.workspace.dsproc.AbstractDataStoreProcessor;
import java.io.File;

/**
 *
 * @author Sven Kralisch <sven.kralisch at uni-jena.de>
 */
public class FileObject {

    private File file;
    private boolean isValid = false;
    private String[] validExtensions = {"csv", "dat", "txt", "jmp"};

    AbstractDataStoreProcessor processor;

    public FileObject(File file) {
        this.file = file;
        if (file.isFile()) {
            boolean validExt = false;
            for (String ext : validExtensions) {
                if (file.getName().endsWith(ext)) {
                    validExt = true;
                }
            }
            if (validExt) {
                this.processor = AbstractDataStoreProcessor.getProcessor(file);
            }
        }
        if (processor != null) {
            isValid = true;
        }
    }

    public boolean isValid() {
        return isValid;
    }

    private FileObject(File file, AbstractDataStoreProcessor processor) {
        this.file = file;
        this.processor = processor;
    }

    public FileObject[] getSubDataStores() {
        if (processor == null) {
            return new FileObject[0];
        }
        AbstractDataStoreProcessor processors[] = this.processor.getSubDataStores();
        FileObject fileObjects[] = new FileObject[processors.length];

        int i = 0;
        for (AbstractDataStoreProcessor p : processors) {
            fileObjects[i++] = new FileObject(file, p);
        }
        return fileObjects;
    }

    @Override
    public String toString() {
        if (processor == null) {
            return file.getName();
        } else {
            return processor.toString();
        }
    }

    /**
     * @return the file
     */
    public File getFile() {
        return file;
    }

}
