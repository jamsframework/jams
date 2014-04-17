/*
 * PojoClient.java
 * Created on 02.03.2014, 20:45:28
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

import jams.server.entities.Files;
import jams.server.entities.User;
import jams.server.entities.Users;
import jams.server.entities.Workspace;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Date;
import java.util.Random;
import javax.xml.bind.JAXBException;

/**
 *
 * @author Sven Kralisch <sven.kralisch at uni-jena.de>
 */
public class PojoClient {

    String SEPARATOR = "**********************";
    String serverUrl = "";

    HTTPClient client = new HTTPClient();

    boolean connected = false;

    public PojoClient() {
        System.setProperty("sun.net.http.allowRestrictedHeaders", "true");
    }

    private Object httpGet(String url, Class responseType) {
        log(SEPARATOR + "\nSENDING: http request:" + url);
        if (!connected) {
            log("ERROR: User not logged in, not sending request!");
            return null;
        }

        try {
            Object o = client.httpGet(url, responseType);
            if (o instanceof String) {
                log(o.toString());
                return null;
            }
            log("INFO: Request send successful!");
            return o;
        } catch (IOException ioe) {
            log(ioe.toString());
            return null;
        } catch (JAXBException ex) {
            log(ex.toString());
            return null;
        }
    }

    private Object httpPost(String url, String method, Object o, Class type) {
        log(SEPARATOR + "\nSENDING: request: " + method + "@" + url);
        if (!connected) {
            log("ERROR: User not logged in, not sending request!");
            return null;
        }

        try {
            Object result = (Object) client.httpRequest(url, method, o, type);
            if (result instanceof String) {
                log(result.toString());
                return null;
            }
            log("INFO: Request send successful!");
            return result;
        } catch (IOException ioe) {
            log(ioe.toString());
            return null;
        } catch (JAXBException ex) {
            log(ex.toString());
            return null;
        }
    }

    private Object httpUpload(String url, File f, Class type) {
        log(SEPARATOR + "\nSENDING: Upload file request:" + url + " file: " + f);
        if (!connected) {
            log("ERROR: User not logged in, not sending request!");
            return null;
        }

        try {
            Object result = (Object) client.httpFileUpload(url, f, type);
            if (result instanceof String) {
                log(result.toString());
                return null;
            }
            log("INFO: Request send successful!");
            return result;
        } catch (IOException ioe) {
            log(ioe.toString());
            return null;
        } catch (JAXBException ex) {
            log(ex.toString());
            return null;
        }
    }

    public boolean connect(String serverUrl, String userName, String password) {
        this.serverUrl = serverUrl;
        log(SEPARATOR + "\nTrying to connect .. ");
        User u = null;
        try {
            Object o = client.httpGet(serverUrl + "/user/login?login=" + userName + "&password=" + password, User.class);
            if (o instanceof String) {
                log(o.toString());
            } else {
                u = (User) o;
            }
        } catch (IOException ioe) {
            log(ioe.toString());
        } catch (JAXBException ex) {
            log(ex.toString());
        }

        if (u == null) {
            log("ERROR: Connection not established\n");
            return connected = false;
        }
        log("INFO: Login successful\n");
        return connected = true;
    }

    //Client Methods
    public Users getAllUsers() {
        return (Users) httpGet(serverUrl + "/user/all", Users.class);
    }

    public Users getUsersInRange(int from, int to) {
        Object result = httpGet(serverUrl + "/user/" + from + "/" + to, Users.class);
        if (result instanceof String) {
            log(result.toString());
            return null;
        }
        return (Users) result;
    }

    public User getUser(int id) {
        return (User) httpGet(serverUrl + "/user/" + id, User.class);
    }

    public boolean removeUser(int id) {
        boolean success = httpPost(serverUrl + "/user/" + id, "DELETE", null, User.class) != null;
        if (!success) {
            log("FAILED: User was not deleted!");
            return false;
        } else {
            log("SUCCESS: User was was deleted!");
            return true;
        }
    }

    public int createUser(User user) {
        Object result = httpPost(serverUrl + "/user/create", "PUT", user, User.class);
        if (result instanceof String) {
            log("FAILED: User was not created. The server responed with the following message: " + result);
            return -1;
        } else if (result == null) {
            log("FAILED: User was not created. There was no response from the server");
            return -1;
        } else {
            user = (User) (result);
            log("SUCCESS: User was created with id = " + user.getId());
            return user.getId();
        }
    }

    private String getHashCode(java.io.File f) {
        try {
            return org.apache.commons.codec.digest.DigestUtils.md5Hex(new FileInputStream(f));
        } catch (IOException fnfe) {
            fnfe.printStackTrace();
        }
        return null;
    }

