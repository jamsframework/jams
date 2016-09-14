/*
 * Controller.java
 * Created on 20.04.2014, 14:48:42
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
package jams.server.client;

import jams.JAMS;
import jams.JAMSProperties;
import jams.data.ArrayDataSupplier;
import jams.runtime.StandardRuntime;
import jams.server.client.error.ErrorHandler;
import jams.server.entities.Job;
import jams.server.entities.User;
import jams.server.entities.Users;
import jams.server.entities.Workspace;
import jams.server.entities.WorkspaceFileAssociation;
import static jams.tools.LogTools.log;
import jams.workspace.JAMSWorkspace;
import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.Map;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ws.rs.ProcessingException;

/**
 *
 * @author Christian Fischer <christian.fischer.2@uni-jena.de>
 */
public class Controller {

    private final String SEPARATOR = "***************************************************\n";
    private final HTTPClient client = new HTTPClient();
    private User user;
    private final String urlStr;

    public final String VERSION = "0.1.0.1";

    /**
     * the constructor ensures the construction of a working controller
     *
     * @param urlStr url to server
     * @param userName
     * @param password
     * @throws ProcessingException
     */
    public Controller(String urlStr, String userName, String password) {
        Logger.getLogger(Controller.class.getName()).setLevel(Level.ALL);
        this.urlStr = urlStr;
        connect(userName, password);
    }

    /**
     * connects the controller to jamscloud
     *
     * @param userName
     * @param password
     * @throws ProcessingException
     */
    private void connect(String userName, String password) {
        log(this.getClass(), Level.FINE, "{0}{1} {2}", SEPARATOR, JAMS.i18n("Trying_to_connect_with"), urlStr);
        String serverVersion = client.httpGet(urlStr + "/version", String.class);
        if (isCompatibleWithServer(serverVersion)) {
            user = (User) client.connect(urlStr + "/user/login?login=" + userName + "&password=" + password, User.class);
            log(this.getClass(), Level.FINE, "{0}\n", JAMS.i18n("Login_successful"));
        } else {
            throw new ProcessingException(JAMS.i18n("Client (Version: %1 is not compatible with Server (Version: %2")
                    .replace("%1", VERSION)
                    .replace("%2", serverVersion));
        }
    }

    /**
     * determines if the client is compatible with a jamscloud server
     *
     * @param serverVersion : JAMSCloud version
     * @return true if it is compatible otherwise false
     */
    public boolean isCompatibleWithServer(String serverVersion) {
        return VERSION.compareTo(serverVersion) == 0;
    }

    /**
     * closes the connection
     *
     * @throws ProcessingException
     */
    public void close() {
        client.disconnect();
    }

    /**
     * checks if the controller is still connected to jamscloud
     *
     * @return if the controller is still connected to jamscloud
     * @throws ProcessingException
     */
    public boolean isConnected() {
        try {
            Object b = client.httpGet(urlStr + "/user/isConnected", String.class);
            return Boolean.parseBoolean(b.toString());
        } catch (NumberFormatException ioe) {
            log(this.getClass(), Level.WARNING, ioe.toString(), ioe);
            return false;
        }
    }

    /**
     * forces a clean up of jamsserver
     *
     * @throws ProcessingException
     */
    public void cleanUp() {
        log(this.getClass(), Level.FINE, "{0}{1}", SEPARATOR, JAMS.i18n("Clean_up_JAMSCloud"));
        client.httpGet(urlStr + "/file/clean", String.class);
    }

    /**
     * the average load of the server in the last 5min
     *
     * @return the average load of the server in the last 5min
     * @throws ProcessingException
     */
    public double getLoad() {
        try {
            String b = client.httpGet(urlStr + "/job/load", String.class);
            return Double.parseDouble(b);
        } catch (NumberFormatException | ProcessingException nfe) {
            log(this.getClass(), Level.WARNING, nfe.toString(), nfe);
            return Double.NaN;
        }
    }

    /**
     * @return the url to jamscloud
     *
     */
    public String getServerURL() {
        return urlStr;
    }

    /**
     * @return the HTTPClient
     *
     */
    public HTTPClient getClient() {
        return client;
    }

    /**
     * @return the user
     *
     */
    public User getUser() {
        return user;
    }

    /**
     * @return a new FileController instance
     */
    public FileController files() {
        return new FileController(this);
    }

    /**
     * @return a new WorkspaceController instance
     */
    public WorkspaceController workspaces() {
        return new WorkspaceController(this);
    }

