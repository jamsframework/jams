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

package reg.tree;

import java.io.File;

/**
 *
 * @author Sven Kralisch <sven.kralisch at uni-jena.de>
 */
public class FileObject {

    private File file;

    public FileObject(File file) {
        this.file = file;
    }

    @Override
    public String toString() {
        return file.getName();
    }

    /**
     * @return the file
     */
    public File getFile() {
        return file;
    }
    
}
