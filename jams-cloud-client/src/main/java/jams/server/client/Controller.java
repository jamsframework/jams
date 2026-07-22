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
import jams.server.entities.User;
import static jams.tools.LogTools.log;
import java.io.File;
import java.io.IOException;
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
            user = (User) client.login(urlStr + "/user/login", userName, password, User.class);
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

    /**
     * Small admin utility against the server. With a {@code <users>} XML file
     * (first argument, or a "users.xml" in the working directory) it mass-creates
     * the users in it, skipping logins that already exist so it can be re-run
     * safely. Without such a file it runs a single-user lifecycle demo
     * (create/search/edit/delete). Passwords are hashed server-side.
     */
    public static void main(String[] args) throws Exception {
        Controller client = new Controller(
                "http://localhost:8080/jamscloud/webresources", "admin", "my_secret_pw");

        // Mass-create from a <users> file, if one is given / present (repeatable).
        // Without an argument, look in the working directory and the client module,
        // so it works both from IntelliJ and from the reactor root.
        File usersFile = null;
        if (args.length > 0) {
            usersFile = new File(args[0]);
        } else {
            for (String candidate : new String[]{"users.xml", "jams-cloud-client/users.xml"}) {
                File f = new File(candidate);
                if (f.exists()) {
                    usersFile = f;
                    break;
                }
            }
        }
        if (usersFile != null && usersFile.exists()) {
            int n = client.users().createUsers(usersFile);
            System.out.println("Mass-create from " + usersFile + ": " + n + " user(s) created.");
            return;
        }

        // --- otherwise: single-user lifecycle example ---
        // 1. create a user (the password is hashed server-side)
        User user = new User(0, "sven", "my_secret_pw");
        user.setName("Sven Kralisch");
        user.setEmail("kralisch@gmail.com");
        user.setAdmin(1);
        User created = client.users().createUser(user);
        System.out.println("Created user '" + created.getLogin() + "' with id " + created.getId());

        // 2. search for the user by login among all users
        for (User u : client.users().findAll().getUsers()) {
            if ("sven".equals(u.getLogin())) {
                user = u;
                break;
            }
        }
        System.out.println("Found user '" + user.getLogin() + "' with id " + user.getId());

        // 3. edit the user (an empty/null password keeps the current one)
        user.setName("Sven K.");
        user.setEmail("sven@example.org");
        User updated = client.users().update(user);
        System.out.println("Updated user, name is now '" + updated.getName() + "'");

        // 4. delete the user
        client.users().delete(user.getId());
        System.out.println("Deleted user with id " + user.getId());
    }
}
