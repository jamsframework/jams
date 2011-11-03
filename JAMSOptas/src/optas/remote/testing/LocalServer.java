/*
 * C1.java
 * Created on 31. Mai 2007, 11:34
 *
 * This file is part of JAMS
 * Copyright (C) 2007 FSU Jena
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA
 *
 */

package optas.remote.testing;

import optas.remote.common.FileInfo;
import optas.remote.common.JobState;
import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import optas.remote.client.ClientOPTAS;
import optas.remote.server.ServerOPTAS;

/**
 *
 * @author S. Kralisch
 */
public class LocalServer {

    public static void test_Joblisting()throws Exception {
        final ServerOPTAS managementServer = new ServerOPTAS();
        final ServerOPTAS workServer = new ServerOPTAS();

        Thread tManagerThread = new Thread() {
            @Override
            public void run() {
                try{
                    managementServer.startServer("C:/Users/chris/Desktop/web_test/tmp_manager", "localhost:3306", "optas", "root", "truse123", 9000, false, ServerOPTAS.MANAGER);
                }catch(IOException ioe){
                    ioe.printStackTrace();
                }catch(InterruptedException ioe){
                    ioe.printStackTrace();
                }catch(SQLException ioe){
                    ioe.printStackTrace();
                }
            }
        };
       tManagerThread.start();
        
        Thread tWorkerThread = new Thread() {
            @Override
            public void run() {
                try{
                    workServer.startServer("C:/Users/chris/Desktop/web_test/tmp_worker", "localhost:3306", "optas", "root", "truse123", 9001, true, ServerOPTAS.WORKER);
                }catch(IOException ioe){
                    ioe.printStackTrace();
                }catch(InterruptedException ioe){
                    ioe.printStackTrace();
                }catch(SQLException ioe){
                    ioe.printStackTrace();
                }
            }
        };
        //tWorkerThread.start();

        try{
            Thread.sleep(1000);
        }catch(InterruptedException ie){
            System.out.println(ie);
        }
        //Client client = new Client("sonne.geogr.uni-jena.de", 9000, "admin", "borlandc");
        ClientOPTAS client = new ClientOPTAS("localhost", 9000, "admin", "borlandc");
        client.connect();
        ArrayList<JobState> list = client.getJobListing();

        for (JobState state : list){
            System.out.println(state);
            try{
            ArrayList<FileInfo> fileList = client.getFileListing(state);
            if (fileList==null)
                continue;
            for (FileInfo fi : fileList){
                System.out.println(fi.getName() + "\t" + fi.getSize() + "\t" + fi.getHost() + "\t" + fi.getPath());
            }
            }catch(IOException ioe){
                System.out.println(ioe);
            }
        }
        client.getFile(new File("C:/Arbeit/"), 576, "model.png");
    }

    public static void main(String[] args) throws Exception {
        test_Joblisting();
        /*final Server server1 = new Server();
        final Server server2 = new Server();
        Thread t = new Thread() {

            @Override
            public void run() {
                try{                    
                    server1.startServer("C:/Users/chris/Desktop/web_test/tmp_manager", "localhost:3306", "optas", "root", "truse123", 9000, true, Server.MANAGER);
                }catch(IOException ioe){
                    ioe.printStackTrace();
                }catch(InterruptedException ioe){
                    ioe.printStackTrace();
                }catch(SQLException ioe){
                    ioe.printStackTrace();
                }
            }
        };

        Thread t2 = new Thread() {

            @Override
            public void run() {
                try{
                    Server server2 = new Server();
                    server2.startServer("C:/Users/chris/Desktop/web_test/tmp_worker", "localhost:3306", "optas", "root", "truse123", 9001, true, Server.WORKER);
                }catch(IOException ioe){
                    ioe.printStackTrace();
                }catch(InterruptedException ioe){
                    ioe.printStackTrace();
                }catch(SQLException ioe){
                    ioe.printStackTrace();
                }
            }
        };
        t.start();
        try{
            Thread.sleep(1000);
        }catch(InterruptedException ie){
            System.out.println(ie);
        }
        t2.start();
        try{
            Thread.sleep(1000);
        }catch(InterruptedException ie){
            System.out.println(ie);
        }

        Client client = new Client("localhost", 9000, "admin", "borlandc");
        client.connect();
        client.run(new File("C:/Users/chris/Desktop/web_test/exampleWorkspace/"),
                new File("C:/Users/chris/Desktop/web_test/j2k_gehlberg.jam/"), false);


        //Server.stop();
        server1.stop();
        server2.stop();
        System.exit(0);*/
    
    }
    
    
}
