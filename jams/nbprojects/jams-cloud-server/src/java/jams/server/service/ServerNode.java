/*
 * ServerNode.java
 * Created on 16.10.2014, 21:27:32
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

package jams.server.service;

import java.io.File;

/**
 *
 * @author christian
 */
public class ServerNode {
    String nodeName;
    File localMountingPoint;
    String user;
    String password;
    
    public ServerNode(String nodeName, File localMount, String user, String password){
        this.nodeName = nodeName;
    }
}
