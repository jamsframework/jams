/*
 * C1.java
 * Created on 31. Mai 2007, 11:34
 *
 * This file is part of JAMS
 * Copyright (C) 2007 FSU Jena
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

package jams.remote.testing;

import jams.remote.client.Client;

/**
 *
 * @author Sven Kralisch
 */
public class RemoteServer {
    
    public static void main(String[] args) throws Exception {
        Client test = new Client("141.35.159.80", 9000, "sven", "");
        
        //System.out.println("Dir listing " + test.getDirListing("/home/cluster"));
        //test.pushDir("/home/cluster/remoteruntime/lib", "C:\\Programme\\jams\\lib");
        //test.pushDir("/home/cluster/remoteruntime/j2k_gehlberg", "D:\\jamsapplication\\j2k_gehlberg", ".cache;.svn");
//        test.pushFile("/home/cluster/remoteruntime/j2k_gehlberg/$model.jam", "D:\\jamsapplication\\j2k_gehlberg\\j2k_gehlberg.jam");
        
        test.runJAMS("/home/cluster/remoteruntime/j2k_gehlberg", "/home/cluster/remoteruntime/lib", "/home/cluster/remoteruntime/j2k_gehlberg/$model.jam", "1");
/*
        test.pushDir("d:\\temp\\dest\\lib", "C:\\Programme\\jams\\lib");
 
 
        test.getFile("D:\\jamsapplication\\j2k_gehlberg\\data\\tmean.dat", "d:\\temp\\jams\\x1_1.dat");
        test.getFile("D:\\jamsapplication\\j2k_gehlberg\\data\\tmean.dat", "d:\\temp\\jams\\x1_2.dat");
 
        test.runJAMS("D:\\jamsapplication\\j2k_gehlberg", "C:\\Programme\\jams\\lib", "D:\\jamsapplication\\j2k_gehlberg\\j2k_gehlberg.jam", "1");
 
 */
//        test.getFile("D:\\downloads\\netbeans-6.0m9-full-windows.exe", "d:\\x1_3.dat");
//        test.getFile("D:\\downloads\\netbeans-6.0m9-full-windows.exe", "d:\\x1_4.dat");
//        test.getFile("D:\\downloads\\netbeans-6.0m9-full-windows.exe", "d:\\netbeans-6.0m9-full-windows.exe");
//        test.stopServer();
    }
    
    
}