    public jams.server.entities.File uploadFile(File f) {
        jams.server.entities.File result = this.findServerFile(f);

        if (result == null) {
            result = (jams.server.entities.File) this.httpUpload(serverUrl + "/file/upload", f, jams.server.entities.File.class);
            if (result != null) {
                log("File upload successful:" + result.toString());
            } else {
                log("File upload failed!");
            }
        } else {
            log("File was already uploaded!");
        }

        return result;
    }

    public jams.server.entities.File[] uploadFile(File f[]) {
        Files filesExisting = this.findServerFile(f);
        jams.server.entities.File files[] = filesExisting.getFiles().toArray(new jams.server.entities.File[0]);
        for (int i = 0; i < files.length; i++) {
            if (files[i] == null) {
                files[i] = uploadFile(f[i]);
            }
        }
        return files;
    }

    public jams.server.entities.File findServerFile(File f) {
        String hashCode = getHashCode(f);

        Files filesIn = new Files(new jams.server.entities.File(0, hashCode));
        Files filesOut = (Files) httpPost(serverUrl + "/file/exists", "POST", filesIn, Files.class);
        if (filesOut != null && !filesOut.getFiles().isEmpty()) {
            jams.server.entities.File result = filesOut.getFiles().get(0);
            if (!result.equals(jams.server.entities.File.NON_FILE)) {
                return result;
            }
        }
        return null;
    }

    public Files findServerFile(File f[]) {
        jams.server.entities.Files files = new jams.server.entities.Files();
        for (int i = 0; i < f.length; i++) {
            files.add(new jams.server.entities.File(0, getHashCode(f[i])));
        }
        Files filesOut = (Files) httpPost(serverUrl + "/file/exists", "POST", files, Files.class);
        for (int i = 0; i < f.length; i++) {
            if (filesOut.getFiles().get(i).equals(jams.server.entities.File.NON_FILE)) {
                filesOut.getFiles().set(i, null);
            }
        }
        return filesOut;
    }

    public int createWorkspace(Workspace ws) {
        Object result = (Workspace) httpPost(serverUrl + "/workspace/create", "PUT", ws, Workspace.class);
        if (result instanceof String) {
            log("FAILED: User was not created. The server responed with the following message: " + result);
            return -1;
        } else if (result == null) {
            log("FAILED: User was not created. There was no response from the server");
            return -1;
        } else {
            ws = (Workspace) (result);
            log("SUCCESS: User was created with id = " + ws.getId());
            return ws.getId();
        }
    }

    public boolean assignFileToWorkspace(Workspace ws, jams.server.entities.File f, int role) {
        Object result = httpGet(serverUrl + "/workspace/assign?WORKSPACE_ID=" + ws.getId() + "&FILE_ID=" + f.getId() + "&ROLE=" + role, jams.server.entities.Workspace.class);
        if (result instanceof String) {
            log("FAILED: Unable to assign file to workspace: " + result);
            return false;
        } else if (result == null) {
            log("FAILED: Unable to assign file to workspace. There was no response from the server");
            return false;
        } else if (result instanceof Workspace) {
            log("SUCCESS: File " + f.getId() + " assigned to workspace " + ws.getId());
            return true;
        }
        return false;
    }

    public void log(String msg) {
        System.out.println(msg);
    }

    public void close() {
        client.close();
    }

    public static void main(String[] args) throws IOException, JAXBException {
        PojoClient client = new PojoClient();
        client.connect("http://localhost:8080/jams-server/webresources", "Blubb", "test");

        User user = new User(5);
        user.setAdmin(1);
        user.setEmail("blubb@gmx.de");
        user.setId(5);
        user.setLogin(Integer.toString((new Random()).nextInt()));
        user.setName("Der Marshmellow Mann");
        user.setPassword("test");

        int id = client.createUser(user);
        if (id != -1) {
            client.removeUser(id);
        }
        Users users = client.getAllUsers();
        if (users != null) {
            System.out.println("List of all users is now:" + users.toString());
        }
        Users result = client.getUsersInRange(0, 1);
        if (result != null) {
            System.out.println("Users with id 0 and 1 are" + result.toString());
        }

        //Files
        jams.server.entities.File f1 = client.uploadFile(new File("E:/tmp/style.css"));
        client.uploadFile(new File[]{new File("E:/tmp/show_log.php"), new File("E:/tmp/successful.php")});

        //Workspace
        Workspace ws = new Workspace();
        ws.setId(0);
        ws.setName("TestWs");
        ws.setCreationDate(new Date());
        int wsID = client.createWorkspace(ws);
        if (wsID > 0) {
            ws.setId(wsID);
            client.assignFileToWorkspace(ws, f1,4);
        }

        client.close();
    }
}