    /**
     * @return a new UserController instance
     */
    public UserController users() {
        return new UserController(this);
    }

    /**
     * @return a new JobController instance
     */
    public JobController jobs() {
        return new JobController(this);
    }

    public static void main(String[] args) throws IOException {
        Controller client = new Controller("http://localhost:8080/jamscloud/webresources", "admin", "jamscloud");

        User user;
        user = new User(2, "sven", "skralisch");
        user.setEmail("nsk@uni-jena.de");
        user.setName("Sven Kralisch");
        user.setAdmin(1);
        client.users().createUser(user);

        user = new User(3, "annika", "akuenne");
        user.setEmail("annika.kuenne@uni-jena.de");
        user.setName("Annika Künne");
        user.setAdmin(0);
        client.users().createUser(user);

        user = new User(4, "manfred", "mfink");
        user.setEmail("manfred.fink@uni-jena.de");
        user.setName("Manfred Fink");
        user.setAdmin(0);
        client.users().createUser(user);

        user = new User(5, "juliana", "jsantos");
        user.setEmail("juliana.santos@uni-jena.de");
        user.setName("Juliana Santos");
        user.setAdmin(0);
        client.users().createUser(user);
    }

    /**
     *
     * @param args
     * @throws IOException
     */
    public static void main2(String[] args) throws IOException {
        Controller client = new Controller("http://kirk.geogr.uni-jena.de:8080/jams-cloud-server/webresources", "Christian", "jamscloud");

        User user = new User(5);
        user.setAdmin(1);
        user.setEmail("blubb@gmx.de");
        user.setId(5);
        user.setLogin(Integer.toString((new Random()).nextInt()));
        user.setName("Der Marshmellow Mann");
        user.setPassword("test");

        User newUser = client.users().createUser(user);
        if (newUser != null) {
            System.out.println("User was created successfully with id " + newUser.getId());
            newUser = client.users().delete(newUser.getId());
            if (newUser != null) {
                System.out.println("User was successfully deleted");
            }

        }
        Users users = client.users().findAll();
        if (users != null) {
            System.out.println("List of all users is now:" + users.toString());
        }
        Users result = client.users().findInRange(0, 1);
        if (result != null) {
            System.out.println("Users with id 0 and 1 are" + result.toString());
        }

        //Files
        jams.server.entities.File f1 = client.files().uploadFile(new File("E:/tmp/style.css"));
        if (f1 != null) {
            System.out.println("File with id " + f1.getId() + " was uploaded successfully!");
        }
        File f2 = new File("E:/tmp/show_log.php");
        File f3 = new File("E:/tmp/successful.php");
        Map<File, jams.server.entities.File> map = null;
        try {
            map = client.files().uploadFile(new ArrayDataSupplier(new File[]{f2, f3}),
                    new ErrorHandler<File>() {
                @Override
                public boolean handleError(File o, Throwable ex) {
                    System.out.println("Unable to upload " + o);
                    return true;
                }
            }
            );
        } catch (IOException ioe) {
            System.out.println("File with ids " + map.get(f2).getId() + "/" + map.get(f3).getId() + " were uploaded successfully!");
        }
        //Workspace
        Workspace ws = new Workspace();
        ws.setId(0);
        ws.setName("TestWs");
        ws.setCreationDate(new Date());
        ws = client.workspaces().create(ws);
        if (ws != null) {
            System.out.println("Workspace with id " + ws.getId() + " was created successfully!");

            client.workspaces().attachFile(ws, new WorkspaceFileAssociation(ws, f1, 4, "/test/test1.dat"));
            client.workspaces().attachFile(ws, new WorkspaceFileAssociation(ws, map.get(f2), 4, "/test/test2.dat"));
            client.workspaces().attachFile(ws, new WorkspaceFileAssociation(ws, map.get(f3), 4, "/test/test3.dat"));

            //client.getWorkspaceController().downloadFile(new File("E:/tmp/"),ws, f1);
            ws = client.workspaces().delete(ws);
            if (ws != null) {
                System.out.println("Workspace with id " + ws.getId() + " was removed successfully!");
            }
        }

        JAMSWorkspaceUploader uploader = new JAMSWorkspaceUploader(client);
        jams.workspace.Workspace workspace = new JAMSWorkspace(new File("E:\\ModelData\\JAMS-Gehlberg"), new StandardRuntime(null));
        Workspace ws2 = uploader.uploadWorkspace(workspace, new File[]{new File("E:\\JAMS_rep\\JAMS\\lib")}, new File("E:\\JAMS_rep\\JAMS\\nbprojects\\jams-ui\\dist\\jams-ui.jar"), "",
                new ErrorHandler<File>() {
            @Override
            public boolean handleError(File o, Throwable ex) {
                System.out.println("Unable to upload " + o);
                return true;
            }
        });
        if (ws2 != null) {
            System.out.println("Number of files: " + ws2.getFiles().size());
            System.out.println("Workspace of Wilde Gera Model was uploaded successfully with id " + ws2.getId());
            client.workspaces().downloadWorkspace(new File("E:/test_client/" + ws2.getId() + "/"), ws2);
            System.out.println("Workspace of Wilde Gera Model was downloaded to E:/test_client/" + ws2.getId());
            WorkspaceFileAssociation wfaModel = null;
            for (WorkspaceFileAssociation f : ws2.getFiles()) {
                if (f.getPath().endsWith("j2k_gehlberg.jam")) {
                    wfaModel = f;
                    break;
                }
            }
            Job job = client.jobs().create(ws2, wfaModel);
            if (job != null) {
                System.out.println("Wilde Gera Model started successfully! " + "Job Id is: " + job.getId());
            }
            while (client.jobs().getState(job).isActive()) {
                System.out.println("Job with id " + job.getId() + " still running!");
                try {
                    Thread.sleep(500);
                } catch (InterruptedException ex) {
                    ex.printStackTrace();
                }
            }
            System.out.println("Job with id " + job.getId() + " finished after " + client.jobs().getState(job).getDuration() + " ms.");
            Workspace ws3 = job.getWorkspace();
            System.out.println("Workspace of " + ws3.getName() + " Model is downloading!");
            client.jobs().download(new File("E:/test_client/" + ws3.getName()), job);
            client.jobs().delete(job);
            System.out.println("Job with id " + job.getId() + " deleted");
            client.workspaces().delete(ws2);
            System.out.println("Workspace of Wilde Gera Model was deleted!");
        }

        client.close();
    }

