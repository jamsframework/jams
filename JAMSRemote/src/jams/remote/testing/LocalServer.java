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
 * @author S. Kralisch
 */
public class LocalServer {
    
    public static void main(String[] args) throws Exception {
        Client test = new Client("localhost", 9000, "sven", "");
        
        System.exit(0);
/*
        test.startClient("141.35.159.79", 9000);
 
        test.getFile("/home/cluster/tmean.dat", "d:\\x1_1.dat");
        test.getFile("/home/cluster/tmean.dat", "d:\\x1_2.dat");
        test.getFile("/home/cluster/netbeans-5_5-linux.bin", "d:\\x1_3.dat");
        test.getFile("/home/cluster/netbeans-5_5-linux.bin", "d:\\x1_4.dat");
        test.stopServer();
 */
        
//        test.pushFile("d:\\zzz.dat", "D:\\jamsapplication\\j2k_gehlberg\\data\\tmean.dat");
//        test.createDir("d:\\z1\\y\\x");
        
        System.out.println("File listing " + test.getFileListing("d:\\temp\\jams\\xsrc"));
        System.out.println("Dir listing " + test.getDirListing("d:\\temp\\jams\\xsrc"));
        
        test.getDir("d:\\temp\\jams\\xsrc", "d:\\temp\\dest");
        test.pushDir("d:\\temp\\dest\\lib", "C:\\Programme\\jams\\lib");
        
//        test.pushDir("d:\\temp\\jams\\zsrc", "d:\\xsrc");
//        test.pushDir("d:\\temp\\jams\\zsrc", "d:\\jamsapplication");
        
        test.getFile("D:\\jamsapplication\\j2k_gehlberg\\data\\tmean.dat", "d:\\temp\\jams\\x1_1.dat");
        test.getFile("D:\\jamsapplication\\j2k_gehlberg\\data\\tmean.dat", "d:\\temp\\jams\\x1_2.dat");
        
        test.runJAMS("D:\\jamsapplication\\j2k_gehlberg", "C:\\Programme\\jams\\lib", "D:\\jamsapplication\\j2k_gehlberg\\j2k_gehlberg.jam", "1");
        
        
//        test.getFile("D:\\downloads\\netbeans-6.0m9-full-windows.exe", "d:\\x1_3.dat");
//        test.getFile("D:\\downloads\\netbeans-6.0m9-full-windows.exe", "d:\\x1_4.dat");
//        test.getFile("D:\\downloads\\netbeans-6.0m9-full-windows.exe", "d:\\netbeans-6.0m9-full-windows.exe");
        test.stopServer();
    }
    
    
}