    public static void main3(String[] args) throws IOException {
        Controller client = new Controller("http://localhost:8080/jams-cloud-server/webresources", "admin", "jamscloud");

        JAMSWorkspaceUploader uploader = new JAMSWorkspaceUploader(client);
        jams.workspace.Workspace workspace = new JAMSWorkspace(new File("d:\\jamsapplication\\JAMS-Gehlberg"), new StandardRuntime(JAMSProperties.createProperties()));
        Workspace ws2 = uploader.uploadWorkspace(workspace, new File[]{new File("D:\\jams\\jams-bin\\components"), new File("D:\\jamsmodels\\nbprojects\\components")}, new File("d:\\jams\\jams-bin\\jams-headless.jar"), "",
                new ErrorHandler<File>() {
            @Override
            public boolean handleError(File o, Throwable ex) {
                System.out.println("Unable to upload " + o);
                return true;
            }
        });
        if (ws2 != null) {
            System.out.println("Number of files: " + ws2.getFiles().size());
            System.out.println("Workspace of Wilde Gera Model was uploaded successfully with id " + ws2.getId());
            client.workspaces().downloadWorkspace(new File("E:/test_client/" + ws2.getId() + "/"), ws2);
            System.out.println("Workspace of Wilde Gera Model was downloaded to E:/test_client/" + ws2.getId());
            WorkspaceFileAssociation wfaModel = null;
            for (WorkspaceFileAssociation f : ws2.getFiles()) {
                if (f.getPath().endsWith("j2k_gehlberg.jam")) {
                    wfaModel = f;
                    break;
                }
            }
            Job job = client.jobs().create(ws2, wfaModel);
            if (job != null) {
                System.out.println("Wilde Gera Model started successfully! " + "Job Id is: " + job.getId());
            }
            while (client.jobs().getState(job).isActive()) {
                System.out.println("Job with id " + job.getId() + " still running!");
                try {
                    Thread.sleep(500);
                } catch (InterruptedException ex) {
                    ex.printStackTrace();
                }
            }
            System.out.println("Job with id " + job.getId() + " finished after " + client.jobs().getState(job).getDuration() + " ms.");
            Workspace ws3 = job.getWorkspace();
            System.out.println("Workspace of " + ws3.getName() + " Model is downloading!");
            client.jobs().download(new File("E:/test_client/" + ws3.getName()), job);
            client.jobs().delete(job);
            System.out.println("Job with id " + job.getId() + " deleted");
            client.workspaces().delete(ws2);
            System.out.println("Workspace of Wilde Gera Model was deleted!");
        }

        client.close();
    }

}
